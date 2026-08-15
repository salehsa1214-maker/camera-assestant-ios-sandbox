import Foundation
import UIKit
import DyrectoShared

// MARK: - Suspend-call bridging

/// Runs one exported-Kotlin-suspend call (completion-handler form) synchronously. Only ever used
/// on the coordinator's private single lane / the reference-import queue — NEVER on the Vision
/// worker or main thread — mirroring the Android suspend semantics on a dedicated dispatcher.
// interop: Kotlin suspend funs export as `...(arg, completionHandler:)` and are assumed callable
// from a background thread (Kotlin ≥ 1.9 removed the main-thread-only restriction).
private func awaitSync<T>(_ start: (@escaping (T?, Error?) -> Void) -> Void) -> T? {
    let semaphore = DispatchSemaphore(value: 0)
    var result: T?
    start { value, _ in
        result = value
        semaphore.signal()
    }
    semaphore.wait()
    return result
}

// MARK: - BitmapLuma (port of liveview/reference/ai/BitmapLuma.kt)

/// iOS bridge from a decoded image to the tracker's pure `LumaFrame` working image: downscale to
/// [maxEdge] on the longest side, then Rec.709-weighted luma `(54r + 183g + 19b) >> 8` — the
/// exact Android integer weights.
enum BitmapLuma {
    static func extract(from image: UIImage, maxEdge: Int) -> LumaFrame? {
        guard let extracted = RgbaFrameBuffer.extractScaled(from: image, maxEdge: maxEdge) else {
            return nil
        }
        let w = extracted.width
        let h = extracted.height
        let count = w * h
        let bytes = extracted.data.bytes.assumingMemoryBound(to: UInt8.self)
        // interop: no shared iosMain luma bridge exists; the per-element KotlinIntArray fill below
        // runs on the small tracking image (≤160 px edge, ~14K elements) at tracker cadence only.
        let luma = KotlinIntArray(size: Int32(count))
        var s = 0
        for i in 0..<count {
            let r = Int32(bytes[s])
            let g = Int32(bytes[s + 1])
            let b = Int32(bytes[s + 2])
            luma.set(index: Int32(i), value: (54 * r + 183 * g + 19 * b) >> 8)
            s += 4
        }
        return LumaFrame(width: Int32(w), height: Int32(h), luma: luma)
    }
}

// MARK: - AiPerceptionCoordinator (port of liveview/reference/ai/AiPerceptionCoordinator.kt)

/// Thin sequencing layer over the focused shared perception managers — it decides ONLY what runs
/// when and folds results into a `SceneSnapshot`; every decision rule lives in the shared
/// managers/selectors, never here.
///
/// Live flow (Detection → Tracking → Comparison): per due frame the cheap tracker carries
/// monitoring; the detector runs only when `TrackingManager`'s trigger policy asks (init / loss
/// recovery / scene change / periodic verify). Reference flow: one full pass of detection +
/// embedding + optional segmentation.
///
/// Never-throw contract: [analyzeReference] and [processLiveFrame] fall back to the best snapshot
/// assemblable from whatever capabilities survived; a model failure downgrades the snapshot.
///
/// Threading: all mutable state is confined to the private single [lane] queue — the Swift
/// stand-in for Android's `limitedParallelism(1)` inference scope.
final class AiPerceptionCoordinator: ObservableObject {

    let detection: DetectionManager
    let embedding: EmbeddingManager
    let segmentation: SegmentationManager
    private let strategy: StrategyManager
    let tracking: TrackingManager
    let cadence: AiCadenceConfig

    /// The single inference lane — AiSceneModule hands scaled copies here; reference imports
    /// dispatch-sync onto it so live + import inference never interleave.
    let lane = DispatchQueue(label: "app.dyrecto.ai.inference", qos: .utility)

    @Published private(set) var capabilities = AiCapabilities(
        detection: AiCapability(modelId: "", status: .notLoaded, error: nil),
        embedding: AiCapability(modelId: "", status: .notLoaded, error: nil),
        segmentation: AiCapability(modelId: "", status: .notLoaded, error: nil))

    /// Live AI runs only while reference monitoring wants it.
    private let stateLock = NSLock()
    private var monitoringActive = false
    var liveWorkNeeded: Bool {
        stateLock.lock(); defer { stateLock.unlock() }
        return monitoringActive
    }

    // ---- Live evidence, persisted between inference runs (lane-confined) ----
    private var trackedSubject: SceneSubject?
    private var otherSubjects: [SceneSubject] = []
    private var subjectsAtMs: Int64 = 0
    private var lastEmbedding: VisualEmbeddingResult?
    private var lastEmbeddingAtMs: Int64 = 0
    private var lastVerifiedEmbedding: KotlinFloatArray?
    private var lastVerifiedMeanLuma: Float?

    /// Diagnostics: why the detector last ran, and how often each trigger fired.
    private(set) var lastDetectorTrigger: DetectorTrigger = .none
    private var triggerCountsStore: [DetectorTrigger: Int64] = [:]

    init(detection: DetectionManager,
         embedding: EmbeddingManager,
         segmentation: SegmentationManager,
         strategy: StrategyManager,
         tracking: TrackingManager,
         cadence: AiCadenceConfig = SharedFactory.defaultCadence()) {
        self.detection = detection
        self.embedding = embedding
        self.segmentation = segmentation
        self.strategy = strategy
        self.tracking = tracking
        self.cadence = cadence
    }

    func triggerCounts() -> [DetectorTrigger: Int64] {
        stateLock.lock(); defer { stateLock.unlock() }
        return triggerCountsStore
    }

    /// Pin what live perception should look for and whether monitoring is active at all.
    /// Deactivating clears live evidence.
    // interop: Kotlin nested class SubjectMatcher.Expected exports flattened as
    // SubjectMatcherExpected.
    func setLiveMonitoring(expected: SubjectMatcherExpected?, active: Bool) {
        lane.async {
            self.tracking.expectSubject(expected: expected)
            self.stateLock.lock()
            self.monitoringActive = active
            self.stateLock.unlock()
            if !active { self.resetLiveStateOnLane() }
        }
    }

    /// Forget live evidence (monitoring stopped / reference cleared / new session).
    func resetLiveState() {
        lane.async { self.resetLiveStateOnLane() }
    }

    private func resetLiveStateOnLane() {
        tracking.reset()
        trackedSubject = nil
        otherSubjects = []
        subjectsAtMs = 0
        lastEmbedding = nil
        lastEmbeddingAtMs = 0
        lastVerifiedEmbedding = nil
        lastVerifiedMeanLuma = nil
        stateLock.lock()
        lastDetectorTrigger = .none
        stateLock.unlock()
    }

    // MARK: Reference flow (one-shot, import time)

    /// Full one-shot analysis of a reference image: detection + embedding + one-shot segmentation
    /// statistics (the only place segmentation runs by default). Callable from any background
    /// queue; serialized onto [lane] so it never interleaves with live inference.
    func analyzeReference(image: UIImage, nowMs: Int64) -> SceneSnapshot {
        var snapshot = SceneSnapshot.companion.empty(nowMs: nowMs)
        lane.sync {
            let platform = PlatformImage(handle: image)
            let subjects: [SceneSubject] = awaitSync { done in
                self.detection.detect(bitmap: platform, completionHandler: done)
            } ?? []
            let embeddingResult: VisualEmbeddingResult? = awaitSync { done in
                self.embedding.embed(bitmap: platform, completionHandler: done)
            }
            let segmentationResult: SegmentationResult? = awaitSync { done in
                self.segmentation.segment(bitmap: platform, completionHandler: done)
            }
            snapshot = self.assemble(
                subjects: subjects,
                primaryOverride: nil,
                subjectsAgeMs: 0,
                embeddingResult: embeddingResult,
                embeddingAgeMs: 0,
                segmentationSummary: segmentationResult.map {
                    SegmentationSummary(coverage: $0.coverage, maskBox: $0.maskBox,
                                        pixelAccurate: $0.maskAvailable)
                },
                source: subjects.isEmpty ? nil : .detector,
                nowMs: nowMs)
        }
        publishCapabilities()
        return snapshot
    }

    // MARK: Live flow (lane-confined; the module already holds the busy guard)

    /// One live perception step over an inference-sized copy of the frame (the caller owns it).
    /// MUST be called on [lane]. [runTracker]/[runEmbedding] reflect the module's frame cadence;
    /// the detector decides for itself via the trigger policy.
    func processLiveFrameOnLane(image: UIImage, runTracker: Bool, runEmbedding: Bool,
                                nowMs: Int64) -> SceneSnapshot {
        guard let luma = BitmapLuma.extract(from: image, maxEdge: Int(cadence.trackingMaxEdgePx)) else {
            publishCapabilities()
            return SceneSnapshot.companion.empty(nowMs: nowMs)
        }
        let platform = PlatformImage(handle: image)

        if runEmbedding {
            if let result: VisualEmbeddingResult = awaitSync({ done in
                self.embedding.embed(bitmap: platform, completionHandler: done)
            }) {
                lastEmbedding = result
                lastEmbeddingAtMs = nowMs
            }
        }

        // Scene-change evidence vs the last verified frame, for the trigger policy.
        var embeddingSimilarity: Float?
        if let verified = lastVerifiedEmbedding, let live = lastEmbedding {
            embeddingSimilarity = EmbeddingMath.shared.cosineSimilarity(a: verified, b: live.embedding)
        }
        let meanLuma = luma.meanLuma()
        let lumaDelta: Float? = lastVerifiedMeanLuma.map { abs(meanLuma - $0) }

        let trigger = tracking.detectorTrigger(
            monitoringActive: liveWorkNeeded,
            nowMs: nowMs,
            embeddingSimilarityToVerified: embeddingSimilarity.map { KotlinFloat(value: $0) },
            meanLumaDeltaSinceVerified: lumaDelta.map { KotlinFloat(value: $0) })

        if trigger != .none {
            stateLock.lock()
            lastDetectorTrigger = trigger
            triggerCountsStore[trigger, default: 0] += 1
            stateLock.unlock()

            let detections: [SceneSubject] = awaitSync { done in
                self.detection.detect(bitmap: platform, completionHandler: done)
            } ?? []
            let tracked = tracking.onDetections(frame: luma, detections: detections, nowMs: nowMs)
            trackedSubject = tracked
            // The matched detection is superseded by its tracked version; keep the rest for
            // composition/multi-object evidence.
            if let tracked {
                otherSubjects = detections.filter { $0.boundingBox != tracked.boundingBox }
            } else {
                otherSubjects = detections
            }
            subjectsAtMs = nowMs
            lastVerifiedEmbedding = lastEmbedding?.embedding
            lastVerifiedMeanLuma = meanLuma
        } else if runTracker {
            if let update = tracking.onTrackerFrame(frame: luma) {
                trackedSubject = update
                subjectsAtMs = nowMs
            } else if !tracking.hasActiveTrack {
                trackedSubject = nil
            }
        }

        var subjects: [SceneSubject] = []
        if let trackedSubject { subjects.append(trackedSubject) }
        subjects.append(contentsOf: otherSubjects)

        let source: SnapshotSource?
        if trigger != .none {
            source = .detector
        } else if trackedSubject != nil {
            source = .tracker
        } else {
            source = nil
        }

        let snapshot = assemble(
            subjects: subjects,
            primaryOverride: trackedSubject,
            subjectsAgeMs: subjectsAtMs > 0 ? nowMs - subjectsAtMs : Int64.max,
            embeddingResult: lastEmbedding,
            embeddingAgeMs: lastEmbeddingAtMs > 0 ? nowMs - lastEmbeddingAtMs : Int64.max,
            segmentationSummary: nil,
            source: source,
            nowMs: nowMs)
        publishCapabilities()
        return snapshot
    }

    // MARK: Snapshot assembly (verbatim port of the Android assemble())

    private func assemble(
        subjects: [SceneSubject],
        primaryOverride: SceneSubject?,
        subjectsAgeMs: Int64,
        embeddingResult: VisualEmbeddingResult?,
        embeddingAgeMs: Int64,
        segmentationSummary: SegmentationSummary?,
        source: SnapshotSource?,
        nowMs: Int64
    ) -> SceneSnapshot {
        let embeddingConfidence: Float = embeddingResult.map {
            $0.confidence * stalenessFactor(ageMs: embeddingAgeMs)
        } ?? 0
        let detectionAvailable = detection.capability.available
        let classification = strategy.classify(
            subjects: subjects,
            embeddingAvailable: embeddingResult != nil && embeddingConfidence > 0,
            detectionAvailable: detectionAvailable)
        let subjectStaleness = stalenessFactor(ageMs: subjectsAgeMs)

        // Live tracked subject: identity continuity outranks re-selection.
        let primary: FeatureValue<PrimarySubject>
        if let s = primaryOverride {
            primary = FeatureValue(
                value: PrimarySubject(
                    type: PrimarySubjectSelector.shared.typeFor(category: s.category),
                    category: s.category,
                    rawLabel: s.rawLabel,
                    confidence: s.confidence,
                    boundingBox: s.boundingBox,
                    maskAvailable: s.maskAvailable,
                    normalizedCenterX: s.normalizedCenterX,
                    normalizedCenterY: s.normalizedCenterY,
                    normalizedArea: s.normalizedArea),
                confidence: s.confidence)
        } else {
            primary = classification.primarySubject
        }

        let layoutSignature: FeatureValue<KotlinFloatArray>
        if detectionAvailable && !subjects.isEmpty {
            layoutSignature = FeatureValue(
                value: CompositionAnalyzer.shared.gridSignature(subjects: subjects),
                confidence: subjectStaleness)
        } else {
            layoutSignature = FeatureValue(value: nil, confidence: 0)
        }

        let primaryValue = primary.value as? PrimarySubject
        let subjectPosition: FeatureValue<NormalizedPoint>
        let subjectSize: FeatureValue<KotlinFloat>
        if let box = primaryValue?.boundingBox {
            subjectPosition = FeatureValue(
                value: NormalizedPoint(x: box.centerX, y: box.centerY),
                confidence: primary.confidence * subjectStaleness)
            subjectSize = FeatureValue(
                value: KotlinFloat(value: box.area),
                confidence: primary.confidence * subjectStaleness)
        } else {
            subjectPosition = FeatureValue(value: nil, confidence: 0)
            subjectSize = FeatureValue(value: nil, confidence: 0)
        }

        let embeddingFeature: FeatureValue<KotlinFloatArray>
        if let embeddingResult {
            embeddingFeature = FeatureValue(value: embeddingResult.embedding,
                                            confidence: embeddingConfidence)
        } else {
            embeddingFeature = FeatureValue(value: nil, confidence: 0)
        }

        let resolvedSource: SnapshotSource
        if let source {
            resolvedSource = source
        } else if !subjects.isEmpty {
            resolvedSource = .detector
        } else if embeddingResult != nil {
            resolvedSource = .embeddingOnly
        } else {
            resolvedSource = .none
        }

        return SceneSnapshot(
            subjects: subjects,
            primarySubject: FeatureValue(value: primary.value,
                                         confidence: primary.confidence * subjectStaleness),
            sceneMode: classification.sceneMode,
            subjectPosition: subjectPosition,
            subjectSize: subjectSize,
            layoutSignature: layoutSignature,
            embedding: embeddingFeature,
            embeddingModelId: embeddingResult?.modelId,
            segmentationSummary: segmentationSummary,
            source: resolvedSource,
            analyzedAtMs: nowMs)
    }

    /// Linear confidence decay from 1.0 (fresh) to 0.0 (at/after the staleness horizon).
    private func stalenessFactor(ageMs: Int64) -> Float {
        if ageMs <= 0 { return 1 }
        if ageMs >= cadence.staleAfterMs { return 0 }
        return 1 - Float(ageMs) / Float(cadence.staleAfterMs)
    }

    private func publishCapabilities() {
        let caps = AiCapabilities(
            detection: detection.capability,
            embedding: embedding.capability,
            segmentation: segmentation.capability)
        DispatchQueue.main.async { self.capabilities = caps }
    }
}

// MARK: - AiSceneModule (port of liveview/vision/modules/AiSceneModule.kt)

/// Phase 10: live AI perception at adaptive cadence.
///
/// THREADING IS THE POINT OF THIS CLASS: the Vision pipeline runs all modules sequentially on one
/// worker, and AI inference costs 50–200 ms — so [analyze] NEVER runs inference inline. Per due
/// frame it only (1) publishes the previously completed snapshot, (2) takes a small scaled copy
/// (the renderer owns the source image, so inference must not hold it), and (3) hands the copy to
/// the coordinator's single lane. A busy-guard keeps at most one inference in flight; frames due
/// while busy are counted as skipped (newest-wins at the inference level). The per-frame exposure
/// module is never delayed.
final class AiSceneModule: VisionModuleIos {

    let id = "ai_scene"

    private let coordinator: AiPerceptionCoordinator
    private let clock: () -> Int64

    private let lock = NSLock()
    private var busy = false
    private var completed: SceneSnapshotResult?
    private var skippedInferences: Int64 = 0

    /// Pipeline-worker-thread only.
    private var frameCount: Int64 = 0

    init(coordinator: AiPerceptionCoordinator,
         clock: @escaping () -> Int64 = { SharedFactory.nowMs() }) {
        self.coordinator = coordinator
        self.clock = clock
    }

    func analyze(_ request: FrameAnalysisRequestIos) throws -> [VisionResult] {
        // Publish the last finished inference regardless of what happens below — results
        // re-enter through the standard onResults → VisionContext merge, one frame late.
        lock.lock()
        let ready = completed
        completed = nil
        lock.unlock()

        guard coordinator.liveWorkNeeded else { return ready.map { [$0] } ?? [] }

        frameCount += 1
        let cadence = coordinator.cadence
        let trackerDue = frameCount % Int64(cadence.trackerEveryNFrames) == 0
        let embeddingDue = frameCount % Int64(cadence.embeddingEveryNFrames) == 0
        if trackerDue || embeddingDue {
            lock.lock()
            let acquired = !busy
            if acquired { busy = true }
            lock.unlock()

            if acquired {
                // Small scaled copy (~1–3 ms) — always a COPY, never the renderer's image.
                let copy = Self.defaultScaledCopy(request.image, maxEdge: Int(cadence.inferenceMaxEdgePx))
                if copy == nil {
                    lock.lock(); busy = false; lock.unlock()
                } else if let copy {
                    let nowMs = clock()
                    coordinator.lane.async { [weak self] in
                        guard let self else { return }
                        let snapshot = self.coordinator.processLiveFrameOnLane(
                            image: copy, runTracker: trackerDue, runEmbedding: embeddingDue,
                            nowMs: nowMs)
                        self.lock.lock()
                        self.completed = SceneSnapshotResult(
                            moduleId: self.id,
                            snapshot: snapshot,
                            skippedInferences: self.skippedInferences)
                        self.busy = false
                        self.lock.unlock()
                    }
                }
            } else {
                lock.lock(); skippedInferences += 1; lock.unlock()
            }
        }
        return ready.map { [$0] } ?? []
    }

    /// Scaled copy at [maxEdge] on the longest side — always a COPY, even at equal size
    /// (Android defaultScaledCopy parity: inference never shares the renderer's image lifetime).
    static func defaultScaledCopy(_ source: UIImage, maxEdge: Int) -> UIImage? {
        guard let cg = source.cgImage else { return nil }
        let srcW = cg.width, srcH = cg.height
        guard srcW > 0, srcH > 0 else { return nil }
        let scale = CGFloat(maxEdge) / CGFloat(max(srcW, srcH))
        let w = scale < 1 ? max(1, Int((CGFloat(srcW) * scale).rounded())) : srcW
        let h = scale < 1 ? max(1, Int((CGFloat(srcH) * scale).rounded())) : srcH

        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        format.opaque = false
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: w, height: h), format: format)
        return renderer.image { _ in
            UIImage(cgImage: cg).draw(in: CGRect(x: 0, y: 0, width: w, height: h))
        }
    }
}
