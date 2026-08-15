package app.dyrecto.liveview.reference

import app.dyrecto.liveview.reference.ai.ComparisonStrategy

/**
 * THE single applicability table: which comparison signals make sense under each strategy.
 * Consumed by both the comparator (non-applicable signals report disabled) and the Shot
 * Reference UI (non-applicable toggles are hidden) — never duplicated.
 *
 * Notes:
 *  - Human-only extras (headroom, face, eyes) exist only under [ComparisonStrategy.HUMAN_STRATEGY].
 *  - MULTI_OBJECT has no single position/size; SUBJECT_PRESENCE covers "an important object
 *    went missing" instead.
 *  - ARCHITECTURE/LANDSCAPE/GENERIC lean on composition (when the reference had objects) and
 *    visual similarity — the universal fallback available under every strategy.
 */
object ReferenceSignalApplicability {

    private val GLOBAL = setOf(
        ReferenceSignal.EXPOSURE,
        ReferenceSignal.WHITE_BALANCE,
        ReferenceSignal.VISUAL_SIMILARITY,
    )

    private val SINGLE_SUBJECT = GLOBAL + setOf(
        ReferenceSignal.SUBJECT_POSITION,
        ReferenceSignal.SUBJECT_SIZE,
        ReferenceSignal.SUBJECT_PRESENCE,
        ReferenceSignal.COMPOSITION,
    )

    fun signalsFor(strategy: ComparisonStrategy): Set<ReferenceSignal> = when (strategy) {
        ComparisonStrategy.HUMAN_STRATEGY -> SINGLE_SUBJECT + setOf(
            ReferenceSignal.HEADROOM,
            ReferenceSignal.FACE_PRESENCE,
            ReferenceSignal.EYE_VISIBILITY,
        )

        ComparisonStrategy.ANIMAL_STRATEGY,
        ComparisonStrategy.VEHICLE_STRATEGY,
        ComparisonStrategy.PRODUCT_STRATEGY,
        -> SINGLE_SUBJECT

        ComparisonStrategy.MULTI_OBJECT_STRATEGY -> GLOBAL + setOf(
            ReferenceSignal.SUBJECT_PRESENCE,
            ReferenceSignal.COMPOSITION,
        )

        ComparisonStrategy.ARCHITECTURE_STRATEGY,
        ComparisonStrategy.LANDSCAPE_STRATEGY,
        ComparisonStrategy.GENERIC_SCENE_STRATEGY,
        -> GLOBAL + setOf(ReferenceSignal.COMPOSITION)
    }

    fun applies(signal: ReferenceSignal, strategy: ComparisonStrategy): Boolean =
        signal in signalsFor(strategy)
}
