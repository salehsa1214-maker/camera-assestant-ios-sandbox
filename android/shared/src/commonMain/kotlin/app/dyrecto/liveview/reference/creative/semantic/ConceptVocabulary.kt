package app.dyrecto.liveview.reference.creative.semantic

import app.dyrecto.liveview.reference.creative.CreativeAspect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Phase 16.1 — the bundled creative-concept vocabulary. Each entry is a photographer concept with a
 * precomputed, L2-normalized MobileCLIP **text** embedding (produced OFFLINE by the text tower over
 * a curated prompt list — the text tower is never shipped or run on device). At import we score the
 * reference's image embedding against every entry by cosine similarity.
 *
 * Pure Kotlin / JVM-testable: parsing and scoring do no Android I/O. The Android layer reads the
 * asset bytes and hands the JSON string to [fromJson]; a malformed / dimension-mismatched vocabulary
 * yields null so the semantic expert degrades gracefully.
 */
class ConceptVocabulary private constructor(
    val modelId: String,
    val dimensions: Int,
    val entries: List<Entry>,
) {
    class Entry(
        val label: String,
        val aspect: CreativeAspect?,
        val family: String,
        /** L2-normalized concept text embedding. */
        val vector: FloatArray,
    )

    /**
     * Cosine similarity of [embedding] against every entry, sorted descending. Vectors are stored
     * L2-normalized, so cosine reduces to a dot product. Returns an empty list on a dimension
     * mismatch or empty input — the caller then treats semantics as unavailable.
     */
    fun score(embedding: FloatArray?): List<ConceptScore> {
        if (embedding == null || embedding.size != dimensions || dimensions == 0) return emptyList()
        val norm = l2Norm(embedding)
        if (norm == 0f) return emptyList()
        return entries
            .map { entry ->
                val dot = dot(embedding, entry.vector)
                ConceptScore(
                    label = entry.label,
                    score = (dot / norm).coerceIn(-1f, 1f),
                    aspect = entry.aspect,
                    family = entry.family,
                )
            }
            .sortedByDescending { it.score }
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Parse a vocabulary from its JSON asset; null on any malformed / inconsistent content. */
        fun fromJson(text: String): ConceptVocabulary? = runCatching {
            val dto = json.decodeFromString(VocabularyDto.serializer(), text)
            if (dto.dimensions <= 0 || dto.concepts.isEmpty()) return null
            val entries = dto.concepts.mapNotNull { c ->
                val vec = c.vector
                if (vec.size != dto.dimensions) return@mapNotNull null
                Entry(
                    label = c.label,
                    aspect = parseAspect(c.aspect),
                    family = c.family,
                    vector = normalize(FloatArray(vec.size) { vec[it] }),
                )
            }
            if (entries.isEmpty()) null else ConceptVocabulary(dto.modelId, dto.dimensions, entries)
        }.getOrNull()

        private fun parseAspect(raw: String?): CreativeAspect? {
            if (raw.isNullOrBlank()) return null
            return runCatching { CreativeAspect.valueOf(raw.trim().uppercase()) }.getOrNull()
        }

        private fun normalize(v: FloatArray): FloatArray {
            val n = l2Norm(v)
            if (n == 0f) return v
            for (i in v.indices) v[i] = v[i] / n
            return v
        }

        private fun l2Norm(v: FloatArray): Float {
            var sum = 0.0
            for (x in v) sum += x.toDouble() * x
            return kotlin.math.sqrt(sum).toFloat()
        }

        private fun dot(a: FloatArray, b: FloatArray): Float {
            var sum = 0.0
            val n = minOf(a.size, b.size)
            for (i in 0 until n) sum += a[i].toDouble() * b[i]
            return sum.toFloat()
        }
    }

    // ---- JSON DTOs (asset schema) ---------------------------------------------------------------

    @Serializable
    private data class VocabularyDto(
        @SerialName("modelId") val modelId: String = "",
        @SerialName("dimensions") val dimensions: Int = 0,
        @SerialName("concepts") val concepts: List<ConceptDto> = emptyList(),
    )

    @Serializable
    private data class ConceptDto(
        val label: String,
        val aspect: String? = null,
        val family: String = "",
        val vector: List<Float> = emptyList(),
    )
}
