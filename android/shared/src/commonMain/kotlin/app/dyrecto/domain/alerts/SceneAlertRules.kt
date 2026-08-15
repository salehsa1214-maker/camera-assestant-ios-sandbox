package app.dyrecto.domain.alerts

import app.dyrecto.liveview.scene.SceneContext

/**
 * Pure, platform-agnostic translator of [SceneContext] face/eye transitions into [Alert]s (Phase
 * 5D). It is the Vision/Scene counterpart of the telemetry [AlertEngine]: same transition-only,
 * single-fire latch idiom, same shared [AlertIdGenerator] — so Scene-derived alerts flow through
 * the *one* existing alert lifecycle (history, config, feedback, notifications) with no parallel
 * machinery.
 *
 * Exposure alerts (highlight/shadow clipping + recovery) moved to [ExposureAlertRules] in Phase 7,
 * so they can key off the *confirmed* (debounced) exposure state rather than a single frame; this
 * object now only covers face/eye rules.
 *
 * This object is stateless. All lifecycle state — the previous scene and the per-rule latches — is
 * carried in the immutable [SceneAlertState] threaded by the coordinator
 * ([app.dyrecto.service.DefaultMonitoringSession]), which owns reset (a fresh
 * [SceneAlertState] at the start of each session) just as it owns [AlertEngine.reset].
 *
 * Holds no Android types and never touches Vision modules, the renderer, or [SceneContext]
 * production — it only reads the descriptive flags — so it is fully unit-testable.
 */
object SceneAlertRules {

    /** New alerts produced this tick plus the [SceneAlertState] to carry into the next call. */
    data class Result(val alerts: List<Alert>, val state: SceneAlertState)

    /**
     * Diffs [scene] against [state] and returns any alerts the transition produced, together with
     * the updated state. The first call (when [SceneAlertState.previousScene] is null) only seeds the
     * baseline — it never fires, avoiding a burst when Live View first starts.
     */
    fun evaluate(state: SceneAlertState, scene: SceneContext, idGen: AlertIdGenerator): Result {
        // First snapshot: seed the baseline only, never fire. Latches are seeded from the starting
        // scene so a condition already present when Live View opens (e.g. clipped exposure, a face
        // already missing its eyes) does not produce a burst on the next frame — mirrors the battery
        // latch seeding in AlertEngine.
        if (state.previousScene == null) {
            val faceContext = scene.connection.liveViewActive && scene.recording.isRecording
            val tracking = faceContext && scene.face.hasVisibleFace
            return Result(
                emptyList(),
                SceneAlertState(
                    previousScene = scene,
                    faceTrackingActive = tracking,
                    eyesNotVisibleFired = tracking && !scene.face.hasVisibleEyes,
                ),
            )
        }

        val now = scene.updatedAtMs
        val alerts = mutableListOf<Alert>()

        var faceTracking = state.faceTrackingActive
        var eyesFired = state.eyesNotVisibleFired

        // ---- Face: acquisition-gated tracking ----
        // Face alerts are only meaningful once a face has actually been acquired while the user is
        // genuinely shooting (Live View active AND recording). This avoids nuisance "Face Lost"
        // alerts during landscape/architecture/product/empty-scene recordings where no face is ever
        // intended. Tracking resets when shooting context ends, requiring a fresh acquisition.
        val faceContext = scene.connection.liveViewActive && scene.recording.isRecording

        if (!faceContext) {
            // Left the shooting context: drop tracking and re-arm eyes; wait for a new acquisition.
            faceTracking = false
            eyesFired = false
        } else if (!faceTracking) {
            // Eligible to acquire: first visible face turns tracking on (no alert).
            if (scene.face.hasVisibleFace) faceTracking = true
        } else {
            // Tracking is active within the shooting context.
            if (!scene.face.hasVisibleFace) {
                // Acquired face disappeared -> Face Lost, then reset to await re-acquisition.
                alerts += alert(AlertType.FACE_LOST, "No visible face detected.", now, idGen)
                faceTracking = false
                eyesFired = false
            } else {
                // Face still present: evaluate Eyes Not Visible only while tracking + face visible.
                if (scene.face.hasVisibleEyes) {
                    eyesFired = false
                } else if (!eyesFired) {
                    eyesFired = true
                    alerts += alert(AlertType.EYES_NOT_VISIBLE, "Eye visibility has been lost.", now, idGen)
                }
            }
        }

        return Result(
            alerts = alerts,
            state = SceneAlertState(
                previousScene = scene,
                faceTrackingActive = faceTracking,
                eyesNotVisibleFired = eyesFired,
            ),
        )
    }

    private fun alert(type: AlertType, message: String, now: Long, idGen: AlertIdGenerator): Alert =
        Alert(
            id = idGen.next(),
            type = type,
            severity = type.defaultSeverity,
            title = type.title,
            message = message,
            timestamp = now,
        )
}

/**
 * Immutable lifecycle state for [SceneAlertRules], owned and threaded by the coordinator. A fresh
 * instance (all defaults) is the session-reset state.
 */
data class SceneAlertState(
    val previousScene: SceneContext? = null,
    /** Face alerts are acquisition-gated: only meaningful once a face has actually been seen. */
    val faceTrackingActive: Boolean = false,
    val eyesNotVisibleFired: Boolean = false,
)
