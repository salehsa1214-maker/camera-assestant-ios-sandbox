package app.dyrecto.liveview.reference.ai

/**
 * A visual embedding of one image, produced by an [EmbeddingEngine]. Cosine similarity between
 * a reference embedding and a live embedding is the universal fallback comparison signal —
 * it works for any scene, with or without detectable objects.
 */
data class VisualEmbeddingResult(
    val embedding: FloatArray,
    /** Which model produced the vector — vectors from different models must never be compared. */
    val modelId: String,
    val confidence: Float,
) {
    val dimensions: Int get() = embedding.size

    // FloatArray fields break generated equals/hashCode; identity comparison is fine here
    // (results are single-producer, single-slot), but make the contract explicit.
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = embedding.contentHashCode()
}
