import Foundation
import NMSSH

/// SSH bootstrap transport (docs/wire-protocol.md §3) — used only when CC17 reports SSH=ON.
///
/// The FX3 offers keyboard-interactive auth ONLY: every prompt is answered with the CC17
/// password verbatim. NMSSH (libssh2) provides that plus the raw session pointer we use to
/// open the two `direct-tcpip` channels PTP/IP needs (command + event → localhost:15740).
///
/// Lifecycle parity: SSH mode is bootstrap-only — the caller disconnects this tunnel right
/// after the PTP bootstrap returns, so no steady-state feature runs over SSH.
final class SshTunnel {

    static let port: UInt16 = 22
    static let connectTimeout: TimeInterval = 10

    private let session: NMSSHSession
    private let log = DyrectoLog.shared

    init(host: String, username: String, password: String) throws {
        session = NMSSHSession(host: host, port: Int(Self.port), andUsername: username)
        session.timeout = NSNumber(value: Self.connectTimeout)

        log.line(.conn, "SSH: connecting \(username)@\(host):\(Self.port)")
        session.connect()
        guard session.isConnected else {
            throw SshError.connectFailed
        }
        // Promiscuous host-key policy (accept the camera's key) is NMSSH default behavior —
        // no known_hosts checking unless explicitly enabled.

        // Keyboard-interactive ONLY; respond to every prompt with the CC17 password.
        session.authenticateByKeyboardInteractive { _ in password }
        guard session.isAuthorized else {
            session.disconnect()
            throw SshError.authFailed
        }
        log.line(.conn, "SSH: authenticated (keyboard-interactive)")
    }

    /// Opens a `direct-tcpip` channel to localhost:15740 on the camera and wraps it as a
    /// blocking PtpByteChannel. Uses the raw libssh2 session NMSSH exposes, because NMSSH's
    /// high-level channel types don't cover direct-tcpip.
    func openPtpChannel() throws -> PtpByteChannel {
        guard let raw = session.session else { throw SshError.noRawSession }
        guard let channel = libssh2_channel_direct_tcpip_ex(
            raw, "127.0.0.1", Int32(PtpIpClient.ptpPort), "127.0.0.1", 0) else {
            throw SshError.channelOpenFailed(lastError())
        }
        log.line(.conn, "SSH: direct-tcpip channel open → 127.0.0.1:\(PtpIpClient.ptpPort)")
        return SshByteChannel(session: session, channel: channel)
    }

    func disconnect() {
        session.disconnect()
        log.line(.conn, "SSH: disconnected (bootstrap-only lifecycle)")
    }

    private func lastError() -> String {
        guard let raw = session.session else { return "?" }
        var msg: UnsafeMutablePointer<CChar>? = nil
        var len: Int32 = 0
        libssh2_session_last_error(raw, &msg, &len, 0)
        return msg.map { String(cString: $0) } ?? "?"
    }

    enum SshError: Error, CustomStringConvertible {
        case connectFailed
        case authFailed
        case noRawSession
        case channelOpenFailed(String)

        var description: String {
            switch self {
            case .connectFailed: return "SSH connect failed"
            case .authFailed: return "SSH keyboard-interactive auth failed"
            case .noRawSession: return "NMSSH raw session unavailable"
            case .channelOpenFailed(let m): return "direct-tcpip open failed: \(m)"
            }
        }
    }
}

/// Blocking PtpByteChannel over a libssh2 direct-tcpip channel.
///
/// libssh2 channels are polled: reads spin with a short sleep honoring the caller deadline;
/// buffered bytes survive a timeout, matching the TCP channel's `readFully` contract. libssh2
/// sessions are NOT thread-safe — this channel serializes its own reads/writes; PtpIpClient's
/// ptpLock already serializes command traffic above us, and the event channel (its own
/// SshByteChannel over the SAME libssh2 session) shares `sessionLock` with the command channel.
final class SshByteChannel: PtpByteChannel {
    private let session: NMSSHSession
    private let channel: OpaquePointer
    private var buffer = Data()
    private var closed = false

    /// One lock per libssh2 SESSION, shared by both channels riding it.
    private static var sessionLocks = NSMapTable<NMSSHSession, NSRecursiveLock>.weakToStrongObjects()
    private let sessionLock: NSRecursiveLock

    init(session: NMSSHSession, channel: OpaquePointer) {
        self.session = session
        self.channel = channel
        if let existing = Self.sessionLocks.object(forKey: session) {
            sessionLock = existing
        } else {
            let lock = NSRecursiveLock()
            Self.sessionLocks.setObject(lock, forKey: session)
            sessionLock = lock
        }
        // Non-blocking mode so reads can honor deadlines.
        sessionLock.lock()
        if let raw = session.session { libssh2_session_set_blocking(raw, 0) }
        sessionLock.unlock()
    }

    func readFully(_ count: Int, deadline: TimeInterval?) throws -> Data {
        let end = deadline.map { Date().addingTimeInterval($0) }
        var scratch = [UInt8](repeating: 0, count: 64 * 1024)

        while buffer.count < count {
            if closed { throw PtpChannelError.closed }
            let n: Int = scratch.withUnsafeMutableBytes { ptr in
                sessionLock.lock()
                defer { sessionLock.unlock() }
                return libssh2_channel_read_ex(channel, 0, ptr.baseAddress!.assumingMemoryBound(to: CChar.self), ptr.count)
            }
            if n > 0 {
                buffer.append(contentsOf: scratch[0..<n])
            } else if n == Int(LIBSSH2_ERROR_EAGAIN) {
                if let end, Date() >= end { throw PtpChannelError.timeout }
                Thread.sleep(forTimeInterval: 0.01)
            } else if n == 0 {
                let eof = sessionLockedEof()
                if eof { throw PtpChannelError.eof }
                if let end, Date() >= end { throw PtpChannelError.timeout }
                Thread.sleep(forTimeInterval: 0.01)
            } else {
                throw PtpChannelError.network(NSError(domain: "libssh2", code: n))
            }
        }
        let out = buffer.prefix(count)
        buffer.removeFirst(count)
        return Data(out)
    }

    private func sessionLockedEof() -> Bool {
        sessionLock.lock()
        defer { sessionLock.unlock() }
        return libssh2_channel_eof(channel) == 1
    }

    func write(_ data: Data) throws {
        var offset = 0
        try data.withUnsafeBytes { (ptr: UnsafeRawBufferPointer) in
            while offset < data.count {
                if closed { throw PtpChannelError.closed }
                let n: Int = {
                    sessionLock.lock()
                    defer { sessionLock.unlock() }
                    return libssh2_channel_write_ex(
                        channel, 0,
                        ptr.baseAddress!.advanced(by: offset).assumingMemoryBound(to: CChar.self),
                        data.count - offset)
                }()
                if n > 0 {
                    offset += n
                } else if n == Int(LIBSSH2_ERROR_EAGAIN) {
                    Thread.sleep(forTimeInterval: 0.005)
                } else {
                    throw PtpChannelError.network(NSError(domain: "libssh2", code: n))
                }
            }
        }
    }

    func close() {
        guard !closed else { return }
        closed = true
        sessionLock.lock()
        libssh2_channel_close(channel)
        libssh2_channel_free(channel)
        sessionLock.unlock()
    }
}
