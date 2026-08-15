package app.dyrecto.liveview.exposure

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-scoped on/off state for the phone-side monitoring overlays (waveform, false color, focus
 * peaking), mirroring [ExposureConfig]. Read by the Vision worker's ExposureModule each frame (a
 * [StateFlow] value read is thread-safe) to decide which overlay engines to run — an overlay that is
 * off costs nothing — and observed by the live-view UI toggles.
 *
 * In-memory only, reset on restart (a live-view display preference, like the zebra threshold).
 */
object OverlayConfig {

    private val _waveform = MutableStateFlow(false)
    val waveform: StateFlow<Boolean> = _waveform.asStateFlow()

    private val _falseColor = MutableStateFlow(false)
    val falseColor: StateFlow<Boolean> = _falseColor.asStateFlow()

    private val _focusPeaking = MutableStateFlow(false)
    val focusPeaking: StateFlow<Boolean> = _focusPeaking.asStateFlow()

    fun setWaveform(on: Boolean) { _waveform.value = on }
    fun setFalseColor(on: Boolean) { _falseColor.value = on }
    fun setFocusPeaking(on: Boolean) { _focusPeaking.value = on }

    /** True when any overlay is enabled — lets the module skip all overlay work when all are off. */
    fun anyEnabled(): Boolean = _waveform.value || _falseColor.value || _focusPeaking.value
}
