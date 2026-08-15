package app.dyrecto.liveview.scene

import app.dyrecto.connection.BleLog
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.results.VisionContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

/**
 * Scene Understanding Engine (Phase 5C). Observes the two raw realtime streams and republishes a
 * merged, interpreted [SceneContext] as a [StateFlow].
 *
 * The interpretation itself lives entirely in the pure [SceneAnalyzer]; this class only wires the
 * flows together and throttles diagnostic logging. It contains no business logic beyond scene
 * interpretation — no alerts, no UI, no decisions.
 */
class SceneManager(
    frameContext: StateFlow<FrameContext>,
    visionContext: StateFlow<VisionContext>,
    scope: CoroutineScope,
) {
    val sceneContext: StateFlow<SceneContext> =
        combine(frameContext, visionContext) { frame, vision ->
            SceneAnalyzer.analyze(frame, vision, System.currentTimeMillis())
        }
            .onEach { maybeLog(it) }
            .stateIn(scope, SharingStarted.Eagerly, SceneContext())

    @Volatile private var lastLogAtMs: Long = 0L

    private fun maybeLog(ctx: SceneContext) {
        val now = System.currentTimeMillis()
        if (now - lastLogAtMs < 1000L) return
        lastLogAtMs = now
        BleLog.line(
            BleLog.Kind.INFO,
            "[SCENE] state=%s recording=%b highlight=%.0f%% shadow=%.0f%% faces=%d eyes=%d battery=%s storage=%s".format(
                ctx.overallState.name,
                ctx.recording.isRecording,
                ctx.exposure.highlightPercentage,
                ctx.exposure.shadowPercentage,
                ctx.face.facesDetected,
                ctx.face.eyesDetected,
                ctx.battery.batteryLevel?.let { "$it%" } ?: "—",
                ctx.storage.remainingStatus ?: "—",
            ),
        )
    }
}
