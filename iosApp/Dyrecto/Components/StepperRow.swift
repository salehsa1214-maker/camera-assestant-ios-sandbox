import SwiftUI

/// The Completion-Rules stepper (Android `RuleStepper`): label + ⓘ above a raised −/value/+ strip.
struct RuleStepper: View {
    let label: String
    let valueText: String
    let onInfo: () -> Void
    let onDecrement: () -> Void
    let onIncrement: () -> Void
    let decEnabled: Bool
    let incEnabled: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 4) {
                Text(label)
                    .font(DyrectoType.bodyMedium)
                    .fontWeight(.medium)
                    .foregroundColor(DyrectoColor.textPrimary)
                InfoIconButton(accessibilityLabel: "\(label) info", onClick: onInfo)
            }
            HStack(spacing: 0) {
                StepIconButton(systemIcon: "minus", enabled: decEnabled, onClick: onDecrement)
                Text(valueText)
                    .font(DyrectoType.titleSmall)
                    .foregroundColor(DyrectoColor.textPrimary)
                    .frame(maxWidth: .infinity)
                StepIconButton(systemIcon: "plus", enabled: incEnabled, onClick: onIncrement)
            }
            .frame(height: 46)
            .background(DyrectoColor.surfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.stepper, style: .continuous))
        }
    }
}

private struct StepIconButton: View {
    let systemIcon: String
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Image(systemName: systemIcon)
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(DyrectoColor.textPrimary.opacity(enabled ? 1 : 0.3))
                .frame(width: 46, height: 46)
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

/// The Settings-style inline stepper (Android `AssistantReminderRow`'s −/value/+ trio): a label +
/// subtitle on the left, accent-tinted square buttons on the right.
struct InlineStepperRow: View {
    let title: String
    let subtitle: String
    let valueText: String
    let enabled: Bool
    let decEnabled: Bool
    let incEnabled: Bool
    let onDecrement: () -> Void
    let onIncrement: () -> Void

    var body: some View {
        let alpha = enabled ? 1.0 : 0.4
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(DyrectoType.bodyLarge)
                    .fontWeight(.medium)
                    .foregroundColor(DyrectoColor.textPrimary.opacity(alpha))
                Text(subtitle)
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted.opacity(alpha))
            }
            Spacer()
            HStack(spacing: 6) {
                StepperSquareButton(label: "−", enabled: enabled && decEnabled, onClick: onDecrement)
                Text(valueText)
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary.opacity(alpha))
                    .padding(.horizontal, 4)
                StepperSquareButton(label: "+", enabled: enabled && incEnabled, onClick: onIncrement)
            }
        }
        .padding(.vertical, 6)
    }
}

/// The 40pt accent-tinted square stepper button (Android `StepperButton` in SettingsScreen).
struct StepperSquareButton: View {
    let label: String
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Text(label)
                .font(DyrectoType.titleMedium)
                .fontWeight(.bold)
                .foregroundColor(enabled ? DyrectoColor.accent : DyrectoColor.textMuted.opacity(0.5))
                .frame(width: 40, height: 40)
                .background(enabled ? DyrectoColor.accent.opacity(0.14) : DyrectoColor.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.stepper, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

/// A label (+ optional subtitle) on the left, a toggle on the right — Android `ToggleRow`.
struct ToggleRow: View {
    let label: String
    var subtitle: String? = nil
    let checked: Bool
    var enabled: Bool = true
    let onChange: (Bool) -> Void

    var body: some View {
        let alpha = enabled ? 1.0 : 0.4
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(DyrectoType.bodyLarge)
                    .fontWeight(.medium)
                    .foregroundColor(DyrectoColor.textPrimary.opacity(alpha))
                if let subtitle {
                    Text(subtitle)
                        .font(DyrectoType.bodySmall)
                        .foregroundColor(DyrectoColor.textMuted.opacity(alpha))
                }
            }
            .padding(.trailing, 12)
            Spacer()
            Toggle("", isOn: Binding(get: { checked }, set: onChange))
                .labelsHidden()
                .tint(DyrectoColor.accent)
                .disabled(!enabled)
        }
        .padding(.vertical, 6)
    }
}
