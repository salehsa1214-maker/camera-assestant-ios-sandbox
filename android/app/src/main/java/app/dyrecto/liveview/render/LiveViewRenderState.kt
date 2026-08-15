package app.dyrecto.liveview.render

import android.graphics.Bitmap

/**
 * UI-facing snapshot published by [LiveViewFrameRenderer]. Intentionally Compose-free: it carries a
 * raw [android.graphics.Bitmap] (the screen converts it to `ImageBitmap` only when the frame
 * changes), plus the diagnostic metrics surfaced in Developer Mode.
 *
 * @param frame         latest decoded bitmap, or null before the first frame / after stop.
 * @param active        whether the decode loop is running.
 * @param receivedFps   frames handed to the renderer per second (windowed).
 * @param decodedFps    frames successfully decoded per second (windowed).
 * @param displayedFps  frames published to [LiveViewFrameRenderer.state] per second (windowed).
 * @param droppedFrames total frames dropped because decode fell behind (cumulative).
 * @param avgDecodeMs   mean decode time over the current window.
 * @param avgLatencyMs  mean submit→publish latency over the current window.
 */
data class LiveViewRenderState(
    val frame: Bitmap? = null,
    val active: Boolean = false,
    val receivedFps: Double = 0.0,
    val decodedFps: Double = 0.0,
    val displayedFps: Double = 0.0,
    val droppedFrames: Long = 0,
    val avgDecodeMs: Double = 0.0,
    val avgLatencyMs: Double = 0.0,
)
