package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.ZebraResult

/**
 * Computes zebra coverage from the shared [LumaField] (Phase 6). Reuses the same luma buffer as
 * [HistogramEngine] — it performs no Bitmap traversal of its own.
 *
 * ### Luma ↔ IRE mapping (recovered convention + documented fallback)
 * Sony expresses zebra thresholds in IRE and reads the camera Y plane as **studio-swing** video
 * (`YRangeType.VIDEO_16_235`: code 16 → 0 IRE, code 235 → 100 IRE — see `README-sony-re.md`). Our
 * luma is BT.709 reconstructed from full-range RGB, so we apply the same 16–235 mapping to align our
 * thresholds with Sony's IRE numbers. To avoid a division per pixel we convert the *threshold* to a
 * luma code once and compare in luma space:
 *
 * ```
 * lumaForIre(ire) = 16 + ire * 219 / 100     // inverse of ire = (luma-16)*100/219
 * ```
 *
 * Full-range white (luma 255) maps to ~109 IRE (super-white), matching Sony's over-100 behaviour.
 */
class ZebraEngine(private val moduleId: String = "exposure") {

    fun compute(luma: LumaField, spec: ZebraSpec, generateMask: Boolean = false): ZebraResult {
        val buf = luma.luma
        val n = luma.count

        // Precompute the inclusive luma window [lo, hi] this spec matches.
        val lo: Int
        val hi: Int
        when (spec) {
            is ZebraSpec.Level -> {
                lo = lumaForIre(spec.level)
                hi = Int.MAX_VALUE
            }
            is ZebraSpec.Range -> {
                lo = lumaForIre(spec.center - spec.range)
                hi = lumaForIre(spec.center + spec.range)
            }
        }

        val mask = if (generateMask) ByteArray(n) else null
        var matched = 0
        var i = 0
        while (i < n) {
            val v = buf[i]
            if (v in lo..hi) {
                matched++
                if (mask != null) mask[i] = 1
            }
            i++
        }

        val coverage = if (n > 0) matched.toFloat() / n * 100f else 0f
        return ZebraResult(
            moduleId = moduleId,
            spec = spec,
            pixelsMatched = matched,
            totalPixels = n,
            coveragePercentage = coverage,
            mask = mask,
            effectiveStride = luma.effectiveStride,
        )
    }

    companion object {
        /** IRE (studio-swing) → luma code, clamped to a valid 0..255 comparison bound. */
        fun lumaForIre(ire: Int): Int = (16 + ire * 219 / 100).coerceIn(0, 255)
    }
}
