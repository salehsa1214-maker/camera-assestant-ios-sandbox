package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.CreativeSceneModel

/**
 * Phase 16 — the ONE coupling point between the matching-independent [CreativeSceneModel] and the
 * concrete `ReferenceSignal`s the reasoning pipeline evaluates today.
 *
 * The Creative Scene Model describes creative intent in stable photographer terms ([CreativeAspect]);
 * this mapper translates those aspect priorities into a per-signal *importance multiplier* centered
 * on 1.0. The perception layer folds the multiplier into `PerceptualDifference.importanceOf`, so a
 * shot whose creative identity depends on, say, negative space makes composition/placement drift
 * matter more than white balance — without altering any measurement, score, or threshold.
 *
 * Because this is the only place the two vocabularies meet, introducing a new `ReferenceSignal` or
 * reworking the pipeline touches only this file — the persisted creative model never changes.
 */
object CreativePriorityMapper {

    /** Which creative aspects inform each signal's importance. Empty/missing ⇒ neutral (×1.0). */
    private val CONTRIBUTORS: Map<ReferenceSignal, List<CreativeAspect>> = mapOf(
        ReferenceSignal.EXPOSURE to listOf(CreativeAspect.LIGHTING),
        ReferenceSignal.WHITE_BALANCE to listOf(CreativeAspect.COLOR),
        ReferenceSignal.SUBJECT_POSITION to listOf(
            CreativeAspect.SUBJECT_PLACEMENT,
            CreativeAspect.NEGATIVE_SPACE,
        ),
        ReferenceSignal.SUBJECT_SIZE to listOf(CreativeAspect.SUBJECT_SCALE),
        ReferenceSignal.HEADROOM to listOf(CreativeAspect.HEADROOM),
        ReferenceSignal.FACE_PRESENCE to listOf(CreativeAspect.SUBJECT),
        ReferenceSignal.EYE_VISIBILITY to listOf(CreativeAspect.SUBJECT),
        ReferenceSignal.SUBJECT_PRESENCE to listOf(CreativeAspect.SUBJECT),
        ReferenceSignal.COMPOSITION to listOf(
            CreativeAspect.COMPOSITION,
            CreativeAspect.NEGATIVE_SPACE,
        ),
        ReferenceSignal.VISUAL_SIMILARITY to listOf(
            CreativeAspect.MOOD,
            CreativeAspect.BACKGROUND,
        ),
    )

    /**
     * How far a creative weight of 1.0 (vs the neutral 0.5) can swing the multiplier. 0.8 ⇒ range
     * roughly [0.6 .. 1.4] before clamping — enough to reorder co-drifting signals, gentle enough
     * to keep reasoning stable.
     */
    private const val SWING = 0.8f
    private const val MIN_MULTIPLIER = 0.5f
    private const val MAX_MULTIPLIER = 1.5f

    /**
     * The importance multiplier for [signal] under [model]'s creative priorities. Returns 1.0 (no
     * effect) when no contributing aspect is present, so a model that inferred nothing for a signal
     * leaves it exactly as today.
     */
    fun multiplierFor(signal: ReferenceSignal, model: CreativeSceneModel): Float {
        val aspects = CONTRIBUTORS[signal] ?: return 1f
        val weights = aspects.mapNotNull { model.importance[it]?.weight }
        if (weights.isEmpty()) return 1f
        val avg = weights.average().toFloat()
        return (1f + (avg - 0.5f) * SWING).coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER)
    }
}
