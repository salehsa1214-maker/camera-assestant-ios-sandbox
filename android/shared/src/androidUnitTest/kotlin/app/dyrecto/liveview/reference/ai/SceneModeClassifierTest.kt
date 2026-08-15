package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import org.junit.Assert.assertEquals
import org.junit.Test

class SceneModeClassifierTest {

    private fun classify(subjects: List<SceneSubject>, embeddingAvailable: Boolean = true) =
        SceneModeClassifier.classify(
            subjects = subjects,
            primary = PrimarySubjectSelector.select(subjects),
            embeddingAvailable = embeddingAvailable,
        )

    @Test
    fun `person scene classifies as HUMAN`() {
        val subjects = listOf(subject(SubjectCategory.HUMAN, centeredBox(0.4f)))
        assertEquals(SceneMode.HUMAN, classify(subjects).value)
    }

    @Test
    fun `car scene classifies as VEHICLE`() {
        val subjects = listOf(subject(SubjectCategory.VEHICLE, centeredBox(0.5f)))
        assertEquals(SceneMode.VEHICLE, classify(subjects).value)
    }

    @Test
    fun `dog scene classifies as ANIMAL`() {
        val subjects = listOf(subject(SubjectCategory.ANIMAL, centeredBox(0.3f)))
        assertEquals(SceneMode.ANIMAL, classify(subjects).value)
    }

    @Test
    fun `bottle scene classifies as PRODUCT`() {
        val subjects = listOf(subject(SubjectCategory.PRODUCT, centeredBox(0.25f)))
        assertEquals(SceneMode.PRODUCT, classify(subjects).value)
    }

    @Test
    fun `pizza scene classifies as FOOD`() {
        val subjects = listOf(subject(SubjectCategory.FOOD, centeredBox(0.35f)))
        assertEquals(SceneMode.FOOD, classify(subjects).value)
    }

    @Test
    fun `building or landscape reference (no detections) with embedding is GENERIC_SCENE`() {
        // COCO cannot detect buildings/mountains — those references reach us as empty subject
        // lists, and the embedding carries the monitoring (universal fallback).
        val result = classify(emptyList(), embeddingAvailable = true)
        assertEquals(SceneMode.GENERIC_SCENE, result.value)
    }

    @Test
    fun `nothing detectable and no embedding is UNKNOWN`() {
        val result = classify(emptyList(), embeddingAvailable = false)
        assertEquals(SceneMode.UNKNOWN, result.value)
        assertEquals(0f, result.confidence, 1e-4f)
    }

    @Test
    fun `two people classify as MULTI_SUBJECT`() {
        val subjects = listOf(
            subject(SubjectCategory.HUMAN, box(0.05f, 0.3f, 0.35f, 0.8f)),
            subject(SubjectCategory.HUMAN, box(0.6f, 0.3f, 0.9f, 0.8f)),
        )
        assertEquals(SceneMode.MULTI_SUBJECT, classify(subjects).value)
    }

    @Test
    fun `furniture-dominant scene classifies as INTERIOR`() {
        val subjects = listOf(
            subject(SubjectCategory.FURNITURE, box(0.1f, 0.4f, 0.45f, 0.9f), rawLabel = "couch"),
            subject(SubjectCategory.FURNITURE, box(0.55f, 0.5f, 0.9f, 0.9f), rawLabel = "chair"),
        )
        assertEquals(SceneMode.INTERIOR, classify(subjects).value)
    }

    @Test
    fun `small vehicles plus pedestrian classify as STREET`() {
        val subjects = listOf(
            subject(SubjectCategory.VEHICLE, box(0.1f, 0.5f, 0.25f, 0.65f)),
            subject(SubjectCategory.VEHICLE, box(0.4f, 0.5f, 0.55f, 0.63f)),
            subject(SubjectCategory.HUMAN, box(0.7f, 0.45f, 0.78f, 0.7f)),
        )
        assertEquals(SceneMode.STREET, classify(subjects).value)
    }
}
