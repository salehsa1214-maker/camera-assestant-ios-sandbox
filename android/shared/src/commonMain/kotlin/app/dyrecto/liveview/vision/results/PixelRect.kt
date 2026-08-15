package app.dyrecto.liveview.vision.results

/**
 * Pixel-space bounding box (left/top/right/bottom, right/bottom exclusive by convention) — the
 * KMP stand-in for `android.graphics.Rect` in vision results. The Android detection module
 * converts ML Kit rects into this at the module boundary.
 */
data class PixelRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
