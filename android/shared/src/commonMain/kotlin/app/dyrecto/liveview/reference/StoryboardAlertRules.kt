package app.dyrecto.liveview.reference

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType

/**
 * Phase 15: the storyboard end-of-session reminder. Pure and stateless — the coordinator
 * (`DefaultMonitoringSession`) calls [onSessionEnded] exactly on the edge where a live reference
 * monitoring session ends (user Stop, camera disconnect, or leaving the session). This is NOT a
 * second alert system: the produced [Alert] flows through the same shared delivery seam as every
 * other alert (store → config gate → feedback → notification).
 *
 * Behavior: if the storyboard has shots and not all of them are Completed, produce one WARNING
 * reminder (notification + spoken); if every shot is completed (or there are no shots), produce
 * nothing.
 */
object StoryboardAlertRules {

    fun onSessionEnded(state: ReferenceSessionState, now: Long, idGen: AlertIdGenerator): Alert? {
        val total = state.totalCount
        if (total == 0 || state.completedCount >= total) return null
        return Alert(
            id = idGen.next(),
            type = AlertType.STORYBOARD_INCOMPLETE,
            severity = AlertType.STORYBOARD_INCOMPLETE.defaultSeverity,
            title = AlertType.STORYBOARD_INCOMPLETE.title,
            message = "Storyboard not completed yet. Continue shooting to complete all planned shots.",
            timestamp = now,
        )
    }
}
