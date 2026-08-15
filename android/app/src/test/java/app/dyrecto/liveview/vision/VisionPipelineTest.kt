package app.dyrecto.liveview.vision

import android.graphics.Bitmap
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Pure-JVM tests for [VisionPipeline]. The worker runs on a [StandardTestDispatcher] tied to the
 * test scheduler, so nothing executes until `advanceUntilIdle()` — `submit()` is synchronous, which
 * lets us deterministically force newest-wins drops by submitting multiple frames before the worker
 * gets a chance to run. Bitmaps are Mockito stubs (only their reference identity matters here).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VisionPipelineTest {

    private fun req() = FrameAnalysisRequest(
        bitmap = mock(Bitmap::class.java),
        context = FrameContext(),
        receivedAtMs = 0L,
    )

    /** Records every frame it sees, in order. Returns emptyList() — tests don't need results. */
    private class RecordingModule(override val id: String = "rec") : VisionModule {
        val seen = mutableListOf<FrameAnalysisRequest>()
        override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> {
            seen += request
            return emptyList()
        }
    }

    /** Always throws, to verify isolation. */
    private class ThrowingModule(override val id: String = "boom") : VisionModule {
        var calls = 0
        override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> {
            calls++
            throw IllegalStateException("intentional")
        }
    }

    // ---- registration ----

    @Test fun registerUnregisterClearReflectedInStats() = runTest {
        val pipeline = VisionPipeline(StandardTestDispatcher(testScheduler))
        val a = RecordingModule("a")
        val b = RecordingModule("b")

        pipeline.register(a)
        pipeline.register(b)
        assertEquals(2, pipeline.stats.value.registeredModules)

        pipeline.register(a) // idempotent (addIfAbsent)
        assertEquals(2, pipeline.stats.value.registeredModules)

        pipeline.unregister(a)
        assertEquals(1, pipeline.stats.value.registeredModules)

        pipeline.clear()
        assertEquals(0, pipeline.stats.value.registeredModules)
        pipeline.shutdown()
    }

    // ---- latest-frame-wins + drop counting + no duplicate analysis ----

    @Test fun latestFrameWinsAndCountsDrops() = runTest {
        val pipeline = VisionPipeline(StandardTestDispatcher(testScheduler))
        val mod = RecordingModule()
        pipeline.register(mod)

        val r1 = req()
        val r2 = req()
        pipeline.submit(r1)
        pipeline.submit(r2) // displaces r1 before the worker runs -> r1 dropped
        advanceUntilIdle()

        val s = pipeline.stats.value
        assertEquals(2, s.framesReceived)
        assertEquals(1, s.framesAnalyzed)        // exactly one analysis (no duplicate)
        assertEquals(1, s.framesDropped)
        assertEquals(listOf(r2), mod.seen)       // newest won
        pipeline.shutdown()
    }

    // ---- bounded slot: depth never exceeds 1 regardless of burst size ----

    @Test fun boundedSlotCollapsesBurstToOne() = runTest {
        val pipeline = VisionPipeline(StandardTestDispatcher(testScheduler))
        val mod = RecordingModule()
        pipeline.register(mod)

        val last = req()
        repeat(9) { pipeline.submit(req()) }
        pipeline.submit(last)
        advanceUntilIdle()

        val s = pipeline.stats.value
        assertEquals(10, s.framesReceived)
        assertEquals(9, s.framesDropped)         // only the final frame survived the slot
        assertEquals(1, s.framesAnalyzed)
        assertEquals(listOf(last), mod.seen)
        pipeline.shutdown()
    }

    // ---- module exception isolation + pipeline survives exceptions ----

    @Test fun throwingModuleIsIsolatedAndPipelineSurvives() = runTest {
        val pipeline = VisionPipeline(StandardTestDispatcher(testScheduler))
        val boom = ThrowingModule()
        val peer = RecordingModule("peer")
        pipeline.register(boom)
        pipeline.register(peer)

        pipeline.submit(req())
        advanceUntilIdle()
        // Peer still ran despite the throwing module; pipeline counted the analysis.
        assertEquals(1, peer.seen.size)
        assertEquals(1, boom.calls)
        assertEquals(1, pipeline.stats.value.framesAnalyzed)

        // A subsequent frame is still analyzed — one bad module never stalls the worker.
        pipeline.submit(req())
        advanceUntilIdle()
        assertEquals(2, peer.seen.size)
        assertEquals(2, pipeline.stats.value.framesAnalyzed)
        pipeline.shutdown()
    }

    // ---- shutdown stops the worker ----

    @Test fun shutdownStopsFurtherAnalysis() = runTest {
        val pipeline = VisionPipeline(StandardTestDispatcher(testScheduler))
        val mod = RecordingModule()
        pipeline.register(mod)

        pipeline.submit(req())
        advanceUntilIdle()
        assertEquals(1, pipeline.stats.value.framesAnalyzed)
        assertTrue(pipeline.stats.value.pipelineRunning)

        pipeline.shutdown()
        assertFalse(pipeline.stats.value.pipelineRunning)

        pipeline.submit(req())
        advanceUntilIdle()
        // Worker is dead: no further analysis happens.
        assertEquals(1, pipeline.stats.value.framesAnalyzed)
        assertEquals(1, mod.seen.size)
    }
}
