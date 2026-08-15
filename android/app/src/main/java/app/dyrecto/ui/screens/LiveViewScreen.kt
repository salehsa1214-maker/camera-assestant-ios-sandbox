package app.dyrecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import app.dyrecto.capability.CameraCapabilities
import app.dyrecto.capability.ControlResult
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.liveview.exposure.OverlayConfig
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.session.display
import app.dyrecto.liveview.vision.results.VisionContext
import app.dyrecto.ui.components.GlanceChip
import app.dyrecto.ui.components.RecBadge
import app.dyrecto.ui.liveview.FocusPeakingLayer
import app.dyrecto.ui.liveview.OverlayToggleRail
import app.dyrecto.ui.liveview.SettingEditorDialog
import app.dyrecto.ui.liveview.WaveformPanel
import app.dyrecto.ui.liveview.falseColorBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * The user-facing full-screen Live View — an immersive operating mode. It is a passive subscriber
 * of the persistent frame stream owned by the monitoring session: it neither starts nor stops
 * frame acquisition (that happens automatically on connect/disconnect), it only renders whatever
 * is already flowing, edge-to-edge, with floating camera-monitor overlays: a REC/exposure glance
 * over scrims. Engineering metrics (FPS/latency/byte counters) live in the Developer "Live View
 * Diagnostics" screen, not here.
 *
 * Tapping an exposure chip (ISO/Shutter/Aperture/WB) opens a slider to change it on the camera; the
 * overlay toggles (waveform/false color/peaking) sit in the letterbox bar — top in portrait, right
 * edge in landscape.
 */
@Composable
fun LiveViewScreen(
    render: StateFlow<LiveViewRenderState>,
    frameContext: StateFlow<FrameContext>,
    visionContext: StateFlow<VisionContext>,
    capabilities: StateFlow<CameraCapabilities?>,
    onSetProperty: suspend (code: Int, value: Long) -> ControlResult,
) {
    val renderState by render.collectAsState()
    val frame by frameContext.collectAsState()
    val vision by visionContext.collectAsState()
    val caps by capabilities.collectAsState()
    var editingCode by remember { mutableStateOf<Int?>(null) }

    val bitmap = renderState.frame
    val image = remember(bitmap) { bitmap?.asImageBitmap() }

    // Overlay toggle states — gate rendering here too, so an overlay disappears the instant it's
    // switched off (the Vision context still holds the last waveform/peaking result otherwise).
    val waveformOn by OverlayConfig.waveform.collectAsState()
    val peakingOn by OverlayConfig.focusPeaking.collectAsState()
    val falseColorOn by OverlayConfig.falseColor.collectAsState()

    // False color recolours the whole frame — compute it off the main thread only while enabled.
    val falseColorImage by produceState<ImageBitmap?>(null, bitmap, falseColorOn) {
        value = if (falseColorOn && bitmap != null) {
            withContext(Dispatchers.Default) { falseColorBitmap(bitmap).asImageBitmap() }
        } else {
            null
        }
    }

    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (image != null) {
            Image(
                bitmap = if (falseColorOn) falseColorImage ?: image else image,
                contentDescription = "Camera live view",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            // Focus peaking aligns to the frame (same aspect + ContentScale.Fit).
            if (peakingOn) FocusPeakingLayer(vision.focusPeaking, modifier = Modifier.fillMaxSize())
            Scrims()
            Overlay(frame, onEdit = { editingCode = it })
            // Overlay toggles: portrait -> top black bar (row); landscape -> right black bar (column, compact).
            OverlayToggleRail(
                vertical = landscape,
                compact = landscape,
                modifier = if (landscape) {
                    Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
                } else {
                    // Sit below the REC/battery row so it doesn't overlap them.
                    Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
                },
            )
            if (waveformOn) WaveformPanel(
                vision.waveform,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 64.dp),
            )
        } else {
            StartingState(active = renderState.active, modifier = Modifier.align(Alignment.Center))
        }
    }

    editingCode?.let { code ->
        SettingEditorDialog(
            label = CameraTelemetry.labelFor(code),
            property = caps?.property(code),
            onApply = { value -> onSetProperty(code, value) },
            onDismiss = { editingCode = null },
        )
    }
}

/** Top & bottom gradient scrims so white overlay text stays legible over any frame. */
@Composable
private fun Scrims() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent))),
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))),
        )
    }
}

/** The floating overlay: REC + battery on top, exposure glance on the bottom, AI regions reserved. */
@Composable
private fun Overlay(frame: FrameContext, onEdit: (Int) -> Unit) {
    val t = frame.telemetry
    val recording = t.display(CameraTelemetry.MOVIE_REC).lowercase().let {
        it.contains("rec") && !it.contains("standby")
    }
    val timecode = t.display(CameraTelemetry.REC_TIME).takeIf { it != "—" }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        // Top-left: REC badge (only while recording).
        if (recording) {
            RecBadge(timecode = timecode, modifier = Modifier.align(Alignment.TopStart))
        }

        // Top-right: battery glance.
        Box(Modifier.align(Alignment.TopEnd)) {
            GlanceChip("Battery", t.display(CameraTelemetry.BATTERY))
        }

        // Bottom: exposure glance bar.
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlanceChip("ISO", t.display(CameraTelemetry.ISO), onClick = { onEdit(CameraTelemetry.ISO) })
            GlanceChip("Shutter", t.display(CameraTelemetry.SHUTTER), onClick = { onEdit(CameraTelemetry.SHUTTER) })
            GlanceChip("Aperture", t.display(CameraTelemetry.FNUMBER), onClick = { onEdit(CameraTelemetry.FNUMBER) })
            GlanceChip("WB", t.display(CameraTelemetry.WHITE_BALANCE), onClick = { onEdit(CameraTelemetry.WHITE_BALANCE) })
        }
    }
}

@Composable
private fun StartingState(active: Boolean, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (active) {
            CircularProgressIndicator(color = Color.White)
            Text(
                "Starting Live View…",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            Text(
                "Live view stopped",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
