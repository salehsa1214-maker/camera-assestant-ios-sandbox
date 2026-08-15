package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceDriftDirection
import app.dyrecto.liveview.reference.ReferenceSignal

/**
 * Human-facing drift wording for perceptually drifting signals (Phase 12). The severity adverb
 * makes alerts read like an experienced operator ("noticeably brighter"), not a measurement
 * instrument. Perception owns drift wording now — the comparator's raw messages are replaced
 * whenever a perceptual verdict exists.
 */
object PerceptualMessages {

    private fun adverb(severity: PerceptualSeverity): String = when (severity) {
        PerceptualSeverity.NONE, PerceptualSeverity.SUBTLE -> "slightly"
        PerceptualSeverity.NOTICEABLE -> "noticeably"
        PerceptualSeverity.OBVIOUS -> "clearly"
        PerceptualSeverity.SEVERE -> "severely"
    }

    fun message(
        signal: ReferenceSignal,
        direction: ReferenceDriftDirection,
        severity: PerceptualSeverity,
    ): String {
        val adv = adverb(severity)
        return when (direction) {
            ReferenceDriftDirection.BRIGHTER -> "Exposure is $adv brighter than the reference."
            ReferenceDriftDirection.DARKER -> "Exposure is $adv darker than the reference."
            ReferenceDriftDirection.WARMER -> "Image is $adv warmer than the reference."
            ReferenceDriftDirection.COOLER -> "Image is $adv cooler than the reference."
            ReferenceDriftDirection.LEFT -> "Subject moved $adv left from the reference."
            ReferenceDriftDirection.RIGHT -> "Subject moved $adv right from the reference."
            ReferenceDriftDirection.UP -> when (signal) {
                ReferenceSignal.HEADROOM -> "Headroom is $adv less than the reference."
                else -> "Subject moved $adv higher than the reference."
            }
            ReferenceDriftDirection.DOWN -> when (signal) {
                ReferenceSignal.HEADROOM -> "Headroom is $adv more than the reference."
                else -> "Subject moved $adv lower than the reference."
            }
            ReferenceDriftDirection.LARGER -> "Subject is $adv larger than the reference."
            ReferenceDriftDirection.SMALLER -> "Subject is $adv smaller than the reference."
            ReferenceDriftDirection.MISSING -> when (signal) {
                ReferenceSignal.FACE_PRESENCE -> "Face is not visible like the reference."
                ReferenceSignal.EYE_VISIBILITY -> "Eyes are not visible like the reference."
                else -> "Reference subject is missing."
            }
            ReferenceDriftDirection.DIFFERENT -> when (signal) {
                ReferenceSignal.COMPOSITION -> "Composition is $adv different from the reference."
                else -> "The scene looks $adv different from the reference."
            }
            ReferenceDriftDirection.UNKNOWN -> "Color tint is $adv different from the reference."
            ReferenceDriftDirection.NONE -> ""
        }
    }
}
