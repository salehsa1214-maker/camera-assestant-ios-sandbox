package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ai.ComparisonStrategy

/**
 * Default strategy → per-signal perceptual WEIGHTS (0 = irrelevant, 1 = primary carrier).
 *
 * This refines — never replaces — `ReferenceSignalApplicability`: that table still decides which
 * signals are evaluated at all; this one decides how much each evaluated signal matters to a
 * human under the strategy (a person's exposure matters more than the background's; a landscape
 * is judged frame-wide). The same numeric difference therefore produces different perceptual
 * importance per strategy.
 *
 * These are DEFAULTS: the live table used by the engine comes from [PerceptualTuning], so
 * hardware calibration can adjust weights at runtime without code changes.
 */
object PerceptualApplicability {

    val DEFAULT_WEIGHTS: Map<ComparisonStrategy, Map<ReferenceSignal, Float>> = buildMap {
        put(
            ComparisonStrategy.HUMAN_STRATEGY,
            mapOf(
                ReferenceSignal.EXPOSURE to 0.9f,
                ReferenceSignal.WHITE_BALANCE to 0.7f,
                ReferenceSignal.SUBJECT_POSITION to 1f,
                ReferenceSignal.SUBJECT_SIZE to 1f,
                ReferenceSignal.HEADROOM to 0.8f,
                ReferenceSignal.FACE_PRESENCE to 1f,
                ReferenceSignal.EYE_VISIBILITY to 0.7f,
                ReferenceSignal.SUBJECT_PRESENCE to 1f,
                ReferenceSignal.COMPOSITION to 0.6f,
                ReferenceSignal.VISUAL_SIMILARITY to 0.5f,
            ),
        )
        val singleSubject = mapOf(
            ReferenceSignal.EXPOSURE to 0.8f,
            ReferenceSignal.WHITE_BALANCE to 0.7f,
            ReferenceSignal.SUBJECT_POSITION to 1f,
            ReferenceSignal.SUBJECT_SIZE to 1f,
            ReferenceSignal.SUBJECT_PRESENCE to 1f,
            ReferenceSignal.COMPOSITION to 0.6f,
            ReferenceSignal.VISUAL_SIMILARITY to 0.5f,
        )
        put(ComparisonStrategy.ANIMAL_STRATEGY, singleSubject)
        put(ComparisonStrategy.VEHICLE_STRATEGY, singleSubject)
        put(ComparisonStrategy.PRODUCT_STRATEGY, singleSubject)
        put(
            ComparisonStrategy.MULTI_OBJECT_STRATEGY,
            mapOf(
                ReferenceSignal.EXPOSURE to 0.9f,
                ReferenceSignal.WHITE_BALANCE to 0.8f,
                ReferenceSignal.SUBJECT_PRESENCE to 1f,
                ReferenceSignal.COMPOSITION to 1f,
                ReferenceSignal.VISUAL_SIMILARITY to 0.7f,
            ),
        )
        val frameWide = mapOf(
            ReferenceSignal.EXPOSURE to 1f,
            ReferenceSignal.WHITE_BALANCE to 1f,
            ReferenceSignal.COMPOSITION to 1f,
            ReferenceSignal.VISUAL_SIMILARITY to 1f,
        )
        put(ComparisonStrategy.ARCHITECTURE_STRATEGY, frameWide)
        put(ComparisonStrategy.LANDSCAPE_STRATEGY, frameWide)
        put(ComparisonStrategy.GENERIC_SCENE_STRATEGY, frameWide)
    }

    /** Weight of [signal] under [strategy] from [weights] (0 when the table omits it). */
    fun strategyWeight(
        strategy: ComparisonStrategy,
        signal: ReferenceSignal,
        weights: Map<ComparisonStrategy, Map<ReferenceSignal, Float>> = DEFAULT_WEIGHTS,
    ): Float = weights[strategy]?.get(signal) ?: 0f
}
