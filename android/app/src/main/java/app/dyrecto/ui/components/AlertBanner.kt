package app.dyrecto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.dyrecto.alerts.AlertStore
import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertSeverity

/** Maps an alert severity to the shared [StatusLevel] colour vocabulary. */
fun AlertSeverity.statusLevel(): StatusLevel = when (this) {
    AlertSeverity.INFO -> StatusLevel.GOOD
    AlertSeverity.WARNING -> StatusLevel.WARN
    AlertSeverity.CRITICAL -> StatusLevel.BAD
}

private const val BANNER_RECENT = 3

/**
 * A prominent, glanceable banner surfacing the most recent alerts at the top of the dashboard.
 * Never collapses to just the latest one: it stacks the last [BANNER_RECENT] alerts (newest on top)
 * and shows a "+N more" summary when there are additional ones. The whole banner escalates to the
 * CRITICAL colour when any surfaced alert is critical, and taps through to the full Alert History.
 * Hidden only when there are no alerts at all.
 */
@Composable
fun AlertBanner(onOpenHistory: () -> Unit, modifier: Modifier = Modifier) {
    val alerts by AlertStore.alerts.collectAsState()
    if (alerts.isEmpty()) return

    val recent = alerts.take(BANNER_RECENT)
    val extra = alerts.size - recent.size
    val hasCritical = recent.any { it.severity == AlertSeverity.CRITICAL }
    val frameLevel = if (hasCritical) StatusLevel.BAD else StatusLevel.WARN

    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(frameLevel.color().copy(alpha = 0.12f))
            .clickable(onClick = onOpenHistory)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Alerts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = frameLevel.color(),
            )
            Text(
                if (extra > 0) "+$extra more · View all" else "View all",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        recent.forEach { AlertBannerRow(it) }
    }
}

@Composable
private fun AlertBannerRow(alert: Alert) {
    val level = alert.severity.statusLevel()
    val critical = alert.severity == AlertSeverity.CRITICAL
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .padding(end = 8.dp)
                .background(level.color(), CircleShape)
                .padding(5.dp),
        )
        Text(
            alert.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (critical) FontWeight.Bold else FontWeight.Medium,
            color = if (critical) level.color() else MaterialTheme.colorScheme.onSurface,
        )
        if (alert.message.isNotBlank()) {
            Text(
                " · ${alert.message}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
