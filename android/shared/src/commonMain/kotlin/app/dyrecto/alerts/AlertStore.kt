package app.dyrecto.alerts

import app.dyrecto.domain.alerts.Alert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Isolated alert history, in the spirit of
 * [app.dyrecto.debug.TelemetryEventStore] — a dedicated system, not mixed with logs.
 *
 * Newest-first, capped at [MAX_ALERTS]. Thread-safe (lock-free StateFlow updates). Lifecycle is
 * session-owned: [clear] is called from the ViewModel when a new connection session starts, never
 * from the telemetry listener.
 */
object AlertStore {

    private const val MAX_ALERTS = 200

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    /** All alerts this session, newest-first, capped at [MAX_ALERTS]. */
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    /** Prepends [alert] (newest-first) and trims to the cap. */
    fun record(alert: Alert) {
        _alerts.update { current ->
            val combined = listOf(alert) + current
            if (combined.size > MAX_ALERTS) combined.take(MAX_ALERTS) else combined
        }
    }

    /** Drops all history. Called at the start of each new connection session and on user clear. */
    fun clear() {
        _alerts.value = emptyList()
    }
}
