package app.dyrecto.liveview.reference.ai

import android.graphics.Bitmap
import app.dyrecto.liveview.reference.ai.track.LumaFrame
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Android-side bridge from a Bitmap to the tracker's pure [LumaFrame] working image.
 * Kept out of the pure logic so trackers/policies stay JVM-testable; the coordinator takes
 * this as an injectable function for the same reason.
 */
object BitmapLuma {
    /** Downscale to [maxEdge] on the longest side and convert to Rec.709-weighted luma. */
    fun extract(bitmap: Bitmap, maxEdge: Int): LumaFrame {
        val scale = maxEdge.toFloat() / max(bitmap.width, bitmap.height)
        val w = if (scale < 1f) max(1, (bitmap.width * scale).roundToInt()) else bitmap.width
        val h = if (scale < 1f) max(1, (bitmap.height * scale).roundToInt()) else bitmap.height
        val scaled = if (w != bitmap.width || h != bitmap.height) {
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }
        try {
            val pixels = IntArray(w * h)
            scaled.getPixels(pixels, 0, w, 0, 0, w, h)
            val luma = IntArray(w * h)
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                luma[i] = (54 * r + 183 * g + 19 * b) shr 8
            }
            return LumaFrame(w, h, luma)
        } finally {
            if (scaled !== bitmap) scaled.recycle()
        }
    }
}
