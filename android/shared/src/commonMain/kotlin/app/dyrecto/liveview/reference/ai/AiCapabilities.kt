package app.dyrecto.liveview.reference.ai

/** Lifecycle of one engine's model. */
enum class EngineStatus {
    /** Model not yet needed — will lazy-load on first use. */
    NOT_LOADED,
    READY,
    /** Load or inference init failed; latched permanently for this process. */
    FAILED,
}

/** One capability's status for diagnostics and strategy downgrades. */
data class AiCapability(
    val modelId: String,
    val status: EngineStatus,
    val error: String? = null,
) {
    /** Usable = not known to be broken. */
    val available: Boolean get() = status != EngineStatus.FAILED
}

/**
 * Aggregated model availability, published by the perception coordinator. A failed model marks
 * its capability unavailable and the pipeline continues with the remaining ones (e.g. detector
 * down → GENERIC_SCENE strategy on embedding alone) — model failure never crashes the app.
 */
data class AiCapabilities(
    val detection: AiCapability = AiCapability("", EngineStatus.NOT_LOADED),
    val embedding: AiCapability = AiCapability("", EngineStatus.NOT_LOADED),
    val segmentation: AiCapability = AiCapability("", EngineStatus.NOT_LOADED),
)
