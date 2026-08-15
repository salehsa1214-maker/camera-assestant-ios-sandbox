package app.dyrecto.liveview.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

/**
 * [ExposureConfig] is a process-wide singleton with one-way LUT registration (mirrors production:
 * the real LUT is registered once at app startup and never unregistered). Tests are ordered by name
 * ([MethodSorters.NAME_ASCENDING]) — the "before registration" assertions must run before
 * [registerSLog3TransformStoresItAndUpdatesName] mutates the singleton, since there's no reset hook
 * (matching production, where re-registering mid-session isn't a supported operation).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ExposureConfigTest {

    @Test fun `1_defaultsToSLog3AndFallsBackToRec709BeforeRegistration`() {
        // Default profile is S-Log3 (most footage this assistant monitors is Log); with no LUT
        // registered yet it must safely fall back to Rec709, never throw/crash.
        assertEquals(AnalysisColorSpace.S_LOG3, ExposureConfig.analysisColorSpace.value)
        assertNull(ExposureConfig.sLog3LutName)
        assertSame(Rec709Transform, ExposureConfig.resolveTransform())

        ExposureConfig.setAnalysisColorSpace(AnalysisColorSpace.S_LOG3)
        assertSame(Rec709Transform, ExposureConfig.resolveTransform())

        ExposureConfig.setAnalysisColorSpace(AnalysisColorSpace.REC709)
        assertSame(Rec709Transform, ExposureConfig.resolveTransform())
    }

    @Test fun `2_registerSLog3TransformStoresItAndUpdatesName`() {
        val identity = Lut3D.parse(
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
        val transform = Lut3DColorTransform(identity)
        ExposureConfig.registerSLog3Transform(transform, "test-lut")

        assertEquals("test-lut", ExposureConfig.sLog3LutName)

        ExposureConfig.setAnalysisColorSpace(AnalysisColorSpace.S_LOG3)
        assertSame(transform, ExposureConfig.resolveTransform())

        // AUTO/REC709 still resolve to Rec709Transform even after an S-Log3 transform is registered.
        ExposureConfig.setAnalysisColorSpace(AnalysisColorSpace.AUTO)
        assertSame(Rec709Transform, ExposureConfig.resolveTransform())
        ExposureConfig.setAnalysisColorSpace(AnalysisColorSpace.REC709)
        assertSame(Rec709Transform, ExposureConfig.resolveTransform())
    }
}
