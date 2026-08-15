package app.dyrecto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.screens.details.ExposureSection
import app.dyrecto.ui.screens.details.FocusSection
import app.dyrecto.ui.screens.details.MediaSection
import app.dyrecto.ui.screens.details.MonitoringSection
import app.dyrecto.ui.screens.details.PowerSection
import app.dyrecto.ui.screens.details.RecordingSection

/**
 * Camera Details — the full telemetry, grouped into the six category sections. Composed from
 * independent section composables (so any one can later be promoted to its own screen). Read-only,
 * value-only; no protocol codes (those live in Developer → Telemetry Explorer).
 */
@Composable
fun CameraDetailsScreen(state: CameraConnectionState) {
    val t = state.telemetry
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (t == null) {
            Text(
                "No telemetry received yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        ExposureSection(t)
        RecordingSection(t)
        FocusSection(t)
        MediaSection(t)
        PowerSection(t)
        MonitoringSection(t)
    }
}
