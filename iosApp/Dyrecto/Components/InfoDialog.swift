import SwiftUI

/// A titled informational popup (the Android `AlertDialog` used for the Completion-Rules ⓘ
/// popups and the Storyboard help dialog). Presented as a native iOS alert.
struct InfoDialogContent: Identifiable, Equatable {
    let id: String
    let title: String
    let body: String
    let confirmLabel: String

    init(title: String, body: String, confirmLabel: String = "OK") {
        self.id = title
        self.title = title
        self.body = body
        self.confirmLabel = confirmLabel
    }
}

extension View {
    /// Attach once per screen; set the binding to present a dialog, it clears itself on dismiss.
    func infoDialog(_ item: Binding<InfoDialogContent?>) -> some View {
        alert(
            item.wrappedValue?.title ?? "",
            isPresented: Binding(
                get: { item.wrappedValue != nil },
                set: { if !$0 { item.wrappedValue = nil } }),
            presenting: item.wrappedValue
        ) { content in
            Button(content.confirmLabel, role: .cancel) { item.wrappedValue = nil }
        } message: { content in
            Text(content.body)
        }
    }
}

/// The small tappable ⓘ next to a setting label (Android's 20pt Info icon button).
struct InfoIconButton: View {
    let accessibilityLabel: String
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Image(systemName: "info.circle")
                .font(.system(size: 14, weight: .regular))
                .foregroundColor(DyrectoColor.textMuted)
                .frame(width: 20, height: 20)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(accessibilityLabel)
    }
}
