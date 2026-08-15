package app.dyrecto.ui.liveview

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.dyrecto.liveview.LiveViewFrame

/**
 * Converts a renderer-neutral [LiveViewFrame] (encoded JPEG) into an Android UI object. This is the
 * ONLY place `BitmapFactory` / `ImageBitmap` appear in the Live View path — the pipeline
 * ([app.dyrecto.liveview.LiveViewSession]) stays UI-agnostic, so a future GPU/OpenGL
 * or `Surface`-backed renderer is a drop-in replacement here without touching the pipeline.
 *
 * Implementations must be safe to call off the main thread (decoding is CPU work).
 */
interface LiveViewRenderer {
    fun render(frame: LiveViewFrame): ImageBitmap?
}

/** V1 software renderer: JPEG → [android.graphics.Bitmap] → [ImageBitmap]. */
class BitmapLiveViewRenderer : LiveViewRenderer {
    override fun render(frame: LiveViewFrame): ImageBitmap? =
        runCatching {
            BitmapFactory.decodeByteArray(frame.jpeg, 0, frame.jpeg.size)?.asImageBitmap()
        }.getOrNull()
}
