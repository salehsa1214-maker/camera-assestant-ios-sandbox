import Foundation

/// Swift port of the verified Android `PtpIpClient` (docs/wire-protocol.md §4).
/// Synchronous protocol code on caller-owned threads, one lock around all command-channel I/O
/// and transaction-id allocation — structurally identical to the Android original so wire
/// behavior matches byte for byte.
final class PtpIpClient {

    // MARK: Packet types / opcodes / response codes (verbatim constants)

    enum PacketType: UInt32 {
        case initCmdReq = 1, initCmdAck = 2, initEvtReq = 3, initEvtAck = 4
        case initFail = 5, opRequest = 6, opResponse = 7, event = 8
        case startData = 9, dataPacket = 10, cancel = 11, endData = 12
    }

    enum Op {
        static let getDeviceInfo: UInt16 = 0x1001
        static let openSession: UInt16 = 0x1002
        static let sdioConnect: UInt16 = 0x9201
        static let sdioGetExtDeviceInfo: UInt16 = 0x9202
        static let sdioGetAllExtDevicePropInfo: UInt16 = 0x9209
        static let sdioControlMonitoring: UInt16 = 0x9230
    }

    enum Rc {
        static let ok: UInt16 = 0x2001
    }

    static let ptpPort: UInt16 = 15740
    static let sessionId: UInt32 = 1
    static let initName = "CameraAssistant"
    static let protocolVersion: UInt32 = 0x0001_0000
    static let bootstrapReadTimeout: TimeInterval = 10.0
    static let steadyReadTimeout: TimeInterval = 3.0
    static let livenessInterval: TimeInterval = 5.0
    static let eventDebounce: TimeInterval = 0.150
    static let maxPacketLength = 8_000_000

    struct OperationResult {
        let responseCode: UInt16
        let transactionId: UInt32
        let data: Data
        let responseParams: [UInt32]
    }

    struct BootstrapResult {
        let connectionNumber: UInt32
        let responderName: String
        let deviceInfo: PtpDeviceInfo
        let telemetry: [UInt16: SonyProp]
    }

    // MARK: State

    private let command: PtpByteChannel
    private let event: PtpByteChannel
    private let log = DyrectoLog.shared

    /// Serializes ALL command-channel I/O and tid allocation (Android `ptpLock`).
    private let ptpLock = NSRecursiveLock()
    private var nextTid: UInt32 = 0
    private var steadyState = false
    private(set) var lastReadTimedOut = false

    var onTelemetryRefreshed: (([UInt16: SonyProp]) -> Void)?
    var onSessionEnded: ((String) -> Void)?

    init(command: PtpByteChannel, event: PtpByteChannel) {
        self.command = command
        self.event = event
    }

    // MARK: Framing

    private func writePacket(_ type: PacketType, body: Data, to channel: PtpByteChannel) throws {
        var w = BinaryWriter()
        w.u32(UInt32(8 + body.count))
        w.u32(type.rawValue)
        w.raw(body)
        try channel.write(w.data)
    }

    private struct Packet {
        let type: UInt32
        let body: Data
    }

    private func readPacket(from channel: PtpByteChannel, deadline: TimeInterval?) throws -> Packet {
        let header = try channel.readFully(8, deadline: deadline)
        var r = BinaryReader(header)
        let length = Int(try r.u32())
        let type = try r.u32()
        guard length >= 8, length <= Self.maxPacketLength else {
            throw BinaryIOError.malformed("bad packet length \(length)")
        }
        let body = length > 8 ? try channel.readFully(length - 8, deadline: deadline) : Data()
        return Packet(type: type, body: body)
    }

    private var currentDeadline: TimeInterval? {
        steadyState ? Self.steadyReadTimeout : Self.bootstrapReadTimeout
    }

    // MARK: Init handshake (§4.2)

    func initCommandChannel() throws -> (connectionNumber: UInt32, responderName: String) {
        var body = BinaryWriter()
        // 16-byte random GUID, big-endian halves like the Android UUID serialization.
        var guid = Data(count: 16)
        for i in 0..<16 { guid[i] = UInt8.random(in: 0...255) }
        body.raw(guid)
        body.utf16leNulTerminated(Self.initName)
        body.u32(Self.protocolVersion)
        try writePacket(.initCmdReq, body: body.data, to: command)

        let ack = try readPacket(from: command, deadline: Self.bootstrapReadTimeout)
        guard ack.type == PacketType.initCmdAck.rawValue else {
            throw BinaryIOError.malformed("expected INIT_CMD_ACK, got type \(ack.type)")
        }
        var r = BinaryReader(ack.body)
        let connNo = try r.u32()
        _ = try r.bytes(16) // responder GUID
        // Responder name: UTF-16LE until 0x0000.
        var units = [UInt16]()
        while r.remaining >= 2 {
            let u = try r.u16()
            if u == 0 { break }
            units.append(u)
        }
        let name = String(decoding: units, as: UTF16.self)
        log.line(.conn, "PTP init: connNo=\(connNo) responder=\(name)")
        return (connNo, name)
    }

    func initEventChannel(connectionNumber: UInt32) throws {
        var body = BinaryWriter()
        body.u32(connectionNumber)
        try writePacket(.initEvtReq, body: body.data, to: event)
        let ack = try readPacket(from: event, deadline: Self.bootstrapReadTimeout)
        guard ack.type == PacketType.initEvtAck.rawValue else {
            throw BinaryIOError.malformed("expected INIT_EVT_ACK, got type \(ack.type)")
        }
        log.line(.conn, "PTP event channel ready")
    }

    // MARK: Operations (§4.1, §4.3)

    /// Sends one operation and consumes packets until its OP_RESPONSE, accumulating data
    /// phases; async EVENTs are logged+skipped; stale responses (tid in [1, requested)) are
    /// dropped — the resync guard that prevents a permanent off-by-one after a timeout.
    @discardableResult
    func sendOperation(_ opcode: UInt16, params: [UInt32] = [],
                       dataOut: Data? = nil) throws -> OperationResult {
        ptpLock.lock()
        defer { ptpLock.unlock() }
        lastReadTimedOut = false

        let tid = nextTid
        nextTid += 1

        var req = BinaryWriter()
        req.u32(dataOut == nil ? 1 : 2)
        req.u16(opcode)
        req.u32(tid)
        for p in params { req.u32(p) }
        try writePacket(.opRequest, body: req.data, to: command)

        if let dataOut {
            var start = BinaryWriter()
            start.u32(tid)
            start.u64(UInt64(dataOut.count))
            try writePacket(.startData, body: start.data, to: command)

            var end = BinaryWriter()
            end.u32(tid)
            end.raw(dataOut)
            try writePacket(.endData, body: end.data, to: command)
        }

        var accumulated = Data()
        while true {
            let packet: Packet
            do {
                packet = try readPacket(from: command, deadline: currentDeadline)
            } catch PtpChannelError.timeout {
                lastReadTimedOut = true
                throw PtpChannelError.timeout
            }

            switch packet.type {
            case PacketType.startData.rawValue:
                continue // 20-byte header consumed; payload arrives in DATA/END_DATA
            case PacketType.dataPacket.rawValue, PacketType.endData.rawValue:
                if packet.body.count > 4 {
                    accumulated.append(packet.body.subdata(
                        in: (packet.body.startIndex + 4)..<packet.body.endIndex))
                }
            case PacketType.event.rawValue:
                var r = BinaryReader(packet.body)
                let code = (try? r.u16()) ?? 0
                log.line(.info, String(format: "PTP: async event 0x%04X on cmd channel — skipped", code))
            case PacketType.opResponse.rawValue:
                var r = BinaryReader(packet.body)
                let rc = try r.u16()
                let rtid = try r.u32()
                if rtid != tid, rtid >= 1, rtid < tid {
                    log.line(.info, "PTP: stale response tid=\(rtid) (want \(tid)) — dropped, resyncing")
                    accumulated.removeAll()
                    continue
                }
                var respParams = [UInt32]()
                while r.remaining >= 4 { respParams.append(try r.u32()) }
                return OperationResult(responseCode: rc, transactionId: rtid,
                                       data: accumulated, responseParams: respParams)
            default:
                log.line(.info, "PTP: unexpected packet type \(packet.type) — skipped")
            }
        }
    }

    // MARK: Bootstrap (§4.2, §4.5)

    func bootstrap() throws -> BootstrapResult {
        let (connNo, responder) = try initCommandChannel()
        try initEventChannel(connectionNumber: connNo)

        let open = try sendOperation(Op.openSession, params: [Self.sessionId])
        log.line(.conn, String(format: "OpenSession rc=0x%04X", open.responseCode))
        guard open.responseCode == Rc.ok || open.responseCode == 0x201F /* AlreadyOpen */ else {
            throw BinaryIOError.malformed(String(format: "OpenSession failed rc=0x%04X", open.responseCode))
        }

        // tid 1 burned to mirror Android's "GetDeviceInfo at tid 2" numbering.
        nextTid = 2
        let deviceInfoResult = try sendOperation(Op.getDeviceInfo)
        let deviceInfo = try PtpDeviceInfo.parse(deviceInfoResult.data)
        log.line(.conn, "GetDeviceInfo: \(deviceInfo.manufacturer) \(deviceInfo.model) fw=\(deviceInfo.deviceVersion)")

        // Sony SDIO handshake — non-OK rc is log-and-continue; only socket death aborts.
        for (params, label) in [([UInt32(1), 0, 0], "SDIO_Connect 1"),
                                ([UInt32(2), 0, 0], "SDIO_Connect 2")] {
            let r = try sendOperation(Op.sdioConnect, params: params)
            log.line(.conn, String(format: "%@ rc=0x%04X", label, r.responseCode))
        }
        let ext = try sendOperation(Op.sdioGetExtDeviceInfo, params: [300, 1])
        log.line(.conn, String(format: "SDIO_GetExtDeviceInfo rc=0x%04X (%d B)", ext.responseCode, ext.data.count))
        let c3 = try sendOperation(Op.sdioConnect, params: [3, 0, 0])
        log.line(.conn, String(format: "SDIO_Connect 3 rc=0x%04X", c3.responseCode))

        let snapshot = try sendOperation(Op.sdioGetAllExtDevicePropInfo, params: [0, 1])
        let telemetry = SonyProp.parseDataset(snapshot.data, log: log)
        log.line(.conn, "telemetry snapshot: \(telemetry.count) props")

        return BootstrapResult(connectionNumber: connNo, responderName: responder,
                               deviceInfo: deviceInfo, telemetry: telemetry)
    }

    // MARK: Steady state — event listener + liveness (§4.8)

    private var listening = false
    private var livenessTimer: DispatchSourceTimer?
    private var refreshInProgress = false
    private var lastRefreshAt = Date.distantPast
    private var restartBudget = 3

    func startEventListener() {
        ptpLock.lock()
        steadyState = true
        listening = true
        ptpLock.unlock()

        // Liveness daemon: 0x9209 [1,1] every 5 s — the camera closes idle PTP after ~10 s.
        let timer = DispatchSource.makeTimerSource(queue: DispatchQueue(label: "app.dyrecto.ptp.liveness"))
        timer.schedule(deadline: .now() + Self.livenessInterval, repeating: Self.livenessInterval)
        timer.setEventHandler { [weak self] in
            self?.refreshTelemetry(reason: "liveness")
        }
        timer.resume()
        livenessTimer = timer

        Thread.detachNewThread { [weak self] in
            Thread.current.name = "ptp-event-listener"
            self?.eventLoop()
        }
        log.line(.conn, "PTP steady state: event listener + 5 s liveness started")
    }

    private func eventLoop() {
        while listening {
            do {
                let packet = try readPacket(from: event, deadline: nil) // blocking
                guard packet.type == PacketType.event.rawValue else { continue }
                var r = BinaryReader(packet.body)
                let code = (try? r.u16()) ?? 0
                log.line(.info, String(format: "PTP event 0x%04X", code))
                if Date().timeIntervalSince(lastRefreshAt) >= Self.eventDebounce {
                    refreshTelemetry(reason: String(format: "event 0x%04X", code))
                }
            } catch PtpChannelError.eof {
                log.line(.conn, "PTP event channel EOF — session over")
                listening = false
                onSessionEnded?("event channel EOF")
            } catch {
                guard listening else { break }
                if restartBudget > 0 {
                    restartBudget -= 1
                    log.line(.error, "event loop error (\(error)) — restarting (\(restartBudget) left)")
                    continue
                }
                listening = false
                onSessionEnded?("event loop failed: \(error)")
            }
        }
    }

    /// `0x9209 [1,1]` refresh on the command channel — shared by liveness + event handling,
    /// serialized by ptpLock + the refreshInProgress guard.
    private func refreshTelemetry(reason: String) {
        ptpLock.lock()
        if refreshInProgress {
            ptpLock.unlock()
            return
        }
        refreshInProgress = true
        ptpLock.unlock()
        defer {
            ptpLock.lock()
            refreshInProgress = false
            ptpLock.unlock()
        }

        do {
            let result = try sendOperation(Op.sdioGetAllExtDevicePropInfo, params: [1, 1])
            lastRefreshAt = Date()
            if result.responseCode == Rc.ok, !result.data.isEmpty {
                let props = SonyProp.parseDataset(result.data, log: log)
                if !props.isEmpty { onTelemetryRefreshed?(props) }
            }
        } catch PtpChannelError.timeout {
            // Transient — keep listening (Android `lastReadTimedOut` semantics).
            log.line(.info, "telemetry refresh (\(reason)) timed out — non-fatal")
        } catch {
            log.line(.error, "telemetry refresh (\(reason)) failed: \(error)")
            listening = false
            onSessionEnded?("command channel failed: \(error)")
        }
    }

    // MARK: Push LV borrow point (§5)

    /// Sends a Sony DataOut operation on the live command channel, borrowing the shared lock +
    /// tid sequence — the `sendSonyOperation` seam PushLiveViewSession uses.
    func sendSonyOperation(_ opcode: UInt16, params: [UInt32], payload: Data) throws -> OperationResult {
        try sendOperation(opcode, params: params, dataOut: payload)
    }

    func shutdown() {
        listening = false
        livenessTimer?.cancel()
        livenessTimer = nil
        command.close()
        event.close()
    }
}
