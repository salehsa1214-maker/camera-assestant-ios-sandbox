package app.dyrecto.service

import android.net.Uri
import app.dyrecto.capability.CameraCapabilities
import app.dyrecto.capability.ControlResult
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.alerts.AlertConfig
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.LiveViewState
import app.dyrecto.liveview.perception.PerceptualTuning
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.reference.StoryboardCompletionRule
import app.dyrecto.liveview.reference.ai.AiCapabilities
import app.dyrecto.liveview.PushLvStatus
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.VisionStatistics
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.liveview.voice.VoiceDebugState
import app.dyrecto.liveview.voice.VoiceSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-scoped owner of an active camera monitoring session.
 *
 * This is the contract the UI ([app.dyrecto.ui.CameraViewModel]) and the
 * foreground service ([CameraMonitorService]) both depend on — neither owns the session.
 * Session lifecycle (the connection stack, the Alert Engine, and the alert-evaluation
 * coroutine) lives behind this interface on an app-scoped scope, so it survives Activity
 * rotation, backgrounding, and screen-off.
 *
 * Reached through [MonitoringSessionProvider]; the UI never holds a direct reference, which
 * keeps per-camera / swappable session instances a provider-level change rather than a rewrite.
 */
interface MonitoringSession {

    /** Live, immutable connection + telemetry state for the whole pipeline. */
    val state: StateFlow<CameraConnectionState>

    /** Camera-agnostic capability snapshot (null until first capability read). */
    val cameraCapabilities: StateFlow<CameraCapabilities?>

    /** Per-alert configuration (always one entry per [AlertType]), for the Alert Control Center. */
    val alertConfigs: StateFlow<Map<AlertType, AlertConfig>>

    /** True while a sound/haptic pattern is playing — lets the UI disable the Test button. */
    val testPlaying: StateFlow<Boolean>

    /** Renderer-neutral Live View pipeline state (encoded JPEG frames + status). */
    val liveView: StateFlow<LiveViewState>

    /** Persistent frame stream status (ports, Start rc, byte counters, outcome). Started/stopped
     *  automatically on connection state — see [startPushLiveView]/[stopPushLiveView]. */
    val pushLiveView: StateFlow<PushLvStatus>

    /** Persistent frame stream render state (latest decoded frame + render metrics). Live View and
     *  any other UI subscribe to this passively; none of them start or stop acquisition. */
    val pushLiveRender: StateFlow<LiveViewRenderState>

    /**
     * Unified realtime context (Phase 4D): latest render metrics + latest structured telemetry +
     * connection status, merged latest-wins. Carries no bitmap — the image still comes from
     * [pushLiveRender]. Foundation for future overlays / CV / alerts.
     */
    val frameContext: StateFlow<FrameContext>

    /**
     * Phase 5A: passive Vision pipeline health (frames received/analyzed/dropped, timing, FPS).
     * Read-only; surfaced in the Developer diagnostics screen.
     */
    val visionStatistics: StateFlow<VisionStatistics>

    /**
     * Phase 5B: aggregated latest results from all Vision modules (highlight/shadow clipping,
     * face and eye detection). Updated after every analyzed frame. Read-only; surfaced in the
     * Developer diagnostics screen. Future phases (Assistant, Overlay, Voice) consume this.
     */
    val visionContext: StateFlow<VisionContext>

    /**
     * Phase 5C: interpreted Scene Understanding context — the single merge of [frameContext] and
     * [visionContext] into one descriptive answer to "what is currently happening?". Makes no
     * decisions; read-only and surfaced in the Developer diagnostics screen. Future phases
     * (Alert Engine, Overlay, Assistant) consume this instead of the two underlying streams.
     */
    val sceneContext: StateFlow<SceneContext>

    /**
     * Phase 9: live comparison of the current shot against the active Shot Reference (overall
     * score + per-signal drift/confirmation state). Inactive default when no reference is being
     * monitored. Consumed by the Shot Reference page and Developer diagnostics.
     */
    val referenceMatch: StateFlow<ReferenceMatchResult>

    /** Phase 9: Shot Reference control state (active profile, monitoring flag, analysis status). */
    val referenceState: StateFlow<ReferenceSessionState>

    /**
     * Phase 10: AI perception model availability (detector / embedder / segmenter status +
     * load errors). Read-only; surfaced in the Developer diagnostics screen.
     */
    val aiCapabilities: StateFlow<AiCapabilities>

    /**
     * Phase 12: runtime Human Perception tuning override (null = tolerance defaults). In-memory
     * only — the Developer tuning panel edits it for on-device calibration; never persisted.
     */
    val perceptualTuning: StateFlow<PerceptualTuning?>

    /**
     * Phase 14: persisted voice guidance settings (master switch OFF by default, source mode,
     * speech rate). Voice is an output-only consumer of the existing instruction + alert streams.
     */
    val voiceSettings: StateFlow<VoiceSettings>

    /** Phase 14: voice scheduler observability (queue, speaking, cooldowns) for the Developer card. */
    val voiceDebug: StateFlow<VoiceDebugState>

    // ---- Connection intents ----
    fun startScan()
    fun stopScan()
    fun selectCamera(address: String)
    fun connect()
    fun disconnect()
    fun setCameraIp(ip: String)

    // ---- Camera control intents (writable settings) ----
    /**
     * Sets a writable camera property to [value] (validated against the capability snapshot before
     * the wire). Returns the outcome — including [app.dyrecto.capability.ControlResult.NotEnabled]
     * while the write path is hardware-gated, and [app.dyrecto.capability.ControlResult.Rejected]
     * for non-writable/unavailable/out-of-range requests.
     */
    suspend fun setCameraProperty(code: Int, value: Long): ControlResult

    // ---- Live View intents ----
    fun startLiveView()
    fun stopLiveView()

    // ---- Push Live View PoC intents (legacy/developer-only) ----
    // Frame acquisition is normally driven automatically by connection state — connect() starts
    // it, disconnect() stops it. These are retained only as a manual confirmation no-op for the
    // PushLiveViewPocScreen diagnostic tool.
    // TODO: remove from this interface once that screen's manual entry point is migrated/retired.
    fun startPushLiveView()
    fun stopPushLiveView()

    // ---- Developer intents ----
    fun runOneTimePairing()
    fun readEe02()
    fun readEe04()
    fun dumpEeState()

    // ---- Alert Control Center intents ----
    fun updateAlertConfig(config: AlertConfig)
    fun restoreAlertDefault(type: AlertType)
    fun restoreAllAlertDefaults()
    fun testAlert(config: AlertConfig)

    // ---- Shot Reference / Storyboard intents (Phase 9 / 15) ----
    /** Appends a new shot to the storyboard (analyzes the picked image on a background dispatcher). */
    fun addReferenceImage(uri: Uri, name: String)
    /** Replaces storyboard shot [id] in place with a freshly analyzed image. */
    fun replaceReference(id: String, uri: Uri, name: String)
    /** Removes storyboard shot [id] (and its stored image/embedding). */
    fun removeReference(id: String)
    fun setReferenceOptions(options: ReferenceMonitorOptions)
    /** Phase 15: updates the storyboard-wide completion rule (hold / confirm / threshold). */
    fun setCompletionRule(rule: StoryboardCompletionRule)
    /** Phase 15: clears every shot's completion progress back to Pending. */
    fun resetStoryboardProgress()
    fun startReferenceMonitoring()
    fun stopReferenceMonitoring()
    fun clearReference()

    // ---- Human Perception intents (Phase 12) ----
    fun setPerceptualTuning(tuning: PerceptualTuning?)

    // ---- Voice Guidance intents (Phase 14) ----
    fun setVoiceSettings(settings: VoiceSettings)
}
