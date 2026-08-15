package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.reference.ReferenceSignalResult

/**
 * Progress toward matching the reference, reusing values the pipeline already publishes —
 * no new calculation (Phase 13 spec). The comparator's per-signal score is already
 * 1.0 = perfect match, 0.0 = far outside tolerance.
 */
object InstructionProgress {

    fun of(result: ReferenceSignalResult): Float = result.score.coerceIn(0f, 1f)
}
