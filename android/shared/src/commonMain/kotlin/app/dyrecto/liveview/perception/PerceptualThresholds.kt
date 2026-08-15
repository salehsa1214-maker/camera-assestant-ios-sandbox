package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.reference.ai.ComparisonStrategy

/**
 * Central configuration of the Human Perception Layer (Phase 12) — the ONLY place perceptual
 * numbers live. Evaluators and the engine are purely algorithmic and literal-free: every dead
 * zone, saturation point, curve shape, component weight, salience, hysteresis threshold, and
 * history parameter is read from a [Profile] (or [PerceptualTuning] at runtime), so hardware
 * calibration never touches evaluator code.
 *
 * Tolerance profiles carry HUMAN-TOLERANCE semantics, not numeric multipliers: STRICT means
 * "I would notice this in a print" (small dead zones AND a lower hysteresis enter threshold);
 * LOOSE means "only gross drift matters" (wide dead zones, higher enter, earlier saturation).
 *
 * All values are first-pass estimates pending on-device validation (same as Phases 8–10).
 */
object PerceptualThresholds {

    /** Dead zone + saturation for one metric, in the metric's own raw units. */
    data class SignalBand(
        /** |raw| at or below this is visually identical — perceptual difference is exactly 0. */
        val deadZone: Float,
        /** |raw| at or above this is maximally different — perceptual difference is 1. */
        val saturation: Float,
    )

    /** Strategy-aware weights of the exposure composite's metrics (renormalized over available). */
    data class ExposureWeights(
        val mean: Float,
        val median: Float,
        val p95: Float,
        val p99: Float,
        val highlightCoverage: Float,
        val shadowCoverage: Float,
        val histogramDivergence: Float,
        /** The subject-region sub-composite (mean/median/coverage inside the subject box). */
        val subjectExposure: Float,
    )

    /** One tolerance level's complete perceptual parameter set. */
    data class Profile(
        // --- Exposure metric bands (luma codes 0..255 / coverage % / divergence 0..1) ---
        val exposureMean: SignalBand,
        val exposureMedian: SignalBand,
        val exposureP95: SignalBand,
        val exposureP99: SignalBand,
        val highlightCoverage: SignalBand,
        val shadowCoverage: SignalBand,
        /** 1 − histogram intersection between the 32-bin signatures (0..1). */
        val histogramDivergence: SignalBand,
        // --- Single-metric signal bands ---
        val warmth: SignalBand,
        val position: SignalBand,
        val size: SignalBand,
        val headroom: SignalBand,
        val composition: SignalBand,
        // --- Visual similarity buckets (cosine bounds, descending; 5 bounds → 6 buckets) ---
        val similarityCosineBounds: List<Float>,
        /** Noticeability assigned to each [PerceivedMatchBucket], PERFECT..DIFFERENT. */
        val similarityBucketNoticeability: List<Float>,
        // --- Amplitude hysteresis (noticeability units) ---
        val enterNoticeability: Float,
        val leaveNoticeability: Float,
        // --- Confidence ---
        val minConfidence: Float,
        // --- Severity + importance ---
        /** Ascending noticeability cutoffs: NONE|SUBTLE|NOTICEABLE|OBVIOUS|SEVERE. */
        val severityCutoffs: FloatArray,
        /** Noticeability a binary presence signal reports while its subject is missing. */
        val presenceNoticeability: Float,
        /** Per-signal salience for importance (missing subject > equal-noticeability WB drift). */
        val salience: Map<ReferenceSignal, Float>,
        val importanceGamma: Float,
        // --- Per-signal noticeability curves (evaluator-owned choice, config-owned parameters) ---
        val curves: Map<ReferenceSignal, CurveSpec>,
        // --- Direction confidence for inherently ambiguous signals ---
        val compositionDirectionConfidence: Float,
        val similarityDirectionConfidence: Float,
        // --- Perceptual history (engine-owned rolling window; NOT a hysteresis) ---
        val historyWindow: Int,
        /** A sample this far above the rolling median counts as a transient spike. */
        val spikeDeltaThreshold: Float,
        /** Fraction of the spike's excess over the median that survives damping. */
        val spikeDampingFactor: Float,
        /** Half-window mean difference that classifies INCREASING/DECREASING. */
        val trendDelta: Float,
        /** Consecutive-diff sign flips in the window that classify OSCILLATING. */
        val oscillationMinFlips: Int,
        /** Direction confidence multiplier while the trend is OSCILLATING. */
        val oscillationDirectionDecay: Float,
        // --- Geometry dead-zone scaling ---
        /** Per-strategy multiplier on the position/headroom dead zones (default 1). */
        val positionDeadZoneFactors: Map<ComparisonStrategy, Float>,
        // --- Subject-region exposure classification (luma codes) ---
        val subjectHighlightLumaMin: Int,
        val subjectShadowLumaMax: Int,
    )

    /** Bins in the coarse reference histogram signature (persisted on the profile). */
    const val HISTOGRAM_SIGNATURE_BINS = 32

    private val DEFAULT_SEVERITY_CUTOFFS = floatArrayOf(0.15f, 0.35f, 0.60f, 0.85f)

    private val DEFAULT_SALIENCE = mapOf(
        ReferenceSignal.EXPOSURE to 0.9f,
        ReferenceSignal.WHITE_BALANCE to 0.65f,
        ReferenceSignal.SUBJECT_POSITION to 0.85f,
        ReferenceSignal.SUBJECT_SIZE to 0.85f,
        ReferenceSignal.HEADROOM to 0.7f,
        ReferenceSignal.FACE_PRESENCE to 0.95f,
        ReferenceSignal.EYE_VISIBILITY to 0.6f,
        ReferenceSignal.SUBJECT_PRESENCE to 1f,
        ReferenceSignal.COMPOSITION to 0.75f,
        ReferenceSignal.VISUAL_SIMILARITY to 0.8f,
    )

    /**
     * Per-signal curve defaults: exposure is S-shaped (flat near the JND, steep mid-band — a
     * Weber–Fechner approximation); warmth is gentler (humans adapt to white balance); geometry
     * reads near-linear once past the dead zone; composition is S-shaped over the layout delta.
     */
    private val DEFAULT_CURVES = mapOf(
        ReferenceSignal.EXPOSURE to CurveSpec(CurveSpec.CurveType.SMOOTHSTEP),
        ReferenceSignal.WHITE_BALANCE to CurveSpec(CurveSpec.CurveType.POWER, gamma = 1.4f),
        ReferenceSignal.SUBJECT_POSITION to CurveSpec(CurveSpec.CurveType.LINEAR),
        ReferenceSignal.SUBJECT_SIZE to CurveSpec(CurveSpec.CurveType.LINEAR),
        ReferenceSignal.HEADROOM to CurveSpec(CurveSpec.CurveType.LINEAR),
        ReferenceSignal.COMPOSITION to CurveSpec(CurveSpec.CurveType.SMOOTHSTEP),
    )

    private val DEFAULT_POSITION_FACTORS = mapOf(
        ComparisonStrategy.PRODUCT_STRATEGY to 0.8f, // studio framing is deliberate — tighter
        ComparisonStrategy.ANIMAL_STRATEGY to 1.2f,  // animals move — looser
    )

    /** Bucket noticeability shared by every tolerance: PERFECT..DIFFERENT. */
    private val SIMILARITY_BUCKET_NOTICEABILITY =
        listOf(0f, 0.05f, 0.12f, 0.30f, 0.60f, 0.90f)

    val STRICT = Profile(
        exposureMean = SignalBand(deadZone = 6f, saturation = 30f),
        exposureMedian = SignalBand(deadZone = 6f, saturation = 30f),
        exposureP95 = SignalBand(deadZone = 8f, saturation = 40f),
        exposureP99 = SignalBand(deadZone = 10f, saturation = 45f),
        highlightCoverage = SignalBand(deadZone = 2f, saturation = 15f),
        shadowCoverage = SignalBand(deadZone = 2f, saturation = 15f),
        histogramDivergence = SignalBand(deadZone = 0.10f, saturation = 0.50f),
        warmth = SignalBand(deadZone = 6f, saturation = 30f),
        position = SignalBand(deadZone = 0.03f, saturation = 0.18f),
        size = SignalBand(deadZone = 0.06f, saturation = 0.35f),
        headroom = SignalBand(deadZone = 0.03f, saturation = 0.15f),
        composition = SignalBand(deadZone = 0.04f, saturation = 0.25f),
        similarityCosineBounds = listOf(0.99f, 0.97f, 0.94f, 0.88f, 0.75f),
        similarityBucketNoticeability = SIMILARITY_BUCKET_NOTICEABILITY,
        enterNoticeability = 0.15f,
        leaveNoticeability = 0.07f,
        minConfidence = 0.25f,
        severityCutoffs = DEFAULT_SEVERITY_CUTOFFS,
        presenceNoticeability = 0.85f,
        salience = DEFAULT_SALIENCE,
        importanceGamma = 0.8f,
        curves = DEFAULT_CURVES,
        compositionDirectionConfidence = 0.4f,
        similarityDirectionConfidence = 0.3f,
        historyWindow = 10,
        spikeDeltaThreshold = 0.25f,
        // Low enough that even a full-scale transient (0→~1) stays under every enter threshold;
        // sustained changes still catch up because the history window keeps RAW samples.
        spikeDampingFactor = 0.15f,
        trendDelta = 0.08f,
        oscillationMinFlips = 4,
        oscillationDirectionDecay = 0.5f,
        positionDeadZoneFactors = DEFAULT_POSITION_FACTORS,
        subjectHighlightLumaMin = 250,
        subjectShadowLumaMax = 16,
    )

    val MEDIUM = STRICT.copy(
        exposureMean = SignalBand(deadZone = 10f, saturation = 40f),
        exposureMedian = SignalBand(deadZone = 10f, saturation = 40f),
        exposureP95 = SignalBand(deadZone = 12f, saturation = 50f),
        exposureP99 = SignalBand(deadZone = 14f, saturation = 55f),
        highlightCoverage = SignalBand(deadZone = 3f, saturation = 20f),
        shadowCoverage = SignalBand(deadZone = 3f, saturation = 20f),
        histogramDivergence = SignalBand(deadZone = 0.15f, saturation = 0.60f),
        warmth = SignalBand(deadZone = 10f, saturation = 40f),
        position = SignalBand(deadZone = 0.05f, saturation = 0.25f),
        size = SignalBand(deadZone = 0.10f, saturation = 0.45f),
        headroom = SignalBand(deadZone = 0.05f, saturation = 0.20f),
        composition = SignalBand(deadZone = 0.06f, saturation = 0.30f),
        similarityCosineBounds = listOf(0.99f, 0.96f, 0.92f, 0.85f, 0.70f),
        enterNoticeability = 0.20f,
        leaveNoticeability = 0.10f,
    )

    val LOOSE = STRICT.copy(
        exposureMean = SignalBand(deadZone = 16f, saturation = 55f),
        exposureMedian = SignalBand(deadZone = 16f, saturation = 55f),
        exposureP95 = SignalBand(deadZone = 18f, saturation = 65f),
        exposureP99 = SignalBand(deadZone = 20f, saturation = 70f),
        highlightCoverage = SignalBand(deadZone = 5f, saturation = 30f),
        shadowCoverage = SignalBand(deadZone = 5f, saturation = 30f),
        histogramDivergence = SignalBand(deadZone = 0.22f, saturation = 0.70f),
        warmth = SignalBand(deadZone = 16f, saturation = 55f),
        position = SignalBand(deadZone = 0.08f, saturation = 0.32f),
        size = SignalBand(deadZone = 0.16f, saturation = 0.60f),
        headroom = SignalBand(deadZone = 0.08f, saturation = 0.28f),
        composition = SignalBand(deadZone = 0.10f, saturation = 0.40f),
        similarityCosineBounds = listOf(0.985f, 0.95f, 0.90f, 0.80f, 0.62f),
        enterNoticeability = 0.30f,
        leaveNoticeability = 0.15f,
    )

    fun profileFor(tolerance: ReferenceTolerance): Profile = when (tolerance) {
        ReferenceTolerance.STRICT -> STRICT
        ReferenceTolerance.MEDIUM -> MEDIUM
        ReferenceTolerance.LOOSE -> LOOSE
    }

    /**
     * Exposure composite weights per strategy: a person's exposure lives in the midtones and on
     * the subject; skies dominate landscapes/architecture (p95/highlights); products/vehicles are
     * judged by body brightness and shadow detail.
     */
    val DEFAULT_EXPOSURE_WEIGHTS: Map<ComparisonStrategy, ExposureWeights> = buildMap {
        val humanLike = ExposureWeights(
            mean = 0.8f, median = 1f, p95 = 0.4f, p99 = 0.2f,
            highlightCoverage = 0.4f, shadowCoverage = 0.4f,
            histogramDivergence = 0.5f, subjectExposure = 1.2f,
        )
        put(ComparisonStrategy.HUMAN_STRATEGY, humanLike)
        put(ComparisonStrategy.ANIMAL_STRATEGY, humanLike)
        val objectLike = ExposureWeights(
            mean = 1f, median = 0.8f, p95 = 0.5f, p99 = 0.3f,
            highlightCoverage = 0.5f, shadowCoverage = 0.8f,
            histogramDivergence = 0.5f, subjectExposure = 1.2f,
        )
        put(ComparisonStrategy.VEHICLE_STRATEGY, objectLike)
        put(ComparisonStrategy.PRODUCT_STRATEGY, objectLike)
        val frameWide = ExposureWeights(
            mean = 1f, median = 0.8f, p95 = 1f, p99 = 0.5f,
            highlightCoverage = 1f, shadowCoverage = 0.6f,
            histogramDivergence = 0.8f, subjectExposure = 0f,
        )
        put(ComparisonStrategy.ARCHITECTURE_STRATEGY, frameWide)
        put(ComparisonStrategy.LANDSCAPE_STRATEGY, frameWide)
        put(ComparisonStrategy.GENERIC_SCENE_STRATEGY, frameWide)
        put(
            ComparisonStrategy.MULTI_OBJECT_STRATEGY,
            ExposureWeights(
                mean = 1f, median = 0.9f, p95 = 0.6f, p99 = 0.3f,
                highlightCoverage = 0.6f, shadowCoverage = 0.6f,
                histogramDivergence = 0.7f, subjectExposure = 0f,
            ),
        )
    }
}

/**
 * The live perceptual parameter set the engine reads each frame. Defaults are derived from the
 * profile's tolerance; the Developer tuning panel replaces this holder at runtime (in-memory,
 * never persisted) so on-device calibration requires no rebuild. Compiled defaults in
 * [PerceptualThresholds] stay the single source of truth for shipped values.
 */
data class PerceptualTuning(
    val profile: PerceptualThresholds.Profile,
    val strategyWeights: Map<ComparisonStrategy, Map<ReferenceSignal, Float>> =
        PerceptualApplicability.DEFAULT_WEIGHTS,
    val exposureWeights: Map<ComparisonStrategy, PerceptualThresholds.ExposureWeights> =
        PerceptualThresholds.DEFAULT_EXPOSURE_WEIGHTS,
) {
    companion object {
        fun forTolerance(tolerance: ReferenceTolerance): PerceptualTuning =
            PerceptualTuning(PerceptualThresholds.profileFor(tolerance))
    }
}
