package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.dyrecto.camera.PtpOperation
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusPill

@Composable
fun DeviceInfoScreen(state: CameraConnectionState) {
    val info = state.deviceInfo
    var query by remember { mutableStateOf("") }

    if (info == null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            SectionCard(title = "Device Information") {
                Text(
                    "Device info is retrieved after the PTP/IP GetDeviceInfo step completes. " +
                        "Connect to the camera first.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        return
    }

    val filtered: List<PtpOperation> = remember(info, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) info.operations
        else info.operations.filter { it.searchText.contains(q) }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            SectionCard(title = "Identity") {
                InfoRow("Manufacturer", info.manufacturer)
                InfoRow("Model", info.model)
                InfoRow("Firmware version", info.firmwareVersion)
                InfoRow("Serial number", info.serialNumber, mono = true)
            }
        }
        item {
            SectionCard(title = "Vendor extension") {
                InfoRow("Description", info.vendorExtensionDescription)
                InfoRow("Extension ID", info.vendorExtensionIdHex, mono = true)
                InfoRow("Extension version", info.vendorExtensionVersion.toString())
                InfoRow("PTP standard version", info.standardVersionText)
                InfoRow("Functional mode", info.functionalMode.toString())
            }
        }
        item {
            SectionCard(title = "Capabilities") {
                InfoRow("Supported operations", info.supportedOperationCount.toString())
                InfoRow("Supported events", info.supportedEventCount.toString())
                InfoRow("Device properties", info.supportedPropertyCount.toString())
                InfoRow("Capture formats", info.captureFormatCount.toString())
                InfoRow("Image formats", info.imageFormatCount.toString())
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search operations (name or 0x code)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
            )
        }
        item {
            Text(
                "Supported PTP operations (${filtered.size}/${info.operations.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        items(filtered, key = { it.code }) { op ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(op.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        op.hex,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                if (op.isVendor) StatusPill("Sony", StatusLevel.WARN)
                else StatusPill("PTP", StatusLevel.IDLE)
            }
        }
        item { Column(Modifier.padding(bottom = 24.dp)) {} }
    }
}
