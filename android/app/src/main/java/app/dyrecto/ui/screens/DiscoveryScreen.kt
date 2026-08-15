package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusPill
import app.dyrecto.ui.components.StatusRow

@Composable
fun DiscoveryScreen(state: CameraConnectionState) {
    val ble = state.ble
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        SectionCard(
            title = "Selected Camera",
            trailing = {
                StatusPill(
                    if (ble.scanning) "Scanning" else if (ble.cameraAddress != null) "Found" else "Idle",
                    if (ble.cameraAddress != null) StatusLevel.GOOD
                    else if (ble.scanning) StatusLevel.WARN else StatusLevel.IDLE,
                )
            },
        ) {
            InfoRow("Camera name", ble.cameraName)
            InfoRow("BLE address", ble.cameraAddress, mono = true)
            InfoRow("Signal (RSSI)", ble.rssi?.let { "$it dBm" })
            StatusRow(
                "Connection state",
                ble.connectionState.name,
                if (ble.connected) StatusLevel.GOOD else StatusLevel.IDLE,
            )
            val bleError = ble.error
            if (bleError != null) {
                Text(bleError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        SectionCard(title = "All devices seen (${ble.devices.size})") {
            if (ble.devices.isEmpty()) {
                Text("No devices yet. Start a connection from the dashboard.",
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                ble.devices.forEach { d ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                d.name + if (d.matched) "  ← match" else "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                d.address,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                        StatusPill(
                            "${d.rssi} dBm",
                            if (d.matched) StatusLevel.GOOD else StatusLevel.IDLE,
                        )
                    }
                }
            }
        }
    }
}
