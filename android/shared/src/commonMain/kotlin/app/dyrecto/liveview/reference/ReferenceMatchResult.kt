package app.dyrecto.liveview.reference

import app.dyrecto.liveview.instructions.AssistantInstruction
import app.dyrecto.liveview.perception.PerceptualSignal
import app.dyrecto.liveview.reference.ai.ComparisonStrategy

/** The comparison signals the Shot Reference assistant evaluates. */
enum class ReferenceSignal {
    EXPOSURE,
    SUBJECT_POSITION,
    SUBJECT_SIZE,
    WHITE_BALANCE,
    HEADROOM,
    FACE_PRESENCE,
    EYE_VISIBILITY,

    // Phase 10 — AI perception signals.
    /** The reference's primary subject (any semantic category) is present in the live scene. */
    SUBJECT_PRESENCE,
    /** Object-layout composition matches the reference (3×3 grid signature). */
    COMPOSITION,
    /** Global visual similarity (embedding cosine) — the universal fallback signal. */
    VISUAL_SIMILARITY,
}

/** Which way the live shot has drifted relative to the reference. */
enum class ReferenceDriftDirection {
    NONE,
    BRIGHTER,
    DARKER,
    WARMER,
    COOLER,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    LARGER,
    SMALLER,
    MISSING,
    /** The scene looks different without a single expressible axis (composition/similarity). */
    DIFFERENT,
    UNKNOWN,
}

/** Per-signal lifecycle state produced by [ReferenceStateMachine] (frame-counted, never timers). */
enum class ReferenceSignalState {
    NORMAL,
    DRIFTING,
    CONFIRMED_DRIFT,
    RECOVERING,
}

/**
 * One signal's comparison outcome for the current frame.
 *
 * [matched]/[delta]/[direction]/[message] are the raw per-frame verdict from
 * [SceneComparator]; [state]/[persistenceFrames]/[recoveryFrames]/[confirmedDrift] are added
 * by [ReferenceStateMachine] — alerts fire only on *confirmed* transitions, never from a single
 * frame.
 */
data class ReferenceSignalResult(
    /** User enabled this signal in [ReferenceMonitorOptions]. */
    val enabled: Boolean = false,
    /** Inputs exist to evaluate it this frame (e.g. subject signals need a face on both sides). */
    val available: Boolean = false,
    /** Within tolerance this frame (raw, pre-debounce). */
    val matched: Boolean = true,
    /** 1.0 = perfect match, 0.0 = far outside tolerance. */
    val score: Float = 1f,
    /** Signed distance from the reference in the signal's own units (see [ReferenceConfig]). */
    val delta: Float = 0f,
    val direction: ReferenceDriftDirection = ReferenceDriftDirection.NONE,
    /** Human-readable drift description, empty while matched. */
    val message: String = "",
    // --- ReferenceStateMachine output ---
    val state: ReferenceSignalState = ReferenceSignalState.NORMAL,
    val persistenceFrames: Int = 0,
    val recoveryFrames: Int = 0,
    /** True while the drift is confirmed (persisted N consecutive frames). */
    val confirmedDrift: Boolean = false,
    /**
     * Phase 12: the Human Perception Layer's verdict for this signal. When present, [matched]
     * (and therefore [confirmedDrift]) IS the perceptual decision — downstream consumers must
     * read this object instead of raw deltas. Null while the perception engine is not wired
     * (tests driving the comparator directly) or the signal was not perceptually evaluable.
     */
    val perception: PerceptualSignal? = null,
    /**
     * Phase 13: the operator instruction translated from [perception] by `InstructionTranslator`.
     * Presentation only — [message] stays the perceptual description; alerts prefer
     * `instruction?.message` for their body. Null while matched or untranslated.
     */
    val instruction: AssistantInstruction? = null,
)

/**
 * Aggregate result of comparing the live feed against the active [ReferenceProfile]. Published as
 * a `StateFlow` by the reference monitor; consumed by `ReferenceAlertRules` and the Shot
 * Reference / Developer UI.
 */
data class ReferenceMatchResult(
    /** True while reference monitoring is running with an analyzed profile. */
    val active: Boolean = false,
    val referenceId: String? = null,
    /** The comparison strategy this result was evaluated under (Phase 10). */
    val strategy: ComparisonStrategy = ComparisonStrategy.HUMAN_STRATEGY,
    /** Mean score of the enabled+available signals (1.0 when none are evaluable). */
    val overallScore: Float = 1f,
    /**
     * Phase 16: true when the winning reference carries a Creative Scene Model, so its creative
     * priorities have been folded into each signal's `perception.importance`. Gates the
     * importance-ordered instruction selection — false ⇒ the fixed strategy priority of today.
     * Transient/derived (never persisted).
     */
    val creativeAware: Boolean = false,

    val exposureMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val subjectPositionMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val subjectSizeMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val whiteBalanceMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val headroomMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val facePresenceMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val eyeVisibilityMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val subjectPresenceMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val compositionMatch: ReferenceSignalResult = ReferenceSignalResult(),
    val visualSimilarityMatch: ReferenceSignalResult = ReferenceSignalResult(),

    val updatedAtMs: Long = 0,
) {
    /** Uniform access for iteration (alert rules, diagnostics UI). */
    fun signal(signal: ReferenceSignal): ReferenceSignalResult = when (signal) {
        ReferenceSignal.EXPOSURE -> exposureMatch
        ReferenceSignal.SUBJECT_POSITION -> subjectPositionMatch
        ReferenceSignal.SUBJECT_SIZE -> subjectSizeMatch
        ReferenceSignal.WHITE_BALANCE -> whiteBalanceMatch
        ReferenceSignal.HEADROOM -> headroomMatch
        ReferenceSignal.FACE_PRESENCE -> facePresenceMatch
        ReferenceSignal.EYE_VISIBILITY -> eyeVisibilityMatch
        ReferenceSignal.SUBJECT_PRESENCE -> subjectPresenceMatch
        ReferenceSignal.COMPOSITION -> compositionMatch
        ReferenceSignal.VISUAL_SIMILARITY -> visualSimilarityMatch
    }

    /** Copy with [signal]'s slot replaced — the state machine enriches signals through this. */
    fun withSignal(signal: ReferenceSignal, result: ReferenceSignalResult): ReferenceMatchResult =
        when (signal) {
            ReferenceSignal.EXPOSURE -> copy(exposureMatch = result)
            ReferenceSignal.SUBJECT_POSITION -> copy(subjectPositionMatch = result)
            ReferenceSignal.SUBJECT_SIZE -> copy(subjectSizeMatch = result)
            ReferenceSignal.WHITE_BALANCE -> copy(whiteBalanceMatch = result)
            ReferenceSignal.HEADROOM -> copy(headroomMatch = result)
            ReferenceSignal.FACE_PRESENCE -> copy(facePresenceMatch = result)
            ReferenceSignal.EYE_VISIBILITY -> copy(eyeVisibilityMatch = result)
            ReferenceSignal.SUBJECT_PRESENCE -> copy(subjectPresenceMatch = result)
            ReferenceSignal.COMPOSITION -> copy(compositionMatch = result)
            ReferenceSignal.VISUAL_SIMILARITY -> copy(visualSimilarityMatch = result)
        }

    /** The strongest currently drifting signal (lowest score among unmatched), for the status UI. */
    fun strongestDrift(): Pair<ReferenceSignal, ReferenceSignalResult>? =
        ReferenceSignal.entries
            .map { it to signal(it) }
            .filter { (_, s) -> s.enabled && s.available && !s.matched }
            .minByOrNull { (_, s) -> s.score }
}
