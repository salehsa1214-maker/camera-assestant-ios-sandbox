package app.dyrecto.liveview.reference

import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.TelemetryProp
import app.dyrecto.domain.alerts.AlertType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pins reference-linked camera-settings capture, summary formatting, and drift detection. */
class ReferenceCameraSettingsTest {

    private fun telemetry(vararg entries: Pair<Int, String>): CameraTelemetry {
        val props = entries.associate { (code, value) ->
            code to TelemetryProp(code = code, label = "", rawValue = value, dataType = 0x0004)
        }
        return CameraTelemetry(props)
    }

    @Test
    fun capturesAndSummarizesSettings() {
        // rawValue is used directly when the decoder has no rule (dataType/value here are synthetic).
        val s = ReferenceCameraSettings(
            iso = "800", shutter = "1/50", aperture = "2.8",
            colorTemp = "4300K", pictureProfile = "S-Log3", frameRate = "25p",
        )
        assertEquals("ISO 800 · 1/50 · f/2.8 · 4300K · S-Log3 · 25p", s.summary())
        assertTrue(!s.isEmpty)
        assertTrue(ReferenceCameraSettings().isEmpty)
    }

    @Test
    fun fromTelemetryReadsTrackedCodes() {
        val t = telemetry(
            CameraTelemetry.ISO to "1600",
            CameraTelemetry.FNUMBER to "4.0",
        )
        val s = ReferenceCameraSettings.fromTelemetry(t)
        assertEquals("1600", s.iso)
        assertEquals("4.0", s.aperture)
        assertEquals(null, s.shutter)
    }

    @Test
    fun driftDetectsChangedSettingsWithCorrectTypes() {
        val reference = ReferenceCameraSettings(
            iso = "800", pictureProfile = "S-Log3", frameRate = "25p", whiteBalance = "Daylight",
        )
        val live = ReferenceCameraSettings(
            iso = "3200", pictureProfile = "S-Cinetone", frameRate = "25p", whiteBalance = "Daylight",
        )
        val drifts = SettingsDriftRules.evaluate(reference, live)
        assertEquals(2, drifts.size)
        assertEquals(AlertType.SETTINGS_DRIFT, drifts.first { it.label == "ISO" }.alertType)
        assertEquals(AlertType.PICTURE_PROFILE_CHANGED, drifts.first { it.label == "Picture Profile" }.alertType)
        // unchanged frame rate + WB produce no drift
        assertTrue(drifts.none { it.label == "Frame Rate" || it.label == "White Balance" })
    }

    @Test
    fun noDriftWhenReferenceFieldMissing() {
        val reference = ReferenceCameraSettings(iso = null)
        val live = ReferenceCameraSettings(iso = "3200")
        assertTrue(SettingsDriftRules.evaluate(reference, live).isEmpty())
    }
}
