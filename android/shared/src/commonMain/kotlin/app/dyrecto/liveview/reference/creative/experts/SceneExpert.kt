package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.CreativeCamera
import app.dyrecto.liveview.reference.creative.CreativeColor
import app.dyrecto.liveview.reference.creative.CreativeLighting
import app.dyrecto.liveview.reference.creative.CreativeSubject
import app.dyrecto.liveview.reference.creative.HeadroomLevel
import app.dyrecto.liveview.reference.creative.NegativeSpace
import app.dyrecto.liveview.reference.creative.SceneRelationship
import app.dyrecto.liveview.reference.creative.SignatureElement
import app.dyrecto.liveview.reference.creative.SubjectPlacement
import app.dyrecto.liveview.reference.creative.identity.ShotTrait
import app.dyrecto.liveview.reference.creative.semantic.ConceptScore
import app.dyrecto.liveview.reference.creative.semantic.SemanticObservation

/**
 * Phase 16.1 — a Scene Expert contributes part of the creative understanding. `CreativeSceneAnalyzer`
 * runs every expert over one shared [CreativeAnalysisContext] and merges their [CreativeContribution]s
 * into a single `CreativeSceneModel`. The reasoning pipeline downstream is unchanged.
 *
 * Experts are pure functions of the context (deterministic, JVM-testable). Deterministic experts
 * reproduce the pre-16.1 `deriveX` logic exactly, so a run with no semantic evidence yields the same
 * model — including the importance map — as Phase 16.
 */
interface SceneExpert {
    val name: String
    fun observe(ctx: CreativeAnalysisContext): CreativeContribution
}

/**
 * Everything an expert may read, computed once by the orchestrator. [semantic] is the MobileCLIP
 * observation (import-only); null ⇒ deterministic experts only.
 */
data class CreativeAnalysisContext(
    val exposure: ReferenceExposureProfile,
    val color: ReferenceColorProfile,
    val ai: ReferenceAiProfile?,
    val faceBox: NormalizedRect?,
    /** Primary subject box (AI primary else face), the geometry experts key off. */
    val subjectBox: NormalizedRect?,
    /** True when [subjectBox] is a face box (smaller than a full-subject box at equal framing). */
    val boxIsFace: Boolean,
    val semantic: SemanticObservation? = null,
)

/**
 * A partial, expert-authoritative contribution. Core fields are non-null only for the expert that
 * owns them; the rest are additive lists the orchestrator concatenates. Merging is last-writer-free:
 * each core field has exactly one authoritative expert.
 */
data class CreativeContribution(
    val subject: CreativeSubject? = null,
    val camera: CreativeCamera? = null,
    val placement: SubjectPlacement? = null,
    val negativeSpace: NegativeSpace? = null,
    val headroom: HeadroomLevel? = null,
    val compositionConfidence: Float? = null,
    val lighting: CreativeLighting? = null,
    val color: CreativeColor? = null,
    val relationships: List<SceneRelationship> = emptyList(),
    val signatureElements: List<SignatureElement> = emptyList(),
    /**
     * Phase 16.2 — continuous, distinctiveness-scored identity traits from THIS expert's measurements.
     * The orchestrator concatenates them into `CreativeSceneModel.identity`; additive, no core field.
     */
    val traits: List<ShotTrait> = emptyList(),
    /** Importance nudges — applied by the orchestrator ONLY when semantic evidence is present. */
    val priorityHints: List<PriorityHint> = emptyList(),
    /** Raw concepts (semantic expert only) — carried for diagnostics/signature, never for decisions. */
    val concepts: List<ConceptScore> = emptyList(),
) {
    companion object {
        val EMPTY = CreativeContribution()
    }
}

/**
 * A bounded, additive nudge to a creative aspect's baseline importance weight, produced by the
 * semantic expert. Applied only when semantic evidence exists, then clamped into [0,1] — so the
 * `CreativePriorityMapper` multiplier math is untouched and the no-model path stays byte-identical.
 */
data class PriorityHint(
    val aspect: CreativeAspect,
    val boost: Float,
    val reason: String,
)
