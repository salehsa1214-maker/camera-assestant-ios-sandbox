package app.dyrecto.liveview.reference.ai.snapshot

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.PrimarySubject
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory

/**
 * One extracted feature plus how much the extraction can be trusted (0..1).
 *
 * Confidence sources differ per feature — tracker match score, detector confidence, embedder
 * availability/staleness, classifier margin — and the comparator uses them: below a per-signal
 * floor the signal reports unavailable (state machine holds); above it, confidence scales drift
 * scores so shaky features can't confirm drift as fast as solid ones.
 */
data class FeatureValue<T>(
    val value: T?,
    val confidence: Float,
) {
    val present: Boolean get() = value != null

    companion object {
        fun <T> absent(): FeatureValue<T> = FeatureValue(null, 0f)
    }
}

/** A point in normalized 0.0–1.0 image coordinates. */
data class NormalizedPoint(val x: Float, val y: Float)

/** What produced the subject evidence in a snapshot. */
enum class SnapshotSource {
    /** Subjects come from live tracker updates (normal monitoring mode). */
    TRACKER,

    /** Subjects come from a fresh detector pass (init / recovery / verification). */
    DETECTOR,

    /** No object evidence — only the global embedding is available. */
    EMBEDDING_ONLY,

    /** Nothing available (all capabilities down or not yet run). */
    NONE,
}

/** Compact segmentation statistics carried by a snapshot (optional capability). */
data class SegmentationSummary(
    val coverage: Float,
    val maskBox: NormalizedRect?,
    val pixelAccurate: Boolean,
)

/**
 * One subject in the scene, in stable semantic terms. Tracked subjects carry the track's
 * identity in [trackId]; freshly detected (unverified) subjects use trackId = -1.
 */
data class SceneSubject(
    val trackId: Int,
    val category: SubjectCategory,
    /** Raw detector label — diagnostics/dev UI only, never drives strategy or comparison. */
    val rawLabel: String?,
    val confidence: Float,
    val boundingBox: NormalizedRect,
    val tracked: Boolean,
    val maskAvailable: Boolean,
) {
    val normalizedCenterX: Float get() = boundingBox.centerX
    val normalizedCenterY: Float get() = boundingBox.centerY
    val normalizedArea: Float get() = boundingBox.area

    companion object {
        /** trackId for subjects that came from a detector pass and have no track identity yet. */
        const val NO_TRACK = -1
    }
}

/**
 * The complete analyzed state of one scene (a reference image or a live frame) — the ONLY
 * representation downstream layers (strategy, comparator, UI) consume. Models never feed the
 * comparator directly: any detector/embedder/segmenter can be replaced as long as it fills a
 * SceneSnapshot.
 *
 * NOTE: contains FloatArray fields — snapshots are single-producer values published through a
 * newest-wins slot; structural equality is never relied upon.
 */
data class SceneSnapshot(
    val subjects: List<SceneSubject>,
    val primarySubject: FeatureValue<PrimarySubject>,
    val sceneMode: FeatureValue<SceneMode>,
    /** Primary-subject center. Confidence: tracker match score or detector confidence. */
    val subjectPosition: FeatureValue<NormalizedPoint>,
    /** Primary-subject normalized area. */
    val subjectSize: FeatureValue<Float>,
    /** 3×3 grid composition signature (confidence-weighted cell occupancy). */
    val layoutSignature: FeatureValue<FloatArray>,
    /** Global visual embedding for cosine-similarity comparison. */
    val embedding: FeatureValue<FloatArray>,
    /** Which model produced [embedding] — vectors across models are never comparable. */
    val embeddingModelId: String?,
    val segmentationSummary: SegmentationSummary?,
    val source: SnapshotSource,
    val analyzedAtMs: Long,
) {
    companion object {
        /** Snapshot representing "no AI evidence at all". */
        fun empty(nowMs: Long = 0L): SceneSnapshot = SceneSnapshot(
            subjects = emptyList(),
            primarySubject = FeatureValue.absent(),
            sceneMode = FeatureValue.absent(),
            subjectPosition = FeatureValue.absent(),
            subjectSize = FeatureValue.absent(),
            layoutSignature = FeatureValue.absent(),
            embedding = FeatureValue.absent(),
            embeddingModelId = null,
            segmentationSummary = null,
            source = SnapshotSource.NONE,
            analyzedAtMs = nowMs,
        )
    }
}
