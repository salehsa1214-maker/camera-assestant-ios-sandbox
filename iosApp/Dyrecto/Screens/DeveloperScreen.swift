import SwiftUI
import DyrectoShared

/// Developer — engineering diagnostics and raw protocol tools (port of DeveloperScreen.kt).
/// Everything here is read-only diagnostics or a backend/protocol tool; user-facing knobs live
/// on the Settings tab. Streams the `DyrectoLog` protocol/event log at the bottom.
struct DeveloperScreen: View {
    @ObservedObject var session: MonitoringSessionIos

    @EnvironmentObject private var services: AppServices
    @ObservedObject private var log = DyrectoLog.shared
    @State private var devIp = "192.168.122.1"
    @State private var showPairingWarning = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.section) {
                cameraIpCard
                pushLvCard
                visionCard
                histogramCard
                zebraCard
                pictureProfileCard
                exposureAlertsCard
                sceneCard
                referenceCard
                aiCapabilitiesCard
                instructionsCard
                voiceCard
                pairingCard
                eeCard
                logCard
            }
            .padding(.horizontal, DyrectoSpacing.screen)
            .padding(.vertical, DyrectoSpacing.screen)
        }
        .background(DyrectoColor.surfaceBase)
        .navigationTitle("Developer")
        .navigationBarTitleDisplayMode(.inline)
        .alert("Pair / Register (EE01)", isPresented: $showPairingWarning) {
            Button("Cancel", role: .cancel) {}
            Button("Run Pairing", role: .destructive) { session.runOneTimePairing() }
        } message: {
            Text("Runs the one-time EE01 pairing/registration write so the camera mints SSH " +
                 "credentials for this phone. Only needed once per camera — running it while " +
                 "already registered may invalidate the existing registration. Requires an " +
                 "active BLE connection.")
        }
    }

    // MARK: Cards

    private var cameraIpCard: some View {
        DevCard(title: "Manual camera IP override") {
            DevNote("Use only when the automatic BLE Wi-Fi provisioning path is unavailable. " +
                    "This overrides the IP used for the SSH/PTP layer. " +
                    "Not shown on the main screen.")
            TextField("Camera IP (dev override)", text: $devIp)
                .keyboardType(.decimalPad)
                .font(DyrectoType.mono)
                .foregroundColor(DyrectoColor.textPrimary)
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(DyrectoColor.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                .onChange(of: devIp) { session.setCameraIp($0) }
        }
    }

    private var pushLvCard: some View {
        DevCard(title: "Push Live View") {
            DevNote("Engineering view of the push live-view pipeline (SDIO_ControlMonitoring " +
                    "0x9230): phase, ports, Start rc, byte counters, render metrics.")
            // interop: pushStatusRows — ordered (label, value) diagnostics pairs published by
            // the parallel session layer (Android PushLvStatus flattened for display).
            ForEach(Array(session.pushStatusRows.enumerated()), id: \.offset) { _, row in
                DevRow(row.0, row.1)
            }
            DevRow("Displayed FPS", String(format: "%.1f", session.displayedFps))
            DevRow("Frames Displayed", "\(session.framesDisplayed)")
            DevRow("Stream Active", session.frameStreamActive ? "Yes" : "No")
            HStack(spacing: 12) {
                DevButton("Start Push LV") { session.startPushLiveView() }
                DevButton("Stop Push LV") { session.stopPushLiveView() }
            }
            .padding(.top, 8)
        }
    }

    private var visionCard: some View {
        let stats = session.visionStatistics
        let vision = session.visionContext
        return DevCard(title: "Vision") {
            DevNote("Phase 6: histogram + zebra exposure analysis + face/eye detection. Results " +
                    "update every analyzed frame.")
            DevRow("Vision Running", stats.pipelineRunning ? "Yes" : "No")
            DevRow("Registered Modules", "\(stats.registeredModules)")
            DevRow("Frames Received", "\(stats.framesReceived)")
            DevRow("Frames Analyzed", "\(stats.framesAnalyzed)")
            DevRow("Frames Dropped", "\(stats.framesDropped)")
            DevRow("Analysis FPS", String(format: "%.1f", stats.analysisFps))
            DevRow("Last Analysis", "\(stats.lastAnalysisDurationMs) ms")
            DevRow("Average Analysis", String(format: "%.1f ms", stats.averageAnalysisDurationMs))
            DevRow("Faces Detected", vision.faces.map { "\($0.facesDetected)" } ?? "—")
            DevRow("Eyes Detected", vision.eyes.map { "\($0.eyesDetected)" } ?? "—")
        }
    }

    private var histogramCard: some View {
        let hist = session.visionContext.histogram
        return DevCard(title: "Histogram") {
            DevNote("Phase 6: 256-bin luma statistics (BT.709). Canonical exposure source; every " +
                    "future tool reads these instead of rescanning.")
            DevRow("Mean", hist.map { String(format: "%.1f", $0.mean) } ?? "—")
            DevRow("Median", hist.map { "\($0.median)" } ?? "—")
            DevRow("P95", hist.map { "\($0.percentile95)" } ?? "—")
            DevRow("P99", hist.map { "\($0.percentile99)" } ?? "—")
            DevRow("Highlight %", hist.map { String(format: "%.1f%%", $0.clippedHighlightPercentage) } ?? "—")
            DevRow("Shadow %", hist.map { String(format: "%.1f%%", $0.clippedShadowPercentage) } ?? "—")
            DevRow("Sampled Pixels", hist.map { "\($0.totalPixels)" } ?? "—")
            DevRow("Effective Stride", hist.map {
                "\($0.effectiveStride)" + ($0.effectiveStride == 1 ? " (full-res)" : " (downsampled)")
            } ?? "—")
        }
    }

    private var zebraCard: some View {
        let zebra = session.visionContext.zebra
        return DevCard(title: "Zebra") {
            DevNote("Phase 6: IRE-mapped coverage (Sony 16–235 studio-swing). Zebra2 level. " +
                    "The threshold control now lives on the Settings tab; this is a live readout.")
            DevRow("Active Threshold", "\(services.exposureConfig.zebraSpec.label) IRE")
            DevRow("Coverage %", zebra.map { String(format: "%.1f%%", $0.coveragePercentage) } ?? "—")
        }
    }

    private var pictureProfileCard: some View {
        DevCard(title: "Picture Profile (Phase 8)") {
            DevNote("Analysis Color Transform applied before Histogram/Zebra/Exposure see the frame. " +
                    "Auto currently resolves to Rec709 (no camera telemetry for gamma/picture " +
                    "profile yet). The profile selector now lives on the Settings tab; this is a " +
                    "live readout.")
            DevRow("Active Profile", services.exposureConfig.analysisColorSpace.name)
            // interop: resolveTransform() returns the shared AnalysisColorTransform impl.
            DevRow("Resolved Transform",
                   String(describing: type(of: ExposureConfig.shared.resolveTransform())))
            DevRow("LUT Loaded", ExposureConfig.shared.sLog3LutName != nil ? "Yes" : "No")
            DevRow("LUT Name", ExposureConfig.shared.sLog3LutName ?? "—")
        }
    }

    private var exposureAlertsCard: some View {
        let exposure = session.visionContext.exposure
        return DevCard(title: "Exposure Alerts") {
            DevNote("Phase 7: frame-based persistence/recovery state machine on top of the Zebra/" +
                    "Histogram coverage above. Diagnostics only.")
            DevRow("Highlight Coverage", exposure.map { String(format: "%.1f%%", $0.highlightCoverage) } ?? "—")
            DevRow("Shadow Coverage", exposure.map { String(format: "%.1f%%", $0.shadowCoverage) } ?? "—")
            DevRow("Highlight State", exposure?.highlightState.name ?? "—")
            DevRow("Shadow State", exposure?.shadowState.name ?? "—")
            DevRow("Highlight Persistence", exposure.map { "\($0.highlightPersistenceFrames)" } ?? "—")
            DevRow("Shadow Persistence", exposure.map { "\($0.shadowPersistenceFrames)" } ?? "—")
            DevRow("Highlight Recovery Counter", exposure.map { "\($0.highlightRecoveryFrames)" } ?? "—")
            DevRow("Shadow Recovery Counter", exposure.map { "\($0.shadowRecoveryFrames)" } ?? "—")
            DevRow("Exposure Confidence", exposure.map { String(format: "%.2f", $0.exposureConfidence) } ?? "—")
        }
    }

    private var sceneCard: some View {
        let scene = session.sceneContext
        return DevCard(title: "Scene Understanding") {
            DevNote("Phase 5C: merged, descriptive scene state from telemetry + Vision. " +
                    "Read-only; makes no decisions.")
            DevRow("Overall State", scene.overallState.name)
            DevRow("Recording", scene.recording.isRecording
                ? (scene.recording.recordingDurationAvailable ? "Yes (duration available)" : "Yes")
                : "No")
            DevRow("Highlight %", String(format: "%.1f%%", scene.exposure.highlightPercentage))
            DevRow("Shadow %", String(format: "%.1f%%", scene.exposure.shadowPercentage))
            DevRow("Faces", "\(scene.face.facesDetected)")
            DevRow("Eyes", "\(scene.face.eyesDetected)")
            // interop: Kotlin Int? exports as KotlinInt?.
            DevRow("Battery", scene.battery.batteryLevel.map {
                "\($0.intValue)%" + (scene.battery.isBatteryLow ? " (low)" : "")
            } ?? "—")
            DevRow("Storage", (scene.storage.remainingStatus ?? "—") +
                (scene.storage.isStorageCritical ? " (critical)" : ""))
            DevRow("Updated", "\(scene.updatedAtMs)")
        }
    }

    private var referenceCard: some View {
        let referenceState = session.referenceState
        let match = session.referenceMatch
        let profile = referenceState.profile
        return DevCard(title: "Reference Assistant") {
            DevNote("Phase 9: Shot Reference comparison state. Per-signal drift deltas, state " +
                    "machine states, and persistence/recovery counters. Diagnostics only.")
            DevRow("Reference Id", profile.map { String($0.id.prefix(8)) } ?? "—")
            DevRow("Reference Name", profile?.name ?? "—")
            DevRow("Tolerance", profile?.options.tolerance.name ?? "—")
            DevRow("Monitoring", referenceState.monitoringRequested ? "Requested" : "Off")
            DevRow("Match Active", match.active ? "Yes" : "No")
            DevRow("Overall Score", String(format: "%.2f", match.overallScore))
            DevRow("Exposure Δ", String(format: "%.1f", match.exposureMatch.delta))
            DevRow("WB Δ", String(format: "%.1f", match.whiteBalanceMatch.delta))
            DevRow("Position Δ", String(format: "%.3f", match.subjectPositionMatch.delta))
            DevRow("Size Δ", String(format: "%.3f", match.subjectSizeMatch.delta))
            DevRow("Headroom Δ", String(format: "%.3f", match.headroomMatch.delta))
            ForEach(allReferenceSignals, id: \.name) { signal in
                let s = match.signal(signal: signal)
                DevRow(signal.name,
                       !s.enabled ? "disabled"
                       : !s.available ? "unavailable"
                       : "\(s.state.name) p=\(s.persistenceFrames) r=\(s.recoveryFrames)")
            }
        }
    }

    private var aiCapabilitiesCard: some View {
        let capabilities = session.aiCapabilities
        let match = session.referenceMatch
        return DevCard(title: "AI Reference Assistant") {
            DevNote("Phase 10: on-device perception (detector / embedder / segmenter behind " +
                    "engine seams, detect→track→compare lifecycle). Model status and inference " +
                    "diagnostics.")
            DevRow("Detector", capabilityLabel(capabilities.detection))
            DevRow("Embedder", capabilityLabel(capabilities.embedding))
            DevRow("Segmenter", capabilityLabel(capabilities.segmentation))
            if let error = capabilities.detection.error {
                DevRow("Detector Error", String(error.prefix(48)))
            }
            if let error = capabilities.embedding.error {
                DevRow("Embedder Error", String(error.prefix(48)))
            }
            DevRow("Reference Strategy",
                   session.referenceState.profile?.ai?.strategy.name ?? "—")
            DevRow("Match Strategy", match.strategy.name)
        }
    }

    private var instructionsCard: some View {
        let match = session.referenceMatch
        let selected = InstructionSelector.shared.select(match: match)
        return DevCard(title: "Assistant Instructions") {
            DevNote("Phase 13: translation layer diagnostics. Per drifting signal: the original " +
                    "perceptual description and the operator instruction it became.")
            DevRow("Selected", selected?.action.name ?? "—")
            DevRow("Selected Message", selected?.message ?? "—")
            let drifting = allReferenceSignals.compactMap { signal -> (ReferenceSignal, ReferenceSignalResult, AssistantInstruction)? in
                let s = match.signal(signal: signal)
                guard let instruction = s.instruction else { return nil }
                return (signal, s, instruction)
            }
            if drifting.isEmpty {
                DevRow("Drifting Signals", "none")
            } else {
                ForEach(Array(drifting.enumerated()), id: \.offset) { _, item in
                    let (signal, s, instruction) = item
                    DevRow(signal.name, instruction.action.name)
                    DevRow("  perception", s.message.isEmpty ? "—" : s.message)
                    DevRow("  instruction", instruction.message)
                    DevRow("  progress / conf",
                           String(format: "%.0f%% / %.2f", instruction.progress * 100, instruction.confidence))
                }
            }
        }
    }

    private var voiceCard: some View {
        let voiceSettings = services.voiceStore.settings
        let voiceDebug = session.voiceDebug
        return DevCard(title: "Voice Guidance") {
            DevNote("Phase 14: voice scheduler observability. Voice is an output-only consumer of " +
                    "the instruction + alert streams; settings live on the Settings tab.")
            DevRow("Enabled / Mode", voiceSettings.enabled ? voiceSettings.mode.name : "OFF")
            DevRow("Speech Rate", voiceSettings.speechRate.name)
            DevRow("Assistant Reminder", "\(voiceSettings.assistantReminderSeconds)s")
            DevRow("Speaking", voiceDebug.speaking ?? "—")
            DevRow("Queue Length", "\(voiceDebug.queued.count)")
            ForEach(Array(voiceDebug.queued.enumerated()), id: \.offset) { i, key in
                DevRow("  queue[\(i)]", key)
            }
            if voiceDebug.cooldownsRemainingMs.isEmpty {
                DevRow("Cooldowns", "none active")
            } else {
                // interop: Kotlin Map<String, Long> bridges to [String: KotlinLong].
                ForEach(voiceDebug.cooldownsRemainingMs.keys.sorted(), id: \.self) { key in
                    DevRow("  cooldown \(key)",
                           "\(voiceDebug.cooldownsRemainingMs[key]?.int64Value ?? 0)ms")
                }
            }
            DevRow("Last Spoken", voiceDebug.lastSpoken ?? "—")
            DevRow("Spoken Count", "\(voiceDebug.spokenCount)")
            DevRow("Suppressed Count", "\(voiceDebug.suppressedCount)")
            DevRow("Last Suppressed", voiceDebug.lastSuppressedReason ?? "—")
        }
    }

    private var pairingCard: some View {
        DevCard(title: "Pair / Register (EE01)") {
            DevNote("One-time pairing/registration write — the camera mints SSH credentials for " +
                    "this phone (CC17 then reports SSH ON with id/password). Run once per camera; " +
                    "normal connects never need it. Requires an active BLE connection.")
            DevButton("Run Pair / Register (EE01)") { showPairingWarning = true }
                .padding(.top, 8)
        }
    }

    private var eeCard: some View {
        DevCard(title: "EE service diagnostics") {
            DevNote("Reads the EE registration-state fields (EE02 / EE04) without writing anything. " +
                    "Use to inspect the camera's pairing/registration state before sending any " +
                    "pairing command. Requires an active BLE connection.")
            HStack(spacing: 12) {
                DevButton("Read EE02") { session.readEe02() }
                DevButton("Read EE04") { session.readEe04() }
            }
            .padding(.top, 8)
            DevButton("Dump EE State") { session.dumpEeState() }
                .padding(.top, 8)
        }
    }

    private var logCard: some View {
        DevCard(title: "Protocol log (\(log.entries.count) lines)") {
            HStack(spacing: 12) {
                DevButton("Clear") { log.clear() }
                ShareLink(item: exportLog()) {
                    Text("Export")
                        .font(DyrectoType.labelLarge)
                        .foregroundColor(DyrectoColor.accent)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10, style: .continuous)
                                .strokeBorder(DyrectoColor.accent.opacity(0.5), lineWidth: 1))
                }
            }
            .padding(.bottom, 6)

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 1) {
                        ForEach(log.entries) { entry in
                            Text("\(formatTimeShort(Int64(entry.timestamp.timeIntervalSince1970 * 1000))) " +
                                 "[\(entry.kind.rawValue)] \(entry.message)")
                                .font(DyrectoType.monoSmall)
                                .foregroundColor(entry.kind == .error
                                    ? DyrectoColor.error : DyrectoColor.textPrimary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .id(entry.id)
                        }
                    }
                }
                .frame(height: 320)
                .onChange(of: log.entries.count) { _ in
                    if let last = log.entries.last {
                        withAnimation { proxy.scrollTo(last.id, anchor: .bottom) }
                    }
                }
            }
        }
    }

    private func exportLog() -> String {
        log.entries
            .map { "\(formatTimeShort(Int64($0.timestamp.timeIntervalSince1970 * 1000))) [\($0.kind.rawValue)] \($0.message)" }
            .joined(separator: "\n")
    }
}

// MARK: - Building blocks (Android SectionCard / InfoRow vocabulary)

/// "mobilenet_v3_small · ready" — one-line model status for the AI diagnostics card.
private func capabilityLabel(_ capability: AiCapability) -> String {
    let model = capability.modelId.isEmpty ? "—" : capability.modelId
    let status: String
    switch capability.status {
    case .notLoaded: status = "not loaded"
    case .ready: status = "ready"
    case .failed: status = "FAILED"
    default: status = capability.status.name
    }
    return "\(model) · \(status)"
}

/// A labelled card grouping related diagnostics rows (Android `SectionCard`).
private struct DevCard<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
                .padding(.bottom, 2)
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(DyrectoColor.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous))
    }
}

/// Muted explanatory copy under a card title (Android's bodySmall onSurface 60%).
private struct DevNote: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text)
            .font(DyrectoType.bodySmall)
            .foregroundColor(DyrectoColor.textPrimary.opacity(0.6))
    }
}

/// Label left, monospace value right (Android Developer `InfoRow`).
private struct DevRow: View {
    let label: String
    let value: String
    init(_ label: String, _ value: String) {
        self.label = label
        self.value = value
    }

    var body: some View {
        HStack(alignment: .top) {
            Text(label)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textPrimary)
            Spacer()
            Text(value)
                .font(DyrectoType.mono)
                .foregroundColor(DyrectoColor.textPrimary)
                .multilineTextAlignment(.trailing)
        }
    }
}

/// A full-width outlined action button (Android `OutlinedButton`).
private struct DevButton: View {
    let label: String
    let onClick: () -> Void
    init(_ label: String, onClick: @escaping () -> Void) {
        self.label = label
        self.onClick = onClick
    }

    var body: some View {
        Button(action: onClick) {
            Text(label)
                .font(DyrectoType.labelLarge)
                .foregroundColor(DyrectoColor.accent)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10, style: .continuous)
                        .strokeBorder(DyrectoColor.accent.opacity(0.5), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
