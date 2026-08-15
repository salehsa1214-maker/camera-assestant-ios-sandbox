package app.dyrecto.liveview.vision.results

import app.dyrecto.liveview.reference.ai.snapshot.SceneSnapshot

/**
 * Phase 10: the latest AI perception state, published through the standard Vision result path.
 *
 * Produced by AiSceneModule at ADAPTIVE cadence, not every frame — the VisionContext slot
 * simply keeps the newest one (consumers judge freshness by [SceneSnapshot.analyzedAtMs]).
 */
data class SceneSnapshotResult(
    override val moduleId: String,
    val snapshot: SceneSnapshot,
    /** Inferences that were due but skipped because one was already in flight (diagnostics). */
    val skippedInferences: Long,
) : VisionResult
