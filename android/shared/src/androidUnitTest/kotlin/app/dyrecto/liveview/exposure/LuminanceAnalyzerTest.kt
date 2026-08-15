package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [LuminanceAnalyzer] (BT.709). No Android Bitmap — operates on ARGB [IntArray]s.
 */
class LuminanceAnalyzerTest {

    private fun argb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    private fun solid(width: Int, height: Int, color: Int) = IntArray(width * height) { color }

    @Test fun whiteFrameIsLuma255() {
        val out = LuminanceAnalyzer.analyze(solid(4, 4, argb(255, 255, 255)), 4, 4, 1, LumaField())
        assertEquals(16, out.count)
        for (i in 0 until out.count) assertEquals(255, out.luma[i])
    }

    @Test fun blackFrameIsLuma0() {
        val out = LuminanceAnalyzer.analyze(solid(4, 4, argb(0, 0, 0)), 4, 4, 1, LumaField())
        for (i in 0 until out.count) assertEquals(0, out.luma[i])
    }

    @Test fun greyFrameIsMidLuma() {
        // BT.709 coefficients sum to 256, so a neutral grey maps to itself.
        val out = LuminanceAnalyzer.analyze(solid(2, 2, argb(128, 128, 128)), 2, 2, 1, LumaField())
        for (i in 0 until out.count) assertEquals(128, out.luma[i])
    }

    @Test fun greenIsWeightedHeaviest() {
        val r = LuminanceAnalyzer.analyze(solid(1, 1, argb(255, 0, 0)), 1, 1, 1, LumaField()).luma[0]
        val g = LuminanceAnalyzer.analyze(solid(1, 1, argb(0, 255, 0)), 1, 1, 1, LumaField()).luma[0]
        val b = LuminanceAnalyzer.analyze(solid(1, 1, argb(0, 0, 255)), 1, 1, 1, LumaField()).luma[0]
        // BT.709: G (0.7152) >> R (0.2126) >> B (0.0722).
        assertTrue(g > r && r > b)
        assertEquals(53, r)   // 54*255>>8
        assertEquals(182, g)  // 183*255>>8
        assertEquals(18, b)   // 19*255>>8
    }

    @Test fun gradientPreservesOrdering() {
        // 256x1 horizontal ramp 0..255.
        val px = IntArray(256) { argb(it, it, it) }
        val out = LuminanceAnalyzer.analyze(px, 256, 1, 1, LumaField())
        assertEquals(256, out.count)
        for (i in 0 until 256) assertEquals(i, out.luma[i])
    }

    @Test fun strideSamplesEveryNthPixelBothAxes() {
        val px = IntArray(16) { argb(255, 255, 255) }
        val out = LuminanceAnalyzer.analyze(px, 4, 4, 2, LumaField())
        // ceil(4/2) * ceil(4/2) = 2 * 2 = 4 samples.
        assertEquals(4, out.count)
        assertEquals(2, out.effectiveStride)
    }

    @Test fun reusableFieldIsRefilledNotAppended() {
        val field = LumaField()
        LuminanceAnalyzer.analyze(IntArray(9) { argb(255, 255, 255) }, 3, 3, 1, field)
        assertEquals(9, field.count)
        // Reuse the same field for a smaller frame — count must reflect the new frame only.
        LuminanceAnalyzer.analyze(IntArray(4) { argb(0, 0, 0) }, 2, 2, 1, field)
        assertEquals(4, field.count)
        for (i in 0 until field.count) assertEquals(0, field.luma[i])
    }

    @Test fun defaultTransformMatchesRec709FastPath() {
        // Explicitly passing Rec709Transform must be identical to the default (Phase 8 regression guard).
        val px = IntArray(256) { argb(it, it, it) }
        val explicit = LuminanceAnalyzer.analyze(px, 256, 1, 1, LumaField(), Rec709Transform)
        val default = LuminanceAnalyzer.analyze(px, 256, 1, 1, LumaField())
        for (i in 0 until 256) assertEquals(default.luma[i], explicit.luma[i])
    }

    @Test fun customTransformIsAppliedPerPixel() {
        // A stub transform that always returns a fixed value proves the hook is actually wired in.
        val stub = object : AnalysisColorTransform {
            override fun toLuma(r: Int, g: Int, b: Int): Int = 42
        }
        val out = LuminanceAnalyzer.analyze(solid(4, 4, argb(255, 255, 255)), 4, 4, 1, LumaField(), stub)
        for (i in 0 until out.count) assertEquals(42, out.luma[i])
    }
}
