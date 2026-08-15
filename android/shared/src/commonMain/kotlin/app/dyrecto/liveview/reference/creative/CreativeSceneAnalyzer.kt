package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.creative.experts.ColorExpert
import app.dyrecto.liveview.reference.creative.experts.CompositionExpert
import app.dyrecto.liveview.reference.creative.experts.CreativeAnalysisContext
import app.dyrecto.liveview.reference.creative.experts.CreativeContribution
import app.dyrecto.liveview.reference.creative.experts.GeometryExpert
import app.dyrecto.liveview.reference.creative.experts.LightingExpert
import app.dyrecto.liveview.reference.creative.experts.PriorityHint
import app.dyrecto.liveview.reference.creative.experts.SceneExpert
import app.dyrecto.liveview.reference.creative.experts.SegmentationExpert
import app.dyrecto.liveview.reference.creative.experts.SemanticExpert
import app.dyrecto.liveview.reference.creative.identity.ShotIdentity
import app.dyrecto.liveview.reference.creative.identity.ShotIdentityExtractor
import app.dyrecto.liveview.reference.creative.identity.ShotTrait
import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension
import app.dyrecto.liveview.reference.creative.semantic.SemanticObservation

/**
 * Phase 16 / 16.1 — derives the [CreativeSceneModel] for a reference image, ONCE at import.
 *
 * Phase 16.1 turns this from a monolith into an **orchestrator over independent Scene Experts**
 * ([GeometryExpert], [CompositionExpert], [LightingExpert], [ColorExpert], [SegmentationExpert],
 * [SemanticExpert]). The deterministic experts reproduce the pre-16.1 `deriveX` logic verbatim; the
 * semantic (MobileCLIP) expert adds evidence only when a [SemanticObservation] is supplied. The
 * orchestrator merges the contributions into one coherent model.
 *
 * Invariants preserved:
 * - **Byte-identical fallback.** With `semantic == null` (no model / load failure / legacy), the
 *   merged model's existing fields AND the `importance` map equal Phase 16 exactly. Relationships and
 *   the signature are additive fields not read by the reasoning pipeline.
 * - **Honesty over coverage.** Attributes that cannot be measured stay `UNKNOWN`/`null`; relationships
 *   are emitted only above a confidence floor, never invented.
 * - **Import-only, no new inference here.** Experts are pure functions of already-computed signals;
 *   the MobileCLIP inference happens upstream (in `ReferenceAnalyzer`), never per frame.
 */
object CreativeSceneAnalyzer {

    /** Only relationships at/above this confidence are kept. */
    private const val RELATIONSHIP_FLOOR = 0.4f
    /** Default signature size; the few characteristics that define the shot. */
    private const val SIGNATURE_DEFAULT = 3
    /** Absolute cap, allowed only for elements this salient. */
    private const val SIGNATURE_MAX = 5
    private const val SIGNATURE_STRONG = 0.7f
    /** Minimum salience for an element to enter the signature at all. */
    private const val SIGNATURE_MIN = 0.45f

    private val EXPERTS: List<SceneExpert> = listOf(
        GeometryExpert,
        CompositionExpert,
        LightingExpert,
        ColorExpert,
        SegmentationExpert,
        SemanticExpert,
    )

    fun analyze(
        exposure: ReferenceExposureProfile,
        color: ReferenceColorProfile,
        ai: ReferenceAiProfile?,
        faceBox: NormalizedRect?,
        semantic: SemanticObservation? = null,
    ): CreativeSceneModel {
        val subjectBox = ai?.primarySubjectBox ?: faceBox
        val boxIsFace = ai?.primarySubjectBox == null && faceBox != null

        val ctx = CreativeAnalysisContext(
            exposure = exposure,
            color = color,
            ai = ai,
            faceBox = faceBox,
            subjectBox = subjectBox,
            boxIsFace = boxIsFace,
            semantic = semantic,
        )

        val contributions = EXPERTS.map { it.observe(ctx) }

        // ---- merge authoritative core fields (each owned by exactly one expert) ------------------
        val subject = contributions.firstNotNullOfOrNull { it.subject } ?: CreativeSubject()
        val camera = contributions.firstNotNullOfOrNull { it.camera } ?: CreativeCamera()
        val placement = contributions.firstNotNullOfOrNull { it.placement } ?: SubjectPlacement.UNKNOWN
        val negativeSpace = contributions.firstNotNullOfOrNull { it.negativeSpace } ?: NegativeSpace.UNKNOWN
        val headroom = contributions.firstNotNullOfOrNull { it.headroom } ?: HeadroomLevel.UNKNOWN
        val compositionConfidence = contributions.firstNotNullOfOrNull { it.compositionConfidence } ?: 0f
        val lighting = contributions.firstNotNullOfOrNull { it.lighting } ?: CreativeLighting()
        val colorModel = contributions.firstNotNullOfOrNull { it.color } ?: CreativeColor()

        val composition = CreativeComposition(
            placement = placement,
            negativeSpace = negativeSpace,
            headroom = headroom,
            confidence = compositionConfidence,
        )
        val depth = deriveDepth(camera.shotType, subject)
        val style = deriveStyle(lighting.key, colorModel.temperature)

        // ---- quantitative shot identity (Phase 16.2) ---------------------------------------------
        // Deterministic continuous traits from the already-computed signals, plus any bounded semantic
        // marker (SemanticExpert, gated on evidence). Sources the identity-first signature AND the
        // identity-shaped importance below.
        val identity = buildIdentity(ctx, contributions)

        // ---- importance: categorical baseline, RESHAPED by identity distinctiveness (what matters
        //      most for THIS shot), then bounded semantic refinement. This is a PRIORITY signal only:
        //      it flows to reasoning through the unchanged CreativePriorityMapper multiplier and only
        //      reorders which already-valid drift is surfaced first — it never makes a signal valid or
        //      invalid, never manufactures a deviation, never fires an alert. -----------------------
        val baseImportance = deriveImportance(
            subject = subject,
            shotType = camera.shotType,
            placement = placement,
            negativeSpace = negativeSpace,
            headroom = headroom,
            lighting = lighting,
            color = colorModel,
        )
        val identityShaped = applyIdentityImportance(baseImportance, identity)
        val hints = if (semantic != null) contributions.flatMap { it.priorityHints } else emptyList()
        val importance = refineImportance(identityShaped, hints)

        // ---- additive structural understanding ---------------------------------------------------
        val relationships = mergeRelationships(contributions, identityRelationships(identity))
        val concepts = contributions.flatMap { it.concepts }
        val signature = buildSignature(subject, camera, composition, lighting, colorModel, relationships, concepts, identity)
        val semantics = semantic?.let { obs ->
            SceneSemantics(
                concepts = concepts.map { SemanticConcept(it.label, it.score, it.aspect) },
                modelId = obs.modelId,
            )
        }

        return CreativeSceneModel(
            subject = subject,
            camera = camera,
            composition = composition,
            lighting = lighting,
            color = colorModel,
            depth = depth,
            style = style,
            importance = importance,
            relationships = relationships,
            signature = signature,
            semantics = semantics,
            identity = identity,
        )
    }

    /**
     * Merge the deterministic identity traits with any expert-contributed traits (the semantic
     * marker), then order most-distinctive first. The deterministic extractor already floored its
     * traits; contributed traits carry their own floor.
     */
    private fun buildIdentity(
        ctx: CreativeAnalysisContext,
        contributions: List<CreativeContribution>,
    ): ShotIdentity {
        val deterministic = ShotIdentityExtractor.extract(ctx).traits
        val contributed = contributions.flatMap { it.traits }
        if (contributed.isEmpty()) return ShotIdentity(deterministic)
        val merged = (deterministic + contributed).sortedByDescending { it.distinctiveness }
        return ShotIdentity(merged)
    }

    // ---- orchestrator-level derivations (depend on multiple experts) -----------------------------

    private fun deriveDepth(shotType: ShotType, subject: CreativeSubject): CreativeDepth {
        // No blur/focus signal exists today, so DoF is a deliberately LOW-confidence hint, never a
        // measurement: tight framing of a single dominant subject *often* implies shallow depth.
        val tight = shotType == ShotType.EXTREME_CLOSE_UP ||
            shotType == ShotType.CLOSE_UP ||
            shotType == ShotType.MEDIUM_CLOSE_UP
        val singleSubject = subject.count <= 1 &&
            subject.kind != CreativeSubjectKind.GROUP &&
            subject.kind != CreativeSubjectKind.SCENE
        return if (tight && singleSubject) {
            CreativeDepth(DepthOfField.SHALLOW, confidence = 0.3f)
        } else {
            CreativeDepth(DepthOfField.UNKNOWN, confidence = 0f)
        }
    }

    private fun deriveStyle(key: LightingKey, temperature: ColorTemperature): CreativeStyle {
        val mood = when {
            key == LightingKey.LOW_KEY -> Mood.DARK_MOODY
            key == LightingKey.HIGH_KEY -> Mood.BRIGHT_AIRY
            temperature == ColorTemperature.WARM -> Mood.WARM_CINEMATIC
            else -> Mood.NEUTRAL
        }
        return CreativeStyle(mood, confidence = 0.6f)
    }

    // ---- importance inference (creative-domain, matching-independent) -----------------------------

    private fun deriveImportance(
        subject: CreativeSubject,
        shotType: ShotType,
        placement: SubjectPlacement,
        negativeSpace: NegativeSpace,
        headroom: HeadroomLevel,
        lighting: CreativeLighting,
        color: CreativeColor,
    ): Map<CreativeAspect, CreativePriority> {
        val out = LinkedHashMap<CreativeAspect, CreativePriority>()

        val hasSubject = subject.kind != CreativeSubjectKind.UNKNOWN && subject.kind != CreativeSubjectKind.SCENE
        out[CreativeAspect.SUBJECT] = if (hasSubject) {
            CreativePriority(0.9f, "clear primary subject present")
        } else {
            CreativePriority(0.4f, "no distinct subject")
        }

        val offThirds = placement == SubjectPlacement.LEFT_THIRD || placement == SubjectPlacement.RIGHT_THIRD
        out[CreativeAspect.SUBJECT_PLACEMENT] = if (offThirds) {
            CreativePriority(0.9f, "off-center thirds placement")
        } else if (placement == SubjectPlacement.CENTER) {
            CreativePriority(0.45f, "centered subject")
        } else {
            CreativePriority(0.3f, "subject placement unknown")
        }

        out[CreativeAspect.SUBJECT_SCALE] = when (shotType) {
            ShotType.EXTREME_CLOSE_UP, ShotType.CLOSE_UP ->
                CreativePriority(0.9f, "close-up framing")
            ShotType.MEDIUM_CLOSE_UP, ShotType.MEDIUM ->
                CreativePriority(0.6f, "medium framing")
            ShotType.WIDE -> CreativePriority(0.35f, "wide framing")
            ShotType.UNKNOWN -> CreativePriority(0.4f, "framing unknown")
        }

        if (headroom != HeadroomLevel.UNKNOWN) {
            out[CreativeAspect.HEADROOM] = when (headroom) {
                HeadroomLevel.GENEROUS -> CreativePriority(0.8f, "generous headroom")
                HeadroomLevel.TIGHT -> CreativePriority(0.7f, "tight headroom")
                else -> CreativePriority(0.5f, "balanced headroom")
            }
        }

        val negWeight = when (negativeSpace) {
            NegativeSpace.HIGH -> CreativePriority(1.0f, "strong negative space detected")
            NegativeSpace.MEDIUM -> CreativePriority(0.5f, "moderate negative space")
            NegativeSpace.LOW -> CreativePriority(0.2f, "subject fills the frame")
            NegativeSpace.UNKNOWN -> CreativePriority(0.3f, "negative space unknown")
        }
        out[CreativeAspect.NEGATIVE_SPACE] = negWeight

        out[CreativeAspect.COMPOSITION] = when {
            negativeSpace == NegativeSpace.HIGH -> CreativePriority(0.95f, "strong negative space detected")
            offThirds -> CreativePriority(0.8f, "deliberate off-center composition")
            else -> CreativePriority(0.5f, "balanced composition")
        }

        out[CreativeAspect.LIGHTING] = when {
            lighting.key == LightingKey.HIGH_KEY -> CreativePriority(0.85f, "high-key lighting")
            lighting.key == LightingKey.LOW_KEY -> CreativePriority(0.85f, "low-key lighting")
            lighting.contrast == LightingContrast.HIGH -> CreativePriority(0.8f, "high-contrast lighting")
            else -> CreativePriority(0.5f, "balanced lighting")
        }

        out[CreativeAspect.COLOR] = when (color.temperature) {
            ColorTemperature.WARM -> CreativePriority(0.8f, "strong warm cast")
            ColorTemperature.COOL -> CreativePriority(0.8f, "strong cool cast")
            ColorTemperature.NEUTRAL -> CreativePriority(0.2f, "near-neutral white balance")
            ColorTemperature.UNKNOWN -> CreativePriority(0.3f, "color temperature unknown")
        }

        // Depth of field cannot be measured today — kept as a low, neutral emphasis.
        out[CreativeAspect.DEPTH_OF_FIELD] = CreativePriority(0.3f, "depth of field not reliably measured")

        // Background matters more when the subject does NOT dominate the frame.
        out[CreativeAspect.BACKGROUND] = when (negativeSpace) {
            NegativeSpace.LOW -> CreativePriority(0.2f, "subject-dominated frame")
            NegativeSpace.HIGH -> CreativePriority(0.6f, "background carries the scene")
            else -> CreativePriority(0.4f, "background contributes context")
        }

        out[CreativeAspect.MOOD] = CreativePriority(0.5f, "overall mood anchor")

        return out
    }

    /**
     * Fold bounded semantic [hints] into the baseline importance. Refines only aspects already in the
     * baseline, clamps to [0,1], and returns the baseline unchanged when there are no hints — so the
     * `CreativePriorityMapper` multiplier math is untouched and the no-model path is byte-identical.
     */
    private fun refineImportance(
        base: Map<CreativeAspect, CreativePriority>,
        hints: List<PriorityHint>,
    ): Map<CreativeAspect, CreativePriority> {
        if (hints.isEmpty()) return base
        val strongest = hints.filter { it.boost > 0f }.groupBy { it.aspect }
        if (strongest.isEmpty()) return base
        val out = LinkedHashMap(base)
        for ((aspect, group) in strongest) {
            val current = out[aspect] ?: continue // never introduce a new aspect
            val best = group.maxByOrNull { it.boost }!!
            val newWeight = (current.weight + best.boost).coerceIn(0f, 1f)
            if (newWeight != current.weight) {
                out[aspect] = current.copy(
                    weight = newWeight,
                    reason = "${current.reason}; ${best.reason}",
                )
            }
        }
        return out
    }

    // ---- identity-shaped importance (Phase 16.2) --------------------------------------------------

    /**
     * The stable coupling of an identity axis to the creative aspects it informs. Lives HERE (not in
     * the reasoning layer) so the persisted model's importance already reflects "what matters most for
     * THIS shot"; the reasoning-layer `CreativePriorityMapper` (aspect → signal) is untouched.
     */
    private val DIMENSION_ASPECTS: Map<ShotTraitDimension, List<CreativeAspect>> = mapOf(
        ShotTraitDimension.SUBJECT_FRAME_POSITION to listOf(CreativeAspect.SUBJECT_PLACEMENT),
        ShotTraitDimension.NEGATIVE_SPACE_DIRECTION to listOf(CreativeAspect.NEGATIVE_SPACE, CreativeAspect.COMPOSITION),
        ShotTraitDimension.NEGATIVE_SPACE_VOLUME to listOf(CreativeAspect.NEGATIVE_SPACE, CreativeAspect.BACKGROUND),
        ShotTraitDimension.SUBJECT_SCALE to listOf(CreativeAspect.SUBJECT_SCALE),
        ShotTraitDimension.EDGE_ANCHORING to listOf(CreativeAspect.COMPOSITION, CreativeAspect.SUBJECT_PLACEMENT),
        ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION to listOf(CreativeAspect.BACKGROUND, CreativeAspect.LIGHTING),
        ShotTraitDimension.TONAL_KEY to listOf(CreativeAspect.LIGHTING),
        ShotTraitDimension.DOMINANT_CONTRAST to listOf(CreativeAspect.LIGHTING),
        ShotTraitDimension.BACKGROUND_BRIGHTNESS to listOf(CreativeAspect.BACKGROUND),
        ShotTraitDimension.VISUAL_BALANCE to listOf(CreativeAspect.COMPOSITION),
        ShotTraitDimension.VISUAL_WEIGHT_SPREAD to listOf(CreativeAspect.COMPOSITION),
        ShotTraitDimension.COLOR_WARMTH_MAGNITUDE to listOf(CreativeAspect.COLOR),
        ShotTraitDimension.COLOR_CAST_STRENGTH to listOf(CreativeAspect.COLOR),
        ShotTraitDimension.SUBJECT_ISOLATION to listOf(CreativeAspect.BACKGROUND, CreativeAspect.COMPOSITION),
        ShotTraitDimension.FRAME_DENSITY to listOf(CreativeAspect.COMPOSITION),
        // SEMANTIC_DISTINCTION nudges its concept's aspect through the gated priorityHints path, not here.
    )

    /** A fully-distinctive trait lifts its aspect toward 1.0; a mild one keeps it modest — deliberately
     *  demoting categorical importance when the shot isn't actually unusual on that axis. */
    private const val IDENTITY_WEIGHT_BASE = 0.30f
    private const val IDENTITY_WEIGHT_SPAN = 0.70f

    /**
     * Reshape the categorical baseline so each aspect's importance reflects HOW distinctive this shot
     * really is on that axis (the measured trait), rather than a flat per-category value. Aspects with
     * no mapped trait keep their baseline; the aspect SET is never changed (the mapper still finds every
     * aspect). This feeds importance ONLY — a priority/ordering signal that the unchanged
     * `CreativePriorityMapper` turns into a bounded multiplier; it never alters a measured deviation,
     * a match, or whether an alert fires.
     */
    private fun applyIdentityImportance(
        base: Map<CreativeAspect, CreativePriority>,
        identity: ShotIdentity,
    ): Map<CreativeAspect, CreativePriority> {
        if (identity.traits.isEmpty()) return base
        val best = HashMap<CreativeAspect, ShotTrait>()
        for (tr in identity.traits) {
            val aspects = DIMENSION_ASPECTS[tr.dimension] ?: continue
            for (aspect in aspects) {
                val cur = best[aspect]
                if (cur == null || tr.distinctiveness > cur.distinctiveness) best[aspect] = tr
            }
        }
        if (best.isEmpty()) return base
        val out = LinkedHashMap(base)
        for ((aspect, tr) in best) {
            if (!out.containsKey(aspect)) continue // never introduce or remove an aspect
            val weight = (IDENTITY_WEIGHT_BASE + IDENTITY_WEIGHT_SPAN * tr.distinctiveness).coerceIn(0f, 1f)
            out[aspect] = CreativePriority(weight, tr.reason)
        }
        return out
    }

    // ---- relationships & signature ----------------------------------------------------------------

    /** Concatenate expert + identity relationships, drop below-floor ones, keep the strongest per kind. */
    private fun mergeRelationships(
        contributions: List<CreativeContribution>,
        identityRelationships: List<SceneRelationship>,
    ): List<SceneRelationship> =
        (contributions.asSequence().flatMap { it.relationships.asSequence() } + identityRelationships.asSequence())
            .filter { it.confidence >= RELATIONSHIP_FLOOR }
            .groupBy { it.kind }
            .map { (_, group) -> group.maxByOrNull { it.confidence * it.strength }!! }
            .sortedByDescending { it.confidence * it.strength }
            .toList()

    /**
     * Phase 16.2 — measured structural relationships from strong identity traits. These give the
     * relationship layer real magnitudes/directions (subject↔background separation, edge anchoring,
     * directional weight, visual tension) instead of presence-only semantic hints. Merged (deduped by
     * kind) with the expert relationships; nothing is invented — each maps a trait that survived the
     * distinctiveness floor.
     */
    private fun identityRelationships(identity: ShotIdentity): List<SceneRelationship> {
        val out = mutableListOf<SceneRelationship>()
        identity.traits.forEach { tr ->
            when (tr.dimension) {
                ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION ->
                    out += SceneRelationship(
                        RelationshipKind.LAYER_SEPARATION, tr.distinctiveness, tr.confidence,
                        "measured subject/background separation",
                    )
                ShotTraitDimension.EDGE_ANCHORING ->
                    out += SceneRelationship(
                        RelationshipKind.EDGE_ANCHORING, tr.distinctiveness, tr.confidence,
                        "subject anchored to the ${tr.direction.name.lowercase()} edge",
                    )
                ShotTraitDimension.VISUAL_BALANCE ->
                    out += SceneRelationship(
                        RelationshipKind.DIRECTIONAL_FLOW, tr.distinctiveness, tr.confidence,
                        "visual weight leans ${tr.direction.name.lowercase()}",
                    )
                ShotTraitDimension.NEGATIVE_SPACE_VOLUME ->
                    if (tr.descriptor.startsWith("vast") || tr.descriptor.startsWith("generous")) {
                        out += SceneRelationship(
                            RelationshipKind.NEGATIVE_SPACE_DOMINANT, tr.distinctiveness, tr.confidence,
                            "measured empty space dominates the frame",
                        )
                    }
                ShotTraitDimension.SUBJECT_ISOLATION ->
                    out += SceneRelationship(
                        RelationshipKind.SUBJECT_ISOLATION, tr.distinctiveness, tr.confidence,
                        "measured subject isolation",
                    )
                else -> Unit
            }
        }
        // Visual tension = a subject pressed to an edge while the frame's weight leans off-center.
        val edge = identity.of(ShotTraitDimension.EDGE_ANCHORING)
        val balance = identity.of(ShotTraitDimension.VISUAL_BALANCE)
        if (edge != null && balance != null && edge.distinctiveness >= 0.5f && balance.distinctiveness >= 0.4f) {
            out += SceneRelationship(
                RelationshipKind.VISUAL_TENSION,
                minOf(edge.distinctiveness, balance.distinctiveness),
                minOf(edge.confidence, balance.confidence),
                "edge anchoring with off-center visual weight",
            )
        }
        return out
    }

    /**
     * Phase 16.2 — assemble the compact creative signature, IDENTITY-FIRST: the few measured
     * characteristics that make THIS shot recognizable, plus the strongest structural relationship and
     * the single dominant semantic read. Categorical labels (portrait / close-up / warm) are demoted
     * to a FALLBACK that only fills up when the identity/structure evidence was too thin — so the
     * signature answers "what makes this shot unique", not "what type of image is this".
     *
     * Cap unchanged: ≤3 by default, ≤5 under very strong evidence. Labels are stable internal
     * descriptors — the `SignaturePresenter` translates them to photographer-facing sentences.
     */
    private fun buildSignature(
        subject: CreativeSubject,
        camera: CreativeCamera,
        composition: CreativeComposition,
        lighting: CreativeLighting,
        color: CreativeColor,
        relationships: List<SceneRelationship>,
        concepts: List<app.dyrecto.liveview.reference.creative.semantic.ConceptScore>,
        identity: ShotIdentity,
    ): CreativeSignature? {
        // ---- PRIMARY pool: the shot's distinctive identity + its structure --------------------------
        val primary = mutableListOf<SignatureElement>()
        // Deterministic identity traits become the headline; the semantic marker is carried by the
        // reserved concept slot below instead (so it is never double-counted here).
        identity.traits
            .filter { it.dimension != ShotTraitDimension.SEMANTIC_DISTINCTION }
            .forEach { tr ->
                val salience = (tr.distinctiveness * tr.confidence).coerceIn(0f, 1f)
                primary += SignatureElement("trait_${tr.descriptor}", salience, SignatureSource.IDENTITY)
            }
        relationships.forEach { rel ->
            primary += SignatureElement(
                label = "rel_${rel.kind.name.lowercase()}",
                salience = (rel.confidence * rel.strength).coerceIn(0f, 1f),
                source = SignatureSource.RELATIONSHIP,
            )
        }

        val ranked = primary
            .filter { it.salience >= SIGNATURE_MIN }
            .groupBy { it.label }
            .map { (_, g) -> g.maxByOrNull { it.salience }!! }
            .sortedByDescending { it.salience }

        val chosen = mutableListOf<SignatureElement>()
        for (el in ranked) {
            if (chosen.size < SIGNATURE_DEFAULT) chosen += el
            else if (chosen.size < SIGNATURE_MAX && el.salience >= SIGNATURE_STRONG) chosen += el
            else break
        }

        // Always let the strongest semantic read leave one fingerprint (the dominant concept), so two
        // shots that differ only in WHAT they depict still read differently. Bounded by the cap.
        val topConcept = concepts.filter { it.score >= SIGNATURE_MIN }.maxByOrNull { it.score }
        if (topConcept != null) {
            val label = "concept_${topConcept.label}"
            if (chosen.none { it.label == label } && chosen.size < SIGNATURE_MAX) {
                chosen += SignatureElement(label, topConcept.score.coerceIn(0f, 1f), SignatureSource.SEMANTIC)
            }
        }

        // ---- FALLBACK: only when identity+structure were too thin, top up with categorical labels ---
        if (chosen.size < SIGNATURE_DEFAULT) {
            val fallback = categoricalCandidates(subject, camera, composition, lighting, color)
                .filter { it.salience >= SIGNATURE_MIN }
                .sortedByDescending { it.salience }
            for (el in fallback) {
                if (chosen.size >= SIGNATURE_DEFAULT) break
                if (chosen.none { it.label == el.label }) chosen += el
            }
        }

        if (chosen.isEmpty()) return null
        return CreativeSignature(chosen.sortedByDescending { it.salience }.take(SIGNATURE_MAX))
    }

    /** The demoted categorical candidates — a fallback pool for signatures with thin identity evidence. */
    private fun categoricalCandidates(
        subject: CreativeSubject,
        camera: CreativeCamera,
        composition: CreativeComposition,
        lighting: CreativeLighting,
        color: CreativeColor,
    ): List<SignatureElement> {
        val candidates = mutableListOf<SignatureElement>()
        if (composition.negativeSpace == NegativeSpace.HIGH) {
            candidates += SignatureElement("negative_space_dominant", 0.9f, SignatureSource.COMPOSITION)
        }
        if (composition.placement == SubjectPlacement.LEFT_THIRD || composition.placement == SubjectPlacement.RIGHT_THIRD) {
            candidates += SignatureElement("placement_off_thirds", 0.6f * composition.confidence.orOne(), SignatureSource.COMPOSITION)
        }
        when (lighting.key) {
            LightingKey.HIGH_KEY -> candidates += SignatureElement("lighting_high_key", lighting.confidence, SignatureSource.LIGHTING)
            LightingKey.LOW_KEY -> candidates += SignatureElement("lighting_low_key", lighting.confidence, SignatureSource.LIGHTING)
            else -> if (lighting.contrast == LightingContrast.HIGH) {
                candidates += SignatureElement("lighting_high_contrast", lighting.confidence, SignatureSource.LIGHTING)
            }
        }
        if (lighting.backlightHint == true) {
            candidates += SignatureElement("lighting_backlit", 0.75f, SignatureSource.LIGHTING)
        }
        when (color.temperature) {
            ColorTemperature.WARM -> candidates += SignatureElement("color_warm", color.confidence, SignatureSource.COLOR)
            ColorTemperature.COOL -> candidates += SignatureElement("color_cool", color.confidence, SignatureSource.COLOR)
            else -> Unit
        }
        when (camera.shotType) {
            ShotType.EXTREME_CLOSE_UP, ShotType.CLOSE_UP ->
                candidates += SignatureElement("shot_close", camera.shotTypeConfidence, SignatureSource.GEOMETRY)
            ShotType.WIDE ->
                candidates += SignatureElement("shot_wide", camera.shotTypeConfidence, SignatureSource.GEOMETRY)
            else -> Unit
        }
        if (subject.kind != CreativeSubjectKind.UNKNOWN && subject.kind != CreativeSubjectKind.SCENE && subject.confidence > 0f) {
            candidates += SignatureElement("subject_${subject.kind.name.lowercase()}", subject.confidence * 0.7f, SignatureSource.GEOMETRY)
        }
        return candidates
    }

    private fun Float.orOne(): Float = if (this <= 0f) 1f else this
}
