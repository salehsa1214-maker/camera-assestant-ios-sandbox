package app.dyrecto.liveview.reference

/**
 * Central threshold configuration for the Shot Reference assistant (Phase 9). One named set per
 * [ReferenceTolerance] — no signal reads an inline literal, so every value here can be tuned in
 * one place after hardware validation.
 *
 * Units:
 *  - exposure deltas: luma codes (0..255), compared against histogram mean / P95;
 *  - warmth delta: channel-balance units (avgR − avgB, 0..255 scale) — see [ReferenceColorProfile];
 *  - subject position / headroom deltas: normalized image fraction (0..1);
 *  - subject size delta: *relative* change of the subject's linear size, e.g. 0.14 = the face
 *    got 14% larger/smaller than the reference (relative, so a small face and a large face drift
 *    at the same perceptual rate);
 *  - persistence/recovery: consecutive analyzed frames (never timers).
 */
object ReferenceConfig {

    /** Threshold set applied to every enabled signal for one tolerance level. */
    data class ReferenceThresholds(
        val exposureMeanDelta: Float,
        val exposureP95Delta: Float,
        val warmthDelta: Float,
        val subjectPositionDelta: Float,
        val subjectSizeDelta: Float,
        val headroomDelta: Float,
        /** Composition: mean L1 difference between 3×3 layout signatures (0..1). */
        val compositionDelta: Float,
        /** Visual similarity: minimum embedding cosine that still counts as matched. */
        val visualSimilarityMin: Float,
        /** AI features below this confidence are unavailable (state machine holds). */
        val minFeatureConfidence: Float,
        val requiredPersistenceFrames: Int,
        val requiredRecoveryFrames: Int,
    )

    val STRICT = ReferenceThresholds(
        exposureMeanDelta = 8f,
        exposureP95Delta = 8f,
        warmthDelta = 10f,
        subjectPositionDelta = 0.05f,
        subjectSizeDelta = 0.08f,
        headroomDelta = 0.04f,
        compositionDelta = 0.06f,
        visualSimilarityMin = 0.88f,
        minFeatureConfidence = 0.25f,
        requiredPersistenceFrames = 5,
        requiredRecoveryFrames = 5,
    )

    val MEDIUM = ReferenceThresholds(
        exposureMeanDelta = 14f,
        exposureP95Delta = 14f,
        warmthDelta = 18f,
        subjectPositionDelta = 0.09f,
        subjectSizeDelta = 0.14f,
        headroomDelta = 0.07f,
        compositionDelta = 0.10f,
        visualSimilarityMin = 0.80f,
        minFeatureConfidence = 0.25f,
        requiredPersistenceFrames = 5,
        requiredRecoveryFrames = 5,
    )

    val LOOSE = ReferenceThresholds(
        exposureMeanDelta = 22f,
        exposureP95Delta = 22f,
        warmthDelta = 28f,
        subjectPositionDelta = 0.14f,
        subjectSizeDelta = 0.22f,
        headroomDelta = 0.11f,
        compositionDelta = 0.16f,
        visualSimilarityMin = 0.70f,
        minFeatureConfidence = 0.25f,
        requiredPersistenceFrames = 5,
        requiredRecoveryFrames = 5,
    )

    /** Live AI snapshots older than this are treated as unavailable by the comparator. */
    const val AI_STALE_AFTER_MS = 3_000L

    fun thresholdsFor(tolerance: ReferenceTolerance): ReferenceThresholds = when (tolerance) {
        ReferenceTolerance.STRICT -> STRICT
        ReferenceTolerance.MEDIUM -> MEDIUM
        ReferenceTolerance.LOOSE -> LOOSE
    }
}
