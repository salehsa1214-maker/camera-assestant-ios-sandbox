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
import app.dyrecto.liveview.reference.creative.identity.ShotTraitDimension
import app.dyrecto.liveview.reference.creative.semantic.ConceptScore
import app.dyrecto.liveview.reference.creative.semantic.SemanticObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 16.1 — verifies the expert orchestrator: byte-identical fallback with no semantic evidence,
 * bounded importance refinement + relationships/signature when evidence exists, richer specificity,
 * and the compact signature cap.
 */
class CreativeSceneAnalyzerRefinementTest {

    private fun exposure(mean: Float) = ReferenceExposureProfile(
        mean = mean, median = mean, p95 = (mean + 60f).coerceAtMost(255f),
        p99 = (mean + 80f).coerceAtMost(255f), highlightCoverage = 1f, shadowCoverage = 1f,
    )

    private fun color(r: Float, g: Float, b: Float) =
        ReferenceColorProfile(r, g, b, warmthScore = r - b, tintScore = g - (r + b) / 2f)

    private fun humanAi(box: NormalizedRect, coverage: Float? = null) = ReferenceAiProfile(
        sceneMode = SceneMode.HUMAN,
        sceneModeConfidence = 0.8f,
        strategy = ComparisonStrategy.HUMAN_STRATEGY,
        primarySubjectType = PrimarySubjectType.PERSON,
        primarySubjectCategory = SubjectCategory.HUMAN,
        primarySubjectConfidence = 0.9f,
        primarySubjectBox = box,
        subjects = listOf(ReferenceAiSubject(SubjectCategory.HUMAN, "person", 0.9f, box)),
        segmentationCoverage = coverage,
        segmentationPixelAccurate = coverage != null,
    )

    private fun concept(label: String, score: Float, aspect: CreativeAspect?, family: String = "test") =
        ConceptScore(label, score, aspect, family)

    private fun observation(vararg concepts: ConceptScore) =
        SemanticObservation(concepts.sortedByDescending { it.score }, embedding = null, modelId = "test_model")

    private val box = NormalizedRect(0.60f, 0.10f, 0.80f, 0.50f) // right third, small area -> high neg space

    @Test
    fun `no semantic evidence keeps importance byte-identical to the deterministic baseline`() {
        val base = CreativeSceneAnalyzer.analyze(
            exposure = exposure(200f), color = color(170f, 140f, 120f), ai = humanAi(box), faceBox = null,
        )
        // Same inputs, an observation whose concepts have NO aspect and low scores -> no hints.
        val withInertSemantic = CreativeSceneAnalyzer.analyze(
            exposure = exposure(200f), color = color(170f, 140f, 120f), ai = humanAi(box), faceBox = null,
            semantic = observation(concept("noise", 0.10f, aspect = null)),
        )
        assertEquals(base.importance, withInertSemantic.importance)
        // semantics diagnostics are attached when the expert ran, but never change importance.
        assertNull(base.semantics)
    }

    @Test
    fun `a strong aspect concept boosts that aspect's importance, bounded and reasoned`() {
        val base = CreativeSceneAnalyzer.analyze(
            exposure = exposure(128f), color = color(130f, 128f, 126f), ai = humanAi(box), faceBox = null,
        )
        val refined = CreativeSceneAnalyzer.analyze(
            exposure = exposure(128f), color = color(130f, 128f, 126f), ai = humanAi(box), faceBox = null,
            semantic = observation(concept("shallow depth of field", 0.6f, CreativeAspect.DEPTH_OF_FIELD)),
        )
        val baseDof = base.importance[CreativeAspect.DEPTH_OF_FIELD]!!.weight
        val refinedDof = refined.importance[CreativeAspect.DEPTH_OF_FIELD]!!
        assertTrue("expected boost", refinedDof.weight > baseDof)
        assertTrue("bounded to <=1", refinedDof.weight <= 1f)
        assertTrue("reason appended", refinedDof.reason.contains("semantic read"))
        // Aspects the concept didn't touch stay exactly as baseline.
        assertEquals(base.importance[CreativeAspect.COLOR], refined.importance[CreativeAspect.COLOR])
    }

    @Test
    fun `two references with the same description but different semantics diverge`() {
        val exposureP = exposure(150f)
        val colorP = color(150f, 145f, 140f)
        val aiP = humanAi(box)

        val isolationShot = CreativeSceneAnalyzer.analyze(
            exposure = exposureP, color = colorP, ai = aiP, faceBox = null,
            semantic = observation(
                concept("negative space minimalist", 0.5f, CreativeAspect.NEGATIVE_SPACE),
                concept("subject isolation shallow depth", 0.45f, CreativeAspect.DEPTH_OF_FIELD),
            ),
        )
        val busyShot = CreativeSceneAnalyzer.analyze(
            exposure = exposureP, color = colorP, ai = aiP, faceBox = null,
            semantic = observation(
                concept("busy street background", 0.5f, CreativeAspect.BACKGROUND),
                concept("dominant central subject", 0.45f, CreativeAspect.SUBJECT),
            ),
        )

        assertNotEquals(isolationShot.signature, busyShot.signature)
        assertNotEquals(
            isolationShot.relationships.map { it.kind }.toSet(),
            busyShot.relationships.map { it.kind }.toSet(),
        )
    }

    @Test
    fun `a dominant concept becomes a bounded semantic identity marker`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(150f), color = color(150f, 145f, 140f), ai = humanAi(box), faceBox = null,
            semantic = observation(
                concept("backlit rim separation", 0.55f, CreativeAspect.LIGHTING),
                concept("flat even light", 0.20f, CreativeAspect.LIGHTING),
                concept("cluttered scene", 0.18f, CreativeAspect.BACKGROUND),
            ),
        )
        val marker = model.identity!!.of(ShotTraitDimension.SEMANTIC_DISTINCTION)
        assertNotNull(marker)
        assertEquals("backlit rim separation", marker!!.descriptor)
        // The marker does NOT touch importance — LIGHTING importance comes from measurement + the
        // gated hint path, never from the semantic marker itself.
        assertNotNull(model.importance[CreativeAspect.LIGHTING])
    }

    @Test
    fun `flat concept scores yield no semantic identity marker`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(150f), color = color(150f, 145f, 140f), ai = humanAi(box), faceBox = null,
            semantic = observation(
                concept("a", 0.30f, aspect = null),
                concept("b", 0.29f, aspect = null),
                concept("c", 0.28f, aspect = null),
            ),
        )
        assertNull(model.identity!!.of(ShotTraitDimension.SEMANTIC_DISTINCTION))
    }

    @Test
    fun `signature stays compact - at most three by default and never more than five`() {
        // Many strong signals: high neg space, off-thirds, warm, close-up, plus strong concepts.
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(200f),
            color = color(190f, 140f, 110f),
            ai = humanAi(NormalizedRect(0.62f, 0.02f, 0.98f, 0.62f), coverage = 0.12f),
            faceBox = null,
            semantic = observation(
                concept("cinematic", 0.9f, CreativeAspect.MOOD),
                concept("shallow depth", 0.9f, CreativeAspect.DEPTH_OF_FIELD),
                concept("minimal negative space", 0.9f, CreativeAspect.NEGATIVE_SPACE),
            ),
        )
        val elements = model.signature!!.elements
        assertTrue("size ${elements.size} must be <= 5", elements.size <= 5)
        assertTrue("size ${elements.size} must be >= 3 here", elements.size >= 3)
        // Ordered by salience, descending.
        assertEquals(elements.sortedByDescending { it.salience }, elements)
    }

    @Test
    fun `weak deterministic-only reference can still yield a small signature`() {
        val model = CreativeSceneAnalyzer.analyze(
            exposure = exposure(70f), color = color(90f, 100f, 130f), ai = null, faceBox = null,
        )
        val elements = model.signature?.elements.orEmpty()
        assertTrue(elements.size <= 3)
    }
}
