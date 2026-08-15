import Foundation
import UIKit
import DyrectoShared

// MARK: - VisionStatistics (port of liveview/vision/VisionStatistics.kt)

/// Immutable snapshot of pipeline health for the Developer UI.
struct VisionStatistics {
    var framesReceived: Int64 = 0
    var framesAnalyzed: Int64 = 0
    var framesDropped: Int64 = 0
    var registeredModules: Int = 0
    var lastAnalysisDurationMs: Int64 = 0
    var averageAnalysisDurationMs: Double = 0.0
    var analysisFps: Double = 0.0
    var pipelineRunning: Bool = false
}

// MARK: - FrameAnalysisRequest (port of liveview/vision/FrameAnalysisRequest.kt)

/// Immutable unit of work handed to every module. Built ON the pipeline worker after conflation:
/// [rgba] is the ONE RGBA8888 draw per analyzed frame (see `RgbaFrameBuffer`) — the exposure scan
/// and any future pixel consumer share it; Vision-framework modules use [image]/[cgImage].
/// Modules must treat everything here as strictly read-only and never retain it past analyze().
struct FrameAnalysisRequestIos {
    /// The renderer's published frame — shared by reference, READ-ONLY.
    let image: UIImage
    let cgImage: CGImage
    /// RGBA8888 bytes of the full frame — ONE draw per analyzed frame, shared by all modules.
    let rgba: NSData
    let width: Int
    let height: Int
    /// Read-only realtime metadata paired with this frame (latest-wins snapshot).
    let context: FrameContext
    /// Wall-clock time the pipeline received this frame.
    let receivedAtMs: Int64
}

// MARK: - VisionModule (port of liveview/vision/VisionModule.kt)

/// A passive analyzer that receives rendered frames and returns a (possibly empty) list of
/// results. Implementations run on the pipeline's single serial worker — never on the socket /
/// renderer / main thread. The pipeline wraps every call in do/catch; a throwing module never
/// stops the worker or its peers.
protocol VisionModuleIos: AnyObject {
    /// Stable identifier for diagnostics/logging and `VisionResult.moduleId`.
    var id: String { get }
    /// Analyze one frame; read-only over the request. Exceptions are caught + isolated.
    func analyze(_ request: FrameAnalysisRequestIos) throws -> [VisionResult]
}

// MARK: - VisionPipeline (port of liveview/vision/VisionPipeline.kt + VisionManager.kt)

/// Passive, newest-frame-wins analysis pipeline + result aggregator.
///
/// Threading (the Android contract, ported 1:1):
///  - ONE serial worker queue; modules run sequentially on it, never on the render/socket thread.
///  - Exactly one pending slot, never a queue: [submitFrame] overwrites it; a displaced un-taken
///    frame is counted as dropped. The worker analyzes at most one frame at a time.
///  - Per-module isolation: a throwing/slow module is logged (throttled) and skipped.
///
/// Aggregation (VisionManager.onResults parity): each result type merges into its own slot of a
/// shared `VisionContext`, republished after every pass via @Published + [onContext] (worker
/// thread — the reference monitor / scene manager taps hang off this callback).
final class VisionPipeline: ObservableObject {

    @Published private(set) var statistics = VisionStatistics(pipelineRunning: true)
    @Published private(set) var visionContext: VisionContext = SharedFactory.emptyVisionContext()

    /// Called with each freshly merged VisionContext (WORKER thread) — the per-frame tap the
    /// scene manager + reference monitor consume.
    var onContext: ((VisionContext) -> Void)?

    private let worker = DispatchQueue(label: "app.dyrecto.vision.worker", qos: .utility)
    private let log = DyrectoLog.shared

    private let lock = NSLock()
    private var modules: [VisionModuleIos] = []
    /// The single pending slot (newest-wins). Never grows into a queue.
    private var pending: (image: UIImage, context: FrameContext, receivedAtMs: Int64)?
    private var workScheduled = false
    private var running = true

    // ---- Counters (lock-guarded; read into the published snapshot) ----
    private var framesReceived: Int64 = 0
    private var framesAnalyzed: Int64 = 0
    private var framesDropped: Int64 = 0
    private var lastAnalysisDurationMs: Int64 = 0
    private var averageAnalysisDurationMs: Double = 0.0
    private var analysisFps: Double = 0.0

    // Cadence/timing state (worker-thread only).
    private var lastAnalysisEndNs: UInt64 = 0
    private var lastLogAtMs: Int64 = 0
    private var lastContextLogAtMs: Int64 = 0

    // VisionContext slots (worker-thread only) — the VisionManager merge state.
    private var histogram: HistogramResult?
    private var zebra: ZebraResult?
    private var exposure: ExposureResult?
    private var faces: FaceDetectionResult?
    private var eyes: EyeDetectionResult?
    private var colorStats: ColorStatsResult?
    private var aiScene: SceneSnapshotResult?
    private var subjectExposure: SubjectExposureResult?
    private(set) var latestContext: VisionContext = SharedFactory.emptyVisionContext()

    init() {}

    // MARK: Public API

    func register(_ module: VisionModuleIos) {
        lock.lock()
        if !modules.contains(where: { $0 === module }) { modules.append(module) }
        lock.unlock()
        publishStats()
    }

    func unregister(_ module: VisionModuleIos) {
        lock.lock()
        modules.removeAll { $0 === module }
        lock.unlock()
        publishStats()
    }

    func clear() {
        lock.lock(); modules.removeAll(); lock.unlock()
        publishStats()
    }

    /// Hand a frame to the pipeline. Non-blocking, safe from any thread (the renderer's decode
    /// queue calls it). Overwrites the pending slot; a displaced un-taken frame is dropped.
    func submitFrame(image: UIImage, context: FrameContext) {
        let nowMs = Int64(Date().timeIntervalSince1970 * 1000)
        lock.lock()
        guard running else { lock.unlock(); return }
        framesReceived += 1
        if pending != nil { framesDropped += 1 }
        pending = (image, context, nowMs)
        let schedule = !workScheduled
        if schedule { workScheduled = true }
        lock.unlock()

        publishStats()
        if schedule {
            worker.async { [weak self] in self?.drain() }
        }
    }

    /// Stop the worker permanently. Idempotent.
    func shutdown() {
        lock.lock()
        running = false
        pending = nil
        lock.unlock()
        publishStats()
    }

    // MARK: Worker internals

    private func drain() {
        while true {
            lock.lock()
            guard running, let req = pending else {
                workScheduled = false
                lock.unlock()
                return
            }
            pending = nil
            let mods = modules
            lock.unlock()
            runModules(image: req.image, context: req.context, receivedAtMs: req.receivedAtMs, modules: mods)
        }
    }

    private func runModules(image: UIImage, context: FrameContext, receivedAtMs: Int64,
                            modules: [VisionModuleIos]) {
        let startNs = DispatchTime.now().uptimeNanoseconds

        // The ONE RGBA draw per analyzed frame (Android's shared Bitmap.getPixels-equivalent).
        guard let cg = image.cgImage,
              let extracted = RgbaFrameBuffer.extract(from: image) else {
            return // unreadable frame — Android's recycled-bitmap parity: skip silently
        }
        let request = FrameAnalysisRequestIos(
            image: image, cgImage: cg, rgba: extracted.data,
            width: extracted.width, height: extracted.height,
            context: context, receivedAtMs: receivedAtMs)

        var results: [VisionResult] = []
        for module in modules {
            do {
                results.append(contentsOf: try module.analyze(request))
            } catch {
                // Isolate: one faulty module never stops the worker or its peers.
                maybeLogModuleFailure(module, error)
            }
        }
        onResults(results)
        let endNs = DispatchTime.now().uptimeNanoseconds

        let durationMs = Int64((endNs - startNs) / 1_000_000)
        lock.lock()
        lastAnalysisDurationMs = durationMs
        framesAnalyzed += 1
        let n = framesAnalyzed
        averageAnalysisDurationMs += (Double(durationMs) - averageAnalysisDurationMs) / Double(n)
        if lastAnalysisEndNs != 0 {
            let intervalMs = Double(endNs - lastAnalysisEndNs) / 1_000_000.0
            if intervalMs > 0 {
                let instantFps = 1000.0 / intervalMs
                analysisFps = analysisFps == 0.0 ? instantFps : analysisFps + 0.2 * (instantFps - analysisFps)
            }
        }
        lastAnalysisEndNs = endNs
        lock.unlock()

        publishStats()
        maybeLog()
    }

    // ---- Result collection (VisionManager.onResults parity; worker thread) ----

    private func onResults(_ results: [VisionResult]) {
        for result in results {
            switch result {
            case let r as HistogramResult: histogram = r
            case let r as ZebraResult: zebra = r
            case let r as ExposureResult: exposure = r
            case let r as FaceDetectionResult: faces = r
            case let r as EyeDetectionResult: eyes = r
            case let r as ColorStatsResult: colorStats = r
            case let r as SceneSnapshotResult: aiScene = r
            case let r as SubjectExposureResult: subjectExposure = r
            default: break
            }
        }
        let ctx = VisionContext(
            histogram: histogram, zebra: zebra, exposure: exposure,
            faces: faces, eyes: eyes, colorStats: colorStats,
            aiScene: aiScene, subjectExposure: subjectExposure,
            updatedAtMs: Int64(Date().timeIntervalSince1970 * 1000))
        latestContext = ctx
        DispatchQueue.main.async { self.visionContext = ctx }
        onContext?(ctx)
        maybeLogContext(ctx)
    }

    private func publishStats() {
        lock.lock()
        let stats = VisionStatistics(
            framesReceived: framesReceived,
            framesAnalyzed: framesAnalyzed,
            framesDropped: framesDropped,
            registeredModules: modules.count,
            lastAnalysisDurationMs: lastAnalysisDurationMs,
            averageAnalysisDurationMs: averageAnalysisDurationMs,
            analysisFps: analysisFps,
            pipelineRunning: running)
        lock.unlock()
        DispatchQueue.main.async { self.statistics = stats }
    }

    // MARK: Throttled logging (once per second — never per frame)

    private func maybeLog() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        if now - lastLogAtMs < 1000 { return }
        lastLogAtMs = now
        lock.lock()
        let queueDepth = pending != nil ? 1 : 0
        let line = String(
            format: "[VISION] received=%d analyzed=%d dropped=%d modules=%d analysisFps=%.1f avgMs=%.1f queue=%d",
            framesReceived, framesAnalyzed, framesDropped, modules.count,
            analysisFps, averageAnalysisDurationMs, queueDepth)
        lock.unlock()
        log.line(.info, line)
    }

    private func maybeLogModuleFailure(_ module: VisionModuleIos, _ error: Error) {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        // Reuse the 1 s throttle window so a continuously-throwing module can't flood the log.
        if now - lastLogAtMs < 1000 { return }
        lastLogAtMs = now
        log.line(.error, "[VISION] module '\(module.id)' failed: \(error)")
    }

    private func maybeLogContext(_ ctx: VisionContext) {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        if now - lastContextLogAtMs < 1000 { return }
        lastContextLogAtMs = now
        log.line(.info, String(
            format: "[VISION-CTX] highlight=%.1f%% shadow=%.1f%% stride=%d faces=%d eyes=%d",
            ctx.exposure?.highlightCoverage ?? 0,
            ctx.exposure?.shadowCoverage ?? 0,
            ctx.histogram?.effectiveStride ?? 1,
            ctx.faces?.facesDetected ?? 0,
            ctx.eyes?.eyesDetected ?? 0))
    }
}

// MARK: - Exposure module wrapper

/// Bridges the pipeline to the shared Kotlin/Native `IosExposureModule` — the entire per-frame
/// hot path (RGBA→ARGB + luma scan + histogram/zebra/verdict + subject-region stats) runs in
/// compiled Kotlin with zero per-pixel bridging; Swift only hands over the one NSData buffer.
/// Fresh instances own their own sampler/state machine (Android ExposureModule parity).
final class ExposureVisionModule: VisionModuleIos {
    let id = "exposure"
    private let module = IosExposureModule()

    func analyze(_ request: FrameAnalysisRequestIos) throws -> [VisionResult] {
        module.analyze(rgbaData: request.rgba,
                       width: Int32(request.width),
                       height: Int32(request.height))
    }
}

// MARK: - Shared default-instance factory

/// Kotlin default arguments vanish in the ObjC export, so "empty" shared data classes must be
/// constructed with every parameter. Centralized here so the defaults stay byte-identical to the
/// Kotlin declarations in exactly one place.
enum SharedFactory {

    static func emptyVisionContext() -> VisionContext {
        VisionContext(
            histogram: nil, zebra: nil, exposure: nil, faces: nil, eyes: nil,
            colorStats: nil, aiScene: nil, subjectExposure: nil, updatedAtMs: 0)
    }

    static func emptyFrameContext() -> FrameContext {
        FrameContext(
            renderSequence: 0, observedAtMs: 0, frameAgeMs: 0, frameFps: 0.0, latencyMs: 0.0,
            renderActive: false, telemetry: nil, telemetryTimestampMs: 0,
            connectionPhase: .idle, liveViewActive: false)
    }

    static func emptySignalResult() -> ReferenceSignalResult {
        ReferenceSignalResult(
            enabled: false, available: false, matched: true, score: 1.0, delta: 0.0,
            direction: .none, message: "", state: .normal, persistenceFrames: 0,
            recoveryFrames: 0, confirmedDrift: false, perception: nil, instruction: nil)
    }

    static func emptyMatchResult() -> ReferenceMatchResult {
        ReferenceMatchResult(
            active: false, referenceId: nil, strategy: .humanStrategy, overallScore: 1.0,
            creativeAware: false,
            exposureMatch: emptySignalResult(), subjectPositionMatch: emptySignalResult(),
            subjectSizeMatch: emptySignalResult(), whiteBalanceMatch: emptySignalResult(),
            headroomMatch: emptySignalResult(), facePresenceMatch: emptySignalResult(),
            eyeVisibilityMatch: emptySignalResult(), subjectPresenceMatch: emptySignalResult(),
            compositionMatch: emptySignalResult(), visualSimilarityMatch: emptySignalResult(),
            updatedAtMs: 0)
    }

    static func emptySessionState() -> ReferenceSessionState {
        ReferenceSessionState(session: nil, monitoringRequested: false, analyzing: false, error: nil)
    }

    static func defaultMonitorOptions() -> ReferenceMonitorOptions {
        ReferenceMonitorOptions(
            monitorExposure: true, monitorSubjectPosition: true, monitorSubjectSize: true,
            monitorWhiteBalance: true, monitorFraming: false, monitorHeadroom: true,
            monitorFacePresence: true, monitorEyeVisibility: true,
            monitorSubjectPresence: true, monitorComposition: true, monitorVisualSimilarity: true,
            tolerance: .medium)
    }

    static func defaultCompletionRule() -> StoryboardCompletionRule {
        StoryboardCompletionRule(holdSeconds: 5, confirmCount: 1, matchThreshold: 0.80)
    }

    static func emptyShotCompletion() -> ShotCompletion {
        ShotCompletion(completed: false, confirmations: 0)
    }

    /// Android AiCadenceConfig defaults, verbatim.
    static func defaultCadence() -> AiCadenceConfig {
        AiCadenceConfig(
            trackerEveryNFrames: 2, embeddingEveryNFrames: 15, staleAfterMs: 3_000,
            inferenceMaxEdgePx: 512, trackingMaxEdgePx: 160)
    }

    /// Android TrackPolicyConfig defaults, verbatim.
    static func defaultTrackPolicy() -> TrackPolicyConfig {
        TrackPolicyConfig(
            lossConfidenceThreshold: 0.35, lossFramesToDeclare: 5, verifyIntervalMs: 5_000,
            sceneChangeSimilarityFloor: 0.75, sceneChangeLumaDelta: 40)
    }

    static func nowMs() -> Int64 { Int64(Date().timeIntervalSince1970 * 1000) }
}
