package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject

/**
 * Derives [SceneMode] from semantic subjects + embedding availability — no dedicated scene
 * classifier model, by design. Confidence is a dominance margin: how unambiguous the winning
 * mode was, not a model score.
 *
 * ARCHITECTURE/LANDSCAPE remain reserved outputs: nothing in the current detector vocabulary
 * can positively identify them, so building/landscape references classify as GENERIC_SCENE
 * (same signal set — no monitoring capability is lost).
 */
object SceneModeClassifier {
    /** Subjects smaller than this are "small" for street-scene detection. */
    private const val STREET_SMALL_AREA = 0.15f

    fun classify(
        subjects: List<SceneSubject>,
        primary: FeatureValue<PrimarySubject>,
        embeddingAvailable: Boolean,
    ): FeatureValue<SceneMode> {
        if (subjects.isEmpty()) {
            return if (embeddingAvailable) {
                FeatureValue(SceneMode.GENERIC_SCENE, 0.5f)
            } else {
                FeatureValue(SceneMode.UNKNOWN, 0f)
            }
        }

        val primarySubject = primary.value
            ?: return FeatureValue(SceneMode.GENERIC_SCENE, 0.3f)

        if (isStreetScene(subjects)) {
            return FeatureValue(SceneMode.STREET, 0.6f)
        }

        return when (primarySubject.type) {
            PrimarySubjectType.NONE ->
                if (isFurnitureDominant(subjects)) FeatureValue(SceneMode.INTERIOR, 0.5f)
                else FeatureValue(SceneMode.GENERIC_SCENE, 0.5f)

            PrimarySubjectType.MULTIPLE ->
                if (isFurnitureDominant(subjects)) FeatureValue(SceneMode.INTERIOR, primary.confidence)
                else FeatureValue(SceneMode.MULTI_SUBJECT, primary.confidence)

            else -> FeatureValue(modeFor(primarySubject.category), primary.confidence)
        }
    }

    private fun modeFor(category: SubjectCategory): SceneMode = when (category) {
        SubjectCategory.HUMAN -> SceneMode.HUMAN
        SubjectCategory.ANIMAL -> SceneMode.ANIMAL
        SubjectCategory.VEHICLE -> SceneMode.VEHICLE
        SubjectCategory.FOOD -> SceneMode.FOOD
        SubjectCategory.BUILDING -> SceneMode.ARCHITECTURE
        SubjectCategory.PRODUCT, SubjectCategory.ELECTRONICS -> SceneMode.PRODUCT
        SubjectCategory.FURNITURE -> SceneMode.INTERIOR
        SubjectCategory.NATURE, SubjectCategory.UNKNOWN -> SceneMode.GENERIC_SCENE
    }

    /** Several small vehicles plus at least one small person reads as a street scene. */
    private fun isStreetScene(subjects: List<SceneSubject>): Boolean {
        val small = subjects.filter { it.normalizedArea < STREET_SMALL_AREA }
        if (small.size < subjects.size) return false
        val vehicles = small.count { it.category == SubjectCategory.VEHICLE }
        val humans = small.count { it.category == SubjectCategory.HUMAN }
        return vehicles >= 2 && humans >= 1
    }

    private fun isFurnitureDominant(subjects: List<SceneSubject>): Boolean {
        val furniture = subjects.count { it.category == SubjectCategory.FURNITURE }
        return furniture >= 2 && furniture * 2 >= subjects.size
    }
}
