package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import kotlin.math.sqrt

/**
 * Weighted subject matching — deliberately NOT IoU-primary. A candidate is scored by combining
 * several signals so the match stays robust when multiple similar objects are in frame:
 *
 *  - semantic category: HARD GATE (a dog is never matched to a car);
 *  - IoU with the expected box;
 *  - normalized center distance;
 *  - appearance similarity (optional callback — e.g. embedding similarity of the subject crop);
 *  - track identity (same live track id as last time — reserved weight, fed by TrackingManager);
 *  - raw-label affinity (opaque perception-internal bonus; labels still never leave this layer).
 *
 * Weights live in [MatchWeights] so tuning is one object, and every term is optional: absent
 * evidence contributes 0 rather than disqualifying a candidate.
 */
object SubjectMatcher {
    data class MatchWeights(
        val iou: Float = 0.35f,
        val centerDistance: Float = 0.25f,
        val appearance: Float = 0.15f,
        val trackIdentity: Float = 0.15f,
        val labelAffinity: Float = 0.10f,
        /** Candidates scoring below this are not a match at all. */
        val minScore: Float = 0.25f,
    )

    val DEFAULT_WEIGHTS = MatchWeights()

    data class Expected(
        val category: SubjectCategory,
        val boundingBox: NormalizedRect,
        val rawLabel: String? = null,
        val trackId: Int = SceneSubject.NO_TRACK,
    )

    data class Match(val subject: SceneSubject, val score: Float)

    fun match(
        expected: Expected,
        candidates: List<SceneSubject>,
        weights: MatchWeights = DEFAULT_WEIGHTS,
        appearanceScore: ((SceneSubject) -> Float)? = null,
    ): Match? = candidates
        .asSequence()
        .filter { it.category == expected.category }
        .map { Match(it, score(expected, it, weights, appearanceScore)) }
        .filter { it.score >= weights.minScore }
        .maxByOrNull { it.score }

    private fun score(
        expected: Expected,
        candidate: SceneSubject,
        weights: MatchWeights,
        appearanceScore: ((SceneSubject) -> Float)?,
    ): Float {
        var score = weights.iou * iou(expected.boundingBox, candidate.boundingBox)
        score += weights.centerDistance * centerProximity(expected.boundingBox, candidate.boundingBox)
        appearanceScore?.let { score += weights.appearance * it(candidate).coerceIn(0f, 1f) }
        if (expected.trackId != SceneSubject.NO_TRACK && candidate.trackId == expected.trackId) {
            score += weights.trackIdentity
        }
        if (expected.rawLabel != null && expected.rawLabel == candidate.rawLabel) {
            score += weights.labelAffinity
        }
        return score
    }

    fun iou(a: NormalizedRect, b: NormalizedRect): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        if (right <= left || bottom <= top) return 0f
        val intersection = (right - left) * (bottom - top)
        val union = a.area + b.area - intersection
        if (union <= 0f) return 0f
        return intersection / union
    }

    /** 1.0 at identical centers, 0.0 at opposite frame corners. */
    private fun centerProximity(a: NormalizedRect, b: NormalizedRect): Float {
        val dx = a.centerX - b.centerX
        val dy = a.centerY - b.centerY
        val distance = sqrt(dx * dx + dy * dy) / MAX_DISTANCE
        return 1f - distance.coerceIn(0f, 1f)
    }

    private val MAX_DISTANCE = sqrt(2f)
}
