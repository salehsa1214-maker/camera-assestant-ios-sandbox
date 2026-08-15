package app.dyrecto.liveview.reference.ai.segment

import app.dyrecto.platform.PlatformImage
import kotlin.concurrent.Volatile
import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.platform.nanoTime
import app.dyrecto.liveview.reference.ai.SegmentationResult
import app.dyrecto.liveview.reference.ai.SegmentationSource
import app.dyrecto.liveview.reference.ai.engine.SegmentationEngine

/**
 * Segmentation as an OPTIONAL, on-demand capability. Never part of the live loop: it is invoked
 * only when a strategy explicitly needs pixel-accurate masks (currently one-shot mask statistics
 * at reference-analysis time). When the model is unavailable, [bboxFallback] keeps callers
 * mask-shaped without pixel accuracy — the architecture stays segmentation-ready at zero cost.
 */
class SegmentationManager(
    private val engine: SegmentationEngine,
) {
    val capability: AiCapability get() = engine.capability

    @Volatile var lastDurationMs: Long = 0L
        private set

    suspend fun segment(bitmap: PlatformImage): SegmentationResult? {
        val startNs = nanoTime()
        val result = engine.segment(bitmap)
        lastDurationMs = (nanoTime() - startNs) / 1_000_000
        return result
    }

    /** Mask surrogate built from a subject bounding box (no pixel accuracy). */
    fun bboxFallback(box: NormalizedRect): SegmentationResult = SegmentationResult(
        maskAvailable = false,
        coverage = box.area,
        maskBox = box,
        source = SegmentationSource.BBOX_FALLBACK,
    )
}
