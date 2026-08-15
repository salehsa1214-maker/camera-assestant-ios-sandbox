package app.dyrecto.liveview

import android.content.Context
import app.dyrecto.connection.BleLog
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.Lut3D
import app.dyrecto.liveview.exposure.Lut3DColorTransform

/**
 * Phase 8: the sole Android-aware piece of the Analysis Color Transform feature. Reads the Sony
 * S-Log3 monitoring LUT from assets and registers it with [ExposureConfig]. Everything downstream
 * ([Lut3D], [Lut3DColorTransform], `LuminanceAnalyzer`) stays pure Kotlin/JVM-testable — this class
 * exists only to bridge `AssetManager` (Android) to that pure layer, matching the platform-agnostic
 * boundary the rest of `liveview/exposure` follows.
 *
 * Failure (missing/corrupt asset) is caught and logged, never thrown — [ExposureConfig.resolveTransform]
 * safely falls back to Rec709 when no S-Log3 transform was registered.
 */
object AnalysisLutBootstrap {

    private const val ASSET_PATH = "luts/1_SGamut3CineSLog3_To_LC-709.cube"
    private const val LUT_NAME = "SLog3SGamut3.CineToLC_709"

    fun installSLog3Lut(context: Context) {
        runCatching {
            val text = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
            val lut = Lut3D.parse(text)
            ExposureConfig.registerSLog3Transform(Lut3DColorTransform(lut), LUT_NAME)
        }.onFailure {
            BleLog.error("Phase 8: failed to load S-Log3 monitoring LUT: ${it.message}")
        }
    }
}
