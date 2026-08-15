package app.dyrecto.liveview.reference

/**
 * Frame-counted persistence/recovery state machine for every Shot Reference signal (Phase 9).
 * Sibling of [app.dyrecto.liveview.exposure.ExposureStateMachine]: a drift must hold
 * for N *consecutive* analyzed frames before it becomes [ReferenceSignalState.CONFIRMED_DRIFT],
 * and a confirmed drift must match for N consecutive frames before it clears — so single-frame
 * spikes, alternating match/drift flicker, and detector noise never confirm and never fire alerts.
 *
 * Strictly frame-based, never timer-based. One [SignalTracker] per [ReferenceSignal]; counters
 * reset the instant a streak breaks.
 *
 * Unavailable signals (e.g. subject metrics while no live face is detected but FACE_PRESENCE owns
 * the drift) **hold** their current state without advancing either counter: absence of evidence
 * neither confirms a drift nor recovers one. Disabled signals reset to NORMAL.
 */
class ReferenceStateMachine {

    private val trackers = ReferenceSignal.entries.associateWith { SignalTracker() }

    /**
     * Enriches [raw] (the pure per-frame [SceneComparator] verdict) with per-signal
     * state/counters/confirmed flags, using the persistence/recovery frame counts from
     * [thresholds].
     */
    fun update(
        raw: ReferenceMatchResult,
        thresholds: ReferenceConfig.ReferenceThresholds,
    ): ReferenceMatchResult {
        var result = raw
        for (signal in ReferenceSignal.entries) {
            val tracker = trackers.getValue(signal)
            val s = raw.signal(signal)
            val outcome = tracker.advance(
                enabled = s.enabled,
                available = s.available,
                matched = s.matched,
                persistenceFrames = thresholds.requiredPersistenceFrames,
                recoveryFrames = thresholds.requiredRecoveryFrames,
            )
            result = result.withSignal(
                signal,
                s.copy(
                    state = outcome.state,
                    persistenceFrames = outcome.persistenceFrames,
                    recoveryFrames = outcome.recoveryFrames,
                    confirmedDrift = outcome.confirmed,
                ),
            )
        }
        return result
    }

    /** Clears every signal back to NORMAL — call when monitoring (re)starts or the reference changes. */
    fun reset() {
        trackers.values.forEach { it.reset() }
    }

    /** One signal's counters, mutable and reused frame-to-frame (no per-frame allocation). */
    private class SignalTracker {
        private var confirmed = false
        private var state = ReferenceSignalState.NORMAL
        private var persistence = 0
        private var recovery = 0

        fun advance(
            enabled: Boolean,
            available: Boolean,
            matched: Boolean,
            persistenceFrames: Int,
            recoveryFrames: Int,
        ): Outcome {
            if (!enabled) {
                reset()
                return Outcome(state, persistence, recovery, confirmed)
            }
            if (!available) {
                // Hold: no evidence either way this frame.
                return Outcome(state, persistence, recovery, confirmed)
            }

            if (!confirmed) {
                if (!matched) {
                    persistence++
                    if (persistence >= persistenceFrames) {
                        confirmed = true
                        recovery = 0
                        state = ReferenceSignalState.CONFIRMED_DRIFT
                    } else {
                        state = ReferenceSignalState.DRIFTING
                    }
                } else {
                    persistence = 0
                    state = ReferenceSignalState.NORMAL
                }
            } else {
                if (matched) {
                    recovery++
                    if (recovery >= recoveryFrames) {
                        confirmed = false
                        persistence = 0
                        recovery = 0
                        state = ReferenceSignalState.NORMAL
                    } else {
                        state = ReferenceSignalState.RECOVERING
                    }
                } else {
                    recovery = 0
                    state = ReferenceSignalState.CONFIRMED_DRIFT
                }
            }
            return Outcome(state, persistence, recovery, confirmed)
        }

        fun reset() {
            confirmed = false
            state = ReferenceSignalState.NORMAL
            persistence = 0
            recovery = 0
        }
    }

    private data class Outcome(
        val state: ReferenceSignalState,
        val persistenceFrames: Int,
        val recoveryFrames: Int,
        val confirmed: Boolean,
    )
}
