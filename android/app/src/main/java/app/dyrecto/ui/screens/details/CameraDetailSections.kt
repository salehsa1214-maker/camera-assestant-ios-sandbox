package app.dyrecto.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.liveview.session.display
import app.dyrecto.ui.components.DashboardCard
import app.dyrecto.ui.components.InlineMetric
import app.dyrecto.ui.components.SectionHeader

/**
 * The six telemetry categories as independent, self-contained composables. They render the exact
 * property codes the old dashboard showed, but value-only (decoded → raw → "—"; no `Raw:`/`Code:`
 * — that detail stays in Developer). Each is standalone so it can be promoted to its own screen
 * later without a redesign. A section that has none of its properties present simply renders empty.
 */

/** A value-only telemetry row, rendered only when the camera reports that property. */
@Composable
private fun DetailItem(t: CameraTelemetry?, code: Int, label: String) {
    if (t?.get(code) == null) return
    InlineMetric(label, t.display(code))
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        SectionHeader(title)
        DashboardCard { Column { content() } }
    }
}

@Composable
fun ExposureSection(t: CameraTelemetry?) = Section("Exposure") {
    DetailItem(t, CameraTelemetry.ISO, "ISO")
    DetailItem(t, CameraTelemetry.FNUMBER, "Aperture / F-Number")
    DetailItem(t, CameraTelemetry.SHUTTER, "Shutter Speed")
    DetailItem(t, CameraTelemetry.WHITE_BALANCE, "White Balance")
    DetailItem(t, CameraTelemetry.COLOR_TEMP, "Color Temperature")
}

@Composable
fun RecordingSection(t: CameraTelemetry?) = Section("Recording") {
    DetailItem(t, CameraTelemetry.MOVIE_REC, "Recording State")
    DetailItem(t, CameraTelemetry.REC_RESOLUTION, "Recording Resolution")
    DetailItem(t, CameraTelemetry.REC_FPS, "Recording FPS")
    DetailItem(t, CameraTelemetry.FILE_FORMAT_MOVIE, "Recording Format")
    DetailItem(t, CameraTelemetry.REC_SETTING_MOVIE, "Recording Setting")
    DetailItem(t, CameraTelemetry.SANDQ_MODE, "S&Q Mode")
    DetailItem(t, CameraTelemetry.SANDQ_FPS, "S&Q Frame Rate")
    DetailItem(t, CameraTelemetry.PROXY_REC, "Proxy Recording")
}

@Composable
fun MediaSection(t: CameraTelemetry?) = Section("Media") {
    DetailItem(t, CameraTelemetry.SLOT1_STATUS, "Slot 1 Status")
    DetailItem(t, CameraTelemetry.SLOT1_REMAIN, "Slot 1 Remaining Time")
    DetailItem(t, CameraTelemetry.SLOT1_SHOTS, "Slot 1 Remaining Shots")
    DetailItem(t, CameraTelemetry.SLOT2_STATUS, "Slot 2 Status")
    DetailItem(t, CameraTelemetry.SLOT2_REMAIN, "Slot 2 Remaining Time")
    DetailItem(t, CameraTelemetry.SLOT2_SHOTS, "Slot 2 Remaining Shots")
}

@Composable
fun FocusSection(t: CameraTelemetry?) = Section("Focus") {
    DetailItem(t, CameraTelemetry.FOCUS_MODE_SETTING, "Focus Mode")
    DetailItem(t, CameraTelemetry.FOCUS_TRACKING, "Focus Tracking Status")
    DetailItem(t, CameraTelemetry.FOCUS_TOUCH_SPOT, "Focus Touch Spot")
    DetailItem(t, CameraTelemetry.SUBJECT_AF, "Subject Recognition AF")
}

@Composable
fun PowerSection(t: CameraTelemetry?) = Section("Power & Thermal") {
    DetailItem(t, CameraTelemetry.BATTERY, "Battery %")
    DetailItem(t, CameraTelemetry.BATTERY_MINUTES, "Battery Minutes Remaining")
    DetailItem(t, CameraTelemetry.BATTERY_VOLTAGE, "Battery Voltage")
    DetailItem(t, CameraTelemetry.BATTERY_TOTAL, "Total Battery Remaining")
    DetailItem(t, CameraTelemetry.OVERHEATING, "Overheating State")
    DetailItem(t, CameraTelemetry.AUTO_POWER_OFF_TEMP, "Auto Power-Off Temp")
}

@Composable
fun MonitoringSection(t: CameraTelemetry?) = Section("Monitoring") {
    DetailItem(t, CameraTelemetry.MONITOR_LUT, "LUT Status")
    DetailItem(t, CameraTelemetry.MONITOR_RESOLUTION, "Monitoring Resolution")
    DetailItem(t, CameraTelemetry.MONITOR_FPS, "Monitoring Frame Rate")
    DetailItem(t, CameraTelemetry.MONITOR_CODEC, "Monitoring Codec")
}
