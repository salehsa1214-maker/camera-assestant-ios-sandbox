package app.dyrecto.liveview.reference.ai.engine

import app.dyrecto.platform.PlatformImage
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.VisualEmbeddingResult

/**
 * Image-embedding seam. Same contract as [ObjectDetectorEngine]: lazy load, latch FAILED on
 * error, return null instead of throwing. Vectors are L2-normalized by the implementation so
 * cosine similarity is a plain dot product.
 */
interface EmbeddingEngine {
    val capability: AiCapability

    suspend fun embed(bitmap: PlatformImage): VisualEmbeddingResult?
}
