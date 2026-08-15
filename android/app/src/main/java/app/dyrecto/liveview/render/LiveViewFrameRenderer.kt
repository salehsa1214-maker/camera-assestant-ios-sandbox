package app.dyrecto.liveview.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.dyrecto.connection.BleLog
import app.dyrecto.liveview.veric.VericFrameRef
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/**
 * Decodes pushed JPEG frames and publishes the newest as a [LiveViewRenderState] for the UI.
 *
 * Pipeline boundary: this class knows nothing about PTP/VERIC/sockets/telemetry — it only receives
 * already-extracted JPEG bytes via [submit] (a [VericFrameRef], from which it copies the JPEG out
 * of the parser's borrowed buffer) and exposes decoded [android.graphics.Bitmap]s. It is UI-agnostic
 * (no Compose types), so a future SurfaceView/OpenGL path is a drop-in replacement.
 *
 * Freshness over completeness: [submit] never blocks the parser thread; a single decode coroutine
 * always decodes the *latest* pending frame ([FrameConflator]), and frames that arrive while a
 * decode is in flight are dropped rather than queued — render latency can never grow unbounded.
 *
 * The decoder and dispatcher are injectable so the core logic can run off the Android main thread
 * (and, with a fake decoder, off a real device).
 *
 * @param dispatcher coroutine dispatcher for decoding (default [Dispatchers.Default]).
 * @param clock      time source (injectable for tests/metrics).
 * @param decode     JPEG → Bitmap decoder (default [BitmapFactory]); returns null on failure.
 */
class LiveViewFrameRenderer(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: () -> Long = System::currentTimeMillis,
    private val decode: (ByteArray) -> Bitmap? = { BitmapFactory.decodeByteArray(it, 0, it.size) },
) {
    private val conflator = FrameConflator(clock)
    private val _state = MutableStateFlow(LiveViewRenderState())
    val state: StateFlow<LiveViewRenderState> = _state.asStateFlow()

    private val lastSubmitMs = AtomicLong(0)
    @Volatile private var frameIndex = 0

    @Volatile private var scope: CoroutineScope? = null
    @Volatile private var wakeups: Channel<Unit>? = null

    /** Starts the decode loop. Non-blocking; idempotent. */
    @Synchronized
    fun start() {
        if (scope != null) return
        conflator.reset()
        frameIndex = 0
        val ch = Channel<Unit>(Channel.CONFLATED)
        val sc = CoroutineScope(dispatcher + SupervisorJob())
        wakeups = ch
        scope = sc
        _state.value = LiveViewRenderState(active = true)
        BleLog.line(BleLog.Kind.INFO, "[RENDER] started")
        sc.launch { decodeLoop(ch) }
        sc.launch { metricsLoop() }
    }

    /** Stops the decode loop, clears the frame, resets metrics. Non-blocking; idempotent. */
    @Synchronized
    fun stop() {
        val sc = scope ?: return
        wakeups?.close()
        wakeups = null
        scope = null
        sc.cancel()
        conflator.reset()
        _state.value = LiveViewRenderState(active = false)
        BleLog.line(BleLog.Kind.INFO, "[RENDER] stopped")
    }

    /**
     * Hand a freshly parsed frame to the renderer. Called synchronously on the parser/socket thread:
     * copies the JPEG out of the borrowed buffer (valid only for this call), stores it as the latest
     * pending frame, and wakes the decode coroutine. Never blocks or suspends.
     */
    fun submit(ref: VericFrameRef) {
        val ch = wakeups ?: return // not started; ignore
        val jpeg = ref.buf.copyOfRange(ref.jpegOffset, ref.jpegOffset + ref.jpegLength)
        lastSubmitMs.set(clock())
        conflator.submit(jpeg)
        ch.trySend(Unit) // CONFLATED: coalesces with any pending wakeup
    }

    /**
     * Hand an already-extracted JPEG frame to the renderer (e.g. from the HTTP Live View path, whose
     * [LiveViewClient] emits the JPEG bytes directly). Same freshness/conflation discipline as the
     * [VericFrameRef] overload; never blocks or suspends. [jpeg] must not be mutated after this call.
     */
    fun submit(jpeg: ByteArray) {
        val ch = wakeups ?: return // not started; ignore
        lastSubmitMs.set(clock())
        conflator.submit(jpeg)
        ch.trySend(Unit)
    }

    private suspend fun decodeLoop(ch: Channel<Unit>) {
        for (signal in ch) {
            val jpeg = conflator.takeLatest() ?: continue
            val submitMs = lastSubmitMs.get()
            val t0 = clock()
            val bitmap = runCatching { decode(jpeg) }.getOrNull()
            val decodeMs = clock() - t0
            if (bitmap == null) {
                BleLog.line(BleLog.Kind.ERROR, "[RENDER] decode failed (jpeg=${jpeg.size}B)")
                continue
            }
            conflator.onDecoded(decodeMs)
            val now = clock()
            val frame = RenderedFrame(bitmap, frameIndex++, bitmap.width, bitmap.height, decodeMs, now)
            _state.update { it.copy(frame = frame.bitmap) }
            conflator.onDisplayed(now - submitMs)
        }
    }

    private suspend fun metricsLoop() {
        var lastDropped = conflator.droppedFrames
        while (currentCoroutineContext().isActive) {
            delay(METRICS_PERIOD_MS)
            val m = conflator.snapshot()
            _state.update {
                it.copy(
                    receivedFps = m.receivedFps,
                    decodedFps = m.decodedFps,
                    displayedFps = m.displayedFps,
                    droppedFrames = m.droppedFrames,
                    avgDecodeMs = m.avgDecodeMs,
                    avgLatencyMs = m.avgLatencyMs,
                )
            }
            val queueDepth = if (conflator.pending) 1 else 0
            val rt = Runtime.getRuntime()
            val memMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
            BleLog.line(
                BleLog.Kind.INFO,
                "[RENDER] recvFPS=%.1f decodeFPS=%.1f displayFPS=%.1f drops=%d avgDecodeMs=%.1f avgLatencyMs=%.1f queueDepth=%d memoryMB=%d"
                    .format(
                        m.receivedFps, m.decodedFps, m.displayedFps, m.droppedFrames,
                        m.avgDecodeMs, m.avgLatencyMs, queueDepth, memMB,
                    ),
            )
            // ---- Exceptional events (immediate, not throttled) ----
            if (m.receivedFps > 1.0 && m.decodedFps < 0.5) {
                BleLog.line(BleLog.Kind.ERROR, "[RENDER] stall - receiving but not decoding")
            }
            val dropDelta = m.droppedFrames - lastDropped
            if (dropDelta >= 5 && dropDelta > m.receivedFps * 0.5) {
                BleLog.line(BleLog.Kind.INFO, "[RENDER] dropped-frame burst: $dropDelta in last window")
            }
            if (m.avgLatencyMs > LATENCY_SPIKE_MS) {
                BleLog.line(BleLog.Kind.INFO, "[RENDER] latency spike avgLatencyMs=%.1f".format(m.avgLatencyMs))
            }
            lastDropped = m.droppedFrames
        }
    }

    companion object {
        private const val METRICS_PERIOD_MS = 1_000L
        private const val LATENCY_SPIKE_MS = 500.0
    }
}
