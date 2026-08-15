import Combine
import Foundation
import UIKit
import DyrectoShared

/// App-scoped implementation of the monitoring session — the iOS port of
/// `DefaultMonitoringSession`. Owns the connection controller, the persistent Push-LV frame
/// stream + renderer, the Vision pipeline, the Scene Understanding merge, the Shot Reference /
/// Storyboard monitor, the Alert Engine lifecycle, and the Voice Guidance scheduler — all hosted
/// on its own queues (never the UI), so monitoring and alert delivery keep running while the app
/// is backgrounded (within iOS's `audio` background-mode allowance — the stand-in for Android's
/// foreground service; `CameraMonitorService`/`MonitoringLocks` have no iOS equivalent).
///
/// Android-parity invariants preserved here:
///  - ONE alert system, many producers: telemetry (`AlertEngine`), scene, exposure and reference
///    rules share the `AlertIdGenerator` and the single [deliver] seam in the exact Android
///    order: config gate → `AlertStore` → feedback → notification → voice tap.
///  - Frame acquisition is connection-driven, not UI-driven: the push stream + renderer start on
///    the edge into "monitoring active" (`CameraConnectionState.isMonitoringActive`) and stop on
///    the falling edge. No screen starts or stops it.
///  - A new session begins on the edge into SCANNING: every alert producer + history + voice
///    state resets, and any prior live-view stream is invalidated.
///  - Phase 15: the storyboard end-of-session reminder fires once on the
///    `referenceSessionLive` (monitoringRequested && isMonitoringActive) true→false edge.
final class MonitoringSessionIos: ObservableObject {

    // MARK: Published state (mirrors the Android MonitoringSession StateFlow surface)

    @Published private(set) var state: CameraConnectionState
    @Published private(set) var sceneContext: SceneContext
    @Published private(set) var visionContext: VisionContext = SharedFactory.emptyVisionContext()
    @Published private(set) var visionStatistics = VisionStatistics(pipelineRunning: true)
    @Published private(set) var referenceState: ReferenceSessionState = SharedFactory.emptySessionState()
    @Published private(set) var referenceMatch: ReferenceMatchResult = SharedFactory.emptyMatchResult()
    @Published private(set) var aiCapabilities: AiCapabilities
    @Published private(set) var voiceDebug: VoiceDebugState
    @Published private(set) var perceptualTuning: PerceptualTuning?

    // ---- Frame renderer output (Android LiveViewRenderState surface, flattened for the UI) ----
    @Published private(set) var frame: UIImage?
    @Published private(set) var frameStreamActive = false
    @Published private(set) var displayedFps: Double = 0
    @Published private(set) var framesDisplayed: Int = 0
    @Published private(set) var pushStatusRows: [(String, String)] = []

    /// Zebra overlay image. Deliberately nil: Android renders NO zebra pixel overlay either —
    /// zebra is surfaced as coverage % (ZebraResult.mask stays null because the shared exposure
    /// module computes without generateMask). Kept for the UI contract; a real overlay needs a
    /// bulk mask bridge in shared iosMain first (never a per-pixel Swift loop).
    var zebraOverlay: UIImage? { nil }

    /// Unified realtime context (Phase 4D) for overlays/diagnostics.
    var frameContext: FrameContext { frameContextPublisher.current() }

    // MARK: Collaborators

    let connection: CameraConnectionController
    private let alertConfigStore: AlertConfigStoreIos
    private let voiceSettingsStore: VoiceSettingsStoreIos
    private let alertFeedback: IosAlertFeedback
    private let notificationDelivery: IosNotificationDelivery
    private let speechEngine: IosSpeechEngine
    private let log = DyrectoLog.shared

    /// Phase 4B/4C: the persistent frame stream (created per connection — needs the live PTP
    /// client) + the renderer that decodes it.
    private var pushSession: PushLiveViewSession?
    let frameRenderer = LiveViewFrameRenderer()
    /// HTTP-pull alternative path (prop 0xD278); feeds the same renderer.
    private var liveViewClient: LiveViewClient?

    /// Phase 4D: merges render + telemetry into one realtime FrameContext.
    private let frameContextPublisher = FrameContextPublisherIos()

    /// Phase 5A/5B: passive Vision pipeline (exposure + face/eye + AI scene modules).
    let visionPipeline = VisionPipeline()

    /// Phase 10: on-device AI perception behind the shared engine seams.
    let aiPerception: AiPerceptionCoordinator

    /// Phase 16.1: MobileCLIP2-S0 semantic expert — import-only, fail-safe.
    private let semanticEngine = IosMobileClipEngine()

    /// Phase 9/10/15: Shot Reference / Storyboard monitor.
    private(set) var referenceMonitor: ReferenceMonitorIos!

    // ---- Alerts (one system, many producers — shared id source + delivery seam) ----
    private let alertIdGen = AlertIdGenerator()
    private let alertEngine: AlertEngine
    private var sceneAlertState = SceneAlertState(
        previousScene: nil, faceTrackingActive: false, eyesNotVisibleFired: false)
    private var exposureAlertState = ExposureAlertState(
        seeded: false, highlightClipFired: false, shadowClipFired: false)
    private var referenceAlertState = ReferenceAlertState(
        seeded: false, referenceId: nil, fired: Set())

    // ---- Phase 14: Voice Guidance — output-only consumer of instructions + alerts ----
    private let voiceScheduler: VoiceScheduler
    private var voiceDebugWatcher: FlowWatcher<AnyObject>?

    /// The single-threaded home of alert evaluation + edge bookkeeping (the Android combine
    /// collector's stand-in). All `prev*/was*` vars below are confined to it.
    private let sessionQueue = DispatchQueue(label: "app.dyrecto.session", qos: .utility)
    private var prevPhase: ConnectionPhase?
    private var wasMonitoringActive = false
    private var wasReferenceSessionLive = false
    private var latestState: CameraConnectionState
    private var latestScene: SceneContext
    private var latestRefMatch: ReferenceMatchResult = SharedFactory.emptyMatchResult()
    private var latestConfigs: [AlertType: AlertConfig] = [:]
    private var lastSceneLogAtMs: Int64 = 0

    private var cancellables = Set<AnyCancellable>()

    // MARK: Init (DyrectoApp.kt startup wiring order)

    init(connection: CameraConnectionController,
         alertConfigStore: AlertConfigStoreIos,
         voiceSettingsStore: VoiceSettingsStoreIos,
         alertFeedback: IosAlertFeedback,
         notificationDelivery: IosNotificationDelivery,
         speechEngine: IosSpeechEngine) {
        self.connection = connection
        self.alertConfigStore = alertConfigStore
        self.voiceSettingsStore = voiceSettingsStore
        self.alertFeedback = alertFeedback
        self.notificationDelivery = notificationDelivery
        self.speechEngine = speechEngine

        // Phase 8 startup wiring (AnalysisLutBootstrap parity): register the Sony S-Log3
        // monitoring LUT with the process-scoped ExposureConfig BEFORE any frame is analyzed.
        // Fail-safe: a missing LUT leaves S_LOG3 resolving to Rec709 (documented fallback).
        Self.installSLog3Lut()

        let engine = AlertEngine(idGen: alertIdGen)
        self.alertEngine = engine

        // Phase 10 perception stack: Swift engine impls behind the shared manager seams.
        let perception = AiPerceptionCoordinator(
            detection: DetectionManager(engine: IosObjectDetectorEngine(),
                                        mapper: CocoSemanticMapper.shared),
            embedding: EmbeddingManager(engine: IosEmbeddingEngine()),
            segmentation: SegmentationManager(engine: IosSegmentationEngine()),
            strategy: StrategyManager(),
            tracking: TrackingManager(tracker: NccTemplateTracker(),
                                      policyConfig: SharedFactory.defaultTrackPolicy()),
            cadence: SharedFactory.defaultCadence())
        self.aiPerception = perception
        self.aiCapabilities = perception.capabilities

        let initialScene = SceneAnalyzer.shared.analyze(
            frame: SharedFactory.emptyFrameContext(),
            vision: SharedFactory.emptyVisionContext(),
            nowMs: 0)
        self.sceneContext = initialScene
        self.latestScene = initialScene
        self.state = connection.state
        self.latestState = connection.state

        // Phase 14: pure scheduler + iOS speech engine.
        let scheduler = VoiceScheduler(
            engine: speechEngine,
            clock: { KotlinLong(value: SharedFactory.nowMs()) })
        self.voiceScheduler = scheduler
        // interop: StateFlow.value exports as Any? — seed the debug snapshot from it.
        self.voiceDebug = (scheduler.debug.value as? VoiceDebugState)
            ?? VoiceDebugState(enabled: false, speaking: nil, queued: [],
                               cooldownsRemainingMs: [:], suppressedCount: 0,
                               lastSuppressedReason: nil, lastSpoken: nil, spokenCount: 0)

        // Phase 9/10/15/16.1: the reference monitor reads the newest SceneContext through a
        // provider (Android passes the StateFlow; the provider is the callback-world equivalent).
        self.referenceMonitor = ReferenceMonitorIos(
            perception: perception,
            semanticEngine: semanticEngine,
            sceneContextProvider: { [weak self] in
                self?.latestScene ?? initialScene
            })

        MonitoringSessionProviderIos.register(self)
        wire()
    }

    // MARK: Wiring (the DefaultMonitoringSession init block, 1:1)

    private func wire() {
        // Phase 5B modules + Phase 10 AI as one more Vision module: its analyze() never runs
        // inference on the Vision worker (see AiSceneModule) — exposure is never delayed.
        visionPipeline.register(ExposureVisionModule())
        visionPipeline.register(FaceEyeModule())
        visionPipeline.register(AiSceneModule(coordinator: aiPerception))

        // Phase 4D: republish a merged FrameContext whenever render or telemetry changes.
        frameRenderer.$state
            .sink { [weak self] render in
                guard let self else { return }
                self.frameContextPublisher.onRenderState(render)
                self.frame = render.frame?.image
                self.frameStreamActive = render.active
                self.displayedFps = render.displayedFps
                self.framesDisplayed = (render.frame?.index).map { $0 + 1 } ?? self.framesDisplayed
            }
            .store(in: &cancellables)

        // Phase 5A: passively feed each newly rendered frame to Vision — a read-only tap AFTER
        // rendering, paired with the latest FrameContext. The renderer's explicit frame index is
        // the frame-identity mechanism (the successor Android documented for its temporary
        // bitmap-reference comparison).
        frameRenderer.onFrame = { [weak self] rendered in
            guard let self else { return }
            self.visionPipeline.submitFrame(
                image: rendered.image,
                context: self.frameContextPublisher.current())
        }

        // Phase 5C: Scene Understanding — the single merge of FrameContext + VisionContext.
        frameContextPublisher.onContext = { [weak self] frameCtx in
            self?.sessionQueue.async { self?.recomputeScene(frameContext: frameCtx) }
        }
        visionPipeline.onContext = { [weak self] vc in
            guard let self else { return }
            DispatchQueue.main.async { self.visionContext = vc }
            // Phase 9/15: the reference comparison loop — one evaluation per analyzed frame.
            self.referenceMonitor.onFrame(vc)
            self.sessionQueue.async { self.recomputeScene(frameContext: nil) }
        }

        // Connection state → session edges + telemetry alert producer.
        connection.onStateChanged = { [weak self] connState in
            guard let self else { return }
            self.state = connState
            self.frameContextPublisher.onConnectionState(connState)
            self.sessionQueue.async {
                self.latestState = connState
                self.evaluateTick()
            }
        }

        // Mirror the pipeline / monitor / perception published state.
        visionPipeline.$statistics
            .sink { [weak self] in self?.visionStatistics = $0 }
            .store(in: &cancellables)
        aiPerception.$capabilities
            .sink { [weak self] in self?.aiCapabilities = $0 }
            .store(in: &cancellables)
        referenceMonitor.$state
            .sink { [weak self] in self?.referenceState = $0 }
            .store(in: &cancellables)
        referenceMonitor.$match
            .sink { [weak self] in self?.referenceMatch = $0 }
            .store(in: &cancellables)
        referenceMonitor.$perceptualTuning
            .sink { [weak self] in self?.perceptualTuning = $0 }
            .store(in: &cancellables)

        // ---- Phase 14: voice wiring (Android order preserved) ----
        speechEngine.setOnUtteranceFinished { [weak self] in
            self?.voiceScheduler.onUtteranceFinished()
        }
        // interop: Kotlin (Long) -> Unit closure parameter bridges as KotlinLong.
        voiceScheduler.onScheduleRecheck = { [weak self] delayMs in
            guard let self else { return }
            self.sessionQueue.asyncAfter(
                deadline: .now() + Double(truncating: delayMs) / 1000.0
            ) { self.voiceScheduler.onGapElapsed() }
        }
        voiceSettingsStore.$settings
            .sink { [weak self] settings in
                guard let self else { return }
                self.speechEngine.setSpeechRate(rate: settings.speechRate)
                self.voiceScheduler.updateSettings(s: settings)
            }
            .store(in: &cancellables)
        voiceDebugWatcher = FlowWatcher<AnyObject>(flow: voiceScheduler.debug)
        voiceDebugWatcher?.watch { [weak self] value in
            if let debug = value as? VoiceDebugState { self?.voiceDebug = debug }
        }

        // Assistant tap: the SAME Phase 13 selector verdict the UI shows, per match tick — the
        // selector is stateless, so calling it here and in the UI yields identical results.
        referenceMonitor.onMatch = { [weak self] match in
            guard let self else { return }
            self.voiceScheduler.onAssistantInstruction(
                instruction: InstructionSelector.shared.select(match: match),
                monitoringActive: match.active)
            self.sessionQueue.async {
                self.latestRefMatch = match
                self.evaluateTick()
            }
        }
        // Phase 15: "Shot completed." milestone cue when a storyboard shot flips done.
        referenceMonitor.onShotCompleted = { [weak self] _ in
            self?.voiceScheduler.onShotCompleted()
        }

        // Cache configs for the delivery seam's gate.
        alertConfigStore.$configs
            .sink { [weak self] configs in
                guard let self else { return }
                self.sessionQueue.async { self.latestConfigs = configs }
            }
            .store(in: &cancellables)
    }

    // MARK: Scene Understanding merge (SceneManager port; sessionQueue-confined)

    private func recomputeScene(frameContext: FrameContext?) {
        let frameCtx = frameContext ?? frameContextPublisher.current()
        let scene = SceneAnalyzer.shared.analyze(
            frame: frameCtx,
            vision: visionPipeline.latestContext,
            nowMs: SharedFactory.nowMs())
        latestScene = scene
        DispatchQueue.main.async { self.sceneContext = scene }
        maybeLogScene(scene)
        evaluateTick()
    }

    /// Throttled to once per second — the [SCENE] diagnostics line, Android format.
    private func maybeLogScene(_ ctx: SceneContext) {
        let now = SharedFactory.nowMs()
        if now - lastSceneLogAtMs < 1000 { return }
        lastSceneLogAtMs = now
        log.line(.info, String(
            format: "[SCENE] state=%@ recording=%@ highlight=%.0f%% shadow=%.0f%% faces=%d eyes=%d battery=%@ storage=%@",
            ctx.overallState.name,
            ctx.recording.isRecording ? "true" : "false",
            ctx.exposure.highlightPercentage,
            ctx.exposure.shadowPercentage,
            ctx.face.facesDetected,
            ctx.face.eyesDetected,
            ctx.battery.batteryLevel.map { "\($0)%" } ?? "—",
            ctx.storage.remainingStatus ?? "—"))
    }

    // MARK: Alert evaluation + edges (the Android combine collector; sessionQueue-confined)

    /// One evaluation over the latest (state, scene, refMatch) — fired whenever any of the three
    /// sources tick, exactly like the Android `combine` collector. Telemetry rules are
    /// transition-based, so frequent scene ticks can't re-fire them.
    private func evaluateTick() {
        let connState = latestState
        let scene = latestScene
        let refMatch = latestRefMatch

        // New session begins on the edge into SCANNING: reset every alert producer + history.
        if connState.phase == .scanning && prevPhase != .scanning {
            alertEngine.reset()
            alertIdGen.reset()
            sceneAlertState = SceneAlertState(
                previousScene: nil, faceTrackingActive: false, eyesNotVisibleFired: false)
            exposureAlertState = ExposureAlertState(
                seeded: false, highlightClipFired: false, shadowClipFired: false)
            referenceAlertState = ReferenceAlertState(seeded: false, referenceId: nil, fired: Set())
            AlertStore.shared.clear()
            voiceScheduler.reset() // silence + forget all voice state (Phase 14)
            stopHttpLiveView()     // a new session invalidates any prior Live View stream
            stopFrameStream()      // and any prior push stream
        }
        prevPhase = connState.phase

        // Telemetry producer.
        for alert in alertEngine.evaluate(state: connState) {
            deliver(alert, scene: false)
        }

        // Scene producer.
        // interop: Kotlin nested `Result` data classes export flattened (SceneAlertRulesResult…).
        let sceneResult = SceneAlertRules.shared.evaluate(
            state: sceneAlertState, scene: scene, idGen: alertIdGen)
        sceneAlertState = sceneResult.state
        for alert in sceneResult.alerts { deliver(alert, scene: true) }

        // Exposure producer (Phase 7).
        let exposureResult = ExposureAlertRules.shared.evaluate(
            state: exposureAlertState, scene: scene, idGen: alertIdGen)
        exposureAlertState = exposureResult.state
        for alert in exposureResult.alerts { deliver(alert, scene: true) }

        // Phase 9: Shot Reference drift alerts — same shared id source + delivery seam.
        let referenceResult = ReferenceAlertRules.shared.evaluate(
            state: referenceAlertState, match: refMatch, idGen: alertIdGen)
        referenceAlertState = referenceResult.state
        for alert in referenceResult.alerts { deliver(alert, scene: true) }

        // The persistent frame stream tracks the *active monitoring* edge, not the connect
        // attempt. (Android also toggles the foreground service here — no iOS equivalent; the
        // audio background mode substitutes.)
        let active = connState.isMonitoringActive
        if active && !wasMonitoringActive {
            startFrameStream()
        } else if !active && wasMonitoringActive {
            stopFrameStream()
        }
        wasMonitoringActive = active

        // Phase 15: storyboard end-of-session reminder on the referenceSessionLive true→false
        // edge (user Stop, disconnect, or session leave) with shots still pending.
        let refState = referenceMonitor.stateSnapshot()
        let referenceSessionLive = refState.monitoringRequested && active
        if wasReferenceSessionLive && !referenceSessionLive {
            if let alert = StoryboardAlertRules.shared.onSessionEnded(
                state: refState, now: SharedFactory.nowMs(), idGen: alertIdGen) {
                deliver(alert, scene: true)
            }
        }
        wasReferenceSessionLive = referenceSessionLive
    }

    /// Shared delivery seam — config gate (enable/disable + severity override) → history →
    /// feedback → notification → voice. Written once, used by every alert source (Android order,
    /// byte for byte).
    private func deliver(_ alert: Alert, scene: Bool) {
        let cfg = latestConfigs[alert.type] ?? AlertConfig.companion.default(type: alert.type)
        guard cfg.enabled else { return } // disabled: fully suppress (no history, no feedback)
        let resolved = alert.doCopy(
            id: alert.id, type: alert.type, severity: cfg.severity,
            title: alert.title, message: alert.message, timestamp: alert.timestamp)
        if scene { log.line(.info, "Scene alert fired: \(alert.type.name) — \(alert.message)") }
        AlertStore.shared.record(alert: resolved)
        alertFeedback.deliver(severity: cfg.severity, sound: cfg.soundPattern,
                              vibration: cfg.vibrationPattern)
        notificationDelivery.deliver(alert: resolved)
        // Phase 14: voice is one more consumer AFTER the config gate — an alert type the user
        // disabled is never spoken. The template table decides which types are voiced at all.
        voiceScheduler.onTelemetryAlert(alert: resolved)
    }

    // MARK: Frame stream lifecycle (connection-driven; sessionQueue-confined)

    private func startFrameStream() {
        guard pushSession == nil else { return }
        guard let ptp = connection.ptp, !connection.phoneIp.isEmpty else {
            log.line(.error, "PushLV not started: PTP client / phone IP unavailable")
            return
        }
        frameRenderer.start()
        let push = PushLiveViewSession(ptp: ptp, phoneIp: connection.phoneIp)
        push.onFrame = { [weak self] ref in
            // Zero-copy contract: the renderer copies the JPEG synchronously during this call.
            self?.frameRenderer.submit(ref: ref)
        }
        push.onStats = { [weak self] stats in
            self?.publishPushRows(stats: stats, outcome: nil)
        }
        push.onEnded = { [weak self] reason in
            self?.publishPushRows(stats: nil, outcome: reason)
        }
        pushSession = push
        publishPushRows(stats: PushLiveViewSession.Stats(), outcome: nil)
        push.start()
    }

    private func stopFrameStream() {
        pushSession?.stop()
        pushSession = nil
        frameRenderer.stop()
    }

    private var lastPushStats = PushLiveViewSession.Stats()
    private var lastPushOutcome: String?

    private func publishPushRows(stats: PushLiveViewSession.Stats?, outcome: String?) {
        if let stats { lastPushStats = stats }
        if let outcome { lastPushOutcome = outcome }
        let s = lastPushStats
        let rows: [(String, String)] = [
            ("Stream", pushSession != nil ? "active" : "stopped"),
            ("Frames parsed", "\(s.framesParsed)"),
            ("Bytes received", "\(s.bytesReceived)"),
            ("Parse errors", "\(s.parseErrors)"),
            ("Last keepalive", s.lastKeepAliveResult.map { "\($0)" } ?? "—"),
            ("Outcome", lastPushOutcome ?? "—"),
        ]
        DispatchQueue.main.async { self.pushStatusRows = rows }
    }

    // MARK: Connection intents

    func startScan() { connection.startScan() }
    func stopScan() { connection.stopScan() }
    func connect(to id: UUID) { connection.connect(to: id) }
    func disconnect() {
        sessionQueue.async {
            self.stopHttpLiveView()
            self.stopFrameStream()
        }
        connection.disconnect()
    }
    func setCameraIp(_ ip: String) {
        // The iOS controller derives the camera IP from the joined Wi-Fi gateway; a manual
        // override is a Developer convenience surfaced through the log for now.
        log.line(.info, "setCameraIp(\(ip)) — controller derives IP from the Wi-Fi join")
    }

    // MARK: Live View intents

    /// Manual push confirmation intents (legacy/developer parity — acquisition is normally
    /// connection-driven via the monitoring-active edge above).
    func startPushLiveView() { sessionQueue.async { self.startFrameStream() } }
    func stopPushLiveView() { sessionQueue.async { self.stopFrameStream() } }

    /// HTTP-pull alternative (prop 0xD278, gated on 0xD221) feeding the same renderer.
    func startHttpLiveView() {
        guard let url = connection.liveViewUrl else {
            log.line(.error, "LiveView: no LiveViewUrl (0xD278) in telemetry")
            return
        }
        guard LiveViewClient.isGateOpen(liveViewStatus: connection.liveViewStatusRaw) else {
            log.line(.error, "LiveView: blocked by LiveViewStatus (0xD221)")
            return
        }
        frameRenderer.start()
        let client = LiveViewClient()
        client.onJpeg = { [weak self] jpeg in self?.frameRenderer.submitJpeg(jpeg) }
        client.onEnded = { [weak self] reason in
            self?.log.line(.info, "LiveView ended: \(reason)")
        }
        liveViewClient = client
        client.start(liveViewUrl: url, cameraIp: connection.cameraIp)
    }

    func stopHttpLiveView() {
        liveViewClient?.stop()
        liveViewClient = nil
    }

    // MARK: Pairing / EE diagnostics

    func runOneTimePairing() { connection.ble.sendPairingRequest() }
    // The iOS BLE manager folds the EE02/EE04 registration reads into the pairing sequence's
    // observation window (wire-protocol §1); granular re-reads are surfaced via the log.
    func readEe02() { log.line(.info, "EE02 read is part of the pairing sequence on iOS — run Pair/Register") }
    func readEe04() { log.line(.info, "EE04 read is part of the pairing sequence on iOS — run Pair/Register") }
    func dumpEeState() { log.line(.info, "EE state is captured in the pairing sequence log lines above") }

    // MARK: Alert Control Center intents

    func updateAlertConfig(_ config: AlertConfig) { alertConfigStore.update(config) }
    func restoreAlertDefault(_ type: AlertType) { alertConfigStore.reset(type) }
    func restoreAllAlertDefaults() { alertConfigStore.resetAll() }
    func testAlert(_ config: AlertConfig) {
        alertFeedback.deliver(severity: config.severity, sound: config.soundPattern,
                              vibration: config.vibrationPattern)
    }

    // MARK: Shot Reference / Storyboard intents (Phase 9 / 15)

    func addReferenceImage(_ image: UIImage, name: String) {
        referenceMonitor.addReferenceImage(image: image, name: name)
    }
    func addReferenceImage(url: URL, name: String) {
        referenceMonitor.addReferenceImage(url: url, name: name)
    }
    func replaceReference(id: String, image: UIImage, name: String) {
        referenceMonitor.replaceReference(id: id, image: image, name: name)
    }
    func replaceReference(id: String, url: URL, name: String) {
        referenceMonitor.replaceReference(id: id, url: url, name: name)
    }
    func removeReference(id: String) { referenceMonitor.removeReference(id: id) }
    func setReferenceOptions(_ options: ReferenceMonitorOptions) { referenceMonitor.setOptions(options) }
    func setCompletionRule(_ rule: StoryboardCompletionRule) { referenceMonitor.setCompletionRule(rule) }
    func resetStoryboardProgress() { referenceMonitor.resetStoryboardProgress() }
    func startReferenceMonitoring() { referenceMonitor.startMonitoring() }
    func stopReferenceMonitoring() { referenceMonitor.stopMonitoring() }
    func clearReference() { referenceMonitor.clearReference() }

    // MARK: Human Perception intents (Phase 12)

    func setPerceptualTuning(_ tuning: PerceptualTuning?) {
        referenceMonitor.setPerceptualTuning(tuning)
    }

    // MARK: Voice Guidance intents (Phase 14)

    func setVoiceSettings(_ settings: VoiceSettings) { voiceSettingsStore.update(settings) }

    // MARK: Phase 8 LUT bootstrap (AnalysisLutBootstrap port)

    private static func installSLog3Lut() {
        let name = "1_SGamut3CineSLog3_To_LC-709"
        guard let url = Bundle.main.url(forResource: name, withExtension: "cube"),
              let text = try? String(contentsOf: url, encoding: .utf8) else {
            logError("S-Log3 LUT missing from bundle — S_LOG3 resolves to Rec709 until provided")
            return
        }
        // interop: Lut3D.parse is a companion function → Lut3D.companion.parse(text:). It is not
        // @Throws-annotated, so it is called directly — the bundled .cube is a build artifact and
        // a parse failure is a packaging defect, not a runtime condition.
        let lut = Lut3D.companion.parse(text: text)
        ExposureConfig.shared.registerSLog3Transform(
            transform: Lut3DColorTransform(lut: lut),
            lutName: "\(name).cube")
        logInfo("S-Log3 monitoring LUT registered (\(name).cube)")
    }
}

// MARK: - Monitoring-active predicate (port of the Android extension val)

extension CameraConnectionState {
    /// True once monitoring is actually established: PTP session open / device info retrieved,
    /// or at least one telemetry refresh landed. Deliberately excludes the BLE→SSH→PTP handshake
    /// window (Android `isMonitoringActive` parity).
    var isMonitoringActive: Bool {
        phase == .sessionOpen || phase == .deviceInfo || lastTelemetryUpdateAt != nil
    }
}

// MARK: - Provider (port of MonitoringSessionProvider)

/// The single process-scoped holder that vends the current session. `MonitoringSessionIos`
/// self-registers at construction (DefaultAppWiring builds exactly one at launch); `current()`
/// is safe from any thread thereafter. Swapping to per-camera instances stays a provider-level
/// change — call sites depend only on `current()`.
enum MonitoringSessionProviderIos {

    private static let lock = NSLock()
    private static var session: MonitoringSessionIos?

    static func register(_ instance: MonitoringSessionIos) {
        lock.lock(); defer { lock.unlock() }
        session = instance
    }

    /// The current session. The app wiring builds it at launch, before any UI can reach here.
    static func current() -> MonitoringSessionIos {
        lock.lock(); defer { lock.unlock() }
        guard let session else {
            preconditionFailure("MonitoringSessionProviderIos.current() before DefaultAppWiring.build()")
        }
        return session
    }
}
