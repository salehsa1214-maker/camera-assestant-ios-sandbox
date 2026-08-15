package app.dyrecto.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.dyrecto.capability.CameraCapabilities
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.alerts.AlertConfig
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.LiveViewState
import app.dyrecto.liveview.PushLvStatus
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.VisionStatistics
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.service.MonitoringSession
import app.dyrecto.service.MonitoringSessionProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin, UI-scoped observer over the process-scoped [MonitoringSession]. It owns no session state:
 * every property and intent delegates to the session, which lives beyond the Activity/ViewModel
 * lifecycle (so monitoring + alerts survive backgrounding and screen-off). When the Activity is
 * recreated, a new ViewModel simply re-attaches to the same live session via
 * [MonitoringSessionProvider].
 */
class CameraViewModel(app: Application) : AndroidViewModel(app) {

    private val session: MonitoringSession = MonitoringSessionProvider.current()

    /** Per-alert configuration (always one entry per [AlertType]), for the Alert Control Center. */
    val alertConfigs: StateFlow<Map<AlertType, AlertConfig>> get() = session.alertConfigs

    /** True while a sound/haptic pattern is playing — lets the UI disable the Test button. */
    val testPlaying: StateFlow<Boolean> get() = session.testPlaying

    val state: StateFlow<CameraConnectionState> get() = session.state
    val cameraCapabilities: StateFlow<CameraCapabilities?> get() = session.cameraCapabilities

    /** Renderer-neutral Live View state (encoded JPEG frames + status). */
    val liveView: StateFlow<LiveViewState> get() = session.liveView

    suspend fun setCameraProperty(code: Int, value: Long) = session.setCameraProperty(code, value)

    fun startScan() = session.startScan()
    fun stopScan() = session.stopScan()
    fun selectCamera(address: String) = session.selectCamera(address)
    fun connect() = session.connect()
    fun disconnect() = session.disconnect()
    fun setCameraIp(ip: String) = session.setCameraIp(ip)

    // ---- Live View intents ----
    fun startLiveView() = session.startLiveView()
    fun stopLiveView() = session.stopLiveView()

    /** Developer-only Push Live View PoC status (ports, Start rc, byte counters, outcome). */
    val pushLiveView: StateFlow<PushLvStatus> get() = session.pushLiveView
    /** Developer-only Push Live View render state (latest decoded frame + metrics). */
    val pushLiveRender: StateFlow<LiveViewRenderState> get() = session.pushLiveRender
    /** Unified realtime context (Phase 4D): render metrics + structured telemetry + connection. */
    val frameContext: StateFlow<FrameContext> get() = session.frameContext
    /** Phase 5A: passive Vision pipeline health (Developer diagnostics only). */
    val visionStatistics: StateFlow<VisionStatistics> get() = session.visionStatistics
    /** Phase 5B: aggregated Vision module results (Developer diagnostics + future consumers). */
    val visionContext: StateFlow<VisionContext> get() = session.visionContext
    /** Phase 5C: interpreted Scene Understanding context (Developer diagnostics + future consumers). */
    val sceneContext: StateFlow<SceneContext> get() = session.sceneContext
    fun startPushLiveView() = session.startPushLiveView()
    fun stopPushLiveView() = session.stopPushLiveView()

    fun runOneTimePairing() = session.runOneTimePairing()
    fun readEe02() = session.readEe02()
    fun readEe04() = session.readEe04()
    fun dumpEeState() = session.dumpEeState()

    // ---- Shot Reference (Phase 9) ----

    /** Live comparison of the current shot against the active Shot Reference. */
    val referenceMatch: StateFlow<ReferenceMatchResult> get() = session.referenceMatch
    /** Shot Reference control state (active profile, monitoring flag, analysis status). */
    val referenceState: StateFlow<ReferenceSessionState> get() = session.referenceState
    /** Phase 10: AI perception model availability (Developer diagnostics). */
    val aiCapabilities: StateFlow<app.dyrecto.liveview.reference.ai.AiCapabilities>
        get() = session.aiCapabilities

    fun addReferenceImage(uri: android.net.Uri, name: String) = session.addReferenceImage(uri, name)
    fun replaceReference(id: String, uri: android.net.Uri, name: String) =
        session.replaceReference(id, uri, name)
    fun removeReference(id: String) = session.removeReference(id)
    fun setReferenceOptions(options: ReferenceMonitorOptions) = session.setReferenceOptions(options)
    fun setCompletionRule(rule: app.dyrecto.liveview.reference.StoryboardCompletionRule) =
        session.setCompletionRule(rule)
    fun resetStoryboardProgress() = session.resetStoryboardProgress()
    fun startReferenceMonitoring() = session.startReferenceMonitoring()
    fun stopReferenceMonitoring() = session.stopReferenceMonitoring()
    fun clearReference() = session.clearReference()

    // ---- Human Perception (Phase 12) ----

    /** Runtime perceptual tuning override (null = tolerance defaults); Developer panel only. */
    val perceptualTuning: StateFlow<app.dyrecto.liveview.perception.PerceptualTuning?>
        get() = session.perceptualTuning

    fun setPerceptualTuning(tuning: app.dyrecto.liveview.perception.PerceptualTuning?) =
        session.setPerceptualTuning(tuning)

    // ---- Voice Guidance (Phase 14) ----

    /** Persisted voice guidance settings (master switch, source mode, speech rate). */
    val voiceSettings: StateFlow<app.dyrecto.liveview.voice.VoiceSettings>
        get() = session.voiceSettings

    /** Voice scheduler observability for the Developer card. */
    val voiceDebug: StateFlow<app.dyrecto.liveview.voice.VoiceDebugState>
        get() = session.voiceDebug

    fun setVoiceSettings(settings: app.dyrecto.liveview.voice.VoiceSettings) =
        session.setVoiceSettings(settings)

    // ---- Alert Control Center intents ----

    fun updateAlertConfig(config: AlertConfig) = session.updateAlertConfig(config)
    fun restoreAlertDefault(type: AlertType) = session.restoreAlertDefault(type)
    fun restoreAllAlertDefaults() = session.restoreAllAlertDefaults()
    fun testAlert(config: AlertConfig) = session.testAlert(config)
}
