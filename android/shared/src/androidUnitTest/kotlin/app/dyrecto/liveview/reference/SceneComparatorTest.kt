package app.dyrecto.liveview.reference

import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.NormalizedPoint
import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import app.dyrecto.liveview.reference.ai.snapshot.SnapshotSource
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.FaceBox
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.PixelRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for [SceneComparator]. Face boxes are plain [PixelRect]s (the old
 * android.graphics.Rect field-assignment trick died with the KMP move). MEDIUM tolerance: mean/p95 Δ14, warmth Δ18, position Δ0.09, size Δ0.14
 * (relative), headroom Δ0.07, composition Δ0.10, similarity ≥0.80, feature-confidence floor 0.25.
 */
class SceneComparatorTest {

    // ---- Fixtures ----

    private fun rect(l: Int, t: Int, r: Int, b: Int): PixelRect = PixelRect(l, t, r, b)

    private fun histogram(mean: Float, p95: Int) = HistogramResult(
        moduleId = "test",
        bins = IntArray(256),
        totalPixels = 1000,
        mean = mean,
        median = mean.toInt(),
        percentile95 = p95,
        percentile99 = p95,
        clippedHighlightPixels = 0,
        clippedShadowPixels = 0,
        clippedHighlightPercentage = 0f,
        clippedShadowPercentage = 0f,
        effectiveStride = 1,
    )

    private fun colorStats(r: Float, g: Float, b: Float) =
        ColorStatsResult(moduleId = "test", avgR = r, avgG = g, avgB = b, sampleCount = 1000)

    private fun faces(vararg boxes: PixelRect) = FaceDetectionResult(
        moduleId = "test",
        facesDetected = boxes.size,
        boundingBoxes = boxes.map { FaceBox(it, 0.8f) },
        averageConfidence = if (boxes.isEmpty()) 0f else 0.8f,
        analysisTimeMs = 0,
        sourceWidth = 100,
        sourceHeight = 100,
    )

    private fun eyes(count: Int) = EyeDetectionResult(
        moduleId = "test",
        eyesDetected = count,
        boundingBoxes = emptyList(),
        averageConfidence = if (count == 0) 0f else 0.8f,
        analysisTimeMs = 0,
    )

    /** Reference: mean 128 / p95 200, neutral color, face centered at (0.5, 0.5), 0.2×0.2. */
    private fun referenceProfile(
        withSubject: Boolean = true,
        options: ReferenceMonitorOptions = ReferenceMonitorOptions(tolerance = ReferenceTolerance.MEDIUM),
        ai: ReferenceAiProfile? = null,
    ): ReferenceProfile {
        val box = if (withSubject) NormalizedRect(0.4f, 0.4f, 0.6f, 0.6f) else null
        return ReferenceProfile(
            id = "ref-1",
            name = "Test",
            createdAtMs = 0,
            imageUri = null,
            width = 100,
            height = 100,
            exposure = ReferenceExposureProfile(
                mean = 128f, median = 128f, p95 = 200f, p99 = 220f,
                highlightCoverage = 0f, shadowCoverage = 0f,
            ),
            color = ReferenceColorProfile(avgR = 100f, avgG = 100f, avgB = 100f, warmthScore = 0f, tintScore = 0f),
            subject = box?.let {
                ReferenceSubjectProfile(
                    normalizedCenterX = it.centerX,
                    normalizedCenterY = it.centerY,
                    normalizedWidth = it.width,
                    normalizedHeight = it.height,
                    normalizedArea = it.area,
                )
            },
            face = ReferenceFaceProfile(
                faceDetected = withSubject,
                eyesDetected = withSubject,
                faceBox = box,
                eyeCount = if (withSubject) 2 else 0,
            ),
            options = options,
            ai = ai,
        )
    }

    // ---- AI fixtures (vehicle reference: a car at 0.3,0.3..0.7,0.7) ----

    private val carBox = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)
    private val referenceEmbeddingVector = listOf(1f, 0f, 0f, 0f)

    private fun vehicleAiProfile(
        strategy: ComparisonStrategy = ComparisonStrategy.VEHICLE_STRATEGY,
        compositionSignature: List<Float> = List(9) { if (it == 4) 0.8f else 0.1f },
    ) = ReferenceAiProfile(
        sceneMode = SceneMode.VEHICLE,
        sceneModeConfidence = 0.9f,
        strategy = strategy,
        primarySubjectType = PrimarySubjectType.VEHICLE,
        primarySubjectCategory = SubjectCategory.VEHICLE,
        primarySubjectRawLabel = "car",
        primarySubjectConfidence = 0.9f,
        primarySubjectBox = carBox,
        subjects = listOf(ReferenceAiSubject(SubjectCategory.VEHICLE, "car", 0.9f, carBox)),
        compositionSignature = compositionSignature,
        embeddingId = "ref-1-emb",
        embeddingModelId = "test-model",
    )

    private fun referenceEmbedding(model: String = "test-model") = ReferenceEmbedding(
        id = "ref-1-emb",
        modelId = model,
        dimensions = referenceEmbeddingVector.size,
        values = referenceEmbeddingVector,
        createdAtMs = 0,
    )

    private fun liveSubject(
        category: SubjectCategory = SubjectCategory.VEHICLE,
        box: NormalizedRect = carBox,
        confidence: Float = 0.9f,
        rawLabel: String? = "car",
    ) = SceneSubject(
        trackId = 1,
        category = category,
        rawLabel = rawLabel,
        confidence = confidence,
        boundingBox = box,
        tracked = true,
        maskAvailable = false,
    )

    private fun snapshot(
        subjects: List<SceneSubject> = listOf(liveSubject()),
        embedding: FloatArray? = floatArrayOf(1f, 0f, 0f, 0f),
        embeddingConfidence: Float = 1f,
        layoutSignature: FloatArray? = FloatArray(9) { if (it == 4) 0.8f else 0.1f },
        layoutConfidence: Float = 1f,
        analyzedAtMs: Long = 1000L,
    ) = SceneSnapshot(
        subjects = subjects,
        primarySubject = FeatureValue.absent(),
        sceneMode = FeatureValue(SceneMode.VEHICLE, 0.9f),
        subjectPosition = FeatureValue(NormalizedPoint(0.5f, 0.5f), 0.9f),
        subjectSize = FeatureValue(carBox.area, 0.9f),
        layoutSignature = layoutSignature?.let { FeatureValue(it, layoutConfidence) } ?: FeatureValue.absent(),
        embedding = embedding?.let { FeatureValue(it, embeddingConfidence) } ?: FeatureValue.absent(),
        embeddingModelId = if (embedding != null) "test-model" else null,
        segmentationSummary = null,
        source = SnapshotSource.TRACKER,
        analyzedAtMs = analyzedAtMs,
    )

    /** Live input that matches the reference exactly (face box 40..60 in a 100×100 frame). */
    private fun matchingInput() = CurrentReferenceInput(
        histogram = histogram(mean = 128f, p95 = 200),
        colorStats = colorStats(100f, 100f, 100f),
        faces = faces(rect(40, 40, 60, 60)),
        eyes = eyes(2),
    )

    /** AI vehicle input that matches the vehicle reference exactly. */
    private fun matchingVehicleInput(snap: SceneSnapshot = snapshot()) = CurrentReferenceInput(
        histogram = histogram(mean = 128f, p95 = 200),
        colorStats = colorStats(100f, 100f, 100f),
        sceneSnapshot = snap,
        referenceEmbedding = referenceEmbedding(),
    )

    private fun vehicleReference(
        strategy: ComparisonStrategy = ComparisonStrategy.VEHICLE_STRATEGY,
    ) = referenceProfile(withSubject = false, ai = vehicleAiProfile(strategy))

    private fun compare(
        input: CurrentReferenceInput,
        reference: ReferenceProfile = referenceProfile(),
    ) = SceneComparator.compare(reference, input, nowMs = 1000L)

    // ---- Perfect match (legacy face path, ai = null) ----

    @Test
    fun `matching frame matches every signal with high overall score`() {
        val result = compare(matchingInput())
        assertTrue(result.exposureMatch.matched)
        assertTrue(result.whiteBalanceMatch.matched)
        assertTrue(result.subjectPositionMatch.matched)
        assertTrue(result.subjectSizeMatch.matched)
        assertTrue(result.headroomMatch.matched)
        assertTrue(result.facePresenceMatch.matched)
        assertTrue(result.eyeVisibilityMatch.matched)
        assertTrue(result.overallScore > 0.9f)
        assertEquals("ref-1", result.referenceId)
        assertEquals(ComparisonStrategy.HUMAN_STRATEGY, result.strategy)
        assertTrue(result.active)
    }

    // ---- Exposure ----

    @Test
    fun `brighter exposure drifts BRIGHTER`() {
        val result = compare(matchingInput().copy(histogram = histogram(mean = 150f, p95 = 200)))
        val s = result.exposureMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.BRIGHTER, s.direction)
        assertEquals("Exposure is brighter than the reference.", s.message)
    }

    @Test
    fun `darker exposure drifts DARKER`() {
        val result = compare(matchingInput().copy(histogram = histogram(mean = 100f, p95 = 200)))
        val s = result.exposureMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.DARKER, s.direction)
        assertEquals("Exposure is darker than the reference.", s.message)
    }

    @Test
    fun `exposure within tolerance matches`() {
        val result = compare(matchingInput().copy(histogram = histogram(mean = 138f, p95 = 205)))
        assertTrue(result.exposureMatch.matched)
        assertEquals(ReferenceDriftDirection.NONE, result.exposureMatch.direction)
    }

    @Test
    fun `p95-only drift also drifts even when mean matches`() {
        val result = compare(matchingInput().copy(histogram = histogram(mean = 128f, p95 = 230)))
        assertFalse(result.exposureMatch.matched)
        assertEquals(ReferenceDriftDirection.BRIGHTER, result.exposureMatch.direction)
    }

    @Test
    fun `exposure unavailable without histogram`() {
        val result = compare(matchingInput().copy(histogram = null))
        assertTrue(result.exposureMatch.enabled)
        assertFalse(result.exposureMatch.available)
    }

    // ---- White balance ----

    @Test
    fun `red shift drifts WARMER`() {
        val result = compare(matchingInput().copy(colorStats = colorStats(130f, 100f, 100f)))
        val s = result.whiteBalanceMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.WARMER, s.direction)
        assertEquals("Image is warmer than the reference.", s.message)
    }

    @Test
    fun `blue shift drifts COOLER`() {
        val result = compare(matchingInput().copy(colorStats = colorStats(100f, 100f, 130f)))
        val s = result.whiteBalanceMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.COOLER, s.direction)
        assertEquals("Image is cooler than the reference.", s.message)
    }

    @Test
    fun `tint-only drift reports UNKNOWN direction`() {
        val result = compare(matchingInput().copy(colorStats = colorStats(100f, 130f, 100f)))
        val s = result.whiteBalanceMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.UNKNOWN, s.direction)
    }

    // ---- Subject position (legacy face path) ----

    @Test
    fun `subject shifted left drifts LEFT`() {
        val result = compare(matchingInput().copy(faces = faces(rect(20, 40, 40, 60))))
        val s = result.subjectPositionMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.LEFT, s.direction)
        assertEquals("Subject moved left from the reference.", s.message)
    }

    @Test
    fun `subject shifted right drifts RIGHT`() {
        val result = compare(matchingInput().copy(faces = faces(rect(60, 40, 80, 60))))
        assertEquals(ReferenceDriftDirection.RIGHT, result.subjectPositionMatch.direction)
    }

    @Test
    fun `subject shifted up drifts UP`() {
        val result = compare(matchingInput().copy(faces = faces(rect(40, 20, 60, 40))))
        val s = result.subjectPositionMatch
        assertEquals(ReferenceDriftDirection.UP, s.direction)
        assertEquals("Subject moved higher than the reference.", s.message)
    }

    @Test
    fun `subject shifted down drifts DOWN`() {
        val result = compare(matchingInput().copy(faces = faces(rect(40, 60, 60, 80))))
        val s = result.subjectPositionMatch
        assertEquals(ReferenceDriftDirection.DOWN, s.direction)
        assertEquals("Subject moved lower than the reference.", s.message)
    }

    // ---- Subject size (legacy face path) ----

    @Test
    fun `larger subject drifts LARGER`() {
        val result = compare(matchingInput().copy(faces = faces(rect(25, 25, 75, 75))))
        val s = result.subjectSizeMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.LARGER, s.direction)
        assertEquals("Subject is larger than the reference.", s.message)
    }

    @Test
    fun `smaller subject drifts SMALLER`() {
        val result = compare(matchingInput().copy(faces = faces(rect(45, 45, 55, 55))))
        val s = result.subjectSizeMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.SMALLER, s.direction)
        assertEquals("Subject is smaller than the reference.", s.message)
    }

    // ---- Headroom ----

    @Test
    fun `raised face top drifts headroom UP`() {
        // Same size face, top moved from 0.40 to 0.25 (Δ 0.15 > 0.07).
        val result = compare(matchingInput().copy(faces = faces(rect(40, 25, 60, 45))))
        val s = result.headroomMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.UP, s.direction)
        assertEquals("Headroom is different from the reference.", s.message)
    }

    // ---- Face presence / eyes ----

    @Test
    fun `missing face drifts MISSING and disables subject signals`() {
        val result = compare(matchingInput().copy(faces = faces(), eyes = eyes(0)))
        val s = result.facePresenceMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.MISSING, s.direction)
        assertEquals("Face is not visible like the reference.", s.message)
        // No live subject → geometry signals are unavailable, not drifting.
        assertFalse(result.subjectPositionMatch.available)
        assertFalse(result.subjectSizeMatch.available)
        assertFalse(result.headroomMatch.available)
        // Eyes go unavailable too: FACE_PRESENCE owns the drift (no double alert).
        assertFalse(result.eyeVisibilityMatch.available)
    }

    @Test
    fun `missing eyes with face present drifts eye signal`() {
        val result = compare(matchingInput().copy(eyes = eyes(0)))
        val s = result.eyeVisibilityMatch
        assertTrue(result.facePresenceMatch.matched)
        assertFalse(s.matched)
        assertEquals("Eyes are not visible like the reference.", s.message)
    }

    // ---- Reference without a face ----

    @Test
    fun `subject signals unavailable when reference has no face`() {
        val result = compare(matchingInput(), referenceProfile(withSubject = false))
        assertFalse(result.subjectPositionMatch.available)
        assertFalse(result.subjectSizeMatch.available)
        assertFalse(result.headroomMatch.available)
        assertFalse(result.facePresenceMatch.available)
        assertFalse(result.eyeVisibilityMatch.available)
        // Exposure and WB still evaluate.
        assertTrue(result.exposureMatch.available)
        assertTrue(result.whiteBalanceMatch.available)
    }

    // ---- Options gating ----

    @Test
    fun `disabled signals are not evaluated`() {
        val options = ReferenceMonitorOptions(
            monitorExposure = false,
            monitorWhiteBalance = false,
            tolerance = ReferenceTolerance.MEDIUM,
        )
        val result = compare(
            matchingInput().copy(histogram = histogram(mean = 20f, p95 = 40)),
            referenceProfile(options = options),
        )
        assertFalse(result.exposureMatch.enabled)
        assertTrue(result.exposureMatch.matched) // disabled → neutral, no drift
    }

    @Test
    fun `strict tolerance drifts where medium matches`() {
        val input = matchingInput().copy(histogram = histogram(mean = 138f, p95 = 200)) // Δ10
        assertTrue(compare(input).exposureMatch.matched) // MEDIUM: 10 <= 14
        val strict = referenceProfile(options = ReferenceMonitorOptions(tolerance = ReferenceTolerance.STRICT))
        assertFalse(compare(input, strict).exposureMatch.matched) // STRICT: 10 > 8
    }

    // ============================ Phase 10: AI signals ============================

    @Test
    fun `matching vehicle scene matches AI signals under VEHICLE strategy`() {
        val result = compare(matchingVehicleInput(), vehicleReference())
        assertEquals(ComparisonStrategy.VEHICLE_STRATEGY, result.strategy)
        assertTrue(result.subjectPresenceMatch.matched)
        assertTrue(result.subjectPositionMatch.matched)
        assertTrue(result.subjectSizeMatch.matched)
        assertTrue(result.compositionMatch.matched)
        assertTrue(result.visualSimilarityMatch.matched)
    }

    @Test
    fun `vehicle strategy disables human-only extras`() {
        val result = compare(matchingVehicleInput(), vehicleReference())
        assertFalse(result.headroomMatch.enabled)
        assertFalse(result.facePresenceMatch.enabled)
        assertFalse(result.eyeVisibilityMatch.enabled)
    }

    @Test
    fun `missing vehicle drifts SUBJECT_PRESENCE MISSING and suppresses geometry`() {
        val snap = snapshot(subjects = emptyList())
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        val s = result.subjectPresenceMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.MISSING, s.direction)
        assertEquals("Reference subject is missing.", s.message)
        assertFalse(result.subjectPositionMatch.available)
        assertFalse(result.subjectSizeMatch.available)
    }

    @Test
    fun `moved vehicle drifts position using AI subject boxes`() {
        val movedBox = NormalizedRect(0.5f, 0.3f, 0.9f, 0.7f) // center 0.7 vs ref 0.5
        val snap = snapshot(subjects = listOf(liveSubject(box = movedBox)))
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        val s = result.subjectPositionMatch
        assertTrue(s.available)
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.RIGHT, s.direction)
    }

    @Test
    fun `grown vehicle drifts size LARGER using AI subject boxes`() {
        val biggerBox = NormalizedRect(0.15f, 0.15f, 0.85f, 0.85f)
        val snap = snapshot(subjects = listOf(liveSubject(box = biggerBox)))
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        val s = result.subjectSizeMatch
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.LARGER, s.direction)
    }

    @Test
    fun `different embedding drifts VISUAL_SIMILARITY DIFFERENT`() {
        val snap = snapshot(embedding = floatArrayOf(0f, 1f, 0f, 0f)) // orthogonal → cosine 0
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        val s = result.visualSimilarityMatch
        assertTrue(s.available)
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.DIFFERENT, s.direction)
        assertEquals("The scene looks different from the reference.", s.message)
    }

    @Test
    fun `embedding model mismatch is unavailable, never a garbage cosine`() {
        val input = matchingVehicleInput().copy(referenceEmbedding = referenceEmbedding(model = "other-model"))
        val result = compare(input, vehicleReference())
        assertTrue(result.visualSimilarityMatch.enabled)
        assertFalse(result.visualSimilarityMatch.available)
    }

    @Test
    fun `changed layout drifts COMPOSITION DIFFERENT`() {
        val movedLayout = FloatArray(9).also { it[0] = 0.8f } // weight moved center → corner
        val snap = snapshot(layoutSignature = movedLayout)
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        val s = result.compositionMatch
        assertTrue(s.available)
        assertFalse(s.matched)
        assertEquals(ReferenceDriftDirection.DIFFERENT, s.direction)
    }

    @Test
    fun `stale snapshot makes AI signals unavailable`() {
        val snap = snapshot(analyzedAtMs = 1000L - ReferenceConfig.AI_STALE_AFTER_MS - 1)
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        assertFalse(result.subjectPresenceMatch.available)
        assertFalse(result.visualSimilarityMatch.available)
        assertFalse(result.compositionMatch.available)
        // Exposure still evaluates — non-AI signals are unaffected.
        assertTrue(result.exposureMatch.available)
    }

    @Test
    fun `low-confidence subject makes geometry unavailable instead of drifting`() {
        val snap = snapshot(subjects = listOf(liveSubject(confidence = 0.1f)))
        val result = compare(matchingVehicleInput(snap), vehicleReference())
        // The matcher itself rejects a 0.1-confidence candidate is not the point here — if it
        // matched, the confidence floor turns geometry unavailable rather than reporting drift.
        assertFalse(result.subjectPositionMatch.available)
        assertFalse(result.subjectSizeMatch.available)
    }

    @Test
    fun `human reference uses person box for position when AI is live`() {
        val personBox = NormalizedRect(0.35f, 0.2f, 0.65f, 0.95f)
        val ai = vehicleAiProfile(strategy = ComparisonStrategy.HUMAN_STRATEGY).copy(
            sceneMode = SceneMode.HUMAN,
            primarySubjectType = PrimarySubjectType.PERSON,
            primarySubjectCategory = SubjectCategory.HUMAN,
            primarySubjectRawLabel = "person",
            primarySubjectBox = personBox,
            subjects = listOf(ReferenceAiSubject(SubjectCategory.HUMAN, "person", 0.9f, personBox)),
        )
        val reference = referenceProfile(withSubject = true, ai = ai)
        // Person walked right; face detection unchanged — position must follow the PERSON box.
        val movedPerson = NormalizedRect(0.55f, 0.2f, 0.85f, 0.95f)
        val snap = snapshot(
            subjects = listOf(liveSubject(SubjectCategory.HUMAN, movedPerson, rawLabel = "person")),
        )
        val input = matchingInput().copy(sceneSnapshot = snap, referenceEmbedding = referenceEmbedding())
        val result = compare(input, reference)
        assertEquals(ComparisonStrategy.HUMAN_STRATEGY, result.strategy)
        assertFalse(result.subjectPositionMatch.matched)
        assertEquals(ReferenceDriftDirection.RIGHT, result.subjectPositionMatch.direction)
        // Human extras stay enabled under HUMAN strategy.
        assertTrue(result.facePresenceMatch.enabled)
    }

    @Test
    fun `missing person suppresses face signals so it fires once`() {
        val personBox = NormalizedRect(0.35f, 0.2f, 0.65f, 0.95f)
        val ai = vehicleAiProfile(strategy = ComparisonStrategy.HUMAN_STRATEGY).copy(
            sceneMode = SceneMode.HUMAN,
            primarySubjectType = PrimarySubjectType.PERSON,
            primarySubjectCategory = SubjectCategory.HUMAN,
            primarySubjectRawLabel = "person",
            primarySubjectBox = personBox,
        )
        val reference = referenceProfile(withSubject = true, ai = ai)
        val snap = snapshot(subjects = emptyList())
        val input = matchingInput().copy(
            faces = faces(), eyes = eyes(0),
            sceneSnapshot = snap, referenceEmbedding = referenceEmbedding(),
        )
        val result = compare(input, reference)
        assertFalse(result.subjectPresenceMatch.matched)
        // SUBJECT_PRESENCE owns the drift; the face signals hold instead of double-alerting.
        assertFalse(result.facePresenceMatch.available)
        assertFalse(result.eyeVisibilityMatch.available)
    }

    @Test
    fun `generic scene strategy disables geometry but keeps similarity`() {
        val ai = vehicleAiProfile(strategy = ComparisonStrategy.GENERIC_SCENE_STRATEGY)
        val reference = referenceProfile(withSubject = false, ai = ai)
        val result = compare(matchingVehicleInput(), reference)
        assertFalse(result.subjectPositionMatch.enabled)
        assertFalse(result.subjectSizeMatch.enabled)
        assertFalse(result.subjectPresenceMatch.enabled)
        assertTrue(result.visualSimilarityMatch.enabled)
        assertTrue(result.visualSimilarityMatch.available)
    }
}
