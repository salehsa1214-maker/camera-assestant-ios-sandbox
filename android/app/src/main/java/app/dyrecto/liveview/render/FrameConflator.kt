package app.dyrecto.liveview.render

import java.util.concurrent.atomic.AtomicReference

/**
 * Android-free core of the live-view renderer: a single-slot "latest frame wins" buffer plus the
 * rolling diagnostic counters. Pure JVM logic so it is fully unit-testable without a real
 * [android.graphics.Bitmap] or Android runtime.
 *
 * The producer (parser/socket thread) calls [submit]; the consumer (decode coroutine) calls
 * [takeLatest] then reports back via [onDecoded] / [onDisplayed]. When a submit overwrites a buffer
 * that was never taken, that frame is counted as dropped — so the camera always wins on freshness
 * and we never let rendering latency grow unbounded.
 *
 * All counters are cumulative; [snapshot] computes windowed FPS from the deltas since the previous
 * call (the renderer calls it about once per second) and resets the per-window averages.
 */
class FrameConflator(private val clock: () -> Long = System::currentTimeMillis) {

    private val latest = AtomicReference<ByteArray?>(null)
    private val lock = Any()

    // Cumulative counters (guarded by [lock] except [latest]/[dropped] reads).
    private var received = 0L
    private var decoded = 0L
    private var displayed = 0L
    private var dropped = 0L

    // Per-window accumulators, reset on each [snapshot].
    private var windowStartMs = clock()
    private var windowStartReceived = 0L
    private var windowStartDecoded = 0L
    private var windowStartDisplayed = 0L
    private var decodeMsSum = 0L
    private var decodeMsCount = 0L
    private var latencyMsSum = 0L
    private var latencyMsCount = 0L

    /** Newest buffer awaiting decode, or null. Producer-cheap; useful for `queueDepth` logging. */
    val pending: Boolean get() = latest.get() != null

    /** Total frames dropped because decode fell behind (cumulative). */
    val droppedFrames: Long get() = synchronized(lock) { dropped }

    /** Store the newest frame, overwriting (and counting as dropped) any un-taken previous one. */
    fun submit(bytes: ByteArray) {
        val prev = latest.getAndSet(bytes)
        synchronized(lock) {
            received++
            if (prev != null) dropped++
        }
    }

    /** Take the newest buffer for decoding, clearing the slot. Returns null if nothing pending. */
    fun takeLatest(): ByteArray? = latest.getAndSet(null)

    /** Report a successful decode and its cost. */
    fun onDecoded(decodeMs: Long) = synchronized(lock) {
        decoded++
        decodeMsSum += decodeMs
        decodeMsCount++
    }

    /** Report a frame published to the UI and its submit→publish latency. */
    fun onDisplayed(latencyMs: Long) = synchronized(lock) {
        displayed++
        latencyMsSum += latencyMs
        latencyMsCount++
    }

    /** Windowed metrics since the previous call; resets the per-window averages. */
    fun snapshot(): Metrics = synchronized(lock) {
        val now = clock()
        val dtMs = (now - windowStartMs).coerceAtLeast(1)
        val m = Metrics(
            receivedFps = (received - windowStartReceived) * 1000.0 / dtMs,
            decodedFps = (decoded - windowStartDecoded) * 1000.0 / dtMs,
            displayedFps = (displayed - windowStartDisplayed) * 1000.0 / dtMs,
            droppedFrames = dropped,
            avgDecodeMs = if (decodeMsCount > 0) decodeMsSum.toDouble() / decodeMsCount else 0.0,
            avgLatencyMs = if (latencyMsCount > 0) latencyMsSum.toDouble() / latencyMsCount else 0.0,
        )
        windowStartMs = now
        windowStartReceived = received
        windowStartDecoded = decoded
        windowStartDisplayed = displayed
        decodeMsSum = 0; decodeMsCount = 0
        latencyMsSum = 0; latencyMsCount = 0
        m
    }

    /** Drop the pending buffer and zero all counters (used on start/stop). */
    fun reset() {
        latest.set(null)
        synchronized(lock) {
            received = 0; decoded = 0; displayed = 0; dropped = 0
            windowStartMs = clock()
            windowStartReceived = 0; windowStartDecoded = 0; windowStartDisplayed = 0
            decodeMsSum = 0; decodeMsCount = 0
            latencyMsSum = 0; latencyMsCount = 0
        }
    }

    data class Metrics(
        val receivedFps: Double,
        val decodedFps: Double,
        val displayedFps: Double,
        val droppedFrames: Long,
        val avgDecodeMs: Double,
        val avgLatencyMs: Double,
    )
}
