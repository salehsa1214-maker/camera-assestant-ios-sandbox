package app.dyrecto.debug

data class TelemetryEvent(
    val timestamp: Long,
    val batchId: Int,
    val propertyCode: Int,
    val propertyLabel: String,
    val rawValueBefore: String?,
    val rawValueAfter: String?,
    val decodedValue: String?,
    val eventType: EventType,
) {
    enum class EventType { FIRST_SEEN, CHANGED }
}
