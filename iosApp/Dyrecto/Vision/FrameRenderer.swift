import CoreGraphics
import Foundation
import UIKit
import DyrectoShared

// MARK: - RenderedFrame (port of liveview/render/RenderedFrame.kt)

/// A decoded, retainable live-view frame. Deliberately holds a raw `UIImage` (never a SwiftUI
/// type) so the renderer stays UI-agnostic and can later feed a Metal/CALayer path unchanged.
struct RenderedFrame {
    /// The decoded image.
    let image: UIImage
    /// Emission order within the renderer's lifetime.
    let index: Int
    /// Decoded pixel width.
    let width: Int
    /// Decoded pixel height.
    let height: Int
    /// Wall-clock cost of decoding this frame, ms.
    let decodeMs: Int64
    /// Wall-clock when the frame was published, ms.
    let producedAtMs: Int64
}

// MARK: - LiveViewRenderState (port of liveview/render/LiveViewRenderState.kt)

/// UI-facing snapshot published by [LiveViewFrameRenderer]: latest frame + diagnostic metrics.
struct LiveViewRenderState {
    var frame: RenderedFrame? = nil
    var active: Bool = false
    var receivedFps: Double = 0.0
    var decodedFps: Double = 0.0
    var displayedFps: Double = 0.0
    var droppedFrames: Int64 = 0
    var avgDecodeMs: Double = 0.0
    var avgLatencyMs: Double = 0.0
}

// MARK: - FrameConflator (port of liveview/render/FrameConflator.kt)

/// Single-slot "latest frame wins" buffer plus rolling diagnostic counters — a 1:1 port of the
/// Android pure-JVM core. The producer (socket/parser thread) calls [submit]; the consumer
/// (decode queue) calls [takeLatest] then reports back via [onDecoded] / [onDisplayed]. When a
/// submit overwrites a buffer that was never taken, that frame counts as dropped — the camera
/// always wins on freshness and rendering latency can never grow unbounded.
final class FrameConflator {

    struct Metrics {
        let receivedFps: Double
        let decodedFps: Double
        let displayedFps: Double
        let droppedFrames: Int64
        let avgDecodeMs: Double
        let avgLatencyMs: Double
    }

    private let clock: () -> Int64
    private let lock = NSLock()

    private var latest: Data?

    // Cumulative counters (all guarded by [lock]).
    private var received: Int64 = 0
    private var decoded: Int64 = 0
    private var displayed: Int64 = 0
    private var dropped: Int64 = 0

    // Per-window accumulators, reset on each [snapshot].
    private var windowStartMs: Int64
    private var windowStartReceived: Int64 = 0
    private var windowStartDecoded: Int64 = 0
    private var windowStartDisplayed: Int64 = 0
    private var decodeMsSum: Int64 = 0
    private var decodeMsCount: Int64 = 0
    private var latencyMsSum: Int64 = 0
    private var latencyMsCount: Int64 = 0

    init(clock: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }) {
        self.clock = clock
        self.windowStartMs = clock()
    }

    /// Newest buffer awaiting decode present? Producer-cheap; used for `queueDepth` logging.
    var pending: Bool {
        lock.lock(); defer { lock.unlock() }
        return latest != nil
    }

    /// Total frames dropped because decode fell behind (cumulative).
    var droppedFrames: Int64 {
        lock.lock(); defer { lock.unlock() }
        return dropped
    }

    /// Store the newest frame, overwriting (and counting as dropped) any un-taken previous one.
    func submit(_ bytes: Data) {
        lock.lock(); defer { lock.unlock() }
        let prev = latest
        latest = bytes
        received += 1
        if prev != nil { dropped += 1 }
    }

    /// Take the newest buffer for decoding, clearing the slot. Nil if nothing pending.
    func takeLatest() -> Data? {
        lock.lock(); defer { lock.unlock() }
        let out = latest
        latest = nil
        return out
    }

    /// Report a successful decode and its cost.
    func onDecoded(decodeMs: Int64) {
        lock.lock(); defer { lock.unlock() }
        decoded += 1
        decodeMsSum += decodeMs
        decodeMsCount += 1
    }

    /// Report a frame published to the UI and its submit→publish latency.
    func onDisplayed(latencyMs: Int64) {
        lock.lock(); defer { lock.unlock() }
        displayed += 1
        latencyMsSum += latencyMs
        latencyMsCount += 1
    }

    /// Windowed metrics since the previous call; resets the per-window averages.
    func snapshot() -> Metrics {
        lock.lock(); defer { lock.unlock() }
        let now = clock()
        let dtMs = max(now - windowStartMs, 1)
        let m = Metrics(
            receivedFps: Double(received - windowStartReceived) * 1000.0 / Double(dtMs),
            decodedFps: Double(decoded - windowStartDecoded) * 1000.0 / Double(dtMs),
            displayedFps: Double(displayed - windowStartDisplayed) * 1000.0 / Double(dtMs),
            droppedFrames: dropped,
            avgDecodeMs: decodeMsCount > 0 ? Double(decodeMsSum) / Double(decodeMsCount) : 0.0,
            avgLatencyMs: latencyMsCount > 0 ? Double(latencyMsSum) / Double(latencyMsCount) : 0.0)
        windowStartMs = now
        windowStartReceived = received
        windowStartDecoded = decoded
        windowStartDisplayed = displayed
        decodeMsSum = 0; decodeMsCount = 0
        latencyMsSum = 0; latencyMsCount = 0
        return m
    }

    /// Drop the pending buffer and zero all counters (used on start/stop).
    func reset() {
        lock.lock(); defer { lock.unlock() }
        latest = nil
        received = 0; decoded = 0; displayed = 0; dropped = 0
        windowStartMs = clock()
        windowStartReceived = 0; windowStartDecoded = 0; windowStartDisplayed = 0
        decodeMsSum = 0; decodeMsCount = 0
        latencyMsSum = 0; latencyMsCount = 0
    }
}

// MARK: - LiveViewFrameRenderer (port of liveview/render/LiveViewFrameRenderer.kt)

/// Decodes pushed JPEG frames off-main and publishes the newest as a [LiveViewRenderState].
///
/// Pipeline boundary: this class knows nothing about PTP/VERIC/sockets/telemetry — it receives
/// already-extracted JPEG bytes via [submit] (a `VericFrameRef`, from which it copies the JPEG out
/// of the parser's borrowed buffer synchronously) or [submitJpeg] (the HTTP-pull path) and exposes
/// decoded `UIImage`s. Freshness over completeness: [submit] never blocks the socket thread; a
/// single decode worker always decodes the *latest* pending frame ([FrameConflator]) and frames
/// arriving mid-decode are dropped, never queued.
///
/// [onFrame] is the Vision tap: called on the decode queue with every newly published frame.
final class LiveViewFrameRenderer: ObservableObject {

    private static let metricsPeriodMs: Int64 = 1_000  // Android METRICS_PERIOD_MS
    private static let latencySpikeMs: Double = 500.0  // Android LATENCY_SPIKE_MS

    @Published private(set) var state = LiveViewRenderState()

    /// Vision tap: every newly published frame (decode-queue thread). Read-only observer.
    var onFrame: ((RenderedFrame) -> Void)?

    private let conflator: FrameConflator
    private let clock: () -> Int64
    private let log = DyrectoLog.shared
    private let decodeQueue = DispatchQueue(label: "app.dyrecto.render.decode", qos: .userInitiated)

    private let lock = NSLock()
    private var running = false
    private var decodeScheduled = false
    private var frameIndex = 0
    private var lastSubmitMs: Int64 = 0
    private var metricsTimer: DispatchSourceTimer?
    private var lastDropped: Int64 = 0

    init(clock: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }) {
        self.clock = clock
        self.conflator = FrameConflator(clock: clock)
    }

    // MARK: Lifecycle

    /// Starts the decode loop + metrics. Non-blocking; idempotent.
    func start() {
        lock.lock()
        if running { lock.unlock(); return }
        running = true
        frameIndex = 0
        lock.unlock()

        conflator.reset()
        publish { _ in LiveViewRenderState(active: true) }
        log.line(.info, "[RENDER] started")

        let timer = DispatchSource.makeTimerSource(queue: decodeQueue)
        timer.schedule(deadline: .now() + Double(Self.metricsPeriodMs) / 1000.0,
                       repeating: Double(Self.metricsPeriodMs) / 1000.0)
        timer.setEventHandler { [weak self] in self?.publishMetrics() }
        timer.resume()
        lock.lock(); metricsTimer = timer; lastDropped = 0; lock.unlock()
    }

    /// Stops the decode loop, clears the frame, resets metrics. Non-blocking; idempotent.
    func stop() {
        lock.lock()
        if !running { lock.unlock(); return }
        running = false
        let timer = metricsTimer
        metricsTimer = nil
        lock.unlock()

        timer?.cancel()
        conflator.reset()
        publish { _ in LiveViewRenderState(active: false) }
        log.line(.info, "[RENDER] stopped")
    }

    // MARK: Frame intake

    /// Hand a freshly parsed VERIC frame to the renderer. Called synchronously on the socket
    /// thread: copies the JPEG out of the parser's borrowed buffer (valid only for this call),
    /// stores it as the latest pending frame, and wakes the decode worker. Never blocks.
    func submit(ref: VericFrameRef) {
        lock.lock()
        let active = running
        lock.unlock()
        guard active else { return }
        // Zero-copy contract: materialize the JPEG bytes NOW, before the parser reuses its
        // buffer — via the iosMain bulk bridge (ONE native copy; never per-byte get(index:)).
        let jpeg = ref.jpegNSData() as Data
        submitJpeg(jpeg)
    }

    /// HTTP-pull path: the LiveViewClient already hands us an owned JPEG Data.
    func submitJpeg(_ jpeg: Data) {
        lock.lock()
        guard running else { lock.unlock(); return }
        lastSubmitMs = clock()
        lock.unlock()

        conflator.submit(jpeg)
        wakeDecoder()
    }

    /// Conflated wakeup: at most one drain pass scheduled at a time (Channel.CONFLATED parity).
    private func wakeDecoder() {
        lock.lock()
        if decodeScheduled || !running { lock.unlock(); return }
        decodeScheduled = true
        lock.unlock()
        decodeQueue.async { [weak self] in self?.drainLoop() }
    }

    private func drainLoop() {
        while true {
            lock.lock()
            guard running else { decodeScheduled = false; lock.unlock(); return }
            let submitMs = lastSubmitMs
            lock.unlock()

            guard let jpeg = conflator.takeLatest() else {
                lock.lock(); decodeScheduled = false; lock.unlock()
                // Re-check: a submit may have landed between takeLatest and the flag clear.
                if conflator.pending { wakeDecoder() }
                return
            }

            let t0 = clock()
            guard let image = UIImage(data: jpeg) else {
                log.line(.error, "[RENDER] decode failed (jpeg=\(jpeg.count)B)")
                continue
            }
            let decodeMs = clock() - t0
            conflator.onDecoded(decodeMs: decodeMs)
            let now = clock()

            lock.lock()
            let index = frameIndex
            frameIndex += 1
            lock.unlock()

            let width = Int(image.size.width * image.scale)
            let height = Int(image.size.height * image.scale)
            let frame = RenderedFrame(
                image: image, index: index, width: width, height: height,
                decodeMs: decodeMs, producedAtMs: now)
            publish { st in
                var next = st
                next.frame = frame
                return next
            }
            onFrame?(frame)
            conflator.onDisplayed(latencyMs: now - submitMs)
        }
    }

    // MARK: Metrics (1 s cadence — Android metricsLoop parity)

    private func publishMetrics() {
        let m = conflator.snapshot()
        publish { st in
            var next = st
            next.receivedFps = m.receivedFps
            next.decodedFps = m.decodedFps
            next.displayedFps = m.displayedFps
            next.droppedFrames = m.droppedFrames
            next.avgDecodeMs = m.avgDecodeMs
            next.avgLatencyMs = m.avgLatencyMs
            return next
        }
        let queueDepth = conflator.pending ? 1 : 0
        log.line(.info, String(
            format: "[RENDER] recvFPS=%.1f decodeFPS=%.1f displayFPS=%.1f drops=%d avgDecodeMs=%.1f avgLatencyMs=%.1f queueDepth=%d",
            m.receivedFps, m.decodedFps, m.displayedFps, m.droppedFrames,
            m.avgDecodeMs, m.avgLatencyMs, queueDepth))

        // ---- Exceptional events (immediate, not throttled) ----
        if m.receivedFps > 1.0 && m.decodedFps < 0.5 {
            log.line(.error, "[RENDER] stall - receiving but not decoding")
        }
        let dropDelta = m.droppedFrames - lastDropped
        if dropDelta >= 5 && Double(dropDelta) > m.receivedFps * 0.5 {
            log.line(.info, "[RENDER] dropped-frame burst: \(dropDelta) in last window")
        }
        if m.avgLatencyMs > Self.latencySpikeMs {
            log.line(.info, String(format: "[RENDER] latency spike avgLatencyMs=%.1f", m.avgLatencyMs))
        }
        lastDropped = m.droppedFrames
    }

    private func publish(_ transform: @escaping (LiveViewRenderState) -> LiveViewRenderState) {
        DispatchQueue.main.async {
            self.state = transform(self.state)
        }
    }
}

// MARK: - RGBA8888 extraction (the ONE draw per analyzed frame)

/// Draws a decoded frame into an RGBA8888 buffer exactly once per analyzed frame and hands the
/// bytes over as `NSData` — the buffer `IosExposureModule.analyze(rgbaData:width:height:)` and
/// `RgbaFramePixels.fromNSData` consume (byteOrder32Big + premultipliedLast ⇒ byte order R,G,B,A,
/// which is what the shared Kotlin conversion loop expects).
enum RgbaFrameBuffer {

    struct Extracted {
        let data: NSData
        let width: Int
        let height: Int
    }

    /// One CGContext draw of the full image at native pixel size. Returns nil on any CG failure
    /// (the analyzed frame is then simply skipped — Android's recycled-bitmap parity).
    static func extract(from image: UIImage) -> Extracted? {
        guard let cg = image.cgImage else { return nil }
        let width = cg.width
        let height = cg.height
        guard width > 0, height > 0 else { return nil }

        let byteCount = width * height * 4
        guard let data = NSMutableData(length: byteCount) else { return nil }
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        guard let ctx = CGContext(
            data: data.mutableBytes, width: width, height: height,
            bitsPerComponent: 8, bytesPerRow: width * 4, space: colorSpace,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue | CGBitmapInfo.byteOrder32Big.rawValue)
        else { return nil }
        ctx.interpolationQuality = .none // native size, no resample
        ctx.draw(cg, in: CGRect(x: 0, y: 0, width: width, height: height))
        return Extracted(data: data, width: width, height: height)
    }

    /// Scaled variant (longest edge capped) — used by the AI inference copy and reference import.
    static func extractScaled(from image: UIImage, maxEdge: Int) -> Extracted? {
        guard let cg = image.cgImage else { return nil }
        let srcW = cg.width, srcH = cg.height
        guard srcW > 0, srcH > 0 else { return nil }
        let scale = CGFloat(maxEdge) / CGFloat(max(srcW, srcH))
        let width = scale < 1 ? max(1, Int((CGFloat(srcW) * scale).rounded())) : srcW
        let height = scale < 1 ? max(1, Int((CGFloat(srcH) * scale).rounded())) : srcH

        let byteCount = width * height * 4
        guard let data = NSMutableData(length: byteCount) else { return nil }
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        guard let ctx = CGContext(
            data: data.mutableBytes, width: width, height: height,
            bitsPerComponent: 8, bytesPerRow: width * 4, space: colorSpace,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue | CGBitmapInfo.byteOrder32Big.rawValue)
        else { return nil }
        ctx.interpolationQuality = .medium // bilinear-ish — createScaledBitmap(filter=true) parity
        ctx.draw(cg, in: CGRect(x: 0, y: 0, width: width, height: height))
        return Extracted(data: data, width: width, height: height)
    }
}

// MARK: - KotlinByteArray → Data

extension Data {
    /// Copies a slice of a Kotlin ByteArray. Used to materialize the VERIC JPEG synchronously
    /// while the parser's borrowed buffer is still valid.
    // interop: KotlinByteArray exposes only per-element get(index:) to Swift; this loop is the
    // JPEG-sized (tens of KB) exception to the "no per-element bridging" rule until a bulk
    // toNSData bridge is added to shared iosMain (verify cost on first Mac profile).
    init(kotlinBytes: KotlinByteArray, offset: Int, length: Int) {
        var bytes = [UInt8](repeating: 0, count: Swift.max(length, 0))
        for i in 0..<Swift.max(length, 0) {
            bytes[i] = UInt8(bitPattern: kotlinBytes.get(index: Int32(offset + i)))
        }
        self.init(bytes)
    }
}
