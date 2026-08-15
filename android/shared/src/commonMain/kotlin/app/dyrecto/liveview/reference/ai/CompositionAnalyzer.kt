package app.dyrecto.liveview.reference.ai

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.snapshot.SceneSubject
import kotlin.math.abs

/**
 * Object-layout composition signature: a 3×3 grid where each cell holds the confidence-weighted
 * fraction of the cell covered by subject boxes. Two signatures are compared as a mean L1
 * difference — pure geometry, no pixels, so it is resolution- and model-independent.
 *
 * Empty-scene signatures are all zeros; the comparator treats composition as unavailable when
 * either side has no objects (visual similarity covers no-object scenes instead).
 */
object CompositionAnalyzer {
    const val GRID = 3
    const val CELLS = GRID * GRID

    fun gridSignature(subjects: List<SceneSubject>): FloatArray {
        val signature = FloatArray(CELLS)
        if (subjects.isEmpty()) return signature
        val cellSize = 1f / GRID
        for (row in 0 until GRID) {
            for (col in 0 until GRID) {
                val cell = NormalizedRect(
                    left = col * cellSize,
                    top = row * cellSize,
                    right = (col + 1) * cellSize,
                    bottom = (row + 1) * cellSize,
                )
                var occupancy = 0f
                for (subject in subjects) {
                    occupancy += intersectionArea(cell, subject.boundingBox) * subject.confidence
                }
                // Normalize by cell area so a fully covered cell ≈ 1.0 at confidence 1.0.
                signature[row * GRID + col] = (occupancy / cell.area).coerceAtMost(1f)
            }
        }
        return signature
    }

    /** Mean per-cell L1 difference in [0, 1]. */
    fun delta(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size || a.isEmpty()) return 1f
        var sum = 0f
        for (i in a.indices) sum += abs(a[i] - b[i])
        return sum / a.size
    }

    private fun intersectionArea(a: NormalizedRect, b: NormalizedRect): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        if (right <= left || bottom <= top) return 0f
        return (right - left) * (bottom - top)
    }
}
