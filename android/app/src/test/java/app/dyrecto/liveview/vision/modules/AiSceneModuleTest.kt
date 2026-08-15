package app.dyrecto.liveview.vision.modules

import android.graphics.Bitmap
import app.dyrecto.liveview.reference.ai.AiCadenceConfig
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.AiPerceptionCoordinator
import app.dyrecto.liveview.reference.ai.DetectedObject
import app.dyrecto.liveview.reference.ai.EngineStatus
import app.dyrecto.liveview.reference.ai.SegmentationResult
import app.dyrecto.liveview.reference.ai.VisualEmbeddingResult
import app.dyrecto.liveview.reference.ai.detect.DetectionManager
import app.dyrecto.liveview.reference.ai.embed.EmbeddingManager
import app.dyrecto.liveview.reference.ai.engine.EmbeddingEngine
import app.dyrecto.liveview.reference.ai.engine.ObjectDetectorEngine
import app.dyrecto.liveview.reference.ai.engine.SegmentationEngine
import app.dyrecto.liveview.reference.ai.segment.SegmentationManager
import app.dyrecto.liveview.reference.ai.semantic.CocoSemanticMapper
import app.dyrecto.liveview.reference.ai.strategy.StrategyManager
import app.dyrecto.liveview.reference.ai.track.LumaFrame
import app.dyrecto.liveview.reference.ai.track.NccTemplateTracker
import app.dyrecto.liveview.reference.ai.track.TrackingManager
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.FrameAnalysisRequest
import app.dyrecto.liveview.vision.results.SceneSnapshotResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

/**
 * Proves the module's threading contract with scripted fake engines: analyze() never waits on
 * inference, at most one inference is in flight (due-but-busy frames are counted), and finished
 * snapshots are published on the NEXT analyzed frame (newest-wins).
 */
class AiSceneModuleTest {

    private class FakeDetector : ObjectDetectorEngine {
        override val capability = AiCapability("fake-detector", EngineStatus.READY)
        override suspend fun detect(bitmap: Bitmap): List<DetectedObject> = emptyList()
    }

    private class GatedEmbedder : EmbeddingEngine {
        override val capability = AiCapability("fake-embedder", EngineStatus.READY)
        var gate: CompletableDeferred<Unit>? = null
        var invocations = 0

        override suspend fun embed(bitmap: Bitmap): VisualEmbeddingResult? {
            invocations++
            gate?.await()
            return VisualEmbeddingResult(floatArrayOf(1f, 0f), "fake-embedder", 1f)
        }
    }

    private class FakeSegmenter : SegmentationEngine {
        override val capability = AiCapability("fake-segmenter", EngineStatus.READY)
        override suspend fun segment(bitmap: Bitmap): SegmentationResult? = null
    }

    private fun coordinator(embedder: GatedEmbedder): AiPerceptionCoordinator =
        AiPerceptionCoordinator(
            detection = DetectionManager(FakeDetector(), CocoSemanticMapper),
            embedding = EmbeddingManager(embedder),
            segmentation = SegmentationManager(FakeSegmenter()),
            strategy = StrategyManager(),
            tracking = TrackingManager(NccTemplateTracker()),
            cadence = AiCadenceConfig(trackerEveryNFrames = 1, embeddingEveryNFrames = 1),
            lumaExtractor = { _, _ -> LumaFrame(8, 8, IntArray(64)) },
        )

    private fun request(): FrameAnalysisRequest = FrameAnalysisRequest(
        bitmap = Mockito.mock(Bitmap::class.java),
        context = Mockito.mock(FrameContext::class.java),
        receivedAtMs = 0L,
    )

    @Test
    fun `analyze never blocks on inference and publishes on a later frame`() = runTest {
        val embedder = GatedEmbedder()
        val gate = CompletableDeferred<Unit>()
        embedder.gate = gate
        val coordinator = coordinator(embedder)
        coordinator.setLiveMonitoring(expected = null, active = true)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val module = AiSceneModule(
            coordinator = coordinator,
            dispatcher = dispatcher,
            scaledCopy = { bmp, _ -> bmp },
            clock = { 1_000L },
        )

        // Frame 1: work is due, inference starts but is gated — analyze returns immediately.
        assertTrue(module.analyze(request()).isEmpty())
        testScheduler.runCurrent() // start the lane job; it suspends at the gate
        assertEquals(1, embedder.invocations)
        // Frame 2: still busy — this due frame is skipped, analyze stays non-blocking.
        assertTrue(module.analyze(request()).isEmpty())

        gate.complete(Unit)
        advanceUntilIdle()

        // Frame 3: the finished snapshot from frame 1 is published now.
        val results = module.analyze(request())
        assertEquals(1, results.size)
        val result = results.single() as SceneSnapshotResult
        assertEquals("ai_scene", result.moduleId)
        assertEquals(1_000L, result.snapshot.analyzedAtMs)
        assertTrue("skipped=${result.skippedInferences}", result.skippedInferences >= 1)

        module.shutdown()
    }

    @Test
    fun `only one inference is ever in flight`() = runTest {
        val embedder = GatedEmbedder()
        val gate = CompletableDeferred<Unit>()
        embedder.gate = gate
        val coordinator = coordinator(embedder)
        coordinator.setLiveMonitoring(expected = null, active = true)
        val module = AiSceneModule(
            coordinator = coordinator,
            dispatcher = StandardTestDispatcher(testScheduler),
            scaledCopy = { bmp, _ -> bmp },
        )

        repeat(10) {
            module.analyze(request())
            testScheduler.runCurrent()
        }
        // The gated first inference blocks the lane; nothing else may have started.
        assertEquals(1, embedder.invocations)

        gate.complete(Unit)
        advanceUntilIdle()
        module.shutdown()
    }

    @Test
    fun `inactive monitoring does no AI work at all`() = runTest {
        val embedder = GatedEmbedder()
        val coordinator = coordinator(embedder)
        var copies = 0
        val module = AiSceneModule(
            coordinator = coordinator,
            dispatcher = StandardTestDispatcher(testScheduler),
            scaledCopy = { bmp, _ -> copies++; bmp },
        )

        repeat(5) { assertTrue(module.analyze(request()).isEmpty()) }
        advanceUntilIdle()
        assertEquals(0, copies)
        assertEquals(0, embedder.invocations)
        module.shutdown()
    }
}
