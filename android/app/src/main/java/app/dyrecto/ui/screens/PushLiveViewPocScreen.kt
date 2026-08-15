package app.dyrecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.debug.LogStore
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.liveview.PushLvStatus
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.session.display
import app.dyrecto.liveview.veric.VericOfflineAnalyzer
import app.dyrecto.ui.components.SectionCard
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.concurrent.thread

/**
 * Developer-only **Live View Diagnostics** screen for the push pipeline.
 *
 * Frame acquisition is owned by the persistent frame stream and driven automatically by connection
 * state — this screen no longer starts or stops it. Start is kept only as a harmless manual
 * confirmation (no-op once the stream is already running); there is deliberately no Stop button
 * here, since stopping would halt the shared stream that Vision/Scene/Alerts/Live View all depend
 * on with no automatic recovery until the next connect/disconnect cycle. This screen surfaces the
 * live render, byte/FPS/latency counters, the unified Frame Context, and the [PUSH-LV] log tail.
 * Raw bytes are also captured to files for offline analysis. The user-facing Live View lives in
 * [LiveViewScreen].
 */
@Composable
fun PushLiveViewPocScreen(
    pushLiveView: StateFlow<PushLvStatus>,
    pushLiveRender: StateFlow<LiveViewRenderState>,
    frameContext: StateFlow<FrameContext>,
    onStart: () -> Unit,
) {
    val status by pushLiveView.collectAsState()
    val render by pushLiveRender.collectAsState()
    val frame by frameContext.collectAsState()
    val allLines by LogStore.lines.collectAsState()
    val lines = allLines.filter { it.contains("[PUSH-LV]") || it.contains("[VERIC]") || it.contains("[RENDER]") }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        LiveImage(render)

        SectionCard(title = "Live View Diagnostics") {
            Text(
                "Engineering view of the push Live View pipeline. Opens two local TCP listen sockets " +
                    "and sends SDIO_ControlMonitoring(Start=1) over the live PTP command channel; the raw " +
                    "streams are saved to push_lv_video.bin / push_lv_meta.bin (external files dir) for " +
                    "offline analysis. Decoded JPEG frames are rendered live above. Connect to a camera first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Start (confirm running)")
            }
            OutlinedButton(
                onClick = {
                    val dir = context.getExternalFilesDir(null)
                    thread(name = "veric-analyze") {
                        VericOfflineAnalyzer.runOnDevice(
                            captureDir = dir ?: return@thread,
                            exportDir = File(dir, "veric_frames"),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Analyze capture (offline → [VERIC] log)") }
        }

        SectionCard(title = "Status") {
            StatRow("Phase", status.phase.name)
            StatRow("Phone IP", status.phoneIp ?: "—")
            StatRow("Video port", if (status.videoPort != 0) status.videoPort.toString() else "—")
            StatRow("Meta port", if (status.metaPort != 0) status.metaPort.toString() else "—")
            StatRow("Start response", status.startRcName ?: "—")
            StatRow("Video connected", if (status.videoConnected) "yes" else "no")
            StatRow("Meta connected", if (status.metaConnected) "yes" else "no")
            StatRow("Video bytes", status.videoBytes.toString())
            StatRow("Meta bytes", status.metaBytes.toString())
            StatRow("Frames parsed", status.framesParsed.toString())
            StatRow("Render recv/dec/disp FPS",
                "%.0f / %.0f / %.0f".format(render.receivedFps, render.decodedFps, render.displayedFps))
            StatRow("Dropped frames", render.droppedFrames.toString())
            StatRow("Avg decode / latency",
                "%.1f / %.1f ms".format(render.avgDecodeMs, render.avgLatencyMs))
            status.message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status.phase == PushLvStatus.Phase.ERROR)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        SectionCard(title = "Frame Context") {
            StatRow("Render seq", frame.renderSequence.toString())
            StatRow("Frame FPS", "%.1f".format(frame.frameFps))
            StatRow("Latency", "%.1f ms".format(frame.latencyMs))
            StatRow("Frame age", "${frame.frameAgeMs} ms")
            StatRow("Connection", frame.connectionPhase.label)
            StatRow("Live View active", if (frame.liveViewActive) "yes" else "no")
            val t = frame.telemetry
            StatRow("ISO", t.display(CameraTelemetry.ISO))
            StatRow("Shutter", t.display(CameraTelemetry.SHUTTER))
            StatRow("Aperture", t.display(CameraTelemetry.FNUMBER))
            StatRow("White balance", t.display(CameraTelemetry.WHITE_BALANCE))
            StatRow("Recording", t.display(CameraTelemetry.MOVIE_REC))
            StatRow("Battery", t.display(CameraTelemetry.BATTERY))
            StatRow("Storage", t.display(CameraTelemetry.SLOT1_STATUS))
            StatRow("Focus state", t.display(CameraTelemetry.FOCUS_MODE))
        }

        Text(
            "[PUSH-LV] log (${lines.size} lines)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        val listState = rememberLazyListState()
        LaunchedEffect(lines.size) {
            if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
        }
        Card(Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(lines) { line ->
                    Text(
                        line,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (line.contains("[ERROR]")) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * The live camera image: black backdrop, centered, aspect-ratio preserved (no stretch). Converts the
 * renderer's [android.graphics.Bitmap] to a Compose `ImageBitmap` only when the frame object changes
 * (via `remember(bitmap)`), not on every recomposition.
 */
@Composable
private fun LiveImage(render: LiveViewRenderState) {
    val bitmap = render.frame
    val image = remember(bitmap) { bitmap?.asImageBitmap() }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = "Live view",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                if (render.active) "Waiting for frames…" else "Live view stopped",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
    }
}
