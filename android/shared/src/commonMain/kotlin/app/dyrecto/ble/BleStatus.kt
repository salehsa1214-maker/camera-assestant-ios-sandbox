package app.dyrecto.ble

import app.dyrecto.domain.CameraBrand

/**
 * Platform-agnostic view of the BLE discovery + connection layer.
 * Populated from the verified Fx3BleManager callbacks; no Android types here.
 */

enum class BleConnectionState { DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING }

enum class BondState { NONE, BONDING, BONDED }

/** One device seen during the scan. */
data class DiscoveredDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val matched: Boolean,
    val brand: CameraBrand = CameraBrand.UNKNOWN,
)

data class BleStatus(
    val scanning: Boolean = false,
    val devices: List<DiscoveredDevice> = emptyList(),
    val cameraName: String? = null,
    val cameraAddress: String? = null,
    val rssi: Int? = null,
    val connectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val bondState: BondState = BondState.NONE,
    val mtu: Int? = null,
    val model: String? = null,
    val firmware: String? = null,
    val cameraSsid: String? = null,
    val serviceFound: Boolean? = null,
    val error: String? = null,
    // ---- AP-mode Wi-Fi credentials (from CC06/CC07/CC0C after AP creation) ----
    val wifiApSsid: String? = null,
    val wifiApPassword: String? = null,
    val wifiApBssid: String? = null,
) {
    val connected: Boolean get() = connectionState == BleConnectionState.CONNECTED
}
