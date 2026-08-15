package app.dyrecto.liveview.vision.results

/** A single detected face: bounding box + detection confidence. */
data class FaceBox(val rect: PixelRect, val confidence: Float)

/** Result produced by the face-detection pass of [FaceAndEyeDetectionModule]. */
data class FaceDetectionResult(
    override val moduleId: String,
    val facesDetected: Int,
    val boundingBoxes: List<FaceBox>,
    val averageConfidence: Float,
    val analysisTimeMs: Long,
    /** Analyzed frame width in pixels (Phase 9) — lets consumers normalize [boundingBoxes]. 0 = unknown. */
    val sourceWidth: Int = 0,
    /** Analyzed frame height in pixels (Phase 9) — lets consumers normalize [boundingBoxes]. 0 = unknown. */
    val sourceHeight: Int = 0,
) : VisionResult
