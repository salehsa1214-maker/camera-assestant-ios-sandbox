import SwiftUI
import DyrectoShared

/// App entry — the iOS counterpart of `DyrectoApp.kt`. Android's launch order is: install
/// BouncyCastle (n/a on iOS — the SSH stack is native), attach the log store, init the
/// process-scoped `MonitoringSessionProvider`, then create the monitoring notification channel.
/// Here: build the store/session graph first (DefaultAppWiring), then request the cosmetic
/// notification permission — which NEVER gates monitoring or alerts (Android POST_NOTIFICATIONS
/// parity).
@main
struct DyrectoApp: App {
    @StateObject private var services: AppServices

    init() {
        let built = DefaultAppWiring.build()
        _services = StateObject(wrappedValue: built)
        logInfo("Dyrecto launched — session graph initialized")
        // Cosmetic-only: a denied permission must not block connecting, monitoring, or alerts.
        built.notifications.requestPermission()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(services)
                .preferredColorScheme(.dark) // dark-first premium idiom (Android DarkColors)
                .background(DyrectoColor.surfaceBase)
        }
    }
}
