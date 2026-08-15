package app.dyrecto.liveview.perception

import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.perception.PerceptualThresholds.SignalBand
import app.dyrecto.liveview.reference.CurrentReferenceInput
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalResult
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import kotlin.math.abs

/**
 * Everything one evaluator may look at for one frame. [raw] is the comparator's verdict for this
 * signal; [reference]/[input] allow multi-metric evaluators (exposure) to read richer inputs than
 * the comparator's single delta. All tunables come from [tuning] — evaluators are literal-free.
 */
data class PerceptualContext(
    val signal: ReferenceSignal,
    val raw: ReferenceSignalResult,
    val reference: ReferenceProfile,
    val input: CurrentReferenceInput,
    /** Live subject-region exposure stats (Phase 12 scan output), if a region is tracked. */
    val liveSubjectExposure: SubjectExposureStats?,
    val strategy: ComparisonStrategy,
    val tuning: PerceptualTuning,
)

/**
 * Pre-hysteresis perceptual measurement of one signal for one frame. The engine layers history,
 * trend, damping, hysteresis, severity, and importance on top — evaluators only measure.
 */
data class PerceptualObservation(
    val available: Boolean,
    val rawDifference: Float = 0f,
    val perceptualDifference: Float = 0f,
    /** 0..1 after this evaluator's own curve — "can the operator see it?" */
    val noticeability: Float = 0f,
    /** Confidence that a difference exists. */
    val confidence: Float = 0f,
    /** Confidence in the corrective direction (a certainty of logic, or a configured value). */
    val directionConfidence: Float = 0f,
    val deadZoneApplied: Float = 0f,
    val direction: ReferenceDriftDirection = ReferenceDriftDirection.NONE,
    val matchBucket: PerceivedMatchBucket? = null,
    val components: Map<String, Float> = emptyMap(),
) {
    companion object {
        val UNAVAILABLE = PerceptualObservation(available = false)
    }
}

/**
 * One signal's perceptual measurement (Phase 12).
 *
 * HARD INVARIANT — evaluators are PURE FUNCTIONS of [PerceptualContext]: no hysteresis state, no
 * rolling history, no caches, no previous-frame knowledge, no mutable fields (all
 * implementations are `object`s). Same input ⇒ same output. ALL state lives exclusively in
 * `HumanPerceptionEngine`. This keeps evaluators deterministic, trivially testable, reusable in
 * offline analysis, and tunable purely through [PerceptualThresholds]/[PerceptualTuning].
 *
 * Adding a future signal (focus, motion blur, pose, horizon…) = one new evaluator + a threshold
 * band + a registry entry — zero engine changes.
 */
interface PerceptualEvaluator {
    val signal: ReferenceSignal
    fun evaluate(ctx: PerceptualContext): PerceptualObservation
}

/** The default evaluator per signal (presence signals share one implementation). */
fun defaultPerceptualEvaluators(): Map<ReferenceSignal, PerceptualEvaluator> = mapOf(
    ReferenceSignal.EXPOSURE to ExposureEvaluator,
    ReferenceSignal.WHITE_BALANCE to WhiteBalanceEvaluator,
    ReferenceSignal.SUBJECT_POSITION to PositionEvaluator,
    ReferenceSignal.SUBJECT_SIZE to SizeEvaluator,
    ReferenceSignal.HEADROOM to HeadroomEvaluator,
    ReferenceSignal.FACE_PRESENCE to PresenceEvaluator(ReferenceSignal.FACE_PRESENCE),
    ReferenceSignal.EYE_VISIBILITY to PresenceEvaluator(ReferenceSignal.EYE_VISIBILITY),
    ReferenceSignal.SUBJECT_PRESENCE to PresenceEvaluator(ReferenceSignal.SUBJECT_PRESENCE),
    ReferenceSignal.COMPOSITION to CompositionEvaluator,
    ReferenceSignal.VISUAL_SIMILARITY to SimilarityEvaluator,
)

// ---------------------------------------------------------------------------------------------
// Exposure — multi-metric composite, strategy-weighted
// ---------------------------------------------------------------------------------------------

/**
 * Combines mean/median/p95/p99, highlight/shadow coverage, histogram divergence, and the
 * subject-region sub-composite into ONE perceptual exposure score. The same numeric deltas score
 * differently per strategy through [PerceptualThresholds.ExposureWeights] (a person is judged by
 * midtones + subject; a landscape by frame-wide highlights). Missing metrics (legacy profile
 * without a signature, no subject region) drop out via weight renormalization.
 */
object ExposureEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.EXPOSURE

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        val histogram = ctx.input.histogram ?: return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        val ref = ctx.reference.exposure
        val weights = ctx.tuning.exposureWeights[ctx.strategy]
            ?: ctx.tuning.exposureWeights[ComparisonStrategy.GENERIC_SCENE_STRATEGY]
            ?: return PerceptualObservation.UNAVAILABLE

        val meanDelta = histogram.mean - ref.mean
        val medianDelta = histogram.median - ref.median
        val p95Delta = histogram.percentile95 - ref.p95
        val p99Delta = histogram.percentile99 - ref.p99
        val highlightDelta = (ctx.input.zebra?.coveragePercentage ?: Float.NaN) - ref.highlightCoverage
        val shadowDelta = histogram.clippedShadowPercentage - ref.shadowCoverage

        val divergence = if (ref.histogramSignature.isNotEmpty()) {
            HistogramSignature.divergence(
                ref.histogramSignature,
                HistogramSignature.fromBins(histogram.bins, ref.histogramSignature.size),
            )
        } else {
            null
        }

        val subjectComposite = subjectComposite(ref.subjectExposure, ctx.liveSubjectExposure, p)

        val meanP = PerceptualDifference.normalize(meanDelta, p.exposureMean.deadZone, p.exposureMean.saturation)
        val medianP = PerceptualDifference.normalize(medianDelta, p.exposureMedian.deadZone, p.exposureMedian.saturation)
        val p95P = PerceptualDifference.normalize(p95Delta, p.exposureP95.deadZone, p.exposureP95.saturation)
        val p99P = PerceptualDifference.normalize(p99Delta, p.exposureP99.deadZone, p.exposureP99.saturation)
        val highlightP = if (highlightDelta.isNaN()) null else {
            PerceptualDifference.normalize(highlightDelta, p.highlightCoverage.deadZone, p.highlightCoverage.saturation)
        }
        val shadowP = PerceptualDifference.normalize(shadowDelta, p.shadowCoverage.deadZone, p.shadowCoverage.saturation)
        val divergenceP = divergence?.let {
            PerceptualDifference.normalize(it, p.histogramDivergence.deadZone, p.histogramDivergence.saturation)
        }

        val composite = PerceptualDifference.weightedComposite(
            listOf(
                meanP as Float? to weights.mean,
                medianP as Float? to weights.median,
                p95P as Float? to weights.p95,
                p99P as Float? to weights.p99,
                highlightP to weights.highlightCoverage,
                shadowP as Float? to weights.shadowCoverage,
                divergenceP to weights.histogramDivergence,
                subjectComposite?.perceptual to weights.subjectExposure,
            ),
        )

        // Subject-priority floor: when the strategy weights the subject (HUMAN/PRODUCT/VEHICLE),
        // a drifted subject must not be averaged away by a matching background — the operator is
        // looking AT the subject. The floor scales with the configured subject weight (0 = off).
        val subjectFloor = subjectComposite?.perceptual
            ?.times(weights.subjectExposure.coerceIn(0f, 1f)) ?: 0f
        val perceptual = maxOf(composite, subjectFloor)

        val curve = p.curves[signal] ?: CurveSpec(CurveSpec.CurveType.LINEAR)
        val noticeability = PerceptualDifference.applyCurve(perceptual, curve)

        // Direction from the luma-carrying metrics: perceptual magnitude signed by each delta.
        // Agreement of the components IS the direction confidence (all brighter → 1; split → low).
        var signedSum = 0f
        var absSum = 0f
        fun vote(perceptual: Float?, delta: Float, weight: Float) {
            if (perceptual == null || weight <= 0f || perceptual <= 0f) return
            val v = perceptual * weight
            signedSum += if (delta >= 0f) v else -v
            absSum += v
        }
        vote(meanP, meanDelta, weights.mean)
        vote(medianP, medianDelta.toFloat(), weights.median)
        vote(p95P, p95Delta, weights.p95)
        vote(p99P, p99Delta, weights.p99)
        vote(subjectComposite?.perceptual, subjectComposite?.signedMeanDelta ?: 0f, weights.subjectExposure)

        val direction = when {
            noticeability <= 0f || absSum <= 0f -> ReferenceDriftDirection.NONE
            signedSum >= 0f -> ReferenceDriftDirection.BRIGHTER
            else -> ReferenceDriftDirection.DARKER
        }
        val directionConfidence = if (absSum <= 0f) 0f else abs(signedSum) / absSum

        val components = buildMap {
            put("mean", meanP)
            put("median", medianP)
            put("p95", p95P)
            put("p99", p99P)
            highlightP?.let { put("highlightCov", it) }
            put("shadowCov", shadowP)
            divergenceP?.let { put("histDivergence", it) }
            subjectComposite?.let { put("subject", it.perceptual) }
        }

        return PerceptualObservation(
            available = true,
            rawDifference = meanDelta,
            perceptualDifference = perceptual,
            noticeability = noticeability,
            confidence = 1f, // frame statistics carry no per-feature confidence
            directionConfidence = directionConfidence,
            deadZoneApplied = p.exposureMean.deadZone,
            direction = direction,
            components = components,
        )
    }

    private class SubjectComposite(val perceptual: Float, val signedMeanDelta: Float)

    /** Subject sub-composite over the same metric family, using the frame bands. */
    private fun subjectComposite(
        ref: SubjectExposureStats?,
        live: SubjectExposureStats?,
        p: PerceptualThresholds.Profile,
    ): SubjectComposite? {
        if (ref == null || live == null) return null
        val meanDelta = live.mean - ref.mean
        val composite = PerceptualDifference.weightedComposite(
            listOf(
                PerceptualDifference.normalize(meanDelta, p.exposureMean.deadZone, p.exposureMean.saturation) as Float? to 1f,
                PerceptualDifference.normalize(live.median - ref.median, p.exposureMedian.deadZone, p.exposureMedian.saturation) as Float? to 1f,
                PerceptualDifference.normalize(live.highlightCoverage - ref.highlightCoverage, p.highlightCoverage.deadZone, p.highlightCoverage.saturation) as Float? to 1f,
                PerceptualDifference.normalize(live.shadowCoverage - ref.shadowCoverage, p.shadowCoverage.deadZone, p.shadowCoverage.saturation) as Float? to 1f,
            ),
        )
        return SubjectComposite(composite, meanDelta)
    }
}

// ---------------------------------------------------------------------------------------------
// White balance
// ---------------------------------------------------------------------------------------------

/** Warmth/tint drift through a gentle curve — humans adapt to small color shifts. */
object WhiteBalanceEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.WHITE_BALANCE

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        return singleBandObservation(
            raw = ctx.raw.delta,
            band = p.warmth,
            curve = p.curves[signal],
            direction = ctx.raw.direction,
            // The warm/cool sign is unambiguous once outside the dead zone; a tint-only drift
            // (UNKNOWN) has no single corrective axis.
            directionConfidence = if (ctx.raw.direction == ReferenceDriftDirection.UNKNOWN) {
                p.compositionDirectionConfidence
            } else {
                1f
            },
            confidence = 1f,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Geometry — position / size / headroom
// ---------------------------------------------------------------------------------------------

/**
 * Subject position: the dead zone scales with the strategy and with the subject's size (a face
 * filling half the frame tolerates more absolute center drift than a distant one). Geometry is
 * normalized 0..1, so image-size independence is inherent.
 */
object PositionEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.SUBJECT_POSITION

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        val scale = geometryDeadZoneScale(ctx)
        return singleBandObservation(
            raw = ctx.raw.delta,
            band = SignalBand(p.position.deadZone * scale, p.position.saturation * scale),
            curve = p.curves[signal],
            direction = ctx.raw.direction,
            directionConfidence = 1f, // dominant-axis sign is unambiguous
            confidence = subjectConfidence(ctx),
        )
    }
}

/** Subject size (relative linear change). */
object SizeEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.SUBJECT_SIZE

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        return singleBandObservation(
            raw = ctx.raw.delta,
            band = p.size,
            curve = p.curves[signal],
            direction = ctx.raw.direction,
            directionConfidence = 1f,
            confidence = subjectConfidence(ctx),
        )
    }
}

/** Headroom (face-top drift) — same scaled dead zone family as position. */
object HeadroomEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.HEADROOM

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        val scale = geometryDeadZoneScale(ctx)
        return singleBandObservation(
            raw = ctx.raw.delta,
            band = SignalBand(p.headroom.deadZone * scale, p.headroom.saturation * scale),
            curve = p.curves[signal],
            direction = ctx.raw.direction,
            directionConfidence = 1f,
            confidence = 1f, // ML Kit face path carries no per-feature confidence
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Composition / visual similarity
// ---------------------------------------------------------------------------------------------

/** Whole-layout drift; multiple corrections could fix it, so direction confidence is low. */
object CompositionEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.COMPOSITION

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        return singleBandObservation(
            raw = ctx.raw.delta,
            band = p.composition,
            curve = p.curves[signal],
            direction = if (ctx.raw.delta == 0f) ReferenceDriftDirection.NONE else ReferenceDriftDirection.DIFFERENT,
            directionConfidence = p.compositionDirectionConfidence,
            confidence = ctx.input.sceneSnapshot?.layoutSignature?.confidence ?: 1f,
        )
    }
}

/**
 * Visual similarity: the raw cosine is converted to a [PerceivedMatchBucket] and NEVER exposed
 * downstream — reasoning consumes the bucket's configured noticeability.
 */
object SimilarityEvaluator : PerceptualEvaluator {
    override val signal = ReferenceSignal.VISUAL_SIMILARITY

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        val cosine = 1f - ctx.raw.delta
        val bounds = p.similarityCosineBounds
        val bucketIndex = bounds.indexOfFirst { cosine >= it }.let { if (it < 0) bounds.size else it }
        val bucket = PerceivedMatchBucket.entries[bucketIndex.coerceAtMost(PerceivedMatchBucket.entries.size - 1)]
        val noticeability = p.similarityBucketNoticeability.getOrElse(bucketIndex) { 1f }.coerceIn(0f, 1f)

        return PerceptualObservation(
            available = true,
            rawDifference = ctx.raw.delta,
            perceptualDifference = noticeability,
            noticeability = noticeability,
            confidence = ctx.input.sceneSnapshot?.embedding?.confidence ?: 1f,
            directionConfidence = p.similarityDirectionConfidence,
            deadZoneApplied = 1f - bounds.getOrElse(1) { 1f }, // deltas under the IDENTICAL bound read as identical
            direction = if (noticeability <= 0f) ReferenceDriftDirection.NONE else ReferenceDriftDirection.DIFFERENT,
            matchBucket = bucket,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Presence (binary) — face / eyes / subject
// ---------------------------------------------------------------------------------------------

/** Binary presence: present = noticeability 0; missing = the configured presence noticeability. */
class PresenceEvaluator(override val signal: ReferenceSignal) : PerceptualEvaluator {

    override fun evaluate(ctx: PerceptualContext): PerceptualObservation {
        if (!ctx.raw.available) return PerceptualObservation.UNAVAILABLE
        val p = ctx.tuning.profile
        val missing = !ctx.raw.matched
        val noticeability = if (missing) p.presenceNoticeability else 0f
        return PerceptualObservation(
            available = true,
            rawDifference = ctx.raw.delta,
            perceptualDifference = noticeability,
            noticeability = noticeability,
            confidence = 1f, // the comparator already gated shaky evidence to unavailable
            directionConfidence = 1f, // "bring the subject back" is unambiguous
            deadZoneApplied = 0f,
            direction = if (missing) ReferenceDriftDirection.MISSING else ReferenceDriftDirection.NONE,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Shared helpers (pure)
// ---------------------------------------------------------------------------------------------

/** Single-band → normalize → curve → observation, for the one-delta signals. */
private fun singleBandObservation(
    raw: Float,
    band: SignalBand,
    curve: CurveSpec?,
    direction: ReferenceDriftDirection,
    directionConfidence: Float,
    confidence: Float,
): PerceptualObservation {
    val perceptual = PerceptualDifference.normalize(raw, band.deadZone, band.saturation)
    val noticeability = PerceptualDifference.applyCurve(
        perceptual,
        curve ?: CurveSpec(CurveSpec.CurveType.LINEAR),
    )
    return PerceptualObservation(
        available = true,
        rawDifference = raw,
        perceptualDifference = perceptual,
        noticeability = noticeability,
        confidence = confidence,
        directionConfidence = if (noticeability <= 0f) 0f else directionConfidence,
        deadZoneApplied = band.deadZone,
        direction = if (perceptual <= 0f) ReferenceDriftDirection.NONE else direction,
    )
}

/** Strategy factor × (1 + subject normalized width): bigger subjects tolerate more drift. */
private fun geometryDeadZoneScale(ctx: PerceptualContext): Float {
    val factor = ctx.tuning.profile.positionDeadZoneFactors[ctx.strategy] ?: 1f
    val subjectWidth = ctx.reference.ai?.primarySubjectBox?.width
        ?: ctx.reference.subject?.normalizedWidth
        ?: 0f
    return factor * (1f + subjectWidth.coerceIn(0f, 1f))
}

/**
 * Live tracked-subject confidence for geometry signals. Phase 9 face path carries no
 * per-feature confidence (reads as solid); on the AI path the best available proxy without
 * re-running the matcher is the snapshot's strongest subject confidence.
 */
private fun subjectConfidence(ctx: PerceptualContext): Float {
    if (ctx.reference.ai?.primarySubjectBox == null) return 1f
    val snapshot = ctx.input.sceneSnapshot ?: return 1f
    return snapshot.subjects.maxOfOrNull { it.confidence } ?: 1f
}
