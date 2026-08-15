package app.dyrecto.liveview.reference.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class ComparisonStrategySelectorTest {

    private fun select(
        mode: SceneMode,
        primary: PrimarySubject? = null,
        detectionAvailable: Boolean = true,
    ) = ComparisonStrategySelector.select(mode, primary, detectionAvailable)

    @Test
    fun `scene modes map to their strategies`() {
        assertEquals(ComparisonStrategy.HUMAN_STRATEGY, select(SceneMode.HUMAN))
        assertEquals(ComparisonStrategy.ANIMAL_STRATEGY, select(SceneMode.ANIMAL))
        assertEquals(ComparisonStrategy.VEHICLE_STRATEGY, select(SceneMode.VEHICLE))
        assertEquals(ComparisonStrategy.PRODUCT_STRATEGY, select(SceneMode.PRODUCT))
        assertEquals(ComparisonStrategy.PRODUCT_STRATEGY, select(SceneMode.FOOD))
        assertEquals(ComparisonStrategy.ARCHITECTURE_STRATEGY, select(SceneMode.ARCHITECTURE))
        assertEquals(ComparisonStrategy.LANDSCAPE_STRATEGY, select(SceneMode.LANDSCAPE))
        assertEquals(ComparisonStrategy.GENERIC_SCENE_STRATEGY, select(SceneMode.STREET))
        assertEquals(ComparisonStrategy.GENERIC_SCENE_STRATEGY, select(SceneMode.INTERIOR))
        assertEquals(ComparisonStrategy.GENERIC_SCENE_STRATEGY, select(SceneMode.GENERIC_SCENE))
        assertEquals(ComparisonStrategy.GENERIC_SCENE_STRATEGY, select(SceneMode.UNKNOWN))
    }

    @Test
    fun `multi-subject scene with MULTIPLE primary uses MULTI_OBJECT strategy`() {
        val subjects = listOf(
            subject(SubjectCategory.HUMAN, box(0.05f, 0.3f, 0.35f, 0.8f)),
            subject(SubjectCategory.HUMAN, box(0.6f, 0.3f, 0.9f, 0.8f)),
        )
        val primary = PrimarySubjectSelector.select(subjects).value
        assertEquals(
            ComparisonStrategy.MULTI_OBJECT_STRATEGY,
            select(SceneMode.MULTI_SUBJECT, primary),
        )
    }

    @Test
    fun `detector unavailable downgrades everything to GENERIC_SCENE strategy`() {
        for (mode in SceneMode.values()) {
            assertEquals(
                "mode=$mode",
                ComparisonStrategy.GENERIC_SCENE_STRATEGY,
                select(mode, detectionAvailable = false),
            )
        }
    }
}
