package app.dyrecto.liveview.session

import android.graphics.Bitmap
import app.dyrecto.connection.BleLog
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.liveview.render.LiveViewRenderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 4D publisher: merges the two independent realtime streams — the render state (latest
 * displayed frame + metrics) and the connection state (latest telemetry + phase) — into one
 * "latest-wins" [FrameContext] flow.
 *
 * Rules (by design): never waits for matching timestamps, never blocks, never queues. Whenever
 * *either* source emits, a fresh immutable [FrameContext] is republished pairing the newest of each.
 *
 * Read-only over the existing pipeline: it only *collects* the supplied StateFlows. It does not own,
 * mutate, or copy any [Bitmap] — the renderer stays the sole bitmap owner (see [FrameContext]).
 */
class FrameContextPublisher(private val scope: CoroutineScope) {

    private val _context = MutableStateFlow(FrameContext())
    val context: StateFlow<FrameContext> = _context.asStateFlow()

    // Latest seen from each source; combined into every published context.
    @Volatile private var lastRender: LiveViewRenderState = LiveViewRenderState()
    @Volatile private var lastConn: CameraConnectionState = CameraConnectionState()

    // Frame-detection state. NOTE (temporary mechanism): new frames are detected by comparing the
    // renderer's Bitmap *reference identity* against the previously seen one. This is correct only
    // while the renderer allocates a fresh Bitmap per displayed frame. If the renderer later reuses
    // / pools bitmaps (same reference mutated in place), this will under-count — at that point switch
    // to an explicit renderer-published sequence number (e.g. surface RenderedFrame.index on
    // LiveViewRenderState). The bitmap reference below is used only for this comparison, never stored.
    @Volatile private var lastFrameRef: Bitmap? = null
    @Volatile private var renderSequence: Long = 0L
    @Volatile private var observedAtMs: Long = 0L

    @Volatile private var lastLogAtMs: Long = 0L

    /** Starts two independent collectors on [scope]; idempotent enough for one call at session build. */
    fun start(
        renderState: StateFlow<LiveViewRenderState>,
        connState: StateFlow<CameraConnectionState>,
    ) {
        scope.launch {
            renderState.collect { render ->
                val frame = render.frame
                if (frame !== lastFrameRef) {
                    lastFrameRef = frame
                    if (frame != null) {
                        renderSequence++
                        observedAtMs = System.currentTimeMillis()
                    }
                }
                lastRender = render
                publish()
            }
        }
        scope.launch {
            connState.collect { conn ->
                lastConn = conn
                publish()
            }
        }
    }

    private fun publish() {
        val render = lastRender
        val conn = lastConn
        val now = System.currentTimeMillis()
        val ctx = FrameContext(
            renderSequence = renderSequence,
            observedAtMs = observedAtMs,
            frameAgeMs = if (observedAtMs == 0L) 0L else now - observedAtMs,
            frameFps = render.displayedFps,
            latencyMs = render.avgLatencyMs,
            renderActive = render.active,
            telemetry = conn.telemetry,
            telemetryTimestampMs = conn.lastTelemetryUpdateAt ?: 0L,
            connectionPhase = conn.phase,
            liveViewActive = render.active,
        )
        _context.value = ctx
        maybeLog(ctx, now)
    }

    /** Throttled to once per second — never per frame. */
    private fun maybeLog(ctx: FrameContext, now: Long) {
        if (now - lastLogAtMs < 1000L) return
        lastLogAtMs = now
        val telAge = if (ctx.telemetryTimestampMs == 0L) -1L else now - ctx.telemetryTimestampMs
        BleLog.line(
            BleLog.Kind.INFO,
            "[SYNC] frame=${ctx.renderSequence} iso=${ctx.telemetry.display(CameraTelemetry.ISO)}" +
                " shutter=${ctx.telemetry.display(CameraTelemetry.SHUTTER)}" +
                " latency=%.1fms telemetryAge=${telAge}ms".format(ctx.latencyMs),
        )
    }
}

/** Compact display value for one telemetry property: decoded → raw → "—". */
internal fun CameraTelemetry?.display(code: Int): String {
    val prop = this?.get(code) ?: return "—"
    return prop.decoded ?: prop.rawValue ?: "—"
}
