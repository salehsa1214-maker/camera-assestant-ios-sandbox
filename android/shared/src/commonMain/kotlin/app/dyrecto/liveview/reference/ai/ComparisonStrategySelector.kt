package app.dyrecto.liveview.reference.ai

/**
 * Maps scene understanding to the comparison strategy. Also owns the capability downgrade:
 * without a working detector every strategy collapses to [ComparisonStrategy.GENERIC_SCENE_STRATEGY],
 * where visual similarity (embedding cosine) is the universal fallback signal.
 */
object ComparisonStrategySelector {
    fun select(
        sceneMode: SceneMode,
        primarySubject: PrimarySubject?,
        detectionAvailable: Boolean,
    ): ComparisonStrategy {
        if (!detectionAvailable) return ComparisonStrategy.GENERIC_SCENE_STRATEGY

        return when (sceneMode) {
            SceneMode.HUMAN -> ComparisonStrategy.HUMAN_STRATEGY
            SceneMode.ANIMAL -> ComparisonStrategy.ANIMAL_STRATEGY
            SceneMode.VEHICLE -> ComparisonStrategy.VEHICLE_STRATEGY
            SceneMode.PRODUCT, SceneMode.FOOD -> ComparisonStrategy.PRODUCT_STRATEGY
            SceneMode.ARCHITECTURE -> ComparisonStrategy.ARCHITECTURE_STRATEGY
            SceneMode.LANDSCAPE -> ComparisonStrategy.LANDSCAPE_STRATEGY
            SceneMode.MULTI_SUBJECT ->
                if (primarySubject?.type == PrimarySubjectType.MULTIPLE) {
                    ComparisonStrategy.MULTI_OBJECT_STRATEGY
                } else {
                    ComparisonStrategy.GENERIC_SCENE_STRATEGY
                }
            SceneMode.STREET,
            SceneMode.INTERIOR,
            SceneMode.GENERIC_SCENE,
            SceneMode.UNKNOWN,
            -> ComparisonStrategy.GENERIC_SCENE_STRATEGY
        }
    }
}
