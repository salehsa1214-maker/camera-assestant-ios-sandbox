package app.dyrecto.liveview.exposure

/**
 * Performs the single per-frame pixel scan and produces the canonical [LumaField] (Phase 6).
 *
 * This is the ONLY component that walks the pixel data. Histogram, Zebra, and every future exposure
 * tool consume the resulting [LumaField] instead of rescanning — see the package [architecture note]
 * (`README-sony-re.md`) and [LumaField].
 *
 * ### Luminance formula — BT.709 (documented fallback)
 * Sony's "Monitor & Control" reads the **camera Y plane** directly and processes it in native code we
 * cannot recover (see `README-sony-re.md`). Our app only has the decoded live-view JPEG as ARGB, so
 * we reconstruct a Rec.709 luma from RGB with the integer approximation
 *
 * ```
 * Y = (54*R + 183*G + 19*B) >> 8      // coefficients ≈ 0.2126 / 0.7152 / 0.0722, sum = 256
 * ```
 *
 * Integer math, no floating point, no per-pixel allocation. Channels are unpacked with bit math
 * (identical to `android.graphics.Color.red/green/blue`) so this class stays free of the Android
 * framework and is unit-testable on a plain JVM.
 *
 * ### Sampling stride
 * [stride] == 1 scans every pixel (full resolution — the preferred mode). [stride] > 1 samples every
 * Nth pixel in both axes (stride² fewer samples) as a performance fallback driven by
 * [AdaptiveSampler]. The stride is recorded on the [LumaField] so diagnostics can report whether the
 * frame ran at full resolution or downsampled. Stats computed downstream are over the *sampled*
 * population, which is an unbiased estimate of the full frame.
 */
object LuminanceAnalyzer {

    /**
     * Fill [out] with the luma of [argb] (length must be >= [width] * [height], ARGB-8888), sampling
     * every [stride]-th pixel in both axes. Returns [out] for chaining.
     *
     * [argb] is read-only. [out] is reused in place (its buffer grows only when a larger frame or a
     * smaller stride needs more samples).
     *
     * [transform] (Phase 8) is applied per sampled pixel before the luma is stored, so a Log profile
     * (e.g. S-Log3) can be normalized to Rec.709 before Histogram/Zebra/ExposureAnalyzer see it — see
     * [AnalysisColorTransform]. Defaults to [Rec709Transform], which is special-cased by reference to
     * keep the default path identical to pre-Phase-8 behavior (no virtual call, no float math).
     *
     * [colorAccumulator] (Phase 9) optionally receives the average raw R/G/B of the same sampled
     * pixels — fused into this loop so the Shot Reference color comparison never rescans the Bitmap.
     */
    fun analyze(
        argb: IntArray,
        width: Int,
        height: Int,
        stride: Int,
        out: LumaField,
        transform: AnalysisColorTransform = Rec709Transform,
        colorAccumulator: ColorAccumulator? = null,
    ): LumaField {
        require(stride >= 1) { "stride must be >= 1, was $stride" }
        require(argb.size >= width * height) {
            "argb too small: ${argb.size} < ${width * height}"
        }

        // Upper bound on samples: ceil(width/stride) * ceil(height/stride).
        val cols = (width + stride - 1) / stride
        val rows = (height + stride - 1) / stride
        out.ensureCapacity(cols * rows)
        val luma = out.luma

        val useLutTransform = transform !== Rec709Transform

        var n = 0
        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        var y = 0
        while (y < height) {
            val rowBase = y * width
            var x = 0
            while (x < width) {
                val p = argb[rowBase + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                luma[n++] = if (useLutTransform) {
                    transform.toLuma(r, g, b)
                } else {
                    (54 * r + 183 * g + 19 * b) shr 8
                }
                if (colorAccumulator != null) {
                    sumR += r
                    sumG += g
                    sumB += b
                }
                x += stride
            }
            y += stride
        }

        out.commit(count = n, sourceWidth = width, sourceHeight = height, effectiveStride = stride)
        colorAccumulator?.commit(sumR, sumG, sumB, n)
        return out
    }
}
