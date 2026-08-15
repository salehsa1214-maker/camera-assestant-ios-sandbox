package app.dyrecto.liveview.perception

/**
 * Confidence combination for perceptual signals (Phase 12). Pure and stateless.
 *
 * The blend is a weighted-min: the weakest feature dominates (a composite built on one shaky
 * input is shaky), softened by the mean so one mediocre input among several strong ones does not
 * flatten the result to its exact minimum.
 */
object PerceptualConfidence {

    /** Fraction of the result taken from the minimum; the rest comes from the mean. */
    private const val MIN_WEIGHT = 0.7f

    /**
     * Combine feature confidences (nulls = feature not applicable, ignored). No inputs → 0
     * (nothing to be confident about).
     */
    fun combine(vararg featureConfidences: Float?): Float {
        var min = Float.MAX_VALUE
        var sum = 0f
        var count = 0
        for (c in featureConfidences) {
            if (c == null) continue
            val clamped = c.coerceIn(0f, 1f)
            if (clamped < min) min = clamped
            sum += clamped
            count++
        }
        if (count == 0) return 0f
        val mean = sum / count
        return (MIN_WEIGHT * min + (1f - MIN_WEIGHT) * mean).coerceIn(0f, 1f)
    }

    /** Below the profile's minimum confidence a signal is unavailable to perception (engine holds). */
    fun isUsable(confidence: Float, profile: PerceptualThresholds.Profile): Boolean =
        confidence >= profile.minConfidence
}
