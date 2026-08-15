package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension

/**
 * Phase 16 / 16.2 — translates the internal creative understanding (the measured shot identity,
 * signature, and relationships) into plain photographer-facing sentences for the Reference Analysis
 * card. Pure Kotlin / Compose-free / JVM-testable.
 *
 * Phase 16.2: the card now LEADS with a "Shot Identity" line built from the measured identity traits
 * (what makes THIS shot unique), then the compact "Visual Signature", then structure/lighting. The
 * categorical metadata rows live below, in the UI.
 *
 * Hard rule (unchanged): this NEVER exposes raw MobileCLIP concepts, concept scores, embedding values,
 * model ids, or AI terminology. Only controlled, deterministic descriptors and a CURATED set of
 * concept labels are phrased; anything not in the maps (including raw model text) is skipped, so
 * nothing internal can leak.
 */
object SignaturePresenter {

    /** One titled, human-readable insight row. */
    data class Insight(val title: String, val text: String)

    fun insights(model: CreativeSceneModel?): List<Insight> {
        if (model == null) return emptyList()
        return buildList {
            shotIdentity(model)?.let { add(Insight("Shot Identity", it)) }
            visualSignature(model)?.let { add(Insight("Visual Signature", it)) }
            sceneStructure(model)?.let { add(Insight("Scene Structure", it)) }
            lightingRelationship(model)?.let { add(Insight("Lighting Relationship", it)) }
        }
    }

    // ---- Shot Identity: the measured characteristics that make THIS shot distinctive ---------------

    private fun shotIdentity(model: CreativeSceneModel): String? {
        val phrases = model.identity?.traits.orEmpty()
            .asSequence()
            .mapNotNull { traitDescriptorPhrase(it.dimension, it.descriptor) }
            .distinct()
            .take(3)
            .toList()
        if (phrases.isEmpty()) return null
        return capitalize("defined by " + joinNatural(phrases) + ".")
    }

    // ---- Visual Signature: the few defining signature elements, as one sentence --------------------

    private fun visualSignature(model: CreativeSceneModel): String? {
        val phrases = model.signature?.elements.orEmpty()
            .asSequence()
            .mapNotNull { signaturePhrase(it.label) } // skips anything untranslatable → no raw leak
            .distinct()
            .take(3)
            .toList()
        if (phrases.isEmpty()) return null
        return capitalize("defined by " + joinNatural(phrases) + ".")
    }

    /** Signature label → phrase. Handles Phase-16.2 identity (`trait_*`) and semantic (`concept_*`) labels
     *  plus the demoted categorical / relationship labels. Returns null for anything not translatable. */
    private fun signaturePhrase(label: String): String? = when {
        label.startsWith("trait_") -> traitPhrase(label.removePrefix("trait_"))
        label.startsWith("concept_") -> conceptPhrase(label.removePrefix("concept_"))
        else -> categoricalPhrase(label)
    }

    private fun traitDescriptorPhrase(dimension: ShotTraitDimension, descriptor: String): String? =
        if (dimension == ShotTraitDimension.SEMANTIC_DISTINCTION) conceptPhrase(descriptor)
        else traitPhrase(descriptor)

    /** Deterministic identity descriptors → photographer phrases. Non-distinctive descriptors (centered,
     *  even-with-background) return null: they are not part of what makes the shot unique. */
    private fun traitPhrase(descriptor: String): String? = when (descriptor) {
        "subject_far_right" -> "a subject pushed to the right edge"
        "subject_right_of_center" -> "a subject set right of center"
        "subject_far_left" -> "a subject pushed to the left edge"
        "subject_left_of_center" -> "a subject set left of center"
        "subject_dominates_frame" -> "a subject that dominates the frame"
        "subject_large_in_frame" -> "a large subject in the frame"
        "subject_tiny_in_frame" -> "a tiny subject in the frame"
        "subject_small_in_frame" -> "a small subject in the frame"
        "subject_pinned_left" -> "a subject pinned to the left edge"
        "subject_pinned_right" -> "a subject pinned to the right edge"
        "subject_pinned_up" -> "a subject pinned to the top edge"
        "subject_pinned_down" -> "a subject pinned to the bottom edge"
        "vast_negative_space" -> "vast empty space"
        "generous_negative_space" -> "generous empty space"
        "frame_filling_subject" -> "a frame-filling subject"
        "tight_negative_space" -> "a tightly-packed frame"
        "negative_space_left" -> "open space to the left"
        "negative_space_right" -> "open space to the right"
        "negative_space_up" -> "open space above the subject"
        "negative_space_down" -> "open space below the subject"
        "subject_well_under_background" -> "a subject set well under a brighter background"
        "subject_under_background" -> "a subject darker than its background"
        "subject_well_over_background" -> "a subject much brighter than its background"
        "subject_over_background" -> "a subject brighter than its background"
        "dark_frame" -> "a dark overall exposure"
        "bright_frame" -> "a bright overall exposure"
        "high_contrast" -> "high tonal contrast"
        "flat_contrast" -> "a flat, low-contrast tone"
        "weight_left" -> "visual weight to the left"
        "weight_right" -> "visual weight to the right"
        "weight_up" -> "visual weight up high"
        "weight_down" -> "visual weight down low"
        "dense_frame" -> "a densely filled frame"
        "sparse_frame" -> "a sparse, open frame"
        "warm_palette" -> "a warm palette"
        "cool_palette" -> "a cool palette"
        "green_cast" -> "a green color cast"
        "magenta_cast" -> "a magenta color cast"
        "isolated_subject" -> "an isolated subject"
        "strongly_isolated_subject" -> "a strongly isolated subject"
        else -> null // subject_centered / subject_even_with_background / unknown → not distinctive
    }

    /** CURATED translations of the bundled concept vocabulary — clean phrases, never the raw label. Any
     *  concept not listed here (including raw model text) is skipped, so nothing internal can leak. */
    private fun conceptPhrase(label: String): String? = when (label) {
        "isolated_clean_subject" -> "a cleanly isolated subject"
        "cluttered_busy_scene" -> "a busy, cluttered scene"
        "subject_pushed_to_edge" -> "a subject at the frame edge"
        "subject_centered_calm" -> "a calm, centered subject"
        "layered_depth" -> "layered depth"
        "flat_single_plane" -> "a flat, single-plane look"
        "shallow_focus_separation" -> "shallow-focus separation"
        "deep_focus_sharp" -> "deep, front-to-back focus"
        "dramatic_directional_light" -> "dramatic directional light"
        "flat_even_light" -> "flat, even light"
        "backlit_rim_separation" -> "a backlit, rim-separated subject"
        "silhouette_against_light" -> "a silhouette against the light"
        "minimalist_empty_space" -> "a minimalist, empty composition"
        "dense_edge_to_edge_detail" -> "dense, edge-to-edge detail"
        "single_dominant_anchor" -> "a single dominant focal point"
        "attention_spread_evenly" -> "attention spread across the frame"
        "symmetrical_balanced" -> "a symmetrical, balanced composition"
        "asymmetric_visual_tension" -> "asymmetric visual tension"
        "diagonal_dynamic_energy" -> "dynamic diagonal energy"
        "horizontal_calm_stillness" -> "calm, horizontal stillness"
        "intimate_tight_frame" -> "an intimate, tight frame"
        "distant_environmental" -> "a distant, environmental framing"
        "bold_saturated_color" -> "bold, saturated color"
        "muted_desaturated_color" -> "a muted, desaturated palette"
        "tense_dramatic_mood" -> "a tense, dramatic mood"
        "calm_serene_mood" -> "a calm, serene mood"
        else -> null
    }

    /** The demoted categorical / relationship labels → phrases (unchanged from Phase 16.1). */
    private fun categoricalPhrase(label: String): String? = when (label) {
        "negative_space_dominant", "rel_negative_space_dominant" -> "strong negative space"
        "placement_off_thirds" -> "off-center thirds placement"
        "lighting_high_key" -> "bright high-key lighting"
        "lighting_low_key" -> "dark low-key lighting"
        "lighting_high_contrast" -> "high-contrast lighting"
        "lighting_backlit" -> "a backlit subject"
        "color_warm" -> "warm tones"
        "color_cool" -> "cool tones"
        "shot_close" -> "a tight close-up"
        "shot_wide" -> "a wide framing"
        "subject_portrait" -> "a single portrait subject"
        "subject_group" -> "a group of subjects"
        "subject_animal" -> "an animal subject"
        "subject_vehicle" -> "a vehicle subject"
        "subject_product" -> "a product subject"
        "subject_food" -> "a food subject"
        "rel_subject_isolation" -> "an isolated subject"
        "rel_background_simplicity" -> "a simple background"
        "rel_dominant_anchor" -> "a strong focal anchor"
        "rel_subject_to_background" -> "clear subject–background separation"
        "rel_foreground_background_separation" -> "foreground–background separation"
        "rel_frame_balance" -> "a balanced frame"
        "rel_visual_hierarchy" -> "a clear visual hierarchy"
        "rel_layer_separation" -> "strong layer separation"
        "rel_edge_anchoring" -> "an edge-anchored subject"
        "rel_directional_flow" -> "a strong directional lean"
        "rel_visual_tension" -> "deliberate visual tension"
        else -> null // concept_* / unknown labels are intentionally not surfaced here
    }

    // ---- Scene Structure: the strongest structural relationship -----------------------------------

    private val STRUCTURE_PRIORITY = listOf(
        RelationshipKind.NEGATIVE_SPACE_DOMINANT,
        RelationshipKind.SUBJECT_ISOLATION,
        RelationshipKind.LAYER_SEPARATION,
        RelationshipKind.EDGE_ANCHORING,
        RelationshipKind.VISUAL_TENSION,
        RelationshipKind.DIRECTIONAL_FLOW,
        RelationshipKind.BACKGROUND_SIMPLICITY,
        RelationshipKind.VISUAL_HIERARCHY,
        RelationshipKind.DOMINANT_ANCHOR,
        RelationshipKind.FRAME_BALANCE,
    )

    private fun sceneStructure(model: CreativeSceneModel): String? {
        val present = model.relationships.map { it.kind }.toSet()
        val kind = STRUCTURE_PRIORITY.firstOrNull { it in present } ?: return null
        return when (kind) {
            RelationshipKind.NEGATIVE_SPACE_DOMINANT -> "Strong negative space defines the frame."
            RelationshipKind.SUBJECT_ISOLATION -> "The subject is isolated against its surroundings."
            RelationshipKind.LAYER_SEPARATION -> "The subject separates cleanly from its background."
            RelationshipKind.EDGE_ANCHORING -> "The subject is anchored against the frame edge."
            RelationshipKind.VISUAL_TENSION -> "The frame carries deliberate visual tension."
            RelationshipKind.DIRECTIONAL_FLOW -> "The composition leans decisively to one side."
            RelationshipKind.BACKGROUND_SIMPLICITY -> "A simple background keeps the focus on the subject."
            RelationshipKind.VISUAL_HIERARCHY -> "The frame has a clear visual hierarchy."
            RelationshipKind.DOMINANT_ANCHOR -> "One dominant element anchors the composition."
            RelationshipKind.FRAME_BALANCE -> "The frame is deliberately balanced."
            else -> null
        }
    }

    // ---- Lighting Relationship: separation / backlight only (attributes cover the rest) -----------

    private fun lightingRelationship(model: CreativeSceneModel): String? {
        if (model.lighting.backlightHint == true) {
            return "Subject separated from a brighter background."
        }
        val separation = model.relationships.any {
            it.kind == RelationshipKind.FOREGROUND_BACKGROUND_SEPARATION ||
                it.kind == RelationshipKind.SUBJECT_TO_BACKGROUND ||
                it.kind == RelationshipKind.LAYER_SEPARATION
        }
        return if (separation) "Subject stands out from its background." else null
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private fun joinNatural(items: List<String>): String = when (items.size) {
        0 -> ""
        1 -> items[0]
        2 -> "${items[0]} and ${items[1]}"
        else -> items.dropLast(1).joinToString(", ") + " and " + items.last()
    }

    private fun capitalize(s: String): String =
        if (s.isEmpty()) s else s[0].uppercaseChar() + s.substring(1)
}
