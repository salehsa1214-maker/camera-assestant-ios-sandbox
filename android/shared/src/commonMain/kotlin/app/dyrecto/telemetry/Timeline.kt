package app.dyrecto.telemetry

/**
 * The connection timeline shown on the Camera Diagnostics screen:
 *
 *   BLE Found → Connected → CC17 Read → SSH Authenticated → PTP Init
 *             → OpenSession → GetDeviceInfo
 *
 * Each reached stage carries a timestamp and the duration since the previous
 * stage. Platform-agnostic.
 */

enum class TimelineStage(val order: Int, val label: String) {
    BLE_FOUND(0, "BLE Found"),
    CONNECTED(1, "Connected"),
    AP_CREATED(2, "AP Created"),
    WIFI_JOINED(3, "Wi-Fi Joined"),
    IP_DISCOVERED(4, "IP Discovered"),
    CC17_READ(5, "CC17 Read"),
    SSH_AUTHENTICATED(6, "SSH Authenticated"),
    PTP_INIT(7, "PTP Init"),
    OPEN_SESSION(8, "OpenSession"),
    GET_DEVICE_INFO(9, "GetDeviceInfo");

    companion object {
        val ordered: List<TimelineStage> = entries.sortedBy { it.order }
    }
}

data class TimelineEvent(
    val stage: TimelineStage,
    val timestamp: Long,
    val durationFromPreviousMs: Long?,
    val ok: Boolean = true,
    val detail: String? = null,
)
