package app.dyrecto.domain.alerts

import app.dyrecto.platform.AtomicLong

/**
 * Per-session monotonic source of [Alert.id]s, shared by every alert producer (the telemetry
 * [AlertEngine] and the Scene-derived [SceneAlertRules]) so their ids never collide and stay
 * globally ordered (newest = largest). Allocation is decoupled from any single producer, which keeps
 * the architecture clean as more alert sources are added.
 *
 * Lifecycle is session-owned: the coordinator calls [reset] at the start of a new connection
 * session, exactly where it resets the producers and clears the [AlertStore].
 */
class AlertIdGenerator {
    private val counter = AtomicLong(0)

    /** Returns the next id and advances the counter. */
    fun next(): Long = counter.getAndIncrement()

    /** Restarts ids from 0. Call when a new connection session begins. */
    fun reset() {
        counter.set(0)
    }
}
