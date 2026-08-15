package app.dyrecto.liveview.reference

import app.dyrecto.domain.CameraTelemetry
import kotlinx.serialization.Serializable

/**
 * The camera's exposure/format settings captured at the moment a reference shot is created — the
 * authoritative "this shot was ISO 800, 1/50, f/2.8, 4300K, S-Log3, 25p" record, read from live
 * camera telemetry rather than estimated from pixels. Attached to [ReferenceProfile]; legacy
 * profiles load with null and behave exactly as before.
 *
 * Values are stored as display strings (already decoded) so the summary is stable regardless of
 * later decoder changes. Every field is nullable — a setting the camera didn't report stays null.
 */
@Serializable
data class ReferenceCameraSettings(
    val iso: String? = null,
    val shutter: String? = null,
    val aperture: String? = null,
    val whiteBalance: String? = null,
    val colorTemp: String? = null,
    val frameRate: String? = null,
    val codec: String? = null,
    val pictureProfile: String? = null,
) {
    val isEmpty: Boolean
        get() = listOfNotNull(iso, shutter, aperture, whiteBalance, colorTemp, frameRate, codec, pictureProfile).isEmpty()

    /** e.g. "ISO 800 · 1/50 · f/2.8 · 4300K · S-Log3 · 25p". Empty settings → "". */
    fun summary(): String = listOfNotNull(
        iso?.let { "ISO $it" },
        shutter,
        aperture?.let { if (it.startsWith("f/")) it else "f/$it" },
        colorTemp ?: whiteBalance,
        pictureProfile,
        frameRate,
        codec,
    ).joinToString(" · ")

    companion object {
        /** Captures the tracked settings from a live telemetry snapshot (decoded display strings). */
        fun fromTelemetry(t: CameraTelemetry): ReferenceCameraSettings {
            fun value(code: Int): String? = t[code]?.let { it.decoded ?: it.rawValue }
            return ReferenceCameraSettings(
                iso = value(CameraTelemetry.ISO),
                shutter = value(CameraTelemetry.SHUTTER),
                aperture = value(CameraTelemetry.FNUMBER),
                whiteBalance = value(CameraTelemetry.WHITE_BALANCE),
                colorTemp = value(CameraTelemetry.COLOR_TEMP),
                frameRate = value(CameraTelemetry.REC_FPS),
                codec = value(CameraTelemetry.FILE_FORMAT_MOVIE),
                pictureProfile = value(CameraTelemetry.MONITOR_LUT),
            )
        }
    }
}
