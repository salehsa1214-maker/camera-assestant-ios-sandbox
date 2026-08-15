package app.dyrecto.liveview.vision

/**
 * Portable, read-only view of one rendered frame's ARGB pixels — the KMP boundary between the
 * platform image type (android.graphics.Bitmap today, CVPixelBuffer on iOS) and the pure
 * IntArray-based analyzers.
 *
 * Implementations adapt the platform frame AT the boundary and must not copy it to do so: the one
 * bulk pixel read happens in [readArgb], exactly where `Bitmap.getPixels` ran before.
 */
interface FramePixels {
    /** Frame width in pixels. */
    val width: Int

    /** Frame height in pixels. */
    val height: Int

    /** False when the underlying platform frame is no longer readable (e.g. recycled). */
    val isAvailable: Boolean

    /**
     * Bulk-copies the full frame as packed ARGB_8888 ints into [dest] (row-major, stride =
     * [width]), matching `Bitmap.getPixels(dest, 0, width, 0, 0, width, height)`. [dest] must hold
     * at least `width * height` entries; callers reuse their scratch buffer across frames.
     */
    fun readArgb(dest: IntArray)
}
