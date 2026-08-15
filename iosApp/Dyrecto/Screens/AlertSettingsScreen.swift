import SwiftUI
import DyrectoShared

/// User-facing Alert Control Center (port of AlertControlCenterScreen.kt). The screen is
/// generated from the alert catalog — it iterates the alert categories and, within each, every
/// AlertType in that category — so a newly added alert type appears automatically.
///
/// Each row lets the user enable/disable the alert, override severity, choose 0–10 beeps /
/// pulses, preview with Test, and restore that alert's defaults. A top button restores all
/// defaults at once. Changes auto-save; the Test button is disabled while a pattern is playing
/// (`IosAlertFeedback.playing`) so rapid taps can't stack sounds/vibrations.
struct AlertSettingsScreen: View {
    @ObservedObject var session: MonitoringSessionIos

    @EnvironmentObject private var services: AppServices
    @Environment(\.dismiss) private var dismiss
    /// Which alert types are expanded to reveal their controls (progressive disclosure).
    @State private var expanded: Set<String> = []

    var body: some View {
        // interop: SwiftEnums.shared.alertTypes() enumerates AlertType (entries doesn't export).
        let allTypes = SwiftEnums.shared.alertTypes()
        let categories = allAlertCategories
            .map { category in (category, allTypes.filter { $0.category == category }) }
            .filter { !$0.1.isEmpty }

        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Header(
                    onBack: { dismiss() },
                    onRestoreAll: { session.restoreAllAlertDefaults() })

                ForEach(categories, id: \.0.name) { category, types in
                    Text(category.title.uppercased())
                        .font(DyrectoType.labelMedium)
                        .kerning(DyrectoType.labelMediumKerning)
                        .fontWeight(.semibold)
                        .foregroundColor(DyrectoColor.textMuted)
                        .padding(.leading, 4)
                        .padding(.top, 12)

                    ForEach(types, id: \.name) { type in
                        // interop: AlertConfig.companion.default(type:) — same fallback the
                        // AlertConfigStoreIos reader uses.
                        let config = services.alertConfigStore.configs[type]
                            ?? AlertConfig.companion.default(type: type)
                        AlertConfigCard(
                            config: config,
                            category: category,
                            expanded: expanded.contains(type.name),
                            testPlaying: services.feedbackPlaying.playing,
                            onToggleExpanded: {
                                if expanded.contains(type.name) {
                                    expanded.remove(type.name)
                                } else {
                                    expanded.insert(type.name)
                                }
                            },
                            onUpdate: { session.updateAlertConfig($0) },
                            onTest: { session.testAlert($0) },
                            onRestoreDefault: { session.restoreAlertDefault($0) })
                    }
                }
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.top, 12)
            .padding(.bottom, 28)
        }
        .background(DyrectoColor.surfaceBase)
        .toolbar(.hidden, for: .navigationBar) // owns its own large header (Android parity)
    }
}

// MARK: - Header

private struct Header: View {
    let onBack: () -> Void
    let onRestoreAll: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HeaderIconButton(systemIcon: "arrow.left", onClick: onBack)
                .accessibilityLabel("Back")
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Configure Alerts")
                        .font(DyrectoType.headlineMedium)
                        .foregroundColor(DyrectoColor.textPrimary)
                    Text("Customize how each alert notifies you.")
                        .font(DyrectoType.bodyMedium)
                        .foregroundColor(DyrectoColor.textMuted)
                }
                Spacer()
                PillButton(text: "Restore All", onClick: onRestoreAll)
            }
        }
    }
}

// MARK: - One alert (collapsed summary → expanded controls)

private struct AlertConfigCard: View {
    let config: AlertConfig
    let category: AlertCategory
    let expanded: Bool
    let testPlaying: Bool
    let onToggleExpanded: () -> Void
    let onUpdate: (AlertConfig) -> Void
    let onTest: (AlertConfig) -> Void
    let onRestoreDefault: (AlertType) -> Void

    var body: some View {
        let enabled = config.enabled
        let level: StatusLevel = enabled ? statusLevel(for: config.severity) : .idle
        let accent = level.color

        VStack(alignment: .leading, spacing: 0) {
            // Header row: icon · title/summary · enable switch · expand chevron.
            HStack(spacing: 0) {
                ZStack {
                    Circle().fill(accent.opacity(0.14)).frame(width: 40, height: 40)
                    Image(systemName: categoryIcon(category))
                        .font(.system(size: 17))
                        .foregroundColor(accent)
                }
                Button(action: onToggleExpanded) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(config.alertType.title)
                            .font(DyrectoType.titleMedium)
                            .foregroundColor(DyrectoColor.textPrimary)
                            .lineLimit(1)
                        Text(summaryLine(config))
                            .font(DyrectoType.bodySmall)
                            .foregroundColor(enabled ? accent : DyrectoColor.textMuted)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 14)

                Toggle("", isOn: Binding(
                    get: { enabled },
                    set: { onUpdate(config.with(enabled: $0)) }))
                    .labelsHidden()
                    .tint(DyrectoColor.accent)
                Button(action: onToggleExpanded) {
                    Image(systemName: "chevron.down")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(DyrectoColor.textMuted)
                        .rotationEffect(.degrees(expanded ? 180 : 0))
                }
                .buttonStyle(.plain)
                .padding(.leading, 8)
                .accessibilityLabel(expanded ? "Collapse" : "Expand")
            }

            if expanded {
                if enabled {
                    EnabledControls(
                        config: config,
                        testPlaying: testPlaying,
                        onUpdate: onUpdate,
                        onTest: onTest,
                        onRestoreDefault: onRestoreDefault)
                } else {
                    Text("Turn this alert on to customize its severity, sound, and vibration.")
                        .font(DyrectoType.bodyMedium)
                        .foregroundColor(DyrectoColor.textMuted)
                        .padding(.top, 14)
                }
            }
        }
        .padding(16)
        .background(DyrectoColor.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous)
                .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
        .animation(.easeInOut(duration: 0.2), value: expanded)
    }
}

private struct EnabledControls: View {
    let config: AlertConfig
    let testPlaying: Bool
    let onUpdate: (AlertConfig) -> Void
    let onTest: (AlertConfig) -> Void
    let onRestoreDefault: (AlertType) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Rectangle()
                .fill(DyrectoColor.hairline.opacity(0.6))
                .frame(height: 1)
                .padding(.top, 16)

            Text("Severity")
                .font(DyrectoType.labelLarge)
                .foregroundColor(DyrectoColor.textMuted)
                .padding(.top, 16)
            Spacer().frame(height: 8)
            SeveritySelector(
                selected: config.severity,
                onSelect: { onUpdate(config.with(severity: $0)) })

            PatternSlider(
                label: "Sound",
                count: Int(config.soundPattern.count),
                unitSingular: "Beep", unitPlural: "Beeps", noneLabel: "No sound",
                onCountChange: { onUpdate(config.with(soundCount: $0)) })

            PatternSlider(
                label: "Vibration",
                count: Int(config.vibrationPattern.count),
                unitSingular: "Pulse", unitPlural: "Pulses", noneLabel: "No vibration",
                onCountChange: { onUpdate(config.with(vibrationCount: $0)) })

            Spacer().frame(height: 14)
            HStack {
                TestButton(enabled: !testPlaying, onClick: { onTest(config) })
                Spacer()
                Button("Restore default") { onRestoreDefault(config.alertType) }
                    .font(DyrectoType.labelLarge)
                    .fontWeight(.semibold)
                    .foregroundColor(DyrectoColor.textMuted)
            }
        }
    }
}

// MARK: - Severity selector (tonal segmented pills)

private struct SeveritySelector: View {
    let selected: AlertSeverity
    let onSelect: (AlertSeverity) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ForEach(allAlertSeverities, id: \.name) { level in
                let levelColor = statusLevel(for: level).color
                let isSelected = level == selected
                Button(action: { onSelect(level) }) {
                    Text(level.name)
                        .font(DyrectoType.labelMedium)
                        .fontWeight(.semibold)
                        .foregroundColor(isSelected ? levelColor : DyrectoColor.textMuted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 9)
                        .background(isSelected ? levelColor.opacity(0.16) : DyrectoColor.surfaceElevated)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(
                                isSelected ? levelColor.opacity(0.55) : DyrectoColor.hairline,
                                lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
    }
}

// MARK: - Pattern slider

private struct PatternSlider: View {
    let label: String
    let count: Int
    let unitSingular: String
    let unitPlural: String
    let noneLabel: String
    let onCountChange: (Int) -> Void

    var body: some View {
        let valueLabel: String = {
            switch count {
            case 0: return noneLabel
            case 1: return "1 \(unitSingular)"
            default: return "\(count) \(unitPlural)"
            }
        }()
        VStack(spacing: 0) {
            HStack {
                Text(label)
                    .font(DyrectoType.labelLarge)
                    .foregroundColor(DyrectoColor.textMuted)
                Spacer()
                Text(valueLabel)
                    .font(DyrectoType.labelLarge)
                    .fontWeight(.semibold)
                    .foregroundColor(DyrectoColor.textPrimary)
            }
            .padding(.top, 14)
            Slider(
                value: Binding(
                    get: { Double(count) },
                    set: { onCountChange(Int($0)) }),
                in: Double(AlertPatternBounds.min)...Double(AlertPatternBounds.max),
                step: 1)
                .tint(DyrectoColor.accent)
        }
    }
}

// MARK: - Small building blocks

private struct TestButton: View {
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        let tint = enabled ? DyrectoColor.accent : DyrectoColor.textMuted.opacity(0.4)
        Button(action: onClick) {
            HStack(spacing: 6) {
                Image(systemName: "play.fill")
                    .font(.system(size: 14))
                Text("Test")
                    .font(DyrectoType.labelLarge)
                    .fontWeight(.semibold)
            }
            .foregroundColor(tint)
            .padding(.horizontal, 18)
            .padding(.vertical, 9)
            .overlay(Capsule().strokeBorder(tint.opacity(0.5), lineWidth: 1))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

// MARK: - Presentation-only helpers

/// A one-line summary of the alert's current delivery config, shown while the row is collapsed.
private func summaryLine(_ config: AlertConfig) -> String {
    if !config.enabled { return "Off" }
    let severity = config.severity.name.lowercased().capitalized
    let s = Int(config.soundPattern.count)
    let v = Int(config.vibrationPattern.count)
    if s == 0 && v == 0 { return "\(severity) · Silent" }
    let sound = s == 0 ? "No sound" : "\(s) beep\(s == 1 ? "" : "s")"
    let vib = v == 0 ? "No pulse" : "\(v) pulse\(v == 1 ? "" : "s")"
    return "\(severity) · \(sound) · \(vib)"
}

/// Field-override copies of AlertConfig (Kotlin data-class copy exports as doCopy(ALL params) —
/// constructing fresh instances is equivalent and clearer from Swift).
private extension AlertConfig {
    func with(
        enabled: Bool? = nil,
        severity: AlertSeverity? = nil,
        soundCount: Int? = nil,
        vibrationCount: Int? = nil
    ) -> AlertConfig {
        // interop: AlertPattern.of via the SwiftEnums bridge (clamps into range).
        AlertConfig(
            alertType: alertType,
            enabled: enabled ?? self.enabled,
            severity: severity ?? self.severity,
            soundPattern: soundCount.map { SwiftEnums.shared.alertPattern(count: Int32($0)) }
                ?? soundPattern,
            vibrationPattern: vibrationCount.map { SwiftEnums.shared.alertPattern(count: Int32($0)) }
                ?? vibrationPattern)
    }
}
