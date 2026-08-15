import Foundation
import Network
import DyrectoShared

/// Swift port of `PushLiveViewSession` — Sony SDIO_ControlMonitoring 0x9230 push live view
/// (docs/wire-protocol.md §5). Direct mode only; borrows the live PTP command channel via
/// `PtpIpClient.sendSonyOperation` (shared lock + tid sequence).
///
/// The camera dials back from its IP to our listening video port and pushes a VERIC-framed
/// JPEG stream (~816 KB/s) which is fed chunk-by-chunk into the shared `VericParser`.
final class PushLiveViewSession {

    static let keepAlivePeriod: TimeInterval = 15 // NOT 30 — the Android comment lies, the constant is 15
    static let acceptTimeout: TimeInterval = 8
    static let protocolVersion: UInt16 = 101
    static let deliveryTypeJpeg: UInt16 = 1
    static let qualityLevel3: UInt8 = 3
    static let protocolTcp: UInt16 = 2

    enum SubCommand: UInt32 {
        case start = 1, stop = 2, keepAlive = 3
    }

    /// Application-level result codes in responseParam[1] (rc 0x2001 only means "received").
    enum MonitoringResult: UInt32 {
        case ok = 0, systemError = 1, limitOver = 2, excluded = 3
        case otherTypeProcessing = 4, monitoringStopped = 5, invalidArgs = 6
        case highTemperature = 7, streaming = 8
    }

    struct Stats {
        var framesParsed = 0
        var bytesReceived: Int64 = 0
        var parseErrors = 0
        var lastKeepAliveResult: UInt32? = nil
    }

    private let ptp: PtpIpClient
    private let phoneIp: String
    private let log = DyrectoLog.shared

    /// Zero-copy frame sink — same contract as Android: the ref is valid only during the call.
    var onFrame: ((VericFrameRef) -> Void)?
    var onStats: ((Stats) -> Void)?
    var onEnded: ((String) -> Void)?

    private var videoListener: NWListener?
    private var metaListener: NWListener?
    private var videoConnection: NWConnection?
    private var metaConnection: NWConnection?
    private var deliveryId: UInt32?
    private var keepAliveTimer: DispatchSourceTimer?
    private var generation = 0
    private var stats = Stats()
    private let queue = DispatchQueue(label: "app.dyrecto.pushlv")

    init(ptp: PtpIpClient, phoneIp: String) {
        self.ptp = ptp
        self.phoneIp = phoneIp
    }

    // MARK: Start (non-blocking, idempotent via generation counter)

    func start() {
        queue.async { self.startLocked() }
    }

    private func startLocked() {
        generation += 1
        let gen = generation
        stats = Stats()

        do {
            let video = try makeListener()
            let meta = try makeListener()
            videoListener = video
            metaListener = meta

            let videoPort = video.port!.rawValue
            let metaPort = meta.port!.rawValue
            log.line(.conn, "PushLV: listening video=\(videoPort) meta=\(metaPort) on \(phoneIp)")

            acceptOne(on: video, label: "video", generation: gen) { [weak self] conn in
                self?.videoConnection = conn
                self?.drainVideo(conn, generation: gen)
            }
            acceptOne(on: meta, label: "meta", generation: gen) { [weak self] conn in
                self?.metaConnection = conn
                self?.drainMeta(conn, generation: gen)
            }

            // Start payload (wire-protocol.md §5.2): wrapper + one receiver record.
            var payload = BinaryWriter()
            payload.u16(Self.protocolVersion)
            payload.u16(0)
            payload.u32(1) // count
            let ipBytes = Data(phoneIp.utf8) + Data([0x00])
            payload.u16(UInt16(ipBytes.count))
            payload.raw(ipBytes)
            payload.u32(UInt32(videoPort))
            payload.u32(0) // audio
            payload.u32(UInt32(metaPort))
            payload.u16(Self.deliveryTypeJpeg)
            payload.u8(Self.qualityLevel3)
            payload.u16(Self.protocolTcp)

            let result = try ptp.sendSonyOperation(
                PtpIpClient.Op.sdioControlMonitoring,
                params: [SubCommand.start.rawValue],
                payload: payload.data)

            let appResult = result.responseParams.count >= 2 ? result.responseParams[1] : nil
            guard result.responseCode == PtpIpClient.Rc.ok, appResult == 0,
                  let id = result.responseParams.first else {
                let text = String(format: "PushLV start failed rc=0x%04X result=%@",
                                  result.responseCode, appResult.map(String.init) ?? "?")
                log.line(.error, text)
                teardown(reason: text, sendStop: false)
                return
            }
            deliveryId = id
            log.line(.conn, "PushLV: started, deliveryId=\(id)")
            scheduleKeepAlive(generation: gen)
        } catch {
            teardown(reason: "PushLV start error: \(error)", sendStop: false)
        }
    }

    private func makeListener() throws -> NWListener {
        let params = NWParameters.tcp
        params.requiredInterfaceType = .wifi
        params.requiredLocalEndpoint = NWEndpoint.hostPort(
            host: NWEndpoint.Host(phoneIp), port: .any)
        let listener = try NWListener(using: params)
        listener.start(queue: queue)
        return listener
    }

    private func acceptOne(on listener: NWListener, label: String, generation gen: Int,
                           handler: @escaping (NWConnection) -> Void) {
        var accepted = false
        listener.newConnectionHandler = { [weak self] conn in
            guard let self, self.generation == gen, !accepted else {
                conn.cancel()
                return
            }
            accepted = true
            self.log.line(.conn, "PushLV: \(label) connect-back from camera")
            conn.start(queue: self.queue)
            handler(conn)
        }
        // Accept timeout — Android parity (8 s): no dial-back means Start didn't take.
        queue.asyncAfter(deadline: .now() + Self.acceptTimeout) { [weak self] in
            guard let self, self.generation == gen, !accepted, label == "video" else { return }
            self.teardown(reason: "PushLV: no video connect-back within \(Int(Self.acceptTimeout)) s", sendStop: true)
        }
    }

    // MARK: Stream draining → shared VericParser

    private func drainVideo(_ conn: NWConnection, generation gen: Int) {
        // Shared VERIC parser: emits zero-copy frame refs; logs [VERIC] lines into our log.
        let parser = VericParser(
            onFrame: { [weak self] ref in
                guard let self else { return }
                self.stats.framesParsed += 1
                self.onFrame?(ref)
                if self.stats.framesParsed % 30 == 0 { self.onStats?(self.stats) }
            },
            verbose: false,
            clock: { KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)) },
            logLine: { [weak self] line in self?.log.line(.info, line) })

        func receive() {
            conn.receive(minimumIncompleteLength: 1, maximumLength: 64 * 1024) { [weak self] content, _, isComplete, error in
                guard let self, self.generation == gen else { return }
                if let content, !content.isEmpty {
                    self.stats.bytesReceived += Int64(content.count)
                    let bytes = KotlinByteArray.from(data: content)
                    parser.parse(chunk: bytes, len: Int32(content.count))
                }
                if isComplete {
                    self.teardown(reason: "PushLV: video EOF", sendStop: true)
                } else if let error {
                    self.teardown(reason: "PushLV: video error \(error)", sendStop: true)
                } else {
                    receive()
                }
            }
        }
        receive()
    }

    private func drainMeta(_ conn: NWConnection, generation gen: Int) {
        func receive() {
            conn.receive(minimumIncompleteLength: 1, maximumLength: 16 * 1024) { [weak self] content, _, isComplete, error in
                guard let self, self.generation == gen else { return }
                if let content, !content.isEmpty {
                    self.log.line(.info, "PushLV meta: \(content.count) B")
                }
                if !isComplete && error == nil { receive() }
            }
        }
        receive()
    }

    // MARK: KeepAlive (15 s, deliveryId payload — omitting it is the historic ~60 s death)

    private func scheduleKeepAlive(generation gen: Int) {
        let timer = DispatchSource.makeTimerSource(queue: queue)
        timer.schedule(deadline: .now() + Self.keepAlivePeriod, repeating: Self.keepAlivePeriod)
        timer.setEventHandler { [weak self] in
            guard let self, self.generation == gen, let id = self.deliveryId else { return }
            do {
                let result = try self.ptp.sendSonyOperation(
                    PtpIpClient.Op.sdioControlMonitoring,
                    params: [SubCommand.keepAlive.rawValue],
                    payload: Self.deliveryPayload(id))
                let appResult = result.responseParams.count >= 2 ? result.responseParams[1] : 999
                self.stats.lastKeepAliveResult = UInt32(appResult)
                if appResult != 0 {
                    self.log.line(.error, "PushLV keepalive result=\(appResult)")
                }
            } catch {
                self.log.line(.error, "PushLV keepalive failed: \(error)")
            }
        }
        timer.resume()
        keepAliveTimer = timer
    }

    static func deliveryPayload(_ deliveryId: UInt32) -> Data {
        var w = BinaryWriter()
        w.u16(protocolVersion)
        w.u16(0)
        w.u32(1)
        w.u32(deliveryId)
        return w.data
    }

    // MARK: Stop

    func stop() {
        queue.async { self.teardown(reason: "user stop", sendStop: true) }
    }

    private func teardown(reason: String, sendStop: Bool) {
        generation += 1
        keepAliveTimer?.cancel()
        keepAliveTimer = nil

        if sendStop, let id = deliveryId {
            do {
                let result = try ptp.sendSonyOperation(
                    PtpIpClient.Op.sdioControlMonitoring,
                    params: [SubCommand.stop.rawValue],
                    payload: Self.deliveryPayload(id))
                log.line(.conn, String(format: "PushLV stop rc=0x%04X result=%@",
                                       result.responseCode,
                                       result.responseParams.count >= 2 ? String(result.responseParams[1]) : "?"))
            } catch {
                log.line(.error, "PushLV stop send failed: \(error)")
            }
        }
        deliveryId = nil

        videoConnection?.cancel(); videoConnection = nil
        metaConnection?.cancel(); metaConnection = nil
        videoListener?.cancel(); videoListener = nil
        metaListener?.cancel(); metaListener = nil

        log.line(.conn, "PushLV teardown: \(reason)")
        onEnded?(reason)
    }
}
