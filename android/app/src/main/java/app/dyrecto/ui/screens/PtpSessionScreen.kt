package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ptp.ChannelState
import app.dyrecto.ptp.PtpStatus
import app.dyrecto.ui.components.BoolRow
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusPill
import app.dyrecto.ui.components.StatusRow

@Composable
fun PtpSessionScreen(state: CameraConnectionState) {
    val ptp = state.ptp
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        SectionCard(
            title = "Channels",
            trailing = {
                StatusPill(
                    if (ptp.sessionOpen) "Session open" else if (ptp.tunnelUp) "Tunnel up" else "Idle",
                    if (ptp.sessionOpen) StatusLevel.GOOD
                    else if (ptp.tunnelUp) StatusLevel.WARN else StatusLevel.IDLE,
                )
            },
        ) {
            BoolRow("Tunnel status", if (ptp.tunnelUp) true else null,
                trueText = "Up")
            channelRow("Command channel", ptp.commandChannel)
            channelRow("Event channel", ptp.eventChannel)
            BoolRow("Session", if (ptp.tunnelUp) ptp.sessionOpen else null,
                trueText = "Open", falseText = "Closed")
        }

        SectionCard(title = "Handshake results") {
            BoolRow("InitCommandAck", ptp.initCommandAck)
            BoolRow("InitEventAck", ptp.initEventAck)
            InfoRow("OpenSession", PtpStatus.rcText(ptp.openSessionResponse))
            InfoRow("GetDeviceInfo", PtpStatus.rcText(ptp.getDeviceInfoResponse))
        }

        SectionCard(title = "Responder") {
            InfoRow("Connection number", ptp.connectionNumber?.toString())
            InfoRow("Responder name", ptp.responderName)
        }

        val ptpError = ptp.error
        if (ptpError != null) {
            SectionCard(title = "Error") {
                Text(ptpError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun channelRow(label: String, ch: ChannelState) {
    StatusRow(
        label,
        ch.name,
        when (ch) {
            ChannelState.OPEN -> StatusLevel.GOOD
            ChannelState.FAILED -> StatusLevel.BAD
            ChannelState.IDLE -> StatusLevel.IDLE
        },
    )
}
