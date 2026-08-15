package app.dyrecto.liveview.reference

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import app.dyrecto.liveview.exposure.AnalysisRegion
import app.dyrecto.liveview.exposure.AnalysisRegionRegistry
import app.dyrecto.liveview.instructions.InstructionTranslator
import app.dyrecto.liveview.perception.HumanPerceptionEngine
import app.dyrecto.liveview.perception.PerceptualTuning
import app.dyrecto.liveview.reference.ai.AiPerceptionCoordinator
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SubjectMatcher
import app.dyrecto.liveview.scene.SceneContext
import app.dyrecto.liveview.vision.results.VisionContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Session-owned coordinator of the Shot Reference / Storyboard assistant. Owned by
 * `DefaultMonitoringSession` — exactly like Vision/Scene managers — and runs on the app-scoped
 * session scope, so it survives backgrounding and Activity recreation.
 *
 * Responsibilities:
 *  - hold the [ReferenceSession] (persisted as JSON via [ReferenceRepository]; restored on
 *    startup, monitoring itself never auto-starts);
 *  - decode + downscale user-picked images, analyze each via [ReferenceAnalyzer] (exposure/face
 *    modules + the Phase 10 AI perception pass), persist profiles + embeddings;
 *  - **Phase 15**: compare each live frame against EVERY storyboard shot, pick the highest-scoring
 *    one as the Current Match, and run ONLY that shot through the existing perception → instruction
 *    → state-machine chain — so everything downstream still sees exactly one reference. Track each
 *    shot's completion (winner-only hold accrual) against the storyboard's [StoryboardCompletionRule].
 *
 * The comparison consumes only already-published Vision outputs — it never rescans the live
 * Bitmap. When the connection drops, [VisionContext] simply stops updating, so monitoring pauses
 * implicitly and resumes with the frame stream if the user left it active.
 */
class ReferenceMonitorManager(
    context: Context,
    private val visionContext: StateFlow<VisionContext>,
    private val sceneContext: StateFlow<SceneContext>,
    private val scope: CoroutineScope,
    private val perception: AiPerceptionCoordinator? = null,
    /** Phase 16.1: MobileCLIP semantic expert, used only at reference import (never per frame). */
    private val semanticEngine: app.dyrecto.liveview.reference.creative.semantic.SemanticSceneEngine? = null,
    /**
     * Captures the live camera's settings at reference-import time, stored on the shot for the
     * "this shot was ISO 800, 1/50, f/2.8…" record + settings-drift alerts. Returns null when no
     * camera is connected. Injected so the manager never depends on the camera repository directly.
     */
    private val cameraSettingsProvider: () -> ReferenceCameraSettings? = { null },
) {
    private val appContext = context.applicationContext
    private val repository = ReferenceRepository(appContext)
    private val analyzer = ReferenceAnalyzer(perception = perception, semanticEngine = semanticEngine)

    /**
     * Phase 15: per-shot comparison state, keyed by profile id. Each shot keeps its own perceptual
     * hysteresis + debounce so switching Current Match never corrupts another shot's history;
     * paused shots simply hold their state until they win again (alert latches already re-seed on
     * `referenceId` change). Embeddings are loaded per shot (model/dimension checked at compare).
     */
    private val embeddings = ConcurrentHashMap<String, ReferenceEmbedding>()
    private val perceptionEngines = ConcurrentHashMap<String, HumanPerceptionEngine>()
    private val stateMachines = ConcurrentHashMap<String, ReferenceStateMachine>()
    private val completionRuntime = ConcurrentHashMap<String, StoryboardCompletion.Runtime>()

    /** The Current Match shot id last synced to live perception (repoint only when it changes). */
    @Volatile private var lastWinnerId: String? = null

    /**
     * Runtime perceptual tuning override (Developer panel; in-memory only). Null = derive from
     * the active shot's tolerance defaults.
     */
    private val _perceptualTuning = MutableStateFlow<PerceptualTuning?>(null)
    val perceptualTuning: StateFlow<PerceptualTuning?> = _perceptualTuning.asStateFlow()

    fun setPerceptualTuning(tuning: PerceptualTuning?) {
        _perceptualTuning.value = tuning
    }

    private val _state = MutableStateFlow(ReferenceSessionState())
    val state: StateFlow<ReferenceSessionState> = _state.asStateFlow()

    private val _match = MutableStateFlow(ReferenceMatchResult())
    val match: StateFlow<ReferenceMatchResult> = _match.asStateFlow()

    /** Phase 15: emits a shot id each time it flips to Completed (for the optional voice cue). */
    private val _completedShots = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val completedShots: SharedFlow<String> = _completedShots.asSharedFlow()

    /** Embedding of the active (first) shot, for developer diagnostics. */
    val referenceEmbeddingOrNull: ReferenceEmbedding?
        get() = _state.value.session?.activeProfile?.id?.let { embeddings[it] }

    init {
        // Restore the persisted session (analysis + completion included — no image re-scan).
        // Monitoring is NOT auto-started: the user re-activates it from the Storyboard page.
        scope.launch {
            val restored = runCatching { repository.loadSession() }.getOrNull()
            if (restored != null) {
                _state.update { it.copy(session = restored) }
                restored.profiles.forEach { p ->
                    p.ai?.embeddingId?.let { embId ->
                        repository.loadEmbedding(embId)?.let { embeddings[p.id] = it }
                    }
                }
                Log.d(TAG, "Restored storyboard ${restored.id} (shots=${restored.profiles.size})")
            }
        }

        // Comparison loop: one evaluation per published VisionContext (i.e. per analyzed frame).
        scope.launch {
            visionContext.collect { vc -> onFrame(vc) }
        }
    }

    /**
     * Phase 15 core: compare the frame against every storyboard shot, select the Current Match
     * (highest overall score), and drive ONLY that shot through the existing perception → instruction
     * → state-machine chain. Downstream (alerts, voice, UI) still sees a single reference.
     */
    private fun onFrame(vc: VisionContext) {
        val st = _state.value
        val session = st.session
        if (!st.monitoringRequested || session == null || session.profiles.isEmpty() ||
            vc.updatedAtMs == 0L
        ) {
            return
        }
        val nowMs = System.currentTimeMillis()

        // 1) Compare against every shot (pure, cheap — reads published Vision outputs only).
        var winner: ReferenceProfile? = null
        var winnerRaw: ReferenceMatchResult? = null
        var winnerInput: CurrentReferenceInput? = null
        var winnerScore = Float.NEGATIVE_INFINITY
        for (p in session.profiles) {
            val input = buildInput(vc, embeddings[p.id])
            val raw = SceneComparator.compare(p, input, nowMs)
            if (raw.overallScore > winnerScore) {
                winnerScore = raw.overallScore
                winner = p
                winnerRaw = raw
                winnerInput = input
            }
        }
        val win = winner ?: return
        val rawWin = winnerRaw ?: return
        val input = winnerInput ?: return

        // 2) Point live perception + the exposure subject region at the Current Match. Retarget
        //    tracking only when the winner id changes (avoids per-frame template thrash).
        val tuning = _perceptualTuning.value ?: PerceptualTuning.forTolerance(win.options.tolerance)
        updateSubjectRegion(win, vc, tuning, nowMs)
        if (win.id != lastWinnerId) {
            lastWinnerId = win.id
            syncPerception(win)
        }

        // 3) Winner-only through the EXISTING chain (single reference for everyone downstream).
        val engine = perceptionEngines.getOrPut(win.id) { HumanPerceptionEngine() }
        val stateMachine = stateMachines.getOrPut(win.id) { ReferenceStateMachine() }
        val perceived = engine.evaluate(
            raw = rawWin,
            reference = win,
            input = input,
            liveSubjectExposure = vc.subjectExposure?.stats
                ?.takeIf { AnalysisRegionRegistry.subjectRegion != null },
            tuning = tuning,
        )
        val instructed = InstructionTranslator.annotate(perceived)
        val result = stateMachine.update(
            instructed,
            ReferenceConfig.thresholdsFor(win.options.tolerance),
        )
        _match.value = result

        // 4) Completion tracking — only the Current Match (winner) accrues hold time.
        trackCompletion(session, win.id, aboveThreshold = result.overallScore >= session.completionRule.matchThreshold, nowMs = nowMs)
    }

    private fun buildInput(vc: VisionContext, embedding: ReferenceEmbedding?): CurrentReferenceInput =
        CurrentReferenceInput(
            exposure = vc.exposure,
            histogram = vc.histogram,
            zebra = vc.zebra,
            faces = vc.faces,
            eyes = vc.eyes,
            colorStats = vc.colorStats,
            scene = sceneContext.value,
            sceneSnapshot = vc.aiScene?.snapshot,
            referenceEmbedding = embedding,
        )

    /**
     * Phase 15: advances every shot's completion. Only [winnerId] (when [aboveThreshold]) accrues a
     * continuous hold; all other shots reset their in-flight streak (their confirmation count is
     * preserved). Persists the session and emits [completedShots] when a shot flips to completed.
     * The runtime streak map is touched only from this collector coroutine.
     */
    private fun trackCompletion(session: ReferenceSession, winnerId: String, aboveThreshold: Boolean, nowMs: Long) {
        val rule = session.completionRule
        val transitions = HashMap<String, ShotCompletion>()
        val completedNow = ArrayList<String>()
        for (p in session.profiles) {
            val above = p.id == winnerId && aboveThreshold
            val runtime = completionRuntime[p.id] ?: StoryboardCompletion.Runtime()
            val u = StoryboardCompletion.update(p.completion, runtime, above, nowMs, rule)
            completionRuntime[p.id] = u.runtime
            if (u.completion != p.completion) {
                transitions[p.id] = u.completion
                if (u.justCompleted) completedNow += p.id
            }
        }
        if (transitions.isEmpty()) return

        var toSave: ReferenceSession? = null
        _state.update { st ->
            val s = st.session ?: return@update st
            val newProfiles = s.profiles.map { p ->
                transitions[p.id]?.let { p.copy(completion = it) } ?: p
            }
            val updated = s.copy(profiles = newProfiles)
            toSave = updated
            st.copy(session = updated)
        }
        toSave?.let { s -> scope.launch { repository.saveSession(s) } }
        completedNow.forEach { id ->
            Log.d(TAG, "Storyboard shot completed: $id")
            _completedShots.tryEmit(id)
        }
    }

    // ---- Storyboard editing ----

    /**
     * Decodes [uri] (downscaled, aspect preserved), analyzes it, copies it into app-private storage,
     * and APPENDS it as a new storyboard shot. Monitor options are inherited storyboard-wide from an
     * existing shot (or defaults). Runs on a background dispatcher; progress/errors surface via [state].
     */
    fun addReferenceImage(uri: Uri, name: String) {
        _state.update { it.copy(analyzing = true, error = null) }
        scope.launch(Dispatchers.Default) {
            try {
                val analysis = analyzeImage(uri, name)
                val current = _state.value.session
                val session = (current ?: ReferenceSession(
                    id = UUID.randomUUID().toString(),
                    createdAtMs = System.currentTimeMillis(),
                )).addProfile(analysis.profile)
                analysis.embedding?.let {
                    embeddings[analysis.profile.id] = it
                    repository.saveEmbedding(it)
                }
                repository.saveSession(session)
                _state.update { it.copy(session = session, analyzing = false, error = null) }
                // A newly added shot cannot be the Current Match yet; nothing to reset downstream.
                logAnalyzed("added", analysis.profile)
            } catch (t: Throwable) {
                onAnalysisError(t)
            }
        }
    }

    /**
     * Replaces shot [id] in place (position preserved) with a freshly analyzed [uri]. The old
     * embedding/image are dropped; the replacement is a new shot with its own (reset) completion.
     */
    fun replaceReference(id: String, uri: Uri, name: String) {
        _state.update { it.copy(analyzing = true, error = null) }
        scope.launch(Dispatchers.Default) {
            try {
                val analysis = analyzeImage(uri, name)
                val current = _state.value.session
                if (current == null || current.profiles.none { it.id == id }) {
                    // The shot vanished mid-analysis — fall back to an append.
                    val session = (current ?: ReferenceSession(
                        id = UUID.randomUUID().toString(),
                        createdAtMs = System.currentTimeMillis(),
                    )).addProfile(analysis.profile)
                    analysis.embedding?.let { embeddings[analysis.profile.id] = it; repository.saveEmbedding(it) }
                    repository.saveSession(session)
                    _state.update { it.copy(session = session, analyzing = false, error = null) }
                    return@launch
                }
                deleteShotArtifacts(current, id)
                val session = current.withReplacedProfile(id, analysis.profile)
                analysis.embedding?.let {
                    embeddings[analysis.profile.id] = it
                    repository.saveEmbedding(it)
                }
                resetComparisonStateFor(id)
                repository.saveSession(session)
                _state.update { it.copy(session = session, analyzing = false, error = null) }
                logAnalyzed("replaced", analysis.profile)
            } catch (t: Throwable) {
                onAnalysisError(t)
            }
        }
    }

    /** Removes shot [id] from the storyboard (its image + embedding files are deleted). */
    fun removeReference(id: String) {
        val session = _state.value.session ?: return
        if (session.profiles.none { it.id == id }) return
        val updated = session.removeProfile(id)
        embeddings.remove(id)
        resetComparisonStateFor(id)
        _state.update {
            if (updated.profiles.isEmpty()) it.copy(session = null) else it.copy(session = updated)
        }
        if (updated.profiles.isEmpty()) {
            // Last shot removed: fully stand down monitoring perception like clearReference.
            AnalysisRegionRegistry.clear()
            _match.value = ReferenceMatchResult()
            syncPerception(null)
        }
        scope.launch {
            deleteShotArtifacts(session, id)
            if (updated.profiles.isEmpty()) repository.clear() else repository.saveSession(updated)
        }
    }

    /** Updates the monitor options for the WHOLE storyboard (settings are not per-shot). */
    fun setOptions(options: ReferenceMonitorOptions) {
        val session = _state.value.session ?: return
        val updated = session.withOptionsForAll(options)
        _state.update { it.copy(session = updated) }
        resetComparisonState()
        scope.launch { repository.saveSession(updated) }
    }

    /** Phase 15: updates the storyboard-wide completion rule (hold / confirm / threshold). */
    fun setCompletionRule(rule: StoryboardCompletionRule) {
        val session = _state.value.session ?: return
        val updated = session.withCompletionRule(rule)
        _state.update { it.copy(session = updated) }
        // Rule change re-bases in-flight streaks (persisted completion is untouched).
        completionRuntime.clear()
        scope.launch { repository.saveSession(updated) }
    }

    /** Phase 15: explicit "reset progress" — clears every shot's completion back to Pending. */
    fun resetStoryboardProgress() {
        val session = _state.value.session ?: return
        val updated = session.withProgressReset()
        completionRuntime.clear()
        _state.update { it.copy(session = updated) }
        scope.launch { repository.saveSession(updated) }
    }

    /** Starts comparing live frames against the storyboard (no-op without any shot). */
    fun startMonitoring() {
        if (_state.value.session?.profiles.isNullOrEmpty()) return
        resetComparisonState()
        _state.update { it.copy(monitoringRequested = true) }
        syncPerception(_state.value.profile)
    }

    /** Stops monitoring but keeps the storyboard (shots + options + completion). */
    fun stopMonitoring() {
        _state.update { it.copy(monitoringRequested = false) }
        resetComparisonState()
        AnalysisRegionRegistry.clear()
        _match.value = ReferenceMatchResult() // active=false → alert latches clear silently
        syncPerception(null)
    }

    /** Removes the storyboard entirely (session, embeddings, stored images) and stops monitoring. */
    fun clearReference() {
        _state.update { ReferenceSessionState() }
        embeddings.clear()
        resetComparisonState()
        AnalysisRegionRegistry.clear()
        _match.value = ReferenceMatchResult()
        syncPerception(null)
        scope.launch { repository.clear() }
    }

    // ---- Analysis helpers ----

    private suspend fun analyzeImage(uri: Uri, name: String): ReferenceAnalysis {
        val bitmap = decodeDownscaled(uri, MAX_ANALYSIS_DIMENSION)
            ?: throw IllegalArgumentException("Could not decode the selected image.")
        val id = UUID.randomUUID().toString()
        val imageUri = repository.persistImage(bitmap, id)
        val options = _state.value.session?.activeProfile?.options ?: ReferenceMonitorOptions()
        val analysis = analyzer.analyze(
            bitmap = bitmap,
            id = id,
            name = name.ifBlank { "Shot" },
            imageUri = imageUri,
            options = options,
            nowMs = System.currentTimeMillis(),
        )
        // Capture the live camera settings for this shot (null when no camera is connected).
        val settings = cameraSettingsProvider()?.takeIf { !it.isEmpty }
        return if (settings == null) analysis
        else analysis.copy(profile = analysis.profile.copy(cameraSettings = settings))
    }

    private fun onAnalysisError(t: Throwable) {
        Log.w(TAG, "Reference analysis failed", t)
        _state.update { it.copy(analyzing = false, error = t.message ?: "Image analysis failed.") }
    }

    private fun logAnalyzed(verb: String, profile: ReferenceProfile) {
        val ai = profile.ai
        Log.d(
            TAG,
            "Shot $verb: ${profile.id} ${profile.width}x${profile.height} " +
                "scene=${ai?.sceneMode} subject=${ai?.primarySubjectCategory} strategy=${ai?.strategy}",
        )
    }

    /** Deletes the on-disk image + embedding files for shot [id]. */
    private suspend fun deleteShotArtifacts(session: ReferenceSession, id: String) {
        runCatching { repository.imageFileFor(id).delete() }
        session.profiles.firstOrNull { it.id == id }?.ai?.embeddingId
            ?.let { runCatching { repository.deleteEmbedding(it) } }
    }

    private fun resetComparisonState() {
        perceptionEngines.clear()
        stateMachines.clear()
        completionRuntime.clear()
        lastWinnerId = null
    }

    private fun resetComparisonStateFor(id: String) {
        perceptionEngines.remove(id)
        stateMachines.remove(id)
        completionRuntime.remove(id)
        if (lastWinnerId == id) lastWinnerId = null
    }

    /**
     * Phase 12: registers the current live subject box (AI-matched subject, else largest face)
     * with [AnalysisRegionRegistry] so the exposure scan produces subject-region stats, and keeps
     * the region clip thresholds in sync with the active perceptual tuning.
     */
    private fun updateSubjectRegion(
        profile: ReferenceProfile,
        vc: VisionContext,
        tuning: PerceptualTuning,
        nowMs: Long,
    ) {
        AnalysisRegionRegistry.highlightLumaMin = tuning.profile.subjectHighlightLumaMin
        AnalysisRegionRegistry.shadowLumaMax = tuning.profile.subjectShadowLumaMax

        val ai = profile.ai
        val snapshot = vc.aiScene?.snapshot?.takeIf {
            nowMs - it.analyzedAtMs <= ReferenceConfig.AI_STALE_AFTER_MS
        }
        val expected = ai?.primarySubjectBox?.let { box ->
            SubjectMatcher.Expected(
                category = ai.primarySubjectCategory,
                boundingBox = box,
                rawLabel = ai.primarySubjectRawLabel,
            )
        }
        val box = if (expected != null && snapshot != null) {
            SubjectMatcher.match(expected, snapshot.subjects)?.subject?.boundingBox
        } else {
            SceneComparator.largestFaceNormalized(vc.faces)
        }
        AnalysisRegionRegistry.subjectRegion =
            box?.let { AnalysisRegion(it.left, it.top, it.right, it.bottom) }
    }

    /**
     * Points live perception at [target]'s primary subject while monitoring is on. Phase 15: the
     * target is the Current Match shot (re-pointed on winner change), not a fixed active profile.
     */
    private fun syncPerception(target: ReferenceProfile?) {
        val coordinator = perception ?: return
        val monitoring = _state.value.monitoringRequested
        val ai = target?.ai
        val expected = ai?.primarySubjectBox?.let { box ->
            when (ai.primarySubjectType) {
                PrimarySubjectType.NONE, PrimarySubjectType.UNKNOWN, PrimarySubjectType.MULTIPLE -> null
                else -> SubjectMatcher.Expected(
                    category = ai.primarySubjectCategory,
                    boundingBox = box,
                    rawLabel = ai.primarySubjectRawLabel,
                )
            }
        }
        coordinator.setLiveMonitoring(expected, active = monitoring && target != null)
    }

    // ---- Image decoding ----

    /**
     * Two-pass decode of [uri]: bounds first, then a power-of-two `inSampleSize` so the longest
     * edge lands at or under [maxDimension] — huge gallery images never load unbounded.
     */
    private fun decodeDownscaled(uri: Uri, maxDimension: Int): Bitmap? {
        val resolver = appContext.contentResolver

        // NOTE: BitmapFactory.decodeStream() always returns null in bounds-only mode, so the
        // stream-open check must be separate from the decode call — `.use { decodeStream(...) }
        // ?: return null` would (incorrectly) bail out on every image.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = resolver.openInputStream(uri) ?: return null
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxDimension) {
            sample *= 2
        }

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }

    private companion object {
        const val TAG = "ReferenceMonitor"
        /** Longest analyzed edge — comparable to live-view frame sizes, cheap to scan. */
        const val MAX_ANALYSIS_DIMENSION = 1280
    }
}
