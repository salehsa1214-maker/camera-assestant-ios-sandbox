package app.dyrecto.liveview.reference.ai.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.DetectedObject
import app.dyrecto.liveview.reference.ai.EngineStatus
import app.dyrecto.liveview.reference.ai.SegmentationResult
import app.dyrecto.liveview.reference.ai.SegmentationSource
import app.dyrecto.liveview.reference.ai.VisualEmbeddingResult
import app.dyrecto.liveview.reference.ai.engine.EmbeddingEngine
import app.dyrecto.liveview.reference.ai.engine.ObjectDetectorEngine
import app.dyrecto.liveview.reference.ai.engine.SegmentationEngine

/**
 * MediaPipe Tasks Vision engine implementations (Phase 10). This file is the ONLY place
 * MediaPipe types appear — everything else in the perception layer talks to the engine
 * interfaces, keeping decision logic pure Kotlin and JVM-testable.
 *
 * All models are bundled .tflite assets (fully offline, CPU delegate). Contract shared by the
 * three engines: lazy-load on first use inside runCatching; any failure logs once, latches the
 * capability FAILED permanently for this process, and the engine returns empty/null forever —
 * a broken model degrades that capability, it never crashes the app.
 */
object MediaPipeEngines {
    private const val TAG = "AiEngines"

    const val DETECTOR_MODEL_ID = "efficientdet_lite0"
    const val EMBEDDER_MODEL_ID = "mobilenet_v3_small"
    const val SEGMENTER_MODEL_ID = "deeplab_v3"

    private const val DETECTOR_ASSET = "models/efficientdet_lite0.tflite"
    private const val EMBEDDER_ASSET = "models/mobilenet_v3_small.tflite"
    private const val SEGMENTER_ASSET = "models/deeplab_v3.tflite"

    /** Detections below this score are discarded inside the task itself. */
    private const val DETECTOR_SCORE_THRESHOLD = 0.30f
    private const val DETECTOR_MAX_RESULTS = 8

    data class Engines(
        val detector: ObjectDetectorEngine,
        val embedder: EmbeddingEngine,
        val segmenter: SegmentationEngine,
    )

    fun create(context: Context): Engines {
        val appContext = context.applicationContext
        return Engines(
            detector = MediaPipeObjectDetectorEngine(appContext),
            embedder = MediaPipeEmbeddingEngine(appContext),
            segmenter = MediaPipeSegmentationEngine(appContext),
        )
    }

    /**
     * MediaPipe requires software ARGB_8888 input; live-frame callers already hand us a scaled
     * ARGB copy, but reference images decoded from arbitrary sources may not comply.
     */
    private fun toMpImage(bitmap: Bitmap): MPImage {
        val safe = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap
        else bitmap.copy(Bitmap.Config.ARGB_8888, false)
        return BitmapImageBuilder(safe).build()
    }

    /**
     * Shared lazy-load + failure-latch state machine. [loader] runs at most once; a throw
     * latches FAILED and [use] is never attempted again.
     */
    private class EngineHolder<T>(
        private val modelId: String,
        private val loader: () -> T,
    ) {
        @Volatile var capability: AiCapability = AiCapability(modelId, EngineStatus.NOT_LOADED)
            private set

        private var instance: T? = null

        @Synchronized
        fun <R> run(operation: String, use: (T) -> R): R? {
            if (capability.status == EngineStatus.FAILED) return null
            val engine = instance ?: run {
                runCatching(loader).fold(
                    onSuccess = {
                        instance = it
                        capability = AiCapability(modelId, EngineStatus.READY)
                        Log.i(TAG, "[$modelId] model loaded")
                        it
                    },
                    onFailure = {
                        capability = AiCapability(modelId, EngineStatus.FAILED, it.message)
                        Log.e(TAG, "[$modelId] model load failed — capability disabled", it)
                        return null
                    },
                )
            }
            return runCatching { use(engine) }.getOrElse {
                // Inference errors are latched too: a model that can't run is a model we
                // don't have.
                capability = AiCapability(modelId, EngineStatus.FAILED, it.message)
                Log.e(TAG, "[$modelId] $operation failed — capability disabled", it)
                null
            }
        }
    }

    private class MediaPipeObjectDetectorEngine(context: Context) : ObjectDetectorEngine {
        private val holder = EngineHolder(DETECTOR_MODEL_ID) {
            ObjectDetector.createFromOptions(
                context,
                ObjectDetector.ObjectDetectorOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath(DETECTOR_ASSET).build())
                    .setRunningMode(RunningMode.IMAGE)
                    .setMaxResults(DETECTOR_MAX_RESULTS)
                    .setScoreThreshold(DETECTOR_SCORE_THRESHOLD)
                    .build(),
            )
        }

        override val capability: AiCapability get() = holder.capability

        override suspend fun detect(bitmap: Bitmap): List<DetectedObject> {
            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()
            if (width <= 0f || height <= 0f) return emptyList()
            return holder.run("detect") { detector ->
                detector.detect(toMpImage(bitmap)).detections().mapNotNull { detection ->
                    val category = detection.categories().firstOrNull() ?: return@mapNotNull null
                    val box = detection.boundingBox()
                    DetectedObject(
                        className = category.categoryName(),
                        classId = category.index(),
                        confidence = category.score(),
                        boundingBox = NormalizedRect(
                            left = (box.left / width).coerceIn(0f, 1f),
                            top = (box.top / height).coerceIn(0f, 1f),
                            right = (box.right / width).coerceIn(0f, 1f),
                            bottom = (box.bottom / height).coerceIn(0f, 1f),
                        ),
                    )
                }
            } ?: emptyList()
        }
    }

    private class MediaPipeEmbeddingEngine(context: Context) : EmbeddingEngine {
        private val holder = EngineHolder(EMBEDDER_MODEL_ID) {
            ImageEmbedder.createFromOptions(
                context,
                ImageEmbedder.ImageEmbedderOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath(EMBEDDER_ASSET).build())
                    .setRunningMode(RunningMode.IMAGE)
                    // L2-normalized vectors make cosine similarity a plain dot product.
                    .setL2Normalize(true)
                    .setQuantize(false)
                    .build(),
            )
        }

        override val capability: AiCapability get() = holder.capability

        override suspend fun embed(bitmap: Bitmap): VisualEmbeddingResult? =
            holder.run("embed") { embedder ->
                val vector = embedder.embed(toMpImage(bitmap))
                    .embeddingResult()
                    .embeddings()
                    .firstOrNull()
                    ?.floatEmbedding()
                vector?.let {
                    VisualEmbeddingResult(embedding = it, modelId = EMBEDDER_MODEL_ID, confidence = 1f)
                }
            }
    }

    private class MediaPipeSegmentationEngine(context: Context) : SegmentationEngine {
        private val holder = EngineHolder(SEGMENTER_MODEL_ID) {
            ImageSegmenter.createFromOptions(
                context,
                ImageSegmenter.ImageSegmenterOptions.builder()
                    .setBaseOptions(BaseOptions.builder().setModelAssetPath(SEGMENTER_ASSET).build())
                    .setRunningMode(RunningMode.IMAGE)
                    .setOutputCategoryMask(true)
                    .setOutputConfidenceMasks(false)
                    .build(),
            )
        }

        override val capability: AiCapability get() = holder.capability

        override suspend fun segment(bitmap: Bitmap): SegmentationResult? =
            holder.run("segment") { segmenter ->
                val result = segmenter.segment(toMpImage(bitmap))
                val mask = result.categoryMask().orElse(null) ?: return@run null
                summarizeCategoryMask(mask)
            }

        /**
         * Category mask: one byte per pixel, 0 = background. Coverage is the non-background
         * fraction; the mask box is the tight bounds of non-background pixels, normalized.
         */
        private fun summarizeCategoryMask(mask: MPImage): SegmentationResult {
            val width = mask.width
            val height = mask.height
            val buffer = ByteBufferExtractor.extract(mask)
            buffer.rewind()
            var foreground = 0
            var minX = width
            var minY = height
            var maxX = -1
            var maxY = -1
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (buffer.get(y * width + x).toInt() != 0) {
                        foreground++
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }
            val total = width * height
            val coverage = if (total > 0) foreground.toFloat() / total else 0f
            val maskBox = if (maxX >= minX && maxY >= minY) NormalizedRect(
                left = minX.toFloat() / width,
                top = minY.toFloat() / height,
                right = (maxX + 1).toFloat() / width,
                bottom = (maxY + 1).toFloat() / height,
            ) else null
            return SegmentationResult(
                maskAvailable = foreground > 0,
                coverage = coverage,
                maskBox = maskBox,
                source = SegmentationSource.MODEL,
            )
        }
    }
}
