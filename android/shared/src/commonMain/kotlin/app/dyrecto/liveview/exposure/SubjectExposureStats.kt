package app.dyrecto.liveview.exposure

import kotlin.concurrent.Volatile
import kotlinx.serialization.Serializable

/**
 * Lightweight exposure statistics of one region of interest — the tracked/reference subject box
 * (Phase 12). Same metric family as the whole-frame [app.dyrecto.liveview.vision.results.HistogramResult]
 * so subject exposure and frame exposure compare like-for-like in the Human Perception Layer.
 *
 * Serializable because the reference side is persisted inside `ReferenceExposureProfile`.
 */
@Serializable
data class SubjectExposureStats(
    /** Mean luma (0..255) of the sampled region pixels. */
    val mean: Float,
    /** Median luma bin (0..255). */
    val median: Float,
    /** % of region samples at/above the highlight-clip luma. */
    val highlightCoverage: Float,
    /** % of region samples at/below the shadow-clip luma. */
    val shadowCoverage: Float,
    /** Number of luma samples the stats were computed over. */
    val sampleCount: Int,
)

/** A region of interest in normalized 0..1 image coordinates (top-left origin). */
data class AnalysisRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * Process-wide handoff of the current subject region from the reference monitor to the exposure
 * scan (Phase 12). The Vision worker reads it once per frame; the reference monitor writes it
 * when the tracked subject box moves and clears it when monitoring stops. Generic mechanism —
 * any future consumer needing region exposure stats sets a region here.
 *
 * Clip thresholds ride along so the classification is configured by the perception layer, not
 * hardcoded in the scan.
 */
object AnalysisRegionRegistry {
    @Volatile var subjectRegion: AnalysisRegion? = null
    @Volatile var highlightLumaMin: Int = 250
    @Volatile var shadowLumaMax: Int = 16

    fun clear() {
        subjectRegion = null
    }
}
