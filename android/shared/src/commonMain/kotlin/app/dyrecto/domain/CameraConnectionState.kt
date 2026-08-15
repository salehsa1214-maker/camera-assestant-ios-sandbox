package app.dyrecto.domain

import app.dyrecto.ble.BleStatus
import app.dyrecto.camera.CameraDeviceInfo
import app.dyrecto.ptp.PtpStatus
import app.dyrecto.ssh.SshStatus
import app.dyrecto.telemetry.TimelineEvent

/**
 * The single, immutable source of truth for the whole connection pipeline,
 * aggregated from every layer. ViewModels expose slices of this to each screen.
 */
enum class ConnectionPhase(val label: String) {
    IDLE("Idle"),
    SCANNING("Scanning"),
    BLE_FOUND("Camera found"),
    CONNECTING("Connecting"),
    BONDING("Bonding"),
    CONNECTED("Connected"),
    READING_INFO("Reading camera info"),
    // ---- Sony AP sequence phases (new) ----
    SMARTPHONE_MODE("Enabling smartphone mode"),
    AP_CREATING("Creating access point"),
    WIFI_CREDENTIALS("Reading Wi-Fi credentials"),
    WIFI_JOINING("Joining camera Wi-Fi"),
    IP_DISCOVERY("Discovering camera IP"),
    // ---- SSH / PTP phases ----
    CC17_READ("SSH info read"),
    SSH_CONNECTING("SSH connecting"),
    SSH_AUTHENTICATED("SSH authenticated"),
    PTP_INIT("PTP init"),
    SESSION_OPEN("Session open"),
    DEVICE_INFO("Device info retrieved"),
    ERROR("Error"),
}

data class CameraConnectionState(
    val phase: ConnectionPhase = ConnectionPhase.IDLE,
    val running: Boolean = false,
    val ble: BleStatus = BleStatus(),
    val ssh: SshStatus = SshStatus(),
    val ptp: PtpStatus = PtpStatus(),
    val deviceInfo: CameraDeviceInfo? = null,
    /** Parsed Sony 0x9209 telemetry snapshot; null until a session retrieves it. */
    val telemetry: CameraTelemetry? = null,
    val timeline: List<TimelineEvent> = emptyList(),
    val lastSuccessfulCommunicationAt: Long? = null,
    /** When telemetry was last refreshed (initial fetch or event-driven 0x9209 [1,1]). */
    val lastTelemetryUpdateAt: Long? = null,
    val fatalError: String? = null,
    /** True when CC06/CC07/CC0C all failed — Wi-Fi auto-join is unavailable. */
    val wifiCredentialsFailed: Boolean = false,
    /** Camera IP discovered from DHCP gateway after joining camera Wi-Fi AP. */
    val discoveredCameraIp: String? = null,
) {
    val isFullyConnected: Boolean
        get() = phase == ConnectionPhase.DEVICE_INFO && deviceInfo != null
}
