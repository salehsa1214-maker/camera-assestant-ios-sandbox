package app.dyrecto.liveview.vision.modules

import android.graphics.Bitmap
import app.dyrecto.liveview.reference.ai.AiPerceptionCoordinator
import app.dyrecto.liveview.vision.FrameAnalysisRequest
import app.dyrecto.liveview.vision.VisionModule
import app.dyrecto.liveview.vision.results.SceneSnapshotResult
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Phase 10: live AI perception at adaptive cadence.
 *
 * THREADING IS THE POINT OF THIS CLASS: [VisionPipeline] runs all modules sequentially on one
 * worker, and AI inference costs 50–200 ms — so [analyze] NEVER runs inference inline. Per due
 * frame it only (1) publishes the previously completed snapshot, (2) takes a small scaled ARGB
 * copy (~1–3 ms — the renderer owns the source bitmap, so inference must not hold it), and
 * (3) hands the copy to a private single-lane scope. A busy-guard keeps at most one inference
 * in flight; frames due while busy are counted as skipped (newest-wins at the inference level).
 * The per-frame ExposureModule is never delayed.
 */
class AiSceneModule(
    private val coordinator: AiPerceptionCoordinator,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    /** Injectable so JVM tests can avoid Bitmap statics. */
    private val scaledCopy: (Bitmap, Int) -> Bitmap = { bmp, maxEdge -> defaultScaledCopy(bmp, maxEdge) },
    /** Injectable clock for deterministic tests. */
    private val clock: () -> Long = System::currentTimeMillis,
) : VisionModule {
    override val id: String = "ai_scene"

    @OptIn(ExperimentalCoroutinesApi::class)
    private val inferenceScope = CoroutineScope(SupervisorJob() + dispatcher.limitedParallelism(1))
    private val busy = AtomicBoolean(false)
    private val completed = AtomicReference<SceneSnapshotResult?>(null)
    private val skippedInferences = AtomicLong(0)

    /** Pipeline-worker-thread only. */
    private var frameCount = 0L

    override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> {
        // Publish the last finished inference regardless of what happens below — results
        // re-enter through the standard onResults → VisionContext merge, one frame late.
        val ready = completed.getAndSet(null)

        if (!coordinator.liveWorkNeeded) return listOfNotNull(ready)

        frameCount++
        val cadence = coordinator.cadence
        val trackerDue = frameCount % cadence.trackerEveryNFrames == 0L
        val embeddingDue = frameCount % cadence.embeddingEveryNFrames == 0L
        if (trackerDue || embeddingDue) {
            if (busy.compareAndSet(false, true)) {
                val source = request.bitmap
                val copy = if (source.isRecycled) null else runCatching {
                    scaledCopy(source, cadence.inferenceMaxEdgePx)
                }.getOrNull()
                if (copy == null) {
                    busy.set(false)
                } else {
                    inferenceScope.launch {
                        try {
                            val snapshot = coordinator.processLiveFrame(
                                bitmap = copy,
                                runTracker = trackerDue,
                                runEmbedding = embeddingDue,
                                nowMs = clock(),
                            )
                            completed.set(
                                SceneSnapshotResult(
                                    moduleId = id,
                                    snapshot = snapshot,
                                    skippedInferences = skippedInferences.get(),
                                ),
                            )
                        } finally {
                            busy.set(false)
                            copy.recycle()
                        }
                    }
                }
            } else {
                skippedInferences.incrementAndGet()
            }
        }
        return listOfNotNull(ready)
    }

    fun shutdown() {
        inferenceScope.cancel()
    }

    companion object {
        /**
         * ARGB_8888 copy scaled to [maxEdge] on the longest side. Always a COPY, even at equal
         * size — inference must never share the renderer's bitmap lifetime.
         */
        fun defaultScaledCopy(source: Bitmap, maxEdge: Int): Bitmap {
            val scale = maxEdge.toFloat() / max(source.width, source.height)
            if (scale >= 1f) return source.copy(Bitmap.Config.ARGB_8888, false)
            val w = max(1, (source.width * scale).roundToInt())
            val h = max(1, (source.height * scale).roundToInt())
            val scaled = Bitmap.createScaledBitmap(source, w, h, true)
            if (scaled.config == Bitmap.Config.ARGB_8888 && scaled !== source) return scaled
            val converted = scaled.copy(Bitmap.Config.ARGB_8888, false)
            if (scaled !== source) scaled.recycle()
            return converted
        }
    }
}
