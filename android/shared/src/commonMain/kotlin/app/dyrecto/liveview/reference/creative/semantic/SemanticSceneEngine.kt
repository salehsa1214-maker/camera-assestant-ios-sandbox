package app.dyrecto.liveview.reference.creative.semantic

import app.dyrecto.platform.PlatformImage
import app.dyrecto.liveview.reference.ai.AiCapability
import app.dyrecto.liveview.reference.ai.EngineStatus

/**
 * Phase 16.1 — the semantic-vision seam. Same contract as the Phase 10 engine interfaces
 * (`EmbeddingEngine` et al.): lazy-load on first use, **latch FAILED and return null instead of
 * throwing** on any missing/invalid/incompatible/failed model, so a broken model downgrades a
 * capability rather than crashing.
 *
 * Runs ONLY on the one-shot reference-import path — never per frame. The MobileCLIP implementation
 * lives behind this interface so the model can be swapped later without touching the analyzer, the
 * `CreativeSceneModel`, reasoning, storyboard, voice, alerts, or telemetry.
 */
interface SemanticSceneEngine {
    val capability: AiCapability

    /** Semantic observation for one reference image, or null when the expert is unavailable. */
    suspend fun observe(bitmap: PlatformImage): SemanticObservation?
}

/**
 * A no-op engine used when semantic understanding is disabled or unconfigured. Always reports the
 * capability as unavailable and returns null — the analyzer then falls back to deterministic experts.
 */
object NoSemanticSceneEngine : SemanticSceneEngine {
    override val capability: AiCapability = AiCapability(modelId = "none", status = EngineStatus.NOT_LOADED)

    override suspend fun observe(bitmap: PlatformImage): SemanticObservation? = null
}
