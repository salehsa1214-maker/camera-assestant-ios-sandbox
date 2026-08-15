package app.dyrecto.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.dyrecto.liveview.instructions.InstructionSelector
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceSignalApplicability
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.reference.StoryboardCompletionRule
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.creative.CameraAngle
import app.dyrecto.liveview.reference.creative.ColorTemperature
import app.dyrecto.liveview.reference.creative.CreativeLighting
import androidx.compose.material3.HorizontalDivider
import app.dyrecto.liveview.reference.creative.CreativeSceneModel
import app.dyrecto.liveview.reference.creative.SignaturePresenter
import app.dyrecto.liveview.reference.creative.CreativeSubjectKind
import app.dyrecto.liveview.reference.creative.DepthOfField
import app.dyrecto.liveview.reference.creative.LightingKey
import app.dyrecto.liveview.reference.creative.Mood
import app.dyrecto.liveview.reference.creative.NegativeSpace
import app.dyrecto.liveview.reference.creative.ShotType
import app.dyrecto.liveview.reference.creative.SubjectPlacement
import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.StatusGood
import app.dyrecto.ui.theme.SurfaceElevated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Storyboard (Phase 9/10/15) — teach the assistant the shots you want, then let it protect them.
 *
 * Phase 15 turns the single reference into a **storyboard** of shots. The user manages a grid of
 * reference images; while monitoring, the assistant compares each live frame against every shot,
 * automatically picks the best match (Current Match), and tracks how many planned shots are done.
 * This is presentation only — it drives the same [ReferenceSessionState]/[ReferenceMonitorOptions]
 * state and the same start/stop/clear actions; the monitoring pipeline is unchanged and still sees
 * exactly one reference at a time.
 */
@Composable
fun ShotReferenceScreen(
    referenceState: ReferenceSessionState,
    referenceMatch: ReferenceMatchResult,
    frameStreamActive: Boolean,
    onAddImage: (Uri) -> Unit,
    onReplaceImage: (id: String, uri: Uri) -> Unit,
    onRemoveImage: (id: String) -> Unit,
    onSetOptions: (ReferenceMonitorOptions) -> Unit,
    onSetCompletionRule: (StoryboardCompletionRule) -> Unit,
    onResetProgress: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit,
) {
    val profile = referenceState.profile
    val profiles = referenceState.session?.profiles ?: emptyList()
    val options = profile?.options ?: ReferenceMonitorOptions()
    val hasSubject = profile?.subject != null
    val ai = profile?.ai
    val strategy = ai?.strategy ?: ComparisonStrategy.HUMAN_STRATEGY
    val applicable = ReferenceSignalApplicability.signalsFor(strategy)
    val subjectCapable = if (ai != null) ai.primarySubjectBox != null else hasSubject
    val currentMatchId = referenceMatch.referenceId?.takeIf { referenceMatch.active }

    var showHelp by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<RuleInfo?>(null) }

    // Add supports selecting MULTIPLE images at once; each becomes a new storyboard shot.
    val addLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris -> uris.forEach { onAddImage(it) } }
    // Replace is a single pick targeting a specific shot (remembered across the picker round-trip).
    var replaceTarget by remember { mutableStateOf<String?>(null) }
    val replaceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val id = replaceTarget
        replaceTarget = null
        if (uri != null && id != null) onReplaceImage(id, uri)
    }
    val pickAdd = { addLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val pickReplace: (String) -> Unit = { id ->
        replaceTarget = id
        replaceLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = app.dyrecto.ui.components.FloatingNavBarInset),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        StoryboardHeader(onHelp = { showHelp = true })

        val monitoring = referenceState.monitoringRequested
        if (monitoring) {
            LiveMatchCard(referenceMatch, frameStreamActive)
        }

        ReferencesCard(
            profiles = profiles,
            analyzing = referenceState.analyzing,
            error = referenceState.error,
            onAdd = pickAdd,
            onReplace = pickReplace,
            onRemove = onRemoveImage,
            onClearAll = onClear,
        )

        if (profiles.isNotEmpty()) {
            ProgressCard(
                state = referenceState,
                currentMatchId = currentMatchId,
                onSetRule = onSetCompletionRule,
                onResetProgress = onResetProgress,
                onInfo = { infoDialog = it },
            )
        }

        WhatToMonitorCard(
            profile = profile,
            options = options,
            ai = ai,
            applicable = applicable,
            hasSubject = hasSubject,
            subjectCapable = subjectCapable,
            onSetOptions = onSetOptions,
        )

        MatchingStrictnessCard(
            selected = options.tolerance,
            enabled = profile != null,
            onSelect = { onSetOptions(options.copy(tolerance = it)) },
        )

        PrimaryActionSection(
            monitoring = monitoring,
            hasProfile = profiles.isNotEmpty(),
            onStart = onStart,
            onStop = onStop,
        )

        if (profile != null) {
            ReferenceAnalysisCard(profile.creativeScene)
        }
    }

    if (showHelp) HelpDialog(onDismiss = { showHelp = false })
    infoDialog?.let { info -> RuleInfoDialog(info) { infoDialog = null } }
}

// ── header ──────────────────────────────────────────────────────────────────────

@Composable
private fun StoryboardHeader(onHelp: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Storyboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Organize the shots you want to capture.",
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
                .clickable(onClick = onHelp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Help",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ── live match (monitoring active) ────────────────────────────────────────────────

@Composable
private fun LiveMatchCard(match: ReferenceMatchResult, frameStreamActive: Boolean) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.10f), MaterialTheme.colorScheme.surface),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (frameStreamActive) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Text(
                if (frameStreamActive) "MONITORING LIVE" else "WAITING FOR CAMERA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (frameStreamActive) StatusGood else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (match.active) {
            Spacer(Modifier.height(16.dp))
            Text(
                "${(match.overallScore * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Best match with your storyboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val assistant = InstructionSelector.select(match)
            Spacer(Modifier.height(14.dp))
            ThinBar(match.overallScore, accent)
            Spacer(Modifier.height(12.dp))
            Text(
                assistant?.message ?: "Hold this framing.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                "Start streaming from the camera and the assistant will compare each frame " +
                    "with your storyboard.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── card 1 · references (multi-image grid) ─────────────────────────────────────────

@Composable
private fun ReferencesCard(
    profiles: List<ReferenceProfile>,
    analyzing: Boolean,
    error: String?,
    onAdd: () -> Unit,
    onReplace: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    PremiumCard {
        CardHeader(
            icon = Icons.Filled.Image,
            title = "References",
            trailing = {
                if (profiles.isNotEmpty()) {
                    TextButton(onClick = onClearAll, enabled = !analyzing) {
                        Text("Clear all", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Add the shots you want to capture. Tap a shot to replace it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        if (profiles.isEmpty()) {
            EmptyImagePlaceholder(analyzing = analyzing, onPick = onAdd)
        } else {
            // Responsive 3-column grid, padding the final row to keep tile widths aligned.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                profiles.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { p ->
                            ReferenceGridTile(
                                profile = p,
                                enabled = !analyzing,
                                onReplace = { onReplace(p.id) },
                                onRemove = { onRemove(p.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            AddTile(analyzing = analyzing, onAdd = onAdd)
        }

        if (analyzing) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    "Analyzing…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ReferenceGridTile(
    profile: ReferenceProfile,
    enabled: Boolean,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumb = referenceThumbnail(profile.imageUri)
    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .clickable(enabled = enabled, onClick = onReplace),
        contentAlignment = Alignment.Center,
    ) {
        if (thumb != null) {
            Image(
                bitmap = thumb.asImageBitmap(),
                contentDescription = "Reference shot",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
        // Remove badge (top-right).
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(enabled = enabled, onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove shot",
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun AddTile(analyzing: Boolean, onAdd: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            .clickable(enabled = !analyzing, onClick = onAdd)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "Add shots",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun EmptyImagePlaceholder(analyzing: Boolean, onPick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
            .clickable(enabled = !analyzing, onClick = onPick)
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Add the shots you want to capture",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "The assistant analyzes each on-device and alerts you when the live camera drifts away.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── card 2 · progress ─────────────────────────────────────────────────────────────

/** Presentation status of one storyboard shot in the progress list. */
private enum class ShotStatus { COMPLETED, CURRENT, PENDING }

@Composable
private fun ProgressCard(
    state: ReferenceSessionState,
    currentMatchId: String?,
    onSetRule: (StoryboardCompletionRule) -> Unit,
    onResetProgress: () -> Unit,
    onInfo: (RuleInfo) -> Unit,
) {
    val progress = state.progress
    val rule = state.completionRule
    val profiles = state.session?.profiles ?: emptyList()

    PremiumCard {
        CardHeader(
            icon = Icons.Filled.PlaylistAddCheck,
            title = "Progress",
            trailing = {
                if (state.completedCount > 0) {
                    TextButton(onClick = onResetProgress) {
                        Text("Reset", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
        )
        Spacer(Modifier.height(16.dp))

        Text(
            "${progress.completed} of ${progress.total} shots completed",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(12.dp))
        BigBar(progress.fraction, MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            profiles.forEachIndexed { index, p ->
                val status = when {
                    p.completion.completed -> ShotStatus.COMPLETED
                    p.id == currentMatchId -> ShotStatus.CURRENT
                    else -> ShotStatus.PENDING
                }
                ShotRow(index = index + 1, name = p.name, status = status)
            }
        }

        Spacer(Modifier.height(16.dp))
        LegendRow()

        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Spacer(Modifier.height(16.dp))

        CompletionRulesFooter(rule = rule, onSetRule = onSetRule, onInfo = onInfo)
    }
}

@Composable
private fun ShotRow(index: Int, name: String, status: ShotStatus) {
    val (icon, tint) = when (status) {
        ShotStatus.COMPLETED -> Icons.Filled.CheckCircle to StatusGood
        ShotStatus.CURRENT -> Icons.Filled.PlayArrow to MaterialTheme.colorScheme.primary
        ShotStatus.PENDING -> Icons.Filled.RadioButtonUnchecked to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(
            "Shot $index",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (status == ShotStatus.PENDING) FontWeight.Normal else FontWeight.SemiBold,
            color = if (status == ShotStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp),
        )
        if (status == ShotStatus.CURRENT) {
            Text(
                "Current match",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

@Composable
private fun LegendRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(Icons.Filled.CheckCircle, StatusGood, "Completed")
        LegendItem(Icons.Filled.PlayArrow, MaterialTheme.colorScheme.primary, "Current")
        LegendItem(Icons.Filled.RadioButtonUnchecked, MaterialTheme.colorScheme.onSurfaceVariant, "Pending")
    }
}

@Composable
private fun LegendItem(icon: ImageVector, tint: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

/** A slightly taller filled progress track for the headline storyboard progress. */
@Composable
private fun BigBar(fraction: Float, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(12.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

// ── completion rules (compact footer inside the Progress card) ──────────────────────

@Composable
private fun CompletionRulesFooter(
    rule: StoryboardCompletionRule,
    onSetRule: (StoryboardCompletionRule) -> Unit,
    onInfo: (RuleInfo) -> Unit,
) {
    Text(
        "Completion Rules",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        RuleStepper(
            label = "Hold",
            valueText = "${rule.holdSeconds} s",
            onInfo = { onInfo(RuleInfo.HOLD) },
            onDecrement = {
                onSetRule(rule.copy(holdSeconds = (rule.holdSeconds - 1).coerceAtLeast(HOLD_MIN)))
            },
            onIncrement = {
                onSetRule(rule.copy(holdSeconds = (rule.holdSeconds + 1).coerceAtMost(HOLD_MAX)))
            },
            decEnabled = rule.holdSeconds > HOLD_MIN,
            incEnabled = rule.holdSeconds < HOLD_MAX,
            modifier = Modifier.weight(1f),
        )
        RuleStepper(
            label = "Confirm",
            valueText = "${rule.confirmCount} x",
            onInfo = { onInfo(RuleInfo.CONFIRM) },
            onDecrement = {
                onSetRule(rule.copy(confirmCount = (rule.confirmCount - 1).coerceAtLeast(CONFIRM_MIN)))
            },
            onIncrement = {
                onSetRule(rule.copy(confirmCount = (rule.confirmCount + 1).coerceAtMost(CONFIRM_MAX)))
            },
            decEnabled = rule.confirmCount > CONFIRM_MIN,
            incEnabled = rule.confirmCount < CONFIRM_MAX,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RuleStepper(
    label: String,
    valueText: String,
    onInfo: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    decEnabled: Boolean,
    incEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(
                Modifier
                    .padding(start = 4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onInfo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "$label info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated)
                .height(46.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepButton(Icons.Filled.Remove, enabled = decEnabled, onClick = onDecrement)
            Text(
                valueText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            StepButton(Icons.Filled.Add, enabled = incEnabled, onClick = onIncrement)
        }
    }
}

@Composable
private fun StepButton(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(46.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Which completion-rule setting an info popup is describing. */
private enum class RuleInfo { HOLD, CONFIRM }

@Composable
private fun RuleInfoDialog(info: RuleInfo, onDismiss: () -> Unit) {
    val (title, body) = when (info) {
        RuleInfo.HOLD -> "Hold" to
            "The minimum amount of time a shot must continuously remain above the matching " +
            "threshold before it counts as a successful match."
        RuleInfo.CONFIRM -> "Confirm" to
            "How many successful matches are required before this shot is marked as Completed. " +
            "A new confirmation is counted only after the shot drops below the matching threshold " +
            "and reaches it again."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text(title) },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
    )
}

// ── reference analysis ──────────────────────────────────────────────────────────────

/** Below this inferred confidence a creative attribute is not shown — honesty over filler. */
private const val CREATIVE_UI_CONFIDENCE_FLOOR = 0.5f

/**
 * Phase 16: photographer-facing summary of the reference's Creative Scene Model. Shows only the
 * attributes the assistant could read with confidence, in plain language — never the internal
 * importance weights, never AI jargon.
 */
@Composable
private fun ReferenceAnalysisCard(creative: CreativeSceneModel?) {
    PremiumCard {
        CardHeader(icon = Icons.Filled.AutoAwesome, title = "Reference Analysis")
        Spacer(Modifier.height(16.dp))

        val rows = creative?.let { analysisRows(it) } ?: emptyList()
        val insights = SignaturePresenter.insights(creative)
        if (rows.isEmpty() && insights.isEmpty()) {
            Text(
                "The assistant couldn't read enough from this reference to describe it yet. " +
                    "Replace the image with a clearer shot to re-analyze.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@PremiumCard
        }

        // Phase 16.2: LEAD with the shot identity — what makes THIS shot unique — then the signature,
        // structure and lighting. Plain language only; never raw concepts, scores, or model internals.
        insights.forEachIndexed { index, insight ->
            if (index > 0) Spacer(Modifier.height(14.dp))
            if (insight.title == "Shot Identity") IdentityHeadline(insight.text)
            else InsightRow(insight.title, insight.text)
        }

        // The scene classification is now SUPPORTING metadata — de-emphasized, below the identity.
        if (rows.isNotEmpty()) {
            if (insights.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(14.dp))
                Text(
                    "DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                MetadataRow(label, value)
            }
        }
    }
}

/** The primary line: the shot's unique identity, emphasized above the supporting rows. */
@Composable
private fun IdentityHeadline(text: String) {
    Column {
        Text(
            "SHOT IDENTITY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A titled, full-width sentence insight (Visual Signature / Scene Structure / Lighting Relationship). */
@Composable
private fun InsightRow(title: String, text: String) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** The visible label/value pairs for the analysis card — only confidently-inferred attributes. */
private fun analysisRows(m: CreativeSceneModel): List<Pair<String, String>> = buildList {
    if (m.subject.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        subjectKindLabel(m.subject.kind)?.let { add("Subject" to it) }
    }
    if (m.camera.shotTypeConfidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        shotTypeLabel(m.camera.shotType)?.let { add("Shot Type" to it) }
    }
    if (m.lighting.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        lightingLabel(m.lighting)?.let { add("Lighting" to it) }
    }
    if (m.composition.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        placementLabel(m.composition.placement)?.let { add("Composition" to it) }
        if (m.composition.negativeSpace == NegativeSpace.HIGH) add("Negative Space" to "Prominent")
    }
    if (m.color.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        colorTemperatureLabel(m.color.temperature)?.let { add("Color" to it) }
    }
    if (m.style.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        moodLabel(m.style.mood)?.let { add("Mood" to it) }
    }
    // Camera angle & depth of field are only shown once a future model can infer them confidently;
    // today their confidence is below the floor, so these rows stay hidden rather than guess.
    if (m.camera.angleConfidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        cameraAngleLabel(m.camera.angle)?.let { add("Camera Angle" to it) }
    }
    if (m.depth.confidence >= CREATIVE_UI_CONFIDENCE_FLOOR) {
        depthOfFieldLabel(m.depth.depthOfField)?.let { add("Depth of Field" to it) }
    }
}

private fun subjectKindLabel(kind: CreativeSubjectKind): String? = when (kind) {
    CreativeSubjectKind.PORTRAIT -> "Portrait"
    CreativeSubjectKind.GROUP -> "Group"
    CreativeSubjectKind.ANIMAL -> "Animal"
    CreativeSubjectKind.VEHICLE -> "Vehicle"
    CreativeSubjectKind.PRODUCT -> "Product"
    CreativeSubjectKind.FOOD -> "Food"
    CreativeSubjectKind.SCENE -> "Scene"
    CreativeSubjectKind.UNKNOWN -> null
}

private fun shotTypeLabel(shot: ShotType): String? = when (shot) {
    ShotType.EXTREME_CLOSE_UP -> "Extreme Close-up"
    ShotType.CLOSE_UP -> "Close-up"
    ShotType.MEDIUM_CLOSE_UP -> "Medium Close-up"
    ShotType.MEDIUM -> "Medium"
    ShotType.WIDE -> "Wide"
    ShotType.UNKNOWN -> null
}

private fun lightingLabel(lighting: CreativeLighting): String? = when {
    lighting.backlightHint == true -> "Backlit"
    lighting.key == LightingKey.LOW_KEY -> "Low Key"
    lighting.key == LightingKey.HIGH_KEY -> "High Key"
    lighting.key == LightingKey.BALANCED -> "Balanced"
    else -> null
}

private fun placementLabel(placement: SubjectPlacement): String? = when (placement) {
    SubjectPlacement.LEFT_THIRD -> "Left Third"
    SubjectPlacement.CENTER -> "Centered"
    SubjectPlacement.RIGHT_THIRD -> "Right Third"
    SubjectPlacement.UNKNOWN -> null
}

private fun colorTemperatureLabel(temp: ColorTemperature): String? = when (temp) {
    ColorTemperature.WARM -> "Warm"
    ColorTemperature.COOL -> "Cool"
    ColorTemperature.NEUTRAL -> "Neutral"
    ColorTemperature.UNKNOWN -> null
}

private fun moodLabel(mood: Mood): String? = when (mood) {
    Mood.WARM_CINEMATIC -> "Warm Cinematic"
    Mood.BRIGHT_AIRY -> "Bright & Airy"
    Mood.DARK_MOODY -> "Dark & Moody"
    Mood.NEUTRAL -> "Neutral"
    Mood.UNKNOWN -> null
}

private fun cameraAngleLabel(angle: CameraAngle): String? = when (angle) {
    CameraAngle.EYE_LEVEL -> "Eye Level"
    CameraAngle.HIGH_ANGLE -> "High Angle"
    CameraAngle.LOW_ANGLE -> "Low Angle"
    CameraAngle.UNKNOWN -> null
}

private fun depthOfFieldLabel(dof: DepthOfField): String? = when (dof) {
    DepthOfField.SHALLOW -> "Shallow"
    DepthOfField.DEEP -> "Deep"
    DepthOfField.UNKNOWN -> null
}

/** A de-emphasized metadata row (scene classification) — supporting context below the identity. */
@Composable
private fun MetadataRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(1.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── what to monitor ─────────────────────────────────────────────────────────────

/** One monitor option's presentation (label + icon) resolved from its [ReferenceMonitorOptions] flag. */
private data class MonitorTileSpec(
    val label: String,
    val icon: ImageVector,
    val checked: Boolean,
    val enabled: Boolean,
    val onChange: (Boolean) -> Unit,
)

@Composable
private fun WhatToMonitorCard(
    profile: ReferenceProfile?,
    options: ReferenceMonitorOptions,
    ai: ReferenceAiProfile?,
    applicable: Set<ReferenceSignal>,
    hasSubject: Boolean,
    subjectCapable: Boolean,
    onSetOptions: (ReferenceMonitorOptions) -> Unit,
) {
    PremiumCard {
        CardHeader(icon = Icons.Filled.Visibility, title = "What to Monitor")
        Spacer(Modifier.height(4.dp))
        Text(
            "Choose what aspects you want the assistant to monitor. Applies to every shot.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val gateMessage = when {
            profile == null -> "Add a reference image to choose what to monitor."
            !subjectCapable && ReferenceSignal.SUBJECT_POSITION in applicable ->
                "No subject was detected in the first shot — subject-based monitoring " +
                    "(position, size, presence) is unavailable."
            else -> null
        }
        if (gateMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                gateMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val tiles = buildList {
            add(
                MonitorTileSpec("Exposure", Icons.Filled.AutoAwesome, options.monitorExposure, profile != null) {
                    onSetOptions(options.copy(monitorExposure = it))
                },
            )
            add(
                MonitorTileSpec("White Balance", Icons.Filled.Opacity, options.monitorWhiteBalance, profile != null) {
                    onSetOptions(options.copy(monitorWhiteBalance = it))
                },
            )
            if (ReferenceSignal.SUBJECT_POSITION in applicable) {
                add(
                    MonitorTileSpec(
                        "Framing (Position)", Icons.Filled.CropFree,
                        options.monitorSubjectPosition, profile != null && subjectCapable,
                    ) { onSetOptions(options.copy(monitorSubjectPosition = it)) },
                )
            }
            if (ReferenceSignal.SUBJECT_SIZE in applicable) {
                add(
                    MonitorTileSpec(
                        "Subject Size (Distance)", Icons.Filled.Straighten,
                        options.monitorSubjectSize, profile != null && subjectCapable,
                    ) { onSetOptions(options.copy(monitorSubjectSize = it)) },
                )
            }
            if (ReferenceSignal.SUBJECT_PRESENCE in applicable && ai != null) {
                add(
                    MonitorTileSpec(
                        "Subject Presence", Icons.Filled.Person,
                        options.monitorSubjectPresence, subjectCapable,
                    ) { onSetOptions(options.copy(monitorSubjectPresence = it)) },
                )
            }
            if (ReferenceSignal.COMPOSITION in applicable && ai != null) {
                add(
                    MonitorTileSpec(
                        "Composition", Icons.Filled.GridView,
                        options.monitorComposition, ai.compositionSignature.isNotEmpty(),
                    ) { onSetOptions(options.copy(monitorComposition = it)) },
                )
            }
            if (ReferenceSignal.VISUAL_SIMILARITY in applicable && ai != null) {
                add(
                    MonitorTileSpec(
                        "Visual Similarity", Icons.Filled.AutoAwesome,
                        options.monitorVisualSimilarity, ai.embeddingId != null,
                    ) { onSetOptions(options.copy(monitorVisualSimilarity = it)) },
                )
            }
            if (ReferenceSignal.HEADROOM in applicable) {
                add(
                    MonitorTileSpec(
                        "Headroom", Icons.Filled.VerticalAlignTop,
                        options.monitorHeadroom, profile != null && hasSubject,
                    ) { onSetOptions(options.copy(monitorHeadroom = it)) },
                )
            }
            if (ReferenceSignal.FACE_PRESENCE in applicable) {
                add(
                    MonitorTileSpec(
                        "Face Presence", Icons.Filled.Face,
                        options.monitorFacePresence, profile != null && hasSubject,
                    ) { onSetOptions(options.copy(monitorFacePresence = it)) },
                )
            }
            if (ReferenceSignal.EYE_VISIBILITY in applicable) {
                add(
                    MonitorTileSpec(
                        "Eye Visibility", Icons.Filled.Visibility,
                        options.monitorEyeVisibility, profile != null && hasSubject,
                    ) { onSetOptions(options.copy(monitorEyeVisibility = it)) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { tile ->
                        MonitorTile(tile, Modifier.weight(1f))
                    }
                    if (row.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonitorTile(spec: MonitorTileSpec, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    val active = spec.checked && spec.enabled
    val background = if (active) accent else SurfaceElevated
    val content = if (active) Color.Black else MaterialTheme.colorScheme.onSurface
    val alpha = if (spec.enabled) 1f else 0.4f
    Row(
        modifier
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(enabled = spec.enabled) { spec.onChange(!spec.checked) }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            spec.icon,
            contentDescription = null,
            tint = content.copy(alpha = alpha),
            modifier = Modifier.size(18.dp),
        )
        Text(
            spec.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = content.copy(alpha = alpha),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
    }
}

// ── matching strictness ───────────────────────────────────────────────────────────

@Composable
private fun MatchingStrictnessCard(
    selected: ReferenceTolerance,
    enabled: Boolean,
    onSelect: (ReferenceTolerance) -> Unit,
) {
    val ordered = listOf(
        Triple(ReferenceTolerance.LOOSE, "Loose", "More flexible"),
        Triple(ReferenceTolerance.MEDIUM, "Medium", "Balanced"),
        Triple(ReferenceTolerance.STRICT, "Strict", "More precise"),
    )
    PremiumCard {
        CardHeader(icon = Icons.Filled.GpsFixed, title = "Matching Strictness")
        Spacer(Modifier.height(4.dp))
        Text(
            "How strict the assistant should be when comparing to the reference.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ordered.forEach { (tolerance, title, subtitle) ->
                StrictnessSegment(
                    title = title,
                    subtitle = subtitle,
                    selected = selected == tolerance,
                    enabled = enabled,
                    onClick = { onSelect(tolerance) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StrictnessSegment(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val alpha = if (enabled) 1f else 0.45f
    Column(
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = (if (selected) accent else MaterialTheme.colorScheme.onSurface).copy(alpha = alpha),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            textAlign = TextAlign.Center,
        )
    }
}

// ── primary action ────────────────────────────────────────────────────────────────

@Composable
private fun PrimaryActionSection(
    monitoring: Boolean,
    hasProfile: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Column {
        GradientActionButton(
            monitoring = monitoring,
            enabled = hasProfile,
            onClick = if (monitoring) onStop else onStart,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.CenterFocusStrong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                "Monitoring will run while the camera is streaming.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun GradientActionButton(monitoring: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val gradient = if (monitoring) {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.75f)))
    } else {
        Brush.horizontalGradient(listOf(accent, secondary))
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(if (enabled) Modifier.background(gradient) else Modifier.background(SurfaceElevated))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (monitoring) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Text(
                if (monitoring) "Stop Monitoring" else "Start Monitoring",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

// ── dialogs ─────────────────────────────────────────────────────────────────────

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text("Storyboard") },
        text = {
            Text(
                "1. Add the shots you want to capture.\n\n" +
                    "2. The assistant analyzes each on-device.\n\n" +
                    "3. Choose what to monitor and how strict the match should be.\n\n" +
                    "4. Start monitoring — while the camera streams, the assistant picks the best " +
                    "matching shot for each frame and tracks your progress until every planned " +
                    "shot is captured.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
    )
}

// ── shared building blocks ────────────────────────────────────────────────────────

/** A soft, bordered elevated card — the premium container used across the redesigned screens. */
@Composable
private fun PremiumCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Hairline, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) { content() }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
) {
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
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        if (trailing != null) trailing()
    }
}

/** A rounded 4dp track with a filled portion — the subtle progress indicator (matches Dashboard). */
@Composable
private fun ThinBar(fraction: Float, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

// ── presentation helpers ──────────────────────────────────────────────────────────

/** Decodes the app-private reference thumbnail off the main thread; null while loading/absent. */
@Composable
private fun referenceThumbnail(imageUri: String?): Bitmap? {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = imageUri) {
        value = if (imageUri == null) null else withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(Uri.parse(imageUri).path) }.getOrNull()
        }
    }
    return bitmap
}

// Completion-rule stepper bounds.
private const val HOLD_MIN = 1
private const val HOLD_MAX = 30
private const val CONFIRM_MIN = 1
private const val CONFIRM_MAX = 10
