package app.dyrecto.liveview.reference.ai

/**
 * How the live scene should be compared against the reference. Selected once at
 * reference-analysis time from [SceneMode] + primary subject (and downgraded live when
 * capabilities drop out — e.g. detector unavailable → [GENERIC_SCENE_STRATEGY], where
 * visual similarity is the universal fallback signal).
 */
enum class ComparisonStrategy {
    HUMAN_STRATEGY,
    ANIMAL_STRATEGY,
    VEHICLE_STRATEGY,
    PRODUCT_STRATEGY,
    ARCHITECTURE_STRATEGY,
    LANDSCAPE_STRATEGY,
    MULTI_OBJECT_STRATEGY,
    GENERIC_SCENE_STRATEGY,
}
