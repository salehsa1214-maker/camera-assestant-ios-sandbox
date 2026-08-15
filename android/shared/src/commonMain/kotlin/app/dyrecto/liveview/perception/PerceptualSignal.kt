package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceDriftDirection

/**
 * How strongly a human operator would perceive the difference, bucketed from
 * [PerceptualSignal.humanNoticeability] via the configurable cutoffs in [PerceptualThresholds].
 */
enum class PerceptualSeverity {
    NONE,
    SUBTLE,
    NOTICEABLE,
    OBVIOUS,
    SEVERE,
}

/**
 * Short-window behavior of a signal's noticeability, classified by the engine's rolling
 * [PerceptualSignal] history — NOT a hysteresis layer; it only informs damping and confidence.
 */
enum class PerceptualTrend {
    STABLE,
    INCREASING,
    DECREASING,
    OSCILLATING,
}

/**
 * Amplitude-hysteresis state of a perceptual signal:
 *  - [INSIDE]: perceptually matched (noticeability never crossed the enter threshold, or it
 *    recovered below the leave threshold);
 *  - [ENTERED]: drifting, currently at or above the enter threshold;
 *  - [HOLDING_LEAVE]: still drifting — noticeability fell below the enter threshold but has not
 *    yet reached the (lower) leave threshold, so the drift is held instead of oscillating.
 */
enum class HysteresisState {
    INSIDE,
    ENTERED,
    HOLDING_LEAVE,
}

/** Human-language similarity buckets — raw embedding cosine never leaves the perception layer. */
enum class PerceivedMatchBucket {
    PERFECT,
    IDENTICAL,
    VERY_CLOSE,
    SLIGHTLY_DIFFERENT,
    NOTICEABLY_DIFFERENT,
    DIFFERENT,
}

/**
 * The perceptual verdict for one reference signal (Phase 12 — Human Perception Layer).
 *
 * This object is the ONLY contract between perception and everything downstream (state machine
 * debounce consumes [perceptuallyDrifting] via `matched`; alerts and any future Copilot reasoning
 * consume this object) — raw deltas, histograms, and cosines never cross this boundary.
 *
 * [humanNoticeability] answers "can the operator see it?"; [importance] answers "how much should
 * the assistant care?"; [confidence] answers "does the difference exist?"; [directionConfidence]
 * answers "is the corrective direction right?".
 */
data class PerceptualSignal(
    /** Raw difference in the signal's own units (comparator delta or dominant composite metric). */
    val rawDifference: Float,
    /** 0..1 after dead zone + saturation normalization (0 = inside the dead zone). */
    val perceptualDifference: Float,
    /** 0..1 universal metric across all signals; curve owned by each evaluator. */
    val humanNoticeability: Float,
    /** 0..1 — strategy weight × per-signal salience × noticeability emphasis. */
    val importance: Float,
    /** 0..1 confidence that a difference exists. */
    val confidence: Float,
    /** 0..1 confidence in the correction direction (composition is intentionally low). */
    val directionConfidence: Float,
    val trend: PerceptualTrend = PerceptualTrend.STABLE,
    /** The actual dead zone applied after strategy/subject scaling (signal's own units). */
    val deadZoneApplied: Float,
    val hysteresisState: HysteresisState = HysteresisState.INSIDE,
    val severity: PerceptualSeverity = PerceptualSeverity.NONE,
    /** 0..1 weight of this signal under the active comparison strategy. */
    val strategyWeight: Float = 1f,
    /** Post-hysteresis decision — feeds the temporal state machine as `!matched`. */
    val perceptuallyDrifting: Boolean = false,
    val direction: ReferenceDriftDirection = ReferenceDriftDirection.NONE,
    /** Similarity only: the perceived-match bucket shown instead of the raw cosine. */
    val matchBucket: PerceivedMatchBucket? = null,
    /** Per-metric perceptual differences for composite signals (diagnostics only). */
    val components: Map<String, Float> = emptyMap(),
)
