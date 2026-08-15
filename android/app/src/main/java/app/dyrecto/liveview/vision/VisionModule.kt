package app.dyrecto.liveview.vision

import app.dyrecto.liveview.vision.results.VisionResult

/**
 * A passive analyzer that receives rendered frames and returns a (possibly empty) list of results.
 *
 * Implementations run on the [VisionPipeline]'s own worker — never on the socket / parser /
 * renderer / UI thread. [analyze] is a suspend function so modules can use coroutine-native async
 * operations (e.g. ML Kit's [kotlinx.coroutines.tasks.await]) without blocking any thread.
 * It must not block or suspend for long — a slow module only delays Vision (frames are dropped
 * newest-wins), it must never touch rendering.
 *
 * The pipeline wraps every call in try/catch; a throwing module never stops the worker or its peers.
 *
 * A module that produces one analysis type returns `listOf(result)`. A module that produces
 * nothing (e.g. recycled bitmap) returns `emptyList()`. A module may return multiple results
 * (e.g. [app.dyrecto.liveview.vision.modules.FaceAndEyeDetectionModule] produces
 * both [app.dyrecto.liveview.vision.results.FaceDetectionResult] and
 * [app.dyrecto.liveview.vision.results.EyeDetectionResult] from one ML Kit pass).
 */
interface VisionModule {
    /** Stable identifier for diagnostics/logging and [VisionResult.moduleId]. */
    val id: String

    /**
     * Analyze one frame. Read-only over [request] (see [FrameAnalysisRequest]).
     * Returns a (possibly empty) list of [VisionResult]s. Exceptions are caught and isolated by
     * the pipeline — a throwing module never stops the pipeline or its peers.
     */
    suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult>
}
