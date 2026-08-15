package app.dyrecto.liveview.reference

import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.creative.CreativeSceneModel
import kotlinx.serialization.Serializable

/**
 * Immutable, analyzed representation of the user's reference image (Phase 9 — Shot Reference).
 *
 * Produced once by `ReferenceAnalyzer` when the user picks an image; consumed continuously by
 * the comparator against the live Vision outputs. All geometry is **normalized 0.0–1.0**
 * (never absolute pixels) so a reference photographed at any resolution compares cleanly against
 * live frames of any resolution.
 *
 * Phase 10: [ai] carries the AI perception understanding (scene mode, primary subject of ANY
 * type, strategy, composition, embedding reference). Face ([face]) is just one possible subject
 * kind now, kept for human-specific signals and as the fallback when AI is unavailable —
 * pre-Phase-10 profiles load with `ai = null` and behave exactly as before.
 */
@Serializable
data class ReferenceProfile(
    val id: String,
    val name: String,
    val createdAtMs: Long,

    /** URI of the app-private copy of the reference image (for the UI thumbnail), if persisted. */
    val imageUri: String?,

    /** Analyzed image dimensions (after any downscale), for diagnostics only. */
    val width: Int,
    val height: Int,

    val exposure: ReferenceExposureProfile,
    val color: ReferenceColorProfile,
    /** Largest detected face as the subject, or null when the reference contains no face. */
    val subject: ReferenceSubjectProfile?,
    val face: ReferenceFaceProfile?,

    val options: ReferenceMonitorOptions,

    /** Phase 10 AI understanding; null on legacy profiles or when every AI model failed. */
    val ai: ReferenceAiProfile? = null,

    /**
     * Phase 16 (Creative Scene Understanding): the creative-intent model derived once at import
     * from this reference's existing analysis. Null on legacy profiles or when nothing could be
     * inferred — the reasoning pipeline then behaves exactly as pre-Phase-16.
     */
    val creativeScene: CreativeSceneModel? = null,

    /**
     * Phase 15 (Storyboard): this shot's completion progress. Persisted with the storyboard —
     * once [ShotCompletion.completed] flips true it stays true across restarts until the shot is
     * removed/replaced or progress is explicitly reset. Legacy profiles load with the default
     * (Pending). Current Match is never stored here — it is recomputed live every frame.
     */
    val completion: ShotCompletion = ShotCompletion(),

    /**
     * Camera settings (ISO/shutter/aperture/WB/profile/fps/codec) captured from live telemetry when
     * this shot was created — the authoritative record for "Apply reference settings" and settings-
     * drift alerts. Null on legacy profiles or when no camera was connected at capture time.
     */
    val cameraSettings: ReferenceCameraSettings? = null,
)

/**
 * Phase 15: a single storyboard shot's completion progress. [confirmations] counts the
 * independent successful holds achieved so far (each separated by leaving the match threshold);
 * [completed] latches true once enough confirmations are reached and is permanent for the shot.
 * The in-flight hold-streak timer is NOT here — it is transient runtime state in the monitor.
 */
@Serializable
data class ShotCompletion(
    val completed: Boolean = false,
    val confirmations: Int = 0,
)

/**
 * The AI perception section of a reference profile: what the reference IS (scene mode, primary
 * subject in stable semantic terms, chosen strategy) plus the comparison baselines that are pure
 * data (composition signature). The raw embedding vector is deliberately NOT here — it lives in
 * its own versioned [ReferenceEmbedding] file, referenced by [embeddingId]/[embeddingModelId].
 */
@Serializable
data class ReferenceAiProfile(
    val sceneMode: SceneMode,
    val sceneModeConfidence: Float,
    val strategy: ComparisonStrategy,

    val primarySubjectType: PrimarySubjectType,
    val primarySubjectCategory: SubjectCategory,
    /** Raw detector label — diagnostics + matching affinity only, never drives strategy. */
    val primarySubjectRawLabel: String? = null,
    val primarySubjectConfidence: Float,
    val primarySubjectBox: NormalizedRect? = null,

    /** Every detected subject (semantic terms) for composition/multi-object comparison. */
    val subjects: List<ReferenceAiSubject> = emptyList(),

    /** 3×3 confidence-weighted layout signature from CompositionAnalyzer. */
    val compositionSignature: List<Float> = emptyList(),

    /** One-shot segmentation statistics from reference analysis (optional capability). */
    val segmentationCoverage: Float? = null,
    val segmentationPixelAccurate: Boolean = false,

    /** Reference to the versioned embedding file; null when the embedder was unavailable. */
    val embeddingId: String? = null,
    val embeddingModelId: String? = null,

    val schemaVersion: Int = 1,
)

/** One detected subject stored on the profile (semantic category + normalized geometry). */
@Serializable
data class ReferenceAiSubject(
    val category: SubjectCategory,
    val rawLabel: String? = null,
    val confidence: Float,
    val box: NormalizedRect,
)

/**
 * Exposure fingerprint of the reference image, computed through the SAME pipeline as live frames
 * (LuminanceAnalyzer → HistogramEngine/ZebraEngine → ExposureAnalyzer) — never a second,
 * reimplemented exposure path.
 */
@Serializable
data class ReferenceExposureProfile(
    /** Mean luma (0..255). */
    val mean: Float,
    /** Median luma bin (0..255). */
    val median: Float,
    /** 95th percentile luma bin (0..255). */
    val p95: Float,
    /** 99th percentile luma bin (0..255). */
    val p99: Float,
    /** Zebra coverage % at the active threshold (same signal that drives highlight alerts). */
    val highlightCoverage: Float,
    /** Histogram shadow-clip % (same signal that drives shadow alerts). */
    val shadowCoverage: Float,
    /**
     * Phase 12: coarse normalized 32-bin histogram signature (see `HistogramSignature`) for the
     * perceptual histogram-overlap metric. Empty on legacy profiles — the exposure composite
     * simply renormalizes the divergence weight away.
     */
    val histogramSignature: List<Float> = emptyList(),
    /**
     * Phase 12: exposure stats inside the reference subject box (AI primary subject, else face).
     * Null on legacy profiles or subject-less references.
     */
    val subjectExposure: SubjectExposureStats? = null,
)

/**
 * Simple, deterministic color balance of the reference image. This is a **local approximation,
 * not a calibrated CCT meter** — warmth is a red-vs-blue balance and tint a green-vs-magenta
 * balance, sufficient for relative drift detection against the live feed. Future phases may
 * replace it with a stronger color-science model.
 */
@Serializable
data class ReferenceColorProfile(
    val avgR: Float,
    val avgG: Float,
    val avgB: Float,
    /** `avgR - avgB`: positive = warmer (red-leaning). */
    val warmthScore: Float,
    /** `avgG - (avgR + avgB) / 2`: positive = green-leaning. */
    val tintScore: Float,
)

/**
 * The reference subject — for Phase 9, the largest detected face. All values normalized 0.0–1.0
 * relative to the analyzed image; absolute pixel coordinates are never the canonical reference.
 */
@Serializable
data class ReferenceSubjectProfile(
    val normalizedCenterX: Float,
    val normalizedCenterY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float,
    val normalizedArea: Float,
)

/** Face/eye availability captured from the reference image. */
@Serializable
data class ReferenceFaceProfile(
    val faceDetected: Boolean,
    val eyesDetected: Boolean,
    val faceBox: NormalizedRect?,
    val eyeCount: Int,
)

/** A rectangle in normalized 0.0–1.0 image coordinates (top-left origin, y grows downward). */
@Serializable
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
    val area: Float get() = width * height
}
