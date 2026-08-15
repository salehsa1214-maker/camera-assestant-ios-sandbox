package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.perception.PerceptualSignal
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalResult

/**
 * Phase 13 — the Assistant Instruction Layer's translator. Pure and stateless: it consumes only
 * the perceptual verdict already attached to each signal (never pixels, never raw comparisons)
 * and turns "what is wrong" into "what the operator should do".
 *
 * It sets ONLY [ReferenceSignalResult.instruction] — the perceptual [ReferenceSignalResult.message]
 * is never rewritten; perception and presentation stay separate concepts. Alerting, debounce,
 * hysteresis, and severity are untouched upstream/downstream.
 */
object InstructionTranslator {

    /**
     * Below this direction confidence the assistant refuses to guess a corrective direction and
     * falls back to a generic REFRAME. Presentation-only constant (not a perception threshold).
     */
    const val LOW_DIRECTION_CONFIDENCE = 0.4f

    /**
     * Translates one signal's perceptual verdict into an instruction candidate. Null when the
     * signal is not perceptually drifting or carries no perceptual verdict.
     */
    fun translate(signal: ReferenceSignal, result: ReferenceSignalResult): AssistantInstruction? {
        val perception = result.perception ?: return null
        if (!result.enabled || !result.available || result.matched) return null
        if (!perception.perceptuallyDrifting) return null

        val action = actionFor(signal, perception)
        if (action == AssistantAction.NONE) return null

        return AssistantInstruction(
            action = action,
            message = InstructionFormatter.message(action, perception.severity),
            confidence = perception.confidence,
            progress = InstructionProgress.of(result),
            reason = result.message,
        )
    }

    /** Copy of [match] with every drifting signal's [ReferenceSignalResult.instruction] filled. */
    fun annotate(match: ReferenceMatchResult): ReferenceMatchResult {
        var result = match
        for (signal in ReferenceSignal.entries) {
            val s = match.signal(signal)
            val instruction = translate(signal, s) ?: continue
            result = result.withSignal(signal, s.copy(instruction = instruction))
        }
        return result
    }

    private fun actionFor(signal: ReferenceSignal, p: PerceptualSignal): AssistantAction {
        val direct = directionalAction(p.direction)
        return when {
            direct == null -> genericAction(signal, p.direction)
            // Directional guidance requires trusting the direction — never invent one.
            p.directionConfidence < LOW_DIRECTION_CONFIDENCE -> AssistantAction.REFRAME
            else -> direct
        }
    }

    /** Direction-dependent corrective actions; null when the drift has no expressible axis. */
    private fun directionalAction(direction: ReferenceDriftDirection): AssistantAction? = when (direction) {
        ReferenceDriftDirection.BRIGHTER -> AssistantAction.REDUCE_EXPOSURE
        ReferenceDriftDirection.DARKER -> AssistantAction.INCREASE_EXPOSURE
        ReferenceDriftDirection.WARMER -> AssistantAction.COOL_WHITE_BALANCE
        ReferenceDriftDirection.COOLER -> AssistantAction.WARM_WHITE_BALANCE
        ReferenceDriftDirection.LEFT -> AssistantAction.PAN_LEFT
        ReferenceDriftDirection.RIGHT -> AssistantAction.PAN_RIGHT
        // HEADROOM reuses UP/DOWN with inverted meaning ("less headroom" reports UP): the
        // corrective tilt is the same either way, so no special case is needed.
        ReferenceDriftDirection.UP -> AssistantAction.TILT_UP
        ReferenceDriftDirection.DOWN -> AssistantAction.TILT_DOWN
        ReferenceDriftDirection.SMALLER -> AssistantAction.MOVE_CLOSER
        ReferenceDriftDirection.LARGER -> AssistantAction.MOVE_BACK
        else -> null
    }

    /** Non-directional drifts: presence, composition, similarity, unexpressible tint. */
    private fun genericAction(
        signal: ReferenceSignal,
        direction: ReferenceDriftDirection,
    ): AssistantAction = when {
        direction == ReferenceDriftDirection.MISSING -> AssistantAction.WAIT_FOR_SUBJECT
        signal == ReferenceSignal.COMPOSITION -> AssistantAction.REFRAME
        signal == ReferenceSignal.VISUAL_SIMILARITY -> AssistantAction.MATCH_REFERENCE
        // WHITE_BALANCE tint-only drift reports UNKNOWN — no cool/warm axis to recommend.
        signal == ReferenceSignal.WHITE_BALANCE -> AssistantAction.MATCH_REFERENCE
        else -> AssistantAction.REFRAME
    }
}
