package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.ExposureChannelState
import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.ExposureVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [ExposureStateMachine]: frame-based persistence/recovery hysteresis on top of
 * the raw per-frame [ExposureAnalyzer] verdict. Feeds raw [ExposureResult]s directly (as
 * [app.dyrecto.liveview.vision.modules.ExposureModule] would after
 * `ExposureAnalyzer.analyze`) and asserts on the enriched result's confirmed/state/counter fields.
 *
 * Uses the real [ExposureAnalyzer] thresholds ([ExposureAnalyzer.PERSISTENCE_FRAMES] /
 * [ExposureAnalyzer.RECOVERY_FRAMES] = 5, clip/recovery pct = 5f/3f) so the tests fail loudly if
 * those constants are ever retuned without updating this suite's expectations.
 */
class ExposureStateMachineTest {

    private fun raw(highlightCoverage: Float = 0f, shadowCoverage: Float = 0f) = ExposureResult(
        moduleId = "exposure",
        highlightDetected = highlightCoverage > ExposureAnalyzer.DETECT_PCT,
        shadowDetected = shadowCoverage > ExposureAnalyzer.DETECT_PCT,
        highlightCoverage = highlightCoverage,
        shadowCoverage = shadowCoverage,
        exposureState = ExposureVerdict.NORMAL, // not read by the state machine
        exposureConfidence = 1f,
    )

    private val clip = ExposureAnalyzer.HIGHLIGHT_CLIP_PCT + 1f // 6f: above clip threshold
    private val clean = 0f // well below recovery threshold
    private val persistFrames = ExposureAnalyzer.PERSISTENCE_FRAMES
    private val recoverFrames = ExposureAnalyzer.RECOVERY_FRAMES

    // ---- highlight persistence ----

    @Test fun highlightConfirmsExactlyAtPersistenceFrames() {
        val sm = ExposureStateMachine()
        lateinit var last: ExposureResult
        repeat(persistFrames - 1) {
            last = sm.update(raw(highlightCoverage = clip))
            assertFalse("must not confirm before persistence met", last.highlightConfirmed)
            assertEquals(ExposureChannelState.CLIPPING, last.highlightState)
        }
        last = sm.update(raw(highlightCoverage = clip))
        assertTrue("must confirm exactly at persistence threshold", last.highlightConfirmed)
        assertEquals(ExposureChannelState.CONFIRMED, last.highlightState)
        assertEquals(persistFrames, last.highlightPersistenceFrames)
    }

    @Test fun singleFrameSpikeNeverConfirms() {
        val sm = ExposureStateMachine()
        sm.update(raw(highlightCoverage = clip))
        val after = sm.update(raw(highlightCoverage = clean)) // breaks the streak immediately
        assertFalse(after.highlightConfirmed)
        assertEquals(0, after.highlightPersistenceFrames)
        assertEquals(ExposureChannelState.NORMAL, after.highlightState)
    }

    @Test fun alternatingClippedUnclippedNeverConfirms() {
        val sm = ExposureStateMachine()
        var last: ExposureResult? = null
        repeat(20) { i ->
            val coverage = if (i % 2 == 0) clip else clean
            last = sm.update(raw(highlightCoverage = coverage))
        }
        assertFalse("alternating frames must never reach persistence", last!!.highlightConfirmed)
    }

    @Test fun rapidFlickerInHysteresisBandHoldsConfirmedState() {
        val sm = ExposureStateMachine()
        repeat(persistFrames) { sm.update(raw(highlightCoverage = clip)) } // confirm
        // Flicker between just-below-clip and just-above-recovery: neither counts as clean
        // (>= recovery threshold) nor as a fresh clip breach, so CONFIRMED holds, no false recovery.
        val midBand = (ExposureAnalyzer.HIGHLIGHT_RECOVERY_PCT + ExposureAnalyzer.HIGHLIGHT_CLIP_PCT) / 2f
        var last: ExposureResult? = null
        repeat(20) {
            last = sm.update(raw(highlightCoverage = midBand))
        }
        assertTrue("mid-band coverage stays confirmed (not below recovery threshold)", last!!.highlightConfirmed)
        assertEquals(0, last!!.highlightRecoveryFrames)
    }

    // ---- highlight recovery ----

    @Test fun highlightRecoversExactlyAtRecoveryFrames() {
        val sm = ExposureStateMachine()
        repeat(persistFrames) { sm.update(raw(highlightCoverage = clip)) } // confirm
        lateinit var last: ExposureResult
        repeat(recoverFrames - 1) {
            last = sm.update(raw(highlightCoverage = clean))
            assertTrue("must stay confirmed before recovery met", last.highlightConfirmed)
            assertEquals(ExposureChannelState.RECOVERING, last.highlightState)
        }
        last = sm.update(raw(highlightCoverage = clean))
        assertFalse("must recover exactly at recovery threshold", last.highlightConfirmed)
        assertEquals(ExposureChannelState.NORMAL, last.highlightState)
        assertEquals(0, last.highlightPersistenceFrames)
    }

    @Test fun singleCleanFrameNeverRecovers() {
        val sm = ExposureStateMachine()
        repeat(persistFrames) { sm.update(raw(highlightCoverage = clip)) } // confirm
        sm.update(raw(highlightCoverage = clean))
        val after = sm.update(raw(highlightCoverage = clip)) // breaks the recovery streak
        assertTrue("still confirmed: recovery streak was broken", after.highlightConfirmed)
        assertEquals(0, after.highlightRecoveryFrames)
    }

    @Test fun longClippingThenLongRecoveryThenReClipRearms() {
        val sm = ExposureStateMachine()
        repeat(persistFrames * 3) { sm.update(raw(highlightCoverage = clip)) } // long hold
        var last = sm.update(raw(highlightCoverage = clean))
        assertTrue(last.highlightConfirmed) // still confirmed, recovery not yet met
        repeat(recoverFrames * 3 - 1) { last = sm.update(raw(highlightCoverage = clean)) } // long recovery
        assertFalse(last.highlightConfirmed)
        // Repeated clipping after recovery re-arms and confirms again.
        repeat(persistFrames) { last = sm.update(raw(highlightCoverage = clip)) }
        assertTrue("re-arm after recovery must confirm again", last.highlightConfirmed)
    }

    // ---- shadow channel (independent tracker, mirrors highlight) ----

    @Test fun shadowChannelIndependentOfHighlight() {
        val sm = ExposureStateMachine()
        var last: ExposureResult? = null
        repeat(persistFrames) {
            last = sm.update(raw(highlightCoverage = clean, shadowCoverage = clip))
        }
        assertTrue(last!!.shadowConfirmed)
        assertFalse("highlight channel must be unaffected by shadow activity", last!!.highlightConfirmed)
        assertEquals(ExposureChannelState.CONFIRMED, last!!.shadowState)
        assertEquals(ExposureChannelState.NORMAL, last!!.highlightState)
    }

    @Test fun bothChannelsConfirmIndependentlyInSameFrames() {
        val sm = ExposureStateMachine()
        var last: ExposureResult? = null
        repeat(persistFrames) {
            last = sm.update(raw(highlightCoverage = clip, shadowCoverage = clip))
        }
        assertTrue(last!!.highlightConfirmed)
        assertTrue(last!!.shadowConfirmed)
    }

    // ---- reset ----

    @Test fun resetClearsBothChannels() {
        val sm = ExposureStateMachine()
        repeat(persistFrames) { sm.update(raw(highlightCoverage = clip, shadowCoverage = clip)) }
        sm.reset()
        val after = sm.update(raw(highlightCoverage = clean, shadowCoverage = clean))
        assertFalse(after.highlightConfirmed)
        assertFalse(after.shadowConfirmed)
        assertEquals(0, after.highlightPersistenceFrames)
        assertEquals(0, after.shadowPersistenceFrames)
    }
}
