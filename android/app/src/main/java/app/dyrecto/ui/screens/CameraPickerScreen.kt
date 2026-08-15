package app.dyrecto.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dyrecto.ble.BleStatus
import app.dyrecto.ble.DiscoveredDevice
import app.dyrecto.domain.CameraBrand
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.StatusGood

/**
 * Select Camera (the first-run "scanning for camera" screen) — premium redesign.
 *
 * Pure presentation rewrite: the scan lifecycle ([onStartScan]/[onStopScan]), the known-vs-other
 * device split, and camera selection ([onSelectCamera] only on recognised cameras) are preserved
 * exactly from the prior version. Only the visual language changed to match the redesigned app
 * (soft `Hairline`-bordered cards, icon badges, a pulsing radar while scanning).
 */
@Composable
fun CameraPickerScreen(
    state: CameraConnectionState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectCamera: (address: String) -> Unit,
    onBack: () -> Unit = {},
) {
    DisposableEffect(Unit) {
        onStartScan()
        onDispose { }
    }

    val ble: BleStatus = state.ble
    // Only recognised cameras are surfaced; other BLE devices are still scanned (scan behavior is
    // unchanged) but intentionally hidden from the UI as noise.
    val knownCameras = ble.devices.filter { it.brand != CameraBrand.UNKNOWN }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        PickerHeader(onBack = onBack)

        if (knownCameras.isEmpty()) {
            // Default view: a big, obviously-active searching state that fills the screen so the
            // user can tell the app is looking (not frozen). Collapses into the compact card + list
            // the moment a camera appears.
            SearchingState(
                scanning = ble.scanning,
                onStopScan = onStopScan,
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(20.dp))
                ScanStatusCard(
                    scanning = ble.scanning,
                    deviceCount = knownCameras.size,
                    onStopScan = onStopScan,
                )
                DeviceSection(title = "CAMERAS", accent = true) {
                    knownCameras.forEachIndexed { i, device ->
                        if (i > 0) Spacer(Modifier.height(10.dp))
                        CameraCard(device, onClick = { onSelectCamera(device.address) })
                    }
                }
            }
        }
    }
}

// ── searching / empty (default full-screen state) ───────────────────────────────────

/**
 * The default view before any camera is found — a large pulsing radar that fills the empty space so
 * it's visually clear the app is actively scanning (or, once the scan stops, that none were found).
 */
@Composable
private fun SearchingState(
    scanning: Boolean,
    onStopScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BigRadar(scanning = scanning)
        Spacer(Modifier.height(32.dp))
        Text(
            if (scanning) "Searching for cameras…" else "No cameras found yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Make sure your camera is powered on and in Bluetooth pairing/connection mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        if (scanning) {
            Spacer(Modifier.height(28.dp))
            StopPill(onStopScan)
        }
    }
}

/** A large pulsing radar graphic — expanding rings behind a Bluetooth badge. */
@Composable
private fun BigRadar(scanning: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val pulse = rememberInfiniteTransition(label = "bigRadar")
    val progress by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bigRadarPulse",
    )
    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        if (scanning) {
            Canvas(Modifier.fillMaxSize()) {
                val maxR = size.minDimension / 2f
                // Three staggered expanding rings that fade as they grow.
                listOf(progress, (progress + 0.33f) % 1f, (progress + 0.66f) % 1f).forEach { p ->
                    drawCircle(
                        color = accent.copy(alpha = (1f - p) * 0.30f),
                        radius = maxR * (0.35f + p * 0.65f),
                    )
                }
            }
        }
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (scanning) Icons.Filled.BluetoothSearching else Icons.Filled.SearchOff,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

// ── header ────────────────────────────────────────────────────────────────────────

/** The screen's own large header (back button + title + subtitle), matching the redesigned app. */
@Composable
private fun PickerHeader(onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Hairline, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Select Camera",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Pick your camera to connect.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── scan status hero ────────────────────────────────────────────────────────────────

@Composable
private fun ScanStatusCard(
    scanning: Boolean,
    deviceCount: Int,
    onStopScan: () -> Unit,
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadarBadge(scanning = scanning)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (scanning) "Scanning for cameras" else "Scan complete",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        scanning && deviceCount == 0 -> "Looking for nearby cameras…"
                        deviceCount == 0 -> "No cameras found yet"
                        deviceCount == 1 -> "1 camera nearby"
                        else -> "$deviceCount cameras nearby"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (scanning) {
                Spacer(Modifier.width(12.dp))
                StopPill(onStopScan)
            }
        }
    }
}

/** A circular Bluetooth badge with a soft radar ring that pulses outward while [scanning]. */
@Composable
private fun RadarBadge(scanning: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val pulse = rememberInfiniteTransition(label = "radar")
    val progress by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Restart,
        ),
        label = "radarPulse",
    )
    Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
        if (scanning) {
            Canvas(Modifier.fillMaxSize()) {
                val maxR = size.minDimension / 2f
                // Two staggered expanding rings that fade as they grow.
                listOf(progress, (progress + 0.5f) % 1f).forEach { p ->
                    drawCircle(
                        color = accent.copy(alpha = (1f - p) * 0.35f),
                        radius = maxR * (0.5f + p * 0.5f),
                    )
                }
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (scanning) Icons.Filled.BluetoothSearching else Icons.Filled.Bluetooth,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun StopPill(onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Stop",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

// ── sections ──────────────────────────────────────────────────────────────────────

@Composable
private fun DeviceSection(title: String, accent: Boolean, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (accent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        )
        content()
    }
}

/** A recognised camera — a tappable premium card that selects it. */
@Composable
private fun CameraCard(device: DiscoveredDevice, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (device.brand != CameraBrand.UNKNOWN) {
                    Spacer(Modifier.width(8.dp))
                    BrandChip(device.brand.displayName)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                device.address,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(10.dp))
        SignalBars(device.rssi)
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

// ── shared building blocks ──────────────────────────────────────────────────────────

/** A soft, bordered elevated card — the premium container used across the redesigned screens. */
@Composable
private fun PremiumCard(contentPadding: Int = 20, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(24.dp))
            .padding(contentPadding.dp),
    ) { content() }
}

/** A small tonal brand tag (e.g. "Sony") on a faint accent tint. */
@Composable
private fun BrandChip(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/**
 * A compact four-bar signal indicator derived from [rssi] (dBm). Stronger signals light more bars
 * in [StatusGood]; the remaining bars stay a faint outline. Purely a friendlier read of the same
 * number the old screen printed as "-XX dBm".
 */
@Composable
private fun SignalBars(rssi: Int) {
    val active = signalLevel(rssi)
    val onColor = StatusGood
    val offColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    Row(verticalAlignment = Alignment.Bottom) {
        for (i in 0 until 4) {
            Box(
                Modifier
                    .padding(end = if (i < 3) 3.dp else 0.dp)
                    .width(4.dp)
                    .height((6 + i * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < active) onColor else offColor),
            )
        }
    }
}

/** Maps an RSSI (dBm) to 1..4 bars. Typical range: ≥ -55 excellent … ≤ -85 poor. */
private fun signalLevel(rssi: Int): Int = when {
    rssi >= -55 -> 4
    rssi >= -68 -> 3
    rssi >= -80 -> 2
    else -> 1
}
