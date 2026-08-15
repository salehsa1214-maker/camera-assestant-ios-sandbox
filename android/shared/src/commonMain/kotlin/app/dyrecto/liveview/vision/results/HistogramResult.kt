package app.dyrecto.liveview.vision.results

/**
 * Canonical exposure statistics for one analyzed frame (Phase 6). Produced by
 * [app.dyrecto.liveview.exposure.HistogramEngine] from the shared
 * [app.dyrecto.liveview.exposure.LumaField]; consumed by
 * [app.dyrecto.liveview.exposure.ExposureAnalyzer], the Scene layer, the developer
 * screen, and future False Color / Waveform / RGB Parade / AI consumers. Every future exposure
 * feature must read these stats instead of rescanning the image.
 *
 * All counts and percentages are computed over the *sampled* population ([totalPixels] == number of
 * luma samples, which equals the full pixel count only at [effectiveStride] == 1). Percentages are
 * therefore unbiased estimates of the full frame under downsampling.
 *
 * Note: [bins] is a shared, reusable array owned by the producing engine — treat it as read-only and
 * do not retain it across frames. (Equality/hashCode intentionally ignore array identity nuances;
 * this type is a snapshot for display/derivation, not a map key.)
 */
data class HistogramResult(
    override val moduleId: String,
    /** 256 luma bins (index 0..255), counts over the sampled population. */
    val bins: IntArray,
    val totalPixels: Int,
    val mean: Float,
    val median: Int,
    val percentile95: Int,
    val percentile99: Int,
    val clippedHighlightPixels: Int,
    val clippedShadowPixels: Int,
    val clippedHighlightPercentage: Float,
    val clippedShadowPercentage: Float,
    /** Sampling stride used this frame (1 = full resolution; >1 = downsampled fallback). */
    val effectiveStride: Int,
) : VisionResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HistogramResult) return false
        return moduleId == other.moduleId &&
            totalPixels == other.totalPixels &&
            mean == other.mean &&
            median == other.median &&
            percentile95 == other.percentile95 &&
            percentile99 == other.percentile99 &&
            clippedHighlightPixels == other.clippedHighlightPixels &&
            clippedShadowPixels == other.clippedShadowPixels &&
            clippedHighlightPercentage == other.clippedHighlightPercentage &&
            clippedShadowPercentage == other.clippedShadowPercentage &&
            effectiveStride == other.effectiveStride &&
            bins.contentEquals(other.bins)
    }

    override fun hashCode(): Int {
        var result = moduleId.hashCode()
        result = 31 * result + totalPixels
        result = 31 * result + median
        result = 31 * result + effectiveStride
        result = 31 * result + bins.contentHashCode()
        return result
    }
}
