package app.dyrecto.liveview.vision.modules

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import app.dyrecto.liveview.vision.FrameAnalysisRequest
import app.dyrecto.liveview.vision.VisionModule
import app.dyrecto.liveview.vision.results.EyeBox
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.FaceBox
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.PixelRect
import app.dyrecto.liveview.vision.results.VisionResult
import kotlinx.coroutines.tasks.await

/**
 * Runs ML Kit face detection once per frame with [FaceDetectorOptions.LANDMARK_MODE_ALL] and
 * produces two independent results: [FaceDetectionResult] and [EyeDetectionResult].
 *
 * Running a single ML Kit pass for both avoids a redundant second inference call.
 * The two results are fully independent consumers in [VisionManager]: each goes into its own
 * slot in [app.dyrecto.liveview.vision.results.VisionContext].
 *
 * Uses the bundled ML Kit artifact — model ships inside the APK, no Play Services required,
 * works fully offline.
 *
 * [detector.process(image).await()] suspends the pipeline's coroutine without blocking any
 * thread — acceptable and preferred over [Tasks.await] since the pipeline runs on a coroutine.
 *
 * Confidence: ML Kit FAST mode without classification does not expose a per-face raw score,
 * so 0.8f is used as a conservative constant. Refine in a future phase if needed.
 */
class FaceAndEyeDetectionModule : VisionModule {

    override val id: String = "face-and-eye"

    // Created on first use, not at construction: building the ML Kit client touches the MlKit
    // runtime, so eager init would make merely *constructing* this module (e.g. inside VisionManager)
    // require a live Android/MlKit context. Lazy init defers that to the first analyzed frame —
    // identical runtime behavior, and unit tests can construct VisionManager without ML Kit.
    private val detector: FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build()
        )
    }

    override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> {
        if (request.bitmap.isRecycled) return emptyList()
        val startMs = System.currentTimeMillis()

        val image = InputImage.fromBitmap(request.bitmap, 0)
        val faces = detector.process(image).await()

        // ---- Face result ----
        val faceBoxes = faces.map {
            val bb = it.boundingBox
            FaceBox(rect = PixelRect(bb.left, bb.top, bb.right, bb.bottom), confidence = CONFIDENCE)
        }
        val faceResult = FaceDetectionResult(
            moduleId = id,
            facesDetected = faces.size,
            boundingBoxes = faceBoxes,
            averageConfidence = if (faceBoxes.isEmpty()) 0f else CONFIDENCE,
            analysisTimeMs = System.currentTimeMillis() - startMs,
            sourceWidth = request.bitmap.width,
            sourceHeight = request.bitmap.height,
        )

        // ---- Eye result — LEFT_EYE + RIGHT_EYE landmarks, radius-10 Rect around each point ----
        val eyeBoxes = mutableListOf<EyeBox>()
        for (face in faces) {
            for (landmarkType in listOf(FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE)) {
                val pos = face.getLandmark(landmarkType)?.position ?: continue
                val cx = pos.x.toInt()
                val cy = pos.y.toInt()
                eyeBoxes += EyeBox(
                    rect = PixelRect(cx - EYE_RADIUS, cy - EYE_RADIUS, cx + EYE_RADIUS, cy + EYE_RADIUS),
                    confidence = CONFIDENCE,
                )
            }
        }
        val eyeResult = EyeDetectionResult(
            moduleId = id,
            eyesDetected = eyeBoxes.size,
            boundingBoxes = eyeBoxes,
            averageConfidence = if (eyeBoxes.isEmpty()) 0f else CONFIDENCE,
            analysisTimeMs = System.currentTimeMillis() - startMs,
        )

        return listOf(faceResult, eyeResult)
    }

    private companion object {
        const val CONFIDENCE = 0.8f
        const val EYE_RADIUS = 10
    }
}
