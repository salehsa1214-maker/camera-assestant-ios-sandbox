package app.dyrecto.liveview.vision.results

/** Per-band coverage of a false-color exposure map. */
data class FalseColorBand(
    val index: Int,
    val label: String,
    /** Inclusive IRE lower bound of the band. */
    val loIre: Int,
    /** Exclusive IRE upper bound of the band (Int.MAX_VALUE for the top clip band). */
    val hiIre: Int,
    /** Display colour as 0xRRGGBB; the renderer uses this to tint matching pixels. */
    val colorRgb: Int,
    val pixels: Int,
    val percentage: Float,
)

/**
 * False-color exposure coverage for one frame, computed on-device from the shared
 * [app.dyrecto.liveview.exposure.LumaField]. Carries per-band pixel coverage; the Android renderer
 * uses [app.dyrecto.liveview.exposure.FalseColorScale] to build the per-pixel recolour LUT.
 */
data class FalseColorResult(
    override val moduleId: String,
    val bands: List<FalseColorBand>,
    val totalSamples: Int,
    val effectiveStride: Int,
) : VisionResult
