package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.creative.NegativeSpace

/**
 * Phase 16.1 — negative space + composition confidence. The pre-16.1 `deriveNegativeSpace` and the
 * composition-confidence `when` moved verbatim; output is byte-identical to Phase 16. Prefers the
 * segmentation coverage signal, falling back to subject-box area.
 */
object CompositionExpert : SceneExpert {
    override val name = "composition"

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution {
        val negativeSpace = deriveNegativeSpace(ctx.ai, ctx.subjectBox)
        val confidence = when {
            ctx.subjectBox != null -> 0.6f
            ctx.ai?.segmentationCoverage != null -> 0.45f
            else -> 0.3f
        }
        return CreativeContribution(
            negativeSpace = negativeSpace,
            compositionConfidence = confidence,
        )
    }

    private fun deriveNegativeSpace(ai: ReferenceAiProfile?, subjectBox: NormalizedRect?): NegativeSpace {
        ai?.segmentationCoverage?.let { coverage ->
            return when {
                coverage < 0.25f -> NegativeSpace.HIGH
                coverage > 0.60f -> NegativeSpace.LOW
                else -> NegativeSpace.MEDIUM
            }
        }
        val area = subjectBox?.area ?: return NegativeSpace.UNKNOWN
        return when {
            area < 0.15f -> NegativeSpace.HIGH
            area > 0.50f -> NegativeSpace.LOW
            else -> NegativeSpace.MEDIUM
        }
    }
}
