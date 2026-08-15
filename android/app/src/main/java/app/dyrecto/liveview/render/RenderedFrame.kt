package app.dyrecto.liveview.render

import android.graphics.Bitmap

/**
 * A decoded, retainable live-view frame.
 *
 * Deliberately holds an [android.graphics.Bitmap] — NOT a Compose `ImageBitmap` — so the renderer
 * stays UI-agnostic and can later feed a `SurfaceView`/OpenGL/video path unchanged. The Compose
 * screen is the only place that converts `Bitmap → ImageBitmap` for display.
 *
 * @param bitmap     the decoded image.
 * @param index      source VERIC frame index (emission order within the parser's lifetime).
 * @param width      decoded pixel width.
 * @param height     decoded pixel height.
 * @param decodeMs   wall-clock cost of decoding this frame.
 * @param producedAtMs wall-clock when the frame was published.
 */
data class RenderedFrame(
    val bitmap: Bitmap,
    val index: Int,
    val width: Int,
    val height: Int,
    val decodeMs: Long,
    val producedAtMs: Long,
)
