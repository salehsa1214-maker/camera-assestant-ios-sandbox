import Foundation
import DyrectoShared

/// Phase 4D publisher — the iOS port of `FrameContextPublisher`: merges the two independent
/// realtime streams — the render state (latest displayed frame + metrics) and the connection
/// state (latest telemetry + phase) — into one "latest-wins" shared `FrameContext`.
///
/// Rules (by design): never waits for matching timestamps, never blocks, never queues. Whenever
/// *either* source emits, a fresh immutable FrameContext is republished pairing the newest of
/// each. Read-only over the existing pipeline — it never owns or copies a frame image.
///
/// Frame identity: where Android compared Bitmap references (documented there as a temporary
/// mechanism to be replaced by an explicit renderer sequence), the iOS renderer already publishes
/// an explicit `RenderedFrame.index` — the sanctioned replacement, with identical semantics.
final class FrameContextPublisherIos: ObservableObject {

    @Published private(set) var context: FrameContext = SharedFactory.emptyFrameContext()

    /// Latest context, callback + synchronous accessor for worker-thread consumers
    /// (Vision submit pairs frames with this without hopping to main).
    var onContext: ((FrameContext) -> Void)?

    private let lock = NSLock()
    private var latest: FrameContext = SharedFactory.emptyFrameContext()
    private let log = DyrectoLog.shared

    // Latest seen from each source; combined into every published context.
    private var lastRender = LiveViewRenderState()
    private var lastConn: CameraConnectionState?

    // Frame-detection state.
    private var lastFrameIndex: Int = -1
    private var renderSequence: Int64 = 0
    private var observedAtMs: Int64 = 0

    private var lastLogAtMs: Int64 = 0

    /// Thread-safe snapshot of the newest merged context.
    func current() -> FrameContext {
        lock.lock(); defer { lock.unlock() }
        return latest
    }

    /// Feed the newest render state (renderer decode queue or main — any thread).
    func onRenderState(_ render: LiveViewRenderState) {
        lock.lock()
        if let frame = render.frame, frame.index != lastFrameIndex {
            lastFrameIndex = frame.index
            renderSequence += 1
            observedAtMs = SharedFactory.nowMs()
        }
        if render.frame == nil { lastFrameIndex = -1 }
        lastRender = render
        lock.unlock()
        publish()
    }

    /// Feed the newest connection state (main thread from the controller — any thread accepted).
    func onConnectionState(_ conn: CameraConnectionState) {
        lock.lock()
        lastConn = conn
        lock.unlock()
        publish()
    }

    private func publish() {
        lock.lock()
        let render = lastRender
        let conn = lastConn
        let seq = renderSequence
        let observed = observedAtMs
        lock.unlock()

        let now = SharedFactory.nowMs()
        let ctx = FrameContext(
            renderSequence: seq,
            observedAtMs: observed,
            frameAgeMs: observed == 0 ? 0 : now - observed,
            frameFps: render.displayedFps,
            latencyMs: render.avgLatencyMs,
            renderActive: render.active,
            telemetry: conn?.telemetry,
            telemetryTimestampMs: conn?.lastTelemetryUpdateAt?.int64Value ?? 0,
            connectionPhase: conn?.phase ?? .idle,
            liveViewActive: render.active)

        lock.lock()
        latest = ctx
        lock.unlock()
        DispatchQueue.main.async { self.context = ctx }
        onContext?(ctx)
        maybeLog(ctx, now: now)
    }

    /// Throttled to once per second — never per frame.
    private func maybeLog(_ ctx: FrameContext, now: Int64) {
        lock.lock()
        if now - lastLogAtMs < 1000 { lock.unlock(); return }
        lastLogAtMs = now
        lock.unlock()

        let telAge: Int64 = ctx.telemetryTimestampMs == 0 ? -1 : now - ctx.telemetryTimestampMs
        // interop: CameraTelemetry companion const codes (ISO=0xD21E, SHUTTER=0xD20D) — passed as
        // literals because Kotlin companion const vals export unevenly across compiler versions.
        let iso = display(ctx.telemetry, code: 0xD21E)
        let shutter = display(ctx.telemetry, code: 0xD20D)
        log.line(.info, String(
            format: "[SYNC] frame=%d iso=%@ shutter=%@ latency=%.1fms telemetryAge=%dms",
            ctx.renderSequence, iso, shutter, ctx.latencyMs, telAge))
    }

    /// Compact display value for one telemetry property: decoded → raw → "—".
    private func display(_ telemetry: CameraTelemetry?, code: Int32) -> String {
        guard let prop = telemetry?.get(code: code) else { return "—" }
        return prop.decoded ?? prop.rawValue ?? "—"
    }
}
