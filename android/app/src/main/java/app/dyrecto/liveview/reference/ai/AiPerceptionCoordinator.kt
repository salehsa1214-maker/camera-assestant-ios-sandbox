package app.dyrecto.liveview.reference.ai

import android.graphics.Bitmap
import android.util.Log
import app.dyrecto.liveview.reference.ai.detect.DetectionManager
import app.dyrecto.liveview.reference.ai.embed.EmbeddingManager
import app.dyrecto.liveview.reference.ai.segment.SegmentationManager
import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.NormalizedPoint
import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import app.dyrecto.liveview.reference.ai.snapshot.SegmentationSummary
import app.dyrecto.liveview.reference.ai.snapshot.SnapshotSource
import app.dyrecto.liveview.reference.ai.strategy.StrategyManager
import app.dyrecto.liveview.reference.ai.track.DetectorTrigger
import app.dyrecto.liveview.reference.ai.track.LumaFrame
import app.dyrecto.liveview.reference.ai.track.TrackingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Thin sequencing layer over the focused perception managers — it decides ONLY what runs when
 * and folds results into a [SceneSnapshot]; every decision rule lives in the managers or the
 * pure selectors, never here.
 *
 * Live flow (Detection → Tracking → Comparison): per due frame the cheap tracker carries
 * monitoring; the detector runs only when [TrackingManager]'s trigger policy asks for it
 * (init / loss recovery / scene change / periodic verify). Reference flow: one full pass of
 * detection + embedding + optional segmentation.
 *
 * Never-throw contract: [analyzeReference] and [processLiveFrame] catch everything and fall
 * back to the best snapshot assemblable from whatever capabilities survived. A model failure
 * downgrades the snapshot (e.g. detector down → EMBEDDING_ONLY); it never propagates.
 */
class AiPerceptionCoordinator(
    val detection: DetectionManager,
    val embedding: EmbeddingManager,
    val segmentation: SegmentationManager,
    private val strategy: StrategyManager,
    val tracking: TrackingManager,
    val cadence: AiCadenceConfig = AiCadenceConfig(),
    /** Injectable so JVM tests can supply synthetic luma without Bitmap statics. */
    private val lumaExtractor: (Bitmap, Int) -> LumaFrame = BitmapLuma::extract,
) {
    private val _capabilities = MutableStateFlow(AiCapabilities())
    val capabilities: StateFlow<AiCapabilities> = _capabilities.asStateFlow()

    /** Live AI runs only while reference monitoring wants it. */
    @Volatile private var monitoringActive = false
    val liveWorkNeeded: Boolean get() = monitoringActive

    // ---- Live evidence, persisted between inference runs (adaptive cadence reuses the ----
    // ---- latest result of each layer rather than re-running everything per frame).    ----
    private var trackedSubject: SceneSubject? = null
    private var otherSubjects: List<SceneSubject> = emptyList()
    private var subjectsAtMs: Long = 0L
    private var lastEmbedding: VisualEmbeddingResult? = null
    private var lastEmbeddingAtMs: Long = 0L
    private var lastVerifiedEmbedding: FloatArray? = null
    private var lastVerifiedMeanLuma: Float? = null

    /** Diagnostics: why the detector last ran, and how often each trigger fired. */
    @Volatile var lastDetectorTrigger: DetectorTrigger = DetectorTrigger.NONE
        private set
    private val triggerCounts = LinkedHashMap<DetectorTrigger, Long>()

    fun triggerCounts(): Map<DetectorTrigger, Long> = synchronized(triggerCounts) { triggerCounts.toMap() }

    /**
     * Pin what live perception should look for (from the reference profile's primary subject)
     * and whether monitoring is active at all. Deactivating clears live evidence.
     */
    fun setLiveMonitoring(expected: SubjectMatcher.Expected?, active: Boolean) {
        tracking.expectSubject(expected)
        monitoringActive = active
        if (!active) resetLiveState()
    }

    /** Forget live evidence (monitoring stopped / reference cleared / new session). */
    fun resetLiveState() {
        tracking.reset()
        trackedSubject = null
        otherSubjects = emptyList()
        subjectsAtMs = 0L
        lastEmbedding = null
        lastEmbeddingAtMs = 0L
        lastVerifiedEmbedding = null
        lastVerifiedMeanLuma = null
        lastDetectorTrigger = DetectorTrigger.NONE
    }

    /**
     * Full one-shot analysis of a reference image: detection + embedding + one-shot
     * segmentation statistics (the only place segmentation runs by default).
     */
    suspend fun analyzeReference(bitmap: Bitmap, nowMs: Long): SceneSnapshot = runCatching {
        val subjects = detection.detect(bitmap)
        val embeddingResult = embedding.embed(bitmap)
        val segmentationResult = segmentation.segment(bitmap)
        assemble(
            subjects = subjects,
            primaryOverride = null,
            subjectsAgeMs = 0L,
            embeddingResult = embeddingResult,
            embeddingAgeMs = 0L,
            segmentationSummary = segmentationResult?.let {
                SegmentationSummary(it.coverage, it.maskBox, pixelAccurate = it.maskAvailable)
            },
            source = if (subjects.isNotEmpty()) SnapshotSource.DETECTOR else null,
            nowMs = nowMs,
        )
    }.getOrElse {
        Log.e(TAG, "analyzeReference failed", it)
        SceneSnapshot.empty(nowMs)
    }.also { publishCapabilities() }

    /**
     * One live perception step over an inference-sized ARGB copy of the frame (the caller owns
     * and recycles it). [runTracker]/[runEmbedding] reflect the module's frame cadence; the
     * detector decides for itself via the trigger policy.
     */
    suspend fun processLiveFrame(
        bitmap: Bitmap,
        runTracker: Boolean,
        runEmbedding: Boolean,
        nowMs: Long,
    ): SceneSnapshot = runCatching {
        val luma = lumaExtractor(bitmap, cadence.trackingMaxEdgePx)

        if (runEmbedding) {
            embedding.embed(bitmap)?.let {
                lastEmbedding = it
                lastEmbeddingAtMs = nowMs
            }
        }

        // Scene-change evidence vs the last verified frame, for the trigger policy.
        val embeddingSimilarity = lastVerifiedEmbedding?.let { verified ->
            lastEmbedding?.let { EmbeddingMath.cosineSimilarity(verified, it.embedding) }
        }
        val meanLuma = luma.meanLuma()
        val lumaDelta = lastVerifiedMeanLuma?.let { abs(meanLuma - it) }

        val trigger = tracking.detectorTrigger(monitoringActive, nowMs, embeddingSimilarity, lumaDelta)
        if (trigger != DetectorTrigger.NONE) {
            lastDetectorTrigger = trigger
            synchronized(triggerCounts) { triggerCounts.merge(trigger, 1L, Long::plus) }

            val detections = detection.detect(bitmap)
            val tracked = tracking.onDetections(luma, detections, nowMs)
            trackedSubject = tracked
            // The matched detection is superseded by its tracked version; keep the rest for
            // composition/multi-object evidence.
            otherSubjects = if (tracked != null) {
                detections.filter { it.boundingBox != tracked.boundingBox }
            } else {
                detections
            }
            subjectsAtMs = nowMs
            lastVerifiedEmbedding = lastEmbedding?.embedding
            lastVerifiedMeanLuma = meanLuma
        } else if (runTracker) {
            tracking.onTrackerFrame(luma)?.let {
                trackedSubject = it
                subjectsAtMs = nowMs
            } ?: run {
                if (!tracking.hasActiveTrack) trackedSubject = null
            }
        }

        val subjects = listOfNotNull(trackedSubject) + otherSubjects
        assemble(
            subjects = subjects,
            primaryOverride = trackedSubject,
            subjectsAgeMs = if (subjectsAtMs > 0) nowMs - subjectsAtMs else Long.MAX_VALUE,
            embeddingResult = lastEmbedding,
            embeddingAgeMs = if (lastEmbeddingAtMs > 0) nowMs - lastEmbeddingAtMs else Long.MAX_VALUE,
            segmentationSummary = null,
            source = when {
                trigger != DetectorTrigger.NONE -> SnapshotSource.DETECTOR
                trackedSubject != null -> SnapshotSource.TRACKER
                else -> null
            },
            nowMs = nowMs,
        )
    }.getOrElse {
        Log.e(TAG, "processLiveFrame failed", it)
        SceneSnapshot.empty(nowMs)
    }.also { publishCapabilities() }

    private fun assemble(
        subjects: List<SceneSubject>,
        /** Live tracked subject: identity continuity outranks re-selection. */
        primaryOverride: SceneSubject?,
        subjectsAgeMs: Long,
        embeddingResult: VisualEmbeddingResult?,
        embeddingAgeMs: Long,
        segmentationSummary: SegmentationSummary?,
        source: SnapshotSource?,
        nowMs: Long,
    ): SceneSnapshot {
        val embeddingConfidence = embeddingResult?.let {
            it.confidence * stalenessFactor(embeddingAgeMs)
        } ?: 0f
        val detectionAvailable = detection.capability.available
        val classification = strategy.classify(
            subjects = subjects,
            embeddingAvailable = embeddingResult != null && embeddingConfidence > 0f,
            detectionAvailable = detectionAvailable,
        )
        val subjectStaleness = stalenessFactor(subjectsAgeMs)
        val primary = primaryOverride?.let {
            FeatureValue(
                PrimarySubject(
                    type = PrimarySubjectSelector.typeFor(it.category),
                    category = it.category,
                    rawLabel = it.rawLabel,
                    confidence = it.confidence,
                    boundingBox = it.boundingBox,
                    maskAvailable = it.maskAvailable,
                    normalizedCenterX = it.normalizedCenterX,
                    normalizedCenterY = it.normalizedCenterY,
                    normalizedArea = it.normalizedArea,
                ),
                it.confidence,
            )
        } ?: classification.primarySubject
        val layoutSignature = if (detectionAvailable && subjects.isNotEmpty()) {
            FeatureValue(CompositionAnalyzer.gridSignature(subjects), subjectStaleness)
        } else {
            FeatureValue.absent<FloatArray>()
        }
        return SceneSnapshot(
            subjects = subjects,
            primarySubject = primary.copy(confidence = primary.confidence * subjectStaleness),
            sceneMode = classification.sceneMode,
            subjectPosition = primary.value?.boundingBox?.let {
                FeatureValue(
                    NormalizedPoint(it.centerX, it.centerY),
                    primary.confidence * subjectStaleness,
                )
            } ?: FeatureValue.absent(),
            subjectSize = primary.value?.boundingBox?.let {
                FeatureValue(it.area, primary.confidence * subjectStaleness)
            } ?: FeatureValue.absent(),
            layoutSignature = layoutSignature,
            embedding = embeddingResult?.let { FeatureValue(it.embedding, embeddingConfidence) }
                ?: FeatureValue.absent(),
            embeddingModelId = embeddingResult?.modelId,
            segmentationSummary = segmentationSummary,
            source = source ?: when {
                subjects.isNotEmpty() -> SnapshotSource.DETECTOR
                embeddingResult != null -> SnapshotSource.EMBEDDING_ONLY
                else -> SnapshotSource.NONE
            },
            analyzedAtMs = nowMs,
        )
    }

    /** Linear confidence decay from 1.0 (fresh) to 0.0 (at/after the staleness horizon). */
    private fun stalenessFactor(ageMs: Long): Float {
        if (ageMs <= 0) return 1f
        if (ageMs >= cadence.staleAfterMs) return 0f
        return 1f - ageMs.toFloat() / cadence.staleAfterMs
    }

    private fun publishCapabilities() {
        _capabilities.value = AiCapabilities(
            detection = detection.capability,
            embedding = embedding.capability,
            segmentation = segmentation.capability,
        )
    }

    private companion object {
        const val TAG = "AiPerception"
    }
}
