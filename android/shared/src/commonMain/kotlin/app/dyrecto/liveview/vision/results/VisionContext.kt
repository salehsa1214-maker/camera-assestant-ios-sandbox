package app.dyrecto.liveview.vision.results

/**
 * Aggregated snapshot of the latest result from every Vision module.
 *
 * Each slot holds the most recent result for that analysis type, or null if the module has not
 * yet produced one. Published as [kotlinx.coroutines.flow.StateFlow] by
 * [app.dyrecto.liveview.vision.VisionManager]; future phases (Assistant, Overlay,
 * Voice) consume only this context — never individual module results directly.
 */
data class VisionContext(
    val histogram: HistogramResult? = null,
    val zebra: ZebraResult? = null,
    val exposure: ExposureResult? = null,
    val faces: FaceDetectionResult? = null,
    val eyes: EyeDetectionResult? = null,
    /** Phase 9: average-color statistics from the fused exposure scan (Shot Reference input). */
    val colorStats: ColorStatsResult? = null,
    /** Phase 10: latest AI perception state (adaptive cadence — check its analyzedAtMs). */
    val aiScene: SceneSnapshotResult? = null,
    /** Phase 12: subject-region exposure stats (only while a subject region is registered). */
    val subjectExposure: SubjectExposureResult? = null,
    /** Monitoring overlays — non-null only while the matching [app.dyrecto.liveview.exposure.OverlayConfig] toggle is on. */
    val waveform: WaveformResult? = null,
    val falseColor: FalseColorResult? = null,
    val focusPeaking: FocusPeakingResult? = null,
    val updatedAtMs: Long = 0,
)
