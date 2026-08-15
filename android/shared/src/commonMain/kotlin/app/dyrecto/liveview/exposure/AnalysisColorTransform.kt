package app.dyrecto.liveview.exposure

/**
 * Phase 8: converts a source-profile RGB sample (0..255 per channel) into an analysis luma value
 * (0..255, BT.709) before it enters [LumaField]. This is the seam between "what profile the camera
 * recorded in" and "what Histogram/Zebra/ExposureAnalyzer evaluate" — those engines only ever see
 * already-normalized luma and never need to know which transform ran.
 *
 * Applied inline inside [LuminanceAnalyzer]'s existing per-pixel loop — never a second Bitmap scan.
 */
interface AnalysisColorTransform {
    fun toLuma(r: Int, g: Int, b: Int): Int
}

/**
 * Identity/default transform: the current Rec.709 integer approximation, unchanged from pre-Phase-8
 * behavior. [LuminanceAnalyzer] special-cases this object by reference to keep the hot default path
 * free of the virtual call / float math a LUT transform needs.
 */
object Rec709Transform : AnalysisColorTransform {
    override fun toLuma(r: Int, g: Int, b: Int): Int = (54 * r + 183 * g + 19 * b) shr 8
}
