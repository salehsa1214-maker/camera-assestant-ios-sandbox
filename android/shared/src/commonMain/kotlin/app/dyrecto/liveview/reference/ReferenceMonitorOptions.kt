package app.dyrecto.liveview.reference

import kotlinx.serialization.Serializable

/**
 * User-selected monitoring options for the Shot Reference assistant (Phase 9). Each flag enables
 * one comparison signal; [tolerance] selects the threshold set in [ReferenceConfig] — thresholds
 * are never a single hardcoded global.
 */
@Serializable
data class ReferenceMonitorOptions(
    val monitorExposure: Boolean = true,
    val monitorSubjectPosition: Boolean = true,
    val monitorSubjectSize: Boolean = true,
    val monitorWhiteBalance: Boolean = true,
    val monitorFraming: Boolean = false,
    val monitorHeadroom: Boolean = true,
    val monitorFacePresence: Boolean = true,
    val monitorEyeVisibility: Boolean = true,
    // Phase 10 — AI perception signals.
    val monitorSubjectPresence: Boolean = true,
    val monitorComposition: Boolean = true,
    val monitorVisualSimilarity: Boolean = true,
    val tolerance: ReferenceTolerance = ReferenceTolerance.LOOSE,
)

/** How much drift from the reference is acceptable before a signal counts as drifting. */
enum class ReferenceTolerance {
    STRICT,
    MEDIUM,
    LOOSE,
}
