package app.dyrecto.alerts

import app.dyrecto.domain.alerts.AlertPattern
import app.dyrecto.domain.alerts.AlertSeverity
import kotlinx.coroutines.flow.StateFlow

/**
 * Delivery seam for alerts. Generation ([app.dyrecto.domain.alerts.AlertEngine]) and
 * history ([AlertStore]) are independent of how an alert reaches the user, so future channels —
 * Android notifications, a foreground service, lock-screen alerts, wearables, voice — are added by
 * implementing this interface and registering it at the call site, with no engine changes.
 *
 * Delivery is fully configurable: [severity] selects the tone character/intensity, while the
 * [sound] and [vibration] patterns drive how many times each channel fires and the gap between
 * repetitions. V1 ships [AndroidAlertFeedback] (in-app sound + haptic).
 */
interface AlertFeedback {
    /**
     * Deliver an alert through this channel. Must not block the caller. Implementations may drop a
     * call that overlaps an in-flight pattern (see [playing]) to avoid stacking.
     */
    fun deliver(severity: AlertSeverity, sound: AlertPattern, vibration: AlertPattern)

    /** True while a pattern is actively playing; lets callers/UI avoid overlapping (test) triggers. */
    val playing: StateFlow<Boolean>
}
