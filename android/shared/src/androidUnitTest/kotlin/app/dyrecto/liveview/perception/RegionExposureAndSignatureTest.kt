package app.dyrecto.liveview.perception

import app.dyrecto.liveview.exposure.AnalysisRegion
import app.dyrecto.liveview.exposure.RegionLumaStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegionExposureAndSignatureTest {

    /** Gray ARGB pixel of luma ~v (R=G=B=v → BT.709 luma = v within rounding). */
    private fun gray(v: Int): Int = (0xFF shl 24) or (v shl 16) or (v shl 8) or v

    @Test
    fun `region stats cover only the region`() {
        // 4x4 frame: left half black, right half near-white.
        val w = 4
        val h = 4
        val argb = IntArray(w * h) { i -> if (i % w < 2) gray(0) else gray(254) }

        val left = RegionLumaStats.compute(
            argb, w, h, AnalysisRegion(0f, 0f, 0.5f, 1f),
            highlightLumaMin = 250, shadowLumaMax = 16,
        )
        assertNotNull(left)
        assertEquals(0f, left!!.mean, 1.5f)
        assertEquals(100f, left.shadowCoverage, 0.01f)
        assertEquals(0f, left.highlightCoverage, 0.01f)

        val right = RegionLumaStats.compute(
            argb, w, h, AnalysisRegion(0.5f, 0f, 1f, 1f),
            highlightLumaMin = 250, shadowLumaMax = 16,
        )
        assertNotNull(right)
        assertTrue(right!!.mean > 250f)
        assertEquals(100f, right.highlightCoverage, 0.01f)
        assertEquals(0f, right.shadowCoverage, 0.01f)
        assertEquals(8, right.sampleCount)
    }

    @Test
    fun `median comes from the region histogram`() {
        val w = 3
        val h = 1
        val argb = intArrayOf(gray(10), gray(100), gray(200))
        val stats = RegionLumaStats.compute(argb, w, h, AnalysisRegion(0f, 0f, 1f, 1f))
        assertNotNull(stats)
        // BT.709 integer luma of a gray pixel is v-1..v due to >>8 truncation.
        assertEquals(100f, stats!!.median, 1.5f)
    }

    @Test
    fun `degenerate region yields null`() {
        assertNull(RegionLumaStats.compute(IntArray(4), 2, 2, AnalysisRegion(0f, 0f, 1f, 1f), stride = 0))
        assertNull(RegionLumaStats.compute(IntArray(0), 0, 0, AnalysisRegion(0f, 0f, 1f, 1f)))
    }

    // ---- Histogram signature ----

    @Test
    fun `signature is normalized and downsampled`() {
        val bins = IntArray(256)
        bins[0] = 50
        bins[255] = 50
        val sig = HistogramSignature.fromBins(bins)
        assertEquals(PerceptualThresholds.HISTOGRAM_SIGNATURE_BINS, sig.size)
        assertEquals(0.5f, sig.first(), 1e-6f)
        assertEquals(0.5f, sig.last(), 1e-6f)
        assertEquals(1f, sig.sum(), 1e-5f)
    }

    @Test
    fun `divergence is zero for identical and one for disjoint distributions`() {
        val a = IntArray(256).also { it[10] = 100 }
        val b = IntArray(256).also { it[250] = 100 }
        val sigA = HistogramSignature.fromBins(a)
        val sigB = HistogramSignature.fromBins(b)
        assertEquals(0f, HistogramSignature.divergence(sigA, sigA)!!, 1e-6f)
        assertEquals(1f, HistogramSignature.divergence(sigA, sigB)!!, 1e-6f)
    }

    @Test
    fun `incomparable signatures yield null divergence`() {
        val sig = HistogramSignature.fromBins(IntArray(256).also { it[5] = 1 })
        assertNull(HistogramSignature.divergence(emptyList(), sig))
        assertNull(HistogramSignature.divergence(sig, sig.dropLast(1)))
        assertEquals(emptyList<Float>(), HistogramSignature.fromBins(IntArray(256)))
    }
}
