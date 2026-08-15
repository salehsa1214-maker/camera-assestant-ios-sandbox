package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction

/**
 * The spoken-phrase tables (Phase 14). Deliberately SHORTER than the visual
 * `InstructionTemplates` sentences — speech has no severity adverbs and no detail suffixes;
 * one calm sentence per event, maximum.
 *
 * Also the single place that classifies telemetry for voice: which alert types are critical
 * (interrupt everything, no cooldown), which are spoken normally, and which are not voiced at
 * all. Notably every reference *drift* alert returns null here — the assistant channel already
 * speaks the corrective action for the same drift, and voicing both would double-speak every
 * drift. The one reference-category exception is `STORYBOARD_INCOMPLETE`: it is an end-of-session
 * reminder with no assistant-channel equivalent, so it IS spoken. Visual reference alerts are
 * unchanged.
 */
object VoiceTemplates {

    /** Spoken while a guided drift episode resolves; keyed by [VoiceKey.ReferenceMatched]. */
    const val REFERENCE_MATCHED = "Reference matched."

    /** Spoken once when a storyboard shot completes; keyed by [VoiceKey.ShotCompleted] (Phase 15). */
    const val SHOT_COMPLETED = "Shot completed."

    /** Spoken phrase for an assistant action, or null when nothing should be said (NONE). */
    fun forAction(action: AssistantAction): String? = when (action) {
        AssistantAction.PAN_LEFT -> "Pan left slightly."
        AssistantAction.PAN_RIGHT -> "Pan right slightly."
        AssistantAction.TILT_UP -> "Tilt up slightly."
        AssistantAction.TILT_DOWN -> "Tilt down slightly."
        AssistantAction.MOVE_CLOSER -> "Move closer."
        AssistantAction.MOVE_BACK -> "Move back."
        AssistantAction.ZOOM_IN -> "Zoom in slightly."
        AssistantAction.ZOOM_OUT -> "Zoom out slightly."
        AssistantAction.INCREASE_EXPOSURE -> "Increase exposure slightly."
        AssistantAction.REDUCE_EXPOSURE -> "Reduce exposure slightly."
        AssistantAction.COOL_WHITE_BALANCE -> "Cool the white balance slightly."
        AssistantAction.WARM_WHITE_BALANCE -> "Warm the white balance slightly."
        AssistantAction.WAIT_FOR_SUBJECT -> "Wait for the subject."
        AssistantAction.REFRAME -> "Reframe to match the reference."
        AssistantAction.MATCH_REFERENCE -> "Adjust to match the reference."
        AssistantAction.NONE -> null
    }

    /** Spoken phrase for a telemetry alert, or null when the type is not voiced. */
    fun forAlert(type: AlertType): String? = when (type) {
        AlertType.RECORDING_STARTED -> "Recording started."
        AlertType.RECORDING_STOPPED -> "Recording stopped."
        AlertType.BATTERY_LOW_20 -> "Battery low."
        AlertType.BATTERY_LOW_10 -> "Battery critical."
        AlertType.BATTERY_LOW_5 -> "Battery critical."
        AlertType.BATTERY_TIME_10_MIN -> "Ten minutes of battery left."
        AlertType.BATTERY_TIME_5_MIN -> "Five minutes of battery left."
        AlertType.CARD_INSERTED -> null
        AlertType.CARD_REMOVED -> "Memory card removed."
        AlertType.OVERHEATING -> "Camera overheating."
        AlertType.CONNECTION_LOST -> "Camera disconnected."
        AlertType.HIGHLIGHT_CLIPPING -> "Highlights clipping."
        AlertType.SHADOW_CLIPPING -> "Shadows clipping."
        AlertType.FACE_LOST -> "Face lost."
        AlertType.EYES_NOT_VISIBLE -> "Eye autofocus lost."
        // Phase 15: end-of-session reminder — spoken (no assistant-channel equivalent to double it).
        AlertType.STORYBOARD_INCOMPLETE -> "Storyboard not completed yet."
        // Camera-settings drift: authoritative camera-state change with no corrective assistant
        // action to double it, so it is spoken (like the storyboard reminder).
        AlertType.SETTINGS_DRIFT -> "Camera settings changed from the reference."
        AlertType.PICTURE_PROFILE_CHANGED -> "Picture profile changed."
        AlertType.FRAME_RATE_MISMATCH -> "Frame rate does not match the reference."
        // Recoveries and all reference drifts are visual-only (assistant channel covers drifts).
        AlertType.HIGHLIGHT_RECOVERED,
        AlertType.SHADOW_RECOVERED,
        AlertType.REFERENCE_EXPOSURE_DRIFT,
        AlertType.REFERENCE_WHITE_BALANCE_DRIFT,
        AlertType.REFERENCE_SUBJECT_POSITION_DRIFT,
        AlertType.REFERENCE_SUBJECT_SIZE_DRIFT,
        AlertType.REFERENCE_HEADROOM_DRIFT,
        AlertType.REFERENCE_FACE_MISSING,
        AlertType.REFERENCE_EYES_MISSING,
        AlertType.REFERENCE_RECOVERED,
        AlertType.REFERENCE_SUBJECT_MISSING,
        AlertType.REFERENCE_COMPOSITION_DRIFT,
        AlertType.REFERENCE_VISUAL_MISMATCH,
        -> null
    }

    /**
     * Telemetry that interrupts everything: no cooldown on state change, bypasses the speech
     * gap, cuts off any interruptible utterance, and can itself never be interrupted.
     */
    fun isCriticalTelemetry(type: AlertType): Boolean = when (type) {
        AlertType.RECORDING_STOPPED,
        AlertType.BATTERY_LOW_10,
        AlertType.BATTERY_LOW_5,
        AlertType.BATTERY_TIME_5_MIN,
        AlertType.CARD_REMOVED,
        AlertType.OVERHEATING,
        AlertType.CONNECTION_LOST,
        -> true
        else -> false
    }

    fun telemetryPriority(type: AlertType): VoicePriority =
        if (isCriticalTelemetry(type)) VoicePriority.CRITICAL else VoicePriority.HIGH
}
