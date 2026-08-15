package app.dyrecto.liveview.perception

import kotlin.math.abs

/**
 * Declarative noticeability curve — pure data, interpreted by [PerceptualDifference.applyCurve].
 * Each evaluator picks its own curve via [PerceptualThresholds]; there is deliberately NO single
 * universal mapping, because humans do not perceive exposure, warmth, and geometry equally.
 */
data class CurveSpec(
    val type: CurveType,
    /** POWER: the gamma exponent. Ignored by SMOOTHSTEP. */
    val gamma: Float = 1f,
    /**
     * PIECEWISE: (input, output) breakpoints in ascending input order, linearly interpolated;
     * inputs below the first breakpoint map to its output, above the last to its output.
     */
    val breakpoints: List<Pair<Float, Float>> = emptyList(),
) {
    enum class CurveType { LINEAR, SMOOTHSTEP, POWER, PIECEWISE }
}

/**
 * Pure math toolbox of the Human Perception Layer (Phase 12). Everything here is stateless and
 * literal-free: dead zones, saturations, curve parameters, and cutoffs all arrive as arguments
 * from [PerceptualThresholds] so tuning never touches this code.
 */
object PerceptualDifference {

    /**
     * Dead zone + saturation normalization: 0 while |raw| is inside [deadZone] (visually
     * identical), 1 at or beyond [saturation], linear in between.
     */
    fun normalize(raw: Float, deadZone: Float, saturation: Float): Float {
        val magnitude = abs(raw)
        if (magnitude <= deadZone) return 0f
        val span = saturation - deadZone
        if (span <= 0f) return 1f
        return ((magnitude - deadZone) / span).coerceIn(0f, 1f)
    }

    /** Interpret a [CurveSpec] over a normalized perceptual difference (0..1 in, 0..1 out). */
    fun applyCurve(p: Float, curve: CurveSpec): Float {
        val x = p.coerceIn(0f, 1f)
        return when (curve.type) {
            CurveSpec.CurveType.LINEAR -> x
            CurveSpec.CurveType.SMOOTHSTEP -> x * x * (3f - 2f * x)
            CurveSpec.CurveType.POWER -> {
                if (curve.gamma <= 0f) x else Math.pow(x.toDouble(), curve.gamma.toDouble()).toFloat()
            }
            CurveSpec.CurveType.PIECEWISE -> piecewise(x, curve.breakpoints)
        }.coerceIn(0f, 1f)
    }

    private fun piecewise(x: Float, points: List<Pair<Float, Float>>): Float {
        if (points.isEmpty()) return x
        if (x <= points.first().first) return points.first().second
        for (i in 1 until points.size) {
            val (x1, y1) = points[i - 1]
            val (x2, y2) = points[i]
            if (x <= x2) {
                if (x2 <= x1) return y2
                val t = (x - x1) / (x2 - x1)
                return y1 + t * (y2 - y1)
            }
        }
        return points.last().second
    }

    /**
     * Weighted mean over the components whose value is non-null; weights of missing components
     * are renormalized away (a missing metric neither drags toward match nor drift).
     * Returns 0 when nothing is available.
     */
    fun weightedComposite(components: List<Pair<Float?, Float>>): Float {
        var sum = 0f
        var weightSum = 0f
        for ((value, weight) in components) {
            if (value == null || weight <= 0f) continue
            sum += value * weight
            weightSum += weight
        }
        return if (weightSum <= 0f) 0f else (sum / weightSum).coerceIn(0f, 1f)
    }

    /** Bucket noticeability into severity using ascending [cutoffs] (size 4: NONE→SEVERE bounds). */
    fun severityOf(noticeability: Float, cutoffs: FloatArray): PerceptualSeverity = when {
        noticeability < cutoffs[0] -> PerceptualSeverity.NONE
        noticeability < cutoffs[1] -> PerceptualSeverity.SUBTLE
        noticeability < cutoffs[2] -> PerceptualSeverity.NOTICEABLE
        noticeability < cutoffs[3] -> PerceptualSeverity.OBVIOUS
        else -> PerceptualSeverity.SEVERE
    }

    /**
     * "How much should the assistant care?" — combines what the operator can see with how central
     * the signal is to the active strategy ([strategyWeight]) and how much this kind of drift
     * matters relative to others ([salience], e.g. a missing subject outranks equal-noticeability
     * warmth drift). [gamma] shapes the emphasis on strong drifts.
     *
     * Phase 16: [referenceWeight] is a per-reference creative multiplier (centered on 1.0) supplied
     * by `CreativePriorityMapper` from the reference's Creative Scene Model — it lets the creative
     * intent of *this* shot raise or lower how much a given signal's drift matters, without touching
     * measurements. Defaults to 1.0, so callers without a creative model (and all existing behavior)
     * are unaffected.
     */
    fun importanceOf(
        noticeability: Float,
        strategyWeight: Float,
        salience: Float,
        gamma: Float,
        referenceWeight: Float = 1f,
    ): Float {
        val n = applyCurve(noticeability, CurveSpec(CurveSpec.CurveType.POWER, gamma = gamma))
        return (n * strategyWeight.coerceIn(0f, 1f) * salience.coerceIn(0f, 2f) *
            referenceWeight.coerceIn(0f, 2f)).coerceIn(0f, 1f)
    }
}
