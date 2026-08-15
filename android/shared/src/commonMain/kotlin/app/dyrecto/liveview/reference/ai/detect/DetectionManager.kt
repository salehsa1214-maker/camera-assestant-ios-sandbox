package app.dyrecto.liveview.reference.ai.detect

import app.dyrecto.platform.PlatformImage
import kotlin.concurrent.Volatile
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.platform.nanoTime
import app.dyrecto.liveview.reference.ai.engine.ObjectDetectorEngine
import app.dyrecto.liveview.reference.ai.semantic.SemanticMapper
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject

/**
 * Runs object detection ON DEMAND and converts raw detections to semantic [SceneSubject]s.
 *
 * Single responsibility: engine invocation + semantic mapping. It has no cadence and no
 * schedule — WHEN detection runs is decided by TrackingManager's trigger policy (init / loss
 * recovery / scene change / periodic verify) or by one-shot reference analysis. Raw detector
 * labels stop here: everything downstream sees [SceneSubject.category]; the raw label is
 * carried only as a diagnostics field.
 */
class DetectionManager(
    private val engine: ObjectDetectorEngine,
    private val mapper: SemanticMapper,
) {
    val capability: AiCapability get() = engine.capability

    /** Diagnostics: duration of the most recent detector pass. */
    @Volatile var lastDurationMs: Long = 0L
        private set

    /** Diagnostics: how many detector passes ran this process (should stay LOW while tracking). */
    @Volatile var runCount: Long = 0L
        private set

    /** Unverified detections carry no track identity yet. */
    suspend fun detect(bitmap: PlatformImage): List<SceneSubject> {
        val startNs = nanoTime()
        val raw = engine.detect(bitmap)
        lastDurationMs = (nanoTime() - startNs) / 1_000_000
        runCount++
        return raw.map { detection ->
            SceneSubject(
                trackId = SceneSubject.NO_TRACK,
                category = mapper.map(detection.className, detection.classId),
                rawLabel = detection.className,
                confidence = detection.confidence,
                boundingBox = detection.boundingBox,
                tracked = false,
                maskAvailable = false,
            )
        }
    }
}
