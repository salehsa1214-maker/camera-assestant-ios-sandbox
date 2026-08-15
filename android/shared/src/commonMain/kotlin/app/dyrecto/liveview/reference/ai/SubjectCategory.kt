package app.dyrecto.liveview.reference.ai

/**
 * Stable application-level subject concepts (Phase 10 — AI Reference Assistant).
 *
 * The strategy, matching, and comparison layers depend ONLY on these categories — never on
 * detector-specific class names. Raw detector labels (COCO or any future model's vocabulary)
 * are converted by a [SemanticMapper] immediately after detection and never leak downstream,
 * so replacing the detector model never touches anything past the mapper.
 */
enum class SubjectCategory {
    HUMAN,
    ANIMAL,
    VEHICLE,
    PRODUCT,
    BUILDING,
    FOOD,
    FURNITURE,
    ELECTRONICS,
    NATURE,
    UNKNOWN,
}
