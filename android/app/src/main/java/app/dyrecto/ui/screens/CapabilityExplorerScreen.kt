package app.dyrecto.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.capability.CameraCapabilities
import app.dyrecto.capability.CameraProperty
import app.dyrecto.capability.CapabilityReporter
import app.dyrecto.capability.PropertyValueSet
import app.dyrecto.debug.DeveloperState
import app.dyrecto.ui.components.SectionCard

/**
 * Developer-only Capability Explorer: renders everything the agnostic [CameraAdapter] discovered from
 * the connected camera — identity, resolved features, every property (writable / availability / range
 * / enum), and unknown codes preserved verbatim. The Export action produces the JSON/Markdown
 * capability report that makes onboarding a new camera model fast (no raw protocol logs needed).
 */
@Composable
fun CapabilityExplorerScreen(capabilities: CameraCapabilities?) {
    val devMode by DeveloperState.developerMode.collectAsState()
    if (!devMode) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Enable Developer Mode to use the Capability Explorer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        return
    }

    val caps = capabilities
    if (caps == null || caps.properties.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No camera capabilities yet — connect a camera and open a session.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        return
    }

    val context = LocalContext.current
    val sortedProps = caps.properties.sortedBy { it.code }
    val unknownCount = caps.unknownPropertyCodes.size

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            SectionCard(title = "Camera") {
                InfoRow("Manufacturer", caps.info.manufacturer ?: "—")
                InfoRow("Model", caps.info.model ?: "—")
                InfoRow("Firmware", caps.info.firmwareVersion ?: "—")
                InfoRow("Protocol", caps.info.protocolVersion ?: "—")
                InfoRow("Serial", caps.info.serialNumber ?: "—")
                InfoRow("Transport", caps.info.transport.name)
                InfoRow("Schema", caps.schemaVersion.toString())
            }
        }

        item {
            SectionCard(title = "Features (${caps.features.size})") {
                if (caps.features.isEmpty()) {
                    Text("None resolved", style = MaterialTheme.typography.bodySmall)
                } else {
                    caps.features.sortedBy { it.name }.forEach { f ->
                        InfoRow(f.displayName, "✓")
                    }
                }
            }
        }

        item {
            SectionCard(title = "Export") {
                Text(
                    "Share the full capability report to onboard this model.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { shareReport(context, caps, asJson = false) },
                        modifier = Modifier.weight(1f),
                    ) { Text("Markdown") }
                    OutlinedButton(
                        onClick = { shareReport(context, caps, asJson = true) },
                        modifier = Modifier.weight(1f),
                    ) { Text("JSON") }
                }
            }
        }

        item {
            Text(
                "Properties (${sortedProps.size}${if (unknownCount > 0) ", $unknownCount unknown" else ""})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 4.dp),
            )
        }

        items(sortedProps, key = { it.code }) { prop ->
            PropertyRow(prop)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }
    }
}

@Composable
private fun PropertyRow(prop: CameraProperty) {
    val values = when (val v = prop.valueSet) {
        is PropertyValueSet.None -> null
        is PropertyValueSet.Range -> "range ${v.min}..${v.max} / ${v.step}"
        is PropertyValueSet.Enum -> "enum {${v.values.take(12).joinToString(",")}${if (v.values.size > 12) "…" else ""}}"
    }
    val current = prop.currentText ?: prop.currentRaw?.toString() ?: "—"
    val flags = buildString {
        append(if (prop.writable) "W" else "·")
        append(if (prop.available) "A" else "·")
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "0x%04X".format(prop.code) + "  " + prop.label + (if (!prop.known) "  (unknown)" else ""),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (prop.known) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.error,
            )
            Text(flags, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "= $current",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            if (values != null) {
                Text(
                    values,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
}

private fun shareReport(context: android.content.Context, caps: CameraCapabilities, asJson: Boolean) {
    val report = CapabilityReporter.build(caps, System.currentTimeMillis())
    val text = if (asJson) CapabilityReporter.toJson(report) else CapabilityReporter.toMarkdown(report)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Dyrecto capability report — ${caps.info.model ?: "camera"}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Export capability report"))
}
