package app.dyrecto.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import app.dyrecto.alerts.AlertFeedback
import app.dyrecto.capability.CameraCapabilities
import app.dyrecto.alerts.AlertNotificationDelivery
import app.dyrecto.alerts.AlertStore
import app.dyrecto.alerts.AndroidAlertFeedback
import app.dyrecto.billing.EntitlementProvider
import app.dyrecto.data.AlertConfigStore
import app.dyrecto.data.CameraRepositoryImpl
import app.dyrecto.data.VoiceSettingsStore
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraRepository
import app.dyrecto.domain.ConnectionPhase
import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertConfig
import app.dyrecto.domain.alerts.AlertEngine
import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.domain.alerts.ExposureAlertRules
import app.dyrecto.domain.alerts.ExposureAlertState
import app.dyrecto.domain.alerts.SceneAlertRules
import app.dyrecto.domain.alerts.SceneAlertState
import app.dyrecto.liveview.LiveViewSession
import app.dyrecto.liveview.LiveViewState
import app.dyrecto.liveview.reference.ReferenceAlertRules
import app.dyrecto.liveview.reference.ReferenceAlertState
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.perception.PerceptualTuning
import app.dyrecto.liveview.reference.ReferenceMonitorManager
import app.dyrecto.liveview.reference.ReferenceCameraSettings
import app.dyrecto.liveview.reference.ReferenceMonitorOptions
import app.dyrecto.liveview.reference.SettingsDriftRules
import app.dyrecto.liveview.reference.ReferenceSessionState
import app.dyrecto.liveview.reference.StoryboardAlertRules
import app.dyrecto.liveview.reference.StoryboardCompletionRule
import app.dyrecto.liveview.reference.ai.AiCapabilities
import app.dyrecto.liveview.reference.ai.AiPerceptionCoordinator
import app.dyrecto.liveview.reference.ai.detect.DetectionManager
import app.dyrecto.liveview.reference.ai.embed.EmbeddingManager
import app.dyrecto.liveview.reference.ai.mediapipe.MediaPipeEngines
import app.dyrecto.liveview.reference.ai.segment.SegmentationManager
import app.dyrecto.liveview.reference.ai.semantic.CocoSemanticMapper
import app.dyrecto.liveview.reference.ai.strategy.StrategyManager
import app.dyrecto.liveview.reference.ai.track.NccTemplateTracker
import app.dyrecto.liveview.reference.ai.track.TrackingManager
import app.dyrecto.liveview.reference.creative.semantic.MobileClipSemanticEngine
import app.dyrecto.liveview.PushLiveViewSession
import app.dyrecto.liveview.PushLvStatus
import app.dyrecto.liveview.render.LiveViewFrameRenderer
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.ssh.SshEnabled
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.scene.SceneManager
import app.dyrecto.liveview.session.FrameContextPublisher
import app.dyrecto.liveview.vision.VisionManager
import app.dyrecto.liveview.vision.VisionStatistics
import app.dyrecto.liveview.instructions.InstructionSelector
import app.dyrecto.liveview.exposure.OverlayConfig
import app.dyrecto.liveview.vision.modules.AiSceneModule
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.liveview.voice.VoiceDebugState
import app.dyrecto.liveview.voice.VoiceScheduler
import app.dyrecto.liveview.voice.VoiceSettings
import app.dyrecto.voice.AndroidTtsSpeechEngine
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-scoped implementation of [MonitoringSession]. Owns the verified connection stack (via
 * [CameraRepository]) and the Alert Engine lifecycle, both hosted on an app-scoped [sessionScope]
 * — NOT on `viewModelScope` — so monitoring and alert delivery keep running when the app is
 * backgrounded, the screen is off, or the user switches apps.
 *
 * Responsibilities moved here from the old `CameraViewModel`:
 *  - observe the connection state, feed each snapshot to the engine, apply the user's
 *    [AlertConfig] at the delivery seam (dropping disabled alerts, overriding severity), then
 *    record into [AlertStore] and deliver via [AlertFeedback];
 *  - reset the engine + history at the start of each new session (the edge into
 *    [ConnectionPhase.SCANNING]).
 *
 * It additionally drives the foreground [CameraMonitorService] lifecycle and the persistent
 * [PushLiveViewSession] frame stream: both start only on the edge into "monitoring active"
 * (PTP session open / first telemetry) and stop when the session ends. Frame acquisition is
 * connection-driven, not UI-driven — no screen (Live View included) starts or stops it; Live View
 * is a passive subscriber of [pushLiveRender], and Vision/Scene/Alerts keep consuming frames
 * whether or not Live View is on screen.
 */
class DefaultMonitoringSession(context: Context) : MonitoringSession {

    private val appContext = context.applicationContext

    private val repositoryImpl = CameraRepositoryImpl(appContext)
    private val repository: CameraRepository = repositoryImpl
    /** Main HTTP Live View source (SSH=ON bodies, e.g. A7 V): driven ONLY by the monitoring edge
     *  below, feeding [httpRenderer] → [activeRender] → the pipeline. NOT controlled by any UI screen. */
    private val liveViewSession = LiveViewSession(repositoryImpl)

    /** Dedicated HTTP Live View session for the Developer "HTTP Live View (legacy)" diagnostic screen.
     *  Fully isolated from [liveViewSession] so opening/leaving that screen can never start or stop the
     *  main Live View. It decodes locally (BitmapLiveViewRenderer in the screen); nothing else reads it. */
    private val devLiveViewSession = LiveViewSession(repositoryImpl)
    /** The persistent frame stream: owns Push Live View acquisition, decoding raw push streams
     *  to external files dir. Started/stopped automatically on the connection-state edge below —
     *  NOT by any UI screen. Renderer, Vision, Scene, and Alerts all consume its output. */
    private val pushLiveViewSession = PushLiveViewSession(repositoryImpl, appContext.getExternalFilesDir(null))

    /** Decodes the HTTP Live View path's JPEG frames (SSH=ON bodies, e.g. A7 V) into the same
     *  [LiveViewRenderState] shape the push renderer produces — so the whole pipeline is source-agnostic. */
    private val httpRenderer = LiveViewFrameRenderer()

    /** The single decoded frame source the pipeline consumes, fed by whichever live-view source is
     *  active for the connected camera: the push renderer for SSH=OFF (FX3A, unchanged) or [httpRenderer]
     *  for SSH=ON (A7 V). Selection is by [SshEnabled] at the monitoring edge; see the mirror collectors. */
    private val activeRender = MutableStateFlow(LiveViewRenderState())
    /** Shared per-session id source for every alert producer (telemetry + Scene). */
    private val alertIdGen = AlertIdGenerator()
    private val alertEngine = AlertEngine(alertIdGen)
    /** Scene-rule lifecycle state, owned here (the rules object itself is stateless). */
    private var sceneAlertState = SceneAlertState()
    /** Exposure-rule lifecycle state (Phase 7), owned here (the rules object itself is stateless). */
    private var exposureAlertState = ExposureAlertState()
    /** Reference-rule lifecycle state (Phase 9), owned here (the rules object itself is stateless). */
    private var referenceAlertState = ReferenceAlertState()
    /** Keys of settings-drift alerts currently delivered, so each fires once until it clears. */
    private var lastSettingsDriftKeys: Set<String> = emptySet()
    private val alertFeedback: AlertFeedback = AndroidAlertFeedback(appContext)
    private val alertNotification = AlertNotificationDelivery(appContext)
    private val alertConfigStore = AlertConfigStore(appContext)

    // ---- Phase 14: Voice Guidance — an output-only consumer of instructions + alerts ----
    private val voiceSettingsStore = VoiceSettingsStore(appContext)
    private val voiceEngine = AndroidTtsSpeechEngine(appContext)
    private val voiceScheduler = VoiceScheduler(voiceEngine, System::currentTimeMillis)

    /** App-scoped, UI-independent scope. A [SupervisorJob] keeps one failing collector from
     *  tearing down the session. */
    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Phase 4D: merges render + telemetry into one realtime [FrameContext] flow (read-only). */
    private val frameContextPublisher = FrameContextPublisher(sessionScope)

    /** Phase 5A: passive Vision pipeline (no analysis modules yet). Runs off its own dispatcher. */
    private val visionManager = VisionManager()

    /** Phase 10: on-device AI perception (detector/embedder/segmenter behind engine seams,
     *  detect→track→compare lifecycle). Registered as one more Vision module below; inference
     *  runs on the coordinator's own single lane, never on the Vision worker. */
    private val aiPerception: AiPerceptionCoordinator = run {
        val engines = MediaPipeEngines.create(appContext)
        AiPerceptionCoordinator(
            detection = DetectionManager(engines.detector, CocoSemanticMapper),
            embedding = EmbeddingManager(engines.embedder),
            segmentation = SegmentationManager(engines.segmenter),
            strategy = StrategyManager(),
            tracking = TrackingManager(NccTemplateTracker()),
        )
    }

    /** Phase 5C: Scene Understanding Engine — merges FrameContext + VisionContext, read-only. */
    private val sceneManager = SceneManager(
        frameContextPublisher.context,
        visionManager.visionContext,
        sessionScope,
    )

    /** Phase 16.1: MobileCLIP2-S0 semantic expert (LiteRT). Import-only; behind the engine seam and
     *  fail-safe — a missing/invalid model disables the capability without affecting import. */
    private val semanticEngine = MobileClipSemanticEngine(appContext)

    /** Phase 9/10: Shot Reference assistant — compares live Vision output against the reference,
     *  with AI perception (detect→track→compare) supplied by [aiPerception]. Phase 16.1 adds the
     *  semantic expert used only during reference import. */
    private val referenceMonitor = ReferenceMonitorManager(
        appContext,
        visionManager.visionContext,
        sceneManager.sceneContext,
        sessionScope,
        aiPerception,
        semanticEngine,
        // Capture the live camera settings for each shot at import time (null if no camera).
        cameraSettingsProvider = {
            repository.state.value.telemetry?.let { ReferenceCameraSettings.fromTelemetry(it) }
        },
    )

    override val state: StateFlow<CameraConnectionState> get() = repository.state
    override val cameraCapabilities: StateFlow<CameraCapabilities?> get() = repository.camera.capabilities

    override val alertConfigs: StateFlow<Map<AlertType, AlertConfig>> =
        alertConfigStore.configs.stateIn(
            scope = sessionScope,
            started = SharingStarted.Eagerly,
            initialValue = AlertType.entries.associateWith { AlertConfig.default(it) },
        )

    override val testPlaying: StateFlow<Boolean> get() = alertFeedback.playing

    override val liveView: StateFlow<LiveViewState> get() = devLiveViewSession.state

    override val pushLiveView: StateFlow<PushLvStatus> get() = pushLiveViewSession.status

    override val pushLiveRender: StateFlow<LiveViewRenderState> get() = activeRender

    override val frameContext: StateFlow<FrameContext> get() = frameContextPublisher.context

    override val visionStatistics: StateFlow<VisionStatistics> get() = visionManager.statistics
    override val visionContext: StateFlow<VisionContext> get() = visionManager.visionContext
    override val sceneContext: StateFlow<SceneContext> get() = sceneManager.sceneContext
    override val referenceMatch: StateFlow<ReferenceMatchResult> get() = referenceMonitor.match
    override val referenceState: StateFlow<ReferenceSessionState> get() = referenceMonitor.state
    override val aiCapabilities: StateFlow<AiCapabilities> get() = aiPerception.capabilities

    override val voiceSettings: StateFlow<VoiceSettings> =
        voiceSettingsStore.settings.stateIn(
            scope = sessionScope,
            started = SharingStarted.Eagerly,
            initialValue = VoiceSettings(),
        )

    override val voiceDebug: StateFlow<VoiceDebugState> get() = voiceScheduler.debug

    /** Tracks the foreground-service lifecycle so start/stop fire exactly on the edges. */
    private var serviceRunning = false

    init {
        // Phase 10: live AI perception as one more Vision module. Its analyze() never runs
        // inference on the Vision worker (see AiSceneModule) so exposure analysis is never delayed.
        visionManager.register(AiSceneModule(aiPerception))

        // Phase 4D: republish a merged FrameContext whenever render or telemetry changes.
        frameContextPublisher.start(activeRender, repository.state)

        // Source-select the decoded frame stream by SSH mode. Only the active source's emissions reach
        // [activeRender]: SSH=OFF (FX3A) mirrors the push renderer (path unchanged); SSH=ON (A7 V) mirrors
        // [httpRenderer], which is fed the tunneled HTTP Live View JPEGs by the bridge below.
        sessionScope.launch {
            pushLiveViewSession.renderState.collect { render ->
                if (repository.state.value.ssh.sshState != SshEnabled.ON) activeRender.value = render
            }
        }
        sessionScope.launch {
            httpRenderer.state.collect { render ->
                if (repository.state.value.ssh.sshState == SshEnabled.ON) activeRender.value = render
            }
        }
        // Bridge: decode the HTTP Live View path's JPEG frames through [httpRenderer]. Inert for FX3A
        // (its liveViewSession is never started, so no frames arrive and httpRenderer is never running).
        sessionScope.launch {
            liveViewSession.state
                .map { it.frame }
                .filterNotNull()
                .distinctUntilChanged()
                .collect { httpRenderer.submit(it.jpeg) }
        }

        // Phase 14: voice wiring. The scheduler is pure — it gets its completion signal from the
        // engine, its speech-gap timer from this scope, and its settings from the DataStore.
        voiceEngine.setOnUtteranceFinished { voiceScheduler.onUtteranceFinished() }
        voiceScheduler.onScheduleRecheck = { delayMs ->
            sessionScope.launch {
                delay(delayMs)
                voiceScheduler.onGapElapsed()
            }
        }
        sessionScope.launch {
            // Dyrecto Premium seam: voice guidance is premium-only. Forcing enabled=false here
            // silences the scheduler (updateSettings on !enabled stops speech + clears the queue)
            // without touching the user's stored preference — it comes back on upgrade.
            combine(voiceSettings, EntitlementProvider.isPremium) { settings, premium ->
                if (premium) settings else settings.copy(enabled = false)
            }.collect { settings ->
                voiceEngine.setSpeechRate(settings.speechRate)
                voiceScheduler.updateSettings(settings)
            }
        }
        // Assistant tap: the SAME Phase 13 selector verdict the UI shows, per match tick. The
        // selector is stateless, so calling it here and in the UI yields identical results.
        sessionScope.launch {
            referenceMonitor.match.collect { match ->
                voiceScheduler.onAssistantInstruction(InstructionSelector.select(match), match.active)
            }
        }
        // Phase 15: optional "Shot completed." milestone cue when a storyboard shot flips done.
        sessionScope.launch {
            referenceMonitor.completedShots.collect { voiceScheduler.onShotCompleted() }
        }

        // Dyrecto Premium: authoritative revocation (refund) while the app runs. Fires ONLY on
        // a real Premium→Free(authoritative) transition (never Unknown/startup — see
        // EntitlementReconciler.isDowngradeEdge). Stops everything premium that doesn't
        // self-clean; the storyboard reminder is separately premium-gated so this synthetic
        // stop can't fire it. Voice silencing rides the combine() in the settings collector.
        sessionScope.launch {
            var last = EntitlementProvider.downgradeEdge.value
            EntitlementProvider.downgradeEdge.collect { edge ->
                if (edge != last) {
                    last = edge
                    referenceMonitor.stopMonitoring()
                    voiceScheduler.reset()
                    OverlayConfig.setWaveform(false)
                    OverlayConfig.setFalseColor(false)
                    OverlayConfig.setFocusPeaking(false)
                    // Drop stale premium-rule latches so a later re-purchase starts clean.
                    sceneAlertState = SceneAlertState()
                    exposureAlertState = ExposureAlertState()
                    referenceAlertState = ReferenceAlertState()
                    lastSettingsDriftKeys = emptySet()
                }
            }
        }

        // Phase 5A: passively feed each newly rendered frame to the Vision pipeline. This is a
        // read-only tap *after* rendering — it observes the Bitmap the renderer already published
        // and pairs it with the latest FrameContext; it adds no second decode/parser/render path.
        //
        // TEMPORARY frame-identity mechanism: a "new frame" is detected by Bitmap *reference
        // identity* (bmp !== lastRef). This is correct only because the renderer currently allocates
        // a fresh Bitmap per displayed frame (see the matching note in FrameContextPublisher). It is
        // NOT the permanent frame-id mechanism: a future phase should replace it with an explicit
        // renderer-published renderSequence / frameId on LiveViewRenderState. Do NOT redesign the
        // renderer in Phase 5A.
        //
        // TODO(frame-stream): Vision currently taps pushLiveViewSession.renderState (the Renderer's
        // decoded output). Target architecture is for Vision (and future consumers: Histogram,
        // Zebra, False Color) to subscribe to the persistent frame stream directly, upstream of /
        // parallel to the Renderer, rather than reading Renderer output:
        //
        //   Decoded Frame
        //     ├── Renderer
        //     ├── Vision
        //     ├── Histogram
        //     ├── Zebra
        //     └── Future consumers
        //
        // Not changed in Phase R1 — the current tap is correct and sufficient for now.
        sessionScope.launch {
            var lastRef: Bitmap? = null
            activeRender.collect { render ->
                val bmp = render.frame
                if (bmp != null && bmp !== lastRef) {
                    lastRef = bmp
                    // Dyrecto Premium seam: the entire Vision/AI stack (exposure, face/eye,
                    // scene, reference matching) hangs off this single tap. Free tier skips it —
                    // rendering, telemetry, and the service edge are all upstream and unaffected.
                    if (EntitlementProvider.isPremium.value) {
                        visionManager.submitFrame(bmp, frameContextPublisher.context.value)
                    }
                }
            }
        }

        // One single-threaded collector feeds BOTH alert producers (telemetry + Scene) so they share
        // the id source and never race. `combine` re-emits on every scene tick too, but the telemetry
        // rules are transition-based (prev == current ⇒ no alert), so frequent frames can't re-fire
        // telemetry alerts. The foreground-service edge stays driven by `state` exactly as before.
        sessionScope.launch {
            var prevPhase: ConnectionPhase? = null
            var wasMonitoringActive = false
            var wasReferenceSessionLive = false
            combine(
                repository.state,
                sceneManager.sceneContext,
                referenceMonitor.match,
            ) { state, scene, refMatch -> Triple(state, scene, refMatch) }
                .collect { (state, scene, refMatch) ->
                    // New session begins on the edge into SCANNING: reset every alert producer + history.
                    if (state.phase == ConnectionPhase.SCANNING && prevPhase != ConnectionPhase.SCANNING) {
                        alertEngine.reset()
                        alertIdGen.reset()
                        sceneAlertState = SceneAlertState()
                        exposureAlertState = ExposureAlertState()
                        referenceAlertState = ReferenceAlertState()
                        lastSettingsDriftKeys = emptySet()
                        AlertStore.clear()
                        voiceScheduler.reset() // silence + forget all voice state (Phase 14)
                        liveViewSession.stop() // a new session invalidates any prior Live View stream
                        devLiveViewSession.stop() // and any lingering dev HTTP Live View diagnostic
                        httpRenderer.stop() // and its decode loop (SSH=ON HTTP path)
                        pushLiveViewSession.stop() // and any prior Push Live View PoC
                    }
                    prevPhase = state.phase

                    val configs = alertConfigs.value
                    // Shared delivery seam: config gate (enable/disable + severity override) → history
                    // → feedback → notification. Written once, used by every alert source.
                    fun deliver(alert: Alert, scene: Boolean) {
                        val cfg = configs[alert.type] ?: AlertConfig.default(alert.type)
                        if (!cfg.enabled) return // disabled: fully suppress (no history, no feedback)
                        val resolved = alert.copy(severity = cfg.severity)
                        if (scene) Log.d(TAG, "Scene alert fired: ${alert.type} — ${alert.message}")
                        AlertStore.record(resolved)
                        alertFeedback.deliver(cfg.severity, cfg.soundPattern, cfg.vibrationPattern)
                        alertNotification.deliver(resolved)
                        // Phase 14: voice is one more consumer AFTER the config gate — an alert
                        // type the user disabled is never spoken. The scheduler's own template
                        // table decides which types are voiced at all (REFERENCE_* never is).
                        voiceScheduler.onTelemetryAlert(resolved)
                    }

                    alertEngine.evaluate(state).forEach { deliver(it, scene = false) }

                    // Dyrecto Premium seam: everything below evaluates intelligence-derived
                    // alerts (scene/exposure/reference/settings-drift). Free tier keeps the
                    // telemetry alerts above and the service edge below; gating per-rule (not
                    // inside deliver()) also keeps each rule's latch state from advancing.
                    val premium = EntitlementProvider.isPremium.value
                    if (premium) {
                    val sceneResult = SceneAlertRules.evaluate(sceneAlertState, scene, alertIdGen)
                    sceneAlertState = sceneResult.state
                    sceneResult.alerts.forEach { deliver(it, scene = true) }

                    val exposureResult = ExposureAlertRules.evaluate(exposureAlertState, scene, alertIdGen)
                    exposureAlertState = exposureResult.state
                    exposureResult.alerts.forEach { deliver(it, scene = true) }

                    // Phase 9: Shot Reference drift alerts — same shared id source + delivery seam.
                    val referenceResult = ReferenceAlertRules.evaluate(referenceAlertState, refMatch, alertIdGen)
                    referenceAlertState = referenceResult.state
                    referenceResult.alerts.forEach { deliver(it, scene = true) }

                    // Reference-linked camera-settings drift: the winning shot's captured settings vs
                    // the live camera telemetry (authoritative, not pixel-estimated). Each drift is
                    // delivered once through the same seam and re-arms only after it clears.
                    val liveTelemetry = state.telemetry
                    val winnerSettings = referenceMonitor.state.value.session
                        ?.profiles?.firstOrNull { it.id == refMatch.referenceId }?.cameraSettings
                    if (state.isMonitoringActive && liveTelemetry != null && winnerSettings != null && !winnerSettings.isEmpty) {
                        val drifts = SettingsDriftRules.evaluate(
                            winnerSettings, ReferenceCameraSettings.fromTelemetry(liveTelemetry),
                        )
                        val keys = drifts.map { "${it.alertType}:${it.label}" }.toSet()
                        drifts.filter { "${it.alertType}:${it.label}" !in lastSettingsDriftKeys }.forEach { d ->
                            deliver(
                                Alert(
                                    id = alertIdGen.next(),
                                    type = d.alertType,
                                    severity = d.alertType.defaultSeverity,
                                    title = d.alertType.title,
                                    message = "${d.label}: ${d.referenceValue} → ${d.liveValue}",
                                    timestamp = System.currentTimeMillis(),
                                ),
                                scene = true,
                            )
                        }
                        lastSettingsDriftKeys = keys
                    } else {
                        lastSettingsDriftKeys = emptySet()
                    }
                    } // end premium alert rules

                    // Foreground service and the persistent frame stream both track the *active
                    // monitoring* edge, not the connect attempt. Frame acquisition is driven purely
                    // by connection state here — no UI screen starts or stops it (see
                    // startPushLiveView/stopPushLiveView legacy KDoc).
                    val active = state.isMonitoringActive
                    if (active && !wasMonitoringActive) {
                        startMonitorService()
                        // Frame source by SSH mode: SSH=ON (A7 V) pulls MJPEG over the SSH tunnel via the
                        // HTTP Live View path (its push stream isn't decodable JPEG); SSH=OFF (FX3A) uses
                        // the push stream exactly as before.
                        if (state.ssh.sshState == SshEnabled.ON) {
                            httpRenderer.start()
                            liveViewSession.start()
                        } else {
                            pushLiveViewSession.start()
                        }
                    } else if (!active && wasMonitoringActive) {
                        stopMonitorService()
                        // Stop whichever was running (idempotent; the unused one was never started).
                        liveViewSession.stop()
                        httpRenderer.stop()
                        pushLiveViewSession.stop()
                    }
                    wasMonitoringActive = active

                    // Phase 15: storyboard end-of-session reminder. A live reference session is
                    // "monitoring requested AND camera actively monitoring"; when that edge falls
                    // (user Stop, disconnect, or session leave) with shots still pending, fire one
                    // reminder through the same delivery seam. All shots done → silent.
                    val refState = referenceMonitor.state.value
                    val referenceSessionLive = refState.monitoringRequested && active
                    // Premium-gated (and thereby suppressed on the synthetic falling edge an
                    // entitlement downgrade produces — a revocation must not fire a reminder).
                    if (premium && wasReferenceSessionLive && !referenceSessionLive) {
                        StoryboardAlertRules
                            .onSessionEnded(refState, System.currentTimeMillis(), alertIdGen)
                            ?.let { deliver(it, scene = true) }
                    }
                    wasReferenceSessionLive = referenceSessionLive
                }
        }
    }

    // ---- Connection intents ----
    override suspend fun setCameraProperty(code: Int, value: Long) =
        repository.camera.control.setProperty(code, value)

    override fun startScan() = repository.startScan()
    override fun stopScan() = repository.stopScan()
    override fun selectCamera(address: String) = repository.connectToCamera(address)
    override fun connect() = repository.connect()
    override fun disconnect() {
        liveViewSession.stop()
        devLiveViewSession.stop()
        httpRenderer.stop()
        pushLiveViewSession.stop()
        repository.disconnect()
    }
    override fun setCameraIp(ip: String) = repository.setCameraIp(ip)

    // ---- Live View intents (Developer "HTTP Live View (legacy)" screen ONLY) ----
    // These drive the isolated [devLiveViewSession], never the monitoring-driven main source, so the
    // diagnostic screen's enter/leave can't disturb the main Live View.
    override fun startLiveView() = devLiveViewSession.start()
    override fun stopLiveView() = devLiveViewSession.stop()

    // ---- Push Live View PoC intents (legacy/developer-only) ----
    // Frame acquisition is normally driven automatically by connection state (see the
    // isMonitoringActive edge above). These are retained only as a manual confirmation no-op for
    // the PushLiveViewPocScreen diagnostic tool. TODO: remove from the public MonitoringSession
    // interface once that screen's manual entry point is migrated or retired.
    override fun startPushLiveView() = pushLiveViewSession.start()
    override fun stopPushLiveView() = pushLiveViewSession.stop()

    // ---- Developer intents ----
    override fun runOneTimePairing() = repository.runOneTimePairing()
    override fun readEe02() = repository.readEe02()
    override fun readEe04() = repository.readEe04()
    override fun dumpEeState() = repository.dumpEeState()

    // ---- Alert Control Center intents ----
    override fun updateAlertConfig(config: AlertConfig) {
        sessionScope.launch { alertConfigStore.update(config) }
    }

    override fun restoreAlertDefault(type: AlertType) {
        sessionScope.launch { alertConfigStore.restoreDefault(type) }
    }

    override fun restoreAllAlertDefaults() {
        sessionScope.launch { alertConfigStore.restoreAllDefaults() }
    }

    override fun testAlert(config: AlertConfig) {
        alertFeedback.deliver(config.severity, config.soundPattern, config.vibrationPattern)
    }

    // ---- Shot Reference / Storyboard intents (Phase 9 / 15) ----
    override fun addReferenceImage(uri: android.net.Uri, name: String) =
        referenceMonitor.addReferenceImage(uri, name)

    override fun replaceReference(id: String, uri: android.net.Uri, name: String) =
        referenceMonitor.replaceReference(id, uri, name)

    override fun removeReference(id: String) = referenceMonitor.removeReference(id)

    override fun setReferenceOptions(options: ReferenceMonitorOptions) =
        referenceMonitor.setOptions(options)

    override fun setCompletionRule(rule: StoryboardCompletionRule) =
        referenceMonitor.setCompletionRule(rule)

    override fun resetStoryboardProgress() = referenceMonitor.resetStoryboardProgress()

    override fun startReferenceMonitoring() {
        // Dyrecto Premium seam: reference/storyboard monitoring is premium-only. Guarded here
        // (not just in UI) so no non-UI path can start it.
        if (!EntitlementProvider.isPremium.value) return
        referenceMonitor.startMonitoring()
    }
    override fun stopReferenceMonitoring() = referenceMonitor.stopMonitoring()
    override fun clearReference() = referenceMonitor.clearReference()

    // ---- Human Perception intents (Phase 12) ----
    override val perceptualTuning: StateFlow<PerceptualTuning?>
        get() = referenceMonitor.perceptualTuning

    override fun setPerceptualTuning(tuning: PerceptualTuning?) =
        referenceMonitor.setPerceptualTuning(tuning)

    // ---- Voice Guidance intents (Phase 14) ----
    override fun setVoiceSettings(settings: VoiceSettings) {
        sessionScope.launch { voiceSettingsStore.update(settings) }
    }

    // ---- Foreground service control ----

    private fun startMonitorService() {
        if (serviceRunning) return
        serviceRunning = true
        ContextCompat.startForegroundService(
            appContext,
            Intent(appContext, CameraMonitorService::class.java),
        )
    }

    private fun stopMonitorService() {
        if (!serviceRunning) return
        serviceRunning = false
        appContext.stopService(Intent(appContext, CameraMonitorService::class.java))
    }
}

/**
 * True once monitoring is actually established — the foreground service's start condition.
 * Either the PTP session is open / device info retrieved, or at least one telemetry refresh has
 * landed. Deliberately excludes the BLE→SSH→PTP handshake window, which stays foreground-only.
 */
val CameraConnectionState.isMonitoringActive: Boolean
    get() = phase == ConnectionPhase.SESSION_OPEN ||
        phase == ConnectionPhase.DEVICE_INFO ||
        lastTelemetryUpdateAt != null

private const val TAG = "MonitoringSession"
