package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.ReferenceAiSubject
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreativeSceneAnalyzerTest {

    private fun exposure(
        mean: Float,
        median: Float = mean,
        p95: Float = (mean + 60f).coerceAtMost(255f),
        p99: Float = (mean + 80f).coerceAtMost(255f),
        highlight: Float = 1f,
        shadow: Float = 1f,
    ) = ReferenceExposureProfile(
        mean = mean,
        median = median,
        p95 = p95,
        p99 = p99,
        highlightCoverage = highlight,
        shadowCoverage = shadow,
    )

    private fun color(r: Float, g: Float, b: Float) =
        ReferenceColorProfile(r, g, b, warmthScore = r - b, tintScore = g - (r + b) / 2f)

    private fun humanAi(box: NormalizedRect) = ReferenceAiProfile(
        sceneMode = SceneMode.HUMAN,
        sceneModeConfidence = 0.8f,
        strategy = ComparisonStrategy.HUMAN_STRATEGY,
        primarySubjectType = PrimarySubjectType.PERSON,
        primarySubjectCategory = SubjectCategory.HUMAN,
        primarySubjectConfidence = 0.9f,
        primarySubjectBox = box,
        subjects = listOf(ReferenceAiSubject(SubjectCategory.HUMAN, "person", 0.9f, box)),
    )

    @Test
    fun `off-center bright warm portrait emphasizes composition and color`() {
        val box = NormalizedRect(0.60f, 0.10f, 0.80f, 0.50f) // right third, small area -> high neg space
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 200f, median = 190f, p95 = 245f, highlight = 8f),
            color = color(170f, 140f, 120f), // warmth = +50 -> WARM
            ai = humanAi(box),
            faceBox = null,
        )

        assertEquals(CreativeSubjectKind.PORTRAIT, model.subject.kind)
        assertEquals(SubjectPlacement.RIGHT_THIRD, model.composition.placement)
        assertEquals(NegativeSpace.HIGH, model.composition.negativeSpace)
        assertEquals(LightingKey.HIGH_KEY, model.lighting.key)
        assertEquals(ColorTemperature.WARM, model.color.temperature)
        assertEquals(Mood.BRIGHT_AIRY, model.style.mood)

        // Phase 16.2: importance now reflects HOW distinctive the shot is on each axis (measured
        // identity), not a flat per-category weight. This strongly off-center, warm, high-key,
        // small-subject shot leans on composition/negative-space/color, and its measured off-center
        // placement now matters too (raised from the old flat categorical value).
        assertEquals(PriorityLevel.HIGH, model.importance[CreativeAspect.NEGATIVE_SPACE]?.level)
        assertEquals(PriorityLevel.HIGH, model.importance[CreativeAspect.COMPOSITION]?.level)
        assertEquals(PriorityLevel.HIGH, model.importance[CreativeAspect.COLOR]?.level)
        assertEquals(PriorityLevel.HIGH, model.importance[CreativeAspect.SUBJECT_PLACEMENT]?.level)
        // Reasons now cite the measured evidence rather than a category label.
        assertTrue(model.importance[CreativeAspect.COLOR]!!.reason.contains("warmth"))
    }

    @Test
    fun `neutral centered subject deprioritizes white balance`() {
        val box = NormalizedRect(0.35f, 0.10f, 0.65f, 0.70f) // centered, area 0.18 -> medium neg space
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 128f, median = 120f, p95 = 180f),
            color = color(130f, 128f, 126f), // warmth +4 -> NEUTRAL
            ai = humanAi(box),
            faceBox = null,
        )

        assertEquals(SubjectPlacement.CENTER, model.composition.placement)
        assertEquals(ColorTemperature.NEUTRAL, model.color.temperature)
        assertEquals(LightingKey.BALANCED, model.lighting.key)
        assertEquals(0.2f, model.importance[CreativeAspect.COLOR]?.weight)
        assertEquals(
            "near-neutral white balance",
            model.importance[CreativeAspect.COLOR]?.reason,
        )
        assertEquals(PriorityLevel.LOW, model.importance[CreativeAspect.COLOR]?.level)
        assertEquals("centered subject", model.importance[CreativeAspect.SUBJECT_PLACEMENT]?.reason)
    }

    @Test
    fun `no AI and no face yields UNKNOWNs without crashing but still derives lighting and color`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 70f), // low key
            color = color(90f, 100f, 130f), // warmth -40 -> COOL
            ai = null,
            faceBox = null,
        )

        assertEquals(CreativeSubjectKind.UNKNOWN, model.subject.kind)
        assertEquals(ShotType.UNKNOWN, model.camera.shotType)
        assertEquals(CameraAngle.UNKNOWN, model.camera.angle)
        assertEquals(SubjectPlacement.UNKNOWN, model.composition.placement)
        assertEquals(NegativeSpace.UNKNOWN, model.composition.negativeSpace)
        // Lighting/color are always derivable from the exposure/color profile.
        assertEquals(LightingKey.LOW_KEY, model.lighting.key)
        assertEquals(ColorTemperature.COOL, model.color.temperature)
        assertEquals(Mood.DARK_MOODY, model.style.mood)
        // Importance is always populated (matching-independent).
        assertNotNull(model.importance[CreativeAspect.SUBJECT])
        assertEquals("no distinct subject", model.importance[CreativeAspect.SUBJECT]?.reason)
    }

    @Test
    fun `face-only reference derives portrait shot type and headroom`() {
        val faceBox = NormalizedRect(0.40f, 0.08f, 0.60f, 0.50f) // face height 0.42 -> close-up
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 130f),
            color = color(140f, 130f, 122f),
            ai = null,
            faceBox = faceBox,
        )

        assertEquals(CreativeSubjectKind.PORTRAIT, model.subject.kind)
        assertEquals(ShotType.CLOSE_UP, model.camera.shotType)
        assertEquals(HeadroomLevel.BALANCED, model.composition.headroom)
        assertTrue(model.importance.containsKey(CreativeAspect.HEADROOM))
    }
}
