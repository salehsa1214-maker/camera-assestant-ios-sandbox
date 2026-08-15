package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.creative.RelationshipKind
import app.dyrecto.liveview.reference.creative.SceneRelationship

/**
 * Phase 16.1 — structural relationships from segmentation + subject geometry. **Purely additive**:
 * it contributes only relationships (never core fields, never importance hints), so with or without
 * it the model's existing fields and importance map are byte-identical to Phase 16. Relationships
 * are inferred only when the evidence supports them, each above a confidence floor.
 */
object SegmentationExpert : SceneExpert {
    override val name = "segmentation"

    // Coverage = fraction of the frame the subject occupies (0..1). Low ⇒ small subject / lots of
    // surround. These are conservative geometry proxies, honest about their confidence.
    private const val ISOLATION_COVERAGE = 0.30f
    private const val ISOLATION_AREA = 0.18f

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution {
        val relationships = mutableListOf<SceneRelationship>()
        val box = ctx.subjectBox
        val coverage = ctx.ai?.segmentationCoverage

        // Subject isolated against a large, empty surround.
        if (box != null) {
            val isolated = coverage?.let { it < ISOLATION_COVERAGE } ?: (box.area < ISOLATION_AREA)
            if (isolated) {
                val emptiness = coverage?.let { (1f - it) } ?: (1f - box.area)
                relationships += SceneRelationship(
                    kind = RelationshipKind.SUBJECT_ISOLATION,
                    strength = emptiness.coerceIn(0f, 1f),
                    confidence = if (coverage != null) 0.7f else 0.5f,
                    reason = if (coverage != null) "small subject against a large clean surround"
                    else "small subject area against the frame",
                )
            }
        }

        // Cleanly separable subject ⇒ a simple background (proxy: a pixel-accurate, small mask).
        if (coverage != null && coverage < 0.35f && ctx.ai?.segmentationPixelAccurate == true) {
            relationships += SceneRelationship(
                kind = RelationshipKind.BACKGROUND_SIMPLICITY,
                strength = (1f - coverage).coerceIn(0f, 1f),
                confidence = 0.6f,
                reason = "subject occupies a small, cleanly-separable region",
            )
        }

        return CreativeContribution(relationships = relationships)
    }
}
