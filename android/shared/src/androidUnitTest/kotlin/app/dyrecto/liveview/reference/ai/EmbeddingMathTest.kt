package app.dyrecto.liveview.reference.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class EmbeddingMathTest {

    @Test
    fun `identical vectors have similarity 1`() {
        val v = floatArrayOf(0.3f, -0.5f, 0.8f, 0.1f)
        assertEquals(1f, EmbeddingMath.cosineSimilarity(v, v), 1e-4f)
    }

    @Test
    fun `orthogonal vectors have similarity 0`() {
        val a = floatArrayOf(1f, 0f)
        val b = floatArrayOf(0f, 1f)
        assertEquals(0f, EmbeddingMath.cosineSimilarity(a, b), 1e-4f)
    }

    @Test
    fun `opposite vectors have similarity -1`() {
        val a = floatArrayOf(0.5f, -0.25f)
        val b = floatArrayOf(-0.5f, 0.25f)
        assertEquals(-1f, EmbeddingMath.cosineSimilarity(a, b), 1e-4f)
    }

    @Test
    fun `mismatched dimensions return 0 instead of throwing`() {
        assertEquals(0f, EmbeddingMath.cosineSimilarity(floatArrayOf(1f, 2f), floatArrayOf(1f)), 0f)
    }

    @Test
    fun `zero vectors return 0 instead of dividing by zero`() {
        assertEquals(0f, EmbeddingMath.cosineSimilarity(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f)), 0f)
        assertEquals(0f, EmbeddingMath.cosineSimilarity(FloatArray(0), FloatArray(0)), 0f)
    }
}
