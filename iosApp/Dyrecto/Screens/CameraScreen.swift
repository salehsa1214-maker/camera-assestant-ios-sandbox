import SwiftUI
import DyrectoShared

/// Dashboard — the premium, end-user home (port of CameraScreen.kt). Top to bottom: is the
/// camera connected and how is it doing, what does the assistant want right now, is anything
/// wrong, is monitoring on, and the two primary shortcuts (Live View, Voice Guidance).
/// Pure presentation over existing session state — no logic.
struct CameraScreen: View {
    @ObservedObject var session: MonitoringSessionIos
    let onOpenLiveView: () -> Void
    let onOpenDetails: () -> Void
    let onScanCameras: () -> Void

    @EnvironmentObject private var services: AppServices
    @Environment(\.switchTab) private var switchTab

    var body: some View {
        let state = session.state
        let showDashboard = state.isFullyConnectedIos || state.telemetry != nil
        Group {
            if showDashboard {
                CameraDashboard(
                    session: session,
                    alerts: services.alertHistory.alerts,
                    voiceSettings: services.voiceStore.settings,
                    onDisconnect: { session.disconnect() },
                    onOpenAlerts: { switchTab(.alerts) },
                    onOpenLiveView: onOpenLiveView,
                    onOpenDetails: onOpenDetails,
                    onOpenReference: { switchTab(.storyboard) },
                    onOpenVoice: { switchTab(.settings) },
                    onStartMonitoring: { session.startReferenceMonitoring() },
                    onStopMonitoring: { session.stopReferenceMonitoring() })
            } else {
                ConnectHome(
                    state: state,
                    onScanCameras: onScanCameras,
                    onDisconnect: { session.disconnect() })
            }
        }
        .background(DyrectoColor.surfaceBase)
        .toolbar(.hidden, for: .navigationBar) // the screen owns its own large header
    }
}

/// Compact, user-facing connection status derived from the detailed pipeline phase.
func connectionStatus(of state: CameraConnectionState) -> (String, StatusLevel) {
    if state.phase == .idle { return ("Disconnected", .idle) }
    if state.phase == .error { return ("Error", .bad) }
    if state.isFullyConnectedIos { return ("Connected", .good) }
    return ("Connecting", .warn)
}

// MARK: - Connect home (pre-connection hero)

private struct ConnectHome: View {
    let state: CameraConnectionState
    let onScanCameras: () -> Void
    let onDisconnect: () -> Void

    var body: some View {
        let (statusText, level) = connectionStatus(of: state)
        let connecting = state.runningIos

        VStack(spacing: 20) {
            Spacer()
            ZStack {
                Circle()
                    .fill(RadialGradient(
                        colors: [DyrectoColor.accent.opacity(0.22), .clear],
                        center: .center, startRadius: 0, endRadius: 70))
                    .frame(width: 140, height: 140)
                Circle()
                    .fill(DyrectoColor.surfaceElevated)
                    .frame(width: 96, height: 96)
                Image(systemName: "camera.fill")
                    .font(.system(size: 40))
                    .foregroundColor(DyrectoColor.accent)
            }

            Text("Dyrecto")
                .font(DyrectoType.headlineMedium)
                .foregroundColor(DyrectoColor.textPrimary)

            StatusChip(text: statusText, level: level)

            if state.phase == .error, let fatalError = state.fatalError {
                Text(fatalError)
                    .font(DyrectoType.bodyMedium)
                    .foregroundColor(DyrectoColor.error)
                    .multilineTextAlignment(.center)
            }

            Spacer().frame(height: 8)

            if connecting {
                ProgressView()
                    .tint(DyrectoColor.accent)
                Button("Cancel", action: onDisconnect)
                    .font(DyrectoType.labelLarge)
                    .foregroundColor(DyrectoColor.accent)
            } else {
                Button(action: onScanCameras) {
                    Text("Scan for Camera")
                        .font(DyrectoType.titleMedium)
                        .foregroundColor(DyrectoColor.onPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(DyrectoColor.accent)
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            }
            Spacer()
        }
        .padding(24)
    }
}

// MARK: - Dashboard

private struct CameraDashboard: View {
    @ObservedObject var session: MonitoringSessionIos
    let alerts: [DyrectoAlert]
    let voiceSettings: VoiceSettings
    let onDisconnect: () -> Void
    let onOpenAlerts: () -> Void
    let onOpenLiveView: () -> Void
    let onOpenDetails: () -> Void
    let onOpenReference: () -> Void
    let onOpenVoice: () -> Void
    let onStartMonitoring: () -> Void
    let onStopMonitoring: () -> Void

    var body: some View {
        let state = session.state
        let (statusText, level) = connectionStatus(of: state)
        let latestAlert = alerts.first
        // interop: Kotlin object → .shared (InstructionSelector.select(match:))
        let instruction = InstructionSelector.shared.select(match: session.referenceMatch)

        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.cardGap) {
                ScreenHeader(title: "Dashboard", subtitle: "Ready to capture your next shot.") {
                    HeaderIconButton(
                        systemIcon: "bell",
                        showsBadge: !alerts.isEmpty,
                        onClick: onOpenAlerts)
                }

                CameraHeroCard(
                    state: state, statusText: statusText, level: level,
                    onOpenDetails: onOpenDetails, onDisconnect: onDisconnect)

                GuidanceCard(instruction: instruction)

                if let latestAlert {
                    CriticalAlertCard(alert: latestAlert, onOpen: onOpenAlerts)
                }

                MonitoringCard(
                    active: session.referenceState.monitoringRequested,
                    hasProfile: session.referenceState.profile != nil,
                    onStart: onStartMonitoring,
                    onStop: onStopMonitoring,
                    onSetup: onOpenReference)

                QuickActions(
                    liveEnabled: state.telemetry != nil,
                    frame: session.frame,
                    frameActive: session.frameStreamActive,
                    onOpenLiveView: onOpenLiveView,
                    voiceSettings: voiceSettings,
                    onOpenVoice: onOpenVoice)
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.top, 12)
            .padding(.bottom, DyrectoSpacing.bottomInset)
        }
    }
}

// MARK: - Camera hero card

private struct CameraHeroCard: View {
    let state: CameraConnectionState
    let statusText: String
    let level: StatusLevel
    let onOpenDetails: () -> Void
    let onDisconnect: () -> Void

    var body: some View {
        let t = state.telemetry
        PremiumCard {
            HStack {
                ZStack {
                    Circle()
                        .fill(RadialGradient(
                            colors: [DyrectoColor.accent.opacity(0.22), .clear],
                            center: .center, startRadius: 0, endRadius: 34))
                        .frame(width: 68, height: 68)
                    Circle().fill(DyrectoColor.surfaceElevated).frame(width: 58, height: 58)
                    Image(systemName: "camera.fill")
                        .font(.system(size: 24))
                        .foregroundColor(DyrectoColor.accent)
                }
                VStack(alignment: .leading, spacing: 6) {
                    Text(displayModel)
                        .font(DyrectoType.titleLarge)
                        .foregroundColor(DyrectoColor.textPrimary)
                    StatusChip(text: statusText, level: level)
                }
                .padding(.leading, 14)
                Spacer()
                Button(action: onOpenDetails) {
                    HStack(spacing: 2) {
                        Text("Details").font(DyrectoType.labelLarge)
                        Image(systemName: "chevron.right").font(.system(size: 12, weight: .semibold))
                    }
                    .foregroundColor(DyrectoColor.accent)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .overlay(Capsule().strokeBorder(DyrectoColor.hairline, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }

            Spacer().frame(height: 20)

            HStack(alignment: .top, spacing: 6) {
                StatItem(
                    systemIcon: "battery.100", label: "Battery",
                    value: telemetryDisplay(t, TelemetryCode.battery),
                    fraction: batteryFraction(state), accent: batteryAccent(state))
                StatItem(
                    systemIcon: "thermometer.medium", label: "Temp",
                    value: telemetryDisplay(t, TelemetryCode.overheating))
                StatItem(
                    systemIcon: "sdcard", label: "Storage",
                    value: telemetryDisplay(t, TelemetryCode.slot1Remain))
                StatItem(
                    systemIcon: "record.circle", label: "Recording",
                    value: telemetryDisplay(t, TelemetryCode.movieRec),
                    accent: recordingAccent(state))
            }

            HStack {
                Text("Updated \(formatTimeShort(state.lastTelemetryUpdateAt))")
                    .font(DyrectoType.labelSmall)
                    .foregroundColor(DyrectoColor.textMuted)
                Spacer()
                Button("Disconnect", action: onDisconnect)
                    .font(DyrectoType.labelMedium)
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .padding(.top, 6)
        }
    }

    private var displayModel: String {
        let model = state.deviceInfo?.model ?? ""
        return model.isEmpty ? "Camera" : model
    }
}

/// One camera stat: icon, small label, large value, optional thin fill bar.
private struct StatItem: View {
    let systemIcon: String
    let label: String
    let value: String
    var fraction: Float? = nil
    var accent: Color? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Image(systemName: systemIcon)
                .font(.system(size: 15))
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 8)
            Text(label.uppercased())
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .foregroundColor(DyrectoColor.textMuted)
                .lineLimit(1)
            Spacer().frame(height: 2)
            Text(value)
                .font(DyrectoType.titleMedium)
                .foregroundColor(accent ?? DyrectoColor.textPrimary)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            if let fraction {
                Spacer().frame(height: 8)
                ThinBar(fraction, color: accent ?? DyrectoColor.accent)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - Assistant guidance card (the hero of the screen)

private struct GuidanceCard: View {
    let instruction: AssistantInstruction?

    var body: some View {
        let accent = DyrectoColor.accent
        let action = instruction?.action ?? .none
        let headline = instruction == nil ? "Everything looks good" : actionHeadline(action)
        let detail: String = {
            guard let instruction else { return "Keep shooting." }
            return instruction.message.isEmpty ? actionHeadline(action) : instruction.message
        }()

        VStack(spacing: 0) {
            Text("CURRENT GUIDANCE")
                .font(DyrectoType.labelMedium)
                .kerning(DyrectoType.labelMediumKerning)
                .fontWeight(.semibold)
                .foregroundColor(accent)

            Spacer().frame(height: 20)

            ZStack {
                Circle()
                    .fill(RadialGradient(
                        colors: [accent.opacity(0.28), .clear],
                        center: .center, startRadius: 0, endRadius: 64))
                    .frame(width: 128, height: 128)
                Circle()
                    .fill(LinearGradient(
                        colors: [accent.opacity(0.28), accent.opacity(0.08)],
                        startPoint: .top, endPoint: .bottom))
                    .frame(width: 108, height: 108)
                    .overlay(Circle().strokeBorder(accent.opacity(0.35), lineWidth: 1))
                Image(systemName: actionIcon(action))
                    .font(.system(size: 44, weight: .medium))
                    .foregroundColor(instruction == nil ? DyrectoColor.statusGood : accent)
            }

            Spacer().frame(height: 22)

            Text(headline)
                .font(DyrectoType.headlineSmall)
                .fontWeight(.bold)
                .foregroundColor(DyrectoColor.textPrimary)
                .multilineTextAlignment(.center)
            Spacer().frame(height: 6)
            Text(detail)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
                .multilineTextAlignment(.center)

            if let instruction {
                Spacer().frame(height: 18)
                ThinBar(instruction.progress, color: accent)
                Spacer().frame(height: 8)
                // interop: InstructionFormatter object → .shared
                Text("\(InstructionFormatter.shared.progressPercent(instruction: instruction)) matched · " +
                     "\(InstructionFormatter.shared.confidenceLabel(instruction: instruction)) confidence")
                    .font(DyrectoType.labelSmall)
                    .foregroundColor(DyrectoColor.textMuted)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 24)
        .padding(.vertical, 28)
        .background(LinearGradient(
            colors: [DyrectoColor.accent.opacity(0.10), DyrectoColor.surfaceCard],
            startPoint: .top, endPoint: .bottom))
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.guidanceCard, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.guidanceCard, style: .continuous)
                .strokeBorder(DyrectoColor.accent.opacity(0.20), lineWidth: 1))
    }
}

// MARK: - Current critical alert

private struct CriticalAlertCard: View {
    let alert: DyrectoAlert
    let onOpen: () -> Void

    var body: some View {
        let accent = statusLevel(for: alert.severity).color
        Button(action: onOpen) {
            HStack {
                ZStack {
                    Circle().fill(accent.opacity(0.18)).frame(width: 44, height: 44)
                    Image(systemName: "record.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(accent)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(alert.title)
                        .font(DyrectoType.titleMedium)
                        .foregroundColor(DyrectoColor.textPrimary)
                    if !alert.message.isEmpty {
                        Text(alert.message)
                            .font(DyrectoType.bodySmall)
                            .foregroundColor(DyrectoColor.textMuted)
                            .lineLimit(1)
                    }
                }
                .padding(.leading, 14)
                Spacer()
                Text(formatTimeShort(alert.timestamp))
                    .font(DyrectoType.labelSmall)
                    .foregroundColor(DyrectoColor.textMuted)
                    .padding(.leading, 8)
                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .padding(16)
            .background(accent.opacity(0.12))
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous)
                    .strokeBorder(accent.opacity(0.25), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Monitoring status

private struct MonitoringCard: View {
    let active: Bool
    let hasProfile: Bool
    let onStart: () -> Void
    let onStop: () -> Void
    let onSetup: () -> Void

    var body: some View {
        let level: StatusLevel = active ? .good : .idle
        HStack {
            ZStack {
                Circle().fill(level.color.opacity(0.16)).frame(width: 44, height: 44)
                Image(systemName: "camera.metering.center.weighted")
                    .font(.system(size: 18))
                    .foregroundColor(level.color)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(active ? "Monitoring Active" : "Monitoring Paused")
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text("Live View · AI Assistant · Alerts")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .padding(.leading, 14)
            Spacer()
            if !hasProfile {
                PillButton(text: "Set Up", onClick: onSetup)
            } else if active {
                PillButton(text: "Stop", onClick: onStop)
            } else {
                Button(action: onStart) {
                    Text("Start")
                        .font(DyrectoType.labelLarge)
                        .fontWeight(.semibold)
                        .foregroundColor(DyrectoColor.onPrimary)
                        .padding(.horizontal, 18)
                        .padding(.vertical, 10)
                        .background(DyrectoColor.accent)
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(16)
        .background(DyrectoColor.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous)
                .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
    }
}

// MARK: - Quick actions (exactly two shortcuts)

private struct QuickActions: View {
    let liveEnabled: Bool
    let frame: UIImage?
    let frameActive: Bool
    let onOpenLiveView: () -> Void
    let voiceSettings: VoiceSettings
    let onOpenVoice: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            LiveViewQuickCard(
                enabled: liveEnabled, frame: frame, running: frameActive, onOpen: onOpenLiveView)
            VoiceQuickCard(settings: voiceSettings, onOpen: onOpenVoice)
        }
    }
}

private struct LiveViewQuickCard: View {
    let enabled: Bool
    let frame: UIImage?
    let running: Bool
    let onOpen: () -> Void

    var body: some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: 0) {
                Text("Live View")
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text("See what the camera sees")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
                    .padding(.top, 2)
                Spacer().frame(height: 14)
                ZStack(alignment: .topLeading) {
                    RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                        .fill(DyrectoColor.surfaceElevated)
                        .aspectRatio(16.0 / 10.0, contentMode: .fit)
                    if running, let frame {
                        Image(uiImage: frame)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .aspectRatio(16.0 / 10.0, contentMode: .fit)
                            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
                        HStack(spacing: 5) {
                            Circle().fill(DyrectoColor.accent).frame(width: 6, height: 6)
                            Text("LIVE")
                                .font(DyrectoType.labelSmall)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.black.opacity(0.45))
                        .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                        .padding(8)
                    } else {
                        VStack {
                            Spacer()
                            HStack {
                                Spacer()
                                ZStack {
                                    Circle()
                                        .fill(enabled
                                            ? DyrectoColor.accent.opacity(0.18)
                                            : DyrectoColor.textPrimary.opacity(0.06))
                                        .frame(width: 44, height: 44)
                                    Image(systemName: "play.fill")
                                        .font(.system(size: 20))
                                        .foregroundColor(enabled ? DyrectoColor.accent : DyrectoColor.textMuted)
                                }
                                Spacer()
                            }
                            Spacer()
                        }
                    }
                }
            }
            .padding(16)
            .background(DyrectoColor.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 22, style: .continuous)
                    .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

private struct VoiceQuickCard: View {
    let settings: VoiceSettings
    let onOpen: () -> Void

    var body: some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: 0) {
                Text("Voice Guidance")
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text("Listen to assistant guidance")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
                    .padding(.top, 2)
                Spacer().frame(height: 14)
                ZStack(alignment: .bottomLeading) {
                    RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                        .fill(DyrectoColor.surfaceElevated)
                        .aspectRatio(16.0 / 10.0, contentMode: .fit)
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            ZStack {
                                Circle()
                                    .fill(RadialGradient(
                                        colors: [DyrectoColor.voiceAccent.opacity(0.22), .clear],
                                        center: .center, startRadius: 0, endRadius: 30))
                                    .frame(width: 60, height: 60)
                                Circle()
                                    .fill(settings.enabled
                                        ? DyrectoColor.voiceAccent.opacity(0.22)
                                        : DyrectoColor.textPrimary.opacity(0.06))
                                    .frame(width: 44, height: 44)
                                Image(systemName: "speaker.wave.2.fill")
                                    .font(.system(size: 18))
                                    .foregroundColor(settings.enabled
                                        ? DyrectoColor.voiceAccent : DyrectoColor.textMuted)
                            }
                            Spacer()
                        }
                        Spacer()
                    }
                    Text(voiceStatus(settings))
                        .font(DyrectoType.labelSmall)
                        .fontWeight(.semibold)
                        .foregroundColor(settings.enabled ? DyrectoColor.voiceAccent : DyrectoColor.textMuted)
                        .padding(8)
                }
            }
            .padding(16)
            .background(DyrectoColor.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 22, style: .continuous)
                    .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Presentation-only helpers (no logic / no telemetry change)

/// Short voice-status caption from the persisted settings (never toggles anything here).
private func voiceStatus(_ settings: VoiceSettings) -> String {
    if !settings.enabled { return "Off" }
    if settings.mode == .assistantOnly { return "Assistant" }
    if settings.mode == .telemetryOnly { return "Alerts" }
    return "On"
}

/// The circular icon shown for each recommended action in the guidance card (SF equivalents
/// of the Android Material icons).
func actionIcon(_ action: AssistantAction) -> String {
    switch action {
    case .panLeft: return "arrow.left"
    case .panRight: return "arrow.right"
    case .tiltUp: return "arrow.up"
    case .tiltDown: return "arrow.down"
    case .moveCloser, .zoomIn: return "plus"
    case .moveBack, .zoomOut: return "minus"
    case .increaseExposure: return "arrow.up"
    case .reduceExposure: return "arrow.down"
    case .coolWhiteBalance: return "snowflake"
    case .warmWhiteBalance: return "sun.max"
    case .waitForSubject: return "hourglass"
    case .reframe: return "viewfinder"
    case .matchReference: return "slider.horizontal.3"
    case .none: return "checkmark.circle"
    default: return "checkmark.circle"
    }
}

/// Short imperative headline for each action (the big line in the guidance card).
func actionHeadline(_ action: AssistantAction) -> String {
    switch action {
    case .panLeft: return "Pan left"
    case .panRight: return "Pan right"
    case .tiltUp: return "Tilt up"
    case .tiltDown: return "Tilt down"
    case .moveCloser: return "Move closer"
    case .moveBack: return "Move back"
    case .zoomIn: return "Zoom in"
    case .zoomOut: return "Zoom out"
    case .increaseExposure: return "Raise the exposure"
    case .reduceExposure: return "Lower the exposure"
    case .coolWhiteBalance: return "Cool the white balance"
    case .warmWhiteBalance: return "Warm the white balance"
    case .waitForSubject: return "Wait for the subject"
    case .reframe: return "Reframe the shot"
    case .matchReference: return "Match the reference"
    case .none: return "Everything looks good"
    default: return "Everything looks good"
    }
}

/// Battery level as a 0..1 fraction for the stat bar, or nil when unknown.
private func batteryFraction(_ state: CameraConnectionState) -> Float? {
    guard let pct = state.telemetry?.get(code: TelemetryCode.battery)?.rawNumber?.int64Value
    else { return nil }
    return min(max(Float(pct) / 100.0, 0), 1)
}

/// Red while recording, so the tile reads at a glance; default otherwise.
private func recordingAccent(_ state: CameraConnectionState) -> Color? {
    let v = telemetryDisplay(state.telemetry, TelemetryCode.movieRec).lowercased()
    return (v.contains("rec") && !v.contains("standby")) ? DyrectoColor.statusBad : nil
}

/// Amber/red when battery is low; default otherwise.
private func batteryAccent(_ state: CameraConnectionState) -> Color? {
    guard let pct = state.telemetry?.get(code: TelemetryCode.battery)?.rawNumber?.int64Value
    else { return nil }
    if pct <= 10 { return DyrectoColor.statusBad }
    if pct <= 20 { return DyrectoColor.statusWarn }
    return nil
}
