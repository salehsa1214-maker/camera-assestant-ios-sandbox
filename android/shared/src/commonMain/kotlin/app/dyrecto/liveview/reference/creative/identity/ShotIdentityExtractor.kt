package app.dyrecto.liveview.reference.creative.identity

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceColorProfile
import app.dyrecto.liveview.reference.ReferenceExposureProfile
import app.dyrecto.liveview.reference.creative.experts.CreativeAnalysisContext
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Phase 16.2 — turns the already-computed reference signals into the quantitative [ShotIdentity].
 *
 * Pure, deterministic, JVM-testable. It reads only what the analyzer already has ([CreativeAnalysisContext]
 * — nothing new is plumbed) and keeps each measurement CONTINUOUS: a subject at x=0.60 and one at x=0.78
 * produce different `SUBJECT_FRAME_POSITION` traits, where the old categorical model collapsed both to
 * `RIGHT_THIRD`. `distinctiveness` is the deviation of each value from a neutral anchor (see
 * [ShotIdentityThresholds]); traits below [ShotIdentityThresholds.traitFloor] are dropped — a shot that
 * is unremarkable on an axis simply has no trait there.
 *
 * The semantic marker ([ShotTraitDimension.SEMANTIC_DISTINCTION]) is NOT produced here — it comes from the
 * `SemanticExpert` (Phase 16.2 Step F), gated on semantic evidence; the orchestrator concatenates both.
 */
object ShotIdentityExtractor {

    fun extract(
        ctx: CreativeAnalysisContext,
        t: ShotIdentityThresholds = ShotIdentityThresholds.DEFAULT,
    ): ShotIdentity {
        val traits = ArrayList<ShotTrait>(12)
        val box = ctx.subjectBox
        val exposure = ctx.exposure
        val color = ctx.color
        val grid = ctx.ai?.compositionSignature?.takeIf { it.size == CompositionCells }
        val coverage = ctx.ai?.segmentationCoverage
        val pixelAccurate = ctx.ai?.segmentationPixelAccurate == true
        val subjectConf = ctx.ai?.primarySubjectConfidence?.takeIf { it > 0f }
            ?: if (ctx.boxIsFace) 0.6f else if (box != null) 0.5f else 0f

        if (box != null) {
            traits += positionTrait(box, subjectConf, t)
            traits += scaleTrait(box, subjectConf, t)
            edgeTrait(box, subjectConf, t)?.let(traits::add)
        }
        negativeSpaceVolumeTrait(box, coverage, t)?.let(traits::add)
        negativeSpaceDirectionTrait(box, coverage, subjectConf, t)?.let(traits::add)

        separationTrait(exposure, t)?.let(traits::add)
        traits += tonalKeyTrait(exposure, t)
        traits += contrastTrait(exposure, t)

        if (grid != null) {
            balanceTrait(grid, t)?.let(traits::add)
            traits += densityTrait(grid, t)
        }

        traits += warmthTrait(color, t)
        traits += castTrait(color, t)
        isolationTrait(box, coverage, pixelAccurate, t)?.let(traits::add)

        val kept = traits
            .filter { it.distinctiveness >= t.traitFloor && it.confidence > 0f }
            .sortedByDescending { it.distinctiveness }
        return ShotIdentity(kept)
    }

    // ---- geometry ---------------------------------------------------------------------------------

    private fun positionTrait(box: NormalizedRect, conf: Float, t: ShotIdentityThresholds): ShotTrait {
        val dx = box.centerX - t.positionAnchorX
        val d = ratio(abs(dx), t.positionSaturation)
        val dir = when {
            dx > 0.03f -> TraitDirection.RIGHT
            dx < -0.03f -> TraitDirection.LEFT
            else -> TraitDirection.NONE
        }
        val strong = d >= t.strongDistinctiveness
        val descriptor = when (dir) {
            TraitDirection.RIGHT -> if (strong) "subject_far_right" else "subject_right_of_center"
            TraitDirection.LEFT -> if (strong) "subject_far_left" else "subject_left_of_center"
            else -> "subject_centered"
        }
        return ShotTrait(
            dimension = ShotTraitDimension.SUBJECT_FRAME_POSITION,
            value = box.centerX,
            distinctiveness = d,
            descriptor = descriptor,
            direction = dir,
            confidence = conf,
            reason = "subject at ${pct(box.centerX)}% width",
        )
    }

    private fun scaleTrait(box: NormalizedRect, conf: Float, t: ShotIdentityThresholds): ShotTrait {
        val area = box.area
        val d = ratio(abs(area - t.scaleAnchor), t.scaleSaturation)
        val big = area >= t.scaleAnchor
        val strong = d >= t.strongDistinctiveness
        val descriptor = when {
            big && strong -> "subject_dominates_frame"
            big -> "subject_large_in_frame"
            !big && strong -> "subject_tiny_in_frame"
            else -> "subject_small_in_frame"
        }
        return ShotTrait(
            dimension = ShotTraitDimension.SUBJECT_SCALE,
            value = area,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = conf,
            reason = "subject fills ${pct(area)}% of frame",
        )
    }

    private fun edgeTrait(box: NormalizedRect, conf: Float, t: ShotIdentityThresholds): ShotTrait? {
        val gaps = listOf(
            box.left to TraitDirection.LEFT,
            (1f - box.right) to TraitDirection.RIGHT,
            box.top to TraitDirection.UP,
            (1f - box.bottom) to TraitDirection.DOWN,
        )
        val (gap, dir) = gaps.minByOrNull { it.first } ?: return null
        if (gap >= t.edgeTensionGap) return null
        val d = ((t.edgeTensionGap - gap) / t.edgeTensionGap).coerceIn(0f, 1f)
        return ShotTrait(
            dimension = ShotTraitDimension.EDGE_ANCHORING,
            value = gap,
            distinctiveness = d,
            descriptor = "subject_pinned_${dir.name.lowercase()}",
            direction = dir,
            confidence = conf,
            reason = "subject ${pct(gap)}% from the ${dir.name.lowercase()} edge",
        )
    }

    // ---- negative space ---------------------------------------------------------------------------

    private fun emptiness(box: NormalizedRect?, coverage: Float?): Float? =
        coverage?.let { (1f - it).coerceIn(0f, 1f) } ?: box?.let { (1f - it.area).coerceIn(0f, 1f) }

    private fun negativeSpaceVolumeTrait(
        box: NormalizedRect?,
        coverage: Float?,
        t: ShotIdentityThresholds,
    ): ShotTrait? {
        val empty = emptiness(box, coverage) ?: return null
        val d = ratio(abs(empty - t.negSpaceAnchor), t.negSpaceSaturation)
        val vast = empty >= t.negSpaceAnchor
        val strong = d >= t.strongDistinctiveness
        val descriptor = when {
            vast && strong -> "vast_negative_space"
            vast -> "generous_negative_space"
            !vast && strong -> "frame_filling_subject"
            else -> "tight_negative_space"
        }
        return ShotTrait(
            dimension = ShotTraitDimension.NEGATIVE_SPACE_VOLUME,
            value = empty,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = if (coverage != null) 0.7f else 0.55f,
            reason = "${pct(empty)}% of the frame is empty space",
        )
    }

    private fun negativeSpaceDirectionTrait(
        box: NormalizedRect?,
        coverage: Float?,
        conf: Float,
        t: ShotIdentityThresholds,
    ): ShotTrait? {
        if (box == null) return null
        val empty = emptiness(box, coverage) ?: return null
        // Only meaningful when there IS space and the subject is off-center — the empty side is opposite.
        if (empty < t.negSpaceAnchor) return null
        val dx = box.centerX - 0.5f
        val dy = box.centerY - 0.5f
        val horizontal = abs(dx) >= abs(dy)
        val offset = if (horizontal) abs(dx) else abs(dy)
        val dir = when {
            horizontal && dx > 0.03f -> TraitDirection.LEFT // subject right ⇒ space on the left
            horizontal && dx < -0.03f -> TraitDirection.RIGHT
            !horizontal && dy > 0.03f -> TraitDirection.UP // subject low ⇒ space above
            !horizontal && dy < -0.03f -> TraitDirection.DOWN
            else -> return null
        }
        val d = (ratio(offset, t.positionSaturation) * empty.coerceIn(0f, 1f)).coerceIn(0f, 1f)
        return ShotTrait(
            dimension = ShotTraitDimension.NEGATIVE_SPACE_DIRECTION,
            value = offset,
            distinctiveness = d,
            descriptor = "negative_space_${dir.name.lowercase()}",
            direction = dir,
            confidence = conf * 0.9f,
            reason = "the empty space sits to the ${dir.name.lowercase()}",
        )
    }

    // ---- lighting / tone --------------------------------------------------------------------------

    private fun separationTrait(exposure: ReferenceExposureProfile, t: ShotIdentityThresholds): ShotTrait? {
        val subj = exposure.subjectExposure ?: return null
        val sep = subj.mean - exposure.mean // signed; negative = subject darker than the frame
        val d = ratio(abs(sep), t.separationSaturation)
        val strong = d >= t.strongDistinctiveness
        val descriptor = when {
            sep <= -1f && strong -> "subject_well_under_background"
            sep <= -1f -> "subject_under_background"
            sep >= 1f && strong -> "subject_well_over_background"
            sep >= 1f -> "subject_over_background"
            else -> "subject_even_with_background"
        }
        return ShotTrait(
            dimension = ShotTraitDimension.SUBJECT_BACKGROUND_SEPARATION,
            value = sep,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.75f,
            reason = "subject ${sep.roundToInt()} luma vs the frame",
        )
    }

    private fun tonalKeyTrait(exposure: ReferenceExposureProfile, t: ShotIdentityThresholds): ShotTrait {
        val dev = exposure.mean - t.tonalKeyAnchor
        val d = ratio(abs(dev), t.tonalKeySaturation)
        val descriptor = if (dev < 0f) "dark_frame" else "bright_frame"
        return ShotTrait(
            dimension = ShotTraitDimension.TONAL_KEY,
            value = exposure.mean,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.8f,
            reason = "mean luma ${exposure.mean.roundToInt()}",
        )
    }

    private fun contrastTrait(exposure: ReferenceExposureProfile, t: ShotIdentityThresholds): ShotTrait {
        val spread = exposure.p95 - exposure.median
        val dev = spread - t.contrastAnchor
        val d = ratio(abs(dev), t.contrastSaturation)
        val descriptor = if (dev >= 0f) "high_contrast" else "flat_contrast"
        return ShotTrait(
            dimension = ShotTraitDimension.DOMINANT_CONTRAST,
            value = spread,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.7f,
            reason = "tonal spread ${spread.roundToInt()} luma",
        )
    }

    // ---- grid layout ------------------------------------------------------------------------------

    private fun balanceTrait(grid: List<Float>, t: ShotIdentityThresholds): ShotTrait? {
        var sum = 0f
        var comX = 0f
        var comY = 0f
        for (i in grid.indices) {
            val w = grid[i]
            if (w <= 0f) continue
            val col = i % CompositionGrid
            val row = i / CompositionGrid
            comX += w * ((col + 0.5f) / CompositionGrid)
            comY += w * ((row + 0.5f) / CompositionGrid)
            sum += w
        }
        if (sum <= 0f) return null
        val offX = comX / sum - 0.5f
        val offY = comY / sum - 0.5f
        val mag = hypot(offX.toDouble(), offY.toDouble()).toFloat()
        val d = ratio(mag, t.balanceSaturation)
        val horizontal = abs(offX) >= abs(offY)
        val dir = when {
            horizontal && offX > 0f -> TraitDirection.RIGHT
            horizontal -> TraitDirection.LEFT
            offY > 0f -> TraitDirection.DOWN
            else -> TraitDirection.UP
        }
        return ShotTrait(
            dimension = ShotTraitDimension.VISUAL_BALANCE,
            value = mag,
            distinctiveness = d,
            descriptor = "weight_${dir.name.lowercase()}",
            direction = dir,
            confidence = 0.6f,
            reason = "visual weight leans ${dir.name.lowercase()}",
        )
    }

    private fun densityTrait(grid: List<Float>, t: ShotIdentityThresholds): ShotTrait {
        val mean = grid.sum() / grid.size
        val dev = mean - t.densityAnchor
        val d = ratio(abs(dev), t.densitySaturation)
        val descriptor = if (dev >= 0f) "dense_frame" else "sparse_frame"
        return ShotTrait(
            dimension = ShotTraitDimension.FRAME_DENSITY,
            value = mean,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.55f,
            reason = "${pct(mean)}% average frame occupancy",
        )
    }

    // ---- color ------------------------------------------------------------------------------------

    private fun warmthTrait(color: ReferenceColorProfile, t: ShotIdentityThresholds): ShotTrait {
        val mag = abs(color.warmthScore)
        val d = ratio(mag, t.warmthSaturation)
        val descriptor = if (color.warmthScore >= 0f) "warm_palette" else "cool_palette"
        return ShotTrait(
            dimension = ShotTraitDimension.COLOR_WARMTH_MAGNITUDE,
            value = color.warmthScore,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.6f,
            reason = "warmth ${color.warmthScore.roundToInt()}",
        )
    }

    private fun castTrait(color: ReferenceColorProfile, t: ShotIdentityThresholds): ShotTrait {
        val mag = abs(color.tintScore)
        val d = ratio(mag, t.castSaturation)
        val descriptor = if (color.tintScore >= 0f) "green_cast" else "magenta_cast"
        return ShotTrait(
            dimension = ShotTraitDimension.COLOR_CAST_STRENGTH,
            value = color.tintScore,
            distinctiveness = d,
            descriptor = descriptor,
            confidence = 0.5f,
            reason = "tint ${color.tintScore.roundToInt()}",
        )
    }

    // ---- isolation --------------------------------------------------------------------------------

    private fun isolationTrait(
        box: NormalizedRect?,
        coverage: Float?,
        pixelAccurate: Boolean,
        t: ShotIdentityThresholds,
    ): ShotTrait? {
        // A cleanly-separable subject occupying little of a real (pixel-accurate) mask reads as isolated.
        val cov = coverage ?: box?.area ?: return null
        val d = ((0.5f - cov) / 0.5f).coerceIn(0f, 1f)
        return ShotTrait(
            dimension = ShotTraitDimension.SUBJECT_ISOLATION,
            value = cov,
            distinctiveness = d,
            descriptor = if (d >= t.strongDistinctiveness) "strongly_isolated_subject" else "isolated_subject",
            confidence = if (pixelAccurate) 0.7f else 0.5f,
            reason = "subject covers ${pct(cov)}% of the frame",
        )
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private const val CompositionGrid = 3
    private const val CompositionCells = CompositionGrid * CompositionGrid

    /** Normalized deviation → [0,1]; guards a zero/negative saturation. */
    private fun ratio(magnitude: Float, saturation: Float): Float =
        if (saturation <= 0f) 0f else (magnitude / saturation).coerceIn(0f, 1f)

    private fun pct(v: Float): Int = (v.coerceIn(0f, 1f) * 100f).roundToInt()
}
