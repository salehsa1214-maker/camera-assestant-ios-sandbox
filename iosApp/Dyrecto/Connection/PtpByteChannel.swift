import Foundation
import Network

/// Blocking byte-channel abstraction under `PtpIpClient` — one impl over a plain TCP socket
/// (direct mode) and one over an SSH direct-tcpip channel (SshTunnel). Mirrors the Android
/// client's InputStream/OutputStream model so the protocol code stays a line-by-line port.
protocol PtpByteChannel: AnyObject {
    /// Reads exactly `count` bytes or throws. `deadline` nil = block indefinitely (event
    /// channel); otherwise the overall budget for this read (bootstrap 10 s / steady 3 s).
    /// Timing out mid-packet must NOT lose buffered bytes (the Android `readFully` contract).
    func readFully(_ count: Int, deadline: TimeInterval?) throws -> Data
    func write(_ data: Data) throws
    func close()
}

enum PtpChannelError: Error, CustomStringConvertible {
    case timeout          // budget elapsed; buffered bytes retained
    case eof              // peer closed
    case closed           // local close
    case network(Error)

    var description: String {
        switch self {
        case .timeout: return "read timeout"
        case .eof: return "remote EOF"
        case .closed: return "channel closed"
        case .network(let e): return "network error: \(e)"
        }
    }
}

/// TCP implementation over Network.framework, pinned to the Wi-Fi interface.
///
/// NWConnection is async; the PTP client is deliberately synchronous-per-thread (Android
/// parity), so receives are pumped into an internal buffer and `readFully` blocks on a
/// condition variable. Partial data survives timeouts in `buffer` — the resync-safe behavior
/// the Android stack depends on.
final class TcpByteChannel: PtpByteChannel {
    private let connection: NWConnection
    private let lock = NSCondition()
    private var buffer = Data()
    private var failure: PtpChannelError?
    private var ready = false

    init(host: String, port: UInt16, connectTimeout: TimeInterval = 10) throws {
        let params = NWParameters.tcp
        params.requiredInterfaceType = .wifi
        if let tcp = params.defaultProtocolStack.transportProtocol as? NWProtocolTCP.Options {
            tcp.noDelay = true
            tcp.connectionTimeout = Int(connectTimeout)
        }
        connection = NWConnection(
            host: NWEndpoint.Host(host),
            port: NWEndpoint.Port(rawValue: port)!,
            using: params)

        connection.stateUpdateHandler = { [weak self] state in
            guard let self else { return }
            self.lock.lock()
            switch state {
            case .ready:
                self.ready = true
            case .failed(let e):
                self.failure = .network(e)
            case .cancelled:
                if self.failure == nil { self.failure = .closed }
            default:
                break
            }
            self.lock.broadcast()
            self.lock.unlock()
        }
        connection.start(queue: DispatchQueue(label: "app.dyrecto.tcp.\(port)"))
        pump()

        // Block until connected or failed (Android connect() semantics).
        lock.lock()
        let deadline = Date().addingTimeInterval(connectTimeout)
        while !ready && failure == nil {
            if !lock.wait(until: deadline) { break }
        }
        let failed = failure
        let ok = ready
        lock.unlock()
        if let failed { throw failed }
        if !ok { throw PtpChannelError.timeout }
    }

    private func pump() {
        connection.receive(minimumIncompleteLength: 1, maximumLength: 128 * 1024) { [weak self] content, _, isComplete, error in
            guard let self else { return }
            self.lock.lock()
            if let content, !content.isEmpty {
                self.buffer.append(content)
            }
            if isComplete {
                self.failure = self.failure ?? .eof
            } else if let error {
                self.failure = self.failure ?? .network(error)
            }
            self.lock.broadcast()
            let keepPumping = self.failure == nil
            self.lock.unlock()
            if keepPumping { self.pump() }
        }
    }

    func readFully(_ count: Int, deadline: TimeInterval?) throws -> Data {
        lock.lock()
        defer { lock.unlock() }
        let end = deadline.map { Date().addingTimeInterval($0) }
        while buffer.count < count {
            if let failure {
                // Deliver buffered bytes' shortfall as the failure (EOF/closed/network).
                throw failure
            }
            if let end {
                if Date() >= end { throw PtpChannelError.timeout }
                _ = lock.wait(until: min(end, Date().addingTimeInterval(2.0))) // 2 s poll, Android parity
            } else {
                lock.wait()
            }
        }
        let out = buffer.prefix(count)
        buffer.removeFirst(count)
        return Data(out)
    }

    func write(_ data: Data) throws {
        var sendError: PtpChannelError?
        let sem = DispatchSemaphore(value: 0)
        connection.send(content: data, completion: .contentProcessed { error in
            if let error { sendError = .network(error) }
            sem.signal()
        })
        sem.wait()
        if let sendError { throw sendError }
    }

    func close() {
        lock.lock()
        if failure == nil { failure = .closed }
        lock.broadcast()
        lock.unlock()
        connection.cancel()
    }
}
