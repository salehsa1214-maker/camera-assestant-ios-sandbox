package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure-JVM tests for [AdaptiveSampler]: budget-driven stride with hysteresis. */
class AdaptiveSamplerTest {

    private val overBudget = 6_000_000L  // 6 ms > 5 ms high budget
    private val underBudget = 1_000_000L // 1 ms < 3 ms low budget
    private val deadBand = 4_000_000L    // between low and high budget

    @Test fun defaultsToFullResolution() {
        assertEquals(1, AdaptiveSampler().stride)
    }

    @Test fun sustainedOverBudgetRaisesStrideAfterStreak() {
        val s = AdaptiveSampler()
        s.record(overBudget) // streak 1
        s.record(overBudget) // streak 2
        assertEquals("no change before streak completes", 1, s.stride)
        s.record(overBudget) // streak 3 -> raise
        assertEquals(2, s.stride)
    }

    @Test fun strideRisesToCapThenHolds() {
        val s = AdaptiveSampler()
        repeat(100) { s.record(overBudget) }
        assertEquals(AdaptiveSampler.MAX_STRIDE, s.stride)
    }

    @Test fun sustainedHeadroomLowersStrideBackToFullRes() {
        val s = AdaptiveSampler()
        repeat(3) { s.record(overBudget) } // -> stride 2
        assertEquals(2, s.stride)
        // UNDER_STREAK consecutive under-budget frames lowers by one.
        repeat(AdaptiveSampler.UNDER_STREAK) { s.record(underBudget) }
        assertEquals(1, s.stride)
    }

    @Test fun deadBandHoldsStrideAndResetsStreaks() {
        val s = AdaptiveSampler()
        s.record(overBudget)
        s.record(overBudget) // over-streak 2 (not yet raised)
        s.record(deadBand)   // resets streaks, holds
        s.record(overBudget) // streak restarts at 1
        assertEquals("dead-band reset the streak, so no raise yet", 1, s.stride)
    }

    @Test fun hysteresisPreventsSingleFrameOscillation() {
        val s = AdaptiveSampler()
        repeat(3) { s.record(overBudget) } // stride 2
        s.record(underBudget)              // one good frame must NOT immediately drop stride
        assertEquals(2, s.stride)
    }

    @Test fun resetReturnsToFullRes() {
        val s = AdaptiveSampler()
        repeat(20) { s.record(overBudget) }
        s.reset()
        assertEquals(1, s.stride)
    }
}
