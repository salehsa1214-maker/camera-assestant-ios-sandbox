package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.perception.PerceptualSeverity

/**
 * The action → sentence table (Phase 13). Directional actions take a severity adverb (same
 * buckets as `PerceptualMessages`) so guidance keeps the operator's sense of magnitude:
 * "Pan the camera left slightly." vs "Pan the camera left severely.".
 */
object InstructionTemplates {

    /** Severity → adverb, mirroring the perceptual wording so alert language stays consistent. */
    fun adverb(severity: PerceptualSeverity): String = when (severity) {
        PerceptualSeverity.NONE, PerceptualSeverity.SUBTLE -> "slightly"
        PerceptualSeverity.NOTICEABLE -> "noticeably"
        PerceptualSeverity.OBVIOUS -> "clearly"
        PerceptualSeverity.SEVERE -> "severely"
    }

    /** Sentence for [action]; `%s` is the adverb slot (absent for non-graded actions). */
    fun template(action: AssistantAction): String = when (action) {
        AssistantAction.PAN_LEFT -> "Pan the camera left %s."
        AssistantAction.PAN_RIGHT -> "Pan the camera right %s."
        AssistantAction.TILT_UP -> "Tilt the camera up %s."
        AssistantAction.TILT_DOWN -> "Tilt the camera down %s."
        AssistantAction.MOVE_CLOSER -> "Move closer to the subject %s."
        AssistantAction.MOVE_BACK -> "Move back from the subject %s."
        AssistantAction.ZOOM_IN -> "Zoom in %s."
        AssistantAction.ZOOM_OUT -> "Zoom out %s."
        AssistantAction.INCREASE_EXPOSURE -> "Increase exposure %s."
        AssistantAction.REDUCE_EXPOSURE -> "Reduce exposure %s."
        AssistantAction.COOL_WHITE_BALANCE -> "Cool the white balance %s."
        AssistantAction.WARM_WHITE_BALANCE -> "Warm the white balance %s."
        AssistantAction.WAIT_FOR_SUBJECT -> "Wait for the subject to return."
        AssistantAction.REFRAME -> "Reframe to match the reference."
        AssistantAction.MATCH_REFERENCE -> "Adjust the shot to match the reference."
        AssistantAction.NONE -> ""
    }
}
