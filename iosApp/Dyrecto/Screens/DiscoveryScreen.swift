import SwiftUI
import DyrectoShared

/// Select Camera — the first-run "scanning for camera" screen (port of CameraPickerScreen.kt),
/// plus the selected-camera status card from DiscoveryScreen.kt (name / id / RSSI / connection
/// phase / error). Scan starts on appear; picking a camera hands off to the connect flow and
/// pops back to the Dashboard, which then shows the phase progress.
struct DiscoveryScreen: View {
    @ObservedObject var session: MonitoringSessionIos
    @ObservedObject var connection: CameraConnectionController
    @ObservedObject private var ble: Fx3BleManager
    @Environment(\.dismiss) private var dismiss

    init(session: MonitoringSessionIos, connection: CameraConnectionController) {
        self.session = session
        self.connection = connection
        self.ble = connection.ble
    }

    var body: some View {
        let cameras = ble.discovered
        VStack(alignment: .leading, spacing: 0) {
            ScreenHeader(title: "Select Camera", subtitle: "Pick your camera to connect.")
                .padding(.bottom, 8)

            if cameras.isEmpty {
                // Default view: a big, obviously-active searching state that fills the screen so
                // the user can tell the app is looking (not frozen).
                SearchingState(scanning: ble.isScanning, onStopScan: { session.stopScan() })
                    .frame(maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        Spacer().frame(height: 12)
                        ScanStatusCard(
                            scanning: ble.isScanning,
                            deviceCount: cameras.count,
                            onStopScan: { session.stopScan() })

                        VStack(alignment: .leading, spacing: 10) {
                            SectionEyebrow(text: "Cameras", color: DyrectoColor.accent)
                            ForEach(cameras) { device in
                                CameraCard(device: device) {
                                    session.connect(to: device.id)
                                    dismiss()
                                }
                            }
                        }

                        SelectedCameraCard(state: session.state, ble: ble)
                    }
                }
            }
        }
        .padding(.horizontal, DyrectoSpacing.screenHorizontal)
        .padding(.top, 12)
        .padding(.bottom, DyrectoSpacing.bottomInset)
        .background(DyrectoColor.surfaceBase)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { session.startScan() }
    }
}

// MARK: - Searching / empty (default full-screen state)

private struct SearchingState: View {
    let scanning: Bool
    let onStopScan: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            BigRadar(scanning: scanning)
            Spacer().frame(height: 32)
            Text(scanning ? "Searching for cameras…" : "No cameras found yet")
                .font(DyrectoType.titleLarge)
                .foregroundColor(DyrectoColor.textPrimary)
                .multilineTextAlignment(.center)
            Spacer().frame(height: 8)
            Text("Make sure your camera is powered on and in Bluetooth pairing/connection mode.")
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
            if scanning {
                Spacer().frame(height: 28)
                StopPill(onClick: onStopScan)
            }
            Spacer()
        }
        .frame(maxWidth: .infinity)
    }
}

/// A large pulsing radar graphic — expanding rings behind a Bluetooth badge.
private struct BigRadar: View {
    let scanning: Bool
    @State private var progress: CGFloat = 0

    var body: some View {
        ZStack {
            if scanning {
                ForEach(0..<3, id: \.self) { i in
                    let p = (progress + CGFloat(i) / 3).truncatingRemainder(dividingBy: 1)
                    Circle()
                        .fill(DyrectoColor.accent.opacity(Double(1 - p) * 0.30))
                        .frame(width: 180 * (0.35 + p * 0.65), height: 180 * (0.35 + p * 0.65))
                }
            }
            Circle()
                .fill(DyrectoColor.accent.opacity(0.16))
                .frame(width: 96, height: 96)
            Image(systemName: scanning ? "dot.radiowaves.left.and.right" : "magnifyingglass")
                .font(.system(size: 40))
                .foregroundColor(DyrectoColor.accent)
        }
        .frame(width: 180, height: 180)
        .onAppear {
            withAnimation(.linear(duration: 1.8).repeatForever(autoreverses: false)) {
                progress = 1
            }
        }
    }
}

// MARK: - Scan status hero

private struct ScanStatusCard: View {
    let scanning: Bool
    let deviceCount: Int
    let onStopScan: () -> Void

    var body: some View {
        PremiumCard {
            HStack(spacing: 16) {
                RadarBadge(scanning: scanning)
                VStack(alignment: .leading, spacing: 2) {
                    Text(scanning ? "Scanning for cameras" : "Scan complete")
                        .font(DyrectoType.titleMedium)
                        .foregroundColor(DyrectoColor.textPrimary)
                    Text(statusLine)
                        .font(DyrectoType.bodySmall)
                        .foregroundColor(DyrectoColor.textMuted)
                }
                Spacer()
                if scanning {
                    StopPill(onClick: onStopScan)
                }
            }
        }
    }

    private var statusLine: String {
        if scanning && deviceCount == 0 { return "Looking for nearby cameras…" }
        if deviceCount == 0 { return "No cameras found yet" }
        if deviceCount == 1 { return "1 camera nearby" }
        return "\(deviceCount) cameras nearby"
    }
}

/// A circular Bluetooth badge with a soft radar ring that pulses outward while scanning.
private struct RadarBadge: View {
    let scanning: Bool
    @State private var progress: CGFloat = 0

    var body: some View {
        ZStack {
            if scanning {
                ForEach(0..<2, id: \.self) { i in
                    let p = (progress + CGFloat(i) / 2).truncatingRemainder(dividingBy: 1)
                    Circle()
                        .fill(DyrectoColor.accent.opacity(Double(1 - p) * 0.35))
                        .frame(width: 54 * (0.5 + p * 0.5), height: 54 * (0.5 + p * 0.5))
                }
            }
            Circle().fill(DyrectoColor.accent.opacity(0.16)).frame(width: 40, height: 40)
            Image(systemName: "dot.radiowaves.left.and.right")
                .font(.system(size: 17))
                .foregroundColor(DyrectoColor.accent)
        }
        .frame(width: 54, height: 54)
        .onAppear {
            withAnimation(.linear(duration: 1.6).repeatForever(autoreverses: false)) {
                progress = 1
            }
        }
    }
}

private struct StopPill: View {
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Text("Stop")
                .font(DyrectoType.labelLarge)
                .fontWeight(.semibold)
                .foregroundColor(DyrectoColor.error)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(DyrectoColor.error.opacity(0.14))
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Camera cards

/// A recognised camera — a tappable premium card that selects it (name + brand chip + RSSI bars).
private struct CameraCard: View {
    let device: Fx3BleManager.DiscoveredCamera
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack {
                ZStack {
                    RoundedRectangle(cornerRadius: 13, style: .continuous)
                        .fill(DyrectoColor.accent.opacity(0.14))
                        .frame(width: 44, height: 44)
                    Image(systemName: "camera")
                        .font(.system(size: 18))
                        .foregroundColor(DyrectoColor.accent)
                }
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 8) {
                        Text(device.name)
                            .font(DyrectoType.titleMedium)
                            .foregroundColor(DyrectoColor.textPrimary)
                            .lineLimit(1)
                        BrandChip(label: "Sony")
                    }
                    Text(device.id.uuidString)
                        .font(DyrectoType.monoSmall)
                        .foregroundColor(DyrectoColor.textMuted)
                        .lineLimit(1)
                }
                .padding(.leading, 14)
                Spacer()
                SignalBars(rssi: device.rssi)
                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(DyrectoColor.textMuted)
                    .padding(.leading, 6)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(DyrectoColor.surfaceCard)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

/// A small tonal brand tag (e.g. "Sony") on a faint accent tint.
private struct BrandChip: View {
    let label: String

    var body: some View {
        Text(label)
            .font(DyrectoType.labelSmall)
            .fontWeight(.semibold)
            .foregroundColor(DyrectoColor.accent)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(DyrectoColor.accent.opacity(0.14))
            .clipShape(Capsule())
    }
}

/// A compact four-bar signal indicator derived from RSSI (dBm). Stronger signals light more
/// bars in StatusGood; the remaining bars stay a faint outline.
private struct SignalBars: View {
    let rssi: Int

    var body: some View {
        let active = signalLevel(rssi)
        HStack(alignment: .bottom, spacing: 3) {
            ForEach(0..<4, id: \.self) { i in
                RoundedRectangle(cornerRadius: 2, style: .continuous)
                    .fill(i < active ? DyrectoColor.statusGood : DyrectoColor.textPrimary.opacity(0.14))
                    .frame(width: 4, height: CGFloat(6 + i * 4))
            }
        }
    }

    /// Maps an RSSI (dBm) to 1..4 bars. Typical range: ≥ -55 excellent … ≤ -85 poor.
    private func signalLevel(_ rssi: Int) -> Int {
        if rssi >= -55 { return 4 }
        if rssi >= -68 { return 3 }
        if rssi >= -80 { return 2 }
        return 1
    }
}

// MARK: - Selected camera / phase status (DiscoveryScreen.kt copy)

private struct SelectedCameraCard: View {
    let state: CameraConnectionState
    @ObservedObject var ble: Fx3BleManager

    var body: some View {
        PremiumCard {
            HStack {
                Text("Selected Camera")
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Spacer()
                StatusPill(
                    text: ble.isScanning ? "Scanning" : (state.phase == .idle ? "Idle" : "Found"),
                    level: state.phase == .idle ? (ble.isScanning ? .warn : .idle) : .good)
            }
            Spacer().frame(height: 10)
            VStack(spacing: 8) {
                InfoRow(label: "Camera model", value: ble.model.isEmpty ? nil : ble.model)
                InfoRow(label: "Firmware", value: ble.firmware.isEmpty ? nil : ble.firmware)
                InfoRow(label: "Camera id", value: ble.cameraUuid.isEmpty ? nil : ble.cameraUuid, mono: true)
                // interop: ConnectionPhase.label exports as a property on the enum value.
                InfoRow(label: "Connection state", value: state.phase.label)
            }
            if let fatalError = state.fatalError {
                Spacer().frame(height: 8)
                Text(fatalError)
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.error)
            }
        }
    }
}
