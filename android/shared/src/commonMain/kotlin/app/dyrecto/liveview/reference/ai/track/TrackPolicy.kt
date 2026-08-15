package app.dyrecto.liveview.reference.ai.track

/**
 * When the object detector is allowed to run. Detection is NOT the monitoring mechanism —
 * tracking is; the detector exists to initialize tracking, recover from loss, react to scene
 * changes, and periodically verify/re-anchor the track.
 */
enum class DetectorTrigger {
    NONE,
    INIT,
    LOSS_RECOVERY,
    SCENE_CHANGE,
    PERIODIC_VERIFY,
}

/** All thresholds for the trigger policy in one tunable object. */
data class TrackPolicyConfig(
    /** Tracker confidence below this counts as a low-confidence frame. */
    val lossConfidenceThreshold: Float = 0.35f,
    /** Consecutive low-confidence tracker updates before the track is declared lost. */
    val lossFramesToDeclare: Int = 5,
    /** Heartbeat: verify/re-anchor the track with the detector this often. */
    val verifyIntervalMs: Long = 5_000,
    /**
     * Embedding cosine similarity to the last verified frame below this means the scene
     * changed enough to re-detect.
     */
    val sceneChangeSimilarityFloor: Float = 0.75f,
    /** Mean-luma shift (0..255) vs the last verified frame that means the scene changed. */
    val sceneChangeLumaDelta: Float = 40f,
)

/**
 * Pure trigger decision — no side effects, fully JVM-testable. Priority order: a missing track
 * always re-detects (INIT), then confirmed loss, then scene change, then the periodic heartbeat.
 */
object TrackPolicy {
    data class Input(
        val monitoringActive: Boolean,
        val hasActiveTrack: Boolean,
        val consecutiveLowConfidenceFrames: Int,
        val msSinceLastVerify: Long,
        /** Embedding cosine similarity to the last verified frame; null = no evidence. */
        val embeddingSimilarityToVerified: Float?,
        /** Absolute mean-luma delta vs the last verified frame; null = no evidence. */
        val meanLumaDeltaSinceVerified: Float?,
    )

    fun decide(input: Input, config: TrackPolicyConfig = TrackPolicyConfig()): DetectorTrigger {
        if (!input.monitoringActive) return DetectorTrigger.NONE
        if (!input.hasActiveTrack) return DetectorTrigger.INIT
        if (input.consecutiveLowConfidenceFrames >= config.lossFramesToDeclare) {
            return DetectorTrigger.LOSS_RECOVERY
        }
        val embeddingChanged = input.embeddingSimilarityToVerified
            ?.let { it < config.sceneChangeSimilarityFloor } == true
        val lumaChanged = input.meanLumaDeltaSinceVerified
            ?.let { it > config.sceneChangeLumaDelta } == true
        if (embeddingChanged || lumaChanged) return DetectorTrigger.SCENE_CHANGE
        if (input.msSinceLastVerify >= config.verifyIntervalMs) return DetectorTrigger.PERIODIC_VERIFY
        return DetectorTrigger.NONE
    }
}
