package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dyrecto.ble.BondState
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.components.BoolRow
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusPill
import app.dyrecto.ui.components.StatusRow

@Composable
fun ConnectionScreen(state: CameraConnectionState) {
    val ble = state.ble
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        SectionCard(
            title = "Link",
            trailing = {
                StatusPill(
                    if (ble.connected) "Connected" else "Disconnected",
                    if (ble.connected) StatusLevel.GOOD else StatusLevel.IDLE,
                )
            },
        ) {
            StatusRow(
                "Connection",
                ble.connectionState.name,
                if (ble.connected) StatusLevel.GOOD else StatusLevel.IDLE,
            )
            StatusRow(
                "Bond state",
                ble.bondState.name,
                when (ble.bondState) {
                    BondState.BONDED -> StatusLevel.GOOD
                    BondState.BONDING -> StatusLevel.WARN
                    BondState.NONE -> StatusLevel.IDLE
                },
            )
            InfoRow("MTU", ble.mtu?.toString())
            BoolRow("Provisioning service found", ble.serviceFound)
        }

        SectionCard(title = "Camera identity (BLE)") {
            InfoRow("Camera model", ble.model)
            InfoRow("Firmware version", ble.firmware)
            InfoRow("Camera Wi-Fi SSID", ble.cameraSsid)
            InfoRow("BLE address", ble.cameraAddress, mono = true)
        }
    }
}
