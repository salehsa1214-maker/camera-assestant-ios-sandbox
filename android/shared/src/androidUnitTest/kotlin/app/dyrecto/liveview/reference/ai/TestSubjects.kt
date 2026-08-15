package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject

/** Shared fixture builders for the pure AI-layer tests. */
internal fun subject(
    category: SubjectCategory,
    box: NormalizedRect,
    confidence: Float = 0.8f,
    rawLabel: String? = null,
    trackId: Int = SceneSubject.NO_TRACK,
    tracked: Boolean = false,
): SceneSubject = SceneSubject(
    trackId = trackId,
    category = category,
    rawLabel = rawLabel,
    confidence = confidence,
    boundingBox = box,
    tracked = tracked,
    maskAvailable = false,
)

internal fun box(left: Float, top: Float, right: Float, bottom: Float) =
    NormalizedRect(left, top, right, bottom)

/** A centered box covering roughly [size] of each dimension. */
internal fun centeredBox(size: Float): NormalizedRect {
    val half = size / 2f
    return NormalizedRect(0.5f - half, 0.5f - half, 0.5f + half, 0.5f + half)
}
