package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.ExposureChannelState
import app.dyrecto.liveview.vision.results.ExposureResult

/**
 * Frame-based persistence/recovery state machine (Phase 7). Sits between the pure, stateless
 * per-frame [ExposureAnalyzer] verdict and the Scene layer, turning raw per-frame coverage into
 * *confirmed*, debounced highlight/shadow loss — so a single-frame spike, an alternating
 * clipped/unclipped sequence, or brief flicker never confirms.
 *
 * Highlight and shadow channels are tracked independently, each via [ChannelTracker]. Persistence
 * and recovery are strictly frame-counted (never timer-based), using the thresholds in
 * [ExposureAnalyzer]: [ExposureAnalyzer.PERSISTENCE_FRAMES] consecutive clipping frames confirm a
 * channel; [ExposureAnalyzer.RECOVERY_FRAMES] consecutive clean frames (below the lower recovery
 * threshold — a hysteresis dead-band vs. the clip threshold) clear it. Counters reset to zero the
 * instant a streak breaks, so nothing partial ever confirms.
 *
 * Owned by [app.dyrecto.liveview.vision.modules.ExposureModule] (one instance per
 * Vision worker, ticked once per frame after [ExposureAnalyzer.analyze]). Holds only small mutable
 * counters — no bitmap/luma access, no allocation beyond the [ExposureResult.copy] already
 * produced by the caller.
 */
class ExposureStateMachine {

    private val highlight = ChannelTracker()
    private val shadow = ChannelTracker()

    /** Enriches [raw] (the pure per-frame verdict) with this frame's persistence/recovery/confirmed state. */
    fun update(raw: ExposureResult): ExposureResult {
        val highlightOutcome = highlight.advance(
            coverage = raw.highlightCoverage,
            clipPct = ExposureAnalyzer.HIGHLIGHT_CLIP_PCT,
            recoveryPct = ExposureAnalyzer.HIGHLIGHT_RECOVERY_PCT,
        )
        val shadowOutcome = shadow.advance(
            coverage = raw.shadowCoverage,
            clipPct = ExposureAnalyzer.SHADOW_CLIP_PCT,
            recoveryPct = ExposureAnalyzer.SHADOW_RECOVERY_PCT,
        )

        return raw.copy(
            highlightState = highlightOutcome.state,
            shadowState = shadowOutcome.state,
            highlightPersistenceFrames = highlightOutcome.persistenceFrames,
            shadowPersistenceFrames = shadowOutcome.persistenceFrames,
            highlightRecoveryFrames = highlightOutcome.recoveryFrames,
            shadowRecoveryFrames = shadowOutcome.recoveryFrames,
            highlightConfirmed = highlightOutcome.confirmed,
            shadowConfirmed = shadowOutcome.confirmed,
        )
    }

    /** Clears both channels back to NORMAL — call at the start of a new session. */
    fun reset() {
        highlight.reset()
        shadow.reset()
    }

    /**
     * One channel's persistence/recovery counters. Mutable and reused frame-to-frame (no
     * per-frame allocation); [advance] returns a small immutable [Outcome] snapshot.
     */
    private class ChannelTracker {
        private var confirmed = false
        private var persistenceFrames = 0
        private var recoveryFrames = 0

        fun advance(coverage: Float, clipPct: Float, recoveryPct: Float): Outcome {
            val state: ExposureChannelState
            if (!confirmed) {
                if (coverage >= clipPct) {
                    persistenceFrames++
                    if (persistenceFrames >= ExposureAnalyzer.PERSISTENCE_FRAMES) {
                        confirmed = true
                        recoveryFrames = 0
                        state = ExposureChannelState.CONFIRMED
                    } else {
                        state = ExposureChannelState.CLIPPING
                    }
                } else {
                    persistenceFrames = 0
                    state = ExposureChannelState.NORMAL
                }
            } else {
                if (coverage < recoveryPct) {
                    recoveryFrames++
                    if (recoveryFrames >= ExposureAnalyzer.RECOVERY_FRAMES) {
                        confirmed = false
                        persistenceFrames = 0
                        recoveryFrames = 0
                        state = ExposureChannelState.NORMAL
                    } else {
                        state = ExposureChannelState.RECOVERING
                    }
                } else {
                    recoveryFrames = 0
                    state = ExposureChannelState.CONFIRMED
                }
            }
            return Outcome(state, persistenceFrames, recoveryFrames, confirmed)
        }

        fun reset() {
            confirmed = false
            persistenceFrames = 0
            recoveryFrames = 0
        }
    }

    private data class Outcome(
        val state: ExposureChannelState,
        val persistenceFrames: Int,
        val recoveryFrames: Int,
        val confirmed: Boolean,
    )
}
