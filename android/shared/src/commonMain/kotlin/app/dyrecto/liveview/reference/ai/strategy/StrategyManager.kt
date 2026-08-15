package app.dyrecto.liveview.reference.ai.strategy

import app.dyrecto.liveview.reference.ai.ComparisonStrategy
import app.dyrecto.liveview.reference.ai.ComparisonStrategySelector
import app.dyrecto.liveview.reference.ai.PrimarySubject
import app.dyrecto.liveview.reference.ai.PrimarySubjectSelector
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SceneModeClassifier
import app.dyrecto.liveview.reference.ai.snapshot.FeatureValue
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject

/** Scene understanding derived from one set of subjects + capability context. */
data class SceneClassification(
    val sceneMode: FeatureValue<SceneMode>,
    val primarySubject: FeatureValue<PrimarySubject>,
    val strategy: ComparisonStrategy,
)

/**
 * Derives SceneMode + PrimarySubject + ComparisonStrategy by sequencing the pure selectors
 * (PrimarySubjectSelector → SceneModeClassifier → ComparisonStrategySelector). Deliberately
 * model-free: there is no dedicated scene classifier network; derivation from detections +
 * embeddings is the design (a classifier model would only be introduced if this proves
 * insufficient).
 */
class StrategyManager {
    fun classify(
        subjects: List<SceneSubject>,
        embeddingAvailable: Boolean,
        detectionAvailable: Boolean,
    ): SceneClassification {
        // Without a detector, subject evidence is UNKNOWABLE (absent), which is different from
        // a working detector confidently finding nothing (PrimarySubjectType.NONE).
        val primary = if (detectionAvailable) {
            PrimarySubjectSelector.select(subjects)
        } else {
            FeatureValue.absent()
        }
        val sceneMode = SceneModeClassifier.classify(subjects, primary, embeddingAvailable)
        val strategy = ComparisonStrategySelector.select(
            sceneMode = sceneMode.value ?: SceneMode.UNKNOWN,
            primarySubject = primary.value,
            detectionAvailable = detectionAvailable,
        )
        return SceneClassification(sceneMode, primary, strategy)
    }
}
