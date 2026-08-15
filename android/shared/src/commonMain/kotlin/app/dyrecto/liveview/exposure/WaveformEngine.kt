package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.WaveformResult

/**
 * Builds a luma waveform from the shared [LumaField] — a per-column luma distribution, computed
 * on-device from the same sampled luma buffer the histogram/zebra use (no Bitmap traversal). The
 * LumaField scan is a regular row-major grid (`cols = ceil(sourceWidth / effectiveStride)`), so each
 * sample's column is recovered as `index % cols` and mapped into [targetColumns] output buckets.
 *
 * Output is intensity only (sample counts per cell, capped); colour/brightness mapping is the
 * renderer's job. Stateful: owns a reusable intensity buffer to avoid per-frame allocation.
 */
class WaveformEngine(
    private val moduleId: String = "exposure",
    /** Horizontal resolution of the waveform (output columns). */
    private val targetColumns: Int = 256,
    /** Vertical luma bins (0 = darkest). */
    private val bins: Int = 256,
    /** Per-cell intensity cap so a single bright column can't dominate normalization. */
    private val cellCap: Int = 255,
) {
    private var intensity = IntArray(targetColumns * bins)

    fun compute(luma: LumaField): WaveformResult {
        val out = intensity
        out.fill(0)
        val buf = luma.luma
        val n = luma.count
        val stride = luma.effectiveStride.coerceAtLeast(1)
        val srcCols = ((luma.sourceWidth + stride - 1) / stride).coerceAtLeast(1)

        var max = 0
        var i = 0
        while (i < n) {
            val v = buf[i]
            val srcCol = i % srcCols
            val outCol = if (srcCols <= 1) 0 else srcCol * (targetColumns - 1) / (srcCols - 1)
            val bin = if (bins >= 256) v else (v * bins) / 256
            val idx = outCol * bins + bin
            val cur = out[idx]
            if (cur < cellCap) {
                val nv = cur + 1
                out[idx] = nv
                if (nv > max) max = nv
            }
            i++
        }

        return WaveformResult(
            moduleId = moduleId,
            columns = targetColumns,
            bins = bins,
            intensity = out.copyOf(),
            maxIntensity = max,
            totalSamples = n,
            effectiveStride = luma.effectiveStride,
        )
    }
}
