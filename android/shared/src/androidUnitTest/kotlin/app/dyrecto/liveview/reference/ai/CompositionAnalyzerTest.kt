package app.dyrecto.liveview.reference.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionAnalyzerTest {

    @Test
    fun `empty scene produces an all-zero signature`() {
        val signature = CompositionAnalyzer.gridSignature(emptyList())
        assertEquals(CompositionAnalyzer.CELLS, signature.size)
        assertTrue(signature.all { it == 0f })
    }

    @Test
    fun `a centered subject occupies the center cell most`() {
        val signature = CompositionAnalyzer.gridSignature(
            listOf(subject(SubjectCategory.PRODUCT, centeredBox(0.3f), confidence = 1f)),
        )
        val center = signature[4]
        for ((i, value) in signature.withIndex()) {
            if (i != 4) assertTrue("cell $i ($value) should be <= center ($center)", value <= center)
        }
        assertTrue(center > 0f)
    }

    @Test
    fun `identical layouts have zero delta`() {
        val subjects = listOf(
            subject(SubjectCategory.HUMAN, box(0.1f, 0.2f, 0.4f, 0.9f)),
            subject(SubjectCategory.PRODUCT, box(0.6f, 0.5f, 0.8f, 0.7f)),
        )
        val a = CompositionAnalyzer.gridSignature(subjects)
        val b = CompositionAnalyzer.gridSignature(subjects)
        assertEquals(0f, CompositionAnalyzer.delta(a, b), 1e-6f)
    }

    @Test
    fun `a moved subject increases the delta`() {
        val before = CompositionAnalyzer.gridSignature(
            listOf(subject(SubjectCategory.PRODUCT, box(0.0f, 0.0f, 0.3f, 0.3f), confidence = 1f)),
        )
        val after = CompositionAnalyzer.gridSignature(
            listOf(subject(SubjectCategory.PRODUCT, box(0.7f, 0.7f, 1.0f, 1.0f), confidence = 1f)),
        )
        assertTrue(CompositionAnalyzer.delta(before, after) > 0.1f)
    }

    @Test
    fun `mismatched signature sizes report maximum delta`() {
        assertEquals(1f, CompositionAnalyzer.delta(FloatArray(9), FloatArray(4)), 0f)
    }
}
