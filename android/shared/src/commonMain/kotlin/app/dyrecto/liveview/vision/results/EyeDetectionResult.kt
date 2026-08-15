package app.dyrecto.liveview.vision.results

/** A single detected eye: bounding box (radius-10 around the ML Kit landmark) + confidence. */
data class EyeBox(val rect: PixelRect, val confidence: Float)

/** Result produced by the eye-detection pass of [FaceAndEyeDetectionModule]. */
data class EyeDetectionResult(
    override val moduleId: String,
    val eyesDetected: Int,
    val boundingBoxes: List<EyeBox>,
    val averageConfidence: Float,
    val analysisTimeMs: Long,
) : VisionResult
