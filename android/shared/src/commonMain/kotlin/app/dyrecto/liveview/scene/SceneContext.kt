package app.dyrecto.liveview.scene

import app.dyrecto.domain.ConnectionPhase

/**
 * Immutable, interpreted description of the current shooting situation (Phase 5C).
 *
 * The Scene Understanding Engine merges the two raw realtime streams —
 * [app.dyrecto.liveview.session.FrameContext] (render metrics + telemetry +
 * connection) and [app.dyrecto.liveview.vision.results.VisionContext]
 * (highlight/shadow clipping + face/eye detection) — into this single object. It answers only
 * "what is currently happening?".
 *
 * It makes **no decisions**: no alerts, no thresholds beyond descriptive presence flags, no
 * timers, no AF assumptions, no UI behavior. Future phases (Alert Engine, Overlay, Assistant)
 * consume only this context — never the two underlying streams directly.
 *
 * Published as [kotlinx.coroutines.flow.StateFlow] by
 * [app.dyrecto.liveview.scene.SceneManager].
 */
data class SceneContext(
    val updatedAtMs: Long = 0,
    val exposure: ExposureState = ExposureState(),
    val face: FaceState = FaceState(),
    val recording: RecordingState = RecordingState(),
    val connection: SceneConnectionState = SceneConnectionState(),
    val battery: BatteryState = BatteryState(),
    val storage: StorageState = StorageState(),
    val overallState: SceneState = SceneState.NORMAL,
)

/** Descriptive exposure state derived from the Vision clipping modules. No alert thresholds. */
data class ExposureState(
    val highlightPercentage: Float = 0f,
    val shadowPercentage: Float = 0f,
    val isHighlightClipped: Boolean = false,
    val isShadowClipped: Boolean = false,
)

/** Descriptive face/eye state derived from the Vision detection module. No AF, no tracking. */
data class FaceState(
    val facesDetected: Int = 0,
    val eyesDetected: Int = 0,
    val hasVisibleFace: Boolean = false,
    val hasVisibleEyes: Boolean = false,
)

/** Recording state derived only from telemetry. No timers. */
data class RecordingState(
    val isRecording: Boolean = false,
    val recordingDurationAvailable: Boolean = false,
)

/**
 * Scene-level connection state. Named distinctly from the transport/domain
 * [app.dyrecto.domain.CameraConnectionState] to avoid import/logging/diagnostics
 * ambiguity.
 */
data class SceneConnectionState(
    val phase: ConnectionPhase = ConnectionPhase.IDLE,
    val liveViewActive: Boolean = false,
)

/** Descriptive battery state. [isBatteryLow] is a descriptive flag only — not a notification. */
data class BatteryState(
    /** Percent (0..100), or null when telemetry has not supplied a value. */
    val batteryLevel: Int? = null,
    val isBatteryLow: Boolean = false,
)

/** Descriptive storage state. [isStorageCritical] is a descriptive flag only — not an alert. */
data class StorageState(
    /** Decoded SLOT1 status / remaining string, or null when unknown. */
    val remainingStatus: String? = null,
    val isStorageCritical: Boolean = false,
)
