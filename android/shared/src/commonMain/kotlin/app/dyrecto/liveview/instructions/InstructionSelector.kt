package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ai.ComparisonStrategy

/**
 * Picks exactly ONE instruction for the current frame from the candidates
 * [InstructionTranslator.annotate] attached — the operator always receives one coherent
 * direction, even when several signals drift simultaneously.
 *
 * Deliberately NOT a reasoning engine: a per-strategy strict priority list (mirroring
 * `ReferenceSignalApplicability` — each list holds exactly that strategy's applicable signals)
 * plus one rule: confirmed drifts outrank unconfirmed ones, so the surfaced instruction always
 * agrees with the alert that fired. No scoring, no weighting, fully deterministic.
 *
 * Phase 16: when the winning reference is [ReferenceMatchResult.creativeAware], the selector honors
 * the ranking the reasoning layer already produced — it orders the candidate drifts by their
 * creative-aware `perception.importance` (the strategy priority list becomes the stable tie-break).
 * It computes no creative math of its own. When not creative-aware (legacy/no-model/existing tests),
 * it falls back to the fixed strategy priority list exactly as before.
 */
object InstructionSelector {

    /** Ordered from most to least urgent. Subject presence leads wherever it applies — nothing
     *  else is actionable while the subject is gone. */
    private val HUMAN = listOf(
        ReferenceSignal.SUBJECT_PRESENCE,
        ReferenceSignal.FACE_PRESENCE,
        ReferenceSignal.EYE_VISIBILITY,
        ReferenceSignal.HEADROOM,
        ReferenceSignal.SUBJECT_POSITION,
        ReferenceSignal.SUBJECT_SIZE,
        ReferenceSignal.EXPOSURE,
        ReferenceSignal.WHITE_BALANCE,
        ReferenceSignal.COMPOSITION,
        ReferenceSignal.VISUAL_SIMILARITY,
    )

    private val SINGLE_SUBJECT = listOf(
        ReferenceSignal.SUBJECT_PRESENCE,
        ReferenceSignal.SUBJECT_POSITION,
        ReferenceSignal.SUBJECT_SIZE,
        ReferenceSignal.EXPOSURE,
        ReferenceSignal.WHITE_BALANCE,
        ReferenceSignal.COMPOSITION,
        ReferenceSignal.VISUAL_SIMILARITY,
    )

    private val MULTI_OBJECT = listOf(
        ReferenceSignal.SUBJECT_PRESENCE,
        ReferenceSignal.COMPOSITION,
        ReferenceSignal.EXPOSURE,
        ReferenceSignal.WHITE_BALANCE,
        ReferenceSignal.VISUAL_SIMILARITY,
    )

    private val SCENE = listOf(
        ReferenceSignal.COMPOSITION,
        ReferenceSignal.EXPOSURE,
        ReferenceSignal.WHITE_BALANCE,
        ReferenceSignal.VISUAL_SIMILARITY,
    )

    fun priorityFor(strategy: ComparisonStrategy): List<ReferenceSignal> = when (strategy) {
        ComparisonStrategy.HUMAN_STRATEGY -> HUMAN

        ComparisonStrategy.ANIMAL_STRATEGY,
        ComparisonStrategy.VEHICLE_STRATEGY,
        ComparisonStrategy.PRODUCT_STRATEGY,
        -> SINGLE_SUBJECT

        ComparisonStrategy.MULTI_OBJECT_STRATEGY -> MULTI_OBJECT

        ComparisonStrategy.ARCHITECTURE_STRATEGY,
        ComparisonStrategy.LANDSCAPE_STRATEGY,
        ComparisonStrategy.GENERIC_SCENE_STRATEGY,
        -> SCENE
    }

    /** The one instruction to show this frame, or null while everything matches. */
    fun select(match: ReferenceMatchResult): AssistantInstruction? {
        if (!match.active) return null
        val priority = priorityFor(match.strategy)
        return pick(match, priority, confirmedOnly = true)
            ?: pick(match, priority, confirmedOnly = false)
    }

    private fun pick(
        match: ReferenceMatchResult,
        priority: List<ReferenceSignal>,
        confirmedOnly: Boolean,
    ): AssistantInstruction? {
        // Candidates preserve the fixed priority order (filter is order-preserving).
        val candidates = priority.filter { signal ->
            val s = match.signal(signal)
            (!confirmedOnly || s.confirmedDrift) && s.instruction != null
        }
        val chosen = if (match.creativeAware) {
            // Honor reasoning's importance; ties fall back to the fixed priority order (stable sort).
            candidates.sortedWith(
                compareByDescending<ReferenceSignal> { match.signal(it).perception?.importance ?: 0f }
                    .thenBy { priority.indexOf(it) },
            ).firstOrNull()
        } else {
            candidates.firstOrNull()
        }
        return chosen?.let { match.signal(it).instruction }
    }
}
