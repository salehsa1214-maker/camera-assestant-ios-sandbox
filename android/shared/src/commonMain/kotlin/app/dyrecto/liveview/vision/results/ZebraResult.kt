package app.dyrecto.liveview.vision.results

import app.dyrecto.liveview.exposure.ZebraSpec

/**
 * Zebra coverage for one analyzed frame (Phase 6). Produced by
 * [app.dyrecto.liveview.exposure.ZebraEngine] from the shared
 * [app.dyrecto.liveview.exposure.LumaField] (no separate pixel traversal).
 *
 * [spec] is the active zebra threshold (Zebra2 level or Zebra1 range band, in IRE). [pixelsMatched]
 * and [coveragePercentage] are over the sampled population. [mask] is reserved for the future Overlay
 * Engine — the generation API exists now but the overlay bitmap is deferred (always null this phase).
 */
data class ZebraResult(
    override val moduleId: String,
    val spec: ZebraSpec,
    val pixelsMatched: Int,
    val totalPixels: Int,
    val coveragePercentage: Float,
    /** Per-sample zebra mask (1 = matched), or null while overlay rendering is deferred. */
    val mask: ByteArray? = null,
    /** Sampling stride used this frame (1 = full resolution; >1 = downsampled fallback). */
    val effectiveStride: Int,
) : VisionResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZebraResult) return false
        return moduleId == other.moduleId &&
            spec == other.spec &&
            pixelsMatched == other.pixelsMatched &&
            totalPixels == other.totalPixels &&
            coveragePercentage == other.coveragePercentage &&
            effectiveStride == other.effectiveStride &&
            (mask?.contentEquals(other.mask ?: ByteArray(0)) ?: (other.mask == null))
    }

    override fun hashCode(): Int {
        var result = moduleId.hashCode()
        result = 31 * result + spec.hashCode()
        result = 31 * result + pixelsMatched
        result = 31 * result + coveragePercentage.hashCode()
        result = 31 * result + effectiveStride
        return result
    }
}
