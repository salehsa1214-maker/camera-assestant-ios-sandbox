package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [HistogramEngine]. Builds a [LumaField] via [LuminanceAnalyzer] from synthetic
 * ARGB frames, then asserts the 256-bin statistics.
 */
class HistogramEngineTest {

    private fun argb(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    private fun field(width: Int, height: Int, pixel: (Int, Int) -> Int): LumaField {
        val px = IntArray(width * height)
        for (y in 0 until height) for (x in 0 until width) px[y * width + x] = pixel(x, y)
        return LuminanceAnalyzer.analyze(px, width, height, 1, LumaField())
    }

    private val engine = HistogramEngine()

    @Test fun whiteFrameClipsHighlights() {
        val r = engine.compute(field(8, 8) { _, _ -> argb(255) })
        assertEquals(64, r.totalPixels)
        assertEquals(255f, r.mean, 0.001f)
        assertEquals(255, r.median)
        assertEquals(255, r.percentile95)
        assertEquals(255, r.percentile99)
        assertEquals(64, r.clippedHighlightPixels)
        assertEquals(0, r.clippedShadowPixels)
        assertEquals(100f, r.clippedHighlightPercentage, 0.001f)
        assertEquals(64, r.bins[255])
    }

    @Test fun blackFrameClipsShadows() {
        val r = engine.compute(field(8, 8) { _, _ -> argb(0) })
        assertEquals(0f, r.mean, 0.001f)
        assertEquals(0, r.median)
        assertEquals(64, r.clippedShadowPixels)
        assertEquals(0, r.clippedHighlightPixels)
        assertEquals(100f, r.clippedShadowPercentage, 0.001f)
        assertEquals(64, r.bins[0])
    }

    @Test fun greyFrameIsSingleMidBinNoClip() {
        val r = engine.compute(field(8, 8) { _, _ -> argb(128) })
        assertEquals(128f, r.mean, 0.001f)
        assertEquals(128, r.median)
        assertEquals(64, r.bins[128])
        assertEquals(0, r.clippedHighlightPixels)
        assertEquals(0, r.clippedShadowPixels)
    }

    @Test fun gradientSpreadsAcrossBins() {
        // 256x1 ramp: one pixel per bin.
        val r = engine.compute(field(256, 1) { x, _ -> argb(x) })
        assertEquals(256, r.totalPixels)
        assertTrue("mean near midpoint", r.mean in 126f..129f)
        assertEquals(127, r.median)
        assertEquals(243, r.percentile95)
        assertEquals(253, r.percentile99)
        // Highlight band = bins 251..255 (5 pixels); shadow band = bins 0..4 (5 pixels).
        assertEquals(5, r.clippedHighlightPixels)
        assertEquals(5, r.clippedShadowPixels)
        for (b in 0 until 256) assertEquals("bin $b", 1, r.bins[b])
    }

    @Test fun mixedExposureCountsBothClips() {
        // Half white, half black over 8x8.
        val r = engine.compute(field(8, 8) { x, _ -> if (x < 4) argb(255) else argb(0) })
        assertEquals(32, r.clippedHighlightPixels)
        assertEquals(32, r.clippedShadowPixels)
        assertEquals(50f, r.clippedHighlightPercentage, 0.001f)
        assertEquals(50f, r.clippedShadowPercentage, 0.001f)
    }

    @Test fun binsSnapshotIsStableAcrossFrames() {
        val first = engine.compute(field(4, 4) { _, _ -> argb(255) })
        // A second compute reuses the working buffer; the first result must not mutate.
        engine.compute(field(4, 4) { _, _ -> argb(0) })
        assertEquals(16, first.bins[255])
        assertEquals(0, first.bins[0])
    }
}
