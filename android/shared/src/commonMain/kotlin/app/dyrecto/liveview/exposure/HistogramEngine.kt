package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.HistogramResult

/**
 * Builds the canonical 256-bin luma histogram and exposure statistics from a shared [LumaField]
 * (Phase 6). Histogram is only *one* consumer of the [LumaField] — it never rescans the Bitmap.
 *
 * Stateful by design: owns a reusable `IntArray(256)` working-bins buffer so the per-frame hot path
 * allocates nothing large. Instantiated per Vision worker (single-threaded), so no synchronization is
 * needed. The emitted [HistogramResult] carries a defensive 1 KB copy of the bins so the immutable
 * snapshot stays stable while the working buffer is reused next frame.
 *
 * ### Clip thresholds (documented fallback)
 * Sony flags clipping in the camera's native pipeline (unrecoverable). We mirror the app's previous
 * highlight/shadow behaviour so alert timing stays continuous: highlight = luma bins ≥ [HIGHLIGHT_CLIP_BIN]
 * (was `luminance > 250`), shadow = luma bins ≤ [SHADOW_CLIP_BIN] (was `luminance < 5`).
 */
class HistogramEngine(private val moduleId: String = "exposure") {

    private val bins = IntArray(BIN_COUNT)

    fun compute(luma: LumaField): HistogramResult {
        bins.fill(0)
        val buf = luma.luma
        val n = luma.count
        var i = 0
        var sum = 0L
        while (i < n) {
            val v = buf[i]
            bins[v]++
            sum += v
            i++
        }

        val total = n
        val mean = if (total > 0) sum.toFloat() / total else 0f
        val median = percentileBin(bins, total, 0.50f)
        val p95 = percentileBin(bins, total, 0.95f)
        val p99 = percentileBin(bins, total, 0.99f)

        var highlightClipped = 0
        for (b in HIGHLIGHT_CLIP_BIN..255) highlightClipped += bins[b]
        var shadowClipped = 0
        for (b in 0..SHADOW_CLIP_BIN) shadowClipped += bins[b]

        val highlightPct = if (total > 0) highlightClipped.toFloat() / total * 100f else 0f
        val shadowPct = if (total > 0) shadowClipped.toFloat() / total * 100f else 0f

        return HistogramResult(
            moduleId = moduleId,
            bins = bins.copyOf(),
            totalPixels = total,
            mean = mean,
            median = median,
            percentile95 = p95,
            percentile99 = p99,
            clippedHighlightPixels = highlightClipped,
            clippedShadowPixels = shadowClipped,
            clippedHighlightPercentage = highlightPct,
            clippedShadowPercentage = shadowPct,
            effectiveStride = luma.effectiveStride,
        )
    }

    /** Smallest bin index whose cumulative count reaches [fraction] of [total]. 0 when empty. */
    private fun percentileBin(bins: IntArray, total: Int, fraction: Float): Int {
        if (total <= 0) return 0
        val target = Math.ceil(total.toDouble() * fraction).toLong().coerceAtLeast(1L)
        var cum = 0L
        var b = 0
        while (b < BIN_COUNT) {
            cum += bins[b]
            if (cum >= target) return b
            b++
        }
        return BIN_COUNT - 1
    }

    companion object {
        const val BIN_COUNT = 256
        /** Inclusive lower bin of the highlight-clip band (fallback, matches prior `>250`). */
        const val HIGHLIGHT_CLIP_BIN = 251
        /** Inclusive upper bin of the shadow-clip band (fallback, matches prior `<5`). */
        const val SHADOW_CLIP_BIN = 4
    }
}
