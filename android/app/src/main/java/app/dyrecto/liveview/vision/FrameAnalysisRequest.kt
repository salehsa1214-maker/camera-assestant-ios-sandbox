package app.dyrecto.liveview.vision

import android.graphics.Bitmap
import app.dyrecto.liveview.session.FrameContext

/**
 * Immutable unit of work handed to every [VisionModule].
 *
 * Ownership contract (Phase 5A): Vision is a *passive* consumer of frames the renderer already
 * produced. The [bitmap] is shared **by reference** — it is the very same Bitmap the renderer
 * published. Modules MUST treat it as strictly read-only: never mutate it, never `recycle()` it,
 * never retain it past the [analyze][VisionModule.analyze] call. The renderer stays the sole bitmap
 * owner (mirrors the note in `FrameContextPublisher`). [context] is likewise a read-only snapshot.
 */
data class FrameAnalysisRequest(
    /** The renderer's published frame — shared by reference, READ-ONLY (never clone/mutate/recycle). */
    val bitmap: Bitmap,
    /** Read-only realtime metadata paired with this frame (latest-wins snapshot). */
    val context: FrameContext,
    /** Wall-clock time the pipeline received this frame, for latency/age diagnostics. */
    val receivedAtMs: Long,
) : FramePixels {
    // The request IS the platform→portable boundary adapter (no extra per-frame object): portable
    // consumers read pixels through [FramePixels]; Android-only modules (ML Kit, MediaPipe) keep
    // using [bitmap] directly.
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height
    override val isAvailable: Boolean get() = !bitmap.isRecycled
    override fun readArgb(dest: IntArray) =
        bitmap.getPixels(dest, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
}
