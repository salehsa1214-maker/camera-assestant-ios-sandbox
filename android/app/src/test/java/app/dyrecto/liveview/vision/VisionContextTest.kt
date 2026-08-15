package app.dyrecto.liveview.vision

import android.graphics.Bitmap
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.ExposureVerdict
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Tests that [VisionManager] merges [VisionResult]s into [app.dyrecto.liveview.vision.results.VisionContext] correctly:
 *  - each result type is placed in its own slot
 *  - unrelated slots are preserved when a single module result arrives
 *  - a second result for the same type replaces the previous one
 *
 * Uses a [StandardTestDispatcher] so the pipeline worker is deterministic.
 * [VisionManager.clear] removes the default modules registered in the constructor,
 * letting us inject controlled [FixedModule]s instead.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VisionContextTest {

    private fun req() = FrameAnalysisRequest(
        bitmap = mock(Bitmap::class.java),
        context = FrameContext(),
        receivedAtMs = 0L,
    )

    private class FixedModule(
        override val id: String,
        private val result: VisionResult,
    ) : VisionModule {
        override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> =
            listOf(result)
    }

    private fun exposureResult(highlightCoverage: Float) = ExposureResult(
        moduleId = "exposure",
        highlightDetected = highlightCoverage > 1f,
        shadowDetected = false,
        highlightCoverage = highlightCoverage,
        shadowCoverage = 0f,
        exposureState = ExposureVerdict.NORMAL,
        exposureConfidence = 1f,
    )

    @Test fun exposureResultAppearsInContextAndOtherSlotsRemainNull() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = VisionManager(dispatcher)
        manager.clear()

        val result = exposureResult(42f)
        manager.register(FixedModule("exposure", result))

        manager.submitFrame(mock(Bitmap::class.java), FrameContext())
        advanceUntilIdle()

        val ctx = manager.visionContext.value
        assertEquals(result, ctx.exposure)
        assertNull(ctx.histogram)
        assertNull(ctx.zebra)
        assertNull(ctx.faces)
        assertNull(ctx.eyes)

        manager.shutdown()
    }

    @Test fun latestResultReplacesOlderResult() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val manager = VisionManager(dispatcher)
        manager.clear()

        val first = exposureResult(10f)
        val second = exposureResult(90f)

        val mod = object : VisionModule {
            override val id = "exposure"
            var toReturn: VisionResult = first
            override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> =
                listOf(toReturn)
        }
        manager.register(mod)

        manager.submitFrame(mock(Bitmap::class.java), FrameContext())
        advanceUntilIdle()
        assertEquals(first, manager.visionContext.value.exposure)

        mod.toReturn = second
        manager.submitFrame(mock(Bitmap::class.java), FrameContext())
        advanceUntilIdle()
        assertEquals(second, manager.visionContext.value.exposure)

        manager.shutdown()
    }
}
