package app.dyrecto.liveview.reference

import kotlinx.serialization.Serializable

/**
 * Versioned reference embedding — stored as its OWN file, never inside [ReferenceProfile]
 * (the profile carries only `embeddingId` + `embeddingModelId`). A future embedding model with
 * different dimensions therefore never forces a profile schema change: the stored vector is
 * simply rejected as incompatible (signal unavailable) until the reference is re-analyzed.
 */
@Serializable
data class ReferenceEmbedding(
    val id: String,
    /** Which model produced the vector — vectors across models are never comparable. */
    val modelId: String,
    val dimensions: Int,
    val values: List<Float>,
    val createdAtMs: Long,
    val schemaVersion: Int = 1,
) {
    fun vector(): FloatArray = FloatArray(values.size) { values[it] }
}
