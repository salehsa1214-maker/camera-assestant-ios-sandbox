package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure-JVM tests for [ZebraEngine] (IRE-mapped, Sony 16–235 studio-swing). */
class ZebraEngineTest {

    private fun argb(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    private fun field(width: Int, height: Int, pixel: (Int, Int) -> Int): LumaField {
        val px = IntArray(width * height)
        for (y in 0 until height) for (x in 0 until width) px[y * width + x] = pixel(x, y)
        return LuminanceAnalyzer.analyze(px, width, height, 1, LumaField())
    }

    private val engine = ZebraEngine()

    @Test fun ireMappingMatchesStudioSwing() {
        assertEquals(16, ZebraEngine.lumaForIre(0))     // 0 IRE  -> code 16
        assertEquals(235, ZebraEngine.lumaForIre(100))  // 100 IRE -> code 235
        assertEquals(125, ZebraEngine.lumaForIre(50))   // 16 + 50*219/100
    }

    @Test fun level100MatchesWhiteNotGrey() {
        val white = engine.compute(field(8, 8) { _, _ -> argb(255) }, ZebraSpec.Level(100))
        assertEquals(100f, white.coveragePercentage, 0.001f)
        assertEquals(64, white.pixelsMatched)

        val grey = engine.compute(field(8, 8) { _, _ -> argb(128) }, ZebraSpec.Level(100))
        assertEquals(0f, grey.coveragePercentage, 0.001f)
    }

    @Test fun level100IgnoresBlack() {
        val black = engine.compute(field(4, 4) { _, _ -> argb(0) }, ZebraSpec.Level(100))
        assertEquals(0, black.pixelsMatched)
    }

    @Test fun halfWhiteHalfBlackIs50Percent() {
        val r = engine.compute(field(8, 8) { x, _ -> if (x < 4) argb(255) else argb(0) }, ZebraSpec.Level(100))
        assertEquals(50f, r.coveragePercentage, 0.001f)
        assertEquals(32, r.pixelsMatched)
    }

    @Test fun rangeBandMatchesCenterExcludesExtremes() {
        // Range(50,10) -> IRE [40,60] -> luma [103,147]. Grey 128 is inside; white/black are outside.
        val spec = ZebraSpec.Range(center = 50, range = 10)
        assertEquals(100f, engine.compute(field(4, 4) { _, _ -> argb(128) }, spec).coveragePercentage, 0.001f)
        assertEquals(0f, engine.compute(field(4, 4) { _, _ -> argb(255) }, spec).coveragePercentage, 0.001f)
        assertEquals(0f, engine.compute(field(4, 4) { _, _ -> argb(0) }, spec).coveragePercentage, 0.001f)
    }

    @Test fun levelBoundaryIsInclusive() {
        // Level(70) -> luma >= lumaForIre(70) = 169. 169 matches, 168 does not.
        val at = engine.compute(field(2, 2) { _, _ -> argb(169) }, ZebraSpec.Level(70))
        assertEquals(4, at.pixelsMatched)
        val below = engine.compute(field(2, 2) { _, _ -> argb(168) }, ZebraSpec.Level(70))
        assertEquals(0, below.pixelsMatched)
    }

    @Test fun maskDeferredByDefault() {
        val r = engine.compute(field(2, 2) { _, _ -> argb(255) }, ZebraSpec.Level(100))
        assertNull(r.mask)
    }

    @Test fun effectiveStridePropagates() {
        val px = IntArray(16) { argb(255) }
        val luma = LuminanceAnalyzer.analyze(px, 4, 4, 2, LumaField())
        assertEquals(2, engine.compute(luma, ZebraSpec.Level(100)).effectiveStride)
    }
}
