package app.dyrecto.liveview.perception

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.reference.CurrentReferenceInput
import app.dyrecto.liveview.reference.ReferenceAlertRules
import app.dyrecto.liveview.reference.ReferenceAlertState
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceConfig
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalState
import app.dyrecto.liveview.reference.ReferenceStateMachine
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.reference.SceneComparator
import app.dyrecto.liveview.vision.results.HistogramResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end Phase 12 pipeline: SceneComparator → HumanPerceptionEngine → ReferenceStateMachine →
 * ReferenceAlertRules, driven by real histogram inputs against a real profile (exposure signal).
 * Proves the amplitude + temporal hysteresis compose without double-latching and that alerts
 * carry perceptual severity wording.
 */
class PerceptionPipelineTest {

    private val refMean = 120f

    private val profile = ReferenceProfile(
        id = "ref-1", name = "r", createdAtMs = 0, imageUri = null, width = 100, height = 100,
        exposure = ReferenceExposureProfile(
            mean = refMean, median = refMean, p95 = 200f, p99 = 240f,
            highlightCoverage = 0f, shadowCoverage = 0f,
        ),
        color = ReferenceColorProfile(0f, 0f, 0f, 0f, 0f),
        subject = null, face = null,
        options = ReferenceMonitorOptions(tolerance = ReferenceTolerance.MEDIUM),
    )

    private val tuning = PerceptualTuning.forTolerance(ReferenceTolerance.MEDIUM)

    private inner class Driver {
        val engine = HumanPerceptionEngine()
        val stateMachine = ReferenceStateMachine()
        val idGen = AlertIdGenerator()
        var alertState = ReferenceAlertState()
        var nowMs = 1_000L
        var lastState: ReferenceSignalState = ReferenceSignalState.NORMAL
        var lastMatched = true

        fun tick(meanDelta: Float): List<Alert> {
            nowMs += 100
            val mean = refMean + meanDelta
            val input = CurrentReferenceInput(
                histogram = HistogramResult(
                    moduleId = "t", bins = IntArray(256), totalPixels = 10_000,
                    mean = mean, median = mean.toInt(), percentile95 = 200, percentile99 = 240,
                    clippedHighlightPixels = 0, clippedShadowPixels = 0,
                    clippedHighlightPercentage = 0f, clippedShadowPercentage = 0f,
                    effectiveStride = 1,
                ),
            )
            val raw = SceneComparator.compare(profile, input, nowMs)
            val perceived = engine.evaluate(raw, profile, input, null, tuning)
            val enriched = stateMachine.update(
                perceived,
                ReferenceConfig.thresholdsFor(ReferenceTolerance.MEDIUM),
            )
            lastState = enriched.signal(ReferenceSignal.EXPOSURE).state
            lastMatched = enriched.signal(ReferenceSignal.EXPOSURE).matched
            val result = ReferenceAlertRules.evaluate(alertState, enriched, idGen)
            alertState = result.state
            return result.alerts
        }
    }

    @Test
    fun `slight exposure change never produces an instruction`() {
        val d = Driver()
        val alerts = mutableListOf<Alert>()
        // The founding scenario: mean 126 vs 120 — inside the perceptual dead zone.
        repeat(30) { alerts += d.tick(6f) }
        assertTrue(alerts.isEmpty())
        assertEquals(ReferenceSignalState.NORMAL, d.lastState)
    }

    @Test
    fun `oscillation inside the hysteresis band never confirms - no double latch`() {
        val d = Driver()
        val alerts = mutableListOf<Alert>()
        // Deltas whose noticeability lands between leave (0.10) and enter (0.20).
        repeat(60) { i -> alerts += d.tick(if (i % 2 == 0) 20f else 22f) }
        assertTrue("no alert may fire from in-band oscillation", alerts.isEmpty())
        assertEquals(ReferenceSignalState.NORMAL, d.lastState)
    }

    @Test
    fun `large mismatch confirms once with severity wording, recovers once after hysteresis`() {
        val d = Driver()
        val alerts = mutableListOf<Alert>()
        repeat(5) { alerts += d.tick(0f) } // stable matched baseline
        assertTrue(alerts.isEmpty())

        // Sustained large drift: damped at first (transient suspicion), then the history window
        // catches up, amplitude hysteresis enters, and 5 consecutive frames confirm.
        repeat(20) { alerts += d.tick(50f) }
        val driftAlerts = alerts.filter { it.type == AlertType.REFERENCE_EXPOSURE_DRIFT }
        assertEquals(1, driftAlerts.size)
        assertTrue(
            "alert must carry perceptual severity wording, was: ${driftAlerts[0].message}",
            driftAlerts[0].message.contains("brighter"),
        )
        assertEquals(ReferenceSignalState.CONFIRMED_DRIFT, d.lastState)

        // Recovery: matched frames must persist through BOTH hysteresis layers before the single
        // recovery alert fires.
        alerts.clear()
        repeat(20) { alerts += d.tick(0f) }
        val recoveries = alerts.filter { it.type == AlertType.REFERENCE_RECOVERED }
        assertEquals(1, recoveries.size)
        assertEquals(ReferenceSignalState.NORMAL, d.lastState)
        assertTrue(alerts.none { it.type == AlertType.REFERENCE_EXPOSURE_DRIFT })
    }

    @Test
    fun `recovery does not fire while noticeability holds in the leave band`() {
        val d = Driver()
        repeat(5) { d.tick(0f) }
        repeat(20) { d.tick(50f) } // confirmed
        // Falling back only into the hysteresis band (leave < n < enter) must keep the drift.
        val alerts = mutableListOf<Alert>()
        repeat(15) { alerts += d.tick(21f) }
        assertTrue("no recovery inside the hold band", alerts.none { it.type == AlertType.REFERENCE_RECOVERED })
        assertTrue(!d.lastMatched)
    }

    @Test
    fun `perception data rides along on the published result`() {
        val d = Driver()
        repeat(3) { d.tick(0f) }
        d.tick(50f)
        // Perceptual metadata must be attached for diagnostics/reasoning.
        val input = CurrentReferenceInput(
            histogram = HistogramResult(
                moduleId = "t", bins = IntArray(256), totalPixels = 10_000,
                mean = 170f, median = 170, percentile95 = 200, percentile99 = 240,
                clippedHighlightPixels = 0, clippedShadowPixels = 0,
                clippedHighlightPercentage = 0f, clippedShadowPercentage = 0f,
                effectiveStride = 1,
            ),
        )
        val raw = SceneComparator.compare(profile, input, 99_000L)
        val perceived = d.engine.evaluate(raw, profile, input, null, tuning)
        val perception = perceived.signal(ReferenceSignal.EXPOSURE).perception
        assertTrue(perception != null)
        assertTrue(perception!!.humanNoticeability > 0f)
        assertTrue(perception.importance > 0f)
        assertTrue(perception.strategyWeight > 0f)
    }
}
