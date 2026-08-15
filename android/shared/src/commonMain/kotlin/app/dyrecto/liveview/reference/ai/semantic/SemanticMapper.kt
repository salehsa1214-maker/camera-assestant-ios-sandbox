package app.dyrecto.liveview.reference.ai.semantic

import app.dyrecto.liveview.reference.ai.SubjectCategory

/**
 * Converts detector-specific labels into stable application-level [SubjectCategory] concepts.
 *
 * Applied by DetectionManager immediately after inference, so raw labels never leak past the
 * perception layer. Replacing the detector model later means writing one new mapper paired with
 * the new engine — strategy, matching, comparison, and UI are untouched.
 */
interface SemanticMapper {
    /** Mapper/detector pairing name for diagnostics (e.g. "coco-80"). */
    val id: String

    fun map(rawLabel: String, classId: Int): SubjectCategory
}
