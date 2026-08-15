package app.dyrecto.liveview.reference.ai.track

import app.dyrecto.liveview.reference.NormalizedRect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * First [ObjectTracker] implementation: normalized cross-correlation template matching on the
 * downscaled luma working image.
 *
 * Deliberately simple — appearance-only, fixed scale, no rotation robustness. Its drift is
 * bounded by the periodic detector verification in the trigger policy, which re-anchors the
 * template via [refresh]. A stronger tracker can replace this class behind the interface
 * without touching anything else.
 *
 * Cost model: templates are downsampled to ≤[MAX_TEMPLATE_EDGE]² samples but keep their source
 * footprint (span) in frame pixels; matching samples the frame at the same stride, over a
 * ±search-radius window at integer offsets — a few milliseconds on a ~160px working image.
 */
class NccTemplateTracker : ObjectTracker {
    override val id: String = "ncc-template"

    private class State(
        var template: IntArray,
        /** Template sample grid. */
        var gridW: Int,
        var gridH: Int,
        /** Source footprint of the template in frame pixels. */
        var spanW: Int,
        var spanH: Int,
        /** Top-left of the tracked patch in working-image pixels. */
        var pixelX: Int,
        var pixelY: Int,
        /** Original box size in normalized units (scale is not adapted between refreshes). */
        var boxWidth: Float,
        var boxHeight: Float,
    ) : TrackHandle

    override fun init(frame: LumaFrame, box: NormalizedRect): TrackHandle? {
        val patch = extractPatch(frame, box) ?: return null
        return State(
            template = patch.pixels,
            gridW = patch.gridW,
            gridH = patch.gridH,
            spanW = patch.spanW,
            spanH = patch.spanH,
            pixelX = patch.x,
            pixelY = patch.y,
            boxWidth = box.width,
            boxHeight = box.height,
        )
    }

    override fun update(handle: TrackHandle, frame: LumaFrame): TrackUpdate {
        val state = handle as State
        val searchRadius = max(MIN_SEARCH_RADIUS, max(state.spanW, state.spanH) / 2)

        var bestScore = -1f
        var bestX = state.pixelX
        var bestY = state.pixelY
        val minX = max(0, state.pixelX - searchRadius)
        val maxX = min(frame.width - state.spanW, state.pixelX + searchRadius)
        val minY = max(0, state.pixelY - searchRadius)
        val maxY = min(frame.height - state.spanH, state.pixelY + searchRadius)
        if (maxX < minX || maxY < minY) {
            return TrackUpdate(currentBox(state, frame), 0f)
        }
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val score = ncc(state, frame, x, y)
                if (score > bestScore) {
                    bestScore = score
                    bestX = x
                    bestY = y
                }
            }
        }

        state.pixelX = bestX
        state.pixelY = bestY
        // NCC is [-1, 1]; anything ≤ 0 is "not found" for natural images.
        val confidence = bestScore.coerceIn(0f, 1f)
        return TrackUpdate(currentBox(state, frame), confidence)
    }

    override fun refresh(handle: TrackHandle, frame: LumaFrame, box: NormalizedRect) {
        val state = handle as State
        val patch = extractPatch(frame, box) ?: return
        state.template = patch.pixels
        state.gridW = patch.gridW
        state.gridH = patch.gridH
        state.spanW = patch.spanW
        state.spanH = patch.spanH
        state.pixelX = patch.x
        state.pixelY = patch.y
        state.boxWidth = box.width
        state.boxHeight = box.height
    }

    override fun release(handle: TrackHandle) {
        // State is plain arrays; nothing to free.
    }

    private fun currentBox(state: State, frame: LumaFrame): NormalizedRect {
        val centerX = (state.pixelX + state.spanW / 2f) / frame.width
        val centerY = (state.pixelY + state.spanH / 2f) / frame.height
        val halfW = state.boxWidth / 2f
        val halfH = state.boxHeight / 2f
        return NormalizedRect(
            left = (centerX - halfW).coerceIn(0f, 1f),
            top = (centerY - halfH).coerceIn(0f, 1f),
            right = (centerX + halfW).coerceIn(0f, 1f),
            bottom = (centerY + halfH).coerceIn(0f, 1f),
        )
    }

    private class Patch(
        val pixels: IntArray,
        val gridW: Int,
        val gridH: Int,
        val spanW: Int,
        val spanH: Int,
        val x: Int,
        val y: Int,
    )

    /** Sample the box content on a ≤[MAX_TEMPLATE_EDGE]² grid, keeping its pixel footprint. */
    private fun extractPatch(frame: LumaFrame, box: NormalizedRect): Patch? {
        val left = (box.left * frame.width).roundToInt().coerceIn(0, frame.width - 1)
        val top = (box.top * frame.height).roundToInt().coerceIn(0, frame.height - 1)
        val right = (box.right * frame.width).roundToInt().coerceIn(left + 1, frame.width)
        val bottom = (box.bottom * frame.height).roundToInt().coerceIn(top + 1, frame.height)
        val spanW = right - left
        val spanH = bottom - top
        if (spanW < MIN_TEMPLATE_EDGE || spanH < MIN_TEMPLATE_EDGE) return null

        val gridW = min(spanW, MAX_TEMPLATE_EDGE)
        val gridH = min(spanH, MAX_TEMPLATE_EDGE)
        val pixels = IntArray(gridW * gridH)
        for (gy in 0 until gridH) {
            val srcY = top + gy * spanH / gridH
            for (gx in 0 until gridW) {
                val srcX = left + gx * spanW / gridW
                pixels[gy * gridW + gx] = frame.luma[srcY * frame.width + srcX]
            }
        }
        return Patch(pixels, gridW, gridH, spanW, spanH, left, top)
    }

    /**
     * Normalized cross-correlation between the stored template and the frame content at
     * top-left (x, y), sampling the frame on the template's grid-to-span stride so shapes align.
     */
    private fun ncc(state: State, frame: LumaFrame, x: Int, y: Int): Float {
        val template = state.template
        val gridW = state.gridW
        val gridH = state.gridH
        val n = gridW * gridH

        var sumT = 0L
        var sumF = 0L
        for (i in 0 until n) sumT += template[i]
        for (gy in 0 until gridH) {
            val frameRow = (y + gy * state.spanH / gridH) * frame.width
            for (gx in 0 until gridW) {
                sumF += frame.luma[frameRow + x + gx * state.spanW / gridW]
            }
        }
        val meanT = sumT.toDouble() / n
        val meanF = sumF.toDouble() / n

        var cross = 0.0
        var varT = 0.0
        var varF = 0.0
        for (gy in 0 until gridH) {
            val frameRow = (y + gy * state.spanH / gridH) * frame.width
            val templateRow = gy * gridW
            for (gx in 0 until gridW) {
                val dt = template[templateRow + gx] - meanT
                val df = frame.luma[frameRow + x + gx * state.spanW / gridW] - meanF
                cross += dt * df
                varT += dt * dt
                varF += df * df
            }
        }
        if (varT == 0.0 || varF == 0.0) return 0f
        return (cross / sqrt(varT * varF)).toFloat()
    }

    private companion object {
        const val MAX_TEMPLATE_EDGE = 32
        const val MIN_TEMPLATE_EDGE = 4
        const val MIN_SEARCH_RADIUS = 6
    }
}
