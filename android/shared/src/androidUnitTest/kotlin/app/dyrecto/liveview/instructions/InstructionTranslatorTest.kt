package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.perception.PerceptualSeverity
import app.dyrecto.liveview.perception.PerceptualSignal
import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for [InstructionTranslator] — every existing perceptual output maps to the
 * expected operator instruction (Phase 13 spec table), directions are never invented under low
 * direction confidence, and translation never mutates anything but the instruction slot.
 */
class InstructionTranslatorTest {

    private fun perception(
        direction: ReferenceDriftDirection,
        severity: PerceptualSeverity = PerceptualSeverity.NOTICEABLE,
        confidence: Float = 0.9f,
        directionConfidence: Float = 0.9f,
        drifting: Boolean = true,
    ) = PerceptualSignal(
        rawDifference = 1f,
        perceptualDifference = 0.5f,
        humanNoticeability = 0.5f,
        importance = 0.5f,
        confidence = confidence,
        directionConfidence = directionConfidence,
        deadZoneApplied = 0.1f,
        severity = severity,
        perceptuallyDrifting = drifting,
        direction = direction,
    )

    private fun drifting(
        direction: ReferenceDriftDirection,
        severity: PerceptualSeverity = PerceptualSeverity.NOTICEABLE,
        directionConfidence: Float = 0.9f,
        confidence: Float = 0.9f,
        score: Float = 0.4f,
        message: String = "perceptual description",
    ) = ReferenceSignalResult(
        enabled = true,
        available = true,
        matched = false,
        score = score,
        direction = direction,
        message = message,
        perception = perception(direction, severity, confidence, directionConfidence),
    )

    private fun action(signal: ReferenceSignal, direction: ReferenceDriftDirection): AssistantAction? =
        InstructionTranslator.translate(signal, drifting(direction))?.action

    // ---- Spec mapping table ----

    @Test
    fun `exposure darker maps to increase exposure`() =
        assertEquals(AssistantAction.INCREASE_EXPOSURE, action(ReferenceSignal.EXPOSURE, ReferenceDriftDirection.DARKER))

    @Test
    fun `exposure brighter maps to reduce exposure`() =
        assertEquals(AssistantAction.REDUCE_EXPOSURE, action(ReferenceSignal.EXPOSURE, ReferenceDriftDirection.BRIGHTER))

    @Test
    fun `subject left maps to pan left`() =
        assertEquals(AssistantAction.PAN_LEFT, action(ReferenceSignal.SUBJECT_POSITION, ReferenceDriftDirection.LEFT))

    @Test
    fun `subject right maps to pan right`() =
        assertEquals(AssistantAction.PAN_RIGHT, action(ReferenceSignal.SUBJECT_POSITION, ReferenceDriftDirection.RIGHT))

    @Test
    fun `subject higher maps to tilt up`() =
        assertEquals(AssistantAction.TILT_UP, action(ReferenceSignal.SUBJECT_POSITION, ReferenceDriftDirection.UP))

    @Test
    fun `subject lower maps to tilt down`() =
        assertEquals(AssistantAction.TILT_DOWN, action(ReferenceSignal.SUBJECT_POSITION, ReferenceDriftDirection.DOWN))

    @Test
    fun `headroom drift maps to tilt`() {
        assertEquals(AssistantAction.TILT_UP, action(ReferenceSignal.HEADROOM, ReferenceDriftDirection.UP))
        assertEquals(AssistantAction.TILT_DOWN, action(ReferenceSignal.HEADROOM, ReferenceDriftDirection.DOWN))
    }

    @Test
    fun `subject smaller maps to move closer`() =
        assertEquals(AssistantAction.MOVE_CLOSER, action(ReferenceSignal.SUBJECT_SIZE, ReferenceDriftDirection.SMALLER))

    @Test
    fun `subject larger maps to move back`() =
        assertEquals(AssistantAction.MOVE_BACK, action(ReferenceSignal.SUBJECT_SIZE, ReferenceDriftDirection.LARGER))

    @Test
    fun `warm white balance maps to cool white balance`() =
        assertEquals(AssistantAction.COOL_WHITE_BALANCE, action(ReferenceSignal.WHITE_BALANCE, ReferenceDriftDirection.WARMER))

    @Test
    fun `cool white balance maps to warm white balance`() =
        assertEquals(AssistantAction.WARM_WHITE_BALANCE, action(ReferenceSignal.WHITE_BALANCE, ReferenceDriftDirection.COOLER))

    @Test
    fun `tint-only white balance drift has no invented axis`() =
        assertEquals(AssistantAction.MATCH_REFERENCE, action(ReferenceSignal.WHITE_BALANCE, ReferenceDriftDirection.UNKNOWN))

    @Test
    fun `missing subject maps to wait for subject`() {
        assertEquals(AssistantAction.WAIT_FOR_SUBJECT, action(ReferenceSignal.SUBJECT_PRESENCE, ReferenceDriftDirection.MISSING))
        assertEquals(AssistantAction.WAIT_FOR_SUBJECT, action(ReferenceSignal.FACE_PRESENCE, ReferenceDriftDirection.MISSING))
        assertEquals(AssistantAction.WAIT_FOR_SUBJECT, action(ReferenceSignal.EYE_VISIBILITY, ReferenceDriftDirection.MISSING))
    }

    @Test
    fun `composition drift maps to reframe`() =
        assertEquals(AssistantAction.REFRAME, action(ReferenceSignal.COMPOSITION, ReferenceDriftDirection.DIFFERENT))

    @Test
    fun `low visual similarity maps to match reference`() =
        assertEquals(AssistantAction.MATCH_REFERENCE, action(ReferenceSignal.VISUAL_SIMILARITY, ReferenceDriftDirection.DIFFERENT))

    // ---- Direction confidence gate ----

    @Test
    fun `low direction confidence falls back to reframe instead of a guessed direction`() {
        val result = drifting(ReferenceDriftDirection.LEFT, directionConfidence = 0.1f)
        val instruction = InstructionTranslator.translate(ReferenceSignal.SUBJECT_POSITION, result)!!
        assertEquals(AssistantAction.REFRAME, instruction.action)
        assertEquals("Reframe to match the reference.", instruction.message)
    }

    @Test
    fun `low direction confidence does not affect non-directional actions`() {
        val result = drifting(ReferenceDriftDirection.MISSING, directionConfidence = 0f)
        assertEquals(
            AssistantAction.WAIT_FOR_SUBJECT,
            InstructionTranslator.translate(ReferenceSignal.SUBJECT_PRESENCE, result)!!.action,
        )
    }

    // ---- No instruction cases ----

    @Test
    fun `matched signal produces no instruction`() {
        val matched = drifting(ReferenceDriftDirection.NONE).copy(matched = true)
        assertNull(InstructionTranslator.translate(ReferenceSignal.EXPOSURE, matched))
    }

    @Test
    fun `unavailable or disabled signal produces no instruction`() {
        val base = drifting(ReferenceDriftDirection.DARKER)
        assertNull(InstructionTranslator.translate(ReferenceSignal.EXPOSURE, base.copy(available = false)))
        assertNull(InstructionTranslator.translate(ReferenceSignal.EXPOSURE, base.copy(enabled = false)))
    }

    @Test
    fun `signal without a perceptual verdict produces no instruction`() {
        val raw = drifting(ReferenceDriftDirection.DARKER).copy(perception = null)
        assertNull(InstructionTranslator.translate(ReferenceSignal.EXPOSURE, raw))
    }

    // ---- Instruction contents ----

    @Test
    fun `instruction carries severity adverb, score progress, perceptual confidence and reason`() {
        val result = drifting(
            ReferenceDriftDirection.DARKER,
            severity = PerceptualSeverity.SEVERE,
            confidence = 0.8f,
            score = 0.3f,
            message = "Exposure is severely darker than the reference.",
        )
        val instruction = InstructionTranslator.translate(ReferenceSignal.EXPOSURE, result)!!
        assertEquals("Increase exposure severely.", instruction.message)
        assertEquals(0.3f, instruction.progress)
        assertEquals(0.8f, instruction.confidence)
        assertEquals("Exposure is severely darker than the reference.", instruction.reason)
    }

    // ---- annotate ----

    @Test
    fun `annotate sets only the instruction slot and preserves everything else`() {
        val exposure = drifting(ReferenceDriftDirection.DARKER, message = "Exposure is noticeably darker than the reference.")
        val match = ReferenceMatchResult(active = true, referenceId = "r", exposureMatch = exposure)

        val annotated = InstructionTranslator.annotate(match)
        val out = annotated.exposureMatch

        assertEquals(AssistantAction.INCREASE_EXPOSURE, out.instruction!!.action)
        // Perceptual message never rewritten; all other fields bit-identical.
        assertEquals(exposure, out.copy(instruction = null))
        // Untouched signals stay untouched.
        assertEquals(match.whiteBalanceMatch, annotated.whiteBalanceMatch)
        assertNull(annotated.whiteBalanceMatch.instruction)
    }

    @Test
    fun `annotate covers every drifting signal`() {
        val match = ReferenceMatchResult(
            active = true,
            referenceId = "r",
            exposureMatch = drifting(ReferenceDriftDirection.BRIGHTER),
            subjectPositionMatch = drifting(ReferenceDriftDirection.RIGHT),
            visualSimilarityMatch = drifting(ReferenceDriftDirection.DIFFERENT),
        )
        val annotated = InstructionTranslator.annotate(match)
        assertEquals(AssistantAction.REDUCE_EXPOSURE, annotated.exposureMatch.instruction!!.action)
        assertEquals(AssistantAction.PAN_RIGHT, annotated.subjectPositionMatch.instruction!!.action)
        assertEquals(AssistantAction.MATCH_REFERENCE, annotated.visualSimilarityMatch.instruction!!.action)
        assertTrue(
            ReferenceSignal.entries
                .filter { annotated.signal(it).instruction == null }
                .all { annotated.signal(it).matched || annotated.signal(it).perception == null },
        )
    }
}
