package app.dyrecto.liveview.perception

/**
 * Coarse normalized histogram signature (Phase 12): 256 luma bins folded into
 * [PerceptualThresholds.HISTOGRAM_SIGNATURE_BINS] fractions that sum to 1. Persisted on the
 * reference profile (schema addition — legacy profiles simply lack it and the exposure
 * composite renormalizes the divergence weight away) and computed live from the frame's
 * `HistogramResult` bins by the exposure evaluator.
 */
object HistogramSignature {

    /** Downsample raw 256-luma-bin counts into a normalized signature; empty when no samples. */
    fun fromBins(
        bins: IntArray,
        signatureBins: Int = PerceptualThresholds.HISTOGRAM_SIGNATURE_BINS,
    ): List<Float> {
        if (bins.isEmpty() || signatureBins <= 0) return emptyList()
        val total = bins.sum()
        if (total <= 0) return emptyList()
        val out = FloatArray(signatureBins)
        val perBucket = bins.size / signatureBins
        if (perBucket <= 0) return emptyList()
        for (i in bins.indices) {
            out[(i / perBucket).coerceAtMost(signatureBins - 1)] += bins[i].toFloat()
        }
        for (i in out.indices) out[i] /= total.toFloat()
        return out.toList()
    }

    /**
     * 1 − histogram intersection of two normalized signatures: 0 = identical distributions,
     * 1 = disjoint. Null when the signatures are missing or incomparable (different sizes).
     */
    fun divergence(a: List<Float>, b: List<Float>): Float? {
        if (a.isEmpty() || a.size != b.size) return null
        var intersection = 0f
        for (i in a.indices) intersection += minOf(a[i], b[i])
        return (1f - intersection).coerceIn(0f, 1f)
    }
}
