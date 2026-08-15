package app.dyrecto.liveview.reference

import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.creative.CameraAngle
import app.dyrecto.liveview.reference.creative.ColorTemperature
import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.CreativeCamera
import app.dyrecto.liveview.reference.creative.CreativeColor
import app.dyrecto.liveview.reference.creative.CreativeComposition
import app.dyrecto.liveview.reference.creative.CreativePriority
import app.dyrecto.liveview.reference.creative.CreativeSceneModel
import app.dyrecto.liveview.reference.creative.CreativeSubject
import app.dyrecto.liveview.reference.creative.CreativeSubjectKind
import app.dyrecto.liveview.reference.creative.NegativeSpace
import app.dyrecto.liveview.reference.creative.ShotType
import app.dyrecto.liveview.reference.creative.SubjectPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceProfileCodecTest {

    private fun profile(id: String = "p1", ai: ReferenceAiProfile? = null) = ReferenceProfile(
        id = id,
        name = "Reference",
        createdAtMs = 123L,
        imageUri = "file:///data/ref.jpg",
        width = 1280,
        height = 720,
        exposure = ReferenceExposureProfile(120f, 118f, 231f, 245f, 2.5f, 0.4f),
        color = ReferenceColorProfile(140f, 130f, 120f, 20f, -5f),
        subject = ReferenceSubjectProfile(0.5f, 0.4f, 0.2f, 0.3f, 0.06f),
        face = ReferenceFaceProfile(true, true, NormalizedRect(0.4f, 0.25f, 0.6f, 0.55f), 2),
        options = ReferenceMonitorOptions(),
        ai = ai,
    )

    private fun aiProfile() = ReferenceAiProfile(
        sceneMode = SceneMode.VEHICLE,
        sceneModeConfidence = 0.85f,
        strategy = ComparisonStrategy.VEHICLE_STRATEGY,
        primarySubjectType = PrimarySubjectType.VEHICLE,
        primarySubjectCategory = SubjectCategory.VEHICLE,
        primarySubjectRawLabel = "car",
        primarySubjectConfidence = 0.9f,
        primarySubjectBox = NormalizedRect(0.2f, 0.3f, 0.8f, 0.85f),
        subjects = listOf(
            ReferenceAiSubject(SubjectCategory.VEHICLE, "car", 0.9f, NormalizedRect(0.2f, 0.3f, 0.8f, 0.85f)),
            ReferenceAiSubject(SubjectCategory.HUMAN, "person", 0.6f, NormalizedRect(0.0f, 0.4f, 0.15f, 0.9f)),
        ),
        compositionSignature = List(9) { it * 0.1f },
        segmentationCoverage = 0.42f,
        segmentationPixelAccurate = true,
        embeddingId = "p1-emb",
        embeddingModelId = "mobilenet_v3_small",
    )

    @Test
    fun `session with AI profile round-trips`() {
        val session = ReferenceSession(
            id = "s1",
            name = "Wedding day",
            createdAtMs = 999L,
            profiles = listOf(profile(ai = aiProfile())),
            activeProfileId = "p1",
        )
        val decoded = ReferenceProfileCodec.decodeSession(ReferenceProfileCodec.encodeSession(session))
        assertEquals(session, decoded)
        assertEquals("p1", decoded!!.activeProfile?.id)
        assertEquals(SceneMode.VEHICLE, decoded.activeProfile?.ai?.sceneMode)
        assertEquals("car", decoded.activeProfile?.ai?.primarySubjectRawLabel)
    }

    private fun creativeModel() = CreativeSceneModel(
        subject = CreativeSubject(CreativeSubjectKind.PORTRAIT, count = 1, confidence = 0.9f),
        camera = CreativeCamera(ShotType.CLOSE_UP, CameraAngle.EYE_LEVEL, 0.8f, 0.3f),
        composition = CreativeComposition(
            placement = SubjectPlacement.RIGHT_THIRD,
            negativeSpace = NegativeSpace.HIGH,
        ),
        color = CreativeColor(temperature = ColorTemperature.WARM, confidence = 0.7f),
        importance = mapOf(
            CreativeAspect.COMPOSITION to CreativePriority(0.95f, "strong negative space detected"),
            CreativeAspect.COLOR to CreativePriority(0.2f, "near-neutral white balance"),
        ),
    )

    @Test
    fun `session with a creative scene model round-trips`() {
        val profile = profile(ai = aiProfile()).copy(creativeScene = creativeModel())
        val session = ReferenceSession(
            id = "s1",
            createdAtMs = 7L,
            profiles = listOf(profile),
            activeProfileId = "p1",
        )
        val decoded = ReferenceProfileCodec.decodeSession(ReferenceProfileCodec.encodeSession(session))
        assertEquals(session, decoded)
        val creative = decoded!!.activeProfile?.creativeScene
        assertNotNull(creative)
        assertEquals(SubjectPlacement.RIGHT_THIRD, creative!!.composition.placement)
        assertEquals(
            "strong negative space detected",
            creative.importance[CreativeAspect.COMPOSITION]?.reason,
        )
        assertEquals(0.95f, creative.importance[CreativeAspect.COMPOSITION]?.weight)
    }

    @Test
    fun `legacy-shaped profile with null ai round-trips`() {
        val session = ReferenceSession(
            id = "s1",
            createdAtMs = 1L,
            profiles = listOf(profile(ai = null)),
            activeProfileId = "p1",
        )
        val decoded = ReferenceProfileCodec.decodeSession(ReferenceProfileCodec.encodeSession(session))
        assertNotNull(decoded)
        assertNull(decoded!!.activeProfile?.ai)
    }

    @Test
    fun `embedding with a large vector round-trips`() {
        val embedding = ReferenceEmbedding(
            id = "p1-emb",
            modelId = "mobilenet_v3_small",
            dimensions = 1024,
            values = List(1024) { it / 1024f },
            createdAtMs = 5L,
        )
        val decoded = ReferenceProfileCodec.decodeEmbedding(ReferenceProfileCodec.encodeEmbedding(embedding))
        assertEquals(embedding, decoded)
        assertEquals(1024, decoded!!.vector().size)
        assertEquals(0.5f, decoded.vector()[512], 1e-4f)
    }

    @Test
    fun `malformed payloads decode to null instead of throwing`() {
        assertNull(ReferenceProfileCodec.decodeSession("not json at all"))
        assertNull(ReferenceProfileCodec.decodeSession("""{"id": 42}"""))
        assertNull(ReferenceProfileCodec.decodeEmbedding("{}"))
    }

    @Test
    fun `unknown keys are ignored for forward compatibility`() {
        val text = ReferenceProfileCodec.encodeSession(
            ReferenceSession(id = "s1", createdAtMs = 1L),
        ).removeSuffix("}") + ""","futureField":"ignored"}"""
        val decoded = ReferenceProfileCodec.decodeSession(text)
        assertNotNull(decoded)
        assertEquals("s1", decoded!!.id)
    }

    @Test
    fun `active profile falls back to the first profile when the id is stale`() {
        val session = ReferenceSession(
            id = "s1",
            createdAtMs = 1L,
            profiles = listOf(profile(id = "p2")),
            activeProfileId = "deleted-profile",
        )
        assertEquals("p2", session.activeProfile?.id)
        assertTrue(session.withProfile(profile(id = "p3")).activeProfileId == "p3")
    }
}
