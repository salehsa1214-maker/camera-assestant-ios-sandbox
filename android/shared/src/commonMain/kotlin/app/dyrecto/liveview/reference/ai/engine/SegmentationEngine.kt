package app.dyrecto.liveview.reference.ai.engine

import app.dyrecto.platform.PlatformImage
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.SegmentationResult

/**
 * Segmentation seam — an OPTIONAL capability, invoked on demand only (never per-frame).
 * Same contract as the other engines: lazy load, latch FAILED on error, null instead of throw.
 */
interface SegmentationEngine {
    val capability: AiCapability

    suspend fun segment(bitmap: PlatformImage): SegmentationResult?
}
