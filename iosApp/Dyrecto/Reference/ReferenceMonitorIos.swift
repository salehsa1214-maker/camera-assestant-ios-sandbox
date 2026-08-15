import Foundation
import ImageIO
import UIKit
import DyrectoShared

/// Session-owned coordinator of the Shot Reference / Storyboard assistant — the iOS port of
/// `ReferenceMonitorManager`. Owned by `MonitoringSessionIos` and confined to its own serial
/// [queue] (the Swift stand-in for the Android session-scope collector coroutine), so per-frame
/// state (perception engines, state machines, completion runtimes) needs no further locking.
///
/// Responsibilities (Android parity):
///  - hold the `ReferenceSession` (persisted as JSON via `ReferenceStorageIos`; restored on
///    startup — monitoring itself never auto-starts);
///  - decode + downscale user-picked images, analyze each via `ReferenceAnalyzerIos` (exposure /
///    face modules + the Phase 10 AI perception pass), persist profiles + embeddings;
///  - Phase 15: compare each live frame against EVERY storyboard shot, pick the highest-scoring
///    one as the Current Match, and run ONLY that winner through the existing
///    perception → instruction → state-machine chain. Track per-shot completion
///    (winner-only hold accrual) against the storyboard's completion rule.
///
/// The comparison consumes only already-published Vision outputs — it never rescans the frame.
final class ReferenceMonitorIos: ObservableObject {

    /// Longest analyzed edge — comparable to live-view frame sizes, cheap to scan (Android
    /// MAX_ANALYSIS_DIMENSION parity).
    static let maxAnalysisDimension = 1280

    @Published private(set) var state: ReferenceSessionState = SharedFactory.emptySessionState()
    @Published private(set) var match: ReferenceMatchResult = SharedFactory.emptyMatchResult()
    @Published private(set) var perceptualTuning: PerceptualTuning?

    /// Per-tick match tap (queue thread) — MonitoringSessionIos wires voice + alerts off this.
    var onMatch: ((ReferenceMatchResult) -> Void)?
    /// Emits a shot id each time it flips to Completed (for the "Shot completed." voice cue).
    var onShotCompleted: ((String) -> Void)?

    private let queue = DispatchQueue(label: "app.dyrecto.reference", qos: .utility)
    private let analysisQueue = DispatchQueue(label: "app.dyrecto.reference.analysis", qos: .userInitiated)
    private let storage = ReferenceStorageIos()
    private let analyzer: ReferenceAnalyzerIos
    private let perception: AiPerceptionCoordinator?
    private let sceneContextProvider: () -> SceneContext
    private let log = DyrectoLog.shared

    // ---- Queue-confined state (the Android manager's fields, 1:1) ----
    private var currentState: ReferenceSessionState = SharedFactory.emptySessionState()
    private var currentMatch: ReferenceMatchResult = SharedFactory.emptyMatchResult()
    private var currentTuning: PerceptualTuning?

    /// Phase 15: per-shot comparison state, keyed by profile id. Each shot keeps its own
    /// perceptual hysteresis + debounce so switching Current Match never corrupts another shot's
    /// history; paused shots hold their state until they win again.
    private var embeddings: [String: ReferenceEmbedding] = [:]
    private var perceptionEngines: [String: HumanPerceptionEngine] = [:]
    private var stateMachines: [String: ReferenceStateMachine] = [:]
    private var completionRuntime: [String: StoryboardCompletionRuntime] = [:]

    /// The Current Match shot id last synced to live perception (repoint only when it changes).
    private var lastWinnerId: String?

    init(perception: AiPerceptionCoordinator?,
         semanticEngine: SemanticSceneEngine?,
         sceneContextProvider: @escaping () -> SceneContext) {
        self.perception = perception
        self.analyzer = ReferenceAnalyzerIos(perception: perception, semanticEngine: semanticEngine)
        self.sceneContextProvider = sceneContextProvider

        // Restore the persisted session (analysis + completion included — no image re-scan).
        // Monitoring is NOT auto-started: the user re-activates it from the Storyboard page.
        queue.async {
            guard let restored = self.storage.loadSession() else { return }
            self.mutateState { st in
                ReferenceSessionState(session: restored,
                                      monitoringRequested: st.monitoringRequested,
                                      analyzing: st.analyzing, error: st.error)
            }
            for profile in restored.profiles {
                if let embId = profile.ai?.embeddingId,
                   let emb = self.storage.loadEmbedding(id: embId) {
                    self.embeddings[profile.id] = emb
                }
            }
            self.log.line(.info, "Restored storyboard \(restored.id) (shots=\(restored.profiles.count))")
        }
    }

    /// Thread-safe snapshot for the session's alert-edge bookkeeping.
    func stateSnapshot() -> ReferenceSessionState {
        queue.sync { currentState }
    }

    func setPerceptualTuning(_ tuning: PerceptualTuning?) {
        queue.async {
            self.currentTuning = tuning
            DispatchQueue.main.async { self.perceptualTuning = tuning }
        }
    }

    /// Embedding of the active (first) shot, for developer diagnostics.
    func referenceEmbeddingOrNull() -> ReferenceEmbedding? {
        queue.sync {
            currentState.session?.activeProfile.flatMap { embeddings[$0.id] }
        }
    }

    // MARK: Comparison loop (one evaluation per published VisionContext)

    /// Called by the session for every merged VisionContext (i.e. per analyzed frame).
    func onFrame(_ vc: VisionContext) {
        queue.async { self.onFrameLocked(vc) }
    }

    /// Phase 15 core: compare the frame against every storyboard shot, select the Current Match
    /// (highest overall score), and drive ONLY that shot through the existing chain. Downstream
    /// (alerts, voice, UI) still sees a single reference.
    private func onFrameLocked(_ vc: VisionContext) {
        let st = currentState
        guard st.monitoringRequested,
              let session = st.session,
              !session.profiles.isEmpty,
              vc.updatedAtMs != 0 else { return }
        let nowMs = SharedFactory.nowMs()

        // 1) Compare against every shot (pure, cheap — reads published Vision outputs only).
        var winner: ReferenceProfile?
        var winnerRaw: ReferenceMatchResult?
        var winnerInput: CurrentReferenceInput?
        var winnerScore = -Float.infinity
        for profile in session.profiles {
            let input = buildInput(vc, embedding: embeddings[profile.id])
            let raw = SceneComparator.shared.compare(reference: profile, current: input, nowMs: nowMs)
            if raw.overallScore > winnerScore {
                winnerScore = raw.overallScore
                winner = profile
                winnerRaw = raw
                winnerInput = input
            }
        }
        guard let win = winner, let rawWin = winnerRaw, let input = winnerInput else { return }

        // 2) Point live perception + the exposure subject region at the Current Match. Retarget
        //    tracking only when the winner id changes (avoids per-frame template thrash).
        let tuning = currentTuning
            ?? PerceptualTuning.companion.forTolerance(tolerance: win.options.tolerance)
        updateSubjectRegion(profile: win, vc: vc, tuning: tuning, nowMs: nowMs)
        if win.id != lastWinnerId {
            lastWinnerId = win.id
            syncPerception(target: win)
        }

        // 3) Winner-only through the EXISTING chain (single reference for everyone downstream).
        let engine = perceptionEngines[win.id] ?? {
            // interop: default evaluator table via the exported top-level factory
            // (Kotlin default args vanish on the constructor).
            let e = HumanPerceptionEngine(
                evaluators: PerceptualEvaluatorKt.defaultPerceptualEvaluators())
            perceptionEngines[win.id] = e
            return e
        }()
        let stateMachine = stateMachines[win.id] ?? {
            let m = ReferenceStateMachine()
            stateMachines[win.id] = m
            return m
        }()

        let liveSubjectExposure: SubjectExposureStats? =
            AnalysisRegionRegistry.shared.subjectRegion != nil ? vc.subjectExposure?.stats : nil
        let perceived = engine.evaluate(
            raw: rawWin,
            reference: win,
            input: input,
            liveSubjectExposure: liveSubjectExposure,
            tuning: tuning)
        let instructed = InstructionTranslator.shared.annotate(match: perceived)
        let result = stateMachine.update(
            raw: instructed,
            thresholds: ReferenceConfig.shared.thresholdsFor(tolerance: win.options.tolerance))
        publishMatch(result)

        // 4) Completion tracking — only the Current Match (winner) accrues hold time.
        trackCompletion(
            session: session,
            winnerId: win.id,
            aboveThreshold: result.overallScore >= session.completionRule.matchThreshold,
            nowMs: nowMs)
    }

    private func buildInput(_ vc: VisionContext, embedding: ReferenceEmbedding?) -> CurrentReferenceInput {
        CurrentReferenceInput(
            exposure: vc.exposure,
            histogram: vc.histogram,
            zebra: vc.zebra,
            faces: vc.faces,
            eyes: vc.eyes,
            colorStats: vc.colorStats,
            scene: sceneContextProvider(),
            sceneSnapshot: vc.aiScene?.snapshot,
            referenceEmbedding: embedding)
    }

    /// Phase 15: advances every shot's completion. Only [winnerId] (when [aboveThreshold])
    /// accrues a continuous hold; all other shots reset their in-flight streak (confirmation
    /// count preserved). Persists the session and emits [onShotCompleted] on completion flips.
    private func trackCompletion(session: ReferenceSession, winnerId: String,
                                 aboveThreshold: Bool, nowMs: Int64) {
        let rule = session.completionRule
        var transitions: [String: ShotCompletion] = [:]
        var completedNow: [String] = []
        for profile in session.profiles {
            let above = profile.id == winnerId && aboveThreshold
            let runtime = completionRuntime[profile.id]
                ?? StoryboardCompletionRuntime(holdStartMs: nil, streakConfirmed: false)
            let update = StoryboardCompletion.shared.update(
                completion: profile.completion, runtime: runtime,
                aboveThreshold: above, nowMs: nowMs, rule: rule)
            completionRuntime[profile.id] = update.runtime
            if update.completion != profile.completion {
                transitions[profile.id] = update.completion
                if update.justCompleted { completedNow.append(profile.id) }
            }
        }
        if transitions.isEmpty { return }

        guard let current = currentState.session else { return }
        let newProfiles = current.profiles.map { profile -> ReferenceProfile in
            guard let completion = transitions[profile.id] else { return profile }
            return profile.doCopy(
                id: profile.id, name: profile.name, createdAtMs: profile.createdAtMs,
                imageUri: profile.imageUri, width: profile.width, height: profile.height,
                exposure: profile.exposure, color: profile.color, subject: profile.subject,
                face: profile.face, options: profile.options, ai: profile.ai,
                creativeScene: profile.creativeScene, completion: completion)
        }
        let updated = current.doCopy(
            id: current.id, name: current.name, createdAtMs: current.createdAtMs,
            profiles: newProfiles, activeProfileId: current.activeProfileId,
            completionRule: current.completionRule, schemaVersion: current.schemaVersion)
        mutateState { st in
            ReferenceSessionState(session: updated, monitoringRequested: st.monitoringRequested,
                                  analyzing: st.analyzing, error: st.error)
        }
        storage.saveSession(updated)
        for id in completedNow {
            log.line(.info, "Storyboard shot completed: \(id)")
            onShotCompleted?(id)
        }
    }

    // MARK: Storyboard editing

    /// Decodes the picked image (downscaled, aspect preserved), analyzes it, copies it into
    /// app-private storage, and APPENDS it as a new storyboard shot. Monitor options are
    /// inherited storyboard-wide. Runs on a background queue; progress/errors surface via [state].
    func addReferenceImage(url: URL, name: String) {
        beginAnalysis()
        analysisQueue.async {
            guard let image = Self.decodeDownscaled(url: url, maxDimension: Self.maxAnalysisDimension) else {
                self.onAnalysisError("Could not decode the selected image.")
                return
            }
            self.appendAnalyzed(image: image, name: name)
        }
    }

    /// Variant for pickers that hand over a UIImage directly (PHPicker item provider).
    func addReferenceImage(image: UIImage, name: String) {
        beginAnalysis()
        analysisQueue.async {
            let scaled = Self.downscale(image, maxDimension: Self.maxAnalysisDimension)
            self.appendAnalyzed(image: scaled, name: name)
        }
    }

    private func appendAnalyzed(image: UIImage, name: String) {
        do {
            let analysis = try analyzeImage(image, name: name)
            queue.async {
                let current = self.currentState.session
                let base = current ?? ReferenceSession(
                    id: UUID().uuidString, name: nil, createdAtMs: SharedFactory.nowMs(),
                    profiles: [], activeProfileId: nil,
                    completionRule: SharedFactory.defaultCompletionRule(), schemaVersion: 1)
                let session = base.addProfile(profile: analysis.profile)
                if let embedding = analysis.embedding {
                    self.embeddings[analysis.profile.id] = embedding
                    self.storage.saveEmbedding(embedding)
                }
                self.storage.saveSession(session)
                self.mutateState { st in
                    ReferenceSessionState(session: session,
                                          monitoringRequested: st.monitoringRequested,
                                          analyzing: false, error: nil)
                }
                // A newly added shot cannot be the Current Match yet; nothing to reset downstream.
                self.logAnalyzed(verb: "added", profile: analysis.profile)
            }
        } catch {
            onAnalysisError(error.localizedDescription)
        }
    }

    /// Replaces shot [id] in place (position preserved) with a freshly analyzed image. The old
    /// embedding/image are dropped; the replacement is a new shot with its own reset completion.
    func replaceReference(id: String, url: URL, name: String) {
        beginAnalysis()
        analysisQueue.async {
            guard let image = Self.decodeDownscaled(url: url, maxDimension: Self.maxAnalysisDimension) else {
                self.onAnalysisError("Could not decode the selected image.")
                return
            }
            self.performReplace(id: id, image: image, name: name)
        }
    }

    /// Variant for pickers that hand over a UIImage directly (PHPicker item provider).
    func replaceReference(id: String, image: UIImage, name: String) {
        beginAnalysis()
        analysisQueue.async {
            let scaled = Self.downscale(image, maxDimension: Self.maxAnalysisDimension)
            self.performReplace(id: id, image: scaled, name: name)
        }
    }

    /// Analysis-queue body shared by both replace variants (position-preserving, Android parity).
    private func performReplace(id: String, image: UIImage, name: String) {
            do {
                let analysis = try self.analyzeImage(image, name: name)
                self.queue.async {
                    let current = self.currentState.session
                    guard let current, current.profiles.contains(where: { $0.id == id }) else {
                        // The shot vanished mid-analysis — fall back to an append.
                        let base = current ?? ReferenceSession(
                            id: UUID().uuidString, name: nil, createdAtMs: SharedFactory.nowMs(),
                            profiles: [], activeProfileId: nil,
                            completionRule: SharedFactory.defaultCompletionRule(), schemaVersion: 1)
                        let session = base.addProfile(profile: analysis.profile)
                        if let embedding = analysis.embedding {
                            self.embeddings[analysis.profile.id] = embedding
                            self.storage.saveEmbedding(embedding)
                        }
                        self.storage.saveSession(session)
                        self.mutateState { st in
                            ReferenceSessionState(session: session,
                                                  monitoringRequested: st.monitoringRequested,
                                                  analyzing: false, error: nil)
                        }
                        return
                    }
                    self.deleteShotArtifacts(session: current, id: id)
                    let session = current.withReplacedProfile(oldId: id, profile: analysis.profile)
                    if let embedding = analysis.embedding {
                        self.embeddings[analysis.profile.id] = embedding
                        self.storage.saveEmbedding(embedding)
                    }
                    self.resetComparisonState(for: id)
                    self.storage.saveSession(session)
                    self.mutateState { st in
                        ReferenceSessionState(session: session,
                                              monitoringRequested: st.monitoringRequested,
                                              analyzing: false, error: nil)
                    }
                    self.logAnalyzed(verb: "replaced", profile: analysis.profile)
                }
            } catch {
                self.onAnalysisError(error.localizedDescription)
            }
    }

    /// Removes shot [id] from the storyboard (its image + embedding files are deleted).
    func removeReference(id: String) {
        queue.async {
            guard let session = self.currentState.session,
                  session.profiles.contains(where: { $0.id == id }) else { return }
            let updated = session.removeProfile(id: id)
            self.embeddings.removeValue(forKey: id)
            self.resetComparisonState(for: id)
            let emptied = updated.profiles.isEmpty
            self.mutateState { st in
                ReferenceSessionState(session: emptied ? nil : updated,
                                      monitoringRequested: st.monitoringRequested,
                                      analyzing: st.analyzing, error: st.error)
            }
            if emptied {
                // Last shot removed: fully stand down monitoring perception like clearReference.
                AnalysisRegionRegistry.shared.clear()
                self.publishMatch(SharedFactory.emptyMatchResult())
                self.syncPerception(target: nil)
            }
            self.deleteShotArtifacts(session: session, id: id)
            if emptied { self.storage.clear() } else { self.storage.saveSession(updated) }
        }
    }

    /// Updates the monitor options for the WHOLE storyboard (settings are not per-shot).
    func setOptions(_ options: ReferenceMonitorOptions) {
        queue.async {
            guard let session = self.currentState.session else { return }
            let updated = session.withOptionsForAll(options: options)
            self.mutateState { st in
                ReferenceSessionState(session: updated, monitoringRequested: st.monitoringRequested,
                                      analyzing: st.analyzing, error: st.error)
            }
            self.resetComparisonState()
            self.storage.saveSession(updated)
        }
    }

    /// Phase 15: updates the storyboard-wide completion rule (hold / confirm / threshold).
    func setCompletionRule(_ rule: StoryboardCompletionRule) {
        queue.async {
            guard let session = self.currentState.session else { return }
            let updated = session.withCompletionRule(rule: rule)
            self.mutateState { st in
                ReferenceSessionState(session: updated, monitoringRequested: st.monitoringRequested,
                                      analyzing: st.analyzing, error: st.error)
            }
            // Rule change re-bases in-flight streaks (persisted completion is untouched).
            self.completionRuntime.removeAll()
            self.storage.saveSession(updated)
        }
    }

    /// Phase 15: explicit "reset progress" — clears every shot's completion back to Pending.
    func resetStoryboardProgress() {
        queue.async {
            guard let session = self.currentState.session else { return }
            let updated = session.withProgressReset()
            self.completionRuntime.removeAll()
            self.mutateState { st in
                ReferenceSessionState(session: updated, monitoringRequested: st.monitoringRequested,
                                      analyzing: st.analyzing, error: st.error)
            }
            self.storage.saveSession(updated)
        }
    }

    /// Starts comparing live frames against the storyboard (no-op without any shot).
    func startMonitoring() {
        queue.async {
            guard let profiles = self.currentState.session?.profiles, !profiles.isEmpty else { return }
            self.resetComparisonState()
            self.mutateState { st in
                ReferenceSessionState(session: st.session, monitoringRequested: true,
                                      analyzing: st.analyzing, error: st.error)
            }
            self.syncPerception(target: self.currentState.profile)
        }
    }

    /// Stops monitoring but keeps the storyboard (shots + options + completion).
    func stopMonitoring() {
        queue.async {
            self.mutateState { st in
                ReferenceSessionState(session: st.session, monitoringRequested: false,
                                      analyzing: st.analyzing, error: st.error)
            }
            self.resetComparisonState()
            AnalysisRegionRegistry.shared.clear()
            // active=false → alert latches clear silently.
            self.publishMatch(SharedFactory.emptyMatchResult())
            self.syncPerception(target: nil)
        }
    }

    /// Removes the storyboard entirely (session, embeddings, stored images), stops monitoring.
    func clearReference() {
        queue.async {
            self.mutateState { _ in SharedFactory.emptySessionState() }
            self.embeddings.removeAll()
            self.resetComparisonState()
            AnalysisRegionRegistry.shared.clear()
            self.publishMatch(SharedFactory.emptyMatchResult())
            self.syncPerception(target: nil)
            self.storage.clear()
        }
    }

    // MARK: Analysis helpers

    private func analyzeImage(_ image: UIImage, name: String) throws -> ReferenceAnalysisIos {
        let id = UUID().uuidString
        let imageUri = storage.persistImage(image, id: id)
        let options = queue.sync {
            currentState.session?.activeProfile?.options ?? SharedFactory.defaultMonitorOptions()
        }
        return try analyzer.analyze(
            image: image,
            id: id,
            name: name.isEmpty ? "Shot" : name,
            imageUri: imageUri,
            options: options,
            nowMs: SharedFactory.nowMs())
    }

    private func beginAnalysis() {
        queue.async {
            self.mutateState { st in
                ReferenceSessionState(session: st.session, monitoringRequested: st.monitoringRequested,
                                      analyzing: true, error: nil)
            }
        }
    }

    private func onAnalysisError(_ message: String) {
        log.line(.error, "Reference analysis failed: \(message)")
        queue.async {
            self.mutateState { st in
                ReferenceSessionState(session: st.session, monitoringRequested: st.monitoringRequested,
                                      analyzing: false,
                                      error: message.isEmpty ? "Image analysis failed." : message)
            }
        }
    }

    private func logAnalyzed(verb: String, profile: ReferenceProfile) {
        let ai = profile.ai
        log.line(.info, "Shot \(verb): \(profile.id) \(profile.width)x\(profile.height) "
            + "scene=\(ai?.sceneMode.name ?? "-") subject=\(ai?.primarySubjectCategory.name ?? "-") "
            + "strategy=\(ai?.strategy.name ?? "-")")
    }

    /// Deletes the on-disk image + embedding files for shot [id].
    private func deleteShotArtifacts(session: ReferenceSession, id: String) {
        storage.deleteImage(id: id)
        if let embId = session.profiles.first(where: { $0.id == id })?.ai?.embeddingId {
            storage.deleteEmbedding(id: embId)
        }
    }

    private func resetComparisonState() {
        perceptionEngines.removeAll()
        stateMachines.removeAll()
        completionRuntime.removeAll()
        lastWinnerId = nil
    }

    private func resetComparisonState(for id: String) {
        perceptionEngines.removeValue(forKey: id)
        stateMachines.removeValue(forKey: id)
        completionRuntime.removeValue(forKey: id)
        if lastWinnerId == id { lastWinnerId = nil }
    }

    // MARK: Live perception targeting (Phase 12/15)

    /// Registers the current live subject box (AI-matched subject, else largest face) with the
    /// shared `AnalysisRegionRegistry` so the exposure scan produces subject-region stats, and
    /// keeps the region clip thresholds in sync with the active perceptual tuning.
    private func updateSubjectRegion(profile: ReferenceProfile, vc: VisionContext,
                                     tuning: PerceptualTuning, nowMs: Int64) {
        let registry = AnalysisRegionRegistry.shared
        registry.highlightLumaMin = tuning.profile.subjectHighlightLumaMin
        registry.shadowLumaMax = tuning.profile.subjectShadowLumaMax

        let ai = profile.ai
        var snapshot: SceneSnapshot?
        if let s = vc.aiScene?.snapshot,
           nowMs - s.analyzedAtMs <= ReferenceConfig.shared.AI_STALE_AFTER_MS {
            snapshot = s
        }
        var expected: SubjectMatcherExpected?
        if let ai, let box = ai.primarySubjectBox {
            expected = SubjectMatcherExpected(
                category: ai.primarySubjectCategory,
                boundingBox: box,
                rawLabel: ai.primarySubjectRawLabel,
                trackId: SceneSubject.companion.NO_TRACK)
        }
        let box: NormalizedRect?
        if let expected, let snapshot {
            box = SubjectMatcher.shared.match(
                expected: expected,
                candidates: snapshot.subjects,
                weights: SubjectMatcher.shared.DEFAULT_WEIGHTS,
                appearanceScore: nil)?.subject.boundingBox
        } else {
            box = SceneComparator.shared.largestFaceNormalized(faces: vc.faces)
        }
        registry.subjectRegion = box.map {
            AnalysisRegion(left: $0.left, top: $0.top, right: $0.right, bottom: $0.bottom)
        }
    }

    /// Points live perception at [target]'s primary subject while monitoring is on. Phase 15:
    /// the target is the Current Match shot (re-pointed on winner change).
    private func syncPerception(target: ReferenceProfile?) {
        guard let coordinator = perception else { return }
        let monitoring = currentState.monitoringRequested
        var expected: SubjectMatcherExpected?
        if let ai = target?.ai, let box = ai.primarySubjectBox {
            switch ai.primarySubjectType {
            case .none, .unknown, .multiple:
                expected = nil
            default:
                expected = SubjectMatcherExpected(
                    category: ai.primarySubjectCategory,
                    boundingBox: box,
                    rawLabel: ai.primarySubjectRawLabel,
                    trackId: SceneSubject.companion.NO_TRACK)
            }
        }
        coordinator.setLiveMonitoring(expected: expected, active: monitoring && target != nil)
    }

    // MARK: Publication (queue → main)

    private func mutateState(_ transform: (ReferenceSessionState) -> ReferenceSessionState) {
        let next = transform(currentState)
        currentState = next
        DispatchQueue.main.async { self.state = next }
    }

    private func publishMatch(_ result: ReferenceMatchResult) {
        currentMatch = result
        DispatchQueue.main.async { self.match = result }
        onMatch?(result)
    }

    // MARK: Image decoding

    /// Bounded decode of a picked image: CGImageSource thumbnailing caps the longest edge at
    /// [maxDimension] so huge library images never load unbounded — the iOS counterpart of the
    /// Android two-pass inSampleSize decode.
    static func decodeDownscaled(url: URL, maxDimension: Int) -> UIImage? {
        guard let source = CGImageSourceCreateWithURL(url as CFURL, nil) else { return nil }
        let options: [CFString: Any] = [
            kCGImageSourceCreateThumbnailFromImageAlways: true,
            kCGImageSourceCreateThumbnailWithTransform: true, // bake in EXIF orientation
            kCGImageSourceThumbnailMaxPixelSize: maxDimension,
        ]
        guard let cg = CGImageSourceCreateThumbnailAtIndex(source, 0, options as CFDictionary) else {
            return nil
        }
        return UIImage(cgImage: cg)
    }

    /// In-memory downscale (aspect preserved) for picker-provided UIImages.
    static func downscale(_ image: UIImage, maxDimension: Int) -> UIImage {
        guard let cg = image.cgImage else { return image }
        let longest = max(cg.width, cg.height)
        guard longest > maxDimension else { return image }
        return AiSceneModule.defaultScaledCopy(image, maxEdge: maxDimension) ?? image
    }
}
