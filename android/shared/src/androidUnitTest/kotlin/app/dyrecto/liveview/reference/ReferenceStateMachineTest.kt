package app.dyrecto.liveview.reference

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for [ReferenceStateMachine] — frame-counted persistence/recovery per signal.
 * MEDIUM thresholds: 5 persistence frames, 5 recovery frames.
 */
class ReferenceStateMachineTest {

    private val thresholds = ReferenceConfig.MEDIUM

    private fun raw(
        matched: Boolean,
        enabled: Boolean = true,
        available: Boolean = true,
    ) = ReferenceMatchResult(
        active = true,
        referenceId = "ref-1",
        exposureMatch = ReferenceSignalResult(
            enabled = enabled,
            available = available,
            matched = matched,
            score = if (matched) 1f else 0.2f,
            delta = if (matched) 0f else 20f,
            direction = if (matched) ReferenceDriftDirection.NONE else ReferenceDriftDirection.BRIGHTER,
            message = if (matched) "" else "Exposure is brighter than the reference.",
        ),
    )

    private fun ReferenceStateMachine.tick(
        matched: Boolean,
        enabled: Boolean = true,
        available: Boolean = true,
    ): ReferenceSignalResult = update(raw(matched, enabled, available), thresholds).exposureMatch

    @Test
    fun `single-frame drift is ignored`() {
        val sm = ReferenceStateMachine()
        val s = sm.tick(matched = false)
        assertEquals(ReferenceSignalState.DRIFTING, s.state)
        assertFalse(s.confirmedDrift)
        assertEquals(1, s.persistenceFrames)

        val back = sm.tick(matched = true)
        assertEquals(ReferenceSignalState.NORMAL, back.state)
        assertEquals(0, back.persistenceFrames)
    }

    @Test
    fun `persistent drift confirms exactly at the persistence threshold`() {
        val sm = ReferenceStateMachine()
        repeat(4) {
            val s = sm.tick(matched = false)
            assertEquals(ReferenceSignalState.DRIFTING, s.state)
            assertFalse(s.confirmedDrift)
        }
        val s = sm.tick(matched = false) // 5th consecutive frame
        assertEquals(ReferenceSignalState.CONFIRMED_DRIFT, s.state)
        assertTrue(s.confirmedDrift)
    }

    @Test
    fun `flickering drift never confirms`() {
        val sm = ReferenceStateMachine()
        repeat(10) {
            sm.tick(matched = false)
            sm.tick(matched = false)
            sm.tick(matched = false)
            val s = sm.tick(matched = true) // streak broken before frame 5
            assertEquals(ReferenceSignalState.NORMAL, s.state)
            assertFalse(s.confirmedDrift)
        }
    }

    @Test
    fun `recovery confirms after the recovery threshold and re-arms`() {
        val sm = ReferenceStateMachine()
        repeat(5) { sm.tick(matched = false) } // confirm

        repeat(4) {
            val s = sm.tick(matched = true)
            assertEquals(ReferenceSignalState.RECOVERING, s.state)
            assertTrue(s.confirmedDrift) // still confirmed while recovering
        }
        val recovered = sm.tick(matched = true) // 5th clean frame
        assertEquals(ReferenceSignalState.NORMAL, recovered.state)
        assertFalse(recovered.confirmedDrift)

        // Re-arm: a fresh persistent drift confirms again.
        repeat(4) { sm.tick(matched = false) }
        val again = sm.tick(matched = false)
        assertTrue(again.confirmedDrift)
    }

    @Test
    fun `recovery streak resets when drift returns mid-recovery`() {
        val sm = ReferenceStateMachine()
        repeat(5) { sm.tick(matched = false) } // confirm
        repeat(4) { sm.tick(matched = true) }  // almost recovered
        val relapse = sm.tick(matched = false)
        assertEquals(ReferenceSignalState.CONFIRMED_DRIFT, relapse.state)
        assertEquals(0, relapse.recoveryFrames)
        repeat(4) {
            assertTrue(sm.tick(matched = true).confirmedDrift) // needs a full new streak
        }
        assertFalse(sm.tick(matched = true).confirmedDrift)
    }

    @Test
    fun `unavailable frames hold state without advancing counters`() {
        val sm = ReferenceStateMachine()
        repeat(5) { sm.tick(matched = false) } // confirm

        repeat(10) {
            val s = sm.tick(matched = true, available = false)
            assertEquals(ReferenceSignalState.CONFIRMED_DRIFT, s.state)
            assertTrue(s.confirmedDrift)
            assertEquals(0, s.recoveryFrames) // matched=true carried no evidence: unavailable
        }
    }

    @Test
    fun `disabling a signal resets it to NORMAL`() {
        val sm = ReferenceStateMachine()
        repeat(5) { sm.tick(matched = false) } // confirm
        val s = sm.tick(matched = false, enabled = false)
        assertEquals(ReferenceSignalState.NORMAL, s.state)
        assertFalse(s.confirmedDrift)
    }

    @Test
    fun `reset clears a confirmed drift`() {
        val sm = ReferenceStateMachine()
        repeat(5) { sm.tick(matched = false) }
        sm.reset()
        val s = sm.tick(matched = false)
        assertEquals(ReferenceSignalState.DRIFTING, s.state)
        assertEquals(1, s.persistenceFrames)
        assertFalse(s.confirmedDrift)
    }
}
