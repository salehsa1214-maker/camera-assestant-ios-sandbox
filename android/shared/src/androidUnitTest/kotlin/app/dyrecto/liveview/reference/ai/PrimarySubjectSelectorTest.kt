package app.dyrecto.liveview.reference.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimarySubjectSelectorTest {

    @Test
    fun `one person becomes the primary subject`() {
        val result = PrimarySubjectSelector.select(
            listOf(subject(SubjectCategory.HUMAN, centeredBox(0.4f), confidence = 0.9f, rawLabel = "person")),
        )
        val primary = result.value!!
        assertEquals(PrimarySubjectType.PERSON, primary.type)
        assertEquals(SubjectCategory.HUMAN, primary.category)
        assertEquals(0.9f, result.confidence, 1e-4f)
        assertNotNull(primary.boundingBox)
    }

    @Test
    fun `one car becomes a vehicle subject`() {
        val result = PrimarySubjectSelector.select(
            listOf(subject(SubjectCategory.VEHICLE, centeredBox(0.5f), rawLabel = "car")),
        )
        assertEquals(PrimarySubjectType.VEHICLE, result.value!!.type)
    }

    @Test
    fun `one animal becomes an animal subject`() {
        val result = PrimarySubjectSelector.select(
            listOf(subject(SubjectCategory.ANIMAL, centeredBox(0.3f), rawLabel = "dog")),
        )
        assertEquals(PrimarySubjectType.ANIMAL, result.value!!.type)
    }

    @Test
    fun `dominant large object wins over a small companion`() {
        val large = subject(SubjectCategory.VEHICLE, centeredBox(0.6f), confidence = 0.85f, rawLabel = "car")
        val small = subject(SubjectCategory.HUMAN, box(0.0f, 0.0f, 0.1f, 0.15f), confidence = 0.8f)
        val result = PrimarySubjectSelector.select(listOf(small, large))
        val primary = result.value!!
        assertEquals(PrimarySubjectType.VEHICLE, primary.type)
    }

    @Test
    fun `two comparable subjects become MULTIPLE with a union box`() {
        val a = subject(SubjectCategory.HUMAN, box(0.05f, 0.3f, 0.35f, 0.8f), confidence = 0.85f)
        val b = subject(SubjectCategory.HUMAN, box(0.6f, 0.3f, 0.9f, 0.8f), confidence = 0.85f)
        val result = PrimarySubjectSelector.select(listOf(a, b))
        val primary = result.value!!
        assertEquals(PrimarySubjectType.MULTIPLE, primary.type)
        assertEquals(SubjectCategory.HUMAN, primary.category)
        val union = primary.boundingBox!!
        assertEquals(0.05f, union.left, 1e-4f)
        assertEquals(0.9f, union.right, 1e-4f)
    }

    @Test
    fun `mixed-category MULTIPLE reports UNKNOWN category`() {
        val person = subject(SubjectCategory.HUMAN, box(0.05f, 0.3f, 0.35f, 0.8f))
        val dog = subject(SubjectCategory.ANIMAL, box(0.6f, 0.4f, 0.9f, 0.8f))
        val result = PrimarySubjectSelector.select(listOf(person, dog))
        val primary = result.value!!
        assertEquals(PrimarySubjectType.MULTIPLE, primary.type)
        assertEquals(SubjectCategory.UNKNOWN, primary.category)
    }

    @Test
    fun `empty scene is a confident NONE, not an absent value`() {
        val result = PrimarySubjectSelector.select(emptyList())
        val primary = result.value!!
        assertEquals(PrimarySubjectType.NONE, primary.type)
        assertNull(primary.boundingBox)
        assertTrue(result.confidence > 0f)
    }

    @Test
    fun `low-confidence detections are ignored`() {
        val result = PrimarySubjectSelector.select(
            listOf(subject(SubjectCategory.HUMAN, centeredBox(0.4f), confidence = 0.2f)),
        )
        assertEquals(PrimarySubjectType.NONE, result.value!!.type)
    }
}
