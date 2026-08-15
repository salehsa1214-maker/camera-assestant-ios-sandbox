package app.dyrecto.liveview.perception

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerceptualDifferenceTest {

    // ---- normalize: dead zone + saturation ----

    @Test
    fun `inside dead zone is exactly zero`() {
        assertEquals(0f, PerceptualDifference.normalize(raw = 5f, deadZone = 6f, saturation = 30f))
        assertEquals(0f, PerceptualDifference.normalize(raw = -6f, deadZone = 6f, saturation = 30f))
        assertEquals(0f, PerceptualDifference.normalize(raw = 0f, deadZone = 0f, saturation = 1f))
    }

    @Test
    fun `at and beyond saturation clamps to one`() {
        assertEquals(1f, PerceptualDifference.normalize(raw = 30f, deadZone = 6f, saturation = 30f))
        assertEquals(1f, PerceptualDifference.normalize(raw = -99f, deadZone = 6f, saturation = 30f))
    }

    @Test
    fun `linear between dead zone and saturation and sign-agnostic`() {
        val mid = PerceptualDifference.normalize(raw = 18f, deadZone = 6f, saturation = 30f)
        assertEquals(0.5f, mid, 1e-6f)
        assertEquals(mid, PerceptualDifference.normalize(raw = -18f, deadZone = 6f, saturation = 30f), 1e-6f)
    }

    @Test
    fun `degenerate band (saturation at or below dead zone) jumps to one outside the dead zone`() {
        assertEquals(0f, PerceptualDifference.normalize(raw = 4f, deadZone = 5f, saturation = 5f))
        assertEquals(1f, PerceptualDifference.normalize(raw = 6f, deadZone = 5f, saturation = 5f))
    }

    // ---- curves ----

    @Test
    fun `smoothstep hits endpoints and is monotonic`() {
        val curve = CurveSpec(CurveSpec.CurveType.SMOOTHSTEP)
        assertEquals(0f, PerceptualDifference.applyCurve(0f, curve))
        assertEquals(1f, PerceptualDifference.applyCurve(1f, curve))
        var last = 0f
        for (i in 0..100) {
            val v = PerceptualDifference.applyCurve(i / 100f, curve)
            assertTrue("smoothstep must be monotonic", v >= last)
            last = v
        }
        // Flat near the JND: the first 10% of input produces well under 10% output.
        assertTrue(PerceptualDifference.applyCurve(0.1f, curve) < 0.05f)
    }

    @Test
    fun `power curve respects gamma`() {
        val gentle = CurveSpec(CurveSpec.CurveType.POWER, gamma = 1.4f)
        val steep = CurveSpec(CurveSpec.CurveType.POWER, gamma = 0.5f)
        val x = 0.4f
        assertTrue(PerceptualDifference.applyCurve(x, gentle) < x)
        assertTrue(PerceptualDifference.applyCurve(x, steep) > x)
        assertEquals(1f, PerceptualDifference.applyCurve(1f, gentle))
    }

    @Test
    fun `piecewise interpolates between breakpoints and clamps outside`() {
        val curve = CurveSpec(
            CurveSpec.CurveType.PIECEWISE,
            breakpoints = listOf(0.2f to 0f, 0.6f to 0.5f, 1f to 1f),
        )
        assertEquals(0f, PerceptualDifference.applyCurve(0.1f, curve))
        assertEquals(0.25f, PerceptualDifference.applyCurve(0.4f, curve), 1e-6f)
        assertEquals(1f, PerceptualDifference.applyCurve(1f, curve))
    }

    @Test
    fun `different curves diverge on identical normalized input`() {
        val p = 0.35f
        val exposure = PerceptualDifference.applyCurve(p, CurveSpec(CurveSpec.CurveType.SMOOTHSTEP))
        val warmth = PerceptualDifference.applyCurve(p, CurveSpec(CurveSpec.CurveType.POWER, gamma = 1.4f))
        val geometry = PerceptualDifference.applyCurve(p, CurveSpec(CurveSpec.CurveType.LINEAR))
        assertTrue(exposure != warmth && warmth != geometry && exposure != geometry)
    }

    // ---- weighted composite ----

    @Test
    fun `missing components are renormalized away`() {
        val withMissing = PerceptualDifference.weightedComposite(
            listOf(0.8f as Float? to 1f, null to 5f, 0.4f as Float? to 1f),
        )
        assertEquals(0.6f, withMissing, 1e-6f)
    }

    @Test
    fun `no available components yields zero`() {
        assertEquals(0f, PerceptualDifference.weightedComposite(listOf(null to 1f, null to 2f)))
        assertEquals(0f, PerceptualDifference.weightedComposite(emptyList()))
    }

    // ---- severity ----

    @Test
    fun `severity buckets follow configured cutoffs`() {
        val cutoffs = floatArrayOf(0.15f, 0.35f, 0.60f, 0.85f)
        assertEquals(PerceptualSeverity.NONE, PerceptualDifference.severityOf(0.10f, cutoffs))
        assertEquals(PerceptualSeverity.SUBTLE, PerceptualDifference.severityOf(0.20f, cutoffs))
        assertEquals(PerceptualSeverity.NOTICEABLE, PerceptualDifference.severityOf(0.50f, cutoffs))
        assertEquals(PerceptualSeverity.OBVIOUS, PerceptualDifference.severityOf(0.70f, cutoffs))
        assertEquals(PerceptualSeverity.SEVERE, PerceptualDifference.severityOf(0.90f, cutoffs))
    }

    // ---- importance ----

    @Test
    fun `importance scales with strategy weight and salience`() {
        val base = PerceptualDifference.importanceOf(
            noticeability = 0.6f, strategyWeight = 1f, salience = 1f, gamma = 1f,
        )
        assertEquals(0.6f, base, 1e-6f)
        val halfWeight = PerceptualDifference.importanceOf(0.6f, 0.5f, 1f, 1f)
        assertEquals(base / 2f, halfWeight, 1e-6f)
        val salient = PerceptualDifference.importanceOf(0.6f, 1f, 1.5f, 1f)
        assertTrue(salient > base)
        assertTrue(PerceptualDifference.importanceOf(1f, 1f, 2f, 1f) <= 1f)
    }

    // ---- confidence combine ----

    @Test
    fun `confidence combine is min-dominated but softened by the mean`() {
        val c = PerceptualConfidence.combine(0.9f, 0.3f, null)
        assertTrue(c > 0.3f)
        assertTrue(c < 0.6f)
        assertEquals(0f, PerceptualConfidence.combine(null, null))
        assertEquals(1f, PerceptualConfidence.combine(1f, 1f))
    }
}
