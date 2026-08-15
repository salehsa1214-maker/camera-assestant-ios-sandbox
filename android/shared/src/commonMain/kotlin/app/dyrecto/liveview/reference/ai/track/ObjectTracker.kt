package app.dyrecto.liveview.reference.ai.track

import app.dyrecto.liveview.reference.NormalizedRect

/**
 * Grayscale working image for tracking — plain arrays (luma 0..255 per pixel, row-major) so
 * every tracker implementation stays pure Kotlin and JVM-testable with synthetic frames.
 */
class LumaFrame(
    val width: Int,
    val height: Int,
    val luma: IntArray,
) {
    init {
        require(luma.size == width * height) { "luma size ${luma.size} != $width x $height" }
    }

    /** Mean luma of the frame — cheap global scene-change evidence for the trigger policy. */
    fun meanLuma(): Float {
        if (luma.isEmpty()) return 0f
        var sum = 0L
        for (v in luma) sum += v
        return sum.toFloat() / luma.size
    }
}

/** Opaque per-track state owned by the tracker implementation. */
interface TrackHandle

/** One tracker update: where the subject is now and how sure the tracker is (0..1). */
data class TrackUpdate(
    val box: NormalizedRect,
    val confidence: Float,
)

/**
 * Generic tracking seam (Phase 10). TrackingManager depends ONLY on this interface — the
 * concrete tracker (NCC template matching today; KCF, optical-flow, or a model-based tracker
 * tomorrow) is replaceable without touching the trigger policy, SceneSnapshot, or anything
 * downstream.
 *
 * Contract: implementations are single-threaded (called only from the perception lane),
 * allocation-light per update, and never throw — a degenerate input yields a low-confidence
 * [TrackUpdate] or a null handle instead.
 */
interface ObjectTracker {
    /** Tracker implementation name for diagnostics. */
    val id: String

    /** Start tracking the content of [box]; null when the box/frame is degenerate. */
    fun init(frame: LumaFrame, box: NormalizedRect): TrackHandle?

    /** Locate the tracked content in a new frame. */
    fun update(handle: TrackHandle, frame: LumaFrame): TrackUpdate

    /** Re-anchor the track on a verified box (e.g. after a detector verification pass). */
    fun refresh(handle: TrackHandle, frame: LumaFrame, box: NormalizedRect)

    /** Release any per-track state. */
    fun release(handle: TrackHandle)
}
