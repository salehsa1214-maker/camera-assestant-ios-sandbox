import SwiftUI
import DyrectoShared

/// The shared status colour vocabulary (Android `StatusLevel`).
enum StatusLevel {
    case good, warn, bad, idle

    var color: Color {
        switch self {
        case .good: return DyrectoColor.statusGood
        case .warn: return DyrectoColor.statusWarn
        case .bad: return DyrectoColor.statusBad
        case .idle: return DyrectoColor.statusIdle
        }
    }
}

/// Maps an alert severity to the shared StatusLevel colour vocabulary (Android `statusLevel()`).
// interop: Kotlin enum entries export as lowercase static members (AlertSeverity.info, …).
func statusLevel(for severity: AlertSeverity) -> StatusLevel {
    switch severity {
    case .info: return .good
    case .warning: return .warn
    case .critical: return .bad
    default: return .idle
    }
}

/// A compact tonal status chip — dot + label, on a faint tint of the level colour
/// (Android `StatusChip` in DesignSystem.kt).
struct StatusChip: View {
    let text: String
    let level: StatusLevel

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(level.color)
                .frame(width: 7, height: 7)
            Text(text)
                .font(DyrectoType.labelMedium)
                .fontWeight(.semibold)
                .foregroundColor(level.color)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(level.color.opacity(0.14))
        .clipShape(Capsule())
    }
}

/// Coloured status dot + short label (Android `StatusPill` in UiKit.kt).
struct StatusPill: View {
    let text: String
    let level: StatusLevel

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(level.color)
                .frame(width: 10, height: 10)
            Text(text)
                .font(DyrectoType.labelLarge)
                .foregroundColor(level.color)
        }
    }
}

/// A small severity badge (uppercase severity name on its tint) — Android `SeverityBadge`.
struct SeverityBadge: View {
    let severity: AlertSeverity

    var body: some View {
        let accent = statusLevel(for: severity).color
        Text(severity.name)
            .font(DyrectoType.labelSmall)
            .kerning(0.5)
            .fontWeight(.bold)
            .foregroundColor(accent)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(accent.opacity(0.16))
            .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
    }
}

/// A compact label/value chip for translucent overlays (Live View glance bar) — Android `GlanceChip`.
struct GlanceChip: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label.uppercased())
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .foregroundColor(.white.opacity(0.65))
            Text(value)
                .font(DyrectoType.titleSmall)
                .foregroundColor(.white)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(Color.black.opacity(0.45))
        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
    }
}

/// Live View overlay badge: red dot + "REC" + optional timecode on a dark scrim — Android `RecBadge`.
struct RecBadge: View {
    var timecode: String? = nil

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(DyrectoColor.statusBad)
                .frame(width: 9, height: 9)
            Text("REC")
                .font(DyrectoType.labelMedium)
                .fontWeight(.bold)
                .foregroundColor(.white)
            if let timecode {
                Text(timecode)
                    .font(DyrectoType.labelMedium)
                    .foregroundColor(.white.opacity(0.85))
                    .padding(.leading, 2)
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(Color.black.opacity(0.45))
        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}
