package app.dyrecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.telemetry.TimelineEvent
import app.dyrecto.telemetry.TimelineStage
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusRow
import app.dyrecto.ui.components.color
import app.dyrecto.ui.util.formatDuration
import app.dyrecto.ui.util.formatTime

@Composable
fun DiagnosticsScreen(state: CameraConnectionState) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        SectionCard(title = "Subsystem status") {
            StatusRow("BLE", state.ble.connectionState.name,
                if (state.ble.connected) StatusLevel.GOOD else StatusLevel.IDLE)
            StatusRow("SSH", if (state.ssh.authenticated) "Authenticated" else "Inactive",
                if (state.ssh.authenticated) StatusLevel.GOOD else StatusLevel.IDLE)
            StatusRow("PTP/IP", if (state.ptp.sessionOpen) "Session open" else "Inactive",
                if (state.ptp.sessionOpen) StatusLevel.GOOD else StatusLevel.IDLE)
            InfoRow("Last successful communication", formatTime(state.lastSuccessfulCommunicationAt))
        }

        SectionCard(title = "Connection timeline") {
            val byStage = state.timeline.associateBy { it.stage }
            TimelineStage.ordered.forEachIndexed { index, stage ->
                TimelineRow(
                    stage = stage,
                    event = byStage[stage],
                    isLast = index == TimelineStage.ordered.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(stage: TimelineStage, event: TimelineEvent?, isLast: Boolean) {
    val reached = event != null
    val level = if (reached) StatusLevel.GOOD else StatusLevel.IDLE
    Row(Modifier.fillMaxWidth()) {
        // Rail: dot + connecting line.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(14.dp).background(level.color(), CircleShape))
            if (!isLast) {
                Spacer(
                    Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                )
            }
        }
        Column(Modifier.padding(start = 12.dp, bottom = if (isLast) 0.dp else 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stage.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (reached) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
                if (event?.durationFromPreviousMs != null) {
                    Text(
                        "  +${formatDuration(event.durationFromPreviousMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                if (reached) formatTime(event!!.timestamp) +
                    (event.detail?.let { " • $it" } ?: "")
                else "pending",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}
