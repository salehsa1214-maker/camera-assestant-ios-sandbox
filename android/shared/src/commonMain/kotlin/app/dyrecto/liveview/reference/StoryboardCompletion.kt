package app.dyrecto.liveview.reference

/**
 * Phase 15 (Storyboard) — the pure completion state machine for one storyboard shot.
 *
 * A shot completes only when the live match stays at or above [StoryboardCompletionRule.matchThreshold]
 * continuously for [StoryboardCompletionRule.holdSeconds] **while it is the Current Match**, and that
 * success repeats [StoryboardCompletionRule.confirmCount] independent times — each separated by the
 * match dropping below the threshold (or the shot losing Current Match). Once completed it is
 * permanent and never auto-cleared.
 *
 * This object is a pure function of its inputs (clock injected by the caller as [nowMs]) so it is
 * JVM-testable with zero Android/coroutine dependencies — the monitor owns all mutable state and
 * feeds one shot per call.
 */
object StoryboardCompletion {

    /**
     * Transient (never persisted) hold-streak bookkeeping for one shot. [holdStartMs] marks when
     * the current continuous above-threshold-as-Current-Match streak began (null = not accruing);
     * [streakConfirmed] is true once this streak has already produced its one confirmation, so
     * staying in-threshold longer does nothing until the shot leaves and re-enters.
     */
    data class Runtime(
        val holdStartMs: Long? = null,
        val streakConfirmed: Boolean = false,
    )

    /** Result of one [update] call: the (possibly advanced) persisted + runtime state + edge flag. */
    data class Update(
        val completion: ShotCompletion,
        val runtime: Runtime,
        /** True exactly on the frame this shot flips from pending to completed. */
        val justCompleted: Boolean,
    )

    /**
     * Advances one shot's completion.
     *
     * @param aboveThreshold the shot is the Current Match this frame AND its score ≥ the threshold.
     *   Non-winners and below-threshold winners must pass `false` — that resets the streak.
     */
    fun update(
        completion: ShotCompletion,
        runtime: Runtime,
        aboveThreshold: Boolean,
        nowMs: Long,
        rule: StoryboardCompletionRule,
    ): Update {
        // Completed is permanent — ignore further input and drop any streak.
        if (completion.completed) {
            return Update(completion, Runtime(), justCompleted = false)
        }

        // Not accruing (not the Current Match, or below threshold): the streak ends; the
        // confirmation count is preserved so the NEXT hold is an independent success.
        if (!aboveThreshold) {
            return Update(completion, Runtime(), justCompleted = false)
        }

        val startMs = runtime.holdStartMs ?: nowMs
        val heldLongEnough = nowMs - startMs >= rule.holdMs

        // Still building the hold, or this streak already counted its confirmation → keep streaking.
        if (runtime.streakConfirmed || !heldLongEnough) {
            return Update(
                completion = completion,
                runtime = Runtime(holdStartMs = startMs, streakConfirmed = runtime.streakConfirmed),
                justCompleted = false,
            )
        }

        // The streak just reached the required continuous hold → one independent confirmation.
        val confirmations = completion.confirmations + 1
        val completed = confirmations >= rule.confirmCount
        return Update(
            completion = completion.copy(confirmations = confirmations, completed = completed),
            runtime = Runtime(holdStartMs = startMs, streakConfirmed = true),
            justCompleted = completed,
        )
    }
}

/**
 * Phase 15: storyboard shooting progress — how many planned shots are done. Progress % is simply
 * completed / total (a shot is one unit regardless of how strict its rule is).
 */
data class StoryboardProgress(
    val completed: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total <= 0) 0f else (completed.toFloat() / total).coerceIn(0f, 1f)
    val allComplete: Boolean get() = total > 0 && completed >= total
}
