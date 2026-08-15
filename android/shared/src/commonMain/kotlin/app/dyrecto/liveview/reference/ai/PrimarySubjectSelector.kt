package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Picks the subject the assistant should keep consistent, from semantic subjects only
 * (pure Kotlin — no Android/ML types).
 *
 * Rules:
 *  - candidates below [MIN_CONFIDENCE] are ignored entirely;
 *  - each candidate scores `area^0.7 × confidence × centerWeight` (large, confident, central
 *    objects are what a camera operator composes around);
 *  - one dominant candidate (top ≥ [DOMINANCE_RATIO] × second) → that subject;
 *  - otherwise several comparably-important candidates → [PrimarySubjectType.MULTIPLE] whose
 *    geometry is the union of the important set (score ≥ [IMPORTANCE_RATIO] × top);
 *  - nothing passes → a confident [PrimarySubjectType.NONE] (a scene with no subject is a
 *    valid understanding, distinct from "detector unavailable" which callers encode as an
 *    absent FeatureValue instead).
 */
object PrimarySubjectSelector {
    const val MIN_CONFIDENCE = 0.35f
    const val DOMINANCE_RATIO = 1.6f
    const val IMPORTANCE_RATIO = 0.6f

    /** Confidence assigned to a confidently-empty scene (no candidate passed the gate). */
    private const val NONE_CONFIDENCE = 0.6f

    fun select(subjects: List<SceneSubject>): FeatureValue<PrimarySubject> {
        val scored = subjects
            .filter { it.confidence >= MIN_CONFIDENCE }
            .map { it to score(it) }
            .sortedByDescending { it.second }

        if (scored.isEmpty()) return FeatureValue(none(), NONE_CONFIDENCE)

        val (top, topScore) = scored.first()
        val second = scored.getOrNull(1)

        if (second == null || topScore >= DOMINANCE_RATIO * second.second) {
            return FeatureValue(top.toPrimary(typeFor(top.category)), top.confidence)
        }

        val important = scored.filter { it.second >= IMPORTANCE_RATIO * topScore }.map { it.first }
        return FeatureValue(multiple(important), important.map { it.confidence }.average().toFloat())
    }

    private fun score(subject: SceneSubject): Float {
        val area = subject.normalizedArea.coerceIn(0f, 1f)
        return area.toDouble().pow(0.7).toFloat() * subject.confidence * centerWeight(subject)
    }

    /** 1.0 at frame center, 0.5 at the far corner. */
    private fun centerWeight(subject: SceneSubject): Float {
        val dx = subject.normalizedCenterX - 0.5f
        val dy = subject.normalizedCenterY - 0.5f
        val distance = sqrt(dx * dx + dy * dy) / MAX_CENTER_DISTANCE
        return 1f - 0.5f * distance.coerceIn(0f, 1f)
    }

    private val MAX_CENTER_DISTANCE = sqrt(0.5f * 0.5f + 0.5f * 0.5f)

    fun typeFor(category: SubjectCategory): PrimarySubjectType = when (category) {
        SubjectCategory.HUMAN -> PrimarySubjectType.PERSON
        SubjectCategory.ANIMAL -> PrimarySubjectType.ANIMAL
        SubjectCategory.VEHICLE -> PrimarySubjectType.VEHICLE
        SubjectCategory.FOOD -> PrimarySubjectType.FOOD
        SubjectCategory.BUILDING -> PrimarySubjectType.BUILDING
        SubjectCategory.PRODUCT,
        SubjectCategory.ELECTRONICS,
        SubjectCategory.FURNITURE,
        -> PrimarySubjectType.PRODUCT
        SubjectCategory.NATURE,
        SubjectCategory.UNKNOWN,
        -> PrimarySubjectType.UNKNOWN
    }

    private fun SceneSubject.toPrimary(type: PrimarySubjectType) = PrimarySubject(
        type = type,
        category = category,
        rawLabel = rawLabel,
        confidence = confidence,
        boundingBox = boundingBox,
        maskAvailable = maskAvailable,
        normalizedCenterX = normalizedCenterX,
        normalizedCenterY = normalizedCenterY,
        normalizedArea = normalizedArea,
    )

    private fun multiple(important: List<SceneSubject>): PrimarySubject {
        val union = important
            .map { it.boundingBox }
            .reduce { acc, box ->
                NormalizedRect(
                    left = minOf(acc.left, box.left),
                    top = minOf(acc.top, box.top),
                    right = maxOf(acc.right, box.right),
                    bottom = maxOf(acc.bottom, box.bottom),
                )
            }
        val categories = important.map { it.category }.distinct()
        return PrimarySubject(
            type = PrimarySubjectType.MULTIPLE,
            category = categories.singleOrNull() ?: SubjectCategory.UNKNOWN,
            rawLabel = null,
            confidence = important.map { it.confidence }.average().toFloat(),
            boundingBox = union,
            maskAvailable = false,
            normalizedCenterX = union.centerX,
            normalizedCenterY = union.centerY,
            normalizedArea = union.area,
        )
    }

    private fun none() = PrimarySubject(
        type = PrimarySubjectType.NONE,
        category = SubjectCategory.UNKNOWN,
        rawLabel = null,
        confidence = 0f,
        boundingBox = null,
        maskAvailable = false,
        normalizedCenterX = 0f,
        normalizedCenterY = 0f,
        normalizedArea = 0f,
    )
}
