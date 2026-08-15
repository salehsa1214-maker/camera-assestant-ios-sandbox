package app.dyrecto.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.debug.DeveloperState
import app.dyrecto.debug.LogStore
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.PushLvStatus
import app.dyrecto.liveview.instructions.InstructionSelector
import app.dyrecto.liveview.perception.PerceptualThresholds
import app.dyrecto.liveview.perception.PerceptualTuning
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.ReferenceTolerance
import app.dyrecto.liveview.reference.ai.AiCapabilities
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.EngineStatus
import app.dyrecto.liveview.vision.VisionStatistics
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.liveview.voice.VoiceDebugState
import app.dyrecto.liveview.voice.VoiceSettings
import app.dyrecto.ui.components.SectionCard
import app.dyrecto.ui.navigation.Destination

/**
 * Developer-visible tri-state for whether the persistent frame stream is actually producing
 * frames, independent of camera connection state — lets diagnostics distinguish "connected but
 * no frames yet" from "connected and streaming" from "disconnected."
 */
enum class FrameStreamHealth { ACTIVE, STARTING, STOPPED }

private fun PushLvStatus.Phase.toFrameStreamHealth(): FrameStreamHealth = when (this) {
    PushLvStatus.Phase.RECEIVING -> FrameStreamHealth.ACTIVE
    PushLvStatus.Phase.LISTENING, PushLvStatus.Phase.STARTED, PushLvStatus.Phase.CONNECTED ->
        FrameStreamHealth.STARTING
    PushLvStatus.Phase.IDLE, PushLvStatus.Phase.STOPPED, PushLvStatus.Phase.ERROR ->
        FrameStreamHealth.STOPPED
}

/** Raw protocol-pipeline detail screens, reachable only from Developer (hidden from normal use). */
private val DETAIL_DESTINATIONS = listOf(
    Destination.DISCOVERY,
    Destination.CONNECTION,
    Destination.SSH_INFO,
    Destination.SSH_SESSION,
    Destination.PTP_SESSION,
    Destination.DEVICE_INFO,
    Destination.DIAGNOSTICS,
    Destination.TELEMETRY_EXPLORER,
    Destination.CAPABILITY_EXPLORER,
)

@Composable
fun DeveloperScreen(
    pushLiveView: PushLvStatus = PushLvStatus(),
    visionStats: VisionStatistics = VisionStatistics(),
    visionContext: VisionContext = VisionContext(),
    sceneContext: SceneContext = SceneContext(),
    referenceState: ReferenceSessionState = ReferenceSessionState(),
    referenceMatch: ReferenceMatchResult = ReferenceMatchResult(),
    aiCapabilities: AiCapabilities = AiCapabilities(),
    perceptualTuning: PerceptualTuning? = null,
    voiceDebug: VoiceDebugState = VoiceDebugState(),
    voiceSettings: VoiceSettings = VoiceSettings(),
    onPerceptualTuningChange: (PerceptualTuning?) -> Unit = {},
    onCameraIpChange: (String) -> Unit = {},
    onReadEe02: () -> Unit = {},
    onReadEe04: () -> Unit = {},
    onDumpEeState: () -> Unit = {},
    onNavigate: (Destination) -> Unit = {},
) {
    val context = LocalContext.current
    val devMode by DeveloperState.developerMode.collectAsState()
    val reveal by DeveloperState.revealSecrets.collectAsState()
    val lines by LogStore.lines.collectAsState()
    var devIp by remember { mutableStateOf("192.168.122.1") }

    // Outer column: full-screen, splits vertically between header section and log viewer.
    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // Header: all controls live here. In dev mode it gets weight(1f) so the log
        // card can claim equal space below it, and it scrolls if its own content
        // overflows its allocated half. In normal mode it scrolls the full screen.
        val headerMod = if (devMode)
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
        else
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())

        Column(headerMod) {

            SectionCard(title = "Developer Mode") {
                ToggleRow("Developer Mode", devMode) { DeveloperState.setDeveloperMode(it) }
                ToggleRow(
                    "Reveal secrets (passwords, fingerprint)",
                    reveal,
                    enabled = devMode,
                ) { DeveloperState.setRevealSecrets(it) }
                Text(
                    "Secrets are never shown to normal users and are not persisted to disk. " +
                        "Revealing requires Developer Mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            SectionCard(title = "Entitlement (debug)") {
                val entState by app.dyrecto.billing.EntitlementProvider.state.collectAsState()
                val effective by app.dyrecto.billing.EntitlementProvider.isPremium.collectAsState()
                val override by app.dyrecto.billing.EntitlementProvider.debugOverride.collectAsState()
                val pending by app.dyrecto.billing.EntitlementProvider.purchasePending.collectAsState()
                Text(
                    "Real state: $entState   pending: $pending   effective premium: $effective",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                ToggleRow("Force PREMIUM (override)", override == true) {
                    app.dyrecto.billing.EntitlementProvider.debugOverride.value = if (it) true else null
                }
                ToggleRow("Force FREE (override)", override == false) {
                    app.dyrecto.billing.EntitlementProvider.debugOverride.value = if (it) false else null
                }
                Text(
                    "Overrides are debug-build-only (compile-time gated) and in-memory. " +
                        "Force FREE exercises the locked tier; both off = real Play entitlement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            SectionCard(title = "Manual camera IP override") {
                Text(
                    "Use only when the automatic BLE Wi-Fi provisioning path is unavailable. " +
                        "This overrides the IP used for the SSH/PTP layer. " +
                        "Not shown on the main screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                OutlinedTextField(
                    value = devIp,
                    onValueChange = { devIp = it; onCameraIpChange(it) },
                    label = { Text("Camera IP (dev override)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }

            SectionCard(title = "Live View tools") {
                Text(
                    "Engineering views of the Live View pipelines. Diagnostics drives the push path " +
                        "(render metrics, Frame Context, raw capture + offline analysis); the legacy " +
                        "HTTP path is kept for comparison.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                OutlinedButton(
                    onClick = { onNavigate(Destination.PUSH_LIVE_VIEW_POC) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(Destination.PUSH_LIVE_VIEW_POC.title) }
                OutlinedButton(
                    onClick = { onNavigate(Destination.LIVE_VIEW_HTTP) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(Destination.LIVE_VIEW_HTTP.title) }
            }

            SectionCard(title = "Vision") {
                Text(
                    "Phase 6: histogram + zebra exposure analysis + face/eye detection. Results " +
                        "update every analyzed frame.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                InfoRow("Frame Stream", pushLiveView.phase.toFrameStreamHealth().name)
                InfoRow("Vision Running", if (visionStats.pipelineRunning) "Yes" else "No")
                InfoRow("Registered Modules", visionStats.registeredModules.toString())
                InfoRow("Frames Received", visionStats.framesReceived.toString())
                InfoRow("Frames Analyzed", visionStats.framesAnalyzed.toString())
                InfoRow("Frames Dropped", visionStats.framesDropped.toString())
                InfoRow("Analysis FPS", "%.1f".format(visionStats.analysisFps))
                InfoRow("Last Analysis", "${visionStats.lastAnalysisDurationMs} ms")
                InfoRow("Average Analysis", "%.1f ms".format(visionStats.averageAnalysisDurationMs))
                InfoRow("Faces Detected",
                    visionContext.faces?.facesDetected?.toString() ?: "—")
                InfoRow("Eyes Detected",
                    visionContext.eyes?.eyesDetected?.toString() ?: "—")
            }

            SectionCard(title = "Histogram") {
                Text(
                    "Phase 6: 256-bin luma statistics (BT.709). Canonical exposure source; every " +
                        "future tool reads these instead of rescanning.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                val hist = visionContext.histogram
                InfoRow("Mean", hist?.let { "%.1f".format(it.mean) } ?: "—")
                InfoRow("Median", hist?.median?.toString() ?: "—")
                InfoRow("P95", hist?.percentile95?.toString() ?: "—")
                InfoRow("P99", hist?.percentile99?.toString() ?: "—")
                InfoRow("Highlight %",
                    hist?.let { "%.1f%%".format(it.clippedHighlightPercentage) } ?: "—")
                InfoRow("Shadow %",
                    hist?.let { "%.1f%%".format(it.clippedShadowPercentage) } ?: "—")
                InfoRow("Sampled Pixels", hist?.totalPixels?.toString() ?: "—")
                InfoRow(
                    "Effective Stride",
                    hist?.let { s -> "${s.effectiveStride}" + if (s.effectiveStride == 1) " (full-res)" else " (downsampled)" }
                        ?: "—",
                )
            }

            SectionCard(title = "Zebra") {
                Text(
                    "Phase 6: IRE-mapped coverage (Sony 16–235 studio-swing). Zebra2 level. " +
                        "The threshold control now lives on the Settings tab; this is a live readout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                val activeSpec by ExposureConfig.zebraSpec.collectAsState()
                val zebra = visionContext.zebra
                InfoRow("Active Threshold", "${activeSpec.label} IRE")
                InfoRow("Coverage %",
                    zebra?.let { "%.1f%%".format(it.coveragePercentage) } ?: "—")
            }

            SectionCard(title = "Picture Profile (Phase 8)") {
                Text(
                    "Analysis Color Transform applied before Histogram/Zebra/Exposure see the frame. " +
                        "Auto currently resolves to Rec709 (no camera telemetry for gamma/picture " +
                        "profile yet). The profile selector now lives on the Settings tab; this is a " +
                        "live readout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                val activeColorSpace by ExposureConfig.analysisColorSpace.collectAsState()
                val resolvedTransform = ExposureConfig.resolveTransform()
                InfoRow("Active Profile", activeColorSpace.name)
                InfoRow("Resolved Transform", resolvedTransform::class.simpleName ?: "—")
                InfoRow("LUT Loaded", if (ExposureConfig.sLog3LutName != null) "Yes" else "No")
                InfoRow("LUT Name", ExposureConfig.sLog3LutName ?: "—")
            }

            SectionCard(title = "Exposure Alerts") {
                Text(
                    "Phase 7: frame-based persistence/recovery state machine on top of the Zebra/" +
                        "Histogram coverage above. Diagnostics only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                val exposure = visionContext.exposure
                InfoRow("Highlight Coverage", exposure?.let { "%.1f%%".format(it.highlightCoverage) } ?: "—")
                InfoRow("Shadow Coverage", exposure?.let { "%.1f%%".format(it.shadowCoverage) } ?: "—")
                InfoRow("Highlight State", exposure?.highlightState?.name ?: "—")
                InfoRow("Shadow State", exposure?.shadowState?.name ?: "—")
                InfoRow("Highlight Persistence", exposure?.highlightPersistenceFrames?.toString() ?: "—")
                InfoRow("Shadow Persistence", exposure?.shadowPersistenceFrames?.toString() ?: "—")
                InfoRow("Highlight Recovery Counter", exposure?.highlightRecoveryFrames?.toString() ?: "—")
                InfoRow("Shadow Recovery Counter", exposure?.shadowRecoveryFrames?.toString() ?: "—")
                InfoRow("Exposure Confidence", exposure?.let { "%.2f".format(it.exposureConfidence) } ?: "—")
            }

            SectionCard(title = "Scene Understanding") {
                Text(
                    "Phase 5C: merged, descriptive scene state from telemetry + Vision. " +
                        "Read-only; makes no decisions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                InfoRow("Overall State", sceneContext.overallState.name)
                InfoRow(
                    "Recording",
                    if (sceneContext.recording.isRecording) {
                        if (sceneContext.recording.recordingDurationAvailable) {
                            "Yes (duration available)"
                        } else {
                            "Yes"
                        }
                    } else {
                        "No"
                    },
                )
                InfoRow("Highlight %", "%.1f%%".format(sceneContext.exposure.highlightPercentage))
                InfoRow("Shadow %", "%.1f%%".format(sceneContext.exposure.shadowPercentage))
                InfoRow("Faces", sceneContext.face.facesDetected.toString())
                InfoRow("Eyes", sceneContext.face.eyesDetected.toString())
                InfoRow(
                    "Battery",
                    sceneContext.battery.batteryLevel?.let { level ->
                        "$level%" + if (sceneContext.battery.isBatteryLow) " (low)" else ""
                    } ?: "—",
                )
                InfoRow(
                    "Storage",
                    (sceneContext.storage.remainingStatus ?: "—") +
                        if (sceneContext.storage.isStorageCritical) " (critical)" else "",
                )
                InfoRow("Updated", sceneContext.updatedAtMs.toString())
            }

            SectionCard(title = "Reference Assistant") {
                Text(
                    "Phase 9: Shot Reference comparison state. Per-signal drift deltas, state " +
                        "machine states, and persistence/recovery counters. Diagnostics only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                val profile = referenceState.profile
                InfoRow("Reference Id", profile?.id?.take(8) ?: "—")
                InfoRow("Reference Name", profile?.name ?: "—")
                InfoRow("Tolerance", profile?.options?.tolerance?.name ?: "—")
                InfoRow(
                    "Enabled Options",
                    profile?.options?.let { o ->
                        listOfNotNull(
                            "Exp".takeIf { o.monitorExposure },
                            "WB".takeIf { o.monitorWhiteBalance },
                            "Pos".takeIf { o.monitorSubjectPosition },
                            "Size".takeIf { o.monitorSubjectSize },
                            "Head".takeIf { o.monitorHeadroom },
                            "Face".takeIf { o.monitorFacePresence },
                            "Eyes".takeIf { o.monitorEyeVisibility },
                        ).joinToString(" ").ifBlank { "none" }
                    } ?: "—",
                )
                InfoRow("Monitoring", if (referenceState.monitoringRequested) "Requested" else "Off")
                InfoRow("Match Active", if (referenceMatch.active) "Yes" else "No")
                InfoRow("Overall Score", "%.2f".format(referenceMatch.overallScore))
                InfoRow("Exposure Δ", "%.1f".format(referenceMatch.exposureMatch.delta))
                InfoRow("WB Δ", "%.1f".format(referenceMatch.whiteBalanceMatch.delta))
                InfoRow("Position Δ", "%.3f".format(referenceMatch.subjectPositionMatch.delta))
                InfoRow("Size Δ", "%.3f".format(referenceMatch.subjectSizeMatch.delta))
                InfoRow("Headroom Δ", "%.3f".format(referenceMatch.headroomMatch.delta))
                for (signal in ReferenceSignal.entries) {
                    val s = referenceMatch.signal(signal)
                    InfoRow(
                        signal.name,
                        if (!s.enabled) "disabled"
                        else if (!s.available) "unavailable"
                        else "${s.state.name} p=${s.persistenceFrames} r=${s.recoveryFrames}",
                    )
                }
            }

            SectionCard(title = "AI Reference Assistant") {
                Text(
                    "Phase 10: on-device perception (detector / embedder / segmenter behind " +
                        "engine seams, detect→track→compare lifecycle). Model status, latest " +
                        "scene snapshot, and inference diagnostics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                InfoRow("Detector", capabilityLabel(aiCapabilities.detection))
                InfoRow("Embedder", capabilityLabel(aiCapabilities.embedding))
                InfoRow("Segmenter", capabilityLabel(aiCapabilities.segmentation))
                aiCapabilities.detection.error?.let { InfoRow("Detector Error", it.take(48)) }
                aiCapabilities.embedding.error?.let { InfoRow("Embedder Error", it.take(48)) }

                val aiScene = visionContext.aiScene
                val snapshot = aiScene?.snapshot
                InfoRow("Scene Mode", snapshot?.sceneMode?.value?.name ?: "—")
                InfoRow(
                    "Scene Confidence",
                    snapshot?.sceneMode?.confidence?.let { "%.2f".format(it) } ?: "—",
                )
                InfoRow("Subjects", snapshot?.subjects?.size?.toString() ?: "—")
                InfoRow(
                    "Primary Subject",
                    snapshot?.primarySubject?.value?.let { p ->
                        "${p.category.name}${p.rawLabel?.let { " ($it)" } ?: ""}"
                    } ?: "—",
                )
                InfoRow("Snapshot Source", snapshot?.source?.name ?: "—")
                InfoRow(
                    "Embedding",
                    snapshot?.embedding?.let { emb ->
                        if (emb.present) "%d-d conf %.2f".format(emb.value?.size ?: 0, emb.confidence)
                        else "absent"
                    } ?: "—",
                )
                InfoRow("Last Inference", snapshot?.analyzedAtMs?.toString() ?: "—")
                InfoRow("Skipped Inferences", aiScene?.skippedInferences?.toString() ?: "—")
                InfoRow("Reference Strategy", referenceState.profile?.ai?.strategy?.name ?: "—")
                InfoRow("Match Strategy", referenceMatch.strategy.name)
            }

            SectionCard(title = "Human Perception") {
                Text(
                    "Phase 12: perceptual verdicts between the comparator and the state machine. " +
                        "Per signal: noticeability, importance, confidence, direction confidence, " +
                        "trend, dead zone, hysteresis, and the final decision. All thresholds are " +
                        "runtime-tunable below (Developer Mode, in-memory only).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                val tolerance = referenceState.profile?.options?.tolerance ?: ReferenceTolerance.MEDIUM
                val effective = perceptualTuning ?: PerceptualTuning.forTolerance(tolerance)
                val prof = effective.profile
                InfoRow(
                    "Tuning Source",
                    if (perceptualTuning != null) "override (in-memory)" else "defaults (${tolerance.name})",
                )
                InfoRow("Enter / Leave", "%.2f / %.2f".format(prof.enterNoticeability, prof.leaveNoticeability))
                InfoRow("Overall Score", "%.2f".format(referenceMatch.overallScore))

                for (signal in ReferenceSignal.entries) {
                    val s = referenceMatch.signal(signal)
                    val per = s.perception
                    if (per == null) {
                        InfoRow(signal.name, if (!s.enabled) "disabled" else "no perception")
                        continue
                    }
                    InfoRow(
                        signal.name,
                        "n=%.2f imp=%.2f %s".format(
                            per.humanNoticeability, per.importance,
                            if (s.matched) "OK" else per.severity.name,
                        ),
                    )
                    InfoRow("  raw / dead zone", "%.2f / %.2f".format(per.rawDifference, per.deadZoneApplied))
                    InfoRow("  conf / dirConf", "%.2f / %.2f %s".format(per.confidence, per.directionConfidence, per.direction.name))
                    InfoRow("  trend / hysteresis", "${per.trend.name} / ${per.hysteresisState.name}")
                    InfoRow(
                        "  weight / decision",
                        "%.2f / %s".format(
                            per.strategyWeight,
                            if (s.confirmedDrift) "CONFIRMED_DRIFT" else s.state.name,
                        ),
                    )
                    per.matchBucket?.let { InfoRow("  perceived match", it.name) }
                }

                if (devMode) {
                    fun update(transform: (PerceptualThresholds.Profile) -> PerceptualThresholds.Profile) {
                        onPerceptualTuningChange(effective.copy(profile = transform(effective.profile)))
                    }
                    TuningField("Hysteresis enter", prof.enterNoticeability) { v ->
                        update { it.copy(enterNoticeability = v) }
                    }
                    TuningField("Hysteresis leave", prof.leaveNoticeability) { v ->
                        update { it.copy(leaveNoticeability = v) }
                    }
                    TuningField("Spike damping factor", prof.spikeDampingFactor) { v ->
                        update { it.copy(spikeDampingFactor = v) }
                    }
                    TuningField("Exposure mean dead zone", prof.exposureMean.deadZone) { v ->
                        update { it.copy(exposureMean = it.exposureMean.copy(deadZone = v)) }
                    }
                    TuningField("Warmth dead zone", prof.warmth.deadZone) { v ->
                        update { it.copy(warmth = it.warmth.copy(deadZone = v)) }
                    }
                    TuningField("Position dead zone", prof.position.deadZone) { v ->
                        update { it.copy(position = it.position.copy(deadZone = v)) }
                    }
                    TuningField("Size dead zone", prof.size.deadZone) { v ->
                        update { it.copy(size = it.size.copy(deadZone = v)) }
                    }
                    TuningField("Composition dead zone", prof.composition.deadZone) { v ->
                        update { it.copy(composition = it.composition.copy(deadZone = v)) }
                    }
                    TuningField("Presence noticeability", prof.presenceNoticeability) { v ->
                        update { it.copy(presenceNoticeability = v) }
                    }
                    TuningField("Importance gamma", prof.importanceGamma) { v ->
                        update { it.copy(importanceGamma = v) }
                    }
                    OutlinedButton(
                        onClick = { onPerceptualTuningChange(null) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Reset to ${tolerance.name} defaults") }
                } else {
                    Text(
                        "Enable Developer Mode to tune perception thresholds live.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            SectionCard(title = "Assistant Instructions") { // Phase 13
                Text(
                    "Phase 13: translation layer diagnostics. Per drifting signal: the original " +
                        "perceptual description and the operator instruction it became.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                val selected = InstructionSelector.select(referenceMatch)
                InfoRow("Selected", selected?.action?.name ?: "—")
                InfoRow("Selected Message", selected?.message ?: "—")
                var anyDrift = false
                for (signal in ReferenceSignal.entries) {
                    val s = referenceMatch.signal(signal)
                    val instruction = s.instruction ?: continue
                    anyDrift = true
                    InfoRow(signal.name, instruction.action.name)
                    InfoRow("  perception", s.message.ifBlank { "—" })
                    InfoRow("  instruction", instruction.message)
                    InfoRow(
                        "  progress / conf",
                        "%.0f%% / %.2f".format(instruction.progress * 100, instruction.confidence),
                    )
                }
                if (!anyDrift) InfoRow("Drifting Signals", "none")
            }

            SectionCard(title = "Voice Guidance") { // Phase 14
                Text(
                    "Phase 14: voice scheduler observability. Voice is an output-only consumer of " +
                        "the instruction + alert streams; settings live on the Settings tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                InfoRow("Enabled / Mode", if (voiceSettings.enabled) voiceSettings.mode.name else "OFF")
                InfoRow("Speech Rate", voiceSettings.speechRate.name)
                InfoRow("Assistant Reminder", "${voiceSettings.assistantReminderSeconds}s")
                InfoRow("Speaking", voiceDebug.speaking ?: "—")
                InfoRow("Queue Length", voiceDebug.queued.size.toString())
                voiceDebug.queued.forEachIndexed { i, key -> InfoRow("  queue[$i]", key) }
                if (voiceDebug.cooldownsRemainingMs.isEmpty()) {
                    InfoRow("Cooldowns", "none active")
                } else {
                    voiceDebug.cooldownsRemainingMs.forEach { (key, ms) ->
                        InfoRow("  cooldown $key", "${ms}ms")
                    }
                }
                InfoRow("Last Spoken", voiceDebug.lastSpoken ?: "—")
                InfoRow("Spoken Count", voiceDebug.spokenCount.toString())
                InfoRow("Suppressed Count", voiceDebug.suppressedCount.toString())
                InfoRow("Last Suppressed", voiceDebug.lastSuppressedReason ?: "—")
            }

            SectionCard(title = "Connection details") {
                Text(
                    "Raw protocol pipeline views (BLE, SSH, PTP/IP, device info, diagnostics). " +
                        "Hidden from the main camera screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                for (dest in DETAIL_DESTINATIONS) {
                    OutlinedButton(
                        onClick = { onNavigate(dest) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text(dest.title) }
                }
            }

            SectionCard(title = "EE service diagnostics") {
                Text(
                    "Reads the EE registration-state fields (EE02 / EE04) without writing anything. " +
                        "Use to inspect the camera's pairing/registration state before sending any " +
                        "pairing command. Requires an active BLE connection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onReadEe02, modifier = Modifier.weight(1f)) { Text("Read EE02") }
                    OutlinedButton(onClick = onReadEe04, modifier = Modifier.weight(1f)) { Text("Read EE04") }
                }
                OutlinedButton(
                    onClick = onDumpEeState,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text("Dump EE State") }
            }

            if (!devMode) {
                SectionCard(title = "Protocol logs") {
                    Text(
                        "Enable Developer Mode to view raw protocol packet logs.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = { LogStore.clear() }, modifier = Modifier.weight(1f)) {
                        Text("Clear")
                    }
                    OutlinedButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Dyrecto protocol log")
                                putExtra(Intent.EXTRA_TEXT, LogStore.export())
                            }
                            context.startActivity(Intent.createChooser(send, "Export log"))
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Export") }
                }

                Text(
                    "Protocol log (${lines.size} lines)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }

        // Log viewer: only in dev mode, takes the other half of the screen via weight(1f).
        // LazyColumn handles its own internal scrolling and auto-scrolls to the latest line.
        if (devMode) {
            val listState = rememberLazyListState()
            LaunchedEffect(lines.size) {
                if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
            }

            Card(Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                ) {
                    items(lines) { line ->
                        Text(
                            line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (line.contains("[ERROR]")) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/** "mobilenet_v3_small · ready" — one-line model status for the AI diagnostics card. */
private fun capabilityLabel(capability: AiCapability): String {
    val model = capability.modelId.ifBlank { "—" }
    val status = when (capability.status) {
        EngineStatus.NOT_LOADED -> "not loaded"
        EngineStatus.READY -> "ready"
        EngineStatus.FAILED -> "FAILED"
    }
    return "$model · $status"
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** Numeric tuning input for the Human Perception panel — commits on every valid float. */
@Composable
private fun TuningField(label: String, value: Float, onChange: (Float) -> Unit) {
    var text by remember(label) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            input.toFloatOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}
