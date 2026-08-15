package app.dyrecto.liveview.vision

import app.dyrecto.connection.BleLog
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Passive, newest-frame-wins analysis pipeline. Owns a single worker coroutine on its own
 * [CoroutineScope] (injected [dispatcher], default [Dispatchers.Default]) — it never runs on the
 * socket / parser / renderer / UI thread, and rendering always has priority.
 *
 * Backpressure discipline: there is exactly **one** pending slot, never a queue. [submit] overwrites
 * it; if it displaces a frame the worker hasn't taken yet, that older frame is counted as *dropped*.
 * The worker analyzes at most one frame at a time; frames arriving mid-analysis replace the pending
 * slot. This caps memory and latency: Vision falling behind can never slow or stall rendering.
 *
 * Module isolation: every [VisionModule.analyze] call is wrapped independently; a throwing or slow
 * module is logged (throttled) and skipped — it never stops the worker or its peers.
 *
 * [onResults] is called with the flat, ordered list of [VisionResult]s collected from all modules
 * after each frame pass. It runs on the pipeline's worker coroutine. The pipeline has no knowledge
 * of what [onResults] does — it is a generic callback provided by [VisionManager].
 */
class VisionPipeline(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val onResults: (List<VisionResult>) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /** Registered modules. Copy-on-write: safe to register/unregister during a worker iteration. */
    private val modules = CopyOnWriteArrayList<VisionModule>()

    /** The single pending slot (newest-wins). Never grows into a queue. */
    private val pending = AtomicReference<FrameAnalysisRequest?>(null)
    /** Conflated wake-up signal for the worker; multiple submits collapse to one. */
    private val wake = Channel<Unit>(Channel.CONFLATED)

    // ---- Counters (atomic; read into the published snapshot) ----
    private val framesReceived = AtomicLong(0)
    private val framesAnalyzed = AtomicLong(0)
    private val framesDropped = AtomicLong(0)

    @Volatile private var lastAnalysisDurationMs: Long = 0
    @Volatile private var averageAnalysisDurationMs: Double = 0.0
    @Volatile private var analysisFps: Double = 0.0
    @Volatile private var running: Boolean = true

    // Cadence/timing state (worker-thread only).
    private var lastAnalysisEndNs: Long = 0L
    private var lastLogAtMs: Long = 0L

    private val _stats = MutableStateFlow(VisionStatistics(pipelineRunning = true))
    val stats: StateFlow<VisionStatistics> = _stats.asStateFlow()

    init {
        scope.launch {
            for (signal in wake) {
                val req = pending.getAndSet(null) ?: continue
                runModules(req)
            }
        }
    }

    // ---- Public API ----

    fun register(module: VisionModule) {
        modules.addIfAbsent(module)
        publishStats()
    }

    fun unregister(module: VisionModule) {
        modules.remove(module)
        publishStats()
    }

    fun clear() {
        modules.clear()
        publishStats()
    }

    /**
     * Hand a frame to the pipeline. Non-blocking and safe to call from any thread (e.g. the
     * render-state collector). Overwrites the pending slot; a displaced un-taken frame is dropped.
     */
    fun submit(request: FrameAnalysisRequest) {
        framesReceived.incrementAndGet()
        val displaced = pending.getAndSet(request)
        if (displaced != null) framesDropped.incrementAndGet()
        wake.trySend(Unit)
        publishStats()
    }

    /** Stop the worker permanently. Idempotent. */
    fun shutdown() {
        running = false
        wake.close()
        scope.cancel()
        publishStats()
    }

    // ---- Worker internals ----

    private suspend fun runModules(req: FrameAnalysisRequest) {
        val startNs = System.nanoTime()
        val results = mutableListOf<VisionResult>()
        for (module in modules) {
            try {
                results += module.analyze(req)   // suspend; each module's list is appended (flatten)
            } catch (t: Throwable) {
                // Isolate: one faulty module never stops the worker or its peers.
                maybeLogModuleFailure(module, t)
            }
        }
        onResults(results)   // deliver flat list to VisionManager; pipeline has no knowledge of what happens next
        val endNs = System.nanoTime()

        val durationMs = (endNs - startNs) / 1_000_000
        lastAnalysisDurationMs = durationMs
        val n = framesAnalyzed.incrementAndGet()
        // Running average of pass duration.
        averageAnalysisDurationMs += (durationMs - averageAnalysisDurationMs) / n
        // Analysis throughput from inter-analysis cadence (EMA), independent of render FPS.
        if (lastAnalysisEndNs != 0L) {
            val intervalMs = (endNs - lastAnalysisEndNs) / 1_000_000.0
            if (intervalMs > 0) {
                val instantFps = 1000.0 / intervalMs
                analysisFps = if (analysisFps == 0.0) instantFps else analysisFps + 0.2 * (instantFps - analysisFps)
            }
        }
        lastAnalysisEndNs = endNs

        publishStats()
        maybeLog()
    }

    private fun publishStats() {
        _stats.value = VisionStatistics(
            framesReceived = framesReceived.get(),
            framesAnalyzed = framesAnalyzed.get(),
            framesDropped = framesDropped.get(),
            registeredModules = modules.size,
            lastAnalysisDurationMs = lastAnalysisDurationMs,
            averageAnalysisDurationMs = averageAnalysisDurationMs,
            analysisFps = analysisFps,
            pipelineRunning = running,
        )
    }

    /** Throttled to once per second — never per frame. */
    private fun maybeLog() {
        val now = System.currentTimeMillis()
        if (now - lastLogAtMs < 1000L) return
        lastLogAtMs = now
        val queueDepth = if (pending.get() != null) 1 else 0
        BleLog.line(
            BleLog.Kind.INFO,
            "[VISION] received=${framesReceived.get()} analyzed=${framesAnalyzed.get()}" +
                " dropped=${framesDropped.get()} modules=${modules.size}" +
                " analysisFps=%.1f avgMs=%.1f queue=$queueDepth".format(analysisFps, averageAnalysisDurationMs),
        )
    }

    private fun maybeLogModuleFailure(module: VisionModule, t: Throwable) {
        val now = System.currentTimeMillis()
        // Reuse the 1s throttle window so a continuously-throwing module can't flood the log.
        if (now - lastLogAtMs < 1000L) return
        lastLogAtMs = now
        BleLog.line(
            BleLog.Kind.ERROR,
            "[VISION] module '${module.id}' failed: ${t.javaClass.simpleName}: ${t.message}",
        )
    }
}
