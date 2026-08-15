package app.dyrecto.liveview.exposure

/**
 * Phase 8: applies a 3D LUT (e.g. the Sony S-Log3 monitoring LUT) to each RGB sample before deriving
 * luma, so Histogram/Zebra/ExposureAnalyzer evaluate the LUT-normalized image instead of the raw Log
 * encoding. See [Lut3D] for the interpolation and [AnalysisColorTransform] for how this plugs into
 * [LuminanceAnalyzer].
 */
class Lut3DColorTransform(private val lut: Lut3D) : AnalysisColorTransform {

    override fun toLuma(r: Int, g: Int, b: Int): Int {
        val out = lut.sample(r / 255f, g / 255f, b / 255f)
        val rr = (out[0] * 255f).toInt().coerceIn(0, 255)
        val gg = (out[1] * 255f).toInt().coerceIn(0, 255)
        val bb = (out[2] * 255f).toInt().coerceIn(0, 255)
        val luma = (54 * rr + 183 * gg + 19 * bb) shr 8
        return luma.coerceIn(0, 255)
    }
}
