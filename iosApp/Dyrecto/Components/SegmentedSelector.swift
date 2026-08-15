import SwiftUI

/// The Android `SegmentedSelector`: equal-width tappable segments, the selected one filled with a
/// faint accent tint + accent border. Index-based so it drives enums, presets, and rates alike.
/// `selectedIndex` of -1 selects nothing (e.g. Zebra on a custom IRE value).
struct SegmentedSelector: View {
    let labels: [String]
    let selectedIndex: Int
    var enabled: Bool = true
    let onSelect: (Int) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ForEach(labels.indices, id: \.self) { i in
                Segment(
                    label: labels[i],
                    selected: i == selectedIndex,
                    enabled: enabled,
                    onClick: { onSelect(i) })
            }
        }
    }
}

private struct Segment: View {
    let label: String
    let selected: Bool
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        let alpha = enabled ? 1.0 : 0.45
        Button(action: onClick) {
            Text(label)
                .font(DyrectoType.titleSmall)
                .foregroundColor((selected ? DyrectoColor.accent : DyrectoColor.textPrimary).opacity(alpha))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .padding(.horizontal, 8)
                .background(selected ? DyrectoColor.accent.opacity(0.12) : DyrectoColor.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                        .strokeBorder(
                            selected ? DyrectoColor.accent.opacity(0.55) : Color.clear,
                            lineWidth: 1))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

/// The two-option pill segmented control from the Alerts screen (Active / History) — selected
/// segment fills with the accent→secondary gradient.
struct PillSegmentedControl: View {
    let labels: [String]
    let selectedIndex: Int
    let onSelect: (Int) -> Void

    var body: some View {
        HStack(spacing: 4) {
            ForEach(labels.indices, id: \.self) { i in
                let selected = i == selectedIndex
                Button(action: { onSelect(i) }) {
                    Text(labels[i])
                        .font(DyrectoType.labelLarge)
                        .fontWeight(.semibold)
                        .foregroundColor(selected ? DyrectoColor.onPrimary : DyrectoColor.textMuted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 9)
                        .background(
                            Group {
                                if selected {
                                    LinearGradient(
                                        colors: [DyrectoColor.accent, DyrectoColor.secondary],
                                        startPoint: .leading, endPoint: .trailing)
                                } else {
                                    Color.clear
                                }
                            })
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(4)
        .background(DyrectoColor.surfaceCard)
        .clipShape(Capsule())
        .overlay(Capsule().strokeBorder(DyrectoColor.hairline, lineWidth: 1))
    }
}
