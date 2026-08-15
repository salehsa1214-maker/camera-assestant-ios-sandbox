package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.FocusPeakingResult
import kotlin.math.abs

/**
 * Focus peaking from the shared [LumaField]: flags samples that sit on a high-contrast edge (large
 * local luma gradient). Computed on-device from the same sampled luma grid used by histogram/zebra —
 * no Bitmap traversal. The grid is row-major (`cols = ceil(sourceWidth / effectiveStride)`), so each
 * sample's right/down neighbours are `i+1` / `i+cols`.
 *
 * [threshold] is the gradient magnitude (sum of |Δright| + |Δdown|, in luma units) above which a
 * sample is "peaked". Higher = fewer, stronger edges. This is an exposure/focus assist, not an AF
 * signal — it reports where sharp detail is, mirroring how Sony renders peaking on-device.
 */
class FocusPeakingEngine(
    private val moduleId: String = "exposure",
    private val threshold: Int = 40,
) {
    fun compute(luma: LumaField, generateMask: Boolean = false): FocusPeakingResult {
        val buf = luma.luma
        val n = luma.count
        val stride = luma.effectiveStride.coerceAtLeast(1)
        val cols = ((luma.sourceWidth + stride - 1) / stride).coerceAtLeast(1)
        val rows = if (cols > 0) n / cols else 0

        val mask = if (generateMask) ByteArray(n) else null
        var peaked = 0

        // Only interior samples that have a right and down neighbour within the valid grid.
        var r = 0
        while (r < rows - 1) {
            val rowBase = r * cols
            var c = 0
            while (c < cols - 1) {
                val i = rowBase + c
                val v = buf[i]
                val grad = abs(v - buf[i + 1]) + abs(v - buf[i + cols])
                if (grad >= threshold) {
                    peaked++
                    if (mask != null) mask[i] = 1
                }
                c++
            }
            r++
        }

        val coverage = if (n > 0) peaked.toFloat() / n * 100f else 0f
        return FocusPeakingResult(
            moduleId = moduleId,
            peakedSamples = peaked,
            totalSamples = n,
            coveragePercentage = coverage,
            threshold = threshold,
            cols = cols,
            rows = rows,
            mask = mask,
            effectiveStride = luma.effectiveStride,
        )
    }
}
