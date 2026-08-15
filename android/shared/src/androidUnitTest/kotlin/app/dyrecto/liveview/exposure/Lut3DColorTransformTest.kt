package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure-JVM tests for [Lut3DColorTransform]. */
class Lut3DColorTransformTest {

    // Identity 2x2x2 cube: output RGB == input RGB at every grid point.
    private fun identityLut() = Lut3D.parse(
        """
        LUT_3D_SIZE 2
        0.0 0.0 0.0
        1.0 0.0 0.0
        0.0 1.0 0.0
        1.0 1.0 0.0
        0.0 0.0 1.0
        1.0 0.0 1.0
        0.0 1.0 1.0
        1.0 1.0 1.0
        """.trimIndent(),
    )

    @Test fun identityLutMatchesRec709() {
        val transform = Lut3DColorTransform(identityLut())
        assertEquals(255, transform.toLuma(255, 255, 255))
        assertEquals(0, transform.toLuma(0, 0, 0))
        assertEquals(Rec709Transform.toLuma(128, 64, 32), transform.toLuma(128, 64, 32))
    }

    // Cube that maps everything to pure white — proves the LUT's output, not the input, drives luma.
    private fun allWhiteLut() = Lut3D.parse(
        """
        LUT_3D_SIZE 2
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        1.0 1.0 1.0
        """.trimIndent(),
    )

    @Test fun outputComesFromLutNotRawInput() {
        val transform = Lut3DColorTransform(allWhiteLut())
        assertEquals(255, transform.toLuma(0, 0, 0))
    }

    @Test fun outputStaysClampedTo0_255() {
        val transform = Lut3DColorTransform(identityLut())
        val luma = transform.toLuma(0, 0, 0)
        assertTrue(luma in 0..255)
    }
}
