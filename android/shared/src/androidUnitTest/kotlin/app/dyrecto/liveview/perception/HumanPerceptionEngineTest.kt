package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.CurrentReferenceInput
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalResult
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.CreativePriority
import app.dyrecto.liveview.reference.creative.CreativeSceneModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Drives the engine through a fake evaluator whose noticeability is scripted per frame, so the
 * hysteresis / history / trend behavior is tested in isolation from real evaluator math.
 */
class HumanPerceptionEngineTest {

    private val profile = ReferenceProfile(
        id = "ref", name = "r", createdAtMs = 0, imageUri = null, width = 100, height = 100,
        exposure = ReferenceExposureProfile(120f, 120f, 200f, 240f, 1f, 1f),
        color = ReferenceColorProfile(0f, 0f, 0f, 0f, 0f),
        subject = null, face = null,
        options = ReferenceMonitorOptions(tolerance = ReferenceTolerance.MEDIUM),
    )

    // MEDIUM: enter 0.20, leave 0.10.
    private val tuning = PerceptualTuning.forTolerance(ReferenceTolerance.MEDIUM)

    private class ScriptedEvaluator : PerceptualEvaluator {
        override val signal = ReferenceSignal.EXPOSURE
        var next: PerceptualObservation = PerceptualObservation.UNAVAILABLE

        override fun evaluate(ctx: PerceptualContext) = next
    }

    private fun observation(noticeability: Float) = PerceptualObservation(
        available = true,
        rawDifference = noticeability * 100f,
        perceptualDifference = noticeability,
        noticeability = noticeability,
        confidence = 1f,
        directionConfidence = 1f,
        deadZoneApplied = 10f,
        direction = if (noticeability > 0f) ReferenceDriftDirection.BRIGHTER else ReferenceDriftDirection.NONE,
    )

    private class Driver {
        val evaluator = ScriptedEvaluator()
        val engine = HumanPerceptionEngine(mapOf(ReferenceSignal.EXPOSURE to evaluator))
    }

    private fun Driver.tick(
        noticeability: Float?,
        profile: ReferenceProfile,
        tuning: PerceptualTuning,
        available: Boolean = true,
    ): ReferenceSignalResult {
        evaluator.next = if (noticeability != null) {
            PerceptualObservation(
                available = true, rawDifference = noticeability * 100f,
                perceptualDifference = noticeability, noticeability = noticeability,
                confidence = 1f, directionConfidence = 1f, deadZoneApplied = 10f,
                direction = if (noticeability > 0f) ReferenceDriftDirection.BRIGHTER else ReferenceDriftDirection.NONE,
            )
        } else {
            PerceptualObservation.UNAVAILABLE
        }
        val raw = ReferenceMatchResult(
            active = true, referenceId = profile.id,
            exposureMatch = ReferenceSignalResult(enabled = true, available = available),
        )
        return engine.evaluate(raw, profile, CurrentReferenceInput(), null, tuning)
            .signal(ReferenceSignal.EXPOSURE)
    }

    @Test
    fun `noticeability below enter never drifts`() {
        val d = Driver()
        repeat(20) {
            val s = d.tick(0.15f, profile, tuning)
            assertTrue(s.matched)
            assertEquals(HysteresisState.INSIDE, s.perception!!.hysteresisState)
        }
    }

    @Test
    fun `enter at enter threshold, hold in the band, leave only at leave threshold`() {
        val d = Driver()
        // Establish a stable baseline high enough that damping doesn't mute the entry step.
        repeat(3) { d.tick(0.18f, profile, tuning) }

        val entered = d.tick(0.25f, profile, tuning)
        assertFalse(entered.matched)
        assertEquals(HysteresisState.ENTERED, entered.perception!!.hysteresisState)

        // Falling into the 0.10..0.20 band HOLDS the drift (no oscillation).
        val holding = d.tick(0.15f, profile, tuning)
        assertFalse(holding.matched)
        assertEquals(HysteresisState.HOLDING_LEAVE, holding.perception!!.hysteresisState)

        // Only at/below leave (0.10) does it recover.
        val left = d.tick(0.08f, profile, tuning)
        assertTrue(left.matched)
        assertEquals(HysteresisState.INSIDE, left.perception!!.hysteresisState)
    }

    @Test
    fun `oscillation inside the band never enters`() {
        val d = Driver()
        repeat(30) { i ->
            val s = d.tick(if (i % 2 == 0) 0.12f else 0.19f, profile, tuning)
            assertTrue("frame $i must stay matched", s.matched)
        }
    }

    @Test
    fun `unavailable frames hold state and push no samples`() {
        val d = Driver()
        repeat(3) { d.tick(0.18f, profile, tuning) }
        d.tick(0.30f, profile, tuning) // entered
        val held = d.tick(null, profile, tuning, available = false)
        // Signal reports unavailable downstream; drift state must survive.
        assertFalse(held.available)
        val after = d.tick(0.15f, profile, tuning) // still in the band → still drifting
        assertFalse(after.matched)
    }

    @Test
    fun `single-frame spike is damped and does not enter`() {
        val d = Driver()
        repeat(5) { d.tick(0.02f, profile, tuning) } // stable near-zero baseline
        val spike = d.tick(0.9f, profile, tuning)    // person crosses the frame
        assertTrue("spike must be damped, not latched", spike.matched)
        assertTrue(spike.perception!!.humanNoticeability < tuning.profile.enterNoticeability)
        // Back to normal — still matched.
        assertTrue(d.tick(0.02f, profile, tuning).matched)
    }

    @Test
    fun `sustained rise catches up, classifies INCREASING, and enters`() {
        val d = Driver()
        repeat(4) { d.tick(0.02f, profile, tuning) }
        var lastSignal: ReferenceSignalResult? = null
        // A real step change: damped at first, but raw samples shift the median until it enters.
        repeat(8) { lastSignal = d.tick(0.6f, profile, tuning) }
        assertNotNull(lastSignal)
        assertFalse("sustained drift must eventually enter", lastSignal!!.matched)
        assertEquals(0.6f, lastSignal!!.perception!!.humanNoticeability, 1e-4f)
    }

    @Test
    fun `alternating values classify OSCILLATING and decay direction confidence`() {
        val d = Driver()
        var s: ReferenceSignalResult? = null
        repeat(12) { i -> s = d.tick(if (i % 2 == 0) 0.05f else 0.45f, profile, tuning) }
        val perception = s!!.perception!!
        assertEquals(PerceptualTrend.OSCILLATING, perception.trend)
        assertTrue(
            "direction confidence must decay while oscillating",
            perception.directionConfidence <= tuning.profile.oscillationDirectionDecay + 1e-6f,
        )
    }

    @Test
    fun `reset clears hysteresis and history`() {
        val d = Driver()
        repeat(3) { d.tick(0.18f, profile, tuning) }
        d.tick(0.5f, profile, tuning)
        d.engine.reset()
        val s = d.tick(0.15f, profile, tuning)
        assertTrue(s.matched)
        assertEquals(HysteresisState.INSIDE, s.perception!!.hysteresisState)
    }

    @Test
    fun `drifting signal carries severity wording, matched signal has no message`() {
        val d = Driver()
        repeat(3) { d.tick(0.18f, profile, tuning) }
        val drifting = d.tick(0.30f, profile, tuning)
        assertTrue(drifting.message.contains("brighter"))
        d.engine.reset()
        val matched = d.tick(0.05f, profile, tuning)
        assertEquals("", matched.message)
    }

    private fun Driver.tickResult(
        noticeability: Float,
        profile: ReferenceProfile,
        tuning: PerceptualTuning,
    ): ReferenceMatchResult {
        evaluator.next = PerceptualObservation(
            available = true, rawDifference = noticeability * 100f,
            perceptualDifference = noticeability, noticeability = noticeability,
            confidence = 1f, directionConfidence = 1f, deadZoneApplied = 10f,
            direction = ReferenceDriftDirection.BRIGHTER,
        )
        val raw = ReferenceMatchResult(
            active = true, referenceId = profile.id,
            exposureMatch = ReferenceSignalResult(enabled = true, available = true),
        )
        return engine.evaluate(raw, profile, CurrentReferenceInput(), null, tuning)
    }

    @Test
    fun `creative scene model raises importance and sets creativeAware, null leaves today's behavior`() {
        val creativeProfile = profile.copy(
            creativeScene = CreativeSceneModel(
                importance = mapOf(
                    CreativeAspect.LIGHTING to CreativePriority(1.0f, "high-key lighting"),
                ),
            ),
        )
        val plain = Driver()
        val creative = Driver()
        lateinit var plainResult: ReferenceMatchResult
        lateinit var creativeResult: ReferenceMatchResult
        // Drive both identically to a steady drift; only the creative multiplier differs.
        repeat(8) {
            plainResult = plain.tickResult(0.6f, profile, tuning)
            creativeResult = creative.tickResult(0.6f, creativeProfile, tuning)
        }
        val plainImportance = plainResult.signal(ReferenceSignal.EXPOSURE).perception!!.importance
        val creativeImportance = creativeResult.signal(ReferenceSignal.EXPOSURE).perception!!.importance

        assertFalse("no creative model → not creative-aware (today's behavior)", plainResult.creativeAware)
        assertTrue("creative model present → creative-aware", creativeResult.creativeAware)
        assertTrue(
            "high LIGHTING priority must raise EXPOSURE importance",
            creativeImportance > plainImportance,
        )
    }

    @Test
    fun `disabled signals reset perception state`() {
        val d = Driver()
        repeat(3) { d.tick(0.18f, profile, tuning) }
        d.tick(0.5f, profile, tuning) // entered
        // Disabled frame → state resets.
        val raw = ReferenceMatchResult(
            active = true, referenceId = profile.id,
            exposureMatch = ReferenceSignalResult(enabled = false, available = false),
        )
        d.engine.evaluate(raw, profile, CurrentReferenceInput(), null, tuning)
        val s = d.tick(0.15f, profile, tuning)
        assertTrue(s.matched)
    }
}
