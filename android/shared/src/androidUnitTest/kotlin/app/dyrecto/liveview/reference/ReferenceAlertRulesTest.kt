package app.dyrecto.liveview.reference

import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for [ReferenceAlertRules] — single-fire drift alerts on confirmed edges, one
 * (optionally grouped) recovery alert, no spam. Threads [ReferenceAlertState] through successive
 * evaluations like the coordinator does.
 */
class ReferenceAlertRulesTest {

    private val idGen = AlertIdGenerator()

    /** Threads state through evaluations, mirroring DefaultMonitoringSession. */
    private inner class Driver {
        var state = ReferenceAlertState()

        fun evaluate(match: ReferenceMatchResult): List<app.dyrecto.domain.alerts.Alert> {
            val result = ReferenceAlertRules.evaluate(state, match, idGen)
            state = result.state
            return result.alerts
        }
    }

    private fun signal(
        state: ReferenceSignalState,
        confirmed: Boolean = state == ReferenceSignalState.CONFIRMED_DRIFT ||
            state == ReferenceSignalState.RECOVERING,
        message: String = "drifted",
    ) = ReferenceSignalResult(
        enabled = true,
        available = true,
        matched = state == ReferenceSignalState.NORMAL,
        score = if (state == ReferenceSignalState.NORMAL) 1f else 0.2f,
        message = if (state == ReferenceSignalState.NORMAL) "" else message,
        state = state,
        confirmedDrift = confirmed,
    )

    private fun match(
        exposure: ReferenceSignalState = ReferenceSignalState.NORMAL,
        position: ReferenceSignalState = ReferenceSignalState.NORMAL,
        face: ReferenceSignalState = ReferenceSignalState.NORMAL,
        subjectPresence: ReferenceSignalState = ReferenceSignalState.NORMAL,
        composition: ReferenceSignalState = ReferenceSignalState.NORMAL,
        similarity: ReferenceSignalState = ReferenceSignalState.NORMAL,
        active: Boolean = true,
        at: Long = 1000L,
    ) = ReferenceMatchResult(
        active = active,
        referenceId = "ref-1",
        exposureMatch = signal(exposure, message = "Exposure is brighter than the reference."),
        subjectPositionMatch = signal(position, message = "Subject moved left from the reference."),
        facePresenceMatch = signal(face, message = "Face is not visible like the reference."),
        subjectPresenceMatch = signal(subjectPresence, message = "Reference subject is missing."),
        compositionMatch = signal(composition, message = "Composition differs from the reference."),
        visualSimilarityMatch = signal(similarity, message = "The scene looks different from the reference."),
        updatedAtMs = at,
    )

    @Test
    fun `alert message prefers the assistant instruction when one exists`() {
        val driver = Driver()
        driver.evaluate(match()) // seed

        val instructed = match(exposure = ReferenceSignalState.CONFIRMED_DRIFT).let { m ->
            m.copy(
                exposureMatch = m.exposureMatch.copy(
                    instruction = app.dyrecto.liveview.instructions.AssistantInstruction(
                        action = app.dyrecto.liveview.instructions.AssistantAction.REDUCE_EXPOSURE,
                        message = "Reduce exposure noticeably.",
                        confidence = 0.9f,
                        progress = 0.4f,
                        reason = "Exposure is brighter than the reference.",
                    ),
                ),
            )
        }
        val alerts = driver.evaluate(instructed)
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_EXPOSURE_DRIFT, alerts[0].type)
        assertEquals("Reduce exposure noticeably.", alerts[0].message)
    }

    @Test
    fun `alert fires once on confirmed drift and never repeats while active`() {
        val driver = Driver()
        driver.evaluate(match()) // seed

        val alerts = driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_EXPOSURE_DRIFT, alerts[0].type)
        assertEquals("Exposure is brighter than the reference.", alerts[0].message)

        // Still confirmed for many ticks: no duplicates.
        repeat(20) {
            assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT)).isEmpty())
        }
    }

    @Test
    fun `recovery fires once with a specific message`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT)) // fire

        // Recovering (still confirmed) fires nothing.
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.RECOVERING)).isEmpty())

        val alerts = driver.evaluate(match(exposure = ReferenceSignalState.NORMAL))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_RECOVERED, alerts[0].type)
        assertEquals("Reference exposure restored.", alerts[0].message)

        // No second recovery.
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.NORMAL)).isEmpty())
    }

    @Test
    fun `recovery does not fire if drift never confirmed`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.DRIFTING)).isEmpty())
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.NORMAL)).isEmpty())
    }

    @Test
    fun `multiple signals recovering together fire one grouped recovery alert`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val fired = driver.evaluate(
            match(
                exposure = ReferenceSignalState.CONFIRMED_DRIFT,
                position = ReferenceSignalState.CONFIRMED_DRIFT,
            ),
        )
        assertEquals(2, fired.size) // one per signal, distinct types
        assertEquals(
            setOf(AlertType.REFERENCE_EXPOSURE_DRIFT, AlertType.REFERENCE_SUBJECT_POSITION_DRIFT),
            fired.map { it.type }.toSet(),
        )

        val recovered = driver.evaluate(match())
        assertEquals(1, recovered.size)
        assertEquals(AlertType.REFERENCE_RECOVERED, recovered[0].type)
        assertEquals("Reference match restored.", recovered[0].message)
    }

    @Test
    fun `seeding never fires even against an already-confirmed drift`() {
        val driver = Driver()
        val alerts = driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT))
        assertTrue(alerts.isEmpty())
        // And it stays quiet while the same drift persists (latch was seeded as fired).
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT)).isEmpty())
        // But the recovery still fires when it clears.
        val recovered = driver.evaluate(match())
        assertEquals(1, recovered.size)
        assertEquals(AlertType.REFERENCE_RECOVERED, recovered[0].type)
    }

    @Test
    fun `inactive match clears latches silently`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT)) // fire

        // User stops monitoring mid-drift: nothing fires, latches clear.
        assertTrue(driver.evaluate(ReferenceMatchResult(active = false)).isEmpty())

        // Restart: first active tick re-seeds (still silent even if drift is confirmed again).
        assertTrue(driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT)).isEmpty())
    }

    @Test
    fun `face missing maps to REFERENCE_FACE_MISSING`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val alerts = driver.evaluate(match(face = ReferenceSignalState.CONFIRMED_DRIFT))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_FACE_MISSING, alerts[0].type)
        assertEquals("Face is not visible like the reference.", alerts[0].message)
    }

    // ---- Phase 10: AI perception signals through the same latch lifecycle ----

    @Test
    fun `subject missing maps to REFERENCE_SUBJECT_MISSING and fires once`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val alerts = driver.evaluate(match(subjectPresence = ReferenceSignalState.CONFIRMED_DRIFT))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_SUBJECT_MISSING, alerts[0].type)
        assertEquals("Reference subject is missing.", alerts[0].message)
        repeat(10) {
            assertTrue(driver.evaluate(match(subjectPresence = ReferenceSignalState.CONFIRMED_DRIFT)).isEmpty())
        }
        val recovered = driver.evaluate(match())
        assertEquals(1, recovered.size)
        assertEquals(AlertType.REFERENCE_RECOVERED, recovered[0].type)
        assertEquals("Reference subject is back.", recovered[0].message)
    }

    @Test
    fun `composition drift maps to REFERENCE_COMPOSITION_DRIFT`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val alerts = driver.evaluate(match(composition = ReferenceSignalState.CONFIRMED_DRIFT))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_COMPOSITION_DRIFT, alerts[0].type)
    }

    @Test
    fun `visual mismatch maps to REFERENCE_VISUAL_MISMATCH`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val alerts = driver.evaluate(match(similarity = ReferenceSignalState.CONFIRMED_DRIFT))
        assertEquals(1, alerts.size)
        assertEquals(AlertType.REFERENCE_VISUAL_MISMATCH, alerts[0].type)
        assertEquals("The scene looks different from the reference.", alerts[0].message)
    }

    @Test
    fun `mixed old and new signals recovering together group into one alert`() {
        val driver = Driver()
        driver.evaluate(match()) // seed
        val fired = driver.evaluate(
            match(
                exposure = ReferenceSignalState.CONFIRMED_DRIFT,
                similarity = ReferenceSignalState.CONFIRMED_DRIFT,
                subjectPresence = ReferenceSignalState.CONFIRMED_DRIFT,
            ),
        )
        assertEquals(3, fired.size)
        assertEquals(
            setOf(
                AlertType.REFERENCE_EXPOSURE_DRIFT,
                AlertType.REFERENCE_VISUAL_MISMATCH,
                AlertType.REFERENCE_SUBJECT_MISSING,
            ),
            fired.map { it.type }.toSet(),
        )

        val recovered = driver.evaluate(match())
        assertEquals(1, recovered.size)
        assertEquals(AlertType.REFERENCE_RECOVERED, recovered[0].type)
        assertEquals("Reference match restored.", recovered[0].message)
    }

    @Test
    fun `alert ids are monotonic across mixed alerts`() {
        val driver = Driver()
        driver.evaluate(match())
        val first = driver.evaluate(match(exposure = ReferenceSignalState.CONFIRMED_DRIFT))
        val second = driver.evaluate(
            match(
                exposure = ReferenceSignalState.CONFIRMED_DRIFT,
                position = ReferenceSignalState.CONFIRMED_DRIFT,
            ),
        )
        assertTrue(second[0].id > first[0].id)
    }
}
