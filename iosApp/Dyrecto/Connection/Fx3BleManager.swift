import CoreBluetooth
import Foundation
import DyrectoShared

/// CoreBluetooth port of the verified Android `Fx3BleManager`.
///
/// Parity notes vs Android (see docs/wire-protocol.md §1):
///  - GATT ops run through ONE serialized queue (`opQueue`) exactly like Android — each op
///    completes via its delegate callback, which dequeues the next.
///  - iOS has no `createBond()`/MTU APIs: bonding is triggered implicitly by the first
///    encrypted-characteristic access (the CC17 read pops the system pairing dialog), and MTU
///    is negotiated automatically. The Android "ensure bond → 600 ms settle" step therefore
///    collapses into the CC17 read path.
///  - Wait sentinels (CC0E ack, CC09 wifi-launched) hold the queue ≤30 s and proceed on
///    timeout, matching Android's FX3A tolerance.
final class Fx3BleManager: NSObject, ObservableObject {

    struct DiscoveredCamera: Identifiable, Equatable {
        let id: UUID
        let name: String
        let rssi: Int
    }

    struct WifiCredentials: Equatable {
        let ssid: String
        let password: String
        let bssid: String
    }

    // Published state consumed by CameraConnectionController + Discovery UI.
    @Published private(set) var discovered: [DiscoveredCamera] = []
    @Published private(set) var isScanning = false
    @Published private(set) var firmware = ""
    @Published private(set) var model = ""
    @Published private(set) var cameraUuid = ""

    var onPhase: ((ConnectionPhase) -> Void)?
    var onWifiCredentialsReady: ((WifiCredentials) -> Void)?
    var onWifiFailed: ((String) -> Void)?
    var onSshInfo: ((SshInfoTlv.Result) -> Void)?
    var onError: ((String) -> Void)?

    private var central: CBCentralManager!
    private var peripheral: CBPeripheral?
    private let queue = DispatchQueue(label: "app.dyrecto.ble")
    private let log = DyrectoLog.shared

    // Characteristic cache, keyed by CBUUID.
    private var chars: [CBUUID: CBCharacteristic] = [:]

    // Credentials assembled across the CC06/CC07/CC0C reads.
    private var ssid = ""
    private var password = ""
    private var bssid = ""

    // MARK: Serialized GATT op queue (Android parity)

    private enum Op {
        case read(CBUUID, label: String, handler: (Data?) -> Void)
        case write(CBUUID, Data, label: String, handler: (Bool) -> Void)
        case subscribe(CBUUID, label: String)
        /// Holds the queue until `resolveSentinel` fires or the deadline passes.
        case waitSentinel(label: String, timeout: TimeInterval)
    }

    private var opQueue: [Op] = []
    private var inFlight = false
    private var sentinelTimer: DispatchSourceTimer?
    private var sentinelLabel: String?

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: queue)
    }

    // MARK: Scanning

    private var scanStopTimer: DispatchSourceTimer?

    func startScan() {
        queue.async {
            guard self.central.state == .poweredOn else {
                self.log.line(.error, "BLE not powered on (state=\(self.central.state.rawValue))")
                return
            }
            DispatchQueue.main.async {
                self.discovered = []
                self.isScanning = true
            }
            self.onPhase?(.scanning)
            self.log.line(.scan, "scan start (no HW filter; app-side name match)")
            self.central.scanForPeripherals(withServices: nil, options: [
                CBCentralManagerScanOptionAllowDuplicatesKey: false,
            ])
            // Auto-stop after 30 s, same as Android.
            let timer = DispatchSource.makeTimerSource(queue: self.queue)
            timer.schedule(deadline: .now() + 30)
            timer.setEventHandler { [weak self] in self?.stopScan() }
            timer.resume()
            self.scanStopTimer = timer
        }
    }

    func stopScan() {
        queue.async {
            self.scanStopTimer?.cancel()
            self.scanStopTimer = nil
            if self.central.state == .poweredOn { self.central.stopScan() }
            DispatchQueue.main.async { self.isScanning = false }
            self.log.line(.scan, "scan stop")
        }
    }

    // MARK: Connection

    private var retrievedPeripherals: [UUID: CBPeripheral] = [:]

    func connect(to id: UUID) {
        queue.async {
            guard let p = self.retrievedPeripherals[id] else {
                self.onError?("peripheral \(id) no longer available")
                return
            }
            self.stopScan()
            self.peripheral = p
            p.delegate = self
            self.onPhase?(.connecting)
            self.log.line(.conn, "connectPeripheral \(p.name ?? "?")")
            self.central.connect(p, options: nil)
        }
    }

    func disconnect() {
        queue.async {
            if let p = self.peripheral {
                self.central.cancelPeripheralConnection(p)
            }
            self.resetSession()
        }
    }

    private func resetSession() {
        opQueue.removeAll()
        inFlight = false
        sentinelTimer?.cancel()
        sentinelTimer = nil
        sentinelLabel = nil
        chars.removeAll()
        ssid = ""; password = ""; bssid = ""
    }

    // MARK: Provisioning sequence (wire-protocol.md §1.4)

    private func enqueueProvisioningSequence() {
        // Step 0 — EE diagnostics.
        enqueue(.subscribe(Fx3Uuids.ee03PairingNotify, label: "EE03"))
        enqueue(.read(Fx3Uuids.ee02Registration, label: "EE02") { _ in })
        enqueue(.read(Fx3Uuids.ee04Registration, label: "EE04") { _ in })

        // Step 1 — notifications, exact Android order; missing chars skip gracefully.
        for (uuid, label) in [
            (Fx3Uuids.cc03Notify, "CC03"), (Fx3Uuids.cc09WifiStatus, "CC09"),
            (Fx3Uuids.cc0fNotify, "CC0F"), (Fx3Uuids.cc10Notify, "CC10"),
            (Fx3Uuids.cc16Notify, "CC16"), (Fx3Uuids.cc1bNotify, "CC1B"),
            (Fx3Uuids.cca1CameraSsid, "CCA1"), (Fx3Uuids.cca5Info, "CCA5"),
            (Fx3Uuids.cca9Info, "CCA9"),
        ] {
            enqueue(.subscribe(uuid, label: label))
        }

        // Step 2 — confirmed reads.
        enqueue(.read(Fx3Uuids.cc0aFirmware, label: "CC0A firmware") { [weak self] data in
            let text = data.flatMap { String(data: $0, encoding: .utf8) } ?? ""
            DispatchQueue.main.async { self?.firmware = text }
        })
        enqueue(.read(Fx3Uuids.cc0bModel, label: "CC0B model") { [weak self] data in
            let text = data.flatMap { String(data: $0, encoding: .utf8) } ?? ""
            DispatchQueue.main.async { self?.model = text }
        })
        enqueue(.read(Fx3Uuids.cc0dDeviceInfo, label: "CC0D capability") { _ in })
        enqueue(.read(Fx3Uuids.cca1CameraSsid, label: "CCA1 cameraSsid") { _ in })
        enqueue(.read(Fx3Uuids.cca2CameraUuid, label: "CCA2 cameraUuid") { [weak self] data in
            let text = data.flatMap { String(data: $0, encoding: .utf8) } ?? ""
            DispatchQueue.main.async { self?.cameraUuid = text }
        })
        enqueue(.read(Fx3Uuids.cca7DeviceInfo, label: "CCA7") { _ in })

        // Step 3 — Sony AP sequence (EE01 pairing intentionally NOT sent per-connect).
        enqueue(.subscribe(Fx3Uuids.cc0eControlResult, label: "CC0E"))
        enqueue(.write(Fx3Uuids.cca3SmartphoneControl, Fx3Uuids.smartphoneControlOn,
                       label: "CCA3 smartphoneControl ON") { [weak self] _ in
            self?.onPhase?(.smartphoneMode)
        })
        enqueue(.waitSentinel(label: "CC0E-ack", timeout: 30))
        enqueue(.write(Fx3Uuids.cc08WifiApOn, Fx3Uuids.wifiApOn,
                       label: "CC08 wifiAp ON") { [weak self] _ in
            self?.onPhase?(.apCreating)
        })
        enqueue(.waitSentinel(label: "CC09-launched", timeout: 30))

        // Step 4 — Wi-Fi credentials.
        enqueue(.read(Fx3Uuids.cc06WifiSsid, label: "CC06 ssid") { [weak self] data in
            self?.ssid = data.flatMap { String(data: $0, encoding: .utf8) } ?? ""
        })
        enqueue(.read(Fx3Uuids.cc07WifiPassword, label: "CC07 password") { [weak self] data in
            self?.password = data.flatMap { String(data: $0, encoding: .utf8) } ?? ""
        })
        enqueue(.read(Fx3Uuids.cc0cBssid, label: "CC0C bssid") { [weak self] data in
            guard let self else { return }
            self.bssid = data.map { $0.map { String(format: "%02x", $0) }.joined(separator: ":") } ?? ""
            if !self.ssid.isEmpty && !self.password.isEmpty {
                self.onPhase?(.wifiCredentials)
                self.onWifiCredentialsReady?(WifiCredentials(
                    ssid: self.ssid, password: self.password, bssid: self.bssid))
            } else {
                self.onWifiFailed?("credentials incomplete: ssid=\(!self.ssid.isEmpty) pass=\(!self.password.isEmpty)")
            }
        })

        dequeueNext()
    }

    /// Reads CC17 and decodes the SSH TLV via the shared decoder. Called by the controller
    /// AFTER Wi-Fi join + SSDP resolution (credential-mint gate — wire-protocol.md §1.4).
    /// This read hits an encrypted characteristic, so iOS pops the pairing dialog here on
    /// first contact (the implicit-bond point).
    func readSshInfo() {
        queue.async {
            self.enqueue(.read(Fx3Uuids.cc17SshInfo, label: "CC17 sshInfo") { [weak self] data in
                guard let self else { return }
                guard let data else {
                    self.onError?("CC17 read failed")
                    return
                }
                let result = SshInfoTlv.shared.decode(b: KotlinByteArray.from(data: data))
                self.log.line(.read, "CC17 state=\(result.state) notes=\(result.notes)")
                self.onSshInfo?(result)
            })
            self.dequeueNext()
        }
    }

    /// One-time pairing/registration — explicit user action ONLY (Developer screen), never
    /// per-connect: re-sending EE01 resets registration and stops SSH credential minting.
    func sendPairingRequest() {
        queue.async {
            self.enqueue(.subscribe(Fx3Uuids.ee01Pairing, label: "EE01"))
            self.enqueue(.subscribe(Fx3Uuids.ee03PairingNotify, label: "EE03"))
            self.enqueue(.write(Fx3Uuids.ee01Pairing, Fx3Uuids.pairingRequest, label: "EE01 pairing") { _ in })
            self.enqueue(.read(Fx3Uuids.ee04Registration, label: "EE04") { _ in })
            self.dequeueNext()
        }
    }

    // MARK: Queue mechanics

    private func enqueue(_ op: Op) { opQueue.append(op) }

    private func dequeueNext() {
        guard !inFlight, let p = peripheral else { return }
        guard !opQueue.isEmpty else { return }
        let op = opQueue.removeFirst()
        inFlight = true

        switch op {
        case .read(let uuid, let label, let handler):
            guard let c = chars[uuid] else {
                log.line(.read, "\(label): characteristic missing — skipped")
                pendingReadHandlers[uuid] = nil
                inFlight = false
                handler(nil)
                dequeueNext()
                return
            }
            log.line(.read, "\(label): read")
            pendingReadHandlers[uuid] = handler
            p.readValue(for: c)

        case .write(let uuid, let value, let label, let handler):
            guard let c = chars[uuid] else {
                log.line(.write, "\(label): characteristic missing — skipped")
                inFlight = false
                handler(false)
                dequeueNext()
                return
            }
            log.line(.write, "\(label): write \(value.map { String(format: "%02x", $0) }.joined(separator: " "))")
            pendingWriteHandlers[uuid] = handler
            p.writeValue(value, for: c, type: .withResponse)

        case .subscribe(let uuid, let label):
            guard let c = chars[uuid] else {
                log.line(.desc, "\(label): characteristic missing — subscribe skipped")
                inFlight = false
                dequeueNext()
                return
            }
            log.line(.desc, "\(label): enable notifications")
            p.setNotifyValue(true, for: c)

        case .waitSentinel(let label, let timeout):
            log.line(.info, "sentinel \(label): waiting (≤\(Int(timeout)) s)")
            sentinelLabel = label
            let timer = DispatchSource.makeTimerSource(queue: queue)
            timer.schedule(deadline: .now() + timeout)
            timer.setEventHandler { [weak self] in
                guard let self, self.sentinelLabel == label else { return }
                self.log.line(.info, "sentinel \(label): timeout — proceeding (FX3A tolerance)")
                self.resolveSentinel(label)
            }
            timer.resume()
            sentinelTimer = timer
        }
    }

    private func resolveSentinel(_ label: String) {
        guard sentinelLabel == label else { return }
        sentinelTimer?.cancel()
        sentinelTimer = nil
        sentinelLabel = nil
        inFlight = false
        dequeueNext()
    }

    private var pendingReadHandlers: [CBUUID: ((Data?) -> Void)] = [:]
    private var pendingWriteHandlers: [CBUUID: ((Bool) -> Void)] = [:]

    // MARK: Notification frame parsing (wire-protocol.md §1.5)

    private func handleNotification(_ uuid: CBUUID, _ value: Data) {
        let hex = value.map { String(format: "%02x", $0) }.joined(separator: " ")
        log.line(.notify, "\(uuid.uuidString.prefix(8)): \(hex)")

        if uuid == Fx3Uuids.cc0eControlResult, sentinelLabel == "CC0E-ack" {
            guard value.count >= 8 else { return }
            let type = (UInt16(value[1]) << 8) | UInt16(value[2])
            guard type == 0x000B else { return } // not this frame — keep waiting
            if value[3] == 0x01 {
                log.line(.notify, "CC0E: smartphone-control OK")
            } else {
                let reason = value[4...7].reduce(UInt32(0)) { ($0 << 8) | UInt32($1) }
                log.line(.notify, "CC0E: smartphone-control NG reason=\(reason) — continuing (FX3A quirk)")
            }
            resolveSentinel("CC0E-ack")
        }

        if uuid == Fx3Uuids.cc09WifiStatus, sentinelLabel == "CC09-launched" {
            guard value.count >= 4 else { return }
            switch value[3] {
            case 0x01: log.line(.notify, "CC09: AP creating — waiting")
            case 0x02:
                log.line(.notify, "CC09: AP ready")
                resolveSentinel("CC09-launched")
            default: break
            }
        }
    }
}

// MARK: - CBCentralManagerDelegate

extension Fx3BleManager: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        log.line(.info, "central state=\(central.state.rawValue)")
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String: Any], rssi RSSI: NSNumber) {
        let name = peripheral.name
            ?? (advertisementData[CBAdvertisementDataLocalNameKey] as? String)
            ?? ""
        guard Fx3Uuids.nameFilters.contains(where: { name.localizedCaseInsensitiveContains($0) }) else { return }
        retrievedPeripherals[peripheral.identifier] = peripheral
        let cam = DiscoveredCamera(id: peripheral.identifier, name: name, rssi: RSSI.intValue)
        DispatchQueue.main.async {
            if let idx = self.discovered.firstIndex(where: { $0.id == cam.id }) {
                self.discovered[idx] = cam
            } else {
                self.discovered.append(cam)
                self.log.line(.scan, "found \(name) rssi=\(RSSI)")
            }
        }
        onPhase?(.bleFound)
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        log.line(.conn, "connected — discovering services")
        onPhase?(.connected)
        peripheral.discoverServices([Fx3Uuids.provisioningService, Fx3Uuids.pairingService])
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        log.line(.error, "connect failed: \(error?.localizedDescription ?? "?")")
        onError?("BLE connect failed: \(error?.localizedDescription ?? "unknown")")
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        log.line(.conn, "disconnected: \(error?.localizedDescription ?? "clean")")
        queue.async { self.resetSession() }
    }
}

// MARK: - CBPeripheralDelegate

extension Fx3BleManager: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        guard error == nil, let services = peripheral.services else {
            onError?("service discovery failed: \(error?.localizedDescription ?? "?")")
            return
        }
        for s in services {
            log.line(.service, "service \(s.uuid.uuidString)")
            peripheral.discoverCharacteristics(nil, for: s)
        }
    }

    private var expectedServiceCount: Int { 2 }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        guard error == nil else {
            onError?("characteristic discovery failed: \(error?.localizedDescription ?? "?")")
            return
        }
        for c in service.characteristics ?? [] {
            chars[c.uuid] = c
        }
        // Kick provisioning once the CC service's characteristics are in.
        if service.uuid == Fx3Uuids.provisioningService {
            log.line(.service, "provisioning service ready (\(chars.count) chars cached)")
            onPhase?(.readingInfo)
            enqueueProvisioningSequence()
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        let uuid = characteristic.uuid
        // A value update is either the answer to a queued read, or an unsolicited notification.
        if let handler = pendingReadHandlers.removeValue(forKey: uuid) {
            if let error {
                log.line(.error, "read \(uuid.uuidString.prefix(8)) failed: \(error.localizedDescription)")
                handler(nil)
            } else {
                handler(characteristic.value)
            }
            inFlight = false
            dequeueNext()
        } else if let value = characteristic.value {
            handleNotification(uuid, value)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let handler = pendingWriteHandlers.removeValue(forKey: characteristic.uuid) {
            if let error {
                log.line(.error, "write failed: \(error.localizedDescription)")
                handler(false)
            } else {
                handler(true)
            }
        }
        inFlight = false
        dequeueNext()
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if let error {
            log.line(.error, "notify state \(characteristic.uuid.uuidString.prefix(8)): \(error.localizedDescription)")
        }
        inFlight = false
        dequeueNext()
    }
}

// MARK: - Kotlin interop helper

extension KotlinByteArray {
    static func from(data: Data) -> KotlinByteArray {
        let arr = KotlinByteArray(size: Int32(data.count))
        for (i, b) in data.enumerated() {
            arr.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return arr
    }

    func toData() -> Data {
        var d = Data(capacity: Int(size))
        for i in 0..<size {
            d.append(UInt8(bitPattern: get(index: i)))
        }
        return d
    }
}
