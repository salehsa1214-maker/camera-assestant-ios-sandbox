package app.dyrecto.liveview.reference

import app.dyrecto.liveview.reference.ReferenceConfig.ReferenceThresholds
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.CompositionAnalyzer
import app.dyrecto.liveview.reference.ai.EmbeddingMath
import app.dyrecto.liveview.reference.ai.SubjectMatcher
import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.ZebraResult
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Wraps the *already-published* live analysis outputs the comparator consumes. The comparator
 * never rescans the Bitmap — every field here is produced by the existing Vision pipeline for
 * the current frame. Phase 10 adds the AI [sceneSnapshot] (adaptive cadence — may be older than
 * the frame; staleness is handled here) and the stored [referenceEmbedding] (the profile itself
 * never owns the raw vector).
 */
data class CurrentReferenceInput(
    val exposure: ExposureResult? = null,
    val histogram: HistogramResult? = null,
    val zebra: ZebraResult? = null,
    val faces: FaceDetectionResult? = null,
    val eyes: EyeDetectionResult? = null,
    val colorStats: ColorStatsResult? = null,
    val scene: SceneContext? = null,
    val sceneSnapshot: SceneSnapshot? = null,
    val referenceEmbedding: ReferenceEmbedding? = null,
)

/**
 * Pure, stateless per-frame comparison of two scene states: the pinned reference
 * ([ReferenceProfile] + its stored embedding) against the live analysis outputs. Produces the
 * raw [ReferenceMatchResult]; debouncing (persistence/recovery) is layered on top by
 * [ReferenceStateMachine], and alerting by `ReferenceAlertRules` — this object only answers
 * "how far is this single frame from the reference, and in which direction?".
 *
 * Phase 10 semantics:
 *  - the profile's [ComparisonStrategy] (via [ReferenceSignalApplicability]) decides WHICH
 *    signals are evaluated — non-applicable signals report disabled;
 *  - subject position/size come from the tracked/detected subject box (any semantic category),
 *    falling back to the Phase 9 face box when the AI snapshot is unavailable or stale;
 *  - human-only extras (headroom/face/eyes) run only under HUMAN strategy (or legacy profiles);
 *  - AI features carry per-feature confidence: below [ReferenceThresholds.minFeatureConfidence]
 *    the signal is unavailable (the state machine holds — no false drift), above it the score is
 *    blended toward neutral so shaky features rank below solid ones;
 *  - VISUAL_SIMILARITY (embedding cosine) is the universal fallback that works for any scene.
 *
 * Thresholds come from [ReferenceConfig] via the profile's [ReferenceTolerance] — never inline.
 */
object SceneComparator {

    fun compare(
        reference: ReferenceProfile,
        current: CurrentReferenceInput,
        nowMs: Long,
    ): ReferenceMatchResult {
        val thresholds = ReferenceConfig.thresholdsFor(reference.options.tolerance)
        val options = reference.options
        val ai = reference.ai
        val strategy = ai?.strategy ?: ComparisonStrategy.HUMAN_STRATEGY
        val applicable = ReferenceSignalApplicability.signalsFor(strategy)

        // Stale snapshots are no evidence: every AI signal goes unavailable and the pre-AI
        // fallbacks (face box) take over where they can.
        val snapshot = current.sceneSnapshot?.takeIf {
            nowMs - it.analyzedAtMs <= ReferenceConfig.AI_STALE_AFTER_MS
        }

        // Largest live face (normalized), mirroring the Phase 9 reference analyzer.
        val liveFaceBox = largestFaceNormalized(current.faces)
        val liveFacePresent = (current.faces?.facesDetected ?: 0) > 0
        val liveEyesPresent = (current.eyes?.eyesDetected ?: 0) > 0

        // One weighted match of the reference's primary subject against the live subjects; its
        // outcome feeds SUBJECT_PRESENCE and provides the box for POSITION/SIZE.
        val expected = ai?.primarySubjectBox?.let { box ->
            SubjectMatcher.Expected(
                category = ai.primarySubjectCategory,
                boundingBox = box,
                rawLabel = ai.primarySubjectRawLabel,
            )
        }
        val matchedSubject = if (expected != null && snapshot != null) {
            SubjectMatcher.match(expected, snapshot.subjects)?.subject
        } else {
            null
        }

        val subjectPresence = compareSubjectPresence(
            ai = ai,
            strategy = strategy,
            snapshot = snapshot,
            expected = expected,
            matchedSubject = matchedSubject,
            enabled = options.monitorSubjectPresence && ReferenceSignal.SUBJECT_PRESENCE in applicable,
        )

        // Geometry pair for POSITION/SIZE: reference geometry and live box must come from the
        // SAME kind of source (AI subject box ↔ AI subject box, or face ↔ face) — mixing them
        // would compare a person box against a face box.
        val aiGeometryActive = expected != null && snapshot != null
        val refSubjectGeometry: ReferenceSubjectProfile?
        val liveSubjectBox: NormalizedRect?
        val subjectConfidence: Float
        if (aiGeometryActive) {
            refSubjectGeometry = ai?.primarySubjectBox?.let {
                ReferenceSubjectProfile(it.centerX, it.centerY, it.width, it.height, it.area)
            }
            liveSubjectBox = matchedSubject?.boundingBox
            subjectConfidence = matchedSubject?.confidence ?: 0f
        } else {
            refSubjectGeometry = reference.subject
            liveSubjectBox = liveFaceBox
            subjectConfidence = 1f // ML Kit path has no per-feature confidence — Phase 9 behavior
        }

        val humanExtras = strategy == ComparisonStrategy.HUMAN_STRATEGY || ai == null
        // While the subject itself is confirmed missing, the face signals go unavailable so a
        // missing person fires once (SUBJECT_PRESENCE), not twice.
        val subjectGone = subjectPresence.available && !subjectPresence.matched

        val result = ReferenceMatchResult(
            active = true,
            referenceId = reference.id,
            strategy = strategy,
            exposureMatch = compareExposure(reference, current.histogram, options.monitorExposure, thresholds),
            whiteBalanceMatch = compareWhiteBalance(reference, current.colorStats, options.monitorWhiteBalance, thresholds),
            subjectPositionMatch = comparePosition(
                refSubjectGeometry, liveSubjectBox, subjectConfidence,
                enabled = options.monitorSubjectPosition && ReferenceSignal.SUBJECT_POSITION in applicable,
                thresholds = thresholds,
            ),
            subjectSizeMatch = compareSize(
                refSubjectGeometry, liveSubjectBox, subjectConfidence,
                enabled = options.monitorSubjectSize && ReferenceSignal.SUBJECT_SIZE in applicable,
                thresholds = thresholds,
            ),
            headroomMatch = compareHeadroom(
                reference.subject, liveFaceBox,
                enabled = humanExtras && options.monitorHeadroom && ReferenceSignal.HEADROOM in applicable,
                suppressed = subjectGone,
                thresholds = thresholds,
            ),
            facePresenceMatch = compareFacePresence(
                reference.face, current.faces, liveFacePresent,
                enabled = humanExtras && options.monitorFacePresence && ReferenceSignal.FACE_PRESENCE in applicable,
                suppressed = subjectGone,
            ),
            eyeVisibilityMatch = compareEyeVisibility(
                reference.face, current.eyes, liveFacePresent, liveEyesPresent,
                enabled = humanExtras && options.monitorEyeVisibility && ReferenceSignal.EYE_VISIBILITY in applicable,
                suppressed = subjectGone,
            ),
            subjectPresenceMatch = subjectPresence,
            compositionMatch = compareComposition(
                ai, snapshot,
                enabled = options.monitorComposition && ReferenceSignal.COMPOSITION in applicable,
                thresholds = thresholds,
            ),
            visualSimilarityMatch = compareVisualSimilarity(
                current.referenceEmbedding, snapshot,
                enabled = options.monitorVisualSimilarity && ReferenceSignal.VISUAL_SIMILARITY in applicable,
                thresholds = thresholds,
            ),
            updatedAtMs = nowMs,
        )
        return result.copy(overallScore = overallScore(result))
    }

    // ---- Exposure ----

    private fun compareExposure(
        reference: ReferenceProfile,
        histogram: HistogramResult?,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (histogram == null) return unavailable()

        val meanDelta = histogram.mean - reference.exposure.mean
        val p95Delta = histogram.percentile95 - reference.exposure.p95
        val meanDrift = abs(meanDelta) > thresholds.exposureMeanDelta
        val p95Drift = abs(p95Delta) > thresholds.exposureP95Delta
        val matched = !meanDrift && !p95Drift

        // The dominant delta (relative to its own threshold) decides direction + reporting.
        val p95Dominates = abs(p95Delta) / thresholds.exposureP95Delta >
            abs(meanDelta) / thresholds.exposureMeanDelta
        val dominant = if (p95Dominates) p95Delta else meanDelta
        val threshold = if (p95Dominates) thresholds.exposureP95Delta else thresholds.exposureMeanDelta

        val direction = when {
            matched -> ReferenceDriftDirection.NONE
            dominant > 0 -> ReferenceDriftDirection.BRIGHTER
            else -> ReferenceDriftDirection.DARKER
        }
        val message = when (direction) {
            ReferenceDriftDirection.BRIGHTER -> "Exposure is brighter than the reference."
            ReferenceDriftDirection.DARKER -> "Exposure is darker than the reference."
            else -> ""
        }
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = score(dominant, threshold),
            delta = dominant,
            direction = direction,
            message = message,
        )
    }

    // ---- White balance ----

    private fun compareWhiteBalance(
        reference: ReferenceProfile,
        colorStats: ColorStatsResult?,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (colorStats == null || colorStats.sampleCount == 0) return unavailable()

        val warmthDelta = colorStats.warmthScore - reference.color.warmthScore
        val tintDelta = colorStats.tintScore - reference.color.tintScore
        val warmthDrift = abs(warmthDelta) > thresholds.warmthDelta
        val tintDrift = abs(tintDelta) > thresholds.warmthDelta
        val matched = !warmthDrift && !tintDrift

        val direction = when {
            matched -> ReferenceDriftDirection.NONE
            warmthDrift && warmthDelta > 0 -> ReferenceDriftDirection.WARMER
            warmthDrift -> ReferenceDriftDirection.COOLER
            else -> ReferenceDriftDirection.UNKNOWN // tint-only drift has no warm/cool expression
        }
        val message = when (direction) {
            ReferenceDriftDirection.WARMER -> "Image is warmer than the reference."
            ReferenceDriftDirection.COOLER -> "Image is cooler than the reference."
            ReferenceDriftDirection.UNKNOWN -> "Color tint differs from the reference."
            else -> ""
        }
        val dominant = if (abs(warmthDelta) >= abs(tintDelta)) warmthDelta else tintDelta
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = score(dominant, thresholds.warmthDelta),
            delta = dominant,
            direction = direction,
            message = message,
        )
    }

    // ---- Subject position ----

    private fun comparePosition(
        refSubject: ReferenceSubjectProfile?,
        liveSubject: NormalizedRect?,
        confidence: Float,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (refSubject == null || liveSubject == null) return unavailable()
        if (confidence < thresholds.minFeatureConfidence) return unavailable()

        val dx = liveSubject.centerX - refSubject.normalizedCenterX
        val dy = liveSubject.centerY - refSubject.normalizedCenterY
        val matched = abs(dx) <= thresholds.subjectPositionDelta &&
            abs(dy) <= thresholds.subjectPositionDelta

        // Dominant axis decides the reported direction (screen coords: +y = lower in frame).
        val direction = when {
            matched -> ReferenceDriftDirection.NONE
            abs(dx) >= abs(dy) && dx < 0 -> ReferenceDriftDirection.LEFT
            abs(dx) >= abs(dy) -> ReferenceDriftDirection.RIGHT
            dy < 0 -> ReferenceDriftDirection.UP
            else -> ReferenceDriftDirection.DOWN
        }
        val message = when (direction) {
            ReferenceDriftDirection.LEFT -> "Subject moved left from the reference."
            ReferenceDriftDirection.RIGHT -> "Subject moved right from the reference."
            ReferenceDriftDirection.UP -> "Subject moved higher than the reference."
            ReferenceDriftDirection.DOWN -> "Subject moved lower than the reference."
            else -> ""
        }
        val dominant = if (abs(dx) >= abs(dy)) dx else dy
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = confidenceBlend(score(dominant, thresholds.subjectPositionDelta), confidence),
            delta = dominant,
            direction = direction,
            message = message,
        )
    }

    // ---- Subject size ----

    private fun compareSize(
        refSubject: ReferenceSubjectProfile?,
        liveSubject: NormalizedRect?,
        confidence: Float,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (refSubject == null || liveSubject == null) return unavailable()
        if (confidence < thresholds.minFeatureConfidence) return unavailable()

        // Compare linear size (sqrt of area): relative change, so 0.14 = "14% larger/smaller".
        val refLinear = sqrt(refSubject.normalizedArea)
        val liveLinear = sqrt(liveSubject.area.coerceAtLeast(0f))
        if (refLinear <= 0f) return unavailable()
        val delta = (liveLinear - refLinear) / refLinear
        val matched = abs(delta) <= thresholds.subjectSizeDelta

        val direction = when {
            matched -> ReferenceDriftDirection.NONE
            delta > 0 -> ReferenceDriftDirection.LARGER
            else -> ReferenceDriftDirection.SMALLER
        }
        val message = when (direction) {
            ReferenceDriftDirection.LARGER -> "Subject is larger than the reference."
            ReferenceDriftDirection.SMALLER -> "Subject is smaller than the reference."
            else -> ""
        }
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = confidenceBlend(score(delta, thresholds.subjectSizeDelta), confidence),
            delta = delta,
            direction = direction,
            message = message,
        )
    }

    // ---- Headroom ----

    private fun compareHeadroom(
        refSubject: ReferenceSubjectProfile?,
        liveSubject: NormalizedRect?,
        enabled: Boolean,
        suppressed: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (suppressed || refSubject == null || liveSubject == null) return unavailable()

        // Headroom = space above the face; compare the normalized face-top position.
        val refTop = refSubject.normalizedCenterY - refSubject.normalizedHeight / 2f
        val delta = liveSubject.top - refTop
        val matched = abs(delta) <= thresholds.headroomDelta

        val direction = when {
            matched -> ReferenceDriftDirection.NONE
            delta < 0 -> ReferenceDriftDirection.UP // face top rose → less headroom
            else -> ReferenceDriftDirection.DOWN
        }
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = score(delta, thresholds.headroomDelta),
            delta = delta,
            direction = direction,
            message = if (matched) "" else "Headroom is different from the reference.",
        )
    }

    // ---- Face presence (human-only extra) ----

    private fun compareFacePresence(
        refFace: ReferenceFaceProfile?,
        faces: FaceDetectionResult?,
        liveFacePresent: Boolean,
        enabled: Boolean,
        suppressed: Boolean,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        // Only meaningful when the reference actually contains a face and detection is running;
        // while SUBJECT_PRESENCE owns a confirmed-missing person, this goes unavailable.
        if (suppressed || refFace?.faceDetected != true || faces == null) return unavailable()

        return if (liveFacePresent) {
            ReferenceSignalResult(enabled = true, available = true, matched = true, score = 1f)
        } else {
            ReferenceSignalResult(
                enabled = true,
                available = true,
                matched = false,
                score = 0f,
                delta = 1f,
                direction = ReferenceDriftDirection.MISSING,
                message = "Face is not visible like the reference.",
            )
        }
    }

    // ---- Eye visibility (human-only extra) ----

    private fun compareEyeVisibility(
        refFace: ReferenceFaceProfile?,
        eyes: EyeDetectionResult?,
        liveFacePresent: Boolean,
        liveEyesPresent: Boolean,
        enabled: Boolean,
        suppressed: Boolean,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        // Needs reference eyes AND a live face: when the face itself is gone, FACE_PRESENCE owns
        // the drift — this signal goes unavailable instead of double-alerting.
        if (suppressed || refFace?.eyesDetected != true || eyes == null || !liveFacePresent) {
            return unavailable()
        }

        return if (liveEyesPresent) {
            ReferenceSignalResult(enabled = true, available = true, matched = true, score = 1f)
        } else {
            ReferenceSignalResult(
                enabled = true,
                available = true,
                matched = false,
                score = 0f,
                delta = 1f,
                direction = ReferenceDriftDirection.MISSING,
                message = "Eyes are not visible like the reference.",
            )
        }
    }

    // ---- Subject presence (any semantic category) ----

    private fun compareSubjectPresence(
        ai: ReferenceAiProfile?,
        strategy: ComparisonStrategy,
        snapshot: SceneSnapshot?,
        expected: SubjectMatcher.Expected?,
        matchedSubject: SceneSubject?,
        enabled: Boolean,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (ai == null || snapshot == null) return unavailable()

        if (strategy == ComparisonStrategy.MULTI_OBJECT_STRATEGY) {
            return compareMultiObjectPresence(ai, snapshot)
        }

        if (expected == null) return unavailable() // no-subject reference: nothing to be missing

        return if (matchedSubject != null) {
            ReferenceSignalResult(enabled = true, available = true, matched = true, score = 1f)
        } else {
            ReferenceSignalResult(
                enabled = true,
                available = true,
                matched = false,
                score = 0f,
                delta = 1f,
                direction = ReferenceDriftDirection.MISSING,
                message = "Reference subject is missing.",
            )
        }
    }

    /** MULTI_OBJECT: every important reference category must still be represented live. */
    private fun compareMultiObjectPresence(
        ai: ReferenceAiProfile,
        snapshot: SceneSnapshot,
    ): ReferenceSignalResult {
        val required = ai.subjects
            .filter { it.confidence >= MULTI_IMPORTANT_CONFIDENCE }
            .groupingBy { it.category }
            .eachCount()
        if (required.isEmpty()) return unavailable()

        val live = snapshot.subjects.groupingBy { it.category }.eachCount()
        val missing = required.filter { (category, count) -> (live[category] ?: 0) < count }
        return if (missing.isEmpty()) {
            ReferenceSignalResult(enabled = true, available = true, matched = true, score = 1f)
        } else {
            val missingCount = missing.values.sum().toFloat()
            val requiredCount = required.values.sum().toFloat()
            ReferenceSignalResult(
                enabled = true,
                available = true,
                matched = false,
                score = (1f - missingCount / requiredCount).coerceIn(0f, 1f),
                delta = missingCount,
                direction = ReferenceDriftDirection.MISSING,
                message = "One or more reference subjects are missing.",
            )
        }
    }

    // ---- Composition (object layout) ----

    private fun compareComposition(
        ai: ReferenceAiProfile?,
        snapshot: SceneSnapshot?,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        val refSignature = ai?.compositionSignature
            ?.takeIf { it.isNotEmpty() }?.toFloatArray() ?: return unavailable()
        val liveSignature = snapshot?.layoutSignature ?: return unavailable()
        val liveValues = liveSignature.value ?: return unavailable()
        if (liveSignature.confidence < thresholds.minFeatureConfidence) return unavailable()

        val delta = CompositionAnalyzer.delta(refSignature, liveValues)
        val matched = delta <= thresholds.compositionDelta
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = confidenceBlend(score(delta, thresholds.compositionDelta), liveSignature.confidence),
            delta = delta,
            direction = if (matched) ReferenceDriftDirection.NONE else ReferenceDriftDirection.DIFFERENT,
            message = if (matched) "" else "Composition differs from the reference.",
        )
    }

    // ---- Visual similarity (universal fallback) ----

    private fun compareVisualSimilarity(
        referenceEmbedding: ReferenceEmbedding?,
        snapshot: SceneSnapshot?,
        enabled: Boolean,
        thresholds: ReferenceThresholds,
    ): ReferenceSignalResult {
        if (!enabled) return disabled()
        if (referenceEmbedding == null || snapshot == null) return unavailable()
        val live = snapshot.embedding.value ?: return unavailable()
        if (snapshot.embedding.confidence < thresholds.minFeatureConfidence) return unavailable()
        // Model/dimension gate: vectors from different embedders are never comparable — going
        // unavailable is correct; producing a garbage cosine is not.
        if (referenceEmbedding.modelId != snapshot.embeddingModelId ||
            referenceEmbedding.dimensions != live.size
        ) {
            return unavailable()
        }

        val similarity = EmbeddingMath.cosineSimilarity(referenceEmbedding.vector(), live)
        val delta = 1f - similarity
        val threshold = 1f - thresholds.visualSimilarityMin
        val matched = similarity >= thresholds.visualSimilarityMin
        return ReferenceSignalResult(
            enabled = true,
            available = true,
            matched = matched,
            score = confidenceBlend(score(delta, threshold), snapshot.embedding.confidence),
            delta = delta,
            direction = if (matched) ReferenceDriftDirection.NONE else ReferenceDriftDirection.DIFFERENT,
            message = if (matched) "" else "The scene looks different from the reference.",
        )
    }

    // ---- Helpers ----

    /** Largest live face box in normalized coordinates, or null when none/undimensioned. */
    fun largestFaceNormalized(faces: FaceDetectionResult?): NormalizedRect? {
        if (faces == null || faces.boundingBoxes.isEmpty()) return null
        val w = faces.sourceWidth
        val h = faces.sourceHeight
        if (w <= 0 || h <= 0) return null
        val largest = faces.boundingBoxes.maxByOrNull {
            (it.rect.right - it.rect.left).toLong() * (it.rect.bottom - it.rect.top)
        } ?: return null
        val r = largest.rect
        return NormalizedRect(
            left = r.left.toFloat() / w,
            top = r.top.toFloat() / h,
            right = r.right.toFloat() / w,
            bottom = r.bottom.toFloat() / h,
        )
    }

    /** 1.0 at zero delta, 0.5 exactly at the threshold, 0.0 at 2× the threshold. */
    private fun score(delta: Float, threshold: Float): Float {
        if (threshold <= 0f) return if (delta == 0f) 1f else 0f
        return (1f - abs(delta) / (2f * threshold)).coerceIn(0f, 1f)
    }

    /**
     * Blend a drift score toward neutral (1.0) by feature confidence: a shaky feature's drift
     * ranks below the same drift from a solid feature. The matched decision itself stays
     * threshold-based — confidence gates availability, not the verdict.
     */
    private fun confidenceBlend(score: Float, confidence: Float): Float {
        val c = confidence.coerceIn(0f, 1f)
        return score * c + (1f - c)
    }

    private fun disabled() = ReferenceSignalResult(enabled = false, available = false)

    private fun unavailable() = ReferenceSignalResult(enabled = true, available = false)

    private fun overallScore(result: ReferenceMatchResult): Float {
        val evaluable = ReferenceSignal.entries
            .map { result.signal(it) }
            .filter { it.enabled && it.available }
        if (evaluable.isEmpty()) return 1f
        return evaluable.map { it.score }.sum() / evaluable.size
    }

    /** MULTI_OBJECT: reference subjects below this confidence don't have to stay in frame. */
    private const val MULTI_IMPORTANT_CONFIDENCE = 0.5f
}
