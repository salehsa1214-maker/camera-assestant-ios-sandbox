package app.dyrecto.liveview.reference.ai.embed

import app.dyrecto.platform.PlatformImage
import kotlin.concurrent.Volatile
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.platform.nanoTime
import app.dyrecto.liveview.reference.ai.VisualEmbeddingResult
import app.dyrecto.liveview.reference.ai.engine.EmbeddingEngine

/**
 * Produces global visual embeddings on demand (reference-time and cadenced live).
 *
 * Single responsibility: engine invocation + model-compatibility guarding. Vectors from a
 * different model (or with different dimensions) are never comparable — [canCompare] is the
 * gate that turns a stored-but-incompatible reference embedding into "signal unavailable"
 * instead of a garbage cosine value.
 */
class EmbeddingManager(
    private val engine: EmbeddingEngine,
) {
    val capability: AiCapability get() = engine.capability

    /** Diagnostics: duration of the most recent embedding pass. */
    @Volatile var lastDurationMs: Long = 0L
        private set

    @Volatile var runCount: Long = 0L
        private set

    suspend fun embed(bitmap: PlatformImage): VisualEmbeddingResult? {
        val startNs = nanoTime()
        val result = engine.embed(bitmap)
        lastDurationMs = (nanoTime() - startNs) / 1_000_000
        runCount++
        return result
    }

    /** True when a stored vector was produced by the live embedder (same model, same shape). */
    fun canCompare(storedModelId: String?, storedDimensions: Int, live: VisualEmbeddingResult?): Boolean =
        live != null &&
            storedModelId == live.modelId &&
            storedDimensions == live.dimensions
}
