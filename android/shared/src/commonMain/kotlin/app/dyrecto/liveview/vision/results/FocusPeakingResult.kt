package app.dyrecto.liveview.vision.results

/**
 * Focus-peaking coverage for one frame — the fraction of the image on a high-contrast edge, computed
 * on-device from the shared [app.dyrecto.liveview.exposure.LumaField] spatial luma grid. Higher
 * coverage in the intended subject region indicates sharper focus there.
 *
 * [mask] (when generated) is a per-sample flag (1 = peaked) the renderer overlays; the sample grid is
 * `cols = ceil(sourceWidth / effectiveStride)` wide, matching the LumaField scan order.
 */
data class FocusPeakingResult(
    override val moduleId: String,
    val peakedSamples: Int,
    val totalSamples: Int,
    val coveragePercentage: Float,
    /** Gradient-magnitude threshold used (luma units). */
    val threshold: Int,
    val cols: Int,
    val rows: Int,
    val mask: ByteArray? = null,
    val effectiveStride: Int,
) : VisionResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FocusPeakingResult) return false
        return moduleId == other.moduleId &&
            peakedSamples == other.peakedSamples &&
            totalSamples == other.totalSamples &&
            coveragePercentage == other.coveragePercentage &&
            threshold == other.threshold &&
            cols == other.cols &&
            rows == other.rows &&
            effectiveStride == other.effectiveStride &&
            (mask?.contentEquals(other.mask ?: ByteArray(0)) ?: (other.mask == null))
    }

    override fun hashCode(): Int {
        var result = moduleId.hashCode()
        result = 31 * result + peakedSamples
        result = 31 * result + threshold
        result = 31 * result + cols
        result = 31 * result + effectiveStride
        return result
    }
}
