import Foundation

/// HTTP-pull live view (docs/wire-protocol.md §6) — the alternative to Push LV. URL comes from
/// telemetry prop 0xD278; in direct mode the host is replaced with the camera IP. Frames are
/// `16-byte LE header | padding | JPEG | metadata`.
final class LiveViewClient {

    static let maxFrame = 8_000_000
    private let log = DyrectoLog.shared
    private var channel: TcpByteChannel?
    private var running = false
    private let queue = DispatchQueue(label: "app.dyrecto.liveview.http")

    var onJpeg: ((Data) -> Void)?
    var onEnded: ((String) -> Void)?

    /// Gate on LiveViewStatus (0xD221): raw 0=disabled / 2=not-supported block; 1/absent proceed.
    static func isGateOpen(liveViewStatus: Int64?) -> Bool {
        switch liveViewStatus {
        case .some(0), .some(2): return false
        default: return true
        }
    }

    func start(liveViewUrl: String, cameraIp: String) {
        queue.async {
            self.running = true
            do {
                try self.run(liveViewUrl: liveViewUrl, cameraIp: cameraIp)
            } catch {
                self.log.line(.error, "LiveView: \(error)")
                self.onEnded?("\(error)")
            }
            self.running = false
        }
    }

    func stop() {
        running = false
        channel?.close()
    }

    private func run(liveViewUrl: String, cameraIp: String) throws {
        // Parse the camera-provided URL; direct mode overrides host, keeps port + path.
        guard let url = URL(string: liveViewUrl) else {
            throw BinaryIOError.malformed("bad LiveViewUrl: \(liveViewUrl)")
        }
        let host = cameraIp
        let port = UInt16(url.port ?? 80)
        var path = url.path.isEmpty ? "/" : url.path
        if let q = url.query { path += "?\(q)" }

        log.line(.conn, "LiveView: GET http://\(host):\(port)\(path)")
        let ch = try TcpByteChannel(host: host, port: port)
        channel = ch

        let request = "GET \(path) HTTP/1.1\r\nHost: \(host)\r\nConnection: keep-alive\r\n\r\n"
        try ch.write(Data(request.utf8))

        // Status line + headers, byte-by-byte until CRLFCRLF.
        var headerData = Data()
        while !headerData.suffix(4).elementsEqual([0x0D, 0x0A, 0x0D, 0x0A]) {
            headerData.append(try ch.readFully(1, deadline: 10))
            if headerData.count > 32 * 1024 { throw BinaryIOError.malformed("oversized HTTP header") }
        }
        let headerText = String(decoding: headerData, as: UTF8.self)
        guard headerText.hasPrefix("HTTP/"), headerText.contains(" 200") else {
            throw BinaryIOError.malformed("LiveView HTTP status: \(headerText.prefix(64))")
        }
        let chunked = headerText.lowercased().contains("transfer-encoding: chunked")
        log.line(.conn, "LiveView: 200 OK chunked=\(chunked)")

        let body: BodyReader = chunked
            ? ChunkedBodyReader(channel: ch)
            : PlainBodyReader(channel: ch)

        // Endless frame loop.
        while running {
            let header = try body.read(16)
            var r = BinaryReader(header)
            let offsetToImage = Int(try r.u32())
            let imageSize = Int(try r.u32())
            let offsetToMeta = Int(try r.u32())
            let metaSize = Int(try r.u32())

            guard offsetToImage >= 16, imageSize > 0, imageSize <= Self.maxFrame,
                  metaSize >= 0, metaSize <= Self.maxFrame else {
                throw BinaryIOError.malformed(
                    "LiveView frame header offImg=\(offsetToImage) img=\(imageSize) offMeta=\(offsetToMeta) meta=\(metaSize)")
            }
            let firstOffset = (offsetToMeta >= 1 && offsetToMeta < offsetToImage) ? offsetToMeta : offsetToImage
            let reserved = firstOffset - 16
            if reserved > 0 { _ = try body.read(reserved) }

            // If metadata precedes the image, consume it first (layout per header ordering).
            if firstOffset == offsetToMeta, metaSize > 0 {
                _ = try body.read(metaSize)
                let gap = offsetToImage - (offsetToMeta + metaSize)
                if gap > 0 { _ = try body.read(gap) }
                let jpeg = try body.read(imageSize)
                onJpeg?(jpeg)
            } else {
                let jpeg = try body.read(imageSize)
                onJpeg?(jpeg)
                if metaSize > 0 { _ = try body.read(metaSize) }
            }
        }
        onEnded?("stopped")
    }
}

// MARK: - Body readers

private protocol BodyReader {
    func read(_ count: Int) throws -> Data
}

private final class PlainBodyReader: BodyReader {
    let channel: TcpByteChannel
    init(channel: TcpByteChannel) { self.channel = channel }
    func read(_ count: Int) throws -> Data {
        try channel.readFully(count, deadline: 10)
    }
}

/// Manual de-chunking, matching the Android client's byte-level implementation.
private final class ChunkedBodyReader: BodyReader {
    let channel: TcpByteChannel
    private var remainingInChunk = 0

    init(channel: TcpByteChannel) { self.channel = channel }

    func read(_ count: Int) throws -> Data {
        var out = Data(capacity: count)
        while out.count < count {
            if remainingInChunk == 0 {
                try nextChunk()
            }
            let take = min(count - out.count, remainingInChunk)
            out.append(try channel.readFully(take, deadline: 10))
            remainingInChunk -= take
            if remainingInChunk == 0 {
                _ = try channel.readFully(2, deadline: 10) // trailing CRLF
            }
        }
        return out
    }

    private func nextChunk() throws {
        var line = Data()
        while !line.suffix(2).elementsEqual([0x0D, 0x0A]) {
            line.append(try channel.readFully(1, deadline: 10))
            if line.count > 64 { throw BinaryIOError.malformed("bad chunk size line") }
        }
        let text = String(decoding: line.dropLast(2), as: UTF8.self)
            .split(separator: ";").first.map(String.init) ?? ""
        guard let size = Int(text.trimmingCharacters(in: .whitespaces), radix: 16), size > 0 else {
            throw PtpChannelError.eof // zero-size chunk = end of stream
        }
        remainingInChunk = size
    }
}
