package app.dyrecto.liveview.scene

/**
 * Single descriptive label for the current shooting situation (Phase 5C).
 *
 * This is purely descriptive — it is NOT a priority ordering, a severity, or an alert. The
 * Scene Understanding Engine selects exactly one of these to summarize what is happening; future
 * phases (Alert Engine, Overlay, Assistant) decide what, if anything, to do about it.
 */
enum class SceneState {
    NORMAL,
    RECORDING,
    FACE_VISIBLE,
    LOW_BATTERY,
    STORAGE_LOW,
    HIGHLIGHT_PRESENT,
    SHADOW_PRESENT,
}
