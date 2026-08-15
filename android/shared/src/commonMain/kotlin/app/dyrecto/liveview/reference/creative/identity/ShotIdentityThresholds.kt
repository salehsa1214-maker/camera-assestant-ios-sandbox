package app.dyrecto.liveview.reference.creative.identity

/**
 * Phase 16.2 — the neutral anchors and saturations that turn a raw measurement into a [ShotTrait]'s
 * `distinctiveness` (0..1 = how far the value sits from "typical, unremarkable"). All tunables live
 * here (never as literals in the extractor), so post-hardware calibration is a data change, mirroring
 * the `PerceptualThresholds` / `ReferenceConfig` convention elsewhere in this codebase.
 *
 * `distinctiveness = clamp(|value − anchor| / saturation, 0, 1)` for symmetric axes; one-sided axes
 * (edge tension, isolation) document their own mapping at the call site. Larger saturation ⇒ only more
 * extreme values look distinctive (more conservative). [STRICT] is more sensitive (richer identity),
 * [LOOSE] more conservative; [MEDIUM] is the default. **First-pass values — hardware validation
 * pending.**
 */
data class ShotIdentityThresholds(
    /** Frame center the subject's horizontal position is measured against. */
    val positionAnchorX: Float,
    /** Resting vertical position (typical eye-line sits a little above center). */
    val positionAnchorY: Float,
    /** Off-center distance (normalized) at which placement reads as fully distinctive. */
    val positionSaturation: Float,

    /** Typical subject area fraction; deviation either way is distinctive. */
    val scaleAnchor: Float,
    /** Area-fraction deviation at which scale reads as fully distinctive. */
    val scaleSaturation: Float,

    /** Gap (normalized) from the nearest subject edge to the frame edge below which edge tension ramps
     *  toward 1 (gap 0 = pinned to the edge). Gaps at/above it contribute nothing. */
    val edgeTensionGap: Float,

    /** Typical empty-space fraction; deviation either way (vast emptiness OR subject filling the frame). */
    val negSpaceAnchor: Float,
    val negSpaceSaturation: Float,

    /** Subject↔background luma separation (0..255) at which the layer separation reads as fully distinctive. */
    val separationSaturation: Float,

    /** Mid-key luma; deviation is how far from a neutrally-exposed frame. */
    val tonalKeyAnchor: Float,
    val tonalKeySaturation: Float,

    /** Typical tonal spread (p95 − median, luma); deviation either way (flat OR punchy). */
    val contrastAnchor: Float,
    val contrastSaturation: Float,

    /** Grid center-of-mass offset (normalized) at which the frame reads as fully off-balance. */
    val balanceSaturation: Float,

    /** How spread (vs concentrated) the 3×3 weight is; deviation from this is distinctive. */
    val spreadSaturation: Float,

    /** Typical grid occupancy (mean cell fill); deviation either way (sparse OR dense). */
    val densityAnchor: Float,
    val densitySaturation: Float,

    /** Warm/cool cast magnitude (|avgR − avgB|) at which color reads as fully distinctive. */
    val warmthSaturation: Float,
    /** Green/magenta tint magnitude at which the cast reads as fully distinctive. */
    val castSaturation: Float,

    /** Top-concept margin over the median concept score at which a semantic marker is fully distinctive. */
    val semanticMarginSaturation: Float,

    /** Minimum distinctiveness for a trait to be kept at all (below it the shot isn't unusual on that axis). */
    val traitFloor: Float,
    /** At/above this, a trait is treated as a strong, identity-defining characteristic. */
    val strongDistinctiveness: Float,
) {
    /** Scale every saturation (sensitivity) while keeping the anchors fixed; used to derive STRICT/LOOSE. */
    private fun scaled(satFactor: Float, floor: Float): ShotIdentityThresholds = copy(
        positionSaturation = positionSaturation * satFactor,
        scaleSaturation = scaleSaturation * satFactor,
        edgeTensionGap = edgeTensionGap * satFactor,
        negSpaceSaturation = negSpaceSaturation * satFactor,
        separationSaturation = separationSaturation * satFactor,
        tonalKeySaturation = tonalKeySaturation * satFactor,
        contrastSaturation = contrastSaturation * satFactor,
        balanceSaturation = balanceSaturation * satFactor,
        spreadSaturation = spreadSaturation * satFactor,
        densitySaturation = densitySaturation * satFactor,
        warmthSaturation = warmthSaturation * satFactor,
        castSaturation = castSaturation * satFactor,
        semanticMarginSaturation = semanticMarginSaturation * satFactor,
        traitFloor = floor,
    )

    companion object {
        val MEDIUM = ShotIdentityThresholds(
            positionAnchorX = 0.5f,
            positionAnchorY = 0.45f,
            positionSaturation = 0.28f,
            scaleAnchor = 0.22f,
            scaleSaturation = 0.30f,
            edgeTensionGap = 0.12f,
            negSpaceAnchor = 0.55f,
            negSpaceSaturation = 0.40f,
            separationSaturation = 55f,
            tonalKeyAnchor = 128f,
            tonalKeySaturation = 70f,
            contrastAnchor = 60f,
            contrastSaturation = 100f,
            balanceSaturation = 0.30f,
            spreadSaturation = 0.30f,
            densityAnchor = 0.22f,
            densitySaturation = 0.45f,
            warmthSaturation = 55f,
            castSaturation = 28f,
            semanticMarginSaturation = 0.12f,
            traitFloor = 0.18f,
            strongDistinctiveness = 0.66f,
        )

        /** More sensitive — smaller saturations surface a richer identity. */
        val STRICT = MEDIUM.scaled(satFactor = 0.75f, floor = 0.12f)

        /** More conservative — only pronounced extremes register as distinctive. */
        val LOOSE = MEDIUM.scaled(satFactor = 1.35f, floor = 0.24f)

        /** Extraction default until per-reference strictness is wired through (deferred). */
        val DEFAULT = MEDIUM
    }
}
