package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect

/** Where a segmentation mask came from. */
enum class SegmentationSource {
    /** A real model-produced pixel mask. */
    MODEL,

    /** Bounding-box fallback: the "mask" is just the subject box (no pixel accuracy). */
    BBOX_FALLBACK,
}

/**
 * Segmentation output — an OPTIONAL capability. Bounding boxes are sufficient for most
 * references; segmentation is invoked only when a strategy explicitly requires pixel-accurate
 * masks (currently: one-shot mask statistics at reference-analysis time). The interface keeps
 * the pipeline segmentation-ready without making it a required step for every scene.
 */
data class SegmentationResult(
    val maskAvailable: Boolean,
    /** Fraction of the frame covered by the subject/foreground mask (0..1). */
    val coverage: Float,
    /** Tight bounds of the mask in normalized coordinates, when computable. */
    val maskBox: NormalizedRect?,
    val source: SegmentationSource,
)
