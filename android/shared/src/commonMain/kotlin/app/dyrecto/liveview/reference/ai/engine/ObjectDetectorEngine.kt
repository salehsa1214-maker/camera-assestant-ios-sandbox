package app.dyrecto.liveview.reference.ai.engine

import app.dyrecto.platform.PlatformImage
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.DetectedObject

/**
 * Object-detection seam. Implementations wrap one concrete model (MediaPipe EfficientDet today,
 * anything tomorrow); decision logic never sees past this interface. Contract:
 *  - lazy-load the model on first [detect]; a load/inference-init failure latches the engine
 *    FAILED and every later call returns emptyList() — never throws.
 *  - returned boxes are normalized 0.0–1.0 against the input bitmap.
 */
interface ObjectDetectorEngine {
    val capability: AiCapability

    suspend fun detect(bitmap: PlatformImage): List<DetectedObject>
}
