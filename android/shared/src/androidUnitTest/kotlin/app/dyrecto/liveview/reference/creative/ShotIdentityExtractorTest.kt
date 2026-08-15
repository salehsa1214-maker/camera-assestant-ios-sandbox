package app.dyrecto.liveview.reference.creative

import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.ReferenceAiSubject
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.perception.CreativePriorityMapper
import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 16.2 acceptance test: two references that USED to collapse to the same categorical model
 * (both PORTRAIT / CLOSE_UP / WARM / RIGHT_THIRD) must now produce visibly DIFFERENT shot identities.
 * This proves increased specificity without literal pixel matching.
 */
class ShotIdentityExtractorTest {

    private fun exposure(mean: Float, subjectMean: Float) = ReferenceExposureProfile(
        mean = mean,
        median = mean,
        p95 = (mean + 60f).coerceAtMost(255f),
        p99 = (mean + 80f).coerceAtMost(255f),
        highlightCoverage = 1f,
        shadowCoverage = 1f,
        subjectExposure = SubjectExposureStats(
            mean = subjectMean,
            median = subjectMean,
            highlightCoverage = 0f,
            shadowCoverage = 0f,
            sampleCount = 1000,
        ),
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

    /** Same category (warm close-up portrait, right of center) — different visual identity. */
    @Test
    fun `two similar warm portraits produce different identities`() {
        // A: modestly right of center, small subject, mild warmth, mild subject-under-background.
        val a = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 150f, subjectMean = 135f), // separation -15
            color = color(150f, 138f, 136f), // warmth +14
            ai = humanAi(NormalizedRect(0.55f, 0.10f, 0.75f, 0.55f)), // centerX 0.65
            faceBox = null,
        )
        // B: jammed to the right edge, larger subject, strong warmth, strong separation (near silhouette).
        val b = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 150f, subjectMean = 95f), // separation -55
            color = color(190f, 150f, 140f), // warmth +50
            ai = humanAi(NormalizedRect(0.72f, 0.02f, 0.99f, 0.72f)), // centerX 0.855, hugs right edge
            faceBox = null,
        )

        // Both are still categorically identical on the old axes...
        assertEquals(SubjectPlacement.RIGHT_THIRD, a.composition.placement)
        assertEquals(SubjectPlacement.RIGHT_THIRD, b.composition.placement)
        assertEquals(ColorTemperature.WARM, a.color.temperature)
        assertEquals(ColorTemperature.WARM, b.color.temperature)

        // ...but their shot identities diverge.
        assertNotNull(a.identity)
        assertNotNull(b.identity)
        assertTrue(a.identity!!.traits.isNotEmpty())
        assertTrue(b.identity!!.traits.isNotEmpty())
        assertNotEquals(a.identity, b.identity)

        // Position: "right of center" vs "far right".
        assertEquals(
            "subject_right_of_center",
            a.identity!!.of(ShotTraitDimension.SUBJECT_FRAME_POSITION)?.descriptor,
        )
        assertEquals(
            "subject_far_right",
            b.identity!!.of(ShotTraitDimension.SUBJECT_FRAME_POSITION)?.descriptor,
        )

        // Subject/background separation: mild vs pronounced.
        assertEquals(
            "subject_under_background",
            a.identity!!.of(ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION)?.descriptor,
        )
        assertEquals(
            "subject_well_under_background",
            b.identity!!.of(ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION)?.descriptor,
        )

        // B is the more distinctive shot overall.
        assertTrue(b.identity!!.peakDistinctiveness > a.identity!!.peakDistinctiveness)

        // B pressed against the right edge registers edge tension that A does not.
        assertNotNull(b.identity!!.of(ShotTraitDimension.EDGE_ANCHORING))
    }

    /** The signature now LEADS with a measured identity trait, not a categorical label. */
    @Test
    fun `signature leads with a measured identity trait for a distinctive shot`() {
        val b = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 150f, subjectMean = 95f),
            color = color(190f, 150f, 140f),
            ai = humanAi(NormalizedRect(0.72f, 0.02f, 0.99f, 0.72f)),
            faceBox = null,
        )
        val elements = b.signature!!.elements
        assertTrue(elements.isNotEmpty())
        assertEquals(SignatureSource.IDENTITY, elements.first().source)
        assertTrue(elements.first().label.startsWith("trait_"))
    }

    /** A neutral, centered, evenly-lit subject is unremarkable — few/weak identity traits. */
    @Test
    fun `neutral centered subject is not distinctive`() {
        val neutral = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 128f, subjectMean = 128f),
            color = color(128f, 128f, 128f), // warmth 0
            ai = humanAi(NormalizedRect(0.30f, 0.15f, 0.70f, 0.90f)), // centered, fills the frame
            faceBox = null,
        )
        val extreme = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 150f, subjectMean = 95f),
            color = color(190f, 150f, 140f),
            ai = humanAi(NormalizedRect(0.72f, 0.02f, 0.99f, 0.72f)),
            faceBox = null,
        )
        assertNotNull(neutral.identity)
        // No position trait survives for a centered subject (it is not a distinctive characteristic).
        assertEquals(null, neutral.identity!!.of(ShotTraitDimension.SUBJECT_FRAME_POSITION))
        assertTrue(neutral.identity!!.peakDistinctiveness < extreme.identity!!.peakDistinctiveness)
    }

    /**
     * Step D guardrail — identity shapes reasoning PRIORITY only. A reference whose identity leans on a
     * strongly off-center subject (but only mild color) makes position drift outrank white-balance
     * drift, exactly the user's example. This is the ordering signal (via the unchanged
     * CreativePriorityMapper multiplier); it never changes whether a signal is valid or an alert fires.
     */
    @Test
    fun `identity makes placement outrank white balance in reasoning priority`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 140f, subjectMean = 140f),
            color = color(140f, 136f, 132f), // warmth +8 -> neutral, not distinctive
            ai = humanAi(NormalizedRect(0.74f, 0.20f, 0.94f, 0.80f)), // subject hard to the right
            faceBox = null,
        )
        val position = CreativePriorityMapper.multiplierFor(ReferenceSignal.SUBJECT_POSITION, model)
        val whiteBalance = CreativePriorityMapper.multiplierFor(ReferenceSignal.WHITE_BALANCE, model)
        assertTrue(
            "position ($position) should outrank white balance ($whiteBalance)",
            position > whiteBalance,
        )
        // A no-model reference stays perfectly neutral — legacy reasoning is byte-identical.
        assertEquals(1f, CreativePriorityMapper.multiplierFor(ReferenceSignal.SUBJECT_POSITION, CreativeSceneModel()), 1e-6f)
    }

    /** No AI + no face: still safe, still derives tone/color identity from the exposure/color profile. */
    @Test
    fun `no subject still yields a valid identity without crashing`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(mean = 60f, subjectMean = 60f), // dark frame
            color = color(90f, 100f, 150f), // cool, warmth -60
            ai = null,
            faceBox = null,
        )
        assertNotNull(model.identity)
        // Geometry-dependent traits are absent, but tone/color traits are present.
        assertEquals(null, model.identity!!.of(ShotTraitDimension.SUBJECT_FRAME_POSITION))
        assertNotNull(model.identity!!.of(ShotTraitDimension.TONAL_KEY))
        assertNotNull(model.identity!!.of(ShotTraitDimension.COLOR_WARMTH_MAGNITUDE))
    }
}
