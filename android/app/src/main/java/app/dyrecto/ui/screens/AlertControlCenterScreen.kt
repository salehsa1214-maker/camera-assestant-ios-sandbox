package app.dyrecto.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.alerts.AlertCategory
import app.dyrecto.domain.alerts.AlertConfig
import app.dyrecto.domain.alerts.AlertPattern
import app.dyrecto.domain.alerts.AlertSeverity
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.ui.components.StatusLevel
import app.dyrecto.ui.components.color
import app.dyrecto.ui.components.statusLevel
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.SurfaceElevated

/**
 * User-facing Alert Control Center. The screen is generated from the alert catalog — it iterates
 * [AlertCategory]s and, within each, every [AlertType] in that category — so a newly added alert
 * type appears automatically with no edits here.
 *
 * Each row lets the user enable/disable the alert, override severity, choose 0–10 beeps / pulses,
 * preview with Test, and restore that alert's defaults. A top button restores all defaults at once.
 * Changes auto-save via [onUpdate]; the Test button is disabled while a pattern is playing
 * ([testPlaying]) so rapid taps can't stack sounds/vibrations.
 *
 * Pure presentation over the existing config flow — the redesign only restyles and adds progressive
 * disclosure (rows expand to reveal their controls); no logic, storage, or callback behaviour changed.
 */
@Composable
fun AlertControlCenterScreen(
    configs: Map<AlertType, AlertConfig>,
    testPlaying: Boolean,
    onUpdate: (AlertConfig) -> Unit,
    onTest: (AlertConfig) -> Unit,
    onRestoreDefault: (AlertType) -> Unit,
    onRestoreAll: () -> Unit,
    onBack: () -> Unit = {},
) {
    val categories = remember {
        AlertCategory.entries
            .map { it to AlertType.entries.filter { t -> t.category == it } }
            .filter { it.second.isNotEmpty() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") { Header(onBack = onBack, onRestoreAll = onRestoreAll) }

        categories.forEach { (category, types) ->
            item(key = "cat_${category.name}") {
                Text(
                    category.title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp),
                )
            }
            items(types, key = { it.name }) { type ->
                val config = configs[type] ?: AlertConfig.default(type)
                AlertConfigCard(
                    config = config,
                    category = category,
                    testPlaying = testPlaying,
                    onUpdate = onUpdate,
                    onTest = onTest,
                    onRestoreDefault = onRestoreDefault,
                )
            }
        }
    }
}

// ── header ──────────────────────────────────────────────────────────────────────

@Composable
private fun Header(onBack: () -> Unit, onRestoreAll: () -> Unit) {
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

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Configure Alerts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Customize how each alert notifies you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            PillButton(text = "Restore All", onClick = onRestoreAll)
        }
    }
}

/** A small outlined accent pill — the secondary action vocabulary shared with the Alerts screen. */
@Composable
private fun PillButton(text: String, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .clip(CircleShape)
            .border(1.dp, accent.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1,
        )
    }
}

// ── one alert (collapsed summary → expanded controls) ────────────────────────────

@Composable
private fun AlertConfigCard(
    config: AlertConfig,
    category: AlertCategory,
    testPlaying: Boolean,
    onUpdate: (AlertConfig) -> Unit,
    onTest: (AlertConfig) -> Unit,
    onRestoreDefault: (AlertType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val enabled = config.enabled
    val level = if (enabled) config.severity.statusLevel() else StatusLevel.IDLE
    val accent = level.color()
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        // Header row: icon · title/summary · enable switch · expand chevron.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    iconFor(category),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
                    .clickable { expanded = !expanded },
            ) {
                Text(
                    config.alertType.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    summaryLine(config),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onUpdate(config.copy(enabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(24.dp)
                    .rotate(chevronRotation)
                    .clickable { expanded = !expanded },
            )
        }

        AnimatedVisibility(visible = expanded) {
            if (enabled) {
                EnabledControls(
                    config = config,
                    testPlaying = testPlaying,
                    onUpdate = onUpdate,
                    onTest = onTest,
                    onRestoreDefault = onRestoreDefault,
                )
            } else {
                Text(
                    "Turn this alert on to customize its severity, sound, and vibration.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun EnabledControls(
    config: AlertConfig,
    testPlaying: Boolean,
    onUpdate: (AlertConfig) -> Unit,
    onTest: (AlertConfig) -> Unit,
    onRestoreDefault: (AlertType) -> Unit,
) {
    Column(Modifier.padding(top = 16.dp)) {
        Divider()

        ControlLabel("Severity", top = 16.dp)
        Spacer(Modifier.height(8.dp))
        SeveritySelector(
            selected = config.severity,
            onSelect = { onUpdate(config.copy(severity = it)) },
        )

        PatternSlider(
            label = "Sound",
            count = config.soundPattern.count,
            unitSingular = "Beep",
            unitPlural = "Beeps",
            noneLabel = "No sound",
            onCountChange = { onUpdate(config.copy(soundPattern = AlertPattern.of(it))) },
        )

        PatternSlider(
            label = "Vibration",
            count = config.vibrationPattern.count,
            unitSingular = "Pulse",
            unitPlural = "Pulses",
            noneLabel = "No vibration",
            onCountChange = { onUpdate(config.copy(vibrationPattern = AlertPattern.of(it))) },
        )

        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TestButton(onClick = { onTest(config) }, enabled = !testPlaying)
            Spacer(Modifier.weight(1f))
            Text(
                "Restore default",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { onRestoreDefault(config.alertType) },
            )
        }
    }
}

// ── severity selector (tonal segmented pills) ────────────────────────────────────

@Composable
private fun SeveritySelector(selected: AlertSeverity, onSelect: (AlertSeverity) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AlertSeverity.entries.forEach { level ->
            val levelColor = level.statusLevel().color()
            val isSelected = level == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) levelColor.copy(alpha = 0.16f)
                        else SurfaceElevated,
                    )
                    .border(
                        1.dp,
                        if (isSelected) levelColor.copy(alpha = 0.55f) else Hairline,
                        CircleShape,
                    )
                    .clickable { onSelect(level) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    level.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) levelColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── pattern slider ───────────────────────────────────────────────────────────────

@Composable
private fun PatternSlider(
    label: String,
    count: Int,
    unitSingular: String,
    unitPlural: String,
    noneLabel: String,
    onCountChange: (Int) -> Unit,
) {
    val valueLabel = when (count) {
        0 -> noneLabel
        1 -> "1 $unitSingular"
        else -> "$count $unitPlural"
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            valueLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    Slider(
        value = count.toFloat(),
        onValueChange = { onCountChange(it.toInt()) },
        valueRange = AlertPattern.MIN_COUNT.toFloat()..AlertPattern.MAX_COUNT.toFloat(),
        steps = AlertPattern.MAX_COUNT - AlertPattern.MIN_COUNT - 1, // intermediate ticks
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = Hairline,
        ),
    )
}

// ── small building blocks ────────────────────────────────────────────────────────

@Composable
private fun TestButton(onClick: () -> Unit, enabled: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    val tint = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Row(
        Modifier
            .clip(CircleShape)
            .border(1.dp, tint.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            "Test",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
    }
}

@Composable
private fun ControlLabel(text: String, top: androidx.compose.ui.unit.Dp) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = top),
    )
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Hairline.copy(alpha = 0.6f)),
    )
}

// ── presentation-only helpers ────────────────────────────────────────────────────

/** A one-line summary of the alert's current delivery config, shown while the row is collapsed. */
private fun summaryLine(config: AlertConfig): String {
    if (!config.enabled) return "Off"
    val severity = config.severity.name.lowercase().replaceFirstChar { it.uppercase() }
    val s = config.soundPattern.count
    val v = config.vibrationPattern.count
    return if (s == 0 && v == 0) {
        "$severity · Silent"
    } else {
        val sound = if (s == 0) "No sound" else "$s beep${if (s == 1) "" else "s"}"
        val vib = if (v == 0) "No pulse" else "$v pulse${if (v == 1) "" else "s"}"
        "$severity · $sound · $vib"
    }
}

/** A glanceable icon per alert category (mirrors the Alerts screen's icon vocabulary). */
private fun iconFor(category: AlertCategory): ImageVector = when (category) {
    AlertCategory.RECORDING -> Icons.Filled.FiberManualRecord
    AlertCategory.BATTERY -> Icons.Filled.BatteryAlert
    AlertCategory.MEDIA -> Icons.Filled.SdStorage
    AlertCategory.THERMAL -> Icons.Filled.Thermostat
    AlertCategory.CONNECTION -> Icons.Filled.WifiOff
    AlertCategory.EXPOSURE -> Icons.Filled.WbSunny
    AlertCategory.FACE -> Icons.Filled.Face
    AlertCategory.REFERENCE -> Icons.Filled.CenterFocusStrong
}
