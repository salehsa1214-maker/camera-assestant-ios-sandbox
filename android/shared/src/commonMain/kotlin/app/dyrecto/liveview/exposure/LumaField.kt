package app.dyrecto.liveview.exposure

/**
 * The canonical output of the single per-frame pixel scan (Phase 6).
 *
 * A [LumaField] is produced once by [LuminanceAnalyzer] and then consumed, read-only, by every
 * exposure tool — [HistogramEngine], [ZebraEngine], and future False Color / Waveform / RGB Parade /
 * Focus Peaking / AI analysis. **No consumer ever rescans the Bitmap**; they all read [luma] here.
 *
 * The buffer is *reusable*: the owner (`ExposureModule`) keeps one instance and re-fills it every
 * frame via [LuminanceAnalyzer.analyze], avoiding per-frame allocation. Only [count] entries of
 * [luma] are valid for the current frame — callers MUST iterate `0 until count`, never `luma.size`.
 *
 * Luma is stored as `Int` in 0..255 (BT.709; see [LuminanceAnalyzer]). It is not `ByteArray` so
 * consumers need no `and 0xFF` unsigned dance on every read.
 */
class LumaField {
    /** Reusable luma buffer (0..255). Only the first [count] entries are valid this frame. */
    var luma: IntArray = IntArray(0)
        private set

    /** Number of valid luma samples this frame (== sampled pixel count, not bitmap pixel count). */
    var count: Int = 0
        private set

    /** Source frame width in pixels (full resolution, before striding). */
    var sourceWidth: Int = 0
        private set

    /** Source frame height in pixels (full resolution, before striding). */
    var sourceHeight: Int = 0
        private set

    /** Effective sampling stride used to build this field (1 = full resolution; >1 = downsampled). */
    var effectiveStride: Int = 1
        private set

    /** Ensures [luma] can hold [needed] samples, growing (never shrinking) the reusable buffer. */
    internal fun ensureCapacity(needed: Int) {
        if (luma.size < needed) luma = IntArray(needed)
    }

    /** Called by [LuminanceAnalyzer] after filling [luma] to publish this frame's metadata. */
    internal fun commit(count: Int, sourceWidth: Int, sourceHeight: Int, effectiveStride: Int) {
        this.count = count
        this.sourceWidth = sourceWidth
        this.sourceHeight = sourceHeight
        this.effectiveStride = effectiveStride
    }
}
