package app.dyrecto.liveview.reference.ai

import android.graphics.Bitmap
import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.detect.DetectionManager
import app.dyrecto.liveview.reference.ai.embed.EmbeddingManager
import app.dyrecto.liveview.reference.ai.engine.EmbeddingEngine
import app.dyrecto.liveview.reference.ai.engine.ObjectDetectorEngine
import app.dyrecto.liveview.reference.ai.engine.SegmentationEngine
import app.dyrecto.liveview.reference.ai.segment.SegmentationManager
import app.dyrecto.liveview.reference.ai.semantic.CocoSemanticMapper
import app.dyrecto.liveview.reference.ai.snapshot.SnapshotSource
import app.dyrecto.liveview.reference.ai.strategy.StrategyManager
import app.dyrecto.liveview.reference.ai.track.LumaFrame
import app.dyrecto.liveview.reference.ai.track.NccTemplateTracker
import app.dyrecto.liveview.reference.ai.track.ObjectTracker
import app.dyrecto.liveview.reference.ai.track.TrackHandle
import app.dyrecto.liveview.reference.ai.track.TrackUpdate
import app.dyrecto.liveview.reference.ai.track.TrackingManager
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

/**
 * Model-failure fallbacks and the live detect→track lifecycle, exercised with fake engines —
 * the coordinator must NEVER throw and must degrade to whatever capabilities survive.
 */
class AiPerceptionCoordinatorTest {

    private val carBox = NormalizedRect(0.3f, 0.3f, 0.7f, 0.7f)

    private class FakeDetector(
        var objects: List<DetectedObject> = emptyList(),
        private val failed: Boolean = false,
        private val throws: Boolean = false,
    ) : ObjectDetectorEngine {
        override val capability: AiCapability
            get() = AiCapability(
                "fake-detector",
                if (failed) EngineStatus.FAILED else EngineStatus.READY,
                if (failed) "simulated load failure" else null,
            )

        override suspend fun detect(bitmap: Bitmap): List<DetectedObject> {
            if (throws) error("engine blew up")
            return if (failed) emptyList() else objects
        }
    }

    private class FakeEmbedder(
        var vector: FloatArray? = floatArrayOf(1f, 0f, 0f, 0f),
        private val failed: Boolean = false,
    ) : EmbeddingEngine {
        override val capability: AiCapability
            get() = AiCapability(
                "fake-embedder",
                if (failed) EngineStatus.FAILED else EngineStatus.READY,
            )

        override suspend fun embed(bitmap: Bitmap): VisualEmbeddingResult? =
            if (failed) null else vector?.let { VisualEmbeddingResult(it, "fake-embedder", 1f) }
    }

    private class DeadSegmenter : SegmentationEngine {
        override val capability = AiCapability("fake-segmenter", EngineStatus.FAILED, "no model")
        override suspend fun segment(bitmap: Bitmap): SegmentationResult? = null
    }

    /** Always finds the subject where it was anchored, at the given confidence. */
    private class ScriptedTracker(var confidence: Float = 0.9f) : ObjectTracker {
        override val id = "scripted"
        class Handle(var box: NormalizedRect) : TrackHandle
        override fun init(frame: LumaFrame, box: NormalizedRect): TrackHandle = Handle(box)
        override fun update(handle: TrackHandle, frame: LumaFrame): TrackUpdate =
            TrackUpdate((handle as Handle).box, confidence)
        override fun refresh(handle: TrackHandle, frame: LumaFrame, box: NormalizedRect) {
            (handle as Handle).box = box
        }
        override fun release(handle: TrackHandle) {}
    }

    private fun coordinator(
        detector: ObjectDetectorEngine = FakeDetector(objects = listOf(carDetection())),
        embedder: EmbeddingEngine = FakeEmbedder(),
        tracker: ObjectTracker = ScriptedTracker(),
    ) = AiPerceptionCoordinator(
        detection = DetectionManager(detector, CocoSemanticMapper),
        embedding = EmbeddingManager(embedder),
        segmentation = SegmentationManager(DeadSegmenter()),
        strategy = StrategyManager(),
        tracking = TrackingManager(tracker),
        lumaExtractor = { _, _ -> LumaFrame(8, 8, IntArray(64)) },
    )

    private fun carDetection() = DetectedObject("car", 2, 0.9f, carBox)

    private fun bitmap(): Bitmap = Mockito.mock(Bitmap::class.java)

    @Test
    fun `reference analysis with all models produces a full snapshot`() = runTest {
        val snapshot = coordinator().analyzeReference(bitmap(), nowMs = 100L)
        assertEquals(1, snapshot.subjects.size)
        assertEquals(SubjectCategory.VEHICLE, snapshot.subjects[0].category)
        assertEquals(SceneMode.VEHICLE, snapshot.sceneMode.value)
        assertEquals(PrimarySubjectType.VEHICLE, snapshot.primarySubject.value?.type)
        assertTrue(snapshot.embedding.present)
        assertTrue(snapshot.layoutSignature.present)
        assertEquals(SnapshotSource.DETECTOR, snapshot.source)
    }

    @Test
    fun `detector down degrades to embedding-only GENERIC understanding`() = runTest {
        val c = coordinator(detector = FakeDetector(failed = true))
        val snapshot = c.analyzeReference(bitmap(), nowMs = 100L)
        assertTrue(snapshot.subjects.isEmpty())
        // Detector unknowable → primary subject absent (not a confident NONE).
        assertFalse(snapshot.primarySubject.present)
        assertEquals(SceneMode.GENERIC_SCENE, snapshot.sceneMode.value)
        assertTrue(snapshot.embedding.present)
        assertEquals(SnapshotSource.EMBEDDING_ONLY, snapshot.source)
        assertFalse(c.capabilities.value.detection.available)
        assertTrue(c.capabilities.value.embedding.available)
    }

    @Test
    fun `embedder down keeps detection understanding`() = runTest {
        val c = coordinator(embedder = FakeEmbedder(failed = true))
        val snapshot = c.analyzeReference(bitmap(), nowMs = 100L)
        assertEquals(SceneMode.VEHICLE, snapshot.sceneMode.value)
        assertFalse(snapshot.embedding.present)
        assertFalse(c.capabilities.value.embedding.available)
    }

    @Test
    fun `everything down yields an empty snapshot, never a crash`() = runTest {
        val c = coordinator(
            detector = FakeDetector(failed = true),
            embedder = FakeEmbedder(failed = true),
        )
        val snapshot = c.analyzeReference(bitmap(), nowMs = 100L)
        assertTrue(snapshot.subjects.isEmpty())
        assertFalse(snapshot.embedding.present)
        assertEquals(SnapshotSource.NONE, snapshot.source)
        assertEquals(SceneMode.UNKNOWN, snapshot.sceneMode.value)
    }

    @Test
    fun `a throwing engine is contained by the never-throw contract`() = runTest {
        val c = coordinator(detector = FakeDetector(throws = true))
        val snapshot = c.analyzeReference(bitmap(), nowMs = 100L)
        // The whole pass degraded but returned a snapshot object.
        assertNotNull(snapshot)
        c.setLiveMonitoring(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, carBox, "car"),
            active = true,
        )
        assertNotNull(c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = true, nowMs = 200L))
    }

    @Test
    fun `live monitoring detects once then tracks without further detector runs`() = runTest {
        val detector = FakeDetector(objects = listOf(carDetection()))
        val c = coordinator(detector = detector)
        c.setLiveMonitoring(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, carBox, "car"),
            active = true,
        )

        // Frame 1: INIT trigger → detector pass + track init.
        val first = c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = true, nowMs = 1_000L)
        assertEquals(SnapshotSource.DETECTOR, first.source)
        assertEquals(1, c.detection.runCount)
        assertTrue(first.subjects.any { it.tracked })

        // Frames 2..10 (within the 5s verify heartbeat): tracker only, ZERO detector runs.
        for (frame in 2..10) {
            val snapshot = c.processLiveFrame(
                bitmap(), runTracker = true, runEmbedding = false, nowMs = 1_000L + frame * 100L,
            )
            assertEquals(SnapshotSource.TRACKER, snapshot.source)
        }
        assertEquals(1, c.detection.runCount)

        // Past the heartbeat: exactly one verification pass.
        c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 7_000L)
        assertEquals(2, c.detection.runCount)
    }

    @Test
    fun `track loss triggers detector recovery`() = runTest {
        val detector = FakeDetector(objects = listOf(carDetection()))
        val tracker = ScriptedTracker()
        val c = coordinator(detector = detector, tracker = tracker)
        c.setLiveMonitoring(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, carBox, "car"),
            active = true,
        )
        c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 1_000L)
        assertEquals(1, c.detection.runCount)

        // Subject disappears for the tracker: confidence collapses until loss is declared,
        // then the very next frame re-detects (INIT after loss released the track).
        tracker.confidence = 0.05f
        detector.objects = emptyList() // and the detector can't find it either
        repeat(8) { frame ->
            c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 1_100L + frame * 100L)
        }
        assertTrue("detector should have re-run after loss", c.detection.runCount > 1)

        // Subject returns: detector finds it and tracking resumes.
        detector.objects = listOf(carDetection())
        tracker.confidence = 0.9f
        val recovered = c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 9_000L)
        assertTrue(recovered.subjects.any { it.tracked })
    }

    @Test
    fun `deactivating monitoring stops all live work`() = runTest {
        val c = coordinator()
        c.setLiveMonitoring(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, carBox, "car"),
            active = true,
        )
        c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 1_000L)
        assertTrue(c.liveWorkNeeded)
        c.setLiveMonitoring(null, active = false)
        assertFalse(c.liveWorkNeeded)
    }

    @Test
    fun `real NCC tracker slots in behind the same interface`() = runTest {
        // Sanity: the coordinator is agnostic to the tracker implementation.
        val c = coordinator(tracker = NccTemplateTracker())
        c.setLiveMonitoring(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, carBox, "car"),
            active = true,
        )
        assertNotNull(c.processLiveFrame(bitmap(), runTracker = true, runEmbedding = false, nowMs = 1_000L))
    }
}
