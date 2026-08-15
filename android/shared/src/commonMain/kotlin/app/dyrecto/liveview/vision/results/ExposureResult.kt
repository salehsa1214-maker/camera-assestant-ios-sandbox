package app.dyrecto.liveview.vision.results

/**
 * Final interpreted exposure verdict for one frame (Phase 6). Produced by
 * [app.dyrecto.liveview.exposure.ExposureAnalyzer] from a [HistogramResult] and a
 * [ZebraResult]. This is the single exposure signal the Scene layer consumes — it replaces the old
 * per-module highlight/shadow threshold results entirely.
 *
 * Per the Phase 6 spec: highlight detection comes from **Zebra** high-level coverage; shadow
 * detection comes from the **Histogram** shadow distribution.
 */
data class ExposureResult(
    override val moduleId: String,
    val highlightDetected: Boolean,
    val shadowDetected: Boolean,
    /** Fraction of the frame flagged as clipped highlights (0..100). */
    val highlightCoverage: Float,
    /** Fraction of the frame flagged as crushed shadows (0..100). */
    val shadowCoverage: Float,
    val exposureState: ExposureVerdict,
    /**
     * Confidence in [exposureState] (0.0..1.0). Derived from coverage margins / histogram
     * separation. A shared signal for the future AI Assistant and decision logic; higher-level
     * consumers may ignore it today, but it is populated now so the API is stable.
     */
    val exposureConfidence: Float,
    // --- Phase 7: confirmed-loss state machine output ---
    // These are populated once per frame by
    // [app.dyrecto.liveview.exposure.ExposureStateMachine], which sits between the
    // pure per-frame [ExposureAnalyzer] verdict above and the Scene layer. They add frame-based
    // persistence/recovery hysteresis so alerts fire only on *confirmed* multi-frame loss. Defaults
    // keep every existing constructor (and NORMAL/no-history call sites) compiling unchanged.
    /** Per-channel highlight state (NORMAL → CLIPPING → CONFIRMED → RECOVERING → NORMAL). */
    val highlightState: ExposureChannelState = ExposureChannelState.NORMAL,
    /** Per-channel shadow state. */
    val shadowState: ExposureChannelState = ExposureChannelState.NORMAL,
    /** Consecutive frames highlight coverage has stayed at/above the clip threshold (resets on break). */
    val highlightPersistenceFrames: Int = 0,
    /** Consecutive frames shadow coverage has stayed at/above the clip threshold (resets on break). */
    val shadowPersistenceFrames: Int = 0,
    /** Consecutive clean frames counted toward highlight recovery while confirmed (resets on break). */
    val highlightRecoveryFrames: Int = 0,
    /** Consecutive clean frames counted toward shadow recovery while confirmed (resets on break). */
    val shadowRecoveryFrames: Int = 0,
    /**
     * True only after highlight clipping has persisted for the configured number of consecutive
     * frames. NEVER a single-frame observation — this is the signal the alert layer keys off.
     */
    val highlightConfirmed: Boolean = false,
    /** True only after shadow detail loss has persisted for the configured number of frames. */
    val shadowConfirmed: Boolean = false,
) : VisionResult

/** Coarse exposure classification. */
enum class ExposureVerdict {
    NORMAL,
    HIGHLIGHT_CLIP,
    SHADOW_CLIP,
    MIXED,
}

/**
 * Per-channel exposure state-machine phase (Phase 7). Each channel (highlight, shadow) advances
 * independently: [NORMAL] → (coverage ≥ clip threshold, counting) [CLIPPING] → (persistence met)
 * [CONFIRMED] → (coverage < recovery threshold, counting) [RECOVERING] → (recovery met) [NORMAL].
 */
enum class ExposureChannelState {
    NORMAL,
    CLIPPING,
    CONFIRMED,
    RECOVERING,
}
