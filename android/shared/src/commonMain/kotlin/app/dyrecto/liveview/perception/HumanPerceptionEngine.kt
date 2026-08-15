package app.dyrecto.liveview.perception

import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.reference.CurrentReferenceInput
import app.dyrecto.liveview.reference.ReferenceMatchResult
import app.dyrecto.liveview.reference.ReferenceProfile
import app.dyrecto.liveview.reference.ReferenceSignal

/**
 * The Human Perception Layer's stateful orchestrator (Phase 12). Sits BETWEEN `SceneComparator`
 * and `ReferenceStateMachine`: it takes the raw per-frame comparison, runs each enabled+available
 * signal through its stateless [PerceptualEvaluator], layers on the rolling perceptual history
 * (trend + transient-spike damping) and amplitude hysteresis, and returns the match result with
 * every signal's `matched` REPLACED by the perceptual decision and a full [PerceptualSignal]
 * attached. Downstream (state machine → alert rules → any future Copilot reasoning) therefore
 * consumes only perceptual verdicts — never raw deltas.
 *
 * Hysteresis composition (no double-latch): amplitude hysteresis here converts continuous
 * noticeability into a stable per-frame boolean (enter high / leave low / hold in the band);
 * the existing frame-counted state machine then debounces THAT boolean over N consecutive
 * frames. Oscillation would require repeatedly traversing the full leave→enter band while
 * holding each side for N frames — which is a real drift, not flicker.
 *
 * ALL state lives here (per-signal hysteresis + history); evaluators are pure functions.
 * Unavailable or low-confidence frames push no history samples and hold hysteresis — absence of
 * evidence neither drifts nor recovers, mirroring the state machine's hold rule.
 */
class HumanPerceptionEngine(
    private val evaluators: Map<ReferenceSignal, PerceptualEvaluator> = defaultPerceptualEvaluators(),
) {

    private val states = ReferenceSignal.entries.associateWith { SignalState() }

    fun evaluate(
        raw: ReferenceMatchResult,
        reference: ReferenceProfile,
        input: CurrentReferenceInput,
        liveSubjectExposure: SubjectExposureStats?,
        tuning: PerceptualTuning,
    ): ReferenceMatchResult {
        var result = raw
        val profile = tuning.profile
        // Phase 16: the reference's creative intent (if any) reweights how much each signal's drift
        // matters — folded into importance only, never into measurements/scores/thresholds.
        val creativeScene = reference.creativeScene
        var weightedScoreSum = 0f
        var weightSum = 0f

        for (signal in ReferenceSignal.entries) {
            val s = raw.signal(signal)
            val state = states.getValue(signal)
            if (!s.enabled) {
                state.reset()
                continue
            }
            if (!s.available) continue // hold: no evidence this frame

            val evaluator = evaluators[signal] ?: continue
            val obs = evaluator.evaluate(
                PerceptualContext(
                    signal = signal,
                    raw = s,
                    reference = reference,
                    input = input,
                    liveSubjectExposure = liveSubjectExposure,
                    strategy = raw.strategy,
                    tuning = tuning,
                ),
            )
            if (!obs.available || !PerceptualConfidence.isUsable(obs.confidence, profile)) {
                // Perception cannot judge this frame: the signal is no evidence downstream either
                // (state machine holds), and the hysteresis/history hold untouched.
                result = result.withSignal(signal, s.copy(available = false))
                continue
            }

            // Rolling history: damp transient spikes toward the recent median, classify trend.
            val damped = state.history.push(obs.noticeability, profile)
            val trend = state.history.trend(profile)

            val hysteresis = state.hysteresis.advance(
                noticeability = damped,
                enter = profile.enterNoticeability,
                leave = profile.leaveNoticeability,
            )
            val drifting = hysteresis != HysteresisState.INSIDE

            val strategyWeight = PerceptualApplicability.strategyWeight(
                raw.strategy, signal, tuning.strategyWeights,
            )
            val severity = PerceptualDifference.severityOf(damped, profile.severityCutoffs)
            val importance = PerceptualDifference.importanceOf(
                noticeability = damped,
                strategyWeight = strategyWeight,
                salience = profile.salience[signal] ?: 1f,
                gamma = profile.importanceGamma,
                referenceWeight = creativeScene
                    ?.let { CreativePriorityMapper.multiplierFor(signal, it) } ?: 1f,
            )
            val directionConfidence = if (trend == PerceptualTrend.OSCILLATING) {
                obs.directionConfidence * profile.oscillationDirectionDecay
            } else {
                obs.directionConfidence
            }

            val perception = PerceptualSignal(
                rawDifference = obs.rawDifference,
                perceptualDifference = obs.perceptualDifference,
                humanNoticeability = damped,
                importance = importance,
                confidence = obs.confidence,
                directionConfidence = directionConfidence,
                trend = trend,
                deadZoneApplied = obs.deadZoneApplied,
                hysteresisState = hysteresis,
                severity = severity,
                strategyWeight = strategyWeight,
                perceptuallyDrifting = drifting,
                direction = obs.direction,
                matchBucket = obs.matchBucket,
                components = obs.components,
            )

            result = result.withSignal(
                signal,
                s.copy(
                    matched = !drifting,
                    message = if (drifting) {
                        PerceptualMessages.message(signal, obs.direction, severity)
                    } else {
                        ""
                    },
                    perception = perception,
                ),
            )

            if (strategyWeight > 0f) {
                weightedScoreSum += (1f - damped) * strategyWeight
                weightSum += strategyWeight
            }
        }

        // Phase 16: flag that creative priorities shaped importance this frame, so the instruction
        // selector honors the reasoning-produced ranking. Presence of the model is the sole gate.
        val creativeAware = creativeScene != null
        return if (weightSum > 0f) {
            result.copy(
                overallScore = (weightedScoreSum / weightSum).coerceIn(0f, 1f),
                creativeAware = creativeAware,
            )
        } else {
            result.copy(creativeAware = creativeAware)
        }
    }

    /** Clears every signal's hysteresis + history — call wherever the state machine resets. */
    fun reset() {
        states.values.forEach { it.reset() }
    }

    // ---- Per-signal state (the ONLY mutable state in the perception layer) ----

    private class SignalState {
        val hysteresis = AmplitudeHysteresis()
        val history = PerceptualHistory()

        fun reset() {
            hysteresis.reset()
            history.reset()
        }
    }

    /** Enter-high / leave-low amplitude hysteresis over noticeability. */
    private class AmplitudeHysteresis {
        private var drifting = false

        fun advance(noticeability: Float, enter: Float, leave: Float): HysteresisState {
            if (!drifting && noticeability >= enter) drifting = true
            else if (drifting && noticeability <= leave) drifting = false
            return when {
                !drifting -> HysteresisState.INSIDE
                noticeability >= enter -> HysteresisState.ENTERED
                else -> HysteresisState.HOLDING_LEAVE
            }
        }

        fun reset() {
            drifting = false
        }
    }

    /**
     * Small rolling window of noticeability samples. Damps single-sample spikes toward the
     * recent median (a person crossing the frame, a reflection, a flash) and classifies the
     * short-term trend. Deliberately NOT another hysteresis: it never makes a drift decision.
     */
    private class PerceptualHistory {
        private val samples = ArrayDeque<Float>()

        /** Push [value]; returns the (possibly damped) value the frame should be judged by. */
        fun push(value: Float, profile: PerceptualThresholds.Profile): Float {
            val damped = if (samples.size >= MIN_SAMPLES_FOR_DAMPING) {
                val median = median()
                if (value - median > profile.spikeDeltaThreshold) {
                    median + (value - median) * profile.spikeDampingFactor
                } else {
                    value
                }
            } else {
                value
            }
            samples.addLast(value) // history keeps RAW samples so a sustained rise catches up
            while (samples.size > profile.historyWindow) samples.removeFirst()
            return damped
        }

        fun trend(profile: PerceptualThresholds.Profile): PerceptualTrend {
            if (samples.size < MIN_SAMPLES_FOR_TREND) return PerceptualTrend.STABLE
            val list = samples.toList()

            // Oscillation: many sign flips of consecutive differences with real amplitude.
            var flips = 0
            var lastSign = 0
            var min = Float.MAX_VALUE
            var max = -Float.MAX_VALUE
            for (i in list.indices) {
                min = minOf(min, list[i])
                max = maxOf(max, list[i])
                if (i == 0) continue
                val diff = list[i] - list[i - 1]
                val sign = when {
                    diff > NOISE_EPSILON -> 1
                    diff < -NOISE_EPSILON -> -1
                    else -> 0
                }
                if (sign != 0) {
                    if (lastSign != 0 && sign != lastSign) flips++
                    lastSign = sign
                }
            }
            if (flips >= profile.oscillationMinFlips && max - min > profile.trendDelta) {
                return PerceptualTrend.OSCILLATING
            }

            val half = list.size / 2
            val firstMean = list.subList(0, half).average().toFloat()
            val secondMean = list.subList(half, list.size).average().toFloat()
            return when {
                secondMean - firstMean > profile.trendDelta -> PerceptualTrend.INCREASING
                firstMean - secondMean > profile.trendDelta -> PerceptualTrend.DECREASING
                else -> PerceptualTrend.STABLE
            }
        }

        private fun median(): Float {
            val sorted = samples.sorted()
            val mid = sorted.size / 2
            return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2f
        }

        fun reset() = samples.clear()

        private companion object {
            /** Damping needs an established baseline; trend needs enough shape to classify. */
            const val MIN_SAMPLES_FOR_DAMPING = 3
            const val MIN_SAMPLES_FOR_TREND = 4
            /** Consecutive-difference noise floor for oscillation counting. */
            const val NOISE_EPSILON = 0.01f
        }
    }
}
