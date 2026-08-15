package app.dyrecto.liveview.exposure

import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.ExposureVerdict
import app.dyrecto.liveview.vision.results.HistogramResult
import app.dyrecto.liveview.vision.results.ZebraResult

/**
 * Combines a [HistogramResult] and a [ZebraResult] into the single interpreted [ExposureResult]
 * (Phase 6). This is the sole exposure verdict the Scene layer consumes; it replaces the old
 * per-module highlight/shadow threshold logic.
 *
 * Per the Phase 6 spec:
 *  - **highlight** detection comes from **Zebra** high-level coverage ([ZebraResult.coveragePercentage]),
 *  - **shadow** detection comes from the **Histogram** shadow distribution
 *    ([HistogramResult.clippedShadowPercentage]).
 *
 * [DETECT_PCT] (1%) preserves the app's previous descriptive presence threshold so alert timing stays
 * continuous with the modules this replaces.
 */
object ExposureAnalyzer {

    /** Coverage (%) at/above which a condition is considered present. Matches the prior 1% flag. */
    const val DETECT_PCT = 1f

    /** Coverage (%) at which confidence in a *detected* condition saturates to 1.0. */
    private const val CONFIDENCE_FULL_PCT = 10f

    // --- Phase 7: tunable constants consumed by ExposureStateMachine only. ---
    // Named here (not inline literals at any call site) so they stay configurable and are tuned
    // together during hardware validation. ExposureAnalyzer.analyze() itself does not read these —
    // they exist purely as the shared, named home for the state machine's thresholds.

    /** Highlight coverage (%) at/above which a frame counts toward confirming highlight clipping. */
    const val HIGHLIGHT_CLIP_PCT = 5f

    /** Highlight coverage (%) below which a frame counts toward recovering from confirmed clipping. */
    const val HIGHLIGHT_RECOVERY_PCT = 3f

    /** Shadow coverage (%) at/above which a frame counts toward confirming shadow detail loss. */
    const val SHADOW_CLIP_PCT = 5f

    /** Shadow coverage (%) below which a frame counts toward recovering from confirmed shadow loss. */
    const val SHADOW_RECOVERY_PCT = 3f

    /** Consecutive clipping frames required before a channel is CONFIRMED. Frame-based, not timer-based. */
    const val PERSISTENCE_FRAMES = 5

    /** Consecutive clean frames required before a CONFIRMED channel recovers to NORMAL. */
    const val RECOVERY_FRAMES = 5

    fun analyze(histogram: HistogramResult, zebra: ZebraResult): ExposureResult {
        val highlightCoverage = zebra.coveragePercentage
        val shadowCoverage = histogram.clippedShadowPercentage

        val highlightDetected = highlightCoverage > DETECT_PCT
        val shadowDetected = shadowCoverage > DETECT_PCT

        val verdict = when {
            highlightDetected && shadowDetected -> ExposureVerdict.MIXED
            highlightDetected -> ExposureVerdict.HIGHLIGHT_CLIP
            shadowDetected -> ExposureVerdict.SHADOW_CLIP
            else -> ExposureVerdict.NORMAL
        }

        val confidence = confidence(verdict, highlightCoverage, shadowCoverage)

        return ExposureResult(
            moduleId = histogram.moduleId,
            highlightDetected = highlightDetected,
            shadowDetected = shadowDetected,
            highlightCoverage = highlightCoverage,
            shadowCoverage = shadowCoverage,
            exposureState = verdict,
            exposureConfidence = confidence,
        )
    }

    /**
     * Heuristic confidence (0..1). For a detected condition, confidence rises with how far coverage
     * exceeds [DETECT_PCT], saturating at [CONFIDENCE_FULL_PCT]. For NORMAL, confidence is high when
     * both coverages sit well below the threshold and falls to 0 as either approaches it (i.e. the
     * classification is least certain right at the boundary). A shared signal for future AI logic.
     */
    private fun confidence(verdict: ExposureVerdict, highlight: Float, shadow: Float): Float {
        val maxCoverage = maxOf(highlight, shadow)
        return if (verdict == ExposureVerdict.NORMAL) {
            (1f - (maxCoverage / DETECT_PCT)).coerceIn(0f, 1f)
        } else {
            val relevant = when (verdict) {
                ExposureVerdict.HIGHLIGHT_CLIP -> highlight
                ExposureVerdict.SHADOW_CLIP -> shadow
                else -> maxCoverage // MIXED
            }
            val span = (CONFIDENCE_FULL_PCT - DETECT_PCT).coerceAtLeast(0.001f)
            ((relevant - DETECT_PCT) / span).coerceIn(0f, 1f)
        }
    }
}
