package app.dyrecto.liveview.perception

import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.reference.CurrentReferenceInput
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalResult
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerceptualEvaluatorTest {

    private val tuning = PerceptualTuning.forTolerance(ReferenceTolerance.MEDIUM)

    private fun refExposure(
        mean: Float = 120f,
        median: Float = 118f,
        p95: Float = 200f,
        p99: Float = 240f,
        highlightCoverage: Float = 1f,
        shadowCoverage: Float = 1f,
        subject: SubjectExposureStats? = null,
    ) = ReferenceExposureProfile(
        mean = mean, median = median, p95 = p95, p99 = p99,
        highlightCoverage = highlightCoverage, shadowCoverage = shadowCoverage,
        subjectExposure = subject,
    )

    private fun profile(exposure: ReferenceExposureProfile) = ReferenceProfile(
        id = "ref", name = "r", createdAtMs = 0, imageUri = null, width = 100, height = 100,
        exposure = exposure,
        color = ReferenceColorProfile(0f, 0f, 0f, 0f, 0f),
        subject = null, face = null,
        options = ReferenceMonitorOptions(tolerance = ReferenceTolerance.MEDIUM),
    )

    private fun histogram(
        mean: Float,
        median: Int = mean.toInt(),
        p95: Int = 200,
        p99: Int = 240,
        shadowPct: Float = 1f,
    ) = HistogramResult(
        moduleId = "t", bins = IntArray(256), totalPixels = 10_000,
        mean = mean, median = median, percentile95 = p95, percentile99 = p99,
        clippedHighlightPixels = 0, clippedShadowPixels = 0,
        clippedHighlightPercentage = 0f, clippedShadowPercentage = shadowPct,
        effectiveStride = 1,
    )

    private fun exposureCtx(
        ref: ReferenceExposureProfile,
        live: HistogramResult?,
        strategy: ComparisonStrategy = ComparisonStrategy.HUMAN_STRATEGY,
        liveSubject: SubjectExposureStats? = null,
        delta: Float = 0f,
    ) = PerceptualContext(
        signal = ReferenceSignal.EXPOSURE,
        raw = ReferenceSignalResult(enabled = true, available = live != null, delta = delta),
        reference = profile(ref),
        input = CurrentReferenceInput(histogram = live),
        liveSubjectExposure = liveSubject,
        strategy = strategy,
        tuning = tuning,
    )

    // ---- The Phase 12 founding scenario ----

    @Test
    fun `mean 126 vs 120 is perceptually identical`() {
        val obs = ExposureEvaluator.evaluate(
            exposureCtx(refExposure(mean = 126f, median = 126f), histogram(mean = 120f, median = 120)),
        )
        assertTrue(obs.available)
        assertEquals(0f, obs.noticeability, 1e-6f)
        assertEquals(ReferenceDriftDirection.NONE, obs.direction)
    }

    @Test
    fun `large exposure mismatch is clearly noticeable with agreed direction`() {
        val obs = ExposureEvaluator.evaluate(
            exposureCtx(
                refExposure(mean = 120f, median = 118f, p95 = 180f),
                histogram(mean = 175f, median = 172, p95 = 235),
            ),
        )
        assertTrue(obs.noticeability > 0.5f)
        assertEquals(ReferenceDriftDirection.BRIGHTER, obs.direction)
        assertEquals(1f, obs.directionConfidence, 1e-6f) // every component agrees
    }

    @Test
    fun `conflicting components lower direction confidence`() {
        // Midtones brighter, highlights darker: a difference exists but its correction is unclear.
        val obs = ExposureEvaluator.evaluate(
            exposureCtx(
                refExposure(mean = 120f, median = 120f, p95 = 230f),
                histogram(mean = 155f, median = 155, p95 = 185),
            ),
        )
        assertTrue(obs.noticeability > 0f)
        assertTrue("direction confidence must drop on disagreement", obs.directionConfidence < 0.9f)
    }

    // ---- Strategy awareness ----

    @Test
    fun `same p95 drift scores higher under landscape than human strategy`() {
        val ref = refExposure(mean = 120f, median = 120f, p95 = 180f)
        val live = histogram(mean = 120f, median = 120, p95 = 235)
        val human = ExposureEvaluator.evaluate(exposureCtx(ref, live, ComparisonStrategy.HUMAN_STRATEGY))
        val landscape = ExposureEvaluator.evaluate(exposureCtx(ref, live, ComparisonStrategy.LANDSCAPE_STRATEGY))
        assertTrue(
            "landscape (${landscape.noticeability}) must exceed human (${human.noticeability})",
            landscape.noticeability > human.noticeability,
        )
    }

    @Test
    fun `subject exposure drives the human composite when available`() {
        val ref = refExposure(subject = SubjectExposureStats(120f, 120f, 0f, 0f, 500))
        val live = histogram(mean = 120f, median = 120)
        val withoutSubject = ExposureEvaluator.evaluate(exposureCtx(ref, live))
        val withDriftedSubject = ExposureEvaluator.evaluate(
            exposureCtx(ref, live, liveSubject = SubjectExposureStats(170f, 168f, 5f, 0f, 500)),
        )
        assertEquals(0f, withoutSubject.noticeability, 1e-6f) // frame matches, subject missing → drops out
        assertTrue(withDriftedSubject.noticeability > 0.2f)
        assertEquals(ReferenceDriftDirection.BRIGHTER, withDriftedSubject.direction)
    }

    // ---- Determinism (statelessness invariant) ----

    @Test
    fun `evaluators are pure - repeated evaluation yields identical observations`() {
        val ctx = exposureCtx(
            refExposure(mean = 120f), histogram(mean = 160f, median = 158),
            liveSubject = SubjectExposureStats(150f, 149f, 2f, 0f, 100),
        )
        val first = ExposureEvaluator.evaluate(ctx)
        repeat(5) { assertEquals(first, ExposureEvaluator.evaluate(ctx)) }
    }

    // ---- Similarity buckets ----

    private fun similarityCtx(cosine: Float) = PerceptualContext(
        signal = ReferenceSignal.VISUAL_SIMILARITY,
        raw = ReferenceSignalResult(enabled = true, available = true, delta = 1f - cosine),
        reference = profile(refExposure()),
        input = CurrentReferenceInput(),
        liveSubjectExposure = null,
        strategy = ComparisonStrategy.GENERIC_SCENE_STRATEGY,
        tuning = tuning,
    )

    @Test
    fun `cosine maps to perceived match buckets, never exposed raw`() {
        assertEquals(PerceivedMatchBucket.PERFECT, SimilarityEvaluator.evaluate(similarityCtx(0.995f)).matchBucket)
        assertEquals(PerceivedMatchBucket.IDENTICAL, SimilarityEvaluator.evaluate(similarityCtx(0.97f)).matchBucket)
        assertEquals(PerceivedMatchBucket.VERY_CLOSE, SimilarityEvaluator.evaluate(similarityCtx(0.93f)).matchBucket)
        assertEquals(PerceivedMatchBucket.SLIGHTLY_DIFFERENT, SimilarityEvaluator.evaluate(similarityCtx(0.88f)).matchBucket)
        assertEquals(PerceivedMatchBucket.NOTICEABLY_DIFFERENT, SimilarityEvaluator.evaluate(similarityCtx(0.75f)).matchBucket)
        assertEquals(PerceivedMatchBucket.DIFFERENT, SimilarityEvaluator.evaluate(similarityCtx(0.5f)).matchBucket)

        assertEquals(0f, SimilarityEvaluator.evaluate(similarityCtx(0.995f)).noticeability, 1e-6f)
        assertEquals(0.6f, SimilarityEvaluator.evaluate(similarityCtx(0.75f)).noticeability, 1e-6f)
    }

    // ---- Geometry dead zones + config-only tuning ----

    private fun positionCtx(delta: Float, tuning: PerceptualTuning = this.tuning) = PerceptualContext(
        signal = ReferenceSignal.SUBJECT_POSITION,
        raw = ReferenceSignalResult(
            enabled = true, available = true, delta = delta,
            direction = ReferenceDriftDirection.RIGHT,
        ),
        reference = profile(refExposure()),
        input = CurrentReferenceInput(),
        liveSubjectExposure = null,
        strategy = ComparisonStrategy.HUMAN_STRATEGY,
        tuning = tuning,
    )

    @Test
    fun `tiny subject movement sits in the dead zone`() {
        val obs = PositionEvaluator.evaluate(positionCtx(delta = 0.02f))
        assertEquals(0f, obs.noticeability, 1e-6f)
        assertEquals(ReferenceDriftDirection.NONE, obs.direction)
    }

    @Test
    fun `meaningful movement is noticeable with its direction`() {
        val obs = PositionEvaluator.evaluate(positionCtx(delta = 0.20f))
        assertTrue(obs.noticeability > 0.3f)
        assertEquals(ReferenceDriftDirection.RIGHT, obs.direction)
    }

    @Test
    fun `changing config alone changes the outcome - no literals in evaluators`() {
        val wide = tuning.copy(
            profile = tuning.profile.copy(
                position = PerceptualThresholds.SignalBand(deadZone = 0.25f, saturation = 0.5f),
            ),
        )
        val default = PositionEvaluator.evaluate(positionCtx(delta = 0.20f))
        val tuned = PositionEvaluator.evaluate(positionCtx(delta = 0.20f, tuning = wide))
        assertTrue(default.noticeability > 0f)
        assertEquals(0f, tuned.noticeability, 1e-6f) // same code path, new dead zone absorbs it
    }

    // ---- Presence ----

    @Test
    fun `presence is binary with configured noticeability`() {
        val evaluator = PresenceEvaluator(ReferenceSignal.SUBJECT_PRESENCE)
        val missing = evaluator.evaluate(
            positionCtx(0f).copy(
                signal = ReferenceSignal.SUBJECT_PRESENCE,
                raw = ReferenceSignalResult(
                    enabled = true, available = true, matched = false, delta = 1f,
                    direction = ReferenceDriftDirection.MISSING,
                ),
            ),
        )
        assertEquals(tuning.profile.presenceNoticeability, missing.noticeability, 1e-6f)
        assertEquals(ReferenceDriftDirection.MISSING, missing.direction)
        val present = evaluator.evaluate(
            positionCtx(0f).copy(
                signal = ReferenceSignal.SUBJECT_PRESENCE,
                raw = ReferenceSignalResult(enabled = true, available = true, matched = true),
            ),
        )
        assertEquals(0f, present.noticeability, 1e-6f)
    }
}
