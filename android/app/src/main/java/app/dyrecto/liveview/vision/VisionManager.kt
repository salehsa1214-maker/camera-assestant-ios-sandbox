package app.dyrecto.liveview.vision

import android.graphics.Bitmap
import app.dyrecto.connection.BleLog
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.modules.ExposureModule
import app.dyrecto.liveview.vision.modules.FaceAndEyeDetectionModule
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.FalseColorResult
import app.dyrecto.liveview.vision.results.FocusPeakingResult
import app.dyrecto.liveview.vision.results.WaveformResult
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.SceneSnapshotResult
import app.dyrecto.liveview.vision.results.SubjectExposureResult
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.liveview.vision.results.ZebraResult
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thin coordinator over one [VisionPipeline]. Single entry point for the rest of the app:
 * receive a rendered [Bitmap] + its [FrameContext], build a [FrameAnalysisRequest], submit it.
 *
 * Responsibilities:
 *  - register Phase 5B modules at construction time
 *  - receive the flat [List]<[VisionResult]> from the pipeline via [onResults]
 *  - merge each result into [_visionContext] (each result type goes into its own slot)
 *  - publish [visionContext] as a [StateFlow] for the UI and future consumers
 *  - log [VISION-CTX] once per second (throttled)
 *
 * VisionManager has no knowledge of the pipeline's internal mechanics — it only sees the
 * finished result list. VisionPipeline has no knowledge of VisionContext or state publishing.
 */
class VisionManager(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val pipeline = VisionPipeline(dispatcher, onResults = ::onResults)

    init {
        pipeline.register(ExposureModule())
        pipeline.register(FaceAndEyeDetectionModule())
    }

    /** Live pipeline health for diagnostics (Developer UI). */
    val statistics: StateFlow<VisionStatistics> get() = pipeline.stats

    /** Aggregated latest results from all Vision modules (Developer UI + future consumers). */
    private val _visionContext = MutableStateFlow(VisionContext())
    val visionContext: StateFlow<VisionContext> = _visionContext.asStateFlow()

    /** Submit one rendered frame (newest-wins; never blocks the caller). */
    fun submitFrame(bitmap: Bitmap, context: FrameContext) {
        pipeline.submit(
            FrameAnalysisRequest(
                bitmap = bitmap,
                context = context,
                receivedAtMs = System.currentTimeMillis(),
            ),
        )
    }

    fun register(module: VisionModule) = pipeline.register(module)
    fun unregister(module: VisionModule) = pipeline.unregister(module)
    fun clear() = pipeline.clear()
    fun shutdown() = pipeline.shutdown()

    // ---- Result collection (called on the pipeline's worker thread) ----

    private fun onResults(results: List<VisionResult>) {
        var ctx = _visionContext.value
        for (result in results) {
            ctx = when (result) {
                is HistogramResult     -> ctx.copy(histogram = result)
                is ZebraResult         -> ctx.copy(zebra = result)
                is ExposureResult      -> ctx.copy(exposure = result)
                is FaceDetectionResult -> ctx.copy(faces = result)
                is EyeDetectionResult  -> ctx.copy(eyes = result)
                is ColorStatsResult    -> ctx.copy(colorStats = result)
                is SceneSnapshotResult -> ctx.copy(aiScene = result)
                is SubjectExposureResult -> ctx.copy(subjectExposure = result)
                is WaveformResult      -> ctx.copy(waveform = result)
                is FalseColorResult    -> ctx.copy(falseColor = result)
                is FocusPeakingResult  -> ctx.copy(focusPeaking = result)
                else                   -> ctx
            }
        }
        _visionContext.value = ctx.copy(updatedAtMs = System.currentTimeMillis())
        maybeLogContext()
    }

    @Volatile private var lastContextLogAtMs: Long = 0L

    private fun maybeLogContext() {
        val now = System.currentTimeMillis()
        if (now - lastContextLogAtMs < 1000L) return
        lastContextLogAtMs = now
        val ctx = _visionContext.value
        BleLog.line(
            BleLog.Kind.INFO,
            "[VISION-CTX] highlight=%.1f%% shadow=%.1f%% stride=%d faces=%d eyes=%d".format(
                ctx.exposure?.highlightCoverage ?: 0f,
                ctx.exposure?.shadowCoverage ?: 0f,
                ctx.histogram?.effectiveStride ?: 1,
                ctx.faces?.facesDetected ?: 0,
                ctx.eyes?.eyesDetected ?: 0,
            ),
        )
    }
}
