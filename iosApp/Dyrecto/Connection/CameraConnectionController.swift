import Foundation
import DyrectoShared

/// iOS counterpart of the Android `CameraRepositoryImpl` — owns the connect flow (BLE → Wi-Fi →
/// SSDP → CC17 → SSH-or-direct PTP), the phase state machine, and the published
/// `CameraConnectionState` (the shared domain type the alert engine + UI consume).
///
/// Phase order + gates are byte-for-byte the Android flow (docs/wire-protocol.md §7); the one
/// intentional platform difference is where bonding happens (iOS bonds implicitly on the CC17
/// read — see Fx3BleManager).
final class CameraConnectionController: ObservableObject {

    @Published private(set) var state = CameraConnectionState(
        phase: .idle, bleStatus: nil, sshStatus: nil, ptpStatus: nil, deviceInfo: nil,
        telemetry: nil, timeline: [], lastTelemetryUpdateAt: nil, fatalError: nil,
        lastSuccessfulCommunicationAt: nil)

    let ble = Fx3BleManager()
    private let wifi = WifiJoiner()
    private let ssdp = SsdpDiscoverer()
    private let log = DyrectoLog.shared
    private let workQueue = DispatchQueue(label: "app.dyrecto.connection", qos: .userInitiated)

    // Live connection artifacts.
    private(set) var ptp: PtpIpClient?
    private(set) var cameraIp = WifiJoiner.defaultCameraIp
    private(set) var phoneIp = ""
    private var sshTunnel: SshTunnel?
    private var joinedSsid: String?

    /// Downstream consumers (monitoring session) observe these.
    var onStateChanged: ((CameraConnectionState) -> Void)?
    var onTelemetry: ((CameraTelemetry) -> Void)?

    // MARK: State updates (single-writer on workQueue, published on main)

    private func update(_ transform: (CameraConnectionState) -> CameraConnectionState) {
        let next = transform(stateSnapshot())
        DispatchQueue.main.async {
            self.state = next
            self.onStateChanged?(next)
        }
    }

    private var latest: CameraConnectionState?
    private func stateSnapshot() -> CameraConnectionState {
        if Thread.isMainThread { return state }
        return latest ?? state
    }

    private func setPhase(_ phase: ConnectionPhase) {
        log.line(.conn, "phase → \(phase)")
        update { $0.doCopy(
            phase: phase, bleStatus: $0.bleStatus, sshStatus: $0.sshStatus,
            ptpStatus: $0.ptpStatus, deviceInfo: $0.deviceInfo, telemetry: $0.telemetry,
            timeline: $0.timeline, lastTelemetryUpdateAt: $0.lastTelemetryUpdateAt,
            fatalError: nil, lastSuccessfulCommunicationAt: $0.lastSuccessfulCommunicationAt) }
    }

    private func fail(_ message: String) {
        log.line(.error, "connection failed: \(message)")
        update { $0.doCopy(
            phase: .error, bleStatus: $0.bleStatus, sshStatus: $0.sshStatus,
            ptpStatus: $0.ptpStatus, deviceInfo: $0.deviceInfo, telemetry: $0.telemetry,
            timeline: $0.timeline, lastTelemetryUpdateAt: $0.lastTelemetryUpdateAt,
            fatalError: message, lastSuccessfulCommunicationAt: $0.lastSuccessfulCommunicationAt) }
    }

    // MARK: Public intents

    init() {
        ble.onPhase = { [weak self] phase in self?.setPhase(phase) }
        ble.onError = { [weak self] message in self?.fail(message) }
        ble.onWifiCredentialsReady = { [weak self] creds in
            self?.workQueue.async { self?.joinWifi(creds) }
        }
        ble.onWifiFailed = { [weak self] reason in self?.fail("Wi-Fi credentials: \(reason)") }
        ble.onSshInfo = { [weak self] info in
            self?.workQueue.async { self?.handleSshInfo(info) }
        }
    }

    func startScan() { ble.startScan() }
    func stopScan() { ble.stopScan() }
    func connect(to id: UUID) { ble.connect(to: id) }

    func disconnect() {
        workQueue.async {
            self.ptp?.shutdown()
            self.ptp = nil
            self.sshTunnel?.disconnect()
            self.sshTunnel = nil
            self.ble.disconnect()
            if let ssid = self.joinedSsid { self.wifi.removeConfiguration(ssid: ssid) }
            self.setPhase(.idle)
        }
    }

    // MARK: Flow — Wi-Fi join → SSDP gate → CC17 re-read

    private func joinWifi(_ creds: Fx3BleManager.WifiCredentials) {
        setPhase(.wifiJoining)
        joinedSsid = creds.ssid
        wifi.join(ssid: creds.ssid, password: creds.password) { [weak self] result in
            guard let self else { return }
            switch result {
            case .failure(let error):
                self.fail("Wi-Fi join: \(error.localizedDescription)")
            case .success(let join):
                self.cameraIp = join.cameraIp
                self.phoneIp = join.phoneIp
                self.setPhase(.ipDiscovery)
                // SSDP is the credential-mint gate: CC17 is only re-read after it resolves.
                self.ssdp.discover(cameraIp: join.cameraIp) { ssdpResult in
                    switch ssdpResult {
                    case .failure(let error):
                        self.fail("SSDP: \(error.localizedDescription)")
                    case .success(let uuid):
                        self.log.line(.conn, "SSDP resolved uuid=\(uuid) — reading CC17")
                        self.ble.readSshInfo()
                    }
                }
            }
        }
    }

    private func handleSshInfo(_ info: SshInfoTlv.Result) {
        setPhase(.cc17Read)
        switch info.state {
        case .on:
            guard !info.sshId.isEmpty, !info.sshPass.isEmpty else {
                fail("CC17: SSH ON but credentials incomplete (session didn't mint creds — try Pair/Register once)")
                return
            }
            launchSshSession(user: info.sshId, password: info.sshPass)
        case .off:
            launchDirectPtpSession()
        default:
            fail("CC17: SSH state unknown — cannot select transport")
        }
    }

    // MARK: PTP bootstrap — direct mode (steady-state features live here)

    private func launchDirectPtpSession() {
        workQueue.async {
            self.setPhase(.ptpInit)
            do {
                let command = try TcpByteChannel(host: self.cameraIp, port: PtpIpClient.ptpPort)
                let event = try TcpByteChannel(host: self.cameraIp, port: PtpIpClient.ptpPort)
                let client = PtpIpClient(command: command, event: event)
                self.wireTelemetry(client)
                self.setPhase(.sessionOpen)
                let bootstrap = try client.bootstrap()
                self.ptp = client
                self.publishBootstrap(bootstrap)
                self.setPhase(.deviceInfo)
                // Direct mode keeps the session alive: event listener + 5 s liveness.
                client.startEventListener()
            } catch {
                self.fail("PTP: \(error)")
            }
        }
    }

    // MARK: PTP bootstrap — SSH mode (bootstrap-only; disconnects after DEVICE_INFO)

    private func launchSshSession(user: String, password: String) {
        workQueue.async {
            self.setPhase(.sshConnecting)
            do {
                let tunnel = try SshTunnel(host: self.cameraIp, username: user, password: password)
                self.sshTunnel = tunnel
                self.setPhase(.sshAuthenticated)

                let command = try tunnel.openPtpChannel()
                let event = try tunnel.openPtpChannel()
                let client = PtpIpClient(command: command, event: event)
                self.setPhase(.ptpInit)
                self.setPhase(.sessionOpen)
                let bootstrap = try client.bootstrap()
                self.publishBootstrap(bootstrap)
                self.setPhase(.deviceInfo)

                // SSH mode is bootstrap-only (Android parity): tear the tunnel down now.
                client.shutdown()
                tunnel.disconnect()
                self.sshTunnel = nil
            } catch {
                self.sshTunnel?.disconnect()
                self.sshTunnel = nil
                self.fail("SSH session: \(error)")
            }
        }
    }

    // MARK: Publication into shared domain types

    private func wireTelemetry(_ client: PtpIpClient) {
        client.onTelemetryRefreshed = { [weak self] props in
            guard let self else { return }
            let telemetry = Self.toDomain(props)
            let now = KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000))
            self.update { s in
                let merged: CameraTelemetry
                if let existing = s.telemetry {
                    var union = existing.props
                    for (k, v) in telemetry.props { union[k] = v }
                    merged = CameraTelemetry(props: union)
                } else {
                    merged = telemetry
                }
                return s.doCopy(
                    phase: s.phase, bleStatus: s.bleStatus, sshStatus: s.sshStatus,
                    ptpStatus: s.ptpStatus, deviceInfo: s.deviceInfo, telemetry: merged,
                    timeline: s.timeline, lastTelemetryUpdateAt: now,
                    fatalError: s.fatalError, lastSuccessfulCommunicationAt: now)
            }
            self.onTelemetry?(telemetry)
        }
        client.onSessionEnded = { [weak self] reason in
            self?.fail("session ended: \(reason)")
        }
    }

    private func publishBootstrap(_ bootstrap: PtpIpClient.BootstrapResult) {
        let info = bootstrap.deviceInfo
        let operations = info.operations.map { code in
            PtpOpcodes.shared.describe(code: Int32(code))
        }
        let deviceInfo = CameraDeviceInfo(
            manufacturer: info.manufacturer,
            model: info.model,
            firmwareVersion: info.deviceVersion,
            serialNumber: info.serialNumber,
            standardVersion: Int32(info.standardVersion),
            vendorExtensionId: info.vendorExtensionId,
            vendorExtensionVersion: Int32(info.vendorExtensionVersion),
            vendorExtensionDescription: info.vendorExtensionDescription,
            functionalMode: Int32(info.functionalMode),
            operations: operations,
            supportedEventCount: Int32(info.events.count),
            supportedPropertyCount: Int32(info.properties.count),
            captureFormatCount: Int32(info.captureFormats.count),
            imageFormatCount: Int32(info.imageFormats.count))

        let telemetry = Self.toDomain(bootstrap.telemetry)
        let now = KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000))
        update { s in s.doCopy(
            phase: s.phase, bleStatus: s.bleStatus, sshStatus: s.sshStatus,
            ptpStatus: s.ptpStatus, deviceInfo: deviceInfo, telemetry: telemetry,
            timeline: s.timeline, lastTelemetryUpdateAt: now, fatalError: nil,
            lastSuccessfulCommunicationAt: now) }
        onTelemetry?(telemetry)
    }

    /// SonyProp (wire) → shared domain CameraTelemetry, mirroring Android's `toDomain()`.
    static func toDomain(_ props: [UInt16: SonyProp]) -> CameraTelemetry {
        var domain = [KotlinInt: TelemetryProp]()
        for (code, prop) in props {
            let intCode = Int32(code)
            let raw: String?
            if let s = prop.stringValue {
                raw = s
            } else if let v = prop.intValue {
                raw = String(v)
            } else {
                raw = nil
            }
            domain[KotlinInt(value: intCode)] = TelemetryProp(
                code: intCode,
                label: CameraTelemetry.companion.labelFor(code: intCode),
                rawValue: raw,
                dataType: Int32(prop.dataType),
                rawNumber: prop.intValue.map { KotlinLong(value: $0) })
        }
        return CameraTelemetry(props: domain)
    }

    // MARK: Accessors used by live view / push LV

    var liveViewUrl: String? {
        state.telemetry?.get(code: Int32(SonyProp.Code.liveViewUrl))?.rawValue
    }

    var liveViewStatusRaw: Int64? {
        state.telemetry?.get(code: Int32(SonyProp.Code.liveViewStatus))?.rawNumber?.int64Value
    }
}
