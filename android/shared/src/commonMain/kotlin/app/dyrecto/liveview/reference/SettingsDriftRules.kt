package app.dyrecto.liveview.reference

import app.dyrecto.domain.alerts.AlertType

/** One camera-setting that no longer matches the reference shot's captured value. */
data class SettingDrift(
    val label: String,
    val referenceValue: String,
    val liveValue: String,
    val alertType: AlertType,
)

/**
 * Pure comparison of the live camera settings against a reference shot's captured settings. Only
 * fields the reference actually captured are checked; a live value that differs produces a
 * [SettingDrift] with the appropriate [AlertType]. Camera-authoritative (telemetry vs telemetry),
 * independent of the pixel-based exposure/WB drift signals.
 */
object SettingsDriftRules {
    fun evaluate(reference: ReferenceCameraSettings, live: ReferenceCameraSettings): List<SettingDrift> {
        val out = ArrayList<SettingDrift>()
        fun check(label: String, ref: String?, cur: String?, type: AlertType) {
            if (ref != null && cur != null && ref != cur) {
                out.add(SettingDrift(label, ref, cur, type))
            }
        }
        check("ISO", reference.iso, live.iso, AlertType.SETTINGS_DRIFT)
        check("Shutter", reference.shutter, live.shutter, AlertType.SETTINGS_DRIFT)
        check("Aperture", reference.aperture, live.aperture, AlertType.SETTINGS_DRIFT)
        check("White Balance", reference.whiteBalance, live.whiteBalance, AlertType.SETTINGS_DRIFT)
        check("Color Temperature", reference.colorTemp, live.colorTemp, AlertType.SETTINGS_DRIFT)
        check("Codec", reference.codec, live.codec, AlertType.SETTINGS_DRIFT)
        check("Picture Profile", reference.pictureProfile, live.pictureProfile, AlertType.PICTURE_PROFILE_CHANGED)
        check("Frame Rate", reference.frameRate, live.frameRate, AlertType.FRAME_RATE_MISMATCH)
        return out
    }
}
