package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.exposure.SubjectExposureStats
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.creative.CreativeLighting
import app.dyrecto.liveview.reference.creative.LightingContrast
import app.dyrecto.liveview.reference.creative.LightingKey

/**
 * Phase 16.1 — lighting key / contrast / backlight hint from the exposure profile. The pre-16.1
 * `deriveLighting` moved verbatim; output is byte-identical to Phase 16.
 */
object LightingExpert : SceneExpert {
    override val name = "lighting"

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution =
        CreativeContribution(lighting = deriveLighting(ctx.exposure))

    private fun deriveLighting(exposure: ReferenceExposureProfile): CreativeLighting {
        val key = when {
            exposure.mean < 85f -> LightingKey.LOW_KEY
            exposure.mean > 175f -> LightingKey.HIGH_KEY
            else -> LightingKey.BALANCED
        }
        val dynamicRange = exposure.p95 - exposure.median
        val bothEndsClipped = exposure.shadowCoverage > 3f && exposure.highlightCoverage > 3f
        val contrast = when {
            dynamicRange > 90f || bothEndsClipped -> LightingContrast.HIGH
            dynamicRange < 40f -> LightingContrast.LOW
            else -> LightingContrast.MEDIUM
        }
        val backlight = exposure.subjectExposure?.let { subjectDarkerThanFrame(it, exposure) }
        return CreativeLighting(
            key = key,
            contrast = contrast,
            confidence = 0.8f,
            backlightHint = backlight,
        )
    }

    private fun subjectDarkerThanFrame(
        subject: SubjectExposureStats,
        frame: ReferenceExposureProfile,
    ): Boolean = subject.mean < frame.mean - 20f && frame.highlightCoverage > 3f
}
