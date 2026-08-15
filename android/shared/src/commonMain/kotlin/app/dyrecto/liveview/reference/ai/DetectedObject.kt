package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect

/**
 * One raw detection as produced by an [ObjectDetectorEngine] — detector-specific label included.
 *
 * This type exists ONLY between the detector engine and the [SemanticMapper]; everything past
 * DetectionManager consumes semantic [SceneSubject]s, never raw detections. All geometry is
 * normalized 0.0–1.0 (top-left origin) so detections compare across resolutions.
 */
data class DetectedObject(
    /** Raw detector label (e.g. COCO "person", "car"). Never used downstream of the mapper. */
    val className: String,
    val classId: Int,
    val confidence: Float,
    val boundingBox: NormalizedRect,
) {
    val normalizedCenterX: Float get() = boundingBox.centerX
    val normalizedCenterY: Float get() = boundingBox.centerY
    val normalizedWidth: Float get() = boundingBox.width
    val normalizedHeight: Float get() = boundingBox.height
    val normalizedArea: Float get() = boundingBox.area
}
