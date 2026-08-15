import SwiftUI

/// A rounded filled progress track. `ThinBar` (4pt) is the subtle indicator used on the Dashboard
/// guidance card and stat tiles; `BigBar` (12pt) is the headline storyboard progress.
struct ProgressBar: View {
    let fraction: Float
    var color: Color = DyrectoColor.accent
    var height: CGFloat = 4

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                Capsule()
                    .fill(DyrectoColor.textPrimary.opacity(0.10))
                Capsule()
                    .fill(color)
                    .frame(width: geo.size.width * CGFloat(min(max(fraction, 0), 1)))
            }
        }
        .frame(height: height)
    }
}

/// The 4pt track (Android `ThinBar`).
func ThinBar(_ fraction: Float, color: Color = DyrectoColor.accent) -> some View {
    ProgressBar(fraction: fraction, color: color, height: 4)
}

/// The 12pt headline track (Android `BigBar`).
func BigBar(_ fraction: Float, color: Color = DyrectoColor.accent) -> some View {
    ProgressBar(fraction: fraction, color: color, height: 12)
}

/// A hairline divider between grouped rows inside a card (Android `Divider()` helpers).
struct HairlineDivider: View {
    var alpha: Double = 1.0
    var body: some View {
        Rectangle()
            .fill(DyrectoColor.hairline.opacity(alpha))
            .frame(height: 1)
            .padding(.vertical, 4)
    }
}

/// A key/value row; `mono` for technical values (hex, ids) — Android `InfoRow` in UiKit.kt.
struct InfoRow: View {
    let label: String
    let value: String?
    var mono: Bool = false

    var body: some View {
        HStack(alignment: .top) {
            Text(label)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textPrimary.opacity(0.7))
            Spacer()
            Text((value?.isEmpty ?? true) ? "—" : value!)
                .font(mono ? DyrectoType.mono : DyrectoType.bodyMedium)
                .fontWeight(.medium)
                .foregroundColor(DyrectoColor.textPrimary)
                .multilineTextAlignment(.trailing)
                .padding(.leading, 16)
        }
    }
}

/// A read-only label/value line with an emphasized value (Android `ValueRow` in SettingsScreen).
struct ValueRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer()
            Text(value)
                .font(DyrectoType.titleSmall)
                .foregroundColor(DyrectoColor.textPrimary)
        }
    }
}
