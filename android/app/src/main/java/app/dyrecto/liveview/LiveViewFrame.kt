package app.dyrecto.liveview

/**
 * A single Live View frame, renderer-neutral. V1 carries the **encoded JPEG** bytes — no decoded
 * pixels, no Android/Compose types — so the pipeline stays reusable for GPU rendering, overlays,
 * image analysis, recording, or external-display output. Turning these bytes into a UI object is
 * the renderer's job, not the pipeline's.
 *
 * @property jpeg        the complete JPEG payload for this frame (owned copy)
 * @property sequence    monotonically increasing frame index since the stream started
 * @property timestampMs wall-clock time the frame was received
 */
class LiveViewFrame(
    val jpeg: ByteArray,
    val sequence: Long,
    val timestampMs: Long,
)
