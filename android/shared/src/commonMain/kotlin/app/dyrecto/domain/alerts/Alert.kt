package app.dyrecto.domain.alerts

/**
 * A single generated alert, ready for history + delivery. Immutable and platform-agnostic.
 *
 * @param id         monotonic per-session id (newest = largest), assigned by the engine
 * @param type       which camera event fired
 * @param severity   resolved severity (normally [AlertType.defaultSeverity])
 * @param title      short headline (normally [AlertType.title])
 * @param message    human-readable detail, e.g. "Slot 2" or "Battery at 9%"
 * @param timestamp  epoch millis when the alert was generated
 */
data class Alert(
    val id: Long,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val timestamp: Long,
)
