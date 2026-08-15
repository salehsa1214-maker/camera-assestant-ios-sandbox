package app.dyrecto.liveview.exposure

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-scoped exposure-tool configuration (Phase 6). Currently holds the active zebra threshold
 * selected from the Developer screen. Read by [app.dyrecto.liveview.vision.modules.ExposureModule]
 * on the Vision worker each frame (a [StateFlow] value read is thread-safe) and mutated from the UI.
 *
 * In-memory only, deliberately not persisted (mirrors
 * [app.dyrecto.debug.DeveloperState]) — this is a diagnostics setting, reset on restart.
 */
object ExposureConfig {

    /** Zebra level presets offered in the UI (IRE). Custom values are set via [setZebraLevel]. */
    val PRESET_LEVELS: List<Int> = listOf(70, 95, 100)

    private val _zebraSpec = MutableStateFlow<ZebraSpec>(ZebraSpec.PRESET_95)
    val zebraSpec: StateFlow<ZebraSpec> = _zebraSpec.asStateFlow()

    fun setZebraSpec(spec: ZebraSpec) {
        _zebraSpec.value = spec
    }

    /** Convenience for the level presets and the custom field: clamps to the Sony 0..109 IRE range. */
    fun setZebraLevel(ire: Int) {
        _zebraSpec.value = ZebraSpec.Level(ire.coerceIn(0, 109))
    }

    // --- Phase 8: Analysis Color Transform ------------------------------------------------------

    private val _analysisColorSpace = MutableStateFlow(AnalysisColorSpace.S_LOG3)
    val analysisColorSpace: StateFlow<AnalysisColorSpace> = _analysisColorSpace.asStateFlow()

    fun setAnalysisColorSpace(space: AnalysisColorSpace) {
        _analysisColorSpace.value = space
    }

    /**
     * The S-Log3 transform, registered once at app startup after the Sony monitoring LUT asset is
     * loaded and parsed (see `DyrectoApp`). `null` until registration completes or if the
     * asset failed to load — [resolveTransform] falls back to [Rec709Transform] in that case, so a
     * missing/corrupt LUT never crashes the app or blocks Rec709 analysis.
     */
    private var sLog3Transform: AnalysisColorTransform? = null

    /** Name of the loaded LUT, for Developer-screen diagnostics. Null until [registerSLog3Transform]. */
    var sLog3LutName: String? = null
        private set

    fun registerSLog3Transform(transform: AnalysisColorTransform, lutName: String) {
        sLog3Transform = transform
        sLog3LutName = lutName
    }

    /** Resolves the current [analysisColorSpace] selection to the transform [ExposureModule] uses. */
    fun resolveTransform(): AnalysisColorTransform = when (_analysisColorSpace.value) {
        AnalysisColorSpace.AUTO, AnalysisColorSpace.REC709 -> Rec709Transform
        AnalysisColorSpace.S_LOG3 -> sLog3Transform ?: Rec709Transform
    }
}
