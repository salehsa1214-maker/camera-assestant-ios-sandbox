package app.dyrecto.liveview.reference.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SubjectMatcherTest {

    @Test
    fun `category is a hard gate`() {
        val expected = SubjectMatcher.Expected(SubjectCategory.VEHICLE, centeredBox(0.4f))
        val dog = subject(SubjectCategory.ANIMAL, centeredBox(0.4f))
        assertNull(SubjectMatcher.match(expected, listOf(dog)))
    }

    @Test
    fun `with two similar candidates the overlapping one wins`() {
        val expectedBox = box(0.1f, 0.2f, 0.4f, 0.8f)
        val expected = SubjectMatcher.Expected(SubjectCategory.HUMAN, expectedBox)
        val near = subject(SubjectCategory.HUMAN, box(0.12f, 0.22f, 0.42f, 0.82f))
        val far = subject(SubjectCategory.HUMAN, box(0.6f, 0.2f, 0.9f, 0.8f))
        val match = SubjectMatcher.match(expected, listOf(far, near))
        assertNotNull(match)
        assertEquals(near, match!!.subject)
    }

    @Test
    fun `track identity tips the balance between equal candidates`() {
        // Two candidates symmetric around the expected box: identity must break the tie.
        val expected = SubjectMatcher.Expected(
            SubjectCategory.HUMAN,
            box(0.4f, 0.3f, 0.6f, 0.7f),
            trackId = 7,
        )
        val left = subject(SubjectCategory.HUMAN, box(0.3f, 0.3f, 0.5f, 0.7f), trackId = 3)
        val right = subject(SubjectCategory.HUMAN, box(0.5f, 0.3f, 0.7f, 0.7f), trackId = 7)
        val match = SubjectMatcher.match(expected, listOf(left, right))
        assertEquals(7, match!!.subject.trackId)
    }

    @Test
    fun `raw-label affinity tips the balance between equal candidates`() {
        val expected = SubjectMatcher.Expected(
            SubjectCategory.VEHICLE,
            box(0.4f, 0.3f, 0.6f, 0.7f),
            rawLabel = "truck",
        )
        val car = subject(SubjectCategory.VEHICLE, box(0.3f, 0.3f, 0.5f, 0.7f), rawLabel = "car")
        val truck = subject(SubjectCategory.VEHICLE, box(0.5f, 0.3f, 0.7f, 0.7f), rawLabel = "truck")
        val match = SubjectMatcher.match(expected, listOf(car, truck))
        assertEquals("truck", match!!.subject.rawLabel)
    }

    @Test
    fun `appearance similarity contributes when provided`() {
        val expected = SubjectMatcher.Expected(SubjectCategory.HUMAN, box(0.4f, 0.3f, 0.6f, 0.7f))
        val left = subject(SubjectCategory.HUMAN, box(0.3f, 0.3f, 0.5f, 0.7f))
        val right = subject(SubjectCategory.HUMAN, box(0.5f, 0.3f, 0.7f, 0.7f))
        val match = SubjectMatcher.match(expected, listOf(left, right)) { candidate ->
            if (candidate === right) 1f else 0f
        }
        assertEquals(right, match!!.subject)
    }

    @Test
    fun `a distant non-overlapping candidate is below the minimum score`() {
        val expected = SubjectMatcher.Expected(SubjectCategory.PRODUCT, box(0.0f, 0.0f, 0.1f, 0.1f))
        val distant = subject(SubjectCategory.PRODUCT, box(0.9f, 0.9f, 1.0f, 1.0f))
        assertNull(SubjectMatcher.match(expected, listOf(distant)))
    }

    @Test
    fun `iou is 1 for identical boxes and 0 for disjoint boxes`() {
        val a = box(0.1f, 0.1f, 0.5f, 0.5f)
        assertEquals(1f, SubjectMatcher.iou(a, a), 1e-4f)
        assertEquals(0f, SubjectMatcher.iou(a, box(0.6f, 0.6f, 0.9f, 0.9f)), 0f)
    }
}
