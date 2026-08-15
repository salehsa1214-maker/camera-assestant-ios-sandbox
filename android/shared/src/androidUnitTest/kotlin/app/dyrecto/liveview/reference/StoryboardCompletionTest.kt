package app.dyrecto.liveview.reference

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 15: the pure storyboard completion state machine. Drives the clock explicitly so the
 * hold/confirm/leave-reenter/permanence rules are verified deterministically.
 */
class StoryboardCompletionTest {

    private val hold5s1x = StoryboardCompletionRule(holdSeconds = 5, confirmCount = 1, matchThreshold = 0.80f)

    /** Runs a sequence of (nowMs, aboveThreshold) samples through the engine, returning the last update. */
    private fun run(
        rule: StoryboardCompletionRule,
        samples: List<Pair<Long, Boolean>>,
    ): StoryboardCompletion.Update {
        var completion = ShotCompletion()
        var runtime = StoryboardCompletion.Runtime()
        var last = StoryboardCompletion.Update(completion, runtime, justCompleted = false)
        for ((now, above) in samples) {
            last = StoryboardCompletion.update(completion, runtime, above, now, rule)
            completion = last.completion
            runtime = last.runtime
        }
        return last
    }

    @Test
    fun `single continuous hold reaching duration completes with one confirmation`() {
        val result = run(hold5s1x, listOf(0L to true, 3_000L to true, 5_000L to true))
        assertTrue(result.completion.completed)
        assertEquals(1, result.completion.confirmations)
        assertTrue(result.justCompleted)
    }

    @Test
    fun `hold shorter than duration does not complete`() {
        val result = run(hold5s1x, listOf(0L to true, 4_999L to true))
        assertFalse(result.completion.completed)
        assertEquals(0, result.completion.confirmations)
    }

    @Test
    fun `leaving the threshold before the hold elapses resets the streak`() {
        // 4s in, drop out, then a fresh streak must run the full 5s from the re-entry point.
        val result = run(
            hold5s1x,
            listOf(0L to true, 4_000L to true, 4_500L to false, 5_000L to true, 8_000L to true),
        )
        assertFalse(result.completion.completed) // only 3s into the new streak (5_000..8_000)
        assertEquals(0, result.completion.confirmations)
    }

    @Test
    fun `staying in threshold past the hold yields only one confirmation, not many`() {
        val result = run(
            hold5s1x,
            listOf(0L to true, 5_000L to true, 9_000L to true, 20_000L to true),
        )
        assertEquals(1, result.completion.confirmations)
        assertTrue(result.completion.completed)
    }

    @Test
    fun `confirmCount of two requires two independent holds separated by leaving`() {
        val rule = StoryboardCompletionRule(holdSeconds = 5, confirmCount = 2, matchThreshold = 0.80f)
        // First hold completes → 1 confirmation, still pending.
        val afterFirst = run(rule, listOf(0L to true, 5_000L to true))
        assertEquals(1, afterFirst.completion.confirmations)
        assertFalse(afterFirst.completion.completed)

        // Continue: leave, re-enter, second full hold → 2 confirmations → completed.
        var completion = afterFirst.completion
        var runtime = afterFirst.runtime
        for ((now, above) in listOf(6_000L to false, 7_000L to true, 12_000L to true)) {
            val u = StoryboardCompletion.update(completion, runtime, above, now, rule)
            completion = u.completion
            runtime = u.runtime
        }
        assertEquals(2, completion.confirmations)
        assertTrue(completion.completed)
    }

    @Test
    fun `two full holds without leaving in between still only count once`() {
        val rule = StoryboardCompletionRule(holdSeconds = 5, confirmCount = 2, matchThreshold = 0.80f)
        // Never drops below threshold → streakConfirmed guards against a second confirmation.
        val result = run(rule, listOf(0L to true, 5_000L to true, 10_000L to true, 15_000L to true))
        assertEquals(1, result.completion.confirmations)
        assertFalse(result.completion.completed)
    }

    @Test
    fun `completion is permanent even after dropping below threshold`() {
        val result = run(
            hold5s1x,
            listOf(0L to true, 5_000L to true, 6_000L to false, 7_000L to false),
        )
        assertTrue(result.completion.completed)
        assertEquals(1, result.completion.confirmations)
    }

    @Test
    fun `progress fraction and allComplete`() {
        assertEquals(0f, StoryboardProgress(0, 0).fraction, 0f)
        assertFalse(StoryboardProgress(0, 0).allComplete)
        assertEquals(0.5f, StoryboardProgress(2, 4).fraction, 1e-6f)
        assertFalse(StoryboardProgress(2, 4).allComplete)
        assertTrue(StoryboardProgress(4, 4).allComplete)
    }
}
