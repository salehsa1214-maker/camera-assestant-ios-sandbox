import SwiftUI

/// The Android `CardHeader`: a 36pt rounded icon badge on a faint accent tint, the card title
/// in titleLarge, and an optional trailing action.
struct CardHeader<Trailing: View>: View {
    let systemIcon: String
    let title: String
    @ViewBuilder var trailing: () -> Trailing

    var body: some View {
        HStack(spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: DyrectoRadius.chipIcon, style: .continuous)
                    .fill(DyrectoColor.accent.opacity(0.14))
                    .frame(width: 36, height: 36)
                Image(systemName: systemIcon)
                    .font(.system(size: 17, weight: .medium))
                    .foregroundColor(DyrectoColor.accent)
            }
            Text(title)
                .font(DyrectoType.titleLarge)
                .foregroundColor(DyrectoColor.textPrimary)
                .padding(.leading, 12)
            Spacer()
            trailing()
        }
    }
}

extension CardHeader where Trailing == EmptyView {
    init(systemIcon: String, title: String) {
        self.init(systemIcon: systemIcon, title: title) { EmptyView() }
    }
}

/// The large screen-owned header used by the top-level tabs (title + subtitle), mirroring
/// `DashboardHeader`/`StoryboardHeader`/`AlertsHeader`/`SettingsHeader`.
struct ScreenHeader<Trailing: View>: View {
    let title: String
    let subtitle: String
    @ViewBuilder var trailing: () -> Trailing

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(DyrectoType.headlineMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text(subtitle)
                    .font(DyrectoType.bodyMedium)
                    .foregroundColor(DyrectoColor.textMuted)
            }
            Spacer()
            trailing()
        }
    }
}

extension ScreenHeader where Trailing == EmptyView {
    init(title: String, subtitle: String) {
        self.init(title: title, subtitle: subtitle) { EmptyView() }
    }
}

/// The circular 46pt bordered icon button used in the screen headers (help ⓘ, alerts bell, back).
struct HeaderIconButton: View {
    let systemIcon: String
    var showsBadge: Bool = false
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            ZStack(alignment: .topTrailing) {
                Circle()
                    .fill(DyrectoColor.surfaceCard)
                    .frame(width: 46, height: 46)
                    .overlay(Circle().strokeBorder(DyrectoColor.hairline, lineWidth: 1))
                Image(systemName: systemIcon)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(DyrectoColor.textPrimary)
                    .frame(width: 46, height: 46)
                if showsBadge {
                    Circle()
                        .fill(DyrectoColor.accent)
                        .frame(width: 8, height: 8)
                        .padding(11)
                }
            }
        }
        .buttonStyle(.plain)
    }
}

/// The quiet uppercase section eyebrow used between cards ("ADVANCED", "SPEECH RATE", …).
struct SectionEyebrow: View {
    let text: String
    var color: Color = DyrectoColor.textMuted

    var body: some View {
        Text(text.uppercased())
            .font(DyrectoType.labelMedium)
            .kerning(DyrectoType.labelMediumKerning)
            .fontWeight(.semibold)
            .foregroundColor(color)
    }
}
