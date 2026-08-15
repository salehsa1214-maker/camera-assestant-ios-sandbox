package app.dyrecto.liveview.reference.creative.semantic

import app.dyrecto.liveview.reference.creative.CreativeAspect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptVocabularyTest {

    private val json = """
        {
          "modelId": "test",
          "dimensions": 2,
          "concepts": [
            { "label": "a", "aspect": "LIGHTING", "family": "f", "vector": [1.0, 0.0] },
            { "label": "b", "family": "f", "vector": [0.0, 1.0] }
          ]
        }
    """.trimIndent()

    @Test
    fun `parses concepts, aspects, and dimensions`() {
        val vocab = ConceptVocabulary.fromJson(json)!!
        assertEquals(2, vocab.dimensions)
        assertEquals(2, vocab.entries.size)
        assertEquals(CreativeAspect.LIGHTING, vocab.entries.first { it.label == "a" }.aspect)
        assertNull(vocab.entries.first { it.label == "b" }.aspect)
    }

    @Test
    fun `scores by cosine and sorts descending`() {
        val vocab = ConceptVocabulary.fromJson(json)!!
        val scores = vocab.score(floatArrayOf(1f, 0f))
        assertEquals("a", scores.first().label)
        assertEquals(1f, scores.first().score, 1e-4f)
        assertEquals(0f, scores.first { it.label == "b" }.score, 1e-4f)
    }

    @Test
    fun `dimension mismatch yields no scores`() {
        val vocab = ConceptVocabulary.fromJson(json)!!
        assertTrue(vocab.score(floatArrayOf(1f, 0f, 0f)).isEmpty())
        assertTrue(vocab.score(null).isEmpty())
    }

    @Test
    fun `malformed or empty vocabulary is null`() {
        assertNull(ConceptVocabulary.fromJson("{ not json"))
        assertNull(ConceptVocabulary.fromJson("""{ "modelId":"x","dimensions":0,"concepts":[] }"""))
        // All concept vectors mismatch the declared dimension -> no usable entries -> null.
        assertNull(
            ConceptVocabulary.fromJson("""{ "modelId":"x","dimensions":4,"concepts":[{"label":"a","vector":[1.0,0.0]}] }"""),
        )
    }
}
