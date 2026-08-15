package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Pure-JVM tests for [Lut3D] parsing and trilinear sampling. */
class Lut3DTest {

    private fun synthetic2x2x2() = """
        #comment
        TITLE "test"
        LUT_3D_SIZE 2

        0.0 0.0 0.0
        1.0 0.0 0.0
        0.0 1.0 0.0
        1.0 1.0 0.0
        0.0 0.0 1.0
        1.0 0.0 1.0
        0.0 1.0 1.0
        1.0 1.0 1.0
    """.trimIndent()

    @Test fun parsesSizeAndIdentityGrid() {
        val lut = Lut3D.parse(synthetic2x2x2())
        assertEquals(2, lut.size)
        // This synthetic cube is the identity: sampling at each corner returns that corner.
        val corner = lut.sample(1f, 1f, 0f)
        assertEquals(1f, corner[0], 1e-6f)
        assertEquals(1f, corner[1], 1e-6f)
        assertEquals(0f, corner[2], 1e-6f)
    }

    @Test fun interpolatesMidpointLinearly() {
        val lut = Lut3D.parse(synthetic2x2x2())
        // Midpoint between (0,0,0)->black and (1,0,0)->red along R.
        val mid = lut.sample(0.5f, 0f, 0f)
        assertEquals(0.5f, mid[0], 1e-6f)
        assertEquals(0f, mid[1], 1e-6f)
        assertEquals(0f, mid[2], 1e-6f)
    }

    @Test fun rejectsMalformedInput() {
        assertThrows(IllegalArgumentException::class.java) {
            Lut3D.parse("LUT_3D_SIZE 2\n0.0 0.0\n")
        }
    }

    @Test fun rejectsMissingSize() {
        assertThrows(IllegalArgumentException::class.java) {
            Lut3D.parse("0.0 0.0 0.0\n")
        }
    }

    @Test fun parsesRealSonyLutHeader() {
        // Spot-check the first data row of the real Sony asset without needing Android AssetManager
        // (this is the pure-Kotlin parser; the app-layer loader in AnalysisLutBootstrap reads the
        // actual asset at runtime). Mirrors the header/first row documented in the Phase 8 plan.
        val text = """
            #Sony LookProfile LUT, SLog3SGamut3.CineToLC_709 full in full out v1.08.04

            TITLE 	SLog3SGamut3.CineToLC_709
            LUT_3D_SIZE 2

            0.006644 0.007144 0.000000
            0.015137 0.000000 0.000037
            0.021711 0.000000 0.000053
            0.031136 0.000000 0.000076
            0.044643 0.000000 0.000109
            0.063677 0.000000 0.000156
            0.092521 0.000000 0.000245
            0.124903 0.000000 0.000376
        """.trimIndent()
        val lut = Lut3D.parse(text)
        assertEquals(2, lut.size)
        val first = lut.sample(0f, 0f, 0f)
        assertEquals(0.006644f, first[0], 1e-6f)
        assertEquals(0.007144f, first[1], 1e-6f)
        assertEquals(0.000000f, first[2], 1e-6f)
    }
}
