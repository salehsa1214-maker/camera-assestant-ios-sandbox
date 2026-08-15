package app.dyrecto.liveview.vision.results

import app.dyrecto.liveview.exposure.SubjectExposureStats

/**
 * Exposure statistics of the current subject region (Phase 12), produced by `ExposureModule`
 * from the SAME pixel buffer as Histogram/Zebra — never a second Bitmap read. Emitted only while
 * a subject region is registered (reference monitoring active with a tracked subject).
 */
data class SubjectExposureResult(
    override val moduleId: String,
    val stats: SubjectExposureStats,
) : VisionResult
