package app.dyrecto.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dyrecto.liveview.exposure.AnalysisColorSpace
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.ZebraSpec
import app.dyrecto.liveview.voice.VoiceCooldowns
import app.dyrecto.liveview.voice.VoiceMode
import app.dyrecto.liveview.voice.VoiceSettings
import app.dyrecto.liveview.voice.VoiceSpeechRate
import app.dyrecto.ui.components.FloatingNavBarInset
import app.dyrecto.ui.components.NavListRow
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.SurfaceElevated

/**
 * Settings — the top-level control surface for the app's features. Everything here is a knob the
 * user is meant to touch: how the assistant speaks (Voice Guidance) and how it reads the image
 * (Exposure zebra threshold, footage Picture Profile). Engineering diagnostics and raw protocol
 * tools stay one level deeper, behind "Developer Mode".
 *
 * This is a **pure presentation** rewrite: the voice controls drive exactly the same [VoiceSettings]
 * flow as before, and the exposure controls read/write the same process-scoped [ExposureConfig]
 * singleton they did on the Developer screen — no logic, storage, or wiring changed, only the
 * arrangement and styling.
 */
@Composable
fun SettingsScreen(
    voiceSettings: VoiceSettings = VoiceSettings(),
    onVoiceSettingsChange: (VoiceSettings) -> Unit = {},
    onOpenAlerts: () -> Unit = {},
    onOpenDeveloper: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = FloatingNavBarInset),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SettingsHeader()

        PremiumStatusCard()

        PremiumLockedCard { VoiceGuidanceCard(voiceSettings, onVoiceSettingsChange) }

        AlertsCard(onOpenAlerts)

        PremiumLockedCard { ZebraCard() }

        PremiumLockedCard { PictureProfileCard() }

        // Developer tools (protocol logs, SSH secret reveal, tuning panels) ship only in debug.
        if (app.dyrecto.BuildConfig.DEBUG) {
            AdvancedSection(onOpenDeveloper)
        }

        LegalSection()

        AboutSection()
    }
}

// ── header ──────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeader() {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Control how the assistant speaks and reads your shot.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── premium status ────────────────────────────────────────────────────────────────

/**
 * Wraps a premium settings card for free users: content stays visible (clearly premium, not
 * hidden) but a full-card tap shield opens the purchase sheet, with a lock badge in the corner.
 * Presentation only — the underlying features are enforced at the engine seams.
 */
@Composable
private fun PremiumLockedCard(content: @Composable () -> Unit) {
    val premium by app.dyrecto.billing.EntitlementProvider.isPremium.collectAsState()
    if (premium) {
        content()
        return
    }
    Box {
        content()
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { app.dyrecto.ui.components.PremiumSheetController.show() },
        )
        app.dyrecto.ui.components.LockBadge(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
        )
    }
}

/**
 * Dyrecto Premium status: free users see the unlock CTA (localized Play price) + Restore;
 * premium users see their lifetime-ownership status. The purchase flow itself lives in the
 * shared [app.dyrecto.ui.components.PremiumSheetHost].
 */
@Composable
private fun PremiumStatusCard() {
    val premium by app.dyrecto.billing.EntitlementProvider.isPremium.collectAsState()
    val price by app.dyrecto.billing.EntitlementProvider.formattedPrice.collectAsState()
    PremiumCard {
        CardHeader(Icons.Filled.WorkspacePremium, "Dyrecto Premium")
        Spacer(Modifier.height(14.dp))
        if (premium) {
            ValueRow("Status", "Unlocked — lifetime")
            Spacer(Modifier.height(6.dp))
            Text(
                "You own the complete camera assistant toolkit. Thank you!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                "Get the complete camera assistant toolkit — exposure analysis, smart alerts, " +
                    "storyboard references and voice guidance." +
                    (price?.let { " $it, one-time purchase. No subscription." } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Unlock Dyrecto", Icons.Filled.WorkspacePremium) {
                app.dyrecto.ui.components.PremiumSheetController.show()
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Already purchased? Restore purchases",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { app.dyrecto.ui.components.PremiumSheetController.show() }
                    .padding(6.dp),
            )
        }
    }
}

// ── voice guidance ────────────────────────────────────────────────────────────────

/**
 * Phase 14 voice guidance controls. The two source toggles project onto [VoiceMode]; turning the
 * last remaining source off turns the master switch off (there is no "enabled but silent" mode).
 * Logic preserved verbatim from the prior screen — only the container/rows were restyled.
 */
@Composable
private fun VoiceGuidanceCard(
    settings: VoiceSettings,
    onChange: (VoiceSettings) -> Unit,
) {
    val assistantOn = settings.mode != VoiceMode.TELEMETRY_ONLY
    val telemetryOn = settings.mode != VoiceMode.ASSISTANT_ONLY

    fun modeFor(assistant: Boolean, telemetry: Boolean): VoiceMode? = when {
        assistant && telemetry -> VoiceMode.BOTH
        assistant -> VoiceMode.ASSISTANT_ONLY
        telemetry -> VoiceMode.TELEMETRY_ONLY
        else -> null // both off ⇒ master off
    }

    fun applySources(assistant: Boolean, telemetry: Boolean) {
        val mode = modeFor(assistant, telemetry)
        if (mode == null) {
            onChange(settings.copy(enabled = false))
        } else {
            onChange(settings.copy(mode = mode))
        }
    }

    PremiumCard {
        CardHeader(icon = Icons.Filled.RecordVoiceOver, title = "Voice Guidance")
        Spacer(Modifier.height(4.dp))
        Text(
            "Speaks assistant guidance and camera warnings out loud. Uses media volume; audio " +
                "follows your phone's routing (Bluetooth earbuds win automatically).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))
        ToggleRow(
            "Voice Guidance",
            subtitle = "Master switch for all spoken output.",
            checked = settings.enabled,
        ) { onChange(settings.copy(enabled = it)) }

        Divider()
        ToggleRow(
            "Assistant guidance",
            subtitle = "Spoken framing and exposure instructions.",
            checked = assistantOn,
            enabled = settings.enabled,
        ) { applySources(assistant = it, telemetry = telemetryOn) }

        Divider()
        ToggleRow(
            "Telemetry alerts",
            subtitle = "Spoken camera warnings (battery, storage, recording).",
            checked = telemetryOn,
            enabled = settings.enabled,
        ) { applySources(assistant = assistantOn, telemetry = it) }

        Divider()
        AssistantReminderRow(
            seconds = settings.assistantReminderSeconds,
            enabled = settings.enabled && assistantOn,
        ) { onChange(settings.copy(assistantReminderSeconds = it)) }

        Spacer(Modifier.height(16.dp))
        Text(
            "SPEECH RATE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        val rates = VoiceSpeechRate.entries.toList()
        SegmentedSelector(
            labels = rates.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selectedIndex = rates.indexOf(settings.speechRate),
            enabled = settings.enabled,
        ) { onChange(settings.copy(speechRate = rates[it])) }
    }
}

/**
 * Integer stepper for the assistant reminder interval (how long before the same unchanged
 * instruction is spoken again). Clamped to the supported 1–15 s range.
 */
@Composable
private fun AssistantReminderRow(
    seconds: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Assistant reminder interval",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            Text(
                "Minimum time before the same instruction repeats.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StepperButton(
                "−",
                enabled = enabled && seconds > VoiceCooldowns.ASSISTANT_REMINDER_MIN_SECONDS,
            ) { onChange(VoiceCooldowns.clampReminderSeconds(seconds - 1)) }
            Text(
                "${seconds}s",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            StepperButton(
                "+",
                enabled = enabled && seconds < VoiceCooldowns.ASSISTANT_REMINDER_MAX_SECONDS,
            ) { onChange(VoiceCooldowns.clampReminderSeconds(seconds + 1)) }
        }
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) accent.copy(alpha = 0.14f) else SurfaceElevated)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

// ── alerts ──────────────────────────────────────────────────────────────────────

/**
 * Entry point into the Alert Control Center — the same screen reached from the Alerts tab, surfaced
 * here so alert configuration is discoverable from Settings too. Pure navigation; owns no state.
 */
@Composable
private fun AlertsCard(onOpenAlerts: () -> Unit) {
    PremiumCard {
        CardHeader(icon = Icons.Filled.NotificationsActive, title = "Alerts")
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose which camera warnings fire, set their severity, and tune how they beep and " +
                "vibrate.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Configure Alerts", Icons.Filled.Tune, onClick = onOpenAlerts)
    }
}

/** A full-width accent action button (icon + label) matching the app's premium CTAs. */
@Composable
private fun PrimaryButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

// ── exposure · zebra ──────────────────────────────────────────────────────────────

/**
 * Zebra highlight-warning threshold. Reads/writes [ExposureConfig] directly — the same singleton
 * the Vision worker's ExposureModule samples each frame. Moved here from Developer so the user can
 * tune the highlight warning that also drives the Highlight alert.
 */
@Composable
private fun ZebraCard() {
    val activeSpec by ExposureConfig.zebraSpec.collectAsState()
    val activeLevel = (activeSpec as? ZebraSpec.Level)?.level

    PremiumCard {
        CardHeader(icon = Icons.Filled.Contrast, title = "Zebra")
        Spacer(Modifier.height(4.dp))
        Text(
            "Warns when highlights pass this brightness (Sony 16–235 studio-swing IRE). Also drives " +
                "the Highlight exposure alert.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))
        Text(
            "THRESHOLD",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        val presets = ExposureConfig.PRESET_LEVELS
        SegmentedSelector(
            labels = presets.map { "$it" },
            selectedIndex = presets.indexOf(activeLevel),
        ) { ExposureConfig.setZebraLevel(presets[it]) }

        Spacer(Modifier.height(12.dp))
        // Custom IRE level (0–109; Sony allows super-white above 100).
        var customText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = customText,
            onValueChange = { text ->
                customText = text.filter { it.isDigit() }.take(3)
                customText.toIntOrNull()?.let { ExposureConfig.setZebraLevel(it) }
            },
            label = { Text("Custom IRE (0–109)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))
        ValueRow("Active threshold", "${activeSpec.label} IRE")
    }
}

// ── exposure · picture profile ─────────────────────────────────────────────────────

/**
 * Picture Profile — tells the analysis pipeline what gamma/color your footage uses so the exposure
 * tools read it correctly (renamed from the developer-facing "Analysis Input"). Reads/writes the
 * same [ExposureConfig.analysisColorSpace] the ExposureModule resolves each frame.
 */
@Composable
private fun PictureProfileCard() {
    val activeColorSpace by ExposureConfig.analysisColorSpace.collectAsState()
    val spaces = AnalysisColorSpace.values().toList()

    PremiumCard {
        CardHeader(icon = Icons.Filled.Palette, title = "Picture Profile")
        Spacer(Modifier.height(4.dp))
        Text(
            "Tell the assistant what color profile your footage uses so exposure tools read it " +
                "correctly. Auto currently assumes Rec.709.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))
        SegmentedSelector(
            labels = spaces.map { it.displayLabel() },
            selectedIndex = spaces.indexOf(activeColorSpace),
        ) { ExposureConfig.setAnalysisColorSpace(spaces[it]) }
    }
}

/** "S_LOG3" → "S-Log3" — user-friendly labels for the color-space segments. */
private fun AnalysisColorSpace.displayLabel(): String = when (this) {
    AnalysisColorSpace.AUTO -> "Auto"
    AnalysisColorSpace.REC709 -> "Rec.709"
    AnalysisColorSpace.S_LOG3 -> "S-Log3"
}

// ── advanced ────────────────────────────────────────────────────────────────────

@Composable
private fun AdvancedSection(onOpenDeveloper: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "ADVANCED",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        NavListRow(
            title = "Developer Mode",
            summary = "Diagnostics, protocol logs, raw telemetry, Live View tools",
            onClick = onOpenDeveloper,
            leading = {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}

// ── legal ──────────────────────────────────────────────────────────────────────

/**
 * Legal documents live on the company website (single source of truth, shared with the
 * Play Console listing) — the app only links out, never embeds a copy.
 */
private const val PRIVACY_POLICY_URL = "https://sollabstechnology.com/products/dyrecto/privacy"
private const val TERMS_URL = "https://sollabstechnology.com/products/dyrecto/terms"

@Composable
private fun LegalSection() {
    val context = LocalContext.current
    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }
    Column(Modifier.fillMaxWidth()) {
        Text(
            "LEGAL",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        NavListRow(
            title = "Privacy Policy",
            summary = "How Dyrecto handles your data — opens sollabstechnology.com",
            onClick = { open(PRIVACY_POLICY_URL) },
            leading = { LegalRowIcon(Icons.Filled.Policy) },
        )
        Spacer(Modifier.height(8.dp))
        NavListRow(
            title = "Terms & Conditions",
            summary = "Terms of use for Dyrecto — opens sollabstechnology.com",
            onClick = { open(TERMS_URL) },
            leading = { LegalRowIcon(Icons.Filled.Gavel) },
        )
    }
}

@Composable
private fun LegalRowIcon(icon: ImageVector) {
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── about ──────────────────────────────────────────────────────────────────────

@Composable
private fun AboutSection() {
    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Dyrecto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Wireless monitor & assistant for your camera.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── shared premium building blocks (local to Settings) ─────────────────────────────

@Composable
private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(24.dp))
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun CardHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/** A label (+ optional subtitle) on the left, a Material Switch on the right. */
@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

/** A read-only label/value line (used for the live "Active threshold" reflection under Zebra). */
@Composable
private fun ValueRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A hairline divider between grouped rows inside a card. */
@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(Hairline),
    )
}

/**
 * A horizontal segmented control: equal-width tappable segments, the selected one filled with a
 * faint accent tint + accent border. Index-based so it drives enums, presets, and rates alike.
 * [selectedIndex] of -1 selects nothing (e.g. Zebra on a custom IRE value).
 */
@Composable
private fun SegmentedSelector(
    labels: List<String>,
    selectedIndex: Int,
    enabled: Boolean = true,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, label ->
            Segment(
                label = label,
                selected = i == selectedIndex,
                enabled = enabled,
                onClick = { onSelect(i) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) accent.copy(alpha = 0.12f) else SurfaceElevated)
            .border(
                1.dp,
                if (selected) accent.copy(alpha = 0.55f) else Color.Transparent,
                RoundedCornerShape(14.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = (if (selected) accent else MaterialTheme.colorScheme.onSurface).copy(alpha = alpha),
        )
    }
}
