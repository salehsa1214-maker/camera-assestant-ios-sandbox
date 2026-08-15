package app.dyrecto.liveview.reference

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.reference.ai.ComparisonStrategySelector
import app.dyrecto.liveview.reference.ai.CompositionAnalyzer
import app.dyrecto.liveview.reference.ai.PrimarySubjectSelector
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SceneModeClassifier
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.NormalizedPoint
import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import app.dyrecto.liveview.reference.ai.snapshot.SnapshotSource
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.HistogramResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * End-to-end pure pipeline test: detections → subject/scene/strategy selection → profile →
 * SceneComparator → ReferenceStateMachine → ReferenceAlertRules — the "dog leaves the frame
 * and comes back" scenario, with no Android or ML dependencies anywhere.
 */
class ReferenceEndToEndTest {

    private val dogBox = NormalizedRect(0.35f, 0.4f, 0.65f, 0.8f)

    // ---- Build the reference the way the analyzer does: through the pure selectors ----

    private fun buildReference(): ReferenceProfile {
        val detections = listOf(
            SceneSubject(
                trackId = SceneSubject.NO_TRACK,
                category = SubjectCategory.ANIMAL,
                rawLabel = "dog",
                confidence = 0.85f,
                boundingBox = dogBox,
                tracked = false,
                maskAvailable = false,
            ),
        )
        val primary = PrimarySubjectSelector.select(detections)
        val sceneMode = SceneModeClassifier.classify(detections, primary, embeddingAvailable = true)
        val strategy = ComparisonStrategySelector.select(
            sceneMode.value ?: SceneMode.UNKNOWN,
            primary.value,
            detectionAvailable = true,
        )

        assertEquals(SceneMode.ANIMAL, sceneMode.value)
        assertEquals(PrimarySubjectType.ANIMAL, primary.value?.type)

        return ReferenceProfile(
            id = "dog-ref",
            name = "Dog",
            createdAtMs = 0,
            imageUri = null,
            width = 1280,
            height = 720,
            exposure = ReferenceExposureProfile(128f, 128f, 200f, 220f, 0f, 0f),
            color = ReferenceColorProfile(100f, 100f, 100f, 0f, 0f),
            subject = null,
            face = null,
            options = ReferenceMonitorOptions(tolerance = ReferenceTolerance.MEDIUM),
            ai = ReferenceAiProfile(
                sceneMode = sceneMode.value!!,
                sceneModeConfidence = sceneMode.confidence,
                strategy = strategy,
                primarySubjectType = primary.value!!.type,
                primarySubjectCategory = primary.value!!.category,
                primarySubjectRawLabel = primary.value!!.rawLabel,
                primarySubjectConfidence = primary.value!!.confidence,
                primarySubjectBox = primary.value!!.boundingBox,
                subjects = detections.map {
                    ReferenceAiSubject(it.category, it.rawLabel, it.confidence, it.boundingBox)
                },
                compositionSignature = CompositionAnalyzer.gridSignature(detections).toList(),
                embeddingId = "dog-ref-emb",
                embeddingModelId = "test-model",
            ),
        )
    }

    private val referenceEmbedding = ReferenceEmbedding(
        id = "dog-ref-emb",
        modelId = "test-model",
        dimensions = 4,
        values = listOf(1f, 0f, 0f, 0f),
        createdAtMs = 0,
    )

    // ---- Live-side fixtures ----

    private fun liveSnapshot(dogPresent: Boolean, atMs: Long): SceneSnapshot {
        val subjects = if (dogPresent) {
            listOf(
                SceneSubject(
                    trackId = 1,
                    category = SubjectCategory.ANIMAL,
                    rawLabel = "dog",
                    confidence = 0.85f,
                    boundingBox = dogBox,
                    tracked = true,
                    maskAvailable = false,
                ),
            )
        } else {
            emptyList()
        }
        return SceneSnapshot(
            subjects = subjects,
            primarySubject = FeatureValue.absent(),
            sceneMode = FeatureValue(SceneMode.ANIMAL, 0.8f),
            subjectPosition = FeatureValue(NormalizedPoint(dogBox.centerX, dogBox.centerY), 0.85f),
            subjectSize = FeatureValue(dogBox.area, 0.85f),
            layoutSignature = FeatureValue(
                CompositionAnalyzer.gridSignature(subjects),
                if (subjects.isEmpty()) 0f else 1f,
            ),
            embedding = FeatureValue(floatArrayOf(1f, 0f, 0f, 0f), 1f),
            embeddingModelId = "test-model",
            segmentationSummary = null,
            source = if (dogPresent) SnapshotSource.TRACKER else SnapshotSource.DETECTOR,
            analyzedAtMs = atMs,
        )
    }

    private fun liveInput(dogPresent: Boolean, atMs: Long) = CurrentReferenceInput(
        histogram = HistogramResult(
            moduleId = "test", bins = IntArray(256), totalPixels = 1000,
            mean = 128f, median = 128, percentile95 = 200, percentile99 = 220,
            clippedHighlightPixels = 0, clippedShadowPixels = 0,
            clippedHighlightPercentage = 0f, clippedShadowPercentage = 0f, effectiveStride = 1,
        ),
        colorStats = ColorStatsResult("test", 100f, 100f, 100f, 1000),
        sceneSnapshot = liveSnapshot(dogPresent, atMs),
        referenceEmbedding = referenceEmbedding,
    )

    @Test
    fun `dog leaves the frame and comes back - one alert, one recovery`() {
        val reference = buildReference()
        val stateMachine = ReferenceStateMachine()
        val thresholds = ReferenceConfig.thresholdsFor(ReferenceTolerance.MEDIUM)
        val idGen = AlertIdGenerator()
        var alertState = ReferenceAlertState()
        val allAlerts = mutableListOf<Alert>()
        var nowMs = 1_000L

        fun tick(dogPresent: Boolean): List<Alert> {
            nowMs += 100
            val raw = SceneComparator.compare(reference, liveInput(dogPresent, nowMs), nowMs)
            val enriched = stateMachine.update(raw, thresholds)
            val result = ReferenceAlertRules.evaluate(alertState, enriched, idGen)
            alertState = result.state
            allAlerts += result.alerts
            return result.alerts
        }

        // Phase 1: stable matching frames — nothing fires (first tick seeds).
        repeat(10) { assertTrue(tick(dogPresent = true).isEmpty()) }

        // Phase 2: the dog walks out. One flicker frame is ignored...
        assertTrue(tick(dogPresent = false).isEmpty())
        assertTrue(tick(dogPresent = true).isEmpty())
        // ...but a persistent absence confirms after 5 consecutive frames and fires ONCE.
        val duringAbsence = mutableListOf<Alert>()
        repeat(10) { duringAbsence += tick(dogPresent = false) }
        val missing = duringAbsence.filter { it.type == AlertType.REFERENCE_SUBJECT_MISSING }
        assertEquals(1, missing.size)
        assertEquals("Reference subject is missing.", missing[0].message)

        // Composition drifted with the dog gone too — it may fire its own confirmed alert, but
        // never more than once.
        assertTrue(duringAbsence.count { it.type == AlertType.REFERENCE_COMPOSITION_DRIFT } <= 1)
        // Face alerts never fire: this is an ANIMAL reference (face signals disabled).
        assertTrue(duringAbsence.none { it.type == AlertType.REFERENCE_FACE_MISSING })

        // Phase 3: the dog comes back — one grouped recovery once every signal re-confirms.
        val duringReturn = mutableListOf<Alert>()
        repeat(10) { duringReturn += tick(dogPresent = true) }
        val recoveries = duringReturn.filter { it.type == AlertType.REFERENCE_RECOVERED }
        assertEquals(1, recoveries.size)

        // Phase 4: stable again — silence.
        repeat(10) { assertTrue(tick(dogPresent = true).isEmpty()) }

        // Total sanity: no duplicate drift alerts of any type across the whole scenario.
        val driftCounts = allAlerts.filter { it.type != AlertType.REFERENCE_RECOVERED }
            .groupingBy { it.type }.eachCount()
        driftCounts.forEach { (type, count) ->
            assertEquals("duplicate alert for $type", 1, count)
        }
    }
}
