package app.dyrecto.liveview.reference

import android.graphics.Bitmap
import app.dyrecto.liveview.exposure.AnalysisRegion
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.RegionLumaStats
import app.dyrecto.liveview.perception.HistogramSignature
import app.dyrecto.liveview.reference.ai.AiPerceptionCoordinator
import app.dyrecto.liveview.reference.ai.ComparisonStrategySelector
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot
import app.dyrecto.liveview.reference.creative.CreativeSceneAnalyzer
import app.dyrecto.liveview.reference.creative.semantic.SemanticSceneEngine
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.FrameAnalysisRequest
import app.dyrecto.liveview.vision.modules.ExposureModule
import app.dyrecto.liveview.vision.modules.FaceAndEyeDetectionModule
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.ZebraResult

/** A fully analyzed reference: the profile plus its embedding (persisted as a separate file). */
data class ReferenceAnalysis(
    val profile: ReferenceProfile,
    val embedding: ReferenceEmbedding?,
)

/**
 * One-shot analyzer for a selected reference image (Phase 9): runs the SAME analysis modules the
 * live pipeline uses — [ExposureModule] (LuminanceAnalyzer → Histogram/Zebra/ExposureAnalyzer with
 * the active AnalysisColorTransform, plus fused color stats) and [FaceAndEyeDetectionModule]
 * (ML Kit) — over the reference Bitmap and folds the results into an immutable [ReferenceProfile].
 *
 * There is deliberately **no second analysis path**: the reference travels the identical code the
 * live frames travel, so a perfect live match produces near-zero deltas by construction.
 *
 * Owns fresh module instances (not the live pipeline's): the live ExposureModule's adaptive
 * sampler and Phase 7 state machine must not be perturbed by a one-off reference scan.
 *
 * Phase 10: when a [perception] coordinator is provided, the reference additionally gets the
 * full AI pass (detection + embedding + one-shot segmentation stats) folded into
 * [ReferenceProfile.ai]. AI being unavailable (null coordinator or all models failed) yields
 * `ai = null` — exactly the Phase 9 face-based behavior.
 */
class ReferenceAnalyzer(
    private val exposureModule: ExposureModule = ExposureModule(),
    private val faceModule: FaceAndEyeDetectionModule = FaceAndEyeDetectionModule(),
    private val perception: AiPerceptionCoordinator? = null,
    /**
     * Phase 16.1: MobileCLIP semantic expert, run ONCE here at import (never per frame). Null when
     * semantic understanding is unconfigured/disabled; a load/inference failure returns null too —
     * `CreativeSceneAnalyzer` then falls back to its deterministic experts. Never blocks import.
     */
    private val semanticEngine: SemanticSceneEngine? = null,
) {

    /**
     * Analyzes [bitmap] and returns the [ReferenceAnalysis]. Suspends (ML inference); call from
     * a background dispatcher. [imageUri] is the app-private copy used for the UI thumbnail.
     */
    suspend fun analyze(
        bitmap: Bitmap,
        id: String,
        name: String,
        imageUri: String?,
        options: ReferenceMonitorOptions,
        nowMs: Long,
    ): ReferenceAnalysis {
        val request = FrameAnalysisRequest(
            bitmap = bitmap,
            context = FrameContext(),
            receivedAtMs = nowMs,
        )

        val exposureResults = exposureModule.analyze(request)
        val faceResults = faceModule.analyze(request)

        val histogram = exposureResults.filterIsInstance<HistogramResult>().firstOrNull()
        val zebra = exposureResults.filterIsInstance<ZebraResult>().firstOrNull()
        val colorStats = exposureResults.filterIsInstance<ColorStatsResult>().firstOrNull()
        val faces = faceResults.filterIsInstance<FaceDetectionResult>().firstOrNull()
        val eyes = faceResults.filterIsInstance<EyeDetectionResult>().firstOrNull()

        require(histogram != null && zebra != null && colorStats != null) {
            "Reference exposure analysis produced no results (empty or recycled bitmap?)"
        }

        val subjectBox = SceneComparator.largestFaceNormalized(faces)
        val subject = subjectBox?.let {
            ReferenceSubjectProfile(
                normalizedCenterX = it.centerX,
                normalizedCenterY = it.centerY,
                normalizedWidth = it.width,
                normalizedHeight = it.height,
                normalizedArea = it.area,
            )
        }

        val eyeCount = eyes?.eyesDetected ?: 0
        val face = ReferenceFaceProfile(
            faceDetected = subjectBox != null,
            eyesDetected = eyeCount > 0,
            faceBox = subjectBox,
            eyeCount = eyeCount,
        )

        // Phase 10: full AI perception pass. Never throws (coordinator contract); an empty
        // snapshot (all models down) folds to ai = null → Phase 9 behavior.
        val snapshot = perception?.analyzeReference(bitmap, nowMs)
        val aiProfile = snapshot?.let { buildAiProfile(it) }
        val embedding = snapshot?.let { buildEmbedding(it, id, nowMs) }

        // Phase 12: perceptual exposure inputs. The coarse histogram signature comes from the
        // same scan's bins; subject-region stats use the AI primary subject box when available,
        // else the face box. One extra pixel read is acceptable here — reference analysis is a
        // one-shot user action, not the per-frame path.
        val histogramSignature = HistogramSignature.fromBins(histogram.bins)
        val subjectRegionBox = aiProfile?.primarySubjectBox ?: subjectBox
        val subjectExposureStats = subjectRegionBox?.let { box ->
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            RegionLumaStats.compute(
                argb = pixels,
                width = bitmap.width,
                height = bitmap.height,
                region = AnalysisRegion(box.left, box.top, box.right, box.bottom),
                transform = ExposureConfig.resolveTransform(),
            )
        }

        val exposureProfile = ReferenceExposureProfile(
            mean = histogram.mean,
            median = histogram.median.toFloat(),
            p95 = histogram.percentile95.toFloat(),
            p99 = histogram.percentile99.toFloat(),
            highlightCoverage = zebra.coveragePercentage,
            shadowCoverage = histogram.clippedShadowPercentage,
            histogramSignature = histogramSignature,
            subjectExposure = subjectExposureStats,
        )
        val colorProfile = ReferenceColorProfile(
            avgR = colorStats.avgR,
            avgG = colorStats.avgG,
            avgB = colorStats.avgB,
            warmthScore = colorStats.warmthScore,
            tintScore = colorStats.tintScore,
        )
        val storedAi = aiProfile?.copy(embeddingId = embedding?.id, embeddingModelId = embedding?.modelId)

        // Phase 16.1: the MobileCLIP semantic expert runs ONCE here (import only). Fail-safe: any
        // error yields null and the analyzer falls back to deterministic experts — never blocks import.
        val semanticObservation = semanticEngine?.observe(bitmap)

        // Phase 16: derive the Creative Scene Model ONCE, here at import, from the analysis already
        // computed above plus the (optional) semantic observation. Never runs on the per-frame path.
        val creativeScene = CreativeSceneAnalyzer.analyze(
            exposure = exposureProfile,
            color = colorProfile,
            ai = storedAi,
            faceBox = subjectBox,
            semantic = semanticObservation,
        )

        val profile = ReferenceProfile(
            id = id,
            name = name,
            createdAtMs = nowMs,
            imageUri = imageUri,
            width = bitmap.width,
            height = bitmap.height,
            exposure = exposureProfile,
            color = colorProfile,
            subject = subject,
            face = face,
            options = options,
            ai = storedAi,
            creativeScene = creativeScene,
        )
        return ReferenceAnalysis(profile, embedding)
    }

    private fun buildAiProfile(snapshot: SceneSnapshot): ReferenceAiProfile? {
        val sceneMode = snapshot.sceneMode.value
        val primary = snapshot.primarySubject.value
        // Nothing understood at all (every model down) → no AI section.
        if (sceneMode == null && primary == null && !snapshot.embedding.present) return null
        val strategy = ComparisonStrategySelector.select(
            sceneMode = sceneMode ?: SceneMode.UNKNOWN,
            primarySubject = primary,
            detectionAvailable = snapshot.subjects.isNotEmpty() || primary != null,
        )
        return ReferenceAiProfile(
            sceneMode = sceneMode ?: SceneMode.UNKNOWN,
            sceneModeConfidence = snapshot.sceneMode.confidence,
            strategy = strategy,
            primarySubjectType = primary?.type ?: PrimarySubjectType.UNKNOWN,
            primarySubjectCategory = primary?.category ?: SubjectCategory.UNKNOWN,
            primarySubjectRawLabel = primary?.rawLabel,
            primarySubjectConfidence = primary?.confidence ?: 0f,
            primarySubjectBox = primary?.boundingBox,
            subjects = snapshot.subjects.map {
                ReferenceAiSubject(
                    category = it.category,
                    rawLabel = it.rawLabel,
                    confidence = it.confidence,
                    box = it.boundingBox,
                )
            },
            compositionSignature = snapshot.layoutSignature.value?.toList() ?: emptyList(),
            segmentationCoverage = snapshot.segmentationSummary?.coverage,
            segmentationPixelAccurate = snapshot.segmentationSummary?.pixelAccurate == true,
            embeddingId = null,     // filled by the caller once the embedding object exists
            embeddingModelId = null,
        )
    }

    private fun buildEmbedding(
        snapshot: SceneSnapshot,
        profileId: String,
        nowMs: Long,
    ): ReferenceEmbedding? {
        val vector = snapshot.embedding.value ?: return null
        val modelId = snapshot.embeddingModelId ?: return null
        return ReferenceEmbedding(
            id = "$profileId-emb",
            modelId = modelId,
            dimensions = vector.size,
            values = vector.toList(),
            createdAtMs = nowMs,
        )
    }
}
