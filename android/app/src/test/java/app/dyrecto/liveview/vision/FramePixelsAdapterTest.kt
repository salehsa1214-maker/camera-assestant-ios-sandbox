package app.dyrecto.liveview.vision

import android.graphics.Bitmap
import app.dyrecto.liveview.exposure.AdaptiveSampler
import app.dyrecto.liveview.exposure.ColorAccumulator
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.HistogramEngine
import app.dyrecto.liveview.exposure.LumaField
import app.dyrecto.liveview.exposure.LuminanceAnalyzer
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.modules.ExposureModule
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.HistogramResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

/**
 * Proves the KMP FramePixels boundary is behavior-identical to the old direct Bitmap access:
 * [FrameAnalysisRequest.readArgb] delegates to the exact `Bitmap.getPixels` call the exposure
 * scan used to make, and [ExposureModule] produces the same histogram/color statistics through
 * the boundary as the pure analyzers produce from the same pixel data directly.
 */
class FramePixelsAdapterTest {

    private val w = 8
    private val h = 6

    /** Deterministic ARGB pattern — the single pixel source for both paths under comparison. */
    private fun patternPixel(i: Int): Int {
        val r = (i * 7) % 256
        val g = (i * 13) % 256
        val b = (i * 29) % 256
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun mockBitmap(recycled: Boolean = false): Bitmap {
        val bmp = Mockito.mock(Bitmap::class.java)
        Mockito.`when`(bmp.width).thenReturn(w)
        Mockito.`when`(bmp.height).thenReturn(h)
        Mockito.`when`(bmp.isRecycled).thenReturn(recycled)
        Mockito.doAnswer { inv ->
            val dest = inv.getArgument<IntArray>(0)
            for (i in 0 until w * h) dest[i] = patternPixel(i)
            null
        }.`when`(bmp).getPixels(
            Mockito.any(IntArray::class.java),
            Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
            Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
        )
        return bmp
    }

    private fun request(bmp: Bitmap) =
        FrameAnalysisRequest(bmp, Mockito.mock(FrameContext::class.java), receivedAtMs = 0L)

    @Test
    fun readArgbDelegatesWithTheExactLegacyGetPixelsArgs() {
        val bmp = mockBitmap()
        val req = request(bmp)

        assertEquals(w, req.width)
        assertEquals(h, req.height)
        assertTrue(req.isAvailable)

        val dest = IntArray(w * h)
        req.readArgb(dest)

        // Exactly the call ExposureModule used to make: getPixels(dest, 0, width, 0, 0, width, height).
        Mockito.verify(bmp).getPixels(dest, 0, w, 0, 0, w, h)
        for (i in 0 until w * h) assertEquals(patternPixel(i), dest[i])
    }

    @Test
    fun recycledBitmapReportsUnavailable() {
        assertFalse(request(mockBitmap(recycled = true)).isAvailable)
    }

    @Test
    fun exposureModuleSeesIdenticalPixelDataThroughTheBoundary() = runTest {
        // Path 1: the real module, fed through FrameAnalysisRequest's FramePixels boundary.
        val module = ExposureModule(subjectRegionProvider = { null })
        val results = module.analyze(request(mockBitmap()))
        val histogram = results.filterIsInstance<HistogramResult>().single()
        val colors = results.filterIsInstance<ColorStatsResult>().single()

        // Path 2: the same pattern fed straight into the pure analyzers (pre-KMP data path).
        val pixels = IntArray(w * h) { patternPixel(it) }
        val accumulator = ColorAccumulator()
        val luma = LuminanceAnalyzer.analyze(
            pixels, w, h, AdaptiveSampler().stride, LumaField(),
            ExposureConfig.resolveTransform(), accumulator,
        )
        val expected = HistogramEngine("exposure").compute(luma)

        assertEquals(expected.totalPixels, histogram.totalPixels)
        assertEquals(expected.mean, histogram.mean, 0f)
        assertEquals(expected.median, histogram.median)
        assertEquals(expected.percentile95, histogram.percentile95)
        assertEquals(expected.percentile99, histogram.percentile99)
        assertArrayEquals(expected.bins, histogram.bins)

        assertEquals(accumulator.avgR, colors.avgR, 0f)
        assertEquals(accumulator.avgG, colors.avgG, 0f)
        assertEquals(accumulator.avgB, colors.avgB, 0f)
        assertEquals(accumulator.sampleCount, colors.sampleCount)
    }
}
