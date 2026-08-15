package app.dyrecto.liveview.reference.ai.track

import app.dyrecto.liveview.reference.ai.SubjectCategory
import kotlin.concurrent.Volatile
import app.dyrecto.liveview.reference.ai.SubjectMatcher
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject

/**
 * Owns the live track: subject identity, tracker state, loss counting, and the detector
 * trigger policy. Depends ONLY on the [ObjectTracker] interface — never on a concrete tracker.
 *
 * Flow per Detection → Tracking → Comparison:
 *  - [expectSubject] pins WHAT should be tracked (from the reference profile's primary subject);
 *  - [detectorTrigger] says when a detector pass is justified (init/loss/scene-change/verify);
 *  - [onDetections] consumes a detector pass: weighted-match the expectation against fresh
 *    detections, then init or re-anchor the tracker on the matched box;
 *  - [onTrackerFrame] is the cheap per-cadence update that carries normal monitoring.
 *
 * Pure Kotlin (LumaFrame + normalized geometry) — JVM-testable with a fake tracker.
 */
class TrackingManager(
    private val tracker: ObjectTracker,
    private val policyConfig: TrackPolicyConfig = TrackPolicyConfig(),
) {
    /** What the reference wants tracked; null = nothing to track (no-subject reference). */
    private var expectation: SubjectMatcher.Expected? = null

    private var handle: TrackHandle? = null
    private var trackId: Int = SceneSubject.NO_TRACK
    private var nextTrackId: Int = 1
    private var category: SubjectCategory = SubjectCategory.UNKNOWN
    private var rawLabel: String? = null
    private var lastUpdate: TrackUpdate? = null
    private var lowConfidenceStreak: Int = 0
    private var lastVerifiedAtMs: Long = 0L
    private var lost: Boolean = false

    /** Diagnostics. */
    val trackerId: String get() = tracker.id
    val hasActiveTrack: Boolean get() = handle != null && !lost
    val currentConfidence: Float get() = lastUpdate?.confidence ?: 0f
    val currentTrackId: Int get() = trackId

    @Volatile var detectorRuns: Long = 0L
        private set

    fun expectSubject(expected: SubjectMatcher.Expected?) {
        if (expectation?.category != expected?.category) releaseTrack()
        expectation = expected
    }

    fun reset() {
        releaseTrack()
        expectation = null
        lastVerifiedAtMs = 0L
    }

    /** Which detector trigger (if any) applies right now. */
    fun detectorTrigger(
        monitoringActive: Boolean,
        nowMs: Long,
        embeddingSimilarityToVerified: Float?,
        meanLumaDeltaSinceVerified: Float?,
    ): DetectorTrigger {
        if (expectation == null) return DetectorTrigger.NONE
        return TrackPolicy.decide(
            TrackPolicy.Input(
                monitoringActive = monitoringActive,
                hasActiveTrack = hasActiveTrack,
                consecutiveLowConfidenceFrames = lowConfidenceStreak,
                msSinceLastVerify = if (lastVerifiedAtMs == 0L) Long.MAX_VALUE else nowMs - lastVerifiedAtMs,
                embeddingSimilarityToVerified = embeddingSimilarityToVerified,
                meanLumaDeltaSinceVerified = meanLumaDeltaSinceVerified,
            ),
            policyConfig,
        )
    }

    /**
     * Consume a detector pass: match the expectation against fresh detections and (re)anchor
     * the track. Returns the verified subject, or null when the expected subject is not in
     * frame (which IS evidence — the comparator's subject-presence signal consumes it).
     */
    fun onDetections(
        frame: LumaFrame,
        detections: List<SceneSubject>,
        nowMs: Long,
    ): SceneSubject? {
        val expected = expectation ?: return null
        lastVerifiedAtMs = nowMs
        // Prefer continuity: bias matching toward the current track's last position.
        val anchor = lastUpdate?.box?.takeIf { !lost } ?: expected.boundingBox
        val match = SubjectMatcher.match(
            expected = SubjectMatcher.Expected(
                category = expected.category,
                boundingBox = anchor,
                rawLabel = expected.rawLabel,
                trackId = trackId,
            ),
            candidates = detections,
        )
        detectorRuns++
        if (match == null) {
            declareLost()
            return null
        }

        val box = match.subject.boundingBox
        val existing = handle
        if (existing != null) {
            tracker.refresh(existing, frame, box)
        } else {
            handle = tracker.init(frame, box)
            if (handle != null) trackId = nextTrackId++
        }
        lost = false
        lowConfidenceStreak = 0
        category = match.subject.category
        rawLabel = match.subject.rawLabel
        lastUpdate = TrackUpdate(box, match.subject.confidence)
        return currentSubject(match.subject.confidence)
    }

    /** Cheap per-cadence tracker update. Null while there is no live track. */
    fun onTrackerFrame(frame: LumaFrame): SceneSubject? {
        val activeHandle = handle ?: return null
        if (lost) return null
        val update = tracker.update(activeHandle, frame)
        lastUpdate = update
        if (update.confidence < policyConfig.lossConfidenceThreshold) {
            lowConfidenceStreak++
            if (lowConfidenceStreak >= policyConfig.lossFramesToDeclare) declareLost()
        } else {
            lowConfidenceStreak = 0
        }
        if (lost) return null
        return currentSubject(update.confidence)
    }

    private fun currentSubject(confidence: Float): SceneSubject? {
        val update = lastUpdate ?: return null
        return SceneSubject(
            trackId = trackId,
            category = category,
            rawLabel = rawLabel,
            confidence = confidence,
            boundingBox = update.box,
            tracked = true,
            maskAvailable = false,
        )
    }

    private fun declareLost() {
        lost = true
        // The handle is kept released so re-acquisition builds a fresh template.
        releaseTrack(keepLostFlag = true)
    }

    private fun releaseTrack(keepLostFlag: Boolean = false) {
        handle?.let { tracker.release(it) }
        handle = null
        lastUpdate = if (keepLostFlag) lastUpdate else null
        lowConfidenceStreak = 0
        if (!keepLostFlag) {
            lost = false
            trackId = SceneSubject.NO_TRACK
        }
    }
}
