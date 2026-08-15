package app.dyrecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.ConnectionPhase
import app.dyrecto.domain.alerts.Alert
import app.dyrecto.liveview.instructions.AssistantAction
import app.dyrecto.liveview.instructions.AssistantInstruction
import app.dyrecto.liveview.instructions.InstructionFormatter
import app.dyrecto.liveview.instructions.InstructionSelector
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.session.display
import app.dyrecto.liveview.voice.VoiceMode
import app.dyrecto.liveview.voice.VoiceSettings
import app.dyrecto.ui.components.StatusChip
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.color
import app.dyrecto.ui.components.statusLevel
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.StatusBad
import app.dyrecto.ui.theme.StatusGood
import app.dyrecto.ui.theme.StatusWarn
import app.dyrecto.ui.theme.SurfaceElevated
import app.dyrecto.ui.util.formatTimeShort
import kotlinx.coroutines.flow.StateFlow

/** A restrained secondary (purple) accent, used sparingly for the Voice Guidance shortcut. */
private val VoiceAccent = Color(0xFF8C7BF0)

/** Compact, user-facing connection status derived from the detailed pipeline phase. */
private fun statusOf(state: CameraConnectionState): Pair<String, StatusLevel> = when {
    state.phase == ConnectionPhase.IDLE -> "Disconnected" to StatusLevel.IDLE
    state.phase == ConnectionPhase.ERROR -> "Error" to StatusLevel.BAD
    state.isFullyConnected -> "Connected" to StatusLevel.GOOD
    else -> "Connecting" to StatusLevel.WARN
}

/**
 * Dashboard — the premium, end-user home. It answers, top to bottom: is the camera connected and
 * how is it doing, what does the assistant want right now, is anything wrong, is monitoring on, and
 * the two primary shortcuts (Live View, Voice Guidance). All developer/pipeline detail lives
 * elsewhere (Camera Details, Developer). Pure presentation over existing state flows — no logic.
 */
@Composable
fun CameraScreen(
    state: CameraConnectionState,
    onScanCameras: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenAlerts: () -> Unit = {},
    onOpenLiveView: () -> Unit = {},
    onOpenDetails: () -> Unit = {},
    onOpenReference: () -> Unit = {},
    onOpenVoice: () -> Unit = {},
    onStartMonitoring: () -> Unit = {},
    onStopMonitoring: () -> Unit = {},
    liveRender: StateFlow<LiveViewRenderState>? = null,
    referenceMatch: ReferenceMatchResult = ReferenceMatchResult(),
    referenceState: ReferenceSessionState = ReferenceSessionState(),
    voiceSettings: VoiceSettings = VoiceSettings(),
) {
    val showDashboard = state.isFullyConnected || state.telemetry != null
    if (showDashboard) {
        CameraDashboard(
            state = state,
            onDisconnect = onDisconnect,
            onOpenAlerts = onOpenAlerts,
            onOpenLiveView = onOpenLiveView,
            onOpenDetails = onOpenDetails,
            onOpenReference = onOpenReference,
            onOpenVoice = onOpenVoice,
            onStartMonitoring = onStartMonitoring,
            onStopMonitoring = onStopMonitoring,
            liveRender = liveRender,
            referenceMatch = referenceMatch,
            referenceState = referenceState,
            voiceSettings = voiceSettings,
        )
    } else {
        ConnectHome(state, onScanCameras, onDisconnect)
    }
}

@Composable
private fun ConnectHome(
    state: CameraConnectionState,
    onScanCameras: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val (statusText, level) = statusOf(state)
    val connecting = state.running && state.phase != ConnectionPhase.ERROR

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .glow(MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
            }

            Text(
                "Dyrecto",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            StatusChip(statusText, level)

            val fatalError = state.fatalError
            if (state.phase == ConnectionPhase.ERROR && fatalError != null) {
                Text(
                    fatalError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(8.dp))

            if (connecting) {
                ConnectionStages(state.phase)
                TextButton(onClick = onDisconnect) { Text("Cancel") }
            } else {
                Button(
                    onClick = onScanCameras,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Scan for Camera",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            }
        }
    }
}

/** One user-friendly connection step: an icon, a plain-language label, and the phases it covers. */
private data class ConnectStep(val icon: ImageVector, val label: String)

private val connectSteps = listOf(
    ConnectStep(Icons.Filled.Bluetooth, "Waking up your camera"),
    ConnectStep(Icons.Filled.Wifi, "Connecting over Wi-Fi"),
    ConnectStep(Icons.Filled.CameraAlt, "Getting your camera ready"),
)

/**
 * Collapses the detailed [ConnectionPhase] lifecycle into one of three friendly steps (0..2).
 * Everything up to the SSH info read is "waking up" (Bluetooth); the Wi-Fi handoff and SSH login
 * are "connecting over Wi-Fi"; the final PTP session + device info is "getting ready".
 */
private fun connectStepIndex(phase: ConnectionPhase): Int = when (phase) {
    ConnectionPhase.IDLE,
    ConnectionPhase.SCANNING,
    ConnectionPhase.BLE_FOUND,
    ConnectionPhase.CONNECTING,
    ConnectionPhase.BONDING,
    ConnectionPhase.CONNECTED,
    ConnectionPhase.READING_INFO,
    ConnectionPhase.CC17_READ -> 0

    ConnectionPhase.SMARTPHONE_MODE,
    ConnectionPhase.AP_CREATING,
    ConnectionPhase.WIFI_CREDENTIALS,
    ConnectionPhase.WIFI_JOINING,
    ConnectionPhase.IP_DISCOVERY,
    ConnectionPhase.SSH_CONNECTING,
    ConnectionPhase.SSH_AUTHENTICATED -> 1

    ConnectionPhase.PTP_INIT,
    ConnectionPhase.SESSION_OPEN,
    ConnectionPhase.DEVICE_INFO -> 2

    ConnectionPhase.ERROR -> 0
}

/**
 * The reassuring "something is happening" panel shown while connecting. Renders the three
 * [connectSteps] as a vertical checklist: completed steps get a check, the current step spins,
 * and upcoming steps are dimmed — so the user always sees forward progress, never a blank spinner.
 */
@Composable
private fun ConnectionStages(phase: ConnectionPhase) {
    val current = connectStepIndex(phase)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        connectSteps.forEachIndexed { index, step ->
            val done = index < current
            val active = index == current
            val accent = MaterialTheme.colorScheme.primary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    when {
                        active -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                        )
                        done -> Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StatusGood,
                            modifier = Modifier.size(24.dp),
                        )
                        else -> Icon(
                            step.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    step.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = when {
                        active -> MaterialTheme.colorScheme.onSurface
                        done -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                )
            }
        }
    }
}

@Composable
private fun CameraDashboard(
    state: CameraConnectionState,
    onDisconnect: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenLiveView: () -> Unit,
    onOpenDetails: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenVoice: () -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    liveRender: StateFlow<LiveViewRenderState>?,
    referenceMatch: ReferenceMatchResult,
    referenceState: ReferenceSessionState,
    voiceSettings: VoiceSettings,
) {
    val (statusText, level) = statusOf(state)
    val render = liveRender?.collectAsState()?.value
    val alerts by app.dyrecto.alerts.AlertStore.alerts.collectAsState()
    val latestAlert = alerts.firstOrNull()
    val instruction = InstructionSelector.select(referenceMatch)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = app.dyrecto.ui.components.FloatingNavBarInset),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DashboardHeader(hasAlerts = alerts.isNotEmpty(), onBell = onOpenAlerts)

        CameraHeroCard(state, statusText, level, onOpenDetails, onDisconnect)

        GuidanceCard(instruction)

        if (latestAlert != null) {
            CriticalAlertCard(latestAlert, onOpenAlerts)
        }

        MonitoringCard(
            active = referenceState.monitoringRequested,
            hasProfile = referenceState.profile != null,
            onStart = onStartMonitoring,
            onStop = onStopMonitoring,
            onSetup = onOpenReference,
        )

        QuickActions(
            liveEnabled = state.telemetry != null,
            render = render,
            onOpenLiveView = onOpenLiveView,
            voiceSettings = voiceSettings,
            onOpenVoice = onOpenVoice,
        )
    }
}

// ── header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(hasAlerts: Boolean, onBell: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Ready to capture your next shot.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Hairline, CircleShape)
                .clickable(onClick = onBell),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Alerts",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
            if (hasAlerts) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(11.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

// ── camera hero card ────────────────────────────────────────────────────────────

@Composable
private fun CameraHeroCard(
    state: CameraConnectionState,
    statusText: String,
    level: StatusLevel,
    onOpenDetails: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val t = state.telemetry
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(58.dp)
                    .glow(MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    state.deviceInfo?.model?.ifBlank { null } ?: "Camera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(statusText, level)
            }
            OutlinedButton(
                onClick = onOpenDetails,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 8.dp,
                ),
            ) {
                Text("Details", style = MaterialTheme.typography.labelLarge)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatItem(
                icon = Icons.Filled.BatteryFull,
                label = "Battery",
                value = t.display(CameraTelemetry.BATTERY),
                fraction = batteryFraction(state),
                accent = batteryAccent(state),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                icon = Icons.Filled.Thermostat,
                label = "Temp",
                value = t.display(CameraTelemetry.OVERHEATING),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                icon = Icons.Filled.SdStorage,
                label = "Storage",
                value = t.display(CameraTelemetry.SLOT1_REMAIN),
                modifier = Modifier.weight(1f),
            )
            StatItem(
                icon = Icons.Filled.FiberManualRecord,
                label = "Recording",
                value = t.display(CameraTelemetry.MOVIE_REC),
                accent = recordingAccent(state),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Updated ${formatTimeShort(state.lastTelemetryUpdateAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onDisconnect,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 8.dp,
                    vertical = 4.dp,
                ),
            ) {
                Text(
                    "Disconnect",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** One camera stat: icon, small label, large value, optional thin fill bar. */
@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    fraction: Float? = null,
    accent: Color? = null,
) {
    Column(modifier, horizontalAlignment = Alignment.Start) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (fraction != null) {
            Spacer(Modifier.height(8.dp))
            ThinBar(fraction, accent ?: MaterialTheme.colorScheme.primary)
        }
    }
}

/** A rounded 3dp track with a filled portion — the subtle stat progress indicator. */
@Composable
private fun ThinBar(fraction: Float, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(3.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

// ── assistant guidance card (the hero of the screen) ────────────────────────────

@Composable
private fun GuidanceCard(instruction: AssistantInstruction?) {
    val action = instruction?.action ?: AssistantAction.NONE
    val accent = MaterialTheme.colorScheme.primary
    val headline = if (instruction == null) "Everything looks good" else action.headline()
    val detail = when {
        instruction == null -> "Keep shooting."
        instruction.message.isNotBlank() -> instruction.message
        else -> action.headline()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.surface,
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(28.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "CURRENT GUIDANCE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .size(108.dp)
                .glow(accent, radiusAlpha = 0.28f)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.28f), accent.copy(alpha = 0.08f)),
                    ),
                )
                .border(1.dp, accent.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                action.icon(),
                contentDescription = null,
                tint = if (instruction == null) StatusGood else accent,
                modifier = Modifier.size(52.dp),
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            headline,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (instruction != null) {
            Spacer(Modifier.height(18.dp))
            ThinBar(instruction.progress, accent)
            Spacer(Modifier.height(8.dp))
            Text(
                "${InstructionFormatter.progressPercent(instruction)} matched · " +
                    "${InstructionFormatter.confidenceLabel(instruction)} confidence",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── current critical alert ──────────────────────────────────────────────────────

@Composable
private fun CriticalAlertCard(alert: Alert, onOpen: () -> Unit) {
    val level = alert.severity.statusLevel()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(level.color().copy(alpha = 0.12f))
            .border(1.dp, level.color().copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(level.color().copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.FiberManualRecord,
                contentDescription = null,
                tint = level.color(),
                modifier = Modifier.size(16.dp),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (alert.message.isNotBlank()) {
                Text(
                    alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            formatTimeShort(alert.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── monitoring status ───────────────────────────────────────────────────────────

@Composable
private fun MonitoringCard(
    active: Boolean,
    hasProfile: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSetup: () -> Unit,
) {
    val level = if (active) StatusLevel.GOOD else StatusLevel.IDLE
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(level.color().copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CenterFocusStrong,
                contentDescription = null,
                tint = level.color(),
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                if (active) "Monitoring Active" else "Monitoring Paused",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Live View · AI Assistant · Alerts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        when {
            !hasProfile -> OutlinedButton(onClick = onSetup) { Text("Set Up") }
            active -> OutlinedButton(onClick = onStop) { Text("Stop") }
            else -> Button(onClick = onStart) { Text("Start") }
        }
    }
}

// ── quick actions (exactly two shortcuts) ───────────────────────────────────────

@Composable
private fun QuickActions(
    liveEnabled: Boolean,
    render: LiveViewRenderState?,
    onOpenLiveView: () -> Unit,
    voiceSettings: VoiceSettings,
    onOpenVoice: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LiveViewQuickCard(
            enabled = liveEnabled,
            render = render,
            onOpen = onOpenLiveView,
            modifier = Modifier.weight(1f),
        )
        VoiceQuickCard(
            settings = voiceSettings,
            onOpen = onOpenVoice,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LiveViewQuickCard(
    enabled: Boolean,
    render: LiveViewRenderState?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val running = render?.active == true
    val frame = render?.frame
    val image = remember(frame) { frame?.asImageBitmap() }
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(22.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp),
    ) {
        Text(
            "Live View",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "See what the camera sees",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            if (running && image != null) {
                Image(
                    bitmap = image,
                    contentDescription = "Live view preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            } else {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (enabled) accent.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceQuickCard(
    settings: VoiceSettings,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(22.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp),
    ) {
        Text(
            "Voice Guidance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Listen to assistant guidance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .glow(VoiceAccent)
                    .clip(CircleShape)
                    .background(
                        if (settings.enabled) VoiceAccent.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (settings.enabled) VoiceAccent
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                voiceStatus(settings),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (settings.enabled) VoiceAccent
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            )
        }
    }
}

// ── presentation-only helpers (no logic / no telemetry change) ────────────────

/** Short voice-status caption from the persisted settings (never toggles anything here). */
private fun voiceStatus(settings: VoiceSettings): String = when {
    !settings.enabled -> "Off"
    settings.mode == VoiceMode.ASSISTANT_ONLY -> "Assistant"
    settings.mode == VoiceMode.TELEMETRY_ONLY -> "Alerts"
    else -> "On"
}

/** The circular icon shown for each recommended action in the guidance card. */
private fun AssistantAction.icon(): ImageVector = when (this) {
    AssistantAction.PAN_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
    AssistantAction.PAN_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
    AssistantAction.TILT_UP -> Icons.Filled.ArrowUpward
    AssistantAction.TILT_DOWN -> Icons.Filled.ArrowDownward
    AssistantAction.MOVE_CLOSER, AssistantAction.ZOOM_IN -> Icons.Filled.Add
    AssistantAction.MOVE_BACK, AssistantAction.ZOOM_OUT -> Icons.Filled.Remove
    AssistantAction.INCREASE_EXPOSURE -> Icons.Filled.ArrowUpward
    AssistantAction.REDUCE_EXPOSURE -> Icons.Filled.ArrowDownward
    AssistantAction.COOL_WHITE_BALANCE -> Icons.Filled.AcUnit
    AssistantAction.WARM_WHITE_BALANCE -> Icons.Filled.WbSunny
    AssistantAction.WAIT_FOR_SUBJECT -> Icons.Filled.HourglassEmpty
    AssistantAction.REFRAME -> Icons.Filled.CropFree
    AssistantAction.MATCH_REFERENCE -> Icons.Filled.Tune
    AssistantAction.NONE -> Icons.Filled.CheckCircle
}

/** Short imperative headline for each action (the big line in the guidance card). */
private fun AssistantAction.headline(): String = when (this) {
    AssistantAction.PAN_LEFT -> "Pan left"
    AssistantAction.PAN_RIGHT -> "Pan right"
    AssistantAction.TILT_UP -> "Tilt up"
    AssistantAction.TILT_DOWN -> "Tilt down"
    AssistantAction.MOVE_CLOSER -> "Move closer"
    AssistantAction.MOVE_BACK -> "Move back"
    AssistantAction.ZOOM_IN -> "Zoom in"
    AssistantAction.ZOOM_OUT -> "Zoom out"
    AssistantAction.INCREASE_EXPOSURE -> "Raise the exposure"
    AssistantAction.REDUCE_EXPOSURE -> "Lower the exposure"
    AssistantAction.COOL_WHITE_BALANCE -> "Cool the white balance"
    AssistantAction.WARM_WHITE_BALANCE -> "Warm the white balance"
    AssistantAction.WAIT_FOR_SUBJECT -> "Wait for the subject"
    AssistantAction.REFRAME -> "Reframe the shot"
    AssistantAction.MATCH_REFERENCE -> "Match the reference"
    AssistantAction.NONE -> "Everything looks good"
}

/** A soft radial glow drawn behind a circular element (drawn via a large blurred-like tint ring). */
private fun Modifier.glow(color: Color, radiusAlpha: Float = 0.22f): Modifier =
    this.background(
        Brush.radialGradient(
            colors = listOf(color.copy(alpha = radiusAlpha), Color.Transparent),
        ),
        CircleShape,
    )

/** Battery level as a 0..1 fraction for the stat bar, or null when unknown. */
private fun batteryFraction(state: CameraConnectionState): Float? {
    val pct = state.telemetry?.get(CameraTelemetry.BATTERY)?.rawNumber ?: return null
    return (pct / 100f).coerceIn(0f, 1f)
}

/** Red while recording, so the tile reads at a glance; default otherwise. */
private fun recordingAccent(state: CameraConnectionState): Color? {
    val v = state.telemetry.display(CameraTelemetry.MOVIE_REC).lowercase()
    return if (v.contains("rec") && !v.contains("standby")) StatusBad else null
}

/** Amber/red when battery is low; default otherwise. */
private fun batteryAccent(state: CameraConnectionState): Color? {
    val pct = state.telemetry?.get(CameraTelemetry.BATTERY)?.rawNumber ?: return null
    return when {
        pct <= 10 -> StatusBad
        pct <= 20 -> StatusWarn
        else -> null
    }
}
