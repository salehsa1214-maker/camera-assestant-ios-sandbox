package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.ExposureVerdict
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.ZebraResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM tests for [ExposureAnalyzer]: highlight from Zebra, shadow from Histogram. */
class ExposureAnalyzerTest {

    private fun hist(shadowPct: Float) = HistogramResult(
        moduleId = "exposure",
        bins = IntArray(256),
        totalPixels = 100,
        mean = 0f,
        median = 0,
        percentile95 = 0,
        percentile99 = 0,
        clippedHighlightPixels = 0,
        clippedShadowPixels = 0,
        clippedHighlightPercentage = 0f,
        clippedShadowPercentage = shadowPct,
        effectiveStride = 1,
    )

    private fun zebra(coverage: Float) = ZebraResult(
        moduleId = "exposure",
        spec = ZebraSpec.Level(100),
        pixelsMatched = 0,
        totalPixels = 100,
        coveragePercentage = coverage,
        mask = null,
        effectiveStride = 1,
    )

    @Test fun clippedHighlights() {
        val r = ExposureAnalyzer.analyze(hist(shadowPct = 0f), zebra(coverage = 20f))
        assertTrue(r.highlightDetected)
        assertFalse(r.shadowDetected)
        assertEquals(20f, r.highlightCoverage, 0.001f)
        assertEquals(ExposureVerdict.HIGHLIGHT_CLIP, r.exposureState)
    }

    @Test fun clippedShadows() {
        val r = ExposureAnalyzer.analyze(hist(shadowPct = 20f), zebra(coverage = 0f))
        assertTrue(r.shadowDetected)
        assertFalse(r.highlightDetected)
        assertEquals(20f, r.shadowCoverage, 0.001f)
        assertEquals(ExposureVerdict.SHADOW_CLIP, r.exposureState)
    }

    @Test fun mixed() {
        val r = ExposureAnalyzer.analyze(hist(shadowPct = 15f), zebra(coverage = 25f))
        assertTrue(r.highlightDetected)
        assertTrue(r.shadowDetected)
        assertEquals(ExposureVerdict.MIXED, r.exposureState)
    }

    @Test fun normal() {
        val r = ExposureAnalyzer.analyze(hist(shadowPct = 0f), zebra(coverage = 0f))
        assertFalse(r.highlightDetected)
        assertFalse(r.shadowDetected)
        assertEquals(ExposureVerdict.NORMAL, r.exposureState)
        assertEquals(1f, r.exposureConfidence, 0.001f)
    }

    @Test fun detectionUsesOnePercentThreshold() {
        assertFalse(ExposureAnalyzer.analyze(hist(0f), zebra(1f)).highlightDetected) // exactly 1% not > 1%
        assertTrue(ExposureAnalyzer.analyze(hist(0f), zebra(1.1f)).highlightDetected)
    }

    @Test fun confidenceSaturatesForStrongClip() {
        val strong = ExposureAnalyzer.analyze(hist(0f), zebra(coverage = 10f))
        assertEquals(1f, strong.exposureConfidence, 0.001f)
        val weak = ExposureAnalyzer.analyze(hist(0f), zebra(coverage = 1.9f))
        assertEquals(0.1f, weak.exposureConfidence, 0.02f)
    }

    @Test fun confidenceForNormalFallsNearBoundary() {
        // Normal but half-way to the 1% threshold -> less certain.
        val near = ExposureAnalyzer.analyze(hist(shadowPct = 0.5f), zebra(coverage = 0f))
        assertEquals(ExposureVerdict.NORMAL, near.exposureState)
        assertEquals(0.5f, near.exposureConfidence, 0.001f)
    }
}
