package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.creative.ColorTemperature
import app.dyrecto.liveview.reference.creative.CreativeColor
import app.dyrecto.liveview.reference.creative.TintCast
import kotlin.math.abs

/**
 * Phase 16.1 — color temperature / tint from the color profile. The pre-16.1 `deriveColor` moved
 * verbatim; output is byte-identical to Phase 16.
 */
object ColorExpert : SceneExpert {
    override val name = "color"

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution =
        CreativeContribution(color = deriveColor(ctx.color))

    private fun deriveColor(color: ReferenceColorProfile): CreativeColor {
        val temperature = when {
            color.warmthScore > 12f -> ColorTemperature.WARM
            color.warmthScore < -12f -> ColorTemperature.COOL
            else -> ColorTemperature.NEUTRAL
        }
        val tint = when {
            color.tintScore > 8f -> TintCast.GREEN
            color.tintScore < -8f -> TintCast.MAGENTA
            else -> TintCast.NEUTRAL
        }
        // Stronger, clearly-intentional casts read as more confident inferences.
        val confidence = (0.4f + abs(color.warmthScore) / 60f).coerceIn(0.4f, 0.9f)
        return CreativeColor(temperature, tint, confidence)
    }
}
