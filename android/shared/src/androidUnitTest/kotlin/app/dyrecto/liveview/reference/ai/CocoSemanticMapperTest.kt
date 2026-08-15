package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.ai.semantic.CocoSemanticMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class CocoSemanticMapperTest {

    @Test
    fun `maps core categories`() {
        assertEquals(SubjectCategory.HUMAN, CocoSemanticMapper.map("person", 0))
        assertEquals(SubjectCategory.ANIMAL, CocoSemanticMapper.map("dog", 17))
        assertEquals(SubjectCategory.ANIMAL, CocoSemanticMapper.map("cat", 16))
        assertEquals(SubjectCategory.ANIMAL, CocoSemanticMapper.map("bird", 15))
        assertEquals(SubjectCategory.VEHICLE, CocoSemanticMapper.map("car", 2))
        assertEquals(SubjectCategory.VEHICLE, CocoSemanticMapper.map("truck", 7))
        assertEquals(SubjectCategory.VEHICLE, CocoSemanticMapper.map("motorcycle", 3))
        assertEquals(SubjectCategory.PRODUCT, CocoSemanticMapper.map("bottle", 39))
        assertEquals(SubjectCategory.FOOD, CocoSemanticMapper.map("pizza", 53))
        assertEquals(SubjectCategory.FOOD, CocoSemanticMapper.map("cup", 41))
        assertEquals(SubjectCategory.FURNITURE, CocoSemanticMapper.map("chair", 56))
        assertEquals(SubjectCategory.FURNITURE, CocoSemanticMapper.map("dining table", 60))
        assertEquals(SubjectCategory.ELECTRONICS, CocoSemanticMapper.map("laptop", 63))
        assertEquals(SubjectCategory.ELECTRONICS, CocoSemanticMapper.map("cell phone", 67))
        assertEquals(SubjectCategory.NATURE, CocoSemanticMapper.map("potted plant", 58))
    }

    @Test
    fun `unknown labels map to UNKNOWN instead of throwing`() {
        assertEquals(SubjectCategory.UNKNOWN, CocoSemanticMapper.map("weird_future_label", 999))
        assertEquals(SubjectCategory.UNKNOWN, CocoSemanticMapper.map("", -1))
    }

    @Test
    fun `mapping is case and whitespace insensitive`() {
        assertEquals(SubjectCategory.HUMAN, CocoSemanticMapper.map("Person", 0))
        assertEquals(SubjectCategory.VEHICLE, CocoSemanticMapper.map(" CAR ", 2))
    }

    @Test
    fun `street furniture is scene context, not a subject`() {
        assertEquals(SubjectCategory.UNKNOWN, CocoSemanticMapper.map("traffic light", 9))
        assertEquals(SubjectCategory.UNKNOWN, CocoSemanticMapper.map("stop sign", 11))
    }
}
