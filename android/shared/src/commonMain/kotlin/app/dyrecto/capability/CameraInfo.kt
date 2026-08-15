package app.dyrecto.capability

import kotlinx.serialization.Serializable

/** Physical transport the camera session is running over. */
enum class CameraTransport { WIFI, USB, BLE, UNKNOWN }

/**
 * Immutable identity metadata for a connected camera — deliberately SEPARATE from the runtime
 * property/feature/event models so identity and live capability never entangle. Sourced from PTP
 * `GetDeviceInfo` + BLE characteristics + the active transport.
 *
 * All fields are nullable: a field the camera doesn't report stays null rather than being guessed.
 */
@Serializable
data class CameraInfo(
    val manufacturer: String? = null,
    val model: String? = null,
    val firmwareVersion: String? = null,
    /** PTP/vendor protocol version, when the camera advertises one. */
    val protocolVersion: String? = null,
    val serialNumber: String? = null,
    /** Attached lens description, when available. */
    val lensInfo: String? = null,
    val transport: CameraTransport = CameraTransport.UNKNOWN,
) {
    companion object {
        val UNKNOWN = CameraInfo()
    }
}
