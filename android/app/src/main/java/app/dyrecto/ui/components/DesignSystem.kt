package app.dyrecto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.SurfaceElevated

/**
 * The premium-feel building blocks for the user-facing screens (Home, Camera Details, Settings):
 * soft elevated cards, section eyebrows, large metric tiles, compact list rows and chips. These
 * intentionally favour a camera-monitor aesthetic (large numerals, negative space, few borders)
 * over the denser developer vocabulary in [UiKit].
 */

/** A soft, borderless elevated container. Optional [onClick] makes the whole card tappable. */
@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Int = 16,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    var base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(MaterialTheme.colorScheme.surface)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Box(base.padding(contentPadding.dp)) { content() }
}

/** A quiet section eyebrow with an optional trailing text action (e.g. "See all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

/**
 * A glanceable metric: small uppercase label over a large value, on a raised tile. [accent] tints
 * the value (e.g. red when recording, amber when low). Optional [onClick] for drill-in.
 */
@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    var base = modifier
        .clip(shape)
        .background(SurfaceElevated)
    if (onClick != null) base = base.clickable(onClick = onClick)
    Column(base.padding(horizontal = 14.dp, vertical = 12.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.size(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = accent ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

data class Metric(
    val label: String,
    val value: String,
    val accent: Color? = null,
    val onClick: (() -> Unit)? = null,
)

/** Lays out [metrics] in an even grid of [columns] tiles, padding the last row to stay aligned. */
@Composable
fun MetricGrid(
    metrics: List<Metric>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    gap: Int = 10,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap.dp)) {
        metrics.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap.dp)) {
                row.forEach { m ->
                    MetricTile(
                        label = m.label,
                        value = m.value,
                        accent = m.accent,
                        onClick = m.onClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Pad an incomplete final row so tiles keep their column width.
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** A compact tonal status chip — dot + label, on a faint tint of the level colour. */
@Composable
fun StatusChip(text: String, level: StatusLevel, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(level.color().copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(end = 6.dp)
                .size(7.dp)
                .background(level.color(), CircleShape),
        )
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = level.color(),
        )
    }
}

/** A label/value pair on one line — for compact grouped readouts inside a card. */
@Composable
fun InlineMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** A tappable list row: title (+ optional summary) with a chevron. The list alternative to a card. */
@Composable
fun NavListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (summary != null) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Live View overlay badge: a pulsing-style red dot + "REC" + optional timecode, on a dark scrim.
 * Floats over the live image (top-left), camera-monitor style.
 */
@Composable
fun RecBadge(timecode: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(end = 6.dp)
                .size(9.dp)
                .background(app.dyrecto.ui.theme.StatusBad, CircleShape),
        )
        Text(
            "REC",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        if (timecode != null) {
            Text(
                timecode,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/**
 * A dashed, low-emphasis placeholder marking a region reserved for a future feature (e.g. the
 * Phase-5 AI assistant overlays). Renders only its label so the layout already accounts for the
 * space without implying behavior. On Live View it sits on the dark image, so [onDark] brightens it.
 */
@Composable
fun ReservedRegion(label: String, modifier: Modifier = Modifier, onDark: Boolean = false) {
    val stroke = if (onDark) Color.White.copy(alpha = 0.25f) else Hairline
    val text = if (onDark) Color.White.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, stroke, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = text)
    }
}
