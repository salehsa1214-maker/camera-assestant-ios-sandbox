package app.dyrecto.domain.alerts

/**
 * The catalogue of camera events the Alert Engine can raise. V1 covers recording, battery,
 * media, thermal, and connection. Each type carries its display [title], [defaultSeverity], and
 * [category]; new alert types are added here (with a category) and automatically appear in the
 * Alert Control Center, grouped under their category — without touching the engine or the UI.
 */
enum class AlertType(
    val title: String,
    val defaultSeverity: AlertSeverity,
    val category: AlertCategory,
) {
    RECORDING_STARTED("Recording Started", AlertSeverity.INFO, AlertCategory.RECORDING),
    RECORDING_STOPPED("Recording Stopped", AlertSeverity.WARNING, AlertCategory.RECORDING),
    BATTERY_LOW_20("Battery Below 20%", AlertSeverity.WARNING, AlertCategory.BATTERY),
    BATTERY_LOW_10("Battery Below 10%", AlertSeverity.CRITICAL, AlertCategory.BATTERY),
    BATTERY_LOW_5("Battery Below 5%", AlertSeverity.CRITICAL, AlertCategory.BATTERY),
    BATTERY_TIME_10_MIN("Battery Time Below 10 Min", AlertSeverity.WARNING, AlertCategory.BATTERY),
    BATTERY_TIME_5_MIN("Battery Time Below 5 Min", AlertSeverity.CRITICAL, AlertCategory.BATTERY),
    CARD_INSERTED("Card Inserted", AlertSeverity.INFO, AlertCategory.MEDIA),
    CARD_REMOVED("Card Removed", AlertSeverity.CRITICAL, AlertCategory.MEDIA),
    OVERHEATING("Overheating Detected", AlertSeverity.CRITICAL, AlertCategory.THERMAL),
    CONNECTION_LOST("Camera Connection Lost", AlertSeverity.CRITICAL, AlertCategory.CONNECTION),

    // ---- Phase 5D: Scene-derived alerts (from SceneContext, not telemetry) ----
    HIGHLIGHT_CLIPPING("Highlights Clipping", AlertSeverity.WARNING, AlertCategory.EXPOSURE),
    SHADOW_CLIPPING("Shadow Detail Loss", AlertSeverity.WARNING, AlertCategory.EXPOSURE),
    FACE_LOST("Face Lost", AlertSeverity.INFO, AlertCategory.FACE),
    EYES_NOT_VISIBLE("Eyes Not Visible", AlertSeverity.WARNING, AlertCategory.FACE),

    // ---- Phase 7: confirmed-exposure recovery alerts (from SceneContext, debounced) ----
    HIGHLIGHT_RECOVERED("Highlights Recovered", AlertSeverity.INFO, AlertCategory.EXPOSURE),
    SHADOW_RECOVERED("Shadow Recovered", AlertSeverity.INFO, AlertCategory.EXPOSURE),

    // ---- Phase 9: Shot Reference drift alerts (from ReferenceMatchResult, debounced) ----
    REFERENCE_EXPOSURE_DRIFT("Reference Exposure Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_WHITE_BALANCE_DRIFT("Reference White Balance Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_SUBJECT_POSITION_DRIFT("Reference Subject Position Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_SUBJECT_SIZE_DRIFT("Reference Subject Size Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_HEADROOM_DRIFT("Reference Headroom Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    // Phase 10 retitle: "Subject Missing" now belongs to REFERENCE_SUBJECT_MISSING (any
    // semantic subject); this one is specifically the human face.
    REFERENCE_FACE_MISSING("Reference Face Missing", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_EYES_MISSING("Reference Eyes Not Visible", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_RECOVERED("Reference Match Restored", AlertSeverity.INFO, AlertCategory.REFERENCE),

    // ---- Phase 10: AI Reference Assistant drift alerts (any-subject perception) ----
    REFERENCE_SUBJECT_MISSING("Reference Subject Missing", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_COMPOSITION_DRIFT("Reference Composition Drift", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    REFERENCE_VISUAL_MISMATCH("Reference Look Mismatch", AlertSeverity.WARNING, AlertCategory.REFERENCE),

    // ---- Phase 15: Storyboard end-of-session reminder ----
    STORYBOARD_INCOMPLETE("Storyboard Not Completed", AlertSeverity.WARNING, AlertCategory.REFERENCE),

    // ---- Reference-linked camera-settings drift (from live camera telemetry vs the shot's
    //      captured settings). Authoritative: read from the camera, not estimated from pixels. ----
    SETTINGS_DRIFT("Camera Settings Changed", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    PICTURE_PROFILE_CHANGED("Picture Profile Changed", AlertSeverity.WARNING, AlertCategory.REFERENCE),
    FRAME_RATE_MISMATCH("Frame Rate Mismatch", AlertSeverity.WARNING, AlertCategory.REFERENCE),
}
