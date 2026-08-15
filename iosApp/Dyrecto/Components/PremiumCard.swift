import SwiftUI

/// A soft, bordered elevated card — the premium container used across the redesigned screens
/// (mirror of the Android `PremiumCard`: 24pt corners, surface fill, hairline border, 20pt padding).
struct PremiumCard<Content: View>: View {
    var contentPadding: CGFloat = 20
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content()
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(contentPadding)
        .background(DyrectoColor.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.premiumCard, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.premiumCard, style: .continuous)
                .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
    }
}

/// A tappable list row: title (+ optional summary) with a chevron — the Android `NavListRow`.
struct NavListRow: View {
    let title: String
    var summary: String? = nil
    var leading: AnyView? = nil
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 0) {
                if let leading {
                    leading
                    Spacer().frame(width: 14)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(DyrectoType.titleMedium)
                        .foregroundColor(DyrectoColor.textPrimary)
                    if let summary {
                        Text(summary)
                            .font(DyrectoType.bodySmall)
                            .foregroundColor(DyrectoColor.textMuted)
                            .lineLimit(1)
                    }
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(DyrectoColor.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

/// A full-width accent action button (icon + label) matching the app's premium CTAs
/// (Android `PrimaryButton` in SettingsScreen).
struct PrimaryButton: View {
    let label: String
    let systemIcon: String
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 8) {
                Image(systemName: systemIcon)
                    .font(.system(size: 15, weight: .semibold))
                Text(label)
                    .font(DyrectoType.titleSmall)
            }
            .foregroundColor(DyrectoColor.onPrimary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(DyrectoColor.accent)
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

/// A small outlined accent pill — the secondary action vocabulary shared by the Alerts screens
/// (Android `PillButton` / `ConfigureButton`).
struct PillButton: View {
    let text: String
    var systemIcon: String? = nil
    var tint: Color = DyrectoColor.accent
    var enabled: Bool = true
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 6) {
                if let systemIcon {
                    Image(systemName: systemIcon)
                        .font(.system(size: 14, weight: .medium))
                }
                Text(text)
                    .font(DyrectoType.labelLarge)
                    .fontWeight(.semibold)
                    .lineLimit(1)
            }
            .foregroundColor(enabled ? tint : DyrectoColor.textMuted.opacity(0.4))
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .overlay(
                Capsule().strokeBorder(
                    (enabled ? tint : DyrectoColor.textMuted.opacity(0.4)).opacity(0.5),
                    lineWidth: 1))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}
