package app.dyrecto.liveview.reference.ai

import kotlin.math.sqrt

/** Pure vector math for embedding comparison (JVM-testable, no Android/ML types). */
object EmbeddingMath {
    /**
     * Cosine similarity in [-1, 1]. Defensive contract: mismatched dimensions or a zero-norm
     * vector return 0 (treated as "no similarity evidence") — callers gate real compatibility
     * through EmbeddingManager.canCompare, this is just the last line of defense against
     * producing garbage.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return 0f
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i].toDouble() * b[i]
            normA += a[i].toDouble() * a[i]
            normB += b[i].toDouble() * b[i]
        }
        if (normA == 0.0 || normB == 0.0) return 0f
        return (dot / (sqrt(normA) * sqrt(normB))).toFloat()
    }
}
