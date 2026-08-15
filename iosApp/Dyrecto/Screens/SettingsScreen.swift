import SwiftUI
import DyrectoShared

/// Settings — the top-level control surface for the app's features (port of SettingsScreen.kt).
/// Everything here is a knob the user is meant to touch: how the assistant speaks (Voice
/// Guidance) and how it reads the image (Exposure zebra threshold, footage Picture Profile).
/// Engineering diagnostics and raw protocol tools stay one level deeper, behind "Developer Mode".
///
/// Pure presentation: the voice controls drive the same persisted `VoiceSettings`, and the
/// exposure controls read/write the same process-scoped shared `ExposureConfig` singleton the
/// Vision worker samples each frame.
struct SettingsScreen: View {
    @ObservedObject var session: MonitoringSessionIos
    let onOpenAlerts: () -> Void
    let onOpenDeveloper: () -> Void

    @EnvironmentObject private var services: AppServices

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.cardGap) {
                ScreenHeader(
                    title: "Settings",
                    subtitle: "Control how the assistant speaks and reads your shot.")

                VoiceGuidanceCard(
                    settings: services.voiceStore.settings,
                    onChange: { session.setVoiceSettings($0) })
                    .environmentObject(services)

                AlertsCard(onOpenAlerts: onOpenAlerts)

                ZebraCard()
                    .environmentObject(services)

                PictureProfileCard()
                    .environmentObject(services)

                AdvancedSection(onOpenDeveloper: onOpenDeveloper)

                AboutSection()
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.top, 12)
            .padding(.bottom, DyrectoSpacing.bottomInset)
        }
        .background(DyrectoColor.surfaceBase)
        .toolbar(.hidden, for: .navigationBar) // owns its own large header (Android parity)
    }
}

// MARK: - Voice guidance (Phase 14)

/// The two source toggles project onto VoiceMode; turning the last remaining source off turns
/// the master switch off (there is no "enabled but silent" mode). Logic preserved verbatim from
/// the Android screen.
private struct VoiceGuidanceCard: View {
    let settings: VoiceSettings
    let onChange: (VoiceSettings) -> Void

    var body: some View {
        // interop: Kotlin enum entries export lowercase (VoiceMode.telemetryOnly, …).
        let assistantOn = settings.mode != .telemetryOnly
        let telemetryOn = settings.mode != .assistantOnly

        PremiumCard {
            CardHeader(systemIcon: "person.wave.2", title: "Voice Guidance")
            Spacer().frame(height: 4)
            Text("Speaks assistant guidance and camera warnings out loud. Uses media volume; audio " +
                 "follows your phone's routing (Bluetooth earbuds win automatically).")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)

            Spacer().frame(height: 16)
            ToggleRow(
                label: "Voice Guidance",
                subtitle: "Master switch for all spoken output.",
                checked: settings.enabled
            ) { onChange(settings.with(enabled: $0)) }

            HairlineDivider()
            ToggleRow(
                label: "Assistant guidance",
                subtitle: "Spoken framing and exposure instructions.",
                checked: assistantOn,
                enabled: settings.enabled
            ) { applySources(assistant: $0, telemetry: telemetryOn) }

            HairlineDivider()
            ToggleRow(
                label: "Telemetry alerts",
                subtitle: "Spoken camera warnings (battery, storage, recording).",
                checked: telemetryOn,
                enabled: settings.enabled
            ) { applySources(assistant: assistantOn, telemetry: $0) }

            HairlineDivider()
            InlineStepperRow(
                title: "Assistant reminder interval",
                subtitle: "Minimum time before the same instruction repeats.",
                valueText: "\(settings.assistantReminderSeconds)s",
                enabled: settings.enabled && assistantOn,
                decEnabled: Int(settings.assistantReminderSeconds) > VoiceReminderBounds.min,
                incEnabled: Int(settings.assistantReminderSeconds) < VoiceReminderBounds.max,
                onDecrement: { changeReminder(-1) },
                onIncrement: { changeReminder(+1) })

            Spacer().frame(height: 16)
            Text("SPEECH RATE")
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .fontWeight(.semibold)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 8)
            SegmentedSelector(
                labels: allVoiceSpeechRates.map { $0.name.lowercased().capitalized },
                selectedIndex: allVoiceSpeechRates.firstIndex(of: settings.speechRate) ?? 1,
                enabled: settings.enabled
            ) { onChange(settings.with(speechRate: allVoiceSpeechRates[$0])) }
        }
    }

    private func applySources(assistant: Bool, telemetry: Bool) {
        // both off ⇒ master off (no "enabled but silent" mode)
        if !assistant && !telemetry {
            onChange(settings.with(enabled: false))
        } else if assistant && telemetry {
            onChange(settings.with(mode: .both))
        } else if assistant {
            onChange(settings.with(mode: .assistantOnly))
        } else {
            onChange(settings.with(mode: .telemetryOnly))
        }
    }

    private func changeReminder(_ delta: Int) {
        // interop: VoiceCooldowns object → .shared; clampReminderSeconds keeps 1–15 s.
        let next = VoiceCooldowns.shared.clampReminderSeconds(
            seconds: settings.assistantReminderSeconds + Int32(delta))
        onChange(settings.with(assistantReminderSeconds: next))
    }
}

/// Field-override copies of VoiceSettings (constructed fresh — Kotlin copy exports as doCopy(ALL)).
private extension VoiceSettings {
    func with(
        enabled: Bool? = nil,
        mode: VoiceMode? = nil,
        speechRate: VoiceSpeechRate? = nil,
        assistantReminderSeconds: Int32? = nil
    ) -> VoiceSettings {
        VoiceSettings(
            enabled: enabled ?? self.enabled,
            mode: mode ?? self.mode,
            speechRate: speechRate ?? self.speechRate,
            assistantReminderSeconds: assistantReminderSeconds ?? self.assistantReminderSeconds)
    }
}

// MARK: - Alerts

/// Entry point into the Alert Control Center — the same screen reached from the Alerts tab,
/// surfaced here so alert configuration is discoverable from Settings too. Pure navigation.
private struct AlertsCard: View {
    let onOpenAlerts: () -> Void

    var body: some View {
        PremiumCard {
            CardHeader(systemIcon: "bell.badge", title: "Alerts")
            Spacer().frame(height: 4)
            Text("Choose which camera warnings fire, set their severity, and tune how they beep and " +
                 "vibrate.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 16)
            PrimaryButton(label: "Configure Alerts", systemIcon: "slider.horizontal.3",
                          onClick: onOpenAlerts)
        }
    }
}

// MARK: - Exposure · Zebra

/// Zebra highlight-warning threshold. Reads/writes the shared `ExposureConfig` directly — the
/// same singleton the Vision worker's ExposureModule samples each frame.
private struct ZebraCard: View {
    @EnvironmentObject private var services: AppServices
    @State private var customText = ""

    var body: some View {
        let exposure = services.exposureConfig
        // interop: sealed interface ZebraSpec — the Level case exports as ZebraSpecLevel.
        let activeLevel = (exposure.zebraSpec as? ZebraSpecLevel).map { Int($0.level) }
        let presets = exposure.presetLevels

        PremiumCard {
            CardHeader(systemIcon: "circle.lefthalf.filled", title: "Zebra")
            Spacer().frame(height: 4)
            Text("Warns when highlights pass this brightness (Sony 16–235 studio-swing IRE). Also drives " +
                 "the Highlight exposure alert.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)

            Spacer().frame(height: 16)
            Text("THRESHOLD")
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .fontWeight(.semibold)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 8)
            SegmentedSelector(
                labels: presets.map { "\($0)" },
                selectedIndex: activeLevel.flatMap { presets.firstIndex(of: $0) } ?? -1
            ) { exposure.setZebraLevel(presets[$0]) }

            Spacer().frame(height: 12)
            // Custom IRE level (0–109; Sony allows super-white above 100).
            TextField("Custom IRE (0–109)", text: $customText)
                .keyboardType(.numberPad)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textPrimary)
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
                .background(DyrectoColor.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.stepper, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: DyrectoRadius.stepper, style: .continuous)
                        .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
                .onChange(of: customText) { text in
                    let filtered = String(text.filter(\.isNumber).prefix(3))
                    if filtered != text { customText = filtered }
                    if let value = Int(filtered) { exposure.setZebraLevel(value) }
                }

            Spacer().frame(height: 12)
            ValueRow(label: "Active threshold", value: "\(exposure.zebraSpec.label) IRE")
        }
    }
}

// MARK: - Exposure · Picture Profile

/// Picture Profile — tells the analysis pipeline what gamma/color the footage uses so the
/// exposure tools read it correctly. Reads/writes the same shared
/// `ExposureConfig.analysisColorSpace` the ExposureModule resolves each frame.
private struct PictureProfileCard: View {
    @EnvironmentObject private var services: AppServices

    var body: some View {
        let exposure = services.exposureConfig

        PremiumCard {
            CardHeader(systemIcon: "paintpalette", title: "Picture Profile")
            Spacer().frame(height: 4)
            Text("Tell the assistant what color profile your footage uses so exposure tools read it " +
                 "correctly. Auto currently assumes Rec.709.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)

            Spacer().frame(height: 16)
            SegmentedSelector(
                labels: allAnalysisColorSpaces.map(colorSpaceDisplayLabel),
                selectedIndex: allAnalysisColorSpaces.firstIndex(of: exposure.analysisColorSpace) ?? -1
            ) { exposure.setAnalysisColorSpace(allAnalysisColorSpaces[$0]) }
        }
    }
}

/// "S_LOG3" → "S-Log3" — user-friendly labels for the color-space segments.
func colorSpaceDisplayLabel(_ space: AnalysisColorSpace) -> String {
    switch space {
    case .auto: return "Auto"
    case .rec709: return "Rec.709"
    case .sLog3: return "S-Log3"
    default: return space.name
    }
}

// MARK: - Advanced

private struct AdvancedSection: View {
    let onOpenDeveloper: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("ADVANCED")
                .font(DyrectoType.labelMedium)
                .kerning(DyrectoType.labelMediumKerning)
                .fontWeight(.semibold)
                .foregroundColor(DyrectoColor.textMuted)
                .padding(.leading, 4)
            NavListRow(
                title: "Developer Mode",
                summary: "Diagnostics, protocol logs, raw telemetry, Live View tools",
                leading: AnyView(
                    ZStack {
                        RoundedRectangle(cornerRadius: DyrectoRadius.chipIcon, style: .continuous)
                            .fill(DyrectoColor.accent.opacity(0.14))
                            .frame(width: 36, height: 36)
                        Image(systemName: "chevron.left.forwardslash.chevron.right")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(DyrectoColor.accent)
                    }),
                onClick: onOpenDeveloper)
        }
    }
}

// MARK: - About

private struct AboutSection: View {
    var body: some View {
        VStack(spacing: 2) {
            Text("Dyrecto")
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
            Text("Wireless monitor & assistant for your camera.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 4)
    }
}
