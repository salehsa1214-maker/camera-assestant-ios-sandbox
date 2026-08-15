package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.perception.PerceptualSeverity
import app.dyrecto.liveview.perception.PerceptualSignal
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalApplicability
import app.dyrecto.liveview.reference.ReferenceSignalResult
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure JVM tests for [InstructionSelector] — exactly one deterministic instruction per frame,
 * chosen by the active strategy's priority list, confirmed drifts first.
 */
class InstructionSelectorTest {

    private fun drifting(
        direction: ReferenceDriftDirection,
        confirmed: Boolean = true,
        importance: Float = 0.5f,
    ) = ReferenceSignalResult(
        enabled = true,
        available = true,
        matched = false,
        score = 0.4f,
        direction = direction,
        message = "drifted",
        confirmedDrift = confirmed,
        perception = PerceptualSignal(
            rawDifference = 1f,
            perceptualDifference = 0.5f,
            humanNoticeability = 0.5f,
            importance = importance,
            confidence = 0.9f,
            directionConfidence = 0.9f,
            deadZoneApplied = 0.1f,
            severity = PerceptualSeverity.NOTICEABLE,
            perceptuallyDrifting = true,
            direction = direction,
        ),
    )

    private fun match(
        strategy: ComparisonStrategy = ComparisonStrategy.HUMAN_STRATEGY,
        build: ReferenceMatchResult.() -> ReferenceMatchResult = { this },
    ) = ReferenceMatchResult(active = true, referenceId = "r", strategy = strategy)
        .build()
        .let { InstructionTranslator.annotate(it) }

    @Test
    fun `single drift returns that signal's instruction`() {
        val m = match { copy(exposureMatch = drifting(ReferenceDriftDirection.DARKER)) }
        assertEquals(AssistantAction.INCREASE_EXPOSURE, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `no drift returns null`() {
        assertNull(InstructionSelector.select(match()))
    }

    @Test
    fun `inactive match returns null`() {
        val m = match { copy(active = false, exposureMatch = drifting(ReferenceDriftDirection.DARKER)) }
        assertNull(InstructionSelector.select(m))
    }

    @Test
    fun `human strategy prefers missing face over exposure drift`() {
        val m = match(ComparisonStrategy.HUMAN_STRATEGY) {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER),
                facePresenceMatch = drifting(ReferenceDriftDirection.MISSING),
            )
        }
        assertEquals(AssistantAction.WAIT_FOR_SUBJECT, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `product strategy prefers position over exposure drift`() {
        val m = match(ComparisonStrategy.PRODUCT_STRATEGY) {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER),
                subjectPositionMatch = drifting(ReferenceDriftDirection.LEFT),
            )
        }
        assertEquals(AssistantAction.PAN_LEFT, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `landscape strategy prefers composition over exposure drift`() {
        val m = match(ComparisonStrategy.LANDSCAPE_STRATEGY) {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER),
                compositionMatch = drifting(ReferenceDriftDirection.DIFFERENT),
            )
        }
        assertEquals(AssistantAction.REFRAME, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `same drift set picks differently under different strategies`() {
        val build: ReferenceMatchResult.() -> ReferenceMatchResult = {
            copy(
                compositionMatch = drifting(ReferenceDriftDirection.DIFFERENT),
                subjectSizeMatch = drifting(ReferenceDriftDirection.SMALLER),
            )
        }
        assertEquals(
            AssistantAction.MOVE_CLOSER,
            InstructionSelector.select(match(ComparisonStrategy.PRODUCT_STRATEGY, build))!!.action,
        )
        assertEquals(
            AssistantAction.REFRAME,
            InstructionSelector.select(match(ComparisonStrategy.GENERIC_SCENE_STRATEGY, build))!!.action,
        )
    }

    @Test
    fun `confirmed drift outranks a higher-priority unconfirmed one`() {
        val m = match(ComparisonStrategy.HUMAN_STRATEGY) {
            copy(
                facePresenceMatch = drifting(ReferenceDriftDirection.MISSING, confirmed = false),
                exposureMatch = drifting(ReferenceDriftDirection.DARKER, confirmed = true),
            )
        }
        assertEquals(AssistantAction.INCREASE_EXPOSURE, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `selection is deterministic`() {
        val m = match(ComparisonStrategy.HUMAN_STRATEGY) {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER),
                whiteBalanceMatch = drifting(ReferenceDriftDirection.WARMER),
                subjectPositionMatch = drifting(ReferenceDriftDirection.LEFT),
            )
        }
        val first = InstructionSelector.select(m)
        repeat(10) { assertEquals(first, InstructionSelector.select(m)) }
    }

    @Test
    fun `creative-aware selection surfaces the more important lower-priority drift`() {
        // EXPOSURE outranks COMPOSITION in the fixed HUMAN priority list, but this reference's
        // creative identity leans on composition, so its importance is far higher.
        val build: ReferenceMatchResult.() -> ReferenceMatchResult = {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER, importance = 0.2f),
                compositionMatch = drifting(ReferenceDriftDirection.DIFFERENT, importance = 0.95f),
            )
        }
        // Not creative-aware → fixed priority (exposure wins).
        assertEquals(
            AssistantAction.INCREASE_EXPOSURE,
            InstructionSelector.select(match(ComparisonStrategy.HUMAN_STRATEGY, build))!!.action,
        )
        // Creative-aware → honors importance (composition wins).
        val creative: ReferenceMatchResult.() -> ReferenceMatchResult = {
            build().copy(creativeAware = true)
        }
        assertEquals(
            AssistantAction.REFRAME,
            InstructionSelector.select(match(ComparisonStrategy.HUMAN_STRATEGY, creative))!!.action,
        )
    }

    @Test
    fun `creative-aware with equal importance falls back to fixed priority`() {
        val m = match(ComparisonStrategy.HUMAN_STRATEGY) {
            copy(
                exposureMatch = drifting(ReferenceDriftDirection.DARKER, importance = 0.6f),
                compositionMatch = drifting(ReferenceDriftDirection.DIFFERENT, importance = 0.6f),
                creativeAware = true,
            )
        }
        // Equal importance → stable tie-break on the fixed priority order (exposure precedes composition).
        assertEquals(AssistantAction.INCREASE_EXPOSURE, InstructionSelector.select(m)!!.action)
    }

    @Test
    fun `every strategy priority list covers exactly its applicable signals`() {
        for (strategy in ComparisonStrategy.entries) {
            assertEquals(
                "strategy=$strategy",
                ReferenceSignalApplicability.signalsFor(strategy),
                InstructionSelector.priorityFor(strategy).toSet(),
            )
            // Strict list: no duplicates.
            assertEquals(
                InstructionSelector.priorityFor(strategy).size,
                InstructionSelector.priorityFor(strategy).toSet().size,
            )
        }
    }
}
