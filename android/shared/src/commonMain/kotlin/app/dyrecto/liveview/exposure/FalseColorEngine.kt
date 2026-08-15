package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.FalseColorBand
import app.dyrecto.liveview.vision.results.FalseColorResult

/**
 * The false-color band table (IRE → display colour) and derived luma LUTs. Documented convention
 * (not calibrated to a specific camera's factory false-color scale): the bands follow the widely
 * used IRE mapping — purple for crushed black, blue for shadows, green near the 18% mid-shadow,
 * grey/pink around middle grey and skin, yellow/orange approaching key, red for clipping. Uses the
 * same studio-swing IRE mapping as [ZebraEngine]/[IreScale] so readings match the camera's numbers.
 */
object FalseColorScale {
    /** Ordered, contiguous bands covering the whole luma range; last band is the clip band. */
    val bands: List<FalseColorBand> = listOf(
        band(0, "Black clip", Int.MIN_VALUE, 2, 0x3B0A5C),
        band(1, "Shadow", 2, 12, 0x2233AA),
        band(2, "Low mid", 12, 42, 0x3A3A3A),
        band(3, "18% under", 42, 48, 0x1FA83C),
        band(4, "Middle grey", 48, 52, 0x9AA0A6),
        band(5, "Skin / key", 52, 58, 0xE58FB0),
        band(6, "High mid", 58, 78, 0xC9C9C9),
        band(7, "One over", 78, 84, 0xE6D02A),
        band(8, "Bright", 84, 93, 0xE8912A),
        band(9, "Near clip", 93, 99, 0xE0552A),
        band(10, "White clip", 99, Int.MAX_VALUE, 0xD11A1A),
    )

    private fun band(i: Int, label: String, lo: Int, hi: Int, rgb: Int) =
        FalseColorBand(i, label, lo, hi, rgb, 0, 0f)

    /** 256-entry luma → band-index LUT (built once; reused by the engine and the renderer). */
    val lumaToBand: IntArray by lazy {
        IntArray(256) { luma -> bandForIre(IreScale.lumaToIre(luma)) }
    }

    /** 256-entry luma → display colour LUT (0xRRGGBB), for the Android recolour pass. */
    val lumaToColor: IntArray by lazy {
        IntArray(256) { luma -> bands[lumaToBand[luma]].colorRgb }
    }

    private fun bandForIre(ire: Int): Int {
        for (b in bands) if (ire >= b.loIre && ire < b.hiIre) return b.index
        return bands.last().index
    }
}

/**
 * Computes per-band false-color coverage from the shared [LumaField] — one pass over the sampled
 * luma, no Bitmap traversal (like [HistogramEngine]/[ZebraEngine]). The per-pixel recolouring for
 * display is a renderer concern that uses [FalseColorScale.lumaToColor].
 */
class FalseColorEngine(private val moduleId: String = "exposure") {

    private val counts = IntArray(FalseColorScale.bands.size)

    fun compute(luma: LumaField): FalseColorResult {
        counts.fill(0)
        val buf = luma.luma
        val n = luma.count
        val lut = FalseColorScale.lumaToBand
        var i = 0
        while (i < n) {
            counts[lut[buf[i]]]++
            i++
        }
        val bands = FalseColorScale.bands.map { b ->
            val c = counts[b.index]
            b.copy(pixels = c, percentage = if (n > 0) c.toFloat() / n * 100f else 0f)
        }
        return FalseColorResult(
            moduleId = moduleId,
            bands = bands,
            totalSamples = n,
            effectiveStride = luma.effectiveStride,
        )
    }
}
