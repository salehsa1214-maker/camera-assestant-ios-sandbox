package app.dyrecto.liveview.reference.creative.identity

import kotlinx.serialization.Serializable

/**
 * Phase 16.2 — Shot Identity.
 *
 * Where the [app.dyrecto.liveview.reference.creative.CreativeSceneModel]'s enum fields
 * answer *"what type of image is this?"*, the Shot Identity answers *"what makes THIS shot different
 * from thousands of visually similar shots?"*. It keeps the **continuous** measurements the analyzer
 * already computes (subject position/scale, negative-space direction, subject↔background tonal
 * separation, warmth magnitude, grid balance, …) as first-class, distinctiveness-scored [ShotTrait]s
 * instead of collapsing them into a handful of categorical buckets.
 *
 * Design invariants:
 * - **Derived once at import**, from signals the app already produces — no new models, never per-frame.
 * - **Distinctiveness, not description.** Each trait carries a [ShotTrait.distinctiveness]: how far the
 *   measured value sits from a neutral anchor (see `ShotIdentityThresholds`). An extreme value (subject
 *   jammed to the edge, 2 stops under the background, deeply off-balance) is distinctive; a middling one
 *   is not. This is what separates two "warm close-up portraits" that used to look identical.
 * - **Additive & serialized.** Attached to `CreativeSceneModel.identity` (kotlinx-serialization). Legacy
 *   models load with `identity = null` and behave exactly as pre-Phase-16.2.
 * - **Reasoning reads distinctiveness for PRIORITY only.** A trait raising an aspect's importance can
 *   only reorder which already-valid, already-measured drift is surfaced first — it never makes a signal
 *   valid/invalid and never manufactures a deviation (see `CreativeSceneAnalyzer.deriveImportance`).
 */
@Serializable
data class ShotIdentity(
    /** Distinctiveness-scored traits, ordered most-distinctive first. Empty when nothing stood out. */
    val traits: List<ShotTrait> = emptyList(),
) {
    /** The single most identity-defining trait, or null when none survived the distinctiveness floor. */
    val primary: ShotTrait? get() = traits.firstOrNull()

    /** Highest distinctiveness across all traits (0 when empty) — a coarse "how unusual is this shot". */
    val peakDistinctiveness: Float get() = traits.maxOfOrNull { it.distinctiveness } ?: 0f

    fun of(dimension: ShotTraitDimension): ShotTrait? = traits.firstOrNull { it.dimension == dimension }
}

/**
 * One measured, distinctiveness-scored characteristic of the shot.
 *
 * @property dimension the stable identity axis (decoupled from any matching signal).
 * @property value the raw measured value in the dimension's natural units (normalized 0..1 for geometry,
 *   luma 0..255 for exposure, channel-difference for color) — kept for diagnostics/tuning, never a label.
 * @property distinctiveness 0..1 — how far [value] sits from the neutral anchor; drives the signature and
 *   the (bounded, ordering-only) importance emphasis.
 * @property descriptor a stable, machine-readable key the presenter translates to a photographer sentence
 *   (e.g. `"subject_far_right"`, `"vast_negative_space_left"`, `"subject_well_under_background"`). NEVER a
 *   raw model concept.
 * @property direction spatial orientation when meaningful (which edge, where the empty space sits), else
 *   [TraitDirection.UNKNOWN].
 * @property confidence 0..1 — reliability of the underlying measurement (subject-box confidence, etc.).
 * @property reason short machine-readable evidence string (diagnostics / future reasoning).
 */
@Serializable
data class ShotTrait(
    val dimension: ShotTraitDimension,
    val value: Float,
    val distinctiveness: Float,
    val descriptor: String,
    val direction: TraitDirection = TraitDirection.UNKNOWN,
    val confidence: Float = 1f,
    val reason: String = "",
)

/**
 * The discriminative identity axes. Deliberately about **relationships and extremes** — the things that
 * make one shot different from another visually-similar shot — not scene categories. A closed, stable
 * vocabulary; new members can be added without touching downstream reasoning.
 */
enum class ShotTraitDimension {
    /** Where the subject sits horizontally in the frame (precise, not a thirds bucket). */
    SUBJECT_FRAME_POSITION,
    /** How large the subject is relative to the frame (precise area, not a shot-type bucket). */
    SUBJECT_SCALE,
    /** How hard the subject is pressed against a frame edge (intentional edge tension). */
    EDGE_ANCHORING,
    /** How much of the frame is empty space around the subject. */
    NEGATIVE_SPACE_VOLUME,
    /** Which side the empty space falls on (the space is part of the composition). */
    NEGATIVE_SPACE_DIRECTION,
    /** Tonal separation of the subject from its background (layer separation / rim / silhouette). */
    SUBJECT_BACKGROUND_SEPARATION,
    /** Overall brightness key of the frame (how dark/bright, continuous). */
    TONAL_KEY,
    /** Dominant tonal contrast (dynamic spread) of the frame. */
    DOMINANT_CONTRAST,
    /** How bright the background is on its own (part of the look). */
    BACKGROUND_BRIGHTNESS,
    /** Left/right/up/down imbalance of visual weight across the 3×3 grid. */
    VISUAL_BALANCE,
    /** How concentrated vs spread the visual weight is across the frame. */
    VISUAL_WEIGHT_SPREAD,
    /** Strength of the warm/cool cast (continuous magnitude, not a WARM/COOL bucket). */
    COLOR_WARMTH_MAGNITUDE,
    /** Strength of the green/magenta tint cast. */
    COLOR_CAST_STRENGTH,
    /** How isolated the subject is from its surroundings (low coverage + clean background). */
    SUBJECT_ISOLATION,
    /** How densely the frame is filled with content. */
    FRAME_DENSITY,
    /** A semantic concept that stands out unusually strongly for THIS image (MobileCLIP marker). */
    SEMANTIC_DISTINCTION,
}

/** Spatial orientation for direction-bearing traits (edge side, negative-space side, weight bias). */
enum class TraitDirection { LEFT, RIGHT, UP, DOWN, NONE, UNKNOWN }
