package app.dyrecto.liveview.instructions

import app.dyrecto.liveview.perception.PerceptualSeverity

/** Pure string assembly/display helpers for assistant instructions (Phase 13). */
object InstructionFormatter {

    /** Final operator sentence: template with the severity adverb substituted in. */
    fun message(action: AssistantAction, severity: PerceptualSeverity): String {
        val template = InstructionTemplates.template(action)
        return if ("%s" in template) {
            template.replace("%s", InstructionTemplates.adverb(severity))
        } else {
            template
        }
    }

    /** "0.83" → "83%" for the Shot Reference status rows. */
    fun progressPercent(instruction: AssistantInstruction): String =
        "${(instruction.progress.coerceIn(0f, 1f) * 100).toInt()}%"

    /** Coarse operator-facing confidence bucket — raw floats stay in the Developer screen. */
    fun confidenceLabel(instruction: AssistantInstruction): String = when {
        instruction.confidence >= 0.75f -> "High"
        instruction.confidence >= 0.45f -> "Medium"
        else -> "Low"
    }
}
