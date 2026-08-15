package app.dyrecto.liveview.vision.results

/**
 * Average-color statistics for one analyzed frame (Phase 9), produced by `ExposureModule` from the
 * same fused pixel scan as Histogram/Zebra (see
 * [app.dyrecto.liveview.exposure.ColorAccumulator] — no second Bitmap pass).
 *
 * [warmthScore] and [tintScore] are deliberately simple, deterministic channel balances used by the
 * Shot Reference assistant for relative drift detection. This is a **local approximation, not a
 * calibrated CCT meter** — future phases may replace it with a stronger color-science model.
 */
data class ColorStatsResult(
    override val moduleId: String,
    /** Average red channel (0..255) of the sampled pixels. */
    val avgR: Float,
    /** Average green channel (0..255) of the sampled pixels. */
    val avgG: Float,
    /** Average blue channel (0..255) of the sampled pixels. */
    val avgB: Float,
    /** Number of pixels sampled into the averages. */
    val sampleCount: Int,
) : VisionResult {
    /** Blue-vs-red balance: positive = warmer (red-leaning), negative = cooler (blue-leaning). */
    val warmthScore: Float get() = avgR - avgB

    /** Green-vs-magenta balance: positive = green-leaning, negative = magenta-leaning. */
    val tintScore: Float get() = avgG - (avgR + avgB) / 2f
}
