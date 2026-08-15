package app.dyrecto.liveview.exposure

/**
 * Pure region-exposure statistics over an already-read ARGB pixel array (Phase 12).
 *
 * This deliberately reuses the pixel buffer the exposure scan has ALREADY pulled from the Bitmap
 * — it never triggers a second `Bitmap.getPixels`. Walking only the region's pixels keeps the
 * cost proportional to the subject box, and the luma math is byte-identical to
 * [LuminanceAnalyzer] (same BT.709 integer approximation, same [AnalysisColorTransform] hook),
 * so subject stats and frame stats stay comparable.
 */
object RegionLumaStats {

    /**
     * Compute [SubjectExposureStats] for [region] (normalized 0..1) of the [width]×[height]
     * frame in [argb], sampling every [stride]-th pixel. Returns null when the clamped region
     * contains no samples.
     */
    fun compute(
        argb: IntArray,
        width: Int,
        height: Int,
        region: AnalysisRegion,
        stride: Int = 1,
        transform: AnalysisColorTransform = Rec709Transform,
        highlightLumaMin: Int = AnalysisRegionRegistry.highlightLumaMin,
        shadowLumaMax: Int = AnalysisRegionRegistry.shadowLumaMax,
    ): SubjectExposureStats? {
        if (width <= 0 || height <= 0 || stride < 1) return null
        val x0 = (region.left * width).toInt().coerceIn(0, width - 1)
        val x1 = (region.right * width).toInt().coerceIn(x0 + 1, width)
        val y0 = (region.top * height).toInt().coerceIn(0, height - 1)
        val y1 = (region.bottom * height).toInt().coerceIn(y0 + 1, height)

        val useLutTransform = transform !== Rec709Transform
        val bins = IntArray(256)
        var count = 0
        var sum = 0L
        var y = y0
        while (y < y1) {
            val rowBase = y * width
            var x = x0
            while (x < x1) {
                val p = argb[rowBase + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val luma = if (useLutTransform) {
                    transform.toLuma(r, g, b).coerceIn(0, 255)
                } else {
                    (54 * r + 183 * g + 19 * b) shr 8
                }
                bins[luma]++
                sum += luma
                count++
                x += stride
            }
            y += stride
        }
        if (count == 0) return null

        // Median from the region histogram.
        var median = 0
        var cumulative = 0
        for (i in 0..255) {
            cumulative += bins[i]
            if (cumulative * 2 >= count) {
                median = i
                break
            }
        }

        var highlight = 0
        for (i in highlightLumaMin.coerceIn(0, 255)..255) highlight += bins[i]
        var shadow = 0
        for (i in 0..shadowLumaMax.coerceIn(0, 255)) shadow += bins[i]

        return SubjectExposureStats(
            mean = sum.toFloat() / count,
            median = median.toFloat(),
            highlightCoverage = 100f * highlight / count,
            shadowCoverage = 100f * shadow / count,
            sampleCount = count,
        )
    }
}
