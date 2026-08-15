package app.dyrecto.liveview.exposure

/**
 * Adaptively chooses the pixel-scan stride to keep exposure analysis inside a time budget (Phase 6).
 *
 * Full-resolution ([stride] == 1) is the *preferred* operating mode — it gives desktop-class accuracy
 * on fast devices. Downsampling is a performance fallback that protects Live View FPS on slower ones.
 * The adaptation is transparent to Scene / Alerts / Overlay: they always consume the same
 * [app.dyrecto.liveview.vision.results.HistogramResult] /
 * [app.dyrecto.liveview.vision.results.ZebraResult] APIs, which merely report the
 * effective stride for diagnostics.
 *
 * Policy (with hysteresis so it does not oscillate every frame):
 *  - if a frame's analysis exceeds [highBudgetNs] for [OVER_STREAK] consecutive frames → raise stride
 *    (quick to protect FPS), up to [MAX_STRIDE];
 *  - if it stays under [lowBudgetNs] for [UNDER_STREAK] consecutive frames → lower stride toward 1
 *    (slow, biased back to full resolution);
 *  - the dead-band between [lowBudgetNs] and [highBudgetNs] is where nothing changes.
 *
 * Single-threaded: called only from the Vision worker. Not thread-safe by design.
 */
class AdaptiveSampler(
    private val highBudgetNs: Long = 5_000_000L, // 5 ms — raise stride above this
    private val lowBudgetNs: Long = 3_000_000L,  // 3 ms — headroom to lower stride below this
) {
    var stride: Int = 1
        private set

    private var overStreak = 0
    private var underStreak = 0

    /** Feed the measured analysis time for the frame just processed; returns the stride to use next. */
    fun record(elapsedNs: Long): Int {
        when {
            elapsedNs > highBudgetNs -> {
                overStreak++
                underStreak = 0
                if (overStreak >= OVER_STREAK && stride < MAX_STRIDE) {
                    stride++
                    overStreak = 0
                }
            }
            elapsedNs < lowBudgetNs -> {
                underStreak++
                overStreak = 0
                if (underStreak >= UNDER_STREAK && stride > 1) {
                    stride--
                    underStreak = 0
                }
            }
            else -> {
                // Dead-band: comfortably within budget — decay both streaks, hold stride.
                overStreak = 0
                underStreak = 0
            }
        }
        return stride
    }

    fun reset() {
        stride = 1
        overStreak = 0
        underStreak = 0
    }

    companion object {
        const val MAX_STRIDE = 4
        /** Consecutive over-budget frames before raising stride (fast reaction). */
        const val OVER_STREAK = 3
        /** Consecutive under-budget frames before lowering stride (slow, full-res-biased). */
        const val UNDER_STREAK = 8
    }
}
