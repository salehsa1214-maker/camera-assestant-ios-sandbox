import Combine
import SwiftUI
import UIKit
import DyrectoShared

// MARK: - The thin session contract the UI programs against
//
// The orchestration layer (iosApp/Dyrecto/{Vision,Reference,Session}/) provides
// `MonitoringSessionIos` — the iOS counterpart of Android's `MonitoringSession` +
// `MonitoringSessionProvider`. It owns the connection controller, the vision pipeline, the
// reference monitor, the alert engine wiring and the frame renderer, and exposes @Published
// state. The UI depends on this protocol only; `MonitoringSessionIos` is expected to conform
// (a one-line `extension MonitoringSessionIos: MonitoringSessionContract {}` if it doesn't
// already). If any member name mismatches at first compile it's a one-line fix here or there.
@MainActor
protocol MonitoringSessionContract: ObservableObject {
    // ---- published state (mirrors CameraViewModel.kt's StateFlow surface) ----
    var state: CameraConnectionState { get }                 // interop: shared domain type
    var sceneContext: SceneContext { get }
    var visionContext: VisionContext { get }
    var visionStatistics: VisionStatistics { get }
    var referenceState: ReferenceSessionState { get }
    var referenceMatch: ReferenceMatchResult { get }
    var aiCapabilities: AiCapabilities { get }
    var voiceDebug: VoiceDebugState { get }

    // ---- frame renderer output (Android LiveViewRenderState equivalent) ----
    /// Latest decoded live-view frame (nil before the stream produces one).
    var frame: UIImage? { get }
    /// True while the push live-view stream is running (Android `LiveViewRenderState.active`).
    var frameStreamActive: Bool { get }
    /// Displayed FPS + total frames rendered, for the Live View status chips / Developer stats.
    var displayedFps: Double { get }
    var framesDisplayed: Int { get }
    /// Zebra stripe overlay pre-rendered from `ZebraResult.mask` (per-pixel work stays in the
    /// renderer — the Swift↔Kotlin bridge is never looped per pixel). Nil when zebra is quiet.
    var zebraOverlay: UIImage? { get }
    /// Developer-only push-LV diagnostics rows (Android PushLvStatus flattened for display):
    /// ordered (label, value) pairs — phase, ports, Start rc, byte counters, outcome.
    var pushStatusRows: [(String, String)] { get }

    // ---- connection intents ----
    func startScan()
    func stopScan()
    func connect(to id: UUID)
    func disconnect()
    func setCameraIp(_ ip: String)

    // ---- live view intents ----
    func startPushLiveView()
    func stopPushLiveView()

    // ---- pairing / EE diagnostics ----
    func runOneTimePairing()
    func readEe02()
    func readEe04()
    func dumpEeState()

    // ---- storyboard / reference intents (Phase 9/10/15) ----
    func addReferenceImage(_ image: UIImage, name: String)
    func replaceReference(id: String, image: UIImage, name: String)
    func removeReference(id: String)
    func setReferenceOptions(_ options: ReferenceMonitorOptions)
    func setCompletionRule(_ rule: StoryboardCompletionRule)
    func resetStoryboardProgress()
    func startReferenceMonitoring()
    func stopReferenceMonitoring()
    func clearReference()

    // ---- alert control center intents ----
    func updateAlertConfig(_ config: AlertConfig)
    func restoreAlertDefault(_ type: AlertType)
    func restoreAllAlertDefaults()
    func testAlert(_ config: AlertConfig)

    // ---- voice ----
    func setVoiceSettings(_ settings: VoiceSettings)
}

/// The shared Kotlin `Alert` collides with SwiftUI's `Alert` in files importing both — UI code
/// uses this alias (interop-conventions rule: check collisions before declaring Swift types).
typealias DyrectoAlert = DyrectoShared.Alert

// MARK: - Flow → ObservableObject adapters
//
// Where the parallel layer doesn't already publish, small adapters watch shared Kotlin
// StateFlows via the iosMain `FlowWatcher` (delivers on main; `close()` cancels).

/// Observes the shared `AlertStore.alerts` StateFlow (the single alert history source).
@MainActor
final class AlertStoreObserver: ObservableObject {
    @Published private(set) var alerts: [DyrectoAlert] = []
    // interop: Kotlin object → .shared; StateFlow observed through FlowWatcher (iosMain bridge).
    private let watcher = FlowWatcher<AnyObject>(flow: AlertStore.shared.alerts)

    init() {
        watcher.watch { [weak self] value in
            self?.alerts = (value as? [DyrectoAlert]) ?? []
        }
    }

    func clear() { AlertStore.shared.clear() }

    deinit { watcher.close() }
}

/// Observes the process-scoped `ExposureConfig` singleton (zebra spec + analysis color space) —
/// the same StateFlows Android's Settings/Developer screens collect directly.
@MainActor
final class ExposureConfigObserver: ObservableObject {
    @Published private(set) var zebraSpec: ZebraSpec
    @Published private(set) var analysisColorSpace: AnalysisColorSpace

    private let zebraWatcher = FlowWatcher<AnyObject>(flow: ExposureConfig.shared.zebraSpec)
    private let spaceWatcher = FlowWatcher<AnyObject>(flow: ExposureConfig.shared.analysisColorSpace)

    init() {
        // interop: StateFlow.value exports as `value: Any?` — seed from current values.
        zebraSpec = (ExposureConfig.shared.zebraSpec.value as? ZebraSpec) ?? ZebraSpecCompanion.shared.PRESET_95
        analysisColorSpace = (ExposureConfig.shared.analysisColorSpace.value as? AnalysisColorSpace) ?? .sLog3
        zebraWatcher.watch { [weak self] value in
            if let spec = value as? ZebraSpec { self?.zebraSpec = spec }
        }
        spaceWatcher.watch { [weak self] value in
            if let space = value as? AnalysisColorSpace { self?.analysisColorSpace = space }
        }
    }

    func setZebraLevel(_ ire: Int) { ExposureConfig.shared.setZebraLevel(ire: Int32(ire)) }
    func setAnalysisColorSpace(_ space: AnalysisColorSpace) {
        ExposureConfig.shared.setAnalysisColorSpace(space: space)
    }

    /// The zebra presets (Android `ExposureConfig.PRESET_LEVELS` = [70, 95, 100]).
    var presetLevels: [Int] {
        // interop: Kotlin List<Int> bridges as [KotlinInt] (NSNumber).
        ExposureConfig.shared.PRESET_LEVELS.map { Int(truncating: $0) }
    }

    deinit {
        zebraWatcher.close()
        spaceWatcher.close()
    }
}

/// Mirrors `IosAlertFeedback.playing` so the Alert Control Center's Test button can disable
/// itself while a pattern is playing (Android `testPlaying`).
@MainActor
final class FeedbackPlayingObserver: ObservableObject {
    @Published private(set) var playing = false
    private let watcher: FlowWatcher<AnyObject>

    init(feedback: IosAlertFeedback) {
        watcher = FlowWatcher<AnyObject>(flow: feedback.playing)
        watcher.watch { [weak self] value in
            self?.playing = (value as? Bool) ?? ((value as? KotlinBoolean)?.boolValue ?? false)
        }
    }

    deinit { watcher.close() }
}

// MARK: - DI container

/// The app-scoped service graph, mirroring `DyrectoApp.kt`'s ownership: stores → session →
/// notification permission. Built once at launch and injected into the view tree.
@MainActor
final class AppServices: ObservableObject {
    // Orchestration layer (parallel work — referenced by expected name).
    let session: MonitoringSessionIos

    // Connection (owned by the session; also exposed for the Discovery screen's BLE list).
    let connection: CameraConnectionController

    // Stores (UserDefaults-backed counterparts of the Android DataStores).
    let alertConfigStore: AlertConfigStoreIos
    let voiceStore: VoiceSettingsStoreIos

    // Delivery seams.
    let alertFeedback: IosAlertFeedback
    let notifications: IosNotificationDelivery
    let speech: IosSpeechEngine

    // UI adapters over shared Kotlin flows.
    let alertHistory: AlertStoreObserver
    let exposureConfig: ExposureConfigObserver
    let feedbackPlaying: FeedbackPlayingObserver

    let log = DyrectoLog.shared

    init(session: MonitoringSessionIos,
         connection: CameraConnectionController,
         alertConfigStore: AlertConfigStoreIos,
         voiceStore: VoiceSettingsStoreIos,
         alertFeedback: IosAlertFeedback,
         notifications: IosNotificationDelivery,
         speech: IosSpeechEngine) {
        self.session = session
        self.connection = connection
        self.alertConfigStore = alertConfigStore
        self.voiceStore = voiceStore
        self.alertFeedback = alertFeedback
        self.notifications = notifications
        self.speech = speech
        self.alertHistory = AlertStoreObserver()
        self.exposureConfig = ExposureConfigObserver()
        self.feedbackPlaying = FeedbackPlayingObserver(feedback: alertFeedback)
    }
}

/// Default wiring — the launch-order mirror of `DyrectoApp.kt` (stores first, then the session
/// which owns the connection + pipelines, then the cosmetic notification permission is requested
/// by the App). Class names reference the parallel orchestration layer by its expected names.
enum DefaultAppWiring {
    @MainActor
    static func build() -> AppServices {
        let alertConfigStore = AlertConfigStoreIos()
        let voiceStore = VoiceSettingsStoreIos()
        let alertFeedback = IosAlertFeedback()
        let notifications = IosNotificationDelivery()
        let speech = IosSpeechEngine()
        let connection = CameraConnectionController()
        // interop: MonitoringSessionIos (Session/) is expected to take its collaborators here —
        // it owns CameraRepository-equivalent state, the Vision pipeline, ReferenceMonitorIos,
        // the FrameRenderer, the alert engine wiring, and the voice scheduler tap.
        let session = MonitoringSessionIos(
            connection: connection,
            alertConfigStore: alertConfigStore,
            voiceSettingsStore: voiceStore,
            alertFeedback: alertFeedback,
            notificationDelivery: notifications,
            speechEngine: speech)
        return AppServices(
            session: session,
            connection: connection,
            alertConfigStore: alertConfigStore,
            voiceStore: voiceStore,
            alertFeedback: alertFeedback,
            notifications: notifications,
            speech: speech)
    }
}

// MARK: - Shared presentation helpers (Android ui/util/Format.kt + session/display)

/// "HH:mm:ss" or "—" (Android `formatTimeShort`).
func formatTimeShort(_ ts: KotlinLong?) -> String {
    guard let ts else { return "—" }
    return formatTimeShort(ts.int64Value)
}

func formatTimeShort(_ ts: Int64?) -> String {
    guard let ts else { return "—" }
    let formatter = DateFormatter()
    formatter.dateFormat = "HH:mm:ss"
    return formatter.string(from: Date(timeIntervalSince1970: Double(ts) / 1000.0))
}

/// Decoded-first telemetry display (Android `CameraTelemetry?.display(code)` in
/// FrameContextPublisher.kt): decoded value when a confirmed rule applies, else raw, else "—".
func telemetryDisplay(_ telemetry: CameraTelemetry?, _ code: Int32) -> String {
    guard let prop = telemetry?.get(code: code) else { return "—" }
    if let decoded = prop.decoded, !decoded.isEmpty { return decoded }
    if let raw = prop.rawValue, !raw.isEmpty { return raw }
    return "—"
}

/// Dashboard property codes (mirror of `CameraTelemetry.companion` constants — kept as literals
/// because Kotlin `const val`s on companions export awkwardly; values verified against
/// CameraTelemetry.kt).
enum TelemetryCode {
    static let iso: Int32 = 0xD21E
    static let fNumber: Int32 = 0x5007
    static let shutter: Int32 = 0xD20D
    static let whiteBalance: Int32 = 0x5005
    static let movieRec: Int32 = 0xD21D
    static let recTime: Int32 = 0xD261
    static let battery: Int32 = 0xD218
    static let overheating: Int32 = 0xD251
    static let slot1Remain: Int32 = 0xD24A
    static let slot1Status: Int32 = 0xD248
    static let slot2Status: Int32 = 0xD256
    static let slot2Remain: Int32 = 0xD258
    static let recResolution: Int32 = 0xD024
    static let recFps: Int32 = 0xD286
    static let focusMode: Int32 = 0x500A
    static let colorTemp: Int32 = 0xD20F
    static let liveView: Int32 = 0xD221
}

// MARK: - Connection-state conveniences (mirror of the Android extension vals)

extension CameraConnectionState {
    /// Android `isFullyConnected` — steady-state reached (bootstrap complete).
    var isFullyConnectedIos: Bool { phase == .deviceInfo }
    /// Android `running` — a connection attempt is in flight.
    var runningIos: Bool { phase != .idle && phase != .error && phase != .deviceInfo }
}

// MARK: - Stable enum catalogs
//
// Kotlin enum `entries` doesn't export; these fixed lists mirror the shared enums (a new entry
// needs a one-line addition here, same trade-off the SwiftEnums bridge documents).

// interop: Kotlin enum entries export as lowercase static members.
let allAlertSeverities: [AlertSeverity] = [.info, .warning, .critical]
let allAlertCategories: [AlertCategory] = [
    .recording, .battery, .media, .thermal, .connection, .exposure, .face, .reference,
]
let allVoiceSpeechRates: [VoiceSpeechRate] = [.slow, .normal, .fast]
let allAnalysisColorSpaces: [AnalysisColorSpace] = [.auto, .rec709, .sLog3]
let allReferenceSignals: [ReferenceSignal] = [
    .exposure, .subjectPosition, .subjectSize, .whiteBalance, .headroom,
    .facePresence, .eyeVisibility, .subjectPresence, .composition, .visualSimilarity,
]

/// The user-configurable assistant reminder bounds (VoiceCooldowns constants: default 3, 1–15 s).
enum VoiceReminderBounds {
    static let min = 1
    static let max = 15
    static let defaultSeconds = 3
}

/// AlertPattern count bounds (Android `AlertPattern.MIN_COUNT`/`MAX_COUNT` — 0–10 beeps/pulses).
enum AlertPatternBounds {
    static let min = 0
    static let max = 10
}
