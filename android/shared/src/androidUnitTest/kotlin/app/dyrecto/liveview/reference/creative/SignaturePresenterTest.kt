package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.reference.creative.identity.ShotIdentity
import app.dyrecto.liveview.reference.creative.identity.ShotTrait
import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension
import app.dyrecto.liveview.reference.creative.identity.TraitDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignaturePresenterTest {

    @Test
    fun `null model yields no insights`() {
        assertTrue(SignaturePresenter.insights(null).isEmpty())
    }

    @Test
    fun `never leaks raw semantic concept text or labels`() {
        val model = CreativeSceneModel(
            lighting = CreativeLighting(key = LightingKey.LOW_KEY, backlightHint = true, confidence = 0.8f),
            signature = CreativeSignature(
                listOf(
                    SignatureElement("concept_backlit rim light dramatic", 0.95f, SignatureSource.SEMANTIC),
                    SignatureElement("color_warm", 0.8f, SignatureSource.COLOR),
                    SignatureElement("rel_subject_isolation", 0.7f, SignatureSource.RELATIONSHIP),
                ),
            ),
            relationships = listOf(
                SceneRelationship(RelationshipKind.NEGATIVE_SPACE_DOMINANT, 0.9f, 0.9f, "reason"),
            ),
        )

        val text = SignaturePresenter.insights(model).joinToString(" ") { "${it.title} ${it.text}" }

        // Raw concept text / internal labels must never surface.
        assertFalse(text.contains("concept_"))
        assertFalse(text.contains("rim light"))
        assertFalse(text.contains("rel_"))
        // Translated, photographer-friendly phrasing IS present.
        assertTrue(text.contains("warm tones"))
        assertTrue(text.contains("isolated subject"))
    }

    @Test
    fun `maps structural relationship and backlight to sentences`() {
        val model = CreativeSceneModel(
            lighting = CreativeLighting(backlightHint = true, confidence = 0.8f),
            relationships = listOf(
                SceneRelationship(RelationshipKind.NEGATIVE_SPACE_DOMINANT, 0.9f, 0.9f, "r"),
            ),
        )
        val insights = SignaturePresenter.insights(model)
        assertEquals(
            "Strong negative space defines the frame.",
            insights.first { it.title == "Scene Structure" }.text,
        )
        assertEquals(
            "Subject separated from a brighter background.",
            insights.first { it.title == "Lighting Relationship" }.text,
        )
    }

    @Test
    fun `leads with shot identity from measured traits and never leaks raw descriptors`() {
        val model = CreativeSceneModel(
            identity = ShotIdentity(
                listOf(
                    ShotTrait(
                        ShotTraitDimension.SUBJECT_FRAME_POSITION, 0.85f, 0.9f,
                        "subject_far_right", TraitDirection.RIGHT, 0.9f, "subject at 85% width",
                    ),
                    ShotTrait(
                        ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION, -55f, 0.95f,
                        "subject_well_under_background", TraitDirection.NONE, 0.75f, "subject -55 luma",
                    ),
                    ShotTrait(
                        ShotTraitDimension.SEMANTIC_DISTINCTION, 0.6f, 0.8f,
                        "backlit_rim_separation", TraitDirection.UNKNOWN, 0.6f, "read",
                    ),
                ),
            ),
        )
        val identity = SignaturePresenter.insights(model).first { it.title == "Shot Identity" }.text
        // Measured + semantic identity is phrased for a photographer...
        assertTrue(identity.contains("right edge"))
        assertTrue(identity.contains("under a brighter background"))
        assertTrue(identity.contains("backlit"))
        // ...but the raw internal descriptors / concept labels never surface.
        assertFalse(identity.contains("subject_far_right"))
        assertFalse(identity.contains("subject_well_under_background"))
        assertFalse(identity.contains("backlit_rim_separation"))
    }

    @Test
    fun `empty signature and no relationships yields no insights`() {
        assertTrue(SignaturePresenter.insights(CreativeSceneModel()).isEmpty())
    }
}
