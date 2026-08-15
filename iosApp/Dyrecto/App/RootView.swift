import SwiftUI
import DyrectoShared

/// The four top-level tabs (Android `TABS` in MainActivity.kt): Dashboard, Storyboard, Alerts,
/// Settings. Each tab hosts its own NavigationStack (native iOS equivalent of the per-route
/// back stack); Live View, Camera Details, the Alert Control Center and Developer are pushed
/// destinations. Developer is reachable from Settings, per the Android layout.
enum AppTab: Hashable {
    case dashboard, storyboard, alerts, settings
}

/// Pushed destinations shared across tabs (mirror of the non-top-level Android `Destination`s).
enum AppDestination: Hashable {
    case liveView
    case cameraDetails
    case cameraPicker
    case alertSettings
    case developer
}

struct RootView: View {
    @EnvironmentObject private var services: AppServices
    @State private var tab: AppTab = .dashboard

    init() {
        // Style the tab bar toward the Android floating glass island: translucent dark surface.
        let appearance = UITabBarAppearance()
        appearance.configureWithTransparentBackground()
        appearance.backgroundColor = UIColor(DyrectoColor.surfaceCard).withAlphaComponent(0.82)
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        TabView(selection: $tab) {
            DashboardTab()
                .tabItem { Label("Dashboard", systemImage: "square.grid.2x2") }
                .tag(AppTab.dashboard)

            StoryboardTab()
                .tabItem { Label("Storyboard", systemImage: "camera.metering.center.weighted") }
                .tag(AppTab.storyboard)

            AlertsTab()
                .tabItem { Label("Alerts", systemImage: "bell") }
                .tag(AppTab.alerts)

            SettingsTab()
                .tabItem { Label("Settings", systemImage: "slider.horizontal.3") }
                .tag(AppTab.settings)
        }
        .tint(DyrectoColor.accent)
        .environment(\.switchTab, SwitchTabAction { tab = $0 })
    }
}

// MARK: - Cross-tab navigation (Android `nav.switchTab`)

struct SwitchTabAction {
    let run: (AppTab) -> Void
    func callAsFunction(_ tab: AppTab) { run(tab) }
}

private struct SwitchTabKey: EnvironmentKey {
    static let defaultValue = SwitchTabAction { _ in }
}

extension EnvironmentValues {
    var switchTab: SwitchTabAction {
        get { self[SwitchTabKey.self] }
        set { self[SwitchTabKey.self] = newValue }
    }
}

// MARK: - Tabs

private struct DashboardTab: View {
    @EnvironmentObject private var services: AppServices
    @State private var path: [AppDestination] = []

    var body: some View {
        NavigationStack(path: $path) {
            CameraScreen(
                session: services.session,
                onOpenLiveView: { path.append(.liveView) },
                onOpenDetails: { path.append(.cameraDetails) },
                onScanCameras: { path.append(.cameraPicker) })
                .navigationDestination(for: AppDestination.self) { dest in
                    destinationView(dest)
                }
        }
    }

    @ViewBuilder
    private func destinationView(_ dest: AppDestination) -> some View {
        switch dest {
        case .liveView:
            LiveViewScreen(session: services.session)
        case .cameraDetails:
            CameraDetailsView(session: services.session)
        case .cameraPicker:
            DiscoveryScreen(session: services.session, connection: services.connection)
        case .alertSettings:
            AlertSettingsScreen(session: services.session)
        case .developer:
            DeveloperScreen(session: services.session)
        }
    }
}

private struct StoryboardTab: View {
    @EnvironmentObject private var services: AppServices

    var body: some View {
        NavigationStack {
            StoryboardScreen(session: services.session)
        }
    }
}

private struct AlertsTab: View {
    @EnvironmentObject private var services: AppServices
    @State private var path: [AppDestination] = []

    var body: some View {
        NavigationStack(path: $path) {
            AlertsScreen(
                session: services.session,
                onOpenControlCenter: { path.append(.alertSettings) })
                .navigationDestination(for: AppDestination.self) { dest in
                    if case .alertSettings = dest {
                        AlertSettingsScreen(session: services.session)
                    }
                }
        }
    }
}

private struct SettingsTab: View {
    @EnvironmentObject private var services: AppServices
    @State private var path: [AppDestination] = []

    var body: some View {
        NavigationStack(path: $path) {
            SettingsScreen(
                session: services.session,
                onOpenAlerts: { path.append(.alertSettings) },
                onOpenDeveloper: { path.append(.developer) })
                .navigationDestination(for: AppDestination.self) { dest in
                    switch dest {
                    case .alertSettings: AlertSettingsScreen(session: services.session)
                    case .developer: DeveloperScreen(session: services.session)
                    default: EmptyView()
                    }
                }
        }
    }
}

// MARK: - Camera Details (grouped telemetry drill-in reached from the Dashboard hero card)

/// Camera Details — the full telemetry dashboard behind the Dashboard's "Details" button
/// (Android `CameraDetailsScreen`/`CameraDetailSections`: device identity + grouped decoded
/// telemetry rows).
struct CameraDetailsView: View {
    @ObservedObject var session: MonitoringSessionIos

    var body: some View {
        let state = session.state
        let t = state.telemetry
        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.section) {
                PremiumCard {
                    CardHeader(systemIcon: "camera", title: "Device")
                    Spacer().frame(height: 12)
                    InfoRow(label: "Manufacturer", value: state.deviceInfo?.manufacturer)
                    InfoRow(label: "Model", value: state.deviceInfo?.model)
                    InfoRow(label: "Firmware", value: state.deviceInfo?.firmwareVersion)
                    InfoRow(label: "Serial", value: state.deviceInfo?.serialNumber, mono: true)
                }
                telemetryGroup("Exposure", codes: [
                    ("ISO", TelemetryCode.iso), ("Shutter", TelemetryCode.shutter),
                    ("Aperture", TelemetryCode.fNumber), ("White Balance", TelemetryCode.whiteBalance),
                    ("Color Temp", TelemetryCode.colorTemp),
                ], telemetry: t)
                telemetryGroup("Recording", codes: [
                    ("State", TelemetryCode.movieRec), ("Time", TelemetryCode.recTime),
                    ("Resolution", TelemetryCode.recResolution), ("Frame Rate", TelemetryCode.recFps),
                ], telemetry: t)
                telemetryGroup("Media", codes: [
                    ("Slot 1", TelemetryCode.slot1Status), ("Slot 1 Remaining", TelemetryCode.slot1Remain),
                    ("Slot 2", TelemetryCode.slot2Status), ("Slot 2 Remaining", TelemetryCode.slot2Remain),
                ], telemetry: t)
                telemetryGroup("Power & Thermal", codes: [
                    ("Battery", TelemetryCode.battery), ("Overheating", TelemetryCode.overheating),
                ], telemetry: t)
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.vertical, DyrectoSpacing.screen)
        }
        .background(DyrectoColor.surfaceBase)
        .navigationTitle("Camera Details")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func telemetryGroup(
        _ title: String, codes: [(String, Int32)], telemetry: CameraTelemetry?
    ) -> some View {
        PremiumCard {
            CardHeader(systemIcon: "list.bullet.rectangle", title: title)
            Spacer().frame(height: 12)
            VStack(spacing: 8) {
                ForEach(codes, id: \.1) { label, code in
                    InfoRow(label: label, value: telemetryDisplay(telemetry, code))
                }
            }
        }
    }
}
