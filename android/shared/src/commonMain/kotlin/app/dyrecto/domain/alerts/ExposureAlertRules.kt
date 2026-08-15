package app.dyrecto.domain.alerts

import app.dyrecto.liveview.scene.SceneContext

/**
 * Pure, platform-agnostic translator of confirmed exposure transitions into [Alert]s (Phase 7).
 * Sibling of [SceneAlertRules] — same transition-only, single-fire latch idiom, same shared
 * [AlertIdGenerator] — so exposure alerts flow through the *one* existing alert lifecycle
 * (history, config, feedback, notifications) with no parallel machinery.
 *
 * Deliberately consumes [SceneContext] rather than
 * [app.dyrecto.liveview.vision.results.ExposureResult]: since
 * [SceneContext.ExposureState.isHighlightClipped] / `isShadowClipped` now carry the *confirmed*
 * (debounced) state produced by
 * [app.dyrecto.liveview.exposure.ExposureStateMachine], the alert layer only needs
 * the already-interpreted Scene signal — exactly like every other alert rule — and stays fully
 * decoupled from the Vision/exposure implementation.
 *
 * This object is stateless. All lifecycle state — the per-rule latches — is carried in the
 * immutable [ExposureAlertState] threaded by the coordinator
 * ([app.dyrecto.service.DefaultMonitoringSession]), which owns reset (a fresh
 * [ExposureAlertState] at the start of each session) just as it owns [AlertEngine.reset] and the
 * [SceneAlertState] reset.
 */
object ExposureAlertRules {

    /** New alerts produced this tick plus the [ExposureAlertState] to carry into the next call. */
    data class Result(val alerts: List<Alert>, val state: ExposureAlertState)

    /**
     * Diffs [scene]'s confirmed exposure flags against [state] and returns any alerts the
     * transition produced, together with the updated state. The first call (when
     * [ExposureAlertState.seeded] is false) only seeds the baseline — it never fires, avoiding a
     * burst when Live View first starts with a condition already confirmed.
     */
    fun evaluate(state: ExposureAlertState, scene: SceneContext, idGen: AlertIdGenerator): Result {
        if (!state.seeded) {
            return Result(
                emptyList(),
                ExposureAlertState(
                    seeded = true,
                    highlightClipFired = scene.exposure.isHighlightClipped,
                    shadowClipFired = scene.exposure.isShadowClipped,
                ),
            )
        }

        val now = scene.updatedAtMs
        val alerts = mutableListOf<Alert>()

        var highlightFired = state.highlightClipFired
        var shadowFired = state.shadowClipFired

        // ---- Highlight clipping (single-fire on the confirmed edge; recovered fires once on the
        // way back, only if clipping was actually confirmed; then re-arms) ----
        if (scene.exposure.isHighlightClipped) {
            if (!highlightFired) {
                highlightFired = true
                alerts += alert(AlertType.HIGHLIGHT_CLIPPING, "Bright areas are losing detail.", now, idGen)
            }
        } else if (highlightFired) {
            highlightFired = false
            alerts += alert(AlertType.HIGHLIGHT_RECOVERED, "Highlight detail has recovered.", now, idGen)
        }

        // ---- Shadow detail loss ----
        if (scene.exposure.isShadowClipped) {
            if (!shadowFired) {
                shadowFired = true
                alerts += alert(AlertType.SHADOW_CLIPPING, "Dark areas are losing detail.", now, idGen)
            }
        } else if (shadowFired) {
            shadowFired = false
            alerts += alert(AlertType.SHADOW_RECOVERED, "Shadow detail has recovered.", now, idGen)
        }

        return Result(
            alerts = alerts,
            state = ExposureAlertState(
                seeded = true,
                highlightClipFired = highlightFired,
                shadowClipFired = shadowFired,
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
 * Immutable lifecycle state for [ExposureAlertRules], owned and threaded by the coordinator. A
 * fresh instance (all defaults) is the session-reset state.
 */
data class ExposureAlertState(
    val seeded: Boolean = false,
    val highlightClipFired: Boolean = false,
    val shadowClipFired: Boolean = false,
)
