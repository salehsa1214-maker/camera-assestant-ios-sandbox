package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the phone-side monitoring overlays (waveform / false color / focus peaking) that render from
 * the shared LumaField — the same on-device approach Sony's MonitorAssist uses. Frames are built
 * through the real [LuminanceAnalyzer] so the tests exercise the actual sampled-grid geometry the
 * engines rely on. Grey pixels map luma == value under BT.709, giving deterministic expectations.
 */
class OverlayEnginesTest {

    private fun grey(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    /** Builds a LumaField from an ARGB frame at stride 1. */
    private fun field(argb: IntArray, w: Int, h: Int, stride: Int = 1): LumaField =
        LuminanceAnalyzer.analyze(argb, w, h, stride, LumaField())

    @Test
    fun falseColorPutsUniformMidGreyInTheMiddleBand() {
        // luma 128 -> IRE 51 -> band 4 "Middle grey" (48..52).
        val f = field(IntArray(64) { grey(128) }, 8, 8)
        val result = FalseColorEngine().compute(f)
        val dominant = result.bands.maxByOrNull { it.pixels }!!
        assertEquals(4, dominant.index)
        assertEquals(100f, dominant.percentage, 0.01f)
        assertEquals(64, result.totalSamples)
    }

    @Test
    fun falseColorFlagsHighlightAndShadowClip() {
        val bright = FalseColorEngine().compute(field(IntArray(16) { grey(255) }, 4, 4))
        assertEquals("White clip", bright.bands.maxByOrNull { it.pixels }!!.label)
        val dark = FalseColorEngine().compute(field(IntArray(16) { grey(0) }, 4, 4))
        assertEquals("Black clip", dark.bands.maxByOrNull { it.pixels }!!.label)
    }

    @Test
    fun waveformPlacesLumaByColumn() {
        // 8x1 frame: left 4 columns dark (32), right 4 bright (220).
        val argb = IntArray(8) { i -> grey(if (i < 4) 32 else 220) }
        val wf = WaveformEngine(targetColumns = 8, bins = 256).compute(field(argb, 8, 1))
        assertEquals(8, wf.columns)
        // column 0 has its sample at bin 32, not 220; column 7 at bin 220.
        assertEquals(1, wf.intensity[0 * 256 + 32])
        assertEquals(0, wf.intensity[0 * 256 + 220])
        assertEquals(1, wf.intensity[7 * 256 + 220])
        assertEquals(1, wf.maxIntensity)
    }

    @Test
    fun focusPeakingDetectsEdgesButNotFlatFields() {
        val flat = FocusPeakingEngine(threshold = 40).compute(field(IntArray(64) { grey(128) }, 8, 8))
        assertEquals(0, flat.peakedSamples)

        // Vertical edge between column 3 (0) and column 4 (255).
        val edge = IntArray(64) { i -> grey(if ((i % 8) < 4) 0 else 255) }
        val peaked = FocusPeakingEngine(threshold = 40).compute(field(edge, 8, 8), generateMask = true)
        assertTrue("expected edge samples to peak", peaked.peakedSamples > 0)
        assertTrue(peaked.coveragePercentage > 0f)
        assertEquals(8, peaked.cols)
        assertEquals(64, peaked.mask!!.size)
    }

    @Test
    fun ireScaleRoundTripsStudioSwing() {
        assertEquals(0, IreScale.lumaToIre(16))
        assertEquals(100, IreScale.lumaToIre(235))
        assertEquals(16, IreScale.ireToLuma(0))
        assertEquals(235, IreScale.ireToLuma(100))
    }
}
