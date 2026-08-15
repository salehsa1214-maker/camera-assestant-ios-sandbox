package app.dyrecto.liveview.scene

import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.results.VisionContext

/**
 * Pure, stateless, latest-wins interpreter (Phase 5C). Merges one [FrameContext] and one
 * [VisionContext] into an immutable [SceneContext].
 *
 * No Android UI, no rendering, no coroutines, no I/O. All thresholds here are *descriptive
 * presence flags* only — they decide how to describe the scene, never whether to alert.
 */
object SceneAnalyzer {

    // Descriptive presence threshold (NOT an alert threshold).
    private const val LOW_BATTERY_PCT = 20

    fun analyze(frame: FrameContext, vision: VisionContext, nowMs: Long): SceneContext {
        val exposure = exposure(vision)
        val face = face(vision)
        val recording = recording(frame.telemetry)
        val connection = SceneConnectionState(
            phase = frame.connectionPhase,
            liveViewActive = frame.liveViewActive,
        )
        val battery = battery(frame.telemetry)
        val storage = storage(frame.telemetry)

        return SceneContext(
            updatedAtMs = nowMs,
            exposure = exposure,
            face = face,
            recording = recording,
            connection = connection,
            battery = battery,
            storage = storage,
            overallState = overallState(exposure, face, recording, battery, storage),
        )
    }

    /**
     * Derives the descriptive [ExposureState] from the Phase 6 exposure engine
     * ([app.dyrecto.liveview.vision.results.ExposureResult]) — highlight from Zebra
     * coverage, shadow from the Histogram shadow distribution. The public [ExposureState] shape is
     * unchanged, so Scene/Alert consumers ("only the detection source changes") stay intact.
     *
     * Phase 7: [ExposureState.isHighlightClipped] / [ExposureState.isShadowClipped] now reflect the
     * *confirmed* (debounced, multi-frame) state from
     * [app.dyrecto.liveview.exposure.ExposureStateMachine] — `highlightConfirmed` /
     * `shadowConfirmed` — rather than the single-frame `highlightDetected` / `shadowDetected`. This
     * keeps a single source of truth: overlay/UI and alerts (via
     * [app.dyrecto.domain.alerts.ExposureAlertRules]) now agree on what "clipped"
     * means.
     */
    private fun exposure(vision: VisionContext): ExposureState {
        val exposure = vision.exposure
        return ExposureState(
            highlightPercentage = exposure?.highlightCoverage ?: 0f,
            shadowPercentage = exposure?.shadowCoverage ?: 0f,
            isHighlightClipped = exposure?.highlightConfirmed ?: false,
            isShadowClipped = exposure?.shadowConfirmed ?: false,
        )
    }

    private fun face(vision: VisionContext): FaceState {
        val faces = vision.faces?.facesDetected ?: 0
        val eyes = vision.eyes?.eyesDetected ?: 0
        return FaceState(
            facesDetected = faces,
            eyesDetected = eyes,
            hasVisibleFace = faces > 0,
            hasVisibleEyes = eyes > 0,
        )
    }

    private fun recording(telemetry: CameraTelemetry?): RecordingState {
        val isRecording = telemetry?.get(CameraTelemetry.MOVIE_REC)?.rawNumber == 1L
        val durationAvailable = telemetry?.get(CameraTelemetry.REC_TIME)?.rawNumber != null
        return RecordingState(
            isRecording = isRecording,
            recordingDurationAvailable = durationAvailable,
        )
    }

    private fun battery(telemetry: CameraTelemetry?): BatteryState {
        val level = telemetry?.get(CameraTelemetry.BATTERY)?.rawNumber?.toInt()
        return BatteryState(
            batteryLevel = level,
            isBatteryLow = level != null && level <= LOW_BATTERY_PCT,
        )
    }

    private fun storage(telemetry: CameraTelemetry?): StorageState {
        val statusProp = telemetry?.get(CameraTelemetry.SLOT1_STATUS)
        val status = statusProp?.decoded ?: statusProp?.rawValue
        val statusRaw = statusProp?.rawNumber
        val remain = telemetry?.get(CameraTelemetry.SLOT1_REMAIN)?.rawNumber
        // 1L = OK; 2/3/4 = no-card/error/locked. Critical if not-OK, or zero remaining.
        val critical = (statusRaw != null && statusRaw != 1L) || remain == 0L
        return StorageState(
            remainingStatus = status,
            isStorageCritical = critical,
        )
    }

    /**
     * Selects one descriptive [SceneState]. The order below is an arbitrary descriptive
     * selection (first match wins) — it is NOT a severity or priority ordering and carries no
     * alerting meaning.
     */
    private fun overallState(
        exposure: ExposureState,
        face: FaceState,
        recording: RecordingState,
        battery: BatteryState,
        storage: StorageState,
    ): SceneState = when {
        battery.isBatteryLow -> SceneState.LOW_BATTERY
        storage.isStorageCritical -> SceneState.STORAGE_LOW
        recording.isRecording -> SceneState.RECORDING
        face.hasVisibleFace -> SceneState.FACE_VISIBLE
        exposure.isHighlightClipped -> SceneState.HIGHLIGHT_PRESENT
        exposure.isShadowClipped -> SceneState.SHADOW_PRESENT
        else -> SceneState.NORMAL
    }
}
