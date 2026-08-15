package app.dyrecto.liveview.reference.creative.semantic

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.EngineStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Phase 16.1 — the MobileCLIP2-S0 image encoder behind the [SemanticSceneEngine] seam, run via the
 * LiteRT `Interpreter`. This is the ONLY file importing `org.tensorflow.lite`. It runs ONCE per
 * reference at import (never per frame).
 *
 * Fail-safe by contract: a missing / unreadable / malformed model or vocabulary, a shape/dimension
 * mismatch, or any inference error latches the capability FAILED (permanently, this process) and
 * returns null — the analyzer then falls back to the deterministic experts and reference import still
 * succeeds. The model can be swapped later without touching anything but this file + the two assets.
 *
 * Only the image tower ships. Concept text embeddings are precomputed offline and bundled in
 * [VOCAB_ASSET]; on device we score the image embedding against them (see [ConceptVocabulary]).
 * CLIP preprocessing (resize + mean/std) must match the offline vocabulary's; validate embedding
 * parity (cosine > 0.99 vs the PyTorch reference) before trusting concept scores.
 */
class MobileClipSemanticEngine(
    context: Context,
    private val modelAsset: String = MODEL_ASSET,
    private val vocabAsset: String = VOCAB_ASSET,
) : SemanticSceneEngine {

    private val appContext = context.applicationContext

    private val lock = Any()
    @Volatile private var status = EngineStatus.NOT_LOADED
    @Volatile private var loadError: String? = null

    private var interpreter: Interpreter? = null
    private var vocabulary: ConceptVocabulary? = null

    // Discovered from the model at load; supports both NHWC [1,H,W,3] and NCHW [1,3,H,W].
    private var inputHeight = INPUT_SIZE
    private var inputWidth = INPUT_SIZE
    private var channelsFirst = false
    private var outputDim = 0

    override val capability: AiCapability get() = AiCapability(MODEL_ID, status, loadError)

    override suspend fun observe(bitmap: Bitmap): SemanticObservation? {
        val ready = ensureLoaded() ?: return null
        return withContext(Dispatchers.Default) {
            runCatching { infer(ready.first, ready.second, bitmap) }.getOrElse { t ->
                latchFailed("inference", t)
                null
            }
        }
    }

    /** Lazy load; returns (interpreter, vocabulary) or null once FAILED. */
    private fun ensureLoaded(): Pair<Interpreter, ConceptVocabulary>? {
        interpreter?.let { itp -> vocabulary?.let { v -> return itp to v } }
        synchronized(lock) {
            interpreter?.let { itp -> vocabulary?.let { v -> return itp to v } }
            if (status == EngineStatus.FAILED) return null
            return runCatching {
                val itp = Interpreter(loadModel(modelAsset), Interpreter.Options().apply { numThreads = 2 })
                val inShape = itp.getInputTensor(0).shape() // [1,H,W,3] or [1,3,H,W]
                if (inShape.size == 4 && inShape[1] == 3) {
                    channelsFirst = true
                    inputHeight = inShape[2]
                    inputWidth = inShape[3]
                } else if (inShape.size == 4) {
                    channelsFirst = false
                    inputHeight = inShape[1]
                    inputWidth = inShape[2]
                } else {
                    error("unexpected input shape ${inShape.joinToString()}")
                }
                outputDim = itp.getOutputTensor(0).shape().last()

                val vocabText = appContext.assets.open(vocabAsset).use { it.readBytes().decodeToString() }
                val vocab = ConceptVocabulary.fromJson(vocabText)
                    ?: error("concept vocabulary missing/invalid")
                if (vocab.dimensions != outputDim) {
                    error("vocabulary dim ${vocab.dimensions} != model output $outputDim")
                }
                interpreter = itp
                vocabulary = vocab
                status = EngineStatus.READY
                Log.i(TAG, "[$MODEL_ID] loaded (in ${inputWidth}x$inputHeight, out $outputDim, ${vocab.entries.size} concepts)")
                itp to vocab
            }.getOrElse { t ->
                latchFailed("load", t)
                null
            }
        }
    }

    private fun infer(itp: Interpreter, vocab: ConceptVocabulary, bitmap: Bitmap): SemanticObservation {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(outputDim) }
        itp.run(input, output)
        val embedding = l2Normalize(output[0])
        val concepts = vocab.score(embedding)
        return SemanticObservation(concepts = concepts, embedding = embedding, modelId = MODEL_ID)
    }

    /** CLIP preprocessing: resize to the model input, normalize per-channel with the CLIP mean/std. */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val scaled = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val pixels = IntArray(inputWidth * inputHeight)
        scaled.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        if (scaled !== bitmap) scaled.recycle()

        val buffer = ByteBuffer.allocateDirect(4 * inputWidth * inputHeight * 3).order(ByteOrder.nativeOrder())
        val n = inputWidth * inputHeight
        if (channelsFirst) {
            // NCHW: all R, then all G, then all B.
            for (c in 0 until 3) {
                for (i in 0 until n) {
                    buffer.putFloat(channelValue(pixels[i], c))
                }
            }
        } else {
            // NHWC: R,G,B per pixel.
            for (i in 0 until n) {
                buffer.putFloat(channelValue(pixels[i], 0))
                buffer.putFloat(channelValue(pixels[i], 1))
                buffer.putFloat(channelValue(pixels[i], 2))
            }
        }
        buffer.rewind()
        return buffer
    }

    /** Normalized value for channel c (0=R,1=G,2=B) of an ARGB pixel. */
    private fun channelValue(pixel: Int, c: Int): Float {
        val raw = when (c) {
            0 -> (pixel shr 16) and 0xFF
            1 -> (pixel shr 8) and 0xFF
            else -> pixel and 0xFF
        }
        return (raw / 255f - CLIP_MEAN[c]) / CLIP_STD[c]
    }

    private fun l2Normalize(v: FloatArray): FloatArray {
        var sum = 0.0
        for (x in v) sum += x.toDouble() * x
        val norm = kotlin.math.sqrt(sum).toFloat()
        if (norm == 0f) return v
        for (i in v.indices) v[i] = v[i] / norm
        return v
    }

    /** Memory-map the .tflite from assets (noCompress "tflite" keeps it mappable). */
    private fun loadModel(asset: String): MappedByteBuffer =
        appContext.assets.openFd(asset).use { afd ->
            java.io.FileInputStream(afd.fileDescriptor).channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
            }
        }

    private fun latchFailed(stage: String, t: Throwable) {
        status = EngineStatus.FAILED
        loadError = t.message ?: t.javaClass.simpleName
        interpreter?.let { runCatching { it.close() } }
        interpreter = null
        vocabulary = null
        Log.w(TAG, "[$MODEL_ID] $stage failed — semantic expert disabled (graceful): ${t.message}")
    }

    companion object {
        const val MODEL_ID = "mobileclip2_s0_image"
        const val MODEL_ASSET = "models/mobileclip2_s0_image.tflite"
        const val VOCAB_ASSET = "creative/concept_vocab_mobileclip2_s0.json"
        private const val TAG = "MobileClipSemantic"
        private const val INPUT_SIZE = 256

        // MobileCLIP2-S0 (timm/OpenCLIP) uses NO mean/std normalization — its preprocess transform is
        // Normalize(mean=[0,0,0], std=[1,1,1]), i.e. raw 0..1 pixels. Verified against the model's
        // own transform + a cosine=1.0 conversion parity check. (NOT the OpenAI CLIP mean/std.)
        private val CLIP_MEAN = floatArrayOf(0f, 0f, 0f)
        private val CLIP_STD = floatArrayOf(1f, 1f, 1f)
    }
}
