package app.dyrecto.liveview.vision

/**
 * Immutable snapshot of [VisionPipeline] health, published through a StateFlow.
 *
 * All fields are first-class (not log-only) so the Developer UI, diagnostics, and future tooling
 * can consume them directly.
 */
data class VisionStatistics(
    /** Frames handed to the pipeline via submit (includes ones later dropped). */
    val framesReceived: Long = 0,
    /** Frames actually run through the module pass. */
    val framesAnalyzed: Long = 0,
    /** Frames discarded by newest-wins conflation (displaced before the worker took them). */
    val framesDropped: Long = 0,
    /** Number of currently registered modules. */
    val registeredModules: Int = 0,
    /** Wall-clock duration of the most recent module pass, in ms. */
    val lastAnalysisDurationMs: Long = 0,
    /** Running average module-pass duration, in ms. */
    val averageAnalysisDurationMs: Double = 0.0,
    /** Recent analysis throughput (analyses per second). Derived from worker cadence. */
    val analysisFps: Double = 0.0,
    /** True while the worker is alive (false after shutdown). */
    val pipelineRunning: Boolean = false,
)
