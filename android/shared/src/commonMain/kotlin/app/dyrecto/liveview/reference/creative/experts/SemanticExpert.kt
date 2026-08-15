package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.RelationshipKind
import app.dyrecto.liveview.reference.creative.SceneRelationship
import app.dyrecto.liveview.reference.creative.identity.ShotTrait
import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension
import app.dyrecto.liveview.reference.creative.identity.TraitDirection
import app.dyrecto.liveview.reference.creative.semantic.ConceptScore

/**
 * Phase 16.1 — the MobileCLIP semantic expert. Consumes the import-time [SemanticObservation] and
 * contributes evidence ONLY: pass-through concepts (diagnostics/signature), bounded importance hints,
 * and a few structural relationships. It is NOT a decision maker — it never emits alerts,
 * instructions, priorities, or matches directly; the orchestrator decides how (if at all) to fold
 * this evidence in, and importance hints are applied only when semantic evidence is present.
 *
 * Contributes NOTHING when `ctx.semantic == null` — so the no-model path is byte-identical to Phase 16.
 *
 * The concept→aspect coupling lives entirely in the bundled vocabulary DATA (`ConceptScore.aspect`),
 * not here: swapping the vocabulary changes behavior without touching this code. Thresholds are
 * first-pass estimates; hardware validation of concept cosines is pending.
 */
object SemanticExpert : SceneExpert {
    override val name = "semantic"

    /** Minimum cosine for a concept to count as "present" in the reference. */
    private const val PRESENT = 0.22f
    /** Above this, a concept reads as a defining feature of the shot. */
    private const val STRONG = 0.28f
    /** Max additive importance nudge a single aspect can receive. */
    private const val MAX_BOOST = 0.25f
    /** How many top concepts to carry through for diagnostics/signature. */
    private const val TOP_CONCEPTS = 6
    /** Top-concept margin over the median concept at which the semantic marker is fully distinctive. */
    private const val MARGIN_SATURATION = 0.12f
    /** Minimum distinctiveness for the semantic identity marker to be emitted. */
    private const val SEMANTIC_TRAIT_FLOOR = 0.20f

    // Aspects a strong semantic read implies a structural relationship for (direction-agnostic:
    // presence, not which way). Deterministic experts still own the concrete direction/geometry.
    private val RELATIONSHIP_FOR: Map<CreativeAspect, RelationshipKind> = mapOf(
        CreativeAspect.NEGATIVE_SPACE to RelationshipKind.NEGATIVE_SPACE_DOMINANT,
        CreativeAspect.DEPTH_OF_FIELD to RelationshipKind.SUBJECT_ISOLATION,
        CreativeAspect.SUBJECT to RelationshipKind.DOMINANT_ANCHOR,
        CreativeAspect.BACKGROUND to RelationshipKind.SUBJECT_TO_BACKGROUND,
    )

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution {
        val observation = ctx.semantic ?: return CreativeContribution.EMPTY
        val concepts = observation.concepts

        // Best concept per aspect (aspect-tagged concepts only).
        val bestByAspect: Map<CreativeAspect, ConceptScore> = concepts
            .filter { it.aspect != null && it.score >= PRESENT }
            .groupBy { it.aspect!! }
            .mapValues { (_, list) -> list.maxByOrNull { it.score }!! }

        val hints = bestByAspect.map { (aspect, concept) ->
            // A more-present aspect is a more-defining aspect → higher importance. Bounded.
            val boost = ((concept.score - PRESENT) / (1f - PRESENT) * MAX_BOOST).coerceIn(0f, MAX_BOOST)
            PriorityHint(
                aspect = aspect,
                boost = boost,
                reason = "semantic read: ${concept.label}",
            )
        }

        val relationships = bestByAspect.mapNotNull { (aspect, concept) ->
            if (concept.score < STRONG) return@mapNotNull null
            val kind = RELATIONSHIP_FOR[aspect] ?: return@mapNotNull null
            SceneRelationship(
                kind = kind,
                strength = concept.score.coerceIn(0f, 1f),
                confidence = concept.score.coerceIn(0f, 1f),
                reason = "semantic evidence: ${concept.label}",
            )
        }

        val top = concepts.asSequence().filter { it.score >= PRESENT }.take(TOP_CONCEPTS).toList()

        return CreativeContribution(
            relationships = relationships,
            priorityHints = hints,
            concepts = top,
            traits = listOfNotNull(semanticDistinction(concepts)),
        )
    }

    /**
     * The dominant semantic read as an identity marker: a concept that stands out unusually strongly
     * for THIS image (its cosine margin over the median concept) is part of what makes the shot
     * distinctive. Bounded and evidence-gated — no concept, no marker. It enriches the identity model
     * (Developer diagnostics + the identity-forward UI) but is deliberately kept OUT of the importance
     * math (SEMANTIC_DISTINCTION is not mapped to a CreativeAspect) — importance stays on the gated
     * `priorityHints` path so measured signals are never overridden by semantics.
     */
    private fun semanticDistinction(concepts: List<ConceptScore>): ShotTrait? {
        if (concepts.size < 2) return null
        val top = concepts.first() // already sorted descending by score
        if (top.score < PRESENT) return null
        val median = concepts[concepts.size / 2].score
        val margin = (top.score - median).coerceAtLeast(0f)
        val distinctiveness = (margin / MARGIN_SATURATION).coerceIn(0f, 1f)
        if (distinctiveness < SEMANTIC_TRAIT_FLOOR) return null
        return ShotTrait(
            dimension = ShotTraitDimension.SEMANTIC_DISTINCTION,
            value = top.score,
            distinctiveness = distinctiveness,
            descriptor = top.label,
            direction = TraitDirection.UNKNOWN,
            confidence = top.score.coerceIn(0f, 1f),
            reason = "distinctive semantic read: ${top.label}",
        )
    }
}
