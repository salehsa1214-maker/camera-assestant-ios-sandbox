package app.dyrecto.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.alerts.AlertStore
import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertCategory
import app.dyrecto.domain.alerts.AlertSeverity
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.color
import app.dyrecto.ui.components.statusLevel
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.StatusGood
import app.dyrecto.ui.theme.SurfaceElevated
import app.dyrecto.ui.util.formatTimeShort

/**
 * Alerts — the premium, end-user alerts screen. Top to bottom it answers: what needs attention
 * right now (Active), and what already happened (Recent History). A settings shortcut and a
 * "Configure Alerts" button both open the existing Alert Control Center.
 *
 * Pure presentation over the existing [AlertStore] flow — no logic, no storage change. The
 * Active/History split is a UI-only view over the same immutable alert list: because the alert
 * pipeline has no explicit "resolved" state, "Active" is derived as the newest still-unresolved
 * (WARNING/CRITICAL) alert per [AlertCategory]; a newer INFO/recovery alert for a category (e.g.
 * "Reference Match Restored", "Recording Started") means that category is no longer active.
 */

private enum class AlertTab { ACTIVE, HISTORY }

/** How many history rows to reveal initially, and per "Load Older" tap. */
private const val HISTORY_PAGE = 6

@Composable
fun AlertHistoryScreen(onOpenControlCenter: () -> Unit = {}) {
    val alerts by AlertStore.alerts.collectAsState()

    var tab by remember { mutableStateOf(AlertTab.ACTIVE) }
    var visibleHistory by remember { mutableStateOf(HISTORY_PAGE) }

    val active = remember(alerts) { deriveActiveAlerts(alerts) }
    val activeIds = remember(active) { active.mapTo(HashSet()) { it.id } }
    // Recent History on the Active tab lists past events only (currently-active ones are shown
    // above); the History tab shows the full log.
    val pastHistory = remember(alerts, activeIds) { alerts.filter { it.id !in activeIds } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = app.dyrecto.ui.components.FloatingNavBarInset),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AlertsHeader()

        FilterRow(
            tab = tab,
            onSelect = { tab = it; visibleHistory = HISTORY_PAGE },
            onConfigure = onOpenControlCenter,
        )

        if (alerts.isEmpty()) {
            EmptyAlerts()
        } else when (tab) {
            AlertTab.ACTIVE -> {
                ActiveAlertsSection(active)
                if (active.isNotEmpty()) AttentionCard()
                HistorySection(
                    title = "Recent History",
                    alerts = pastHistory,
                    visible = visibleHistory,
                    onLoadOlder = { visibleHistory += HISTORY_PAGE },
                )
            }

            AlertTab.HISTORY -> {
                HistorySection(
                    title = "All History",
                    alerts = alerts,
                    visible = visibleHistory,
                    onLoadOlder = { visibleHistory += HISTORY_PAGE },
                )
            }
        }
    }
}

// ── header ──────────────────────────────────────────────────────────────────────

@Composable
private fun AlertsHeader() {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Alerts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Stay informed about important events.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── filter row: segmented control + Configure Alerts ─────────────────────────────

@Composable
private fun FilterRow(
    tab: AlertTab,
    onSelect: (AlertTab) -> Unit,
    onConfigure: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SegmentedControl(tab, onSelect, Modifier.weight(1f))
        ConfigureButton(onConfigure)
    }
}

@Composable
private fun SegmentedControl(
    tab: AlertTab,
    onSelect: (AlertTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentTab("Active", tab == AlertTab.ACTIVE, { onSelect(AlertTab.ACTIVE) }, Modifier.weight(1f))
        SegmentTab("History", tab == AlertTab.HISTORY, { onSelect(AlertTab.HISTORY) }, Modifier.weight(1f))
    }
}

@Composable
private fun SegmentTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val shape = CircleShape
    var base = modifier.clip(shape)
    base = if (selected) {
        base.background(Brush.horizontalGradient(listOf(primary, secondary)))
    } else {
        base
    }
    Box(
        base.clickable(onClick = onClick).padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConfigureButton(onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .clip(CircleShape)
            .border(1.dp, accent.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Text(
            "Configure Alerts",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1,
        )
    }
}

// ── active alerts ────────────────────────────────────────────────────────────────

@Composable
private fun ActiveAlertsSection(active: List<Alert>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "Active Alerts (${active.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (active.isEmpty()) {
            AllCaughtUpCard()
        } else {
            active.forEach { ActiveAlertCard(it) }
        }
    }
}

@Composable
private fun ActiveAlertCard(alert: Alert) {
    val level = alert.severity.statusLevel()
    val accent = level.color()
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                iconFor(alert),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp),
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (alert.message.isNotBlank()) {
                Text(
                    alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            SeverityBadge(alert.severity)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatTimeShort(alert.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SeverityBadge(severity: AlertSeverity) {
    val level = severity.statusLevel()
    val accent = level.color()
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            severity.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            letterSpacing = 0.5.sp,
        )
    }
}

/** The small informational card shown beneath the active alerts. */
@Composable
private fun AttentionCard() {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accent.copy(alpha = 0.07f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "These alerts need your attention right now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/** Shown on the Active tab when nothing is currently active but history exists. */
@Composable
private fun AllCaughtUpCard() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(StatusGood.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = StatusGood,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text(
                "You're all caught up",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "No alerts need your attention right now.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── history ──────────────────────────────────────────────────────────────────────

@Composable
private fun HistorySection(
    title: String,
    alerts: List<Alert>,
    visible: Int,
    onLoadOlder: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (alerts.isNotEmpty()) {
                Text(
                    "Clear History",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { AlertStore.clear() },
                )
            }
        }

        if (alerts.isEmpty()) {
            Text(
                "Nothing here yet. Past events will appear in this list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp),
            )
            return@Column
        }

        val shown = alerts.take(visible)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Hairline, RoundedCornerShape(20.dp)),
        ) {
            shown.forEachIndexed { index, alert ->
                HistoryRow(alert)
                if (index < shown.lastIndex) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 68.dp)
                            .height(1.dp)
                            .background(Hairline.copy(alpha = 0.6f)),
                    )
                }
            }

            if (alerts.size > visible) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .height(1.dp)
                        .background(Hairline.copy(alpha = 0.6f)),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLoadOlder)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        "Load Older",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(alert: Alert) {
    val level = alert.severity.statusLevel()
    val accent = level.color()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                iconFor(alert),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                alert.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (alert.message.isNotBlank()) {
                Text(
                    alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        Text(
            formatTimeShort(alert.timestamp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp).size(18.dp),
        )
    }
}

// ── empty state ──────────────────────────────────────────────────────────────────

@Composable
private fun EmptyAlerts() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            "No alerts yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Recording, battery, media, thermal, connection and reference events will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ── presentation-only helpers (no logic / no telemetry change) ────────────────────

/**
 * Derives the currently-active alerts from the full session log for display only. Groups by
 * [AlertCategory], keeps the newest alert per category (ids are monotonic), and treats a category
 * as active only while its newest alert is unresolved (WARNING/CRITICAL) — a newer INFO/recovery
 * alert (e.g. "Reference Match Restored", "Recording Started") clears it. Never mutates the store.
 */
private fun deriveActiveAlerts(all: List<Alert>): List<Alert> =
    all.groupBy { it.type.category }
        .values
        .mapNotNull { group -> group.maxByOrNull { it.id } }
        .filter { it.severity != AlertSeverity.INFO }
        .sortedByDescending { it.id }

/** A glanceable icon for an alert, chosen by category (with a couple of direction/recovery cases). */
private fun iconFor(alert: Alert): ImageVector = when (alert.type) {
    AlertType.RECORDING_STOPPED,
    AlertType.HIGHLIGHT_RECOVERED,
    AlertType.SHADOW_RECOVERED,
    AlertType.REFERENCE_RECOVERED -> Icons.Filled.CheckCircle
    AlertType.REFERENCE_EXPOSURE_DRIFT -> Icons.Filled.ArrowUpward
    AlertType.SHADOW_CLIPPING -> Icons.Filled.ArrowDownward
    else -> when (alert.type.category) {
        AlertCategory.RECORDING -> Icons.Filled.FiberManualRecord
        AlertCategory.BATTERY -> Icons.Filled.BatteryAlert
        AlertCategory.MEDIA -> Icons.Filled.SdStorage
        AlertCategory.THERMAL -> Icons.Filled.Thermostat
        AlertCategory.CONNECTION -> Icons.Filled.WifiOff
        AlertCategory.EXPOSURE -> Icons.Filled.WbSunny
        AlertCategory.FACE -> Icons.Filled.Face
        AlertCategory.REFERENCE -> Icons.Filled.CenterFocusStrong
    }
}
