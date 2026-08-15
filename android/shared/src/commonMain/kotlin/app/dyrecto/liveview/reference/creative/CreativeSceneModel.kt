package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.reference.creative.identity.ShotIdentity
import kotlinx.serialization.Serializable

/**
 * Phase 16 — Creative Scene Understanding.
 *
 * A structured, photographer-facing description of the **creative intent** behind a reference
 * image, derived once at import from the signals the app already produces (subject geometry, the
 * 3×3 composition grid, segmentation coverage, exposure/histogram, color warmth/tint). A storyboard
 * reference is almost never the final photograph — it represents the *vision* of the shot — so the
 * assistant reasons about that vision, not the literal pixels.
 *
 * Design invariants:
 * - **Matching-independent.** This model describes creative intent in stable photographer terms and
 *   deliberately does NOT reference `ReferenceSignal` or any matching-pipeline type. Translating the
 *   [importance] priorities into today's per-signal reasoning weights happens later, in the reasoning
 *   layer (`CreativePriorityMapper`). New signals or a reworked pipeline touch only that mapper —
 *   never this persisted model.
 * - **Never guessed.** Every descriptive field is enum-valued with an `UNKNOWN` member and carries a
 *   confidence; attributes that cannot be measured reliably today (lighting direction, lens
 *   character, leading lines, symmetry) are nullable/reserved and left `null`, ready to be populated
 *   by stronger on-device vision in the future without changing the rest of the pipeline.
 * - **Serializable & additive.** Persisted inside `ReferenceProfile` (kotlinx-serialization). Legacy
 *   profiles load with `creativeScene = null` and behave exactly as before.
 */
@Serializable
data class CreativeSceneModel(
    val subject: CreativeSubject = CreativeSubject(),
    val camera: CreativeCamera = CreativeCamera(),
    val composition: CreativeComposition = CreativeComposition(),
    val lighting: CreativeLighting = CreativeLighting(),
    val color: CreativeColor = CreativeColor(),
    val depth: CreativeDepth = CreativeDepth(),
    val style: CreativeStyle = CreativeStyle(),

    /**
     * Inferred creative priorities keyed by the matching-independent [CreativeAspect]: "if this
     * aspect changes, how much does it affect the creative identity of this shot?" Reasoning-facing
     * only — never shown in the normal UI.
     */
    val importance: Map<CreativeAspect, CreativePriority> = emptyMap(),

    /**
     * Phase 16.1 — structural relationships between the scene's elements (subject↔frame,
     * subject↔background, negative-space dominance, isolation, …). Inferred ONLY when the combined
     * expert evidence supports them; never invented. Empty on legacy models and when nothing is
     * supported. Additive — downstream reasoning does not read this list (it enriches the signature,
     * priorities, and the photographer-facing UI).
     */
    val relationships: List<SceneRelationship> = emptyList(),

    /**
     * Phase 16.1 — the compact "creative signature": the few characteristics that would have to stay
     * recognizable if another photographer recreated this shot. Deliberately small (≤3 by default,
     * ≤5 under very strong evidence). Null on legacy models / when nothing distinctive was found.
     */
    val signature: CreativeSignature? = null,

    /**
     * Phase 16.1 — raw semantic provenance from the MobileCLIP expert (dominant concepts + scores +
     * model id). Retained for Developer diagnostics ONLY; never surfaced in the normal Reference
     * Analysis card and never read by the reasoning pipeline. Null when the semantic expert did not
     * run (no model / load failure / legacy).
     */
    val semantics: SceneSemantics? = null,

    /**
     * Phase 16.2 — the quantitative *shot identity*: continuous, distinctiveness-scored traits that
     * capture what makes THIS shot different from visually similar shots (precise subject position/
     * scale, negative-space direction, subject↔background separation, tonal key/contrast, grid
     * balance, warmth magnitude, …). Sources the identity-first signature, the (bounded, ordering-only)
     * importance emphasis, and the identity-forward UI. Null on legacy models / when nothing stood out.
     */
    val identity: ShotIdentity? = null,

    val schemaVersion: Int = 3,
)

/**
 * A stable, photographer-domain axis of a shot's creative identity — the vocabulary the Creative
 * Scene Model reasons in, decoupled from whatever matching signals exist today. The reasoning layer
 * maps these onto concrete `ReferenceSignal`s.
 */
enum class CreativeAspect {
    SUBJECT,
    SUBJECT_PLACEMENT,
    SUBJECT_SCALE,
    HEADROOM,
    COMPOSITION,
    NEGATIVE_SPACE,
    LIGHTING,
    COLOR,
    DEPTH_OF_FIELD,
    BACKGROUND,
    MOOD,
}

/** How much a creative aspect matters for this reference, with a machine-readable rationale. */
@Serializable
data class CreativePriority(
    /** 0..1 inferred emphasis. */
    val weight: Float,
    /** Short, machine-readable explanation of why this weight was assigned (diagnostics/future reasoning). */
    val reason: String,
) {
    val level: PriorityLevel
        get() = when {
            weight >= 0.66f -> PriorityLevel.HIGH
            weight >= 0.33f -> PriorityLevel.MEDIUM
            else -> PriorityLevel.LOW
        }
}

enum class PriorityLevel { LOW, MEDIUM, HIGH }

// ---------------------------------------------------------------------------------------------
// Descriptive sub-models. Each field is enum-valued (with UNKNOWN) and carries a confidence so the
// UI can surface only what was reliably inferred.
// ---------------------------------------------------------------------------------------------

@Serializable
data class CreativeSubject(
    val kind: CreativeSubjectKind = CreativeSubjectKind.UNKNOWN,
    /** Number of distinct subjects detected (0 when unknown). */
    val count: Int = 0,
    val confidence: Float = 0f,
)

@Serializable
data class CreativeCamera(
    val shotType: ShotType = ShotType.UNKNOWN,
    val angle: CameraAngle = CameraAngle.UNKNOWN,
    val shotTypeConfidence: Float = 0f,
    /** Camera angle is a weak geometric proxy today — reported at deliberately low confidence. */
    val angleConfidence: Float = 0f,
)

@Serializable
data class CreativeComposition(
    val placement: SubjectPlacement = SubjectPlacement.UNKNOWN,
    val negativeSpace: NegativeSpace = NegativeSpace.UNKNOWN,
    val headroom: HeadroomLevel = HeadroomLevel.UNKNOWN,
    val confidence: Float = 0f,
    /** Reserved for future on-device vision. */
    val symmetry: Symmetry? = null,
    /** Reserved for future on-device vision. */
    val leadingLines: Boolean? = null,
)

@Serializable
data class CreativeLighting(
    val key: LightingKey = LightingKey.UNKNOWN,
    val contrast: LightingContrast = LightingContrast.UNKNOWN,
    val confidence: Float = 0f,
    /** Coarse hint: subject darker than the frame with bright highlights. Null when unmeasurable. */
    val backlightHint: Boolean? = null,
    /** Reserved for future on-device vision (true light-direction estimation). */
    val direction: LightingDirection? = null,
)

@Serializable
data class CreativeColor(
    val temperature: ColorTemperature = ColorTemperature.UNKNOWN,
    val tint: TintCast = TintCast.UNKNOWN,
    val confidence: Float = 0f,
)

@Serializable
data class CreativeDepth(
    /** Coarse proxy from subject dominance/segmentation — reported at low confidence. */
    val depthOfField: DepthOfField = DepthOfField.UNKNOWN,
    val confidence: Float = 0f,
    /** Reserved for future on-device vision (lens/focal character). */
    val lensCharacter: String? = null,
)

@Serializable
data class CreativeStyle(
    val mood: Mood = Mood.UNKNOWN,
    val confidence: Float = 0f,
)

// ---------------------------------------------------------------------------------------------
// Descriptive enums. All carry UNKNOWN so an unavailable inference is explicit, never fabricated.
// ---------------------------------------------------------------------------------------------

enum class CreativeSubjectKind { PORTRAIT, GROUP, ANIMAL, VEHICLE, PRODUCT, FOOD, SCENE, UNKNOWN }

enum class ShotType { EXTREME_CLOSE_UP, CLOSE_UP, MEDIUM_CLOSE_UP, MEDIUM, WIDE, UNKNOWN }

enum class CameraAngle { EYE_LEVEL, HIGH_ANGLE, LOW_ANGLE, UNKNOWN }

enum class SubjectPlacement { LEFT_THIRD, CENTER, RIGHT_THIRD, UNKNOWN }

enum class NegativeSpace { LOW, MEDIUM, HIGH, UNKNOWN }

enum class HeadroomLevel { TIGHT, BALANCED, GENEROUS, UNKNOWN }

/** Reserved — future symmetry estimation. */
enum class Symmetry { SYMMETRIC, ASYMMETRIC, UNKNOWN }

enum class LightingKey { LOW_KEY, BALANCED, HIGH_KEY, UNKNOWN }

enum class LightingContrast { LOW, MEDIUM, HIGH, UNKNOWN }

/** Reserved — future light-direction estimation. */
enum class LightingDirection { FRONT, SIDE, BACK, TOP, UNKNOWN }

enum class ColorTemperature { WARM, NEUTRAL, COOL, UNKNOWN }

enum class TintCast { GREEN, NEUTRAL, MAGENTA, UNKNOWN }

enum class DepthOfField { SHALLOW, DEEP, UNKNOWN }

enum class Mood { WARM_CINEMATIC, BRIGHT_AIRY, DARK_MOODY, NEUTRAL, UNKNOWN }

// ---------------------------------------------------------------------------------------------
// Phase 16.1 — richer semantic representation. All additive & @Serializable; a model with none of
// these populated is behaviourally identical to a Phase 16 model.
// ---------------------------------------------------------------------------------------------

/**
 * A structural relationship between scene elements, inferred from combined expert evidence. Carries
 * its own [strength]/[confidence] and a machine-readable [reason] so it is explainable and never a
 * bare label. Only emitted above a confidence floor — an unsupported relationship is simply absent.
 */
@Serializable
data class SceneRelationship(
    val kind: RelationshipKind,
    /** 0..1 — how pronounced the relationship is (e.g. how dominant the negative space). */
    val strength: Float,
    /** 0..1 — how confident the inference is given the evidence that produced it. */
    val confidence: Float,
    /** Short machine-readable rationale (diagnostics / future reasoning). */
    val reason: String,
)

/**
 * Structural relationships the analyzer can infer today from geometry + composition + segmentation +
 * semantic evidence. Deliberately a closed, stable vocabulary (photographer concepts), decoupled
 * from any matching signal. New members can be added without touching downstream reasoning.
 */
enum class RelationshipKind {
    SUBJECT_TO_FRAME,
    SUBJECT_TO_BACKGROUND,
    NEGATIVE_SPACE_DOMINANT,
    FOREGROUND_BACKGROUND_SEPARATION,
    VISUAL_HIERARCHY,
    DOMINANT_ANCHOR,
    SECONDARY_ANCHOR,
    FRAME_BALANCE,
    SUBJECT_ISOLATION,
    BACKGROUND_SIMPLICITY,
    SUBJECT_ORIENTATION,
    EYE_LINE,
    // Phase 16.2 — measured structural relationships from the Shot Identity layer.
    /** Strong tonal separation between subject and background (rim/silhouette/layer). */
    LAYER_SEPARATION,
    /** The subject is pressed against a frame edge — deliberate compositional tension. */
    EDGE_ANCHORING,
    /** The frame's visual weight leans decisively to one side. */
    DIRECTIONAL_FLOW,
    /** Palpable visual tension (off-balance + off-center + tight edge). */
    VISUAL_TENSION,
}

/**
 * The compact creative signature: the ordered few characteristics that define this shot's visual
 * identity. Kept small on purpose (see [CreativeSceneModel.signature]).
 */
@Serializable
data class CreativeSignature(
    /** Ordered most-defining first; ≤3 by default, ≤5 under very strong evidence. */
    val elements: List<SignatureElement> = emptyList(),
)

/**
 * One element of the creative signature. [label] is a stable, internal descriptor (NOT shown raw in
 * the normal UI — the `SignaturePresenter` translates it to a sentence). [salience] orders elements;
 * [source] records which expert family produced it (diagnostics).
 */
@Serializable
data class SignatureElement(
    val label: String,
    /** 0..1 — how strongly this characteristic defines the shot; used for ordering + the ≤3/≤5 cap. */
    val salience: Float,
    val source: SignatureSource,
)

enum class SignatureSource { GEOMETRY, COMPOSITION, LIGHTING, COLOR, SEGMENTATION, SEMANTIC, RELATIONSHIP, IDENTITY }

/**
 * Raw MobileCLIP provenance — Developer-diagnostics only. Never surfaced in the normal Reference
 * Analysis card and never read by the reasoning pipeline.
 */
@Serializable
data class SceneSemantics(
    val concepts: List<SemanticConcept> = emptyList(),
    val modelId: String = "",
)

/** One scored semantic concept from the MobileCLIP expert (raw — diagnostics only). */
@Serializable
data class SemanticConcept(
    val label: String,
    /** Cosine score of the image embedding against this concept's precomputed text embedding. */
    val score: Float,
    /** The creative aspect this concept informs, when it maps to one (else null). */
    val aspect: CreativeAspect? = null,
)
