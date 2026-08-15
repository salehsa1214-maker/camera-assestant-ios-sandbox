import Foundation
import UIKit
import DyrectoShared

/// A fully analyzed reference: the profile plus its embedding (persisted as a separate file).
struct ReferenceAnalysisIos {
    let profile: ReferenceProfile
    let embedding: ReferenceEmbedding?
}

/// One-shot analyzer for a selected reference image — the iOS port of `ReferenceAnalyzer`.
///
/// Runs the SAME analysis the live pipeline uses — the shared exposure scan
/// (LuminanceAnalyzer → Histogram/Zebra/ExposureAnalyzer with the active AnalysisColorTransform,
/// plus fused color stats, via a FRESH `IosExposureModule`) and a fresh Vision-framework face
/// pass — over the reference image and folds the results into an immutable shared
/// `ReferenceProfile`. There is deliberately no second analysis path: the reference travels the
/// identical code live frames travel, so a perfect live match produces near-zero deltas by
/// construction.
///
/// Owns fresh module instances (never the live pipeline's): the live exposure module's adaptive
/// sampler and state machine must not be perturbed by a one-off reference scan.
///
/// Phase 10: with a [perception] coordinator the reference additionally gets the full AI pass
/// (detection + embedding + one-shot segmentation stats) folded into `ReferenceProfile.ai`; AI
/// unavailable ⇒ `ai = nil` — exactly the Phase 9 face-based behavior. Phase 16.1: the optional
/// [semanticEngine] (MobileCLIP) runs ONCE here at import, fail-safe (nil never blocks import).
final class ReferenceAnalyzerIos {

    private let exposureModule = IosExposureModule()
    private let perception: AiPerceptionCoordinator?
    private let semanticEngine: SemanticSceneEngine?

    init(perception: AiPerceptionCoordinator?, semanticEngine: SemanticSceneEngine?) {
        self.perception = perception
        self.semanticEngine = semanticEngine
    }

    /// Analyzes [image] and returns the analysis. Blocking (ML inference) — call from a
    /// background queue only, exactly like the Android suspend variant.
    func analyze(
        image: UIImage,
        id: String,
        name: String,
        imageUri: String?,
        options: ReferenceMonitorOptions,
        nowMs: Int64
    ) throws -> ReferenceAnalysisIos {
        guard let cg = image.cgImage,
              let extracted = RgbaFrameBuffer.extract(from: image) else {
            throw NSError(domain: "app.dyrecto", code: 30, userInfo: [
                NSLocalizedDescriptionKey: "Reference image is not readable."])
        }
        let width = extracted.width
        let height = extracted.height

        // ---- Exposure scan (fresh module; same code path as live frames) ----
        let exposureResults = exposureModule.analyze(
            rgbaData: extracted.data as Data, width: Int32(width), height: Int32(height))

        // ---- Face pass (fresh Vision-framework pass; same conversion as the live module) ----
        let (faces, eyes) = try FaceEyeModule.detect(
            cgImage: cg, width: width, height: height,
            moduleId: "face-and-eye", startMs: nowMs)

        var histogram: HistogramResult?
        var zebra: ZebraResult?
        var colorStats: ColorStatsResult?
        for result in exposureResults {
            switch result {
            case let r as HistogramResult: histogram = histogram ?? r
            case let r as ZebraResult: zebra = zebra ?? r
            case let r as ColorStatsResult: colorStats = colorStats ?? r
            default: break
            }
        }
        guard let histogram, let zebra, let colorStats else {
            throw NSError(domain: "app.dyrecto", code: 31, userInfo: [
                NSLocalizedDescriptionKey:
                    "Reference exposure analysis produced no results (empty image?)"])
        }

        let subjectBox = SceneComparator.shared.largestFaceNormalized(faces: faces)
        let subject: ReferenceSubjectProfile? = subjectBox.map {
            ReferenceSubjectProfile(
                normalizedCenterX: $0.centerX,
                normalizedCenterY: $0.centerY,
                normalizedWidth: $0.width,
                normalizedHeight: $0.height,
                normalizedArea: $0.area)
        }

        let eyeCount = Int(eyes.eyesDetected)
        let face = ReferenceFaceProfile(
            faceDetected: subjectBox != nil,
            eyesDetected: eyeCount > 0,
            faceBox: subjectBox,
            eyeCount: Int32(eyeCount))

        // ---- Phase 10: full AI perception pass (never throws — coordinator contract) ----
        let snapshot = perception?.analyzeReference(image: image, nowMs: nowMs)
        let embedding = snapshot.flatMap { buildEmbedding($0, profileId: id, nowMs: nowMs) }
        let aiProfile = snapshot.flatMap { buildAiProfile($0, embedding: embedding) }

        // ---- Phase 12: perceptual exposure inputs ----
        // Coarse histogram signature from the same scan's bins; subject-region stats use the AI
        // primary subject box when available, else the face box. One extra pixel read is fine —
        // reference analysis is a one-shot user action, not the per-frame path.
        // interop: `signatureBins` mirrors PerceptualThresholds.HISTOGRAM_SIGNATURE_BINS (= 32);
        // Kotlin default args vanish so the constant is passed explicitly.
        let histogramSignature = HistogramSignature.shared.fromBins(
            bins: histogram.bins, signatureBins: 32)
        let subjectRegionBox = aiProfile?.primarySubjectBox ?? subjectBox
        var subjectExposureStats: SubjectExposureStats?
        if let box = subjectRegionBox {
            // Bulk ARGB read through the shared Kotlin conversion loop (no per-pixel bridging).
            let framePixels = RgbaFramePixels.companion.fromNSData(
                data: extracted.data as Data, width: Int32(width), height: Int32(height))
            let argb = KotlinIntArray(size: Int32(width * height))
            framePixels.readArgb(dest: argb)
            subjectExposureStats = RegionLumaStats.shared.compute(
                argb: argb, width: Int32(width), height: Int32(height),
                region: AnalysisRegion(left: box.left, top: box.top,
                                       right: box.right, bottom: box.bottom),
                stride: 1,
                transform: ExposureConfig.shared.resolveTransform(),
                highlightLumaMin: AnalysisRegionRegistry.shared.highlightLumaMin,
                shadowLumaMax: AnalysisRegionRegistry.shared.shadowLumaMax)
        }

        let exposureProfile = ReferenceExposureProfile(
            mean: histogram.mean,
            median: Float(histogram.median),
            p95: Float(histogram.percentile95),
            p99: Float(histogram.percentile99),
            highlightCoverage: zebra.coveragePercentage,
            shadowCoverage: histogram.clippedShadowPercentage,
            histogramSignature: histogramSignature,
            subjectExposure: subjectExposureStats)
        let colorProfile = ReferenceColorProfile(
            avgR: colorStats.avgR,
            avgG: colorStats.avgG,
            avgB: colorStats.avgB,
            warmthScore: colorStats.warmthScore,
            tintScore: colorStats.tintScore)

        // ---- Phase 16.1: MobileCLIP semantic expert, ONCE at import; fail-safe nil ----
        let semanticObservation: SemanticObservation? = semanticEngine.flatMap { engine in
            let semaphore = DispatchSemaphore(value: 0)
            var observation: SemanticObservation?
            engine.observe(bitmap: PlatformImage(handle: image)) { result, _ in
                observation = result
                semaphore.signal()
            }
            semaphore.wait()
            return observation
        }

        // ---- Phase 16: Creative Scene Model derived ONCE here at import ----
        let creativeScene = CreativeSceneAnalyzer.shared.analyze(
            exposure: exposureProfile,
            color: colorProfile,
            ai: aiProfile,
            faceBox: subjectBox,
            semantic: semanticObservation)

        let profile = ReferenceProfile(
            id: id,
            name: name,
            createdAtMs: nowMs,
            imageUri: imageUri,
            width: Int32(width),
            height: Int32(height),
            exposure: exposureProfile,
            color: colorProfile,
            subject: subject,
            face: face,
            options: options,
            ai: aiProfile,
            creativeScene: creativeScene,
            completion: SharedFactory.emptyShotCompletion(),
            cameraSettings: nil)
        return ReferenceAnalysisIos(profile: profile, embedding: embedding)
    }

    // MARK: AI profile assembly (verbatim port of ReferenceAnalyzer.buildAiProfile)

    private func buildAiProfile(_ snapshot: SceneSnapshot,
                                embedding: ReferenceEmbedding?) -> ReferenceAiProfile? {
        let sceneMode = snapshot.sceneMode.value as? SceneMode
        let primary = snapshot.primarySubject.value as? PrimarySubject
        // Nothing understood at all (every model down) → no AI section.
        if sceneMode == nil && primary == nil && !snapshot.embedding.present { return nil }
        let strategy = ComparisonStrategySelector.shared.select(
            sceneMode: sceneMode ?? .unknown,
            primarySubject: primary,
            detectionAvailable: !snapshot.subjects.isEmpty || primary != nil)

        var compositionSignature: [KotlinFloat] = []
        if let layout = snapshot.layoutSignature.value as? KotlinFloatArray {
            for i in 0..<layout.size {
                compositionSignature.append(KotlinFloat(value: layout.get(index: i)))
            }
        }

        return ReferenceAiProfile(
            sceneMode: sceneMode ?? .unknown,
            sceneModeConfidence: snapshot.sceneMode.confidence,
            strategy: strategy,
            primarySubjectType: primary?.type ?? .unknown,
            primarySubjectCategory: primary?.category ?? .unknown,
            primarySubjectRawLabel: primary?.rawLabel,
            primarySubjectConfidence: primary?.confidence ?? 0,
            primarySubjectBox: primary?.boundingBox,
            subjects: snapshot.subjects.map {
                ReferenceAiSubject(
                    category: $0.category,
                    rawLabel: $0.rawLabel,
                    confidence: $0.confidence,
                    box: $0.boundingBox)
            },
            compositionSignature: compositionSignature,
            segmentationCoverage: snapshot.segmentationSummary.map { KotlinFloat(value: $0.coverage) },
            segmentationPixelAccurate: snapshot.segmentationSummary?.pixelAccurate == true,
            // Android fills the embedding link on the stored copy; we know it already here.
            embeddingId: embedding?.id,
            embeddingModelId: embedding?.modelId,
            schemaVersion: 1)
    }

    private func buildEmbedding(_ snapshot: SceneSnapshot, profileId: String,
                                nowMs: Int64) -> ReferenceEmbedding? {
        guard let vector = snapshot.embedding.value as? KotlinFloatArray,
              let modelId = snapshot.embeddingModelId else { return nil }
        var values: [KotlinFloat] = []
        values.reserveCapacity(Int(vector.size))
        for i in 0..<vector.size {
            values.append(KotlinFloat(value: vector.get(index: i)))
        }
        return ReferenceEmbedding(
            id: "\(profileId)-emb",
            modelId: modelId,
            dimensions: vector.size,
            values: values,
            createdAtMs: nowMs,
            schemaVersion: 1)
    }
}
