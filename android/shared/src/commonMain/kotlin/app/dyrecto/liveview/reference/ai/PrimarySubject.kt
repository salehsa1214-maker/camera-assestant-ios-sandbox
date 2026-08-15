package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect

/** What kind of primary subject the scene has (or that it has several / none). */
enum class PrimarySubjectType {
    PERSON,
    ANIMAL,
    VEHICLE,
    PRODUCT,
    FOOD,
    BUILDING,
    MULTIPLE,
    NONE,
    UNKNOWN,
}

/**
 * The subject the assistant should keep consistent. Supports any object type — a face is just
 * one possible subject, not the core model.
 *
 * When [type] is [PrimarySubjectType.MULTIPLE] the geometry fields describe the union of the
 * important objects; when NONE/UNKNOWN they are zeroed and [boundingBox] is null (visual
 * similarity then carries monitoring instead).
 */
data class PrimarySubject(
    val type: PrimarySubjectType,
    val category: SubjectCategory,
    /** Raw detector label, retained for diagnostics/dev UI only — never drives logic. */
    val rawLabel: String?,
    val confidence: Float,
    val boundingBox: NormalizedRect?,
    val maskAvailable: Boolean,
    val normalizedCenterX: Float,
    val normalizedCenterY: Float,
    val normalizedArea: Float,
)
