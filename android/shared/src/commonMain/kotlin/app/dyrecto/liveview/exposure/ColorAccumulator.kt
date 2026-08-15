package app.dyrecto.liveview.exposure

/**
 * Reusable per-frame average-RGB accumulator (Phase 9), filled by [LuminanceAnalyzer] inside the
 * same pixel loop that builds the [LumaField] — average color costs no second Bitmap scan, per the
 * "one scan, shared luminance" mandate.
 *
 * Averages are over the *sampled* population (same stride as the luma), which is an unbiased
 * estimate of the full frame. Values are raw source RGB (pre-[AnalysisColorTransform]): the Shot
 * Reference comparison only needs reference and live to travel the identical path, and warmth/tint
 * are relative channel balances, not calibrated color science.
 */
class ColorAccumulator {
    /** Average red channel (0..255) of the sampled pixels. */
    var avgR: Float = 0f
        private set

    /** Average green channel (0..255) of the sampled pixels. */
    var avgG: Float = 0f
        private set

    /** Average blue channel (0..255) of the sampled pixels. */
    var avgB: Float = 0f
        private set

    /** Number of pixels sampled into the averages this frame. */
    var sampleCount: Int = 0
        private set

    /** Called by [LuminanceAnalyzer] after the scan to publish this frame's channel sums. */
    internal fun commit(sumR: Long, sumG: Long, sumB: Long, count: Int) {
        sampleCount = count
        if (count > 0) {
            avgR = sumR.toFloat() / count
            avgG = sumG.toFloat() / count
            avgB = sumB.toFloat() / count
        } else {
            avgR = 0f
            avgG = 0f
            avgB = 0f
        }
    }
}
