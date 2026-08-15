package app.dyrecto.liveview

import app.dyrecto.connection.BleLog
import app.dyrecto.connection.LiveViewClient
import app.dyrecto.connection.LiveViewTransport
import app.dyrecto.connection.LiveViewTransportProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.concurrent.thread

/**
 * Owns the Live View pipeline lifecycle: opening the transport (via [provider]), running the
 * [LiveViewClient] frame parser on a dedicated thread, and publishing renderer-neutral
 * [LiveViewState] (encoded JPEG frames). UI-agnostic by design — no Compose/Bitmap types here, so
 * the pipeline is reusable for GPU rendering, overlays, analysis, recording, or external displays.
 *
 * Newest-frame discipline: each parsed frame is published to a conflated [MutableStateFlow], so a
 * slow consumer (decoder/renderer) only ever sees the latest frame — no backlog.
 *
 * Threading mirrors the verified PTP event-listener model: a single owned thread does the blocking
 * I/O; [start]/[stop] are non-blocking and idempotent.
 */
class LiveViewSession(
    private val provider: LiveViewTransportProvider,
) {

    private val _state = MutableStateFlow(LiveViewState())
    val state: StateFlow<LiveViewState> = _state.asStateFlow()

    @Volatile private var client: LiveViewClient? = null
    @Volatile private var transport: LiveViewTransport? = null
    @Volatile private var worker: Thread? = null
    @Volatile private var generation = 0

    /** Starts streaming if not already running. Non-blocking; safe to call repeatedly. */
    @Synchronized
    fun start() {
        if (worker != null) {
            log("start ignored — already running")
            return
        }
        val gen = ++generation
        _state.value = LiveViewState(status = LiveViewState.Status.STARTING)
        worker = thread(name = "live-view") { runStream(gen) }
    }

    /** Stops streaming and releases the transport. Non-blocking; idempotent. */
    @Synchronized
    fun stop() {
        generation++ // invalidate any in-flight worker so it won't publish after stop
        val c = client; val t = transport; val w = worker
        client = null; transport = null; worker = null
        runCatching { c?.stop() }
        runCatching { t?.close() }
        runCatching { w?.interrupt() }
        _state.value = LiveViewState(status = LiveViewState.Status.IDLE)
    }

    private fun runStream(gen: Int) {
        val t = provider.openLiveView()
        if (t == null) {
            // provider already logged the precise reason under [LV]; surface it to the UI.
            if (gen == generation) {
                _state.value = LiveViewState(
                    status = LiveViewState.Status.ERROR,
                    error = provider.lastLiveViewError ?: "Live View unavailable",
                )
            }
            synchronized(this) { if (gen == generation) worker = null }
            return
        }
        transport = t
        var seq = 0L
        val c = LiveViewClient(
            input = t.input,
            output = t.output,
            host = t.host,
            path = t.path,
        ) { buf, offset, length ->
            if (gen != generation) return@LiveViewClient
            val jpeg = buf.copyOfRange(offset, offset + length) // copy: buffer is reused
            _state.value = LiveViewState(
                status = LiveViewState.Status.STREAMING,
                frame = LiveViewFrame(jpeg, seq++, System.currentTimeMillis()),
            )
        }
        client = c

        val started = runCatching { c.run() }.getOrDefault(false)

        // The stream ended (EOF/error) or was stopped. Only the live generation updates state.
        if (gen == generation) {
            _state.value = if (started && _state.value.status == LiveViewState.Status.STREAMING) {
                LiveViewState(status = LiveViewState.Status.ERROR, error = "Live View stream ended")
            } else if (!started) {
                LiveViewState(status = LiveViewState.Status.ERROR, error = "Live View failed to start")
            } else {
                LiveViewState(status = LiveViewState.Status.IDLE)
            }
        }
        runCatching { t.close() }
        synchronized(this) {
            if (gen == generation) {
                worker = null
                client = null
                transport = null
            }
        }
    }

    private fun log(m: String) = BleLog.line(BleLog.Kind.INFO, "[LV] $m")
}
