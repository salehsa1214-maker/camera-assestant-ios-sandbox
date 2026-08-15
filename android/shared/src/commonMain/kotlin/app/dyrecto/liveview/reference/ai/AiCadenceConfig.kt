package app.dyrecto.liveview.reference.ai

/**
 * Adaptive-execution cadence for live-frame AI (Phase 10). All values are in ANALYZED frames
 * (the vision pipeline's newest-wins output rate), never wall-clock timers.
 *
 * Detection has no cadence entry on purpose: the detector runs only on TrackingManager's
 * trigger policy (init / loss recovery / scene change / periodic verify), not on a schedule.
 */
data class AiCadenceConfig(
    /** Run a tracker update every Nth analyzed frame. */
    val trackerEveryNFrames: Int = 2,
    /** Refresh the global embedding every Nth analyzed frame. */
    val embeddingEveryNFrames: Int = 15,
    /** Snapshots older than this are treated as unavailable by the comparator. */
    val staleAfterMs: Long = 3_000,
    /** Longest edge of the scaled copy handed to inference. */
    val inferenceMaxEdgePx: Int = 512,
    /** Longest edge of the luma working image used by the tracker. */
    val trackingMaxEdgePx: Int = 160,
)
