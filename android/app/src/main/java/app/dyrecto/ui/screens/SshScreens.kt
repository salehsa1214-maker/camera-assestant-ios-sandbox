package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dyrecto.debug.DeveloperState
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ssh.SshEnabled
import app.dyrecto.ui.components.BoolRow
import app.dyrecto.ui.components.InfoRow
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.StatusPill
import app.dyrecto.ui.components.StatusRow
import app.dyrecto.ui.util.formatDuration
import app.dyrecto.ui.util.formatTime

@Composable
fun SshInfoScreen(state: CameraConnectionState) {
    val ssh = state.ssh
    val reveal by DeveloperState.revealSecrets.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        SectionCard(
            title = "SSH provisioning (CC17)",
            trailing = {
                StatusPill(
                    ssh.sshState.name,
                    when (ssh.sshState) {
                        SshEnabled.ON -> StatusLevel.GOOD
                        SshEnabled.OFF -> StatusLevel.BAD
                        SshEnabled.UNKNOWN -> StatusLevel.IDLE
                    },
                )
            },
        ) {
            StatusRow(
                "SSH enabled",
                ssh.sshState.name,
                if (ssh.sshState == SshEnabled.ON) StatusLevel.GOOD else StatusLevel.IDLE,
            )
            InfoRow("SSH ID", ssh.sshId, mono = true)
            InfoRow("Camera IP", ssh.cameraIp, mono = true)
            // Fingerprint and password are sensitive — hidden unless Developer reveal is on.
            InfoRow(
                "Fingerprint",
                if (reveal) ssh.fingerprint else mask(ssh.fingerprint),
                mono = true,
            )
            InfoRow(
                "SSH password",
                if (reveal) ssh.sshPassword else mask(ssh.sshPassword),
                mono = true,
            )
            if (!reveal) {
                Text(
                    "Secrets are hidden. Enable Developer Mode → Reveal secrets to view.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        if (ssh.notes.isNotEmpty()) {
            SectionCard(title = "Decoder notes") {
                ssh.notes.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
fun SshSessionScreen(state: CameraConnectionState) {
    val ssh = state.ssh
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        SectionCard(
            title = "SSH session",
            trailing = {
                StatusPill(
                    if (ssh.authenticated) "Authenticated" else if (ssh.connected) "Connected" else "Idle",
                    if (ssh.authenticated) StatusLevel.GOOD
                    else if (ssh.connected) StatusLevel.WARN else StatusLevel.IDLE,
                )
            },
        ) {
            BoolRow("SSH connected", if (ssh.connected || ssh.authenticated) true else null)
            BoolRow("Authentication success", if (ssh.lastConnectedAt != null) ssh.authenticated else null)
            BoolRow("Keyboard-interactive supported", ssh.keyboardInteractiveSupported)
        }

        SectionCard(title = "Timing") {
            InfoRow("Connection time", formatDuration(ssh.connectMillis))
            InfoRow("Authentication time", formatDuration(ssh.authMillis))
            InfoRow("Last connection", formatTime(ssh.lastConnectedAt))
        }

        val sshError = ssh.error
        if (sshError != null) {
            SectionCard(title = "Error") {
                Text(sshError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun mask(secret: String?): String =
    if (secret.isNullOrEmpty()) "—" else "•".repeat(secret.length.coerceAtMost(12))
