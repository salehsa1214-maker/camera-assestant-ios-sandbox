package app.dyrecto.liveview.vision.results

/**
 * A luma waveform for one analyzed frame — a column-by-column distribution of luma, computed on-device
 * from the shared [app.dyrecto.liveview.exposure.LumaField] (like Sony's MonitorAssist renders it
 * from the video buffer, not from camera-supplied data).
 *
 * [intensity] is a `[columns * bins]` row-major buffer indexed `col * bins + bin`, where `bin` 0 is
 * the darkest luma and `bin == bins-1` the brightest. Values are sample counts, capped by the engine.
 * The Android renderer maps intensity → brightness to draw the trace; this class carries no colours.
 */
data class WaveformResult(
    override val moduleId: String,
    /** Output column count (horizontal resolution of the waveform). */
    val columns: Int,
    /** Vertical luma-bin count (e.g. 256). */
    val bins: Int,
    /** Row-major `columns * bins` intensity buffer (`col * bins + bin`). */
    val intensity: IntArray,
    /** Largest single-cell count this frame, for the renderer to normalize brightness. */
    val maxIntensity: Int,
    val totalSamples: Int,
    val effectiveStride: Int,
) : VisionResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WaveformResult) return false
        return moduleId == other.moduleId &&
            columns == other.columns &&
            bins == other.bins &&
            maxIntensity == other.maxIntensity &&
            totalSamples == other.totalSamples &&
            effectiveStride == other.effectiveStride &&
            intensity.contentEquals(other.intensity)
    }

    override fun hashCode(): Int {
        var result = moduleId.hashCode()
        result = 31 * result + columns
        result = 31 * result + bins
        result = 31 * result + maxIntensity
        result = 31 * result + effectiveStride
        return result
    }
}
