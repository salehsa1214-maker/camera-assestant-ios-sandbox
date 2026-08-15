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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.dyrecto.ui.theme.StatusBad
import app.dyrecto.ui.theme.StatusGood
import app.dyrecto.ui.theme.StatusIdle
import app.dyrecto.ui.theme.StatusWarn

/** Shared layout rhythm so every screen breathes the same way. */
object Spacing {
    val Screen = 16.dp
    val Section = 12.dp
    val Item = 8.dp
}

enum class StatusLevel { GOOD, WARN, BAD, IDLE }

fun StatusLevel.color(): Color = when (this) {
    StatusLevel.GOOD -> StatusGood
    StatusLevel.WARN -> StatusWarn
    StatusLevel.BAD -> StatusBad
    StatusLevel.IDLE -> StatusIdle
}

/** A labelled card grouping related status rows. */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                trailing?.invoke()
            }
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

/** Coloured status dot + short label. */
@Composable
fun StatusPill(text: String, level: StatusLevel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .padding(end = 6.dp)
                .background(level.color(), CircleShape)
                .padding(5.dp),
        )
        Text(text, style = MaterialTheme.typography.labelLarge, color = level.color())
    }
}

/** A key/value row; [mono] for technical values (hex, ids, fingerprints). */
@Composable
fun InfoRow(label: String, value: String?, mono: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            value?.ifBlank { "—" } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** A row whose value is rendered as a coloured status pill. */
@Composable
fun StatusRow(label: String, text: String, level: StatusLevel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        StatusPill(text, level)
    }
}

/**
 * A glanceable telemetry row: readable label on the left; on the right a large main value
 * (the decoded value when available, otherwise the raw value), with the raw value and property
 * code shown smaller beneath. Shows "—" when no value is present.
 */
@Composable
fun TelemetryRow(label: String, rawValue: String?, code: Int, decoded: String? = null) {
    val raw = rawValue?.ifBlank { null }
    val main = decoded?.ifBlank { null } ?: raw ?: "—"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 16.dp)) {
            Text(
                main,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            // Subline: raw value (only when it differs from the shown main value) + code.
            val rawSuffix = if (raw != null && raw != main) "Raw: $raw · " else ""
            Text(
                rawSuffix + "Code: 0x%04X".format(code),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * A compact label/value chip for translucent overlays (e.g. the Live View glance bar). Reads on a
 * dark scrim: muted label above, bright value below.
 */
@Composable
fun GlanceChip(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        Modifier
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
fun BoolRow(label: String, value: Boolean?, trueText: String = "Yes", falseText: String = "No") {
    val (txt, level) = when (value) {
        true -> trueText to StatusLevel.GOOD
        false -> falseText to StatusLevel.BAD
        null -> "—" to StatusLevel.IDLE
    }
    StatusRow(label, txt, level)
}
