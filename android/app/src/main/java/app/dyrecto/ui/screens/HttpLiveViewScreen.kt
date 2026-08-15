package app.dyrecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dyrecto.liveview.LiveViewState
import app.dyrecto.ui.liveview.BitmapLiveViewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn

/**
 * Legacy **HTTP Live View** (phase 4A foundation), retained as a Developer diagnostic/comparison
 * tool. The primary user-facing Live View is the push pipeline in [LiveViewScreen]; this screen
 * drives the older HTTP transport (`session.startLiveView` / `liveView`) unchanged.
 *
 * Starts the stream on enter and stops it on leave. Decoding the renderer-neutral frames into
 * [ImageBitmap] happens here, off the main thread, via [BitmapLiveViewRenderer].
 */
@Composable
fun HttpLiveViewScreen(
    liveView: StateFlow<LiveViewState>,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    DisposableEffect(Unit) {
        onStart()
        onDispose { onStop() }
    }

    val state by liveView.collectAsState()
    val renderer = remember { BitmapLiveViewRenderer() }

    // Decode the newest frame off the main thread. Upstream conflation guarantees newest-wins if
    // decoding lags, so no backlog builds up.
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, liveView, renderer) {
        liveView
            .map { it.frame }
            .map { frame -> frame?.let { renderer.render(it) } }
            .flowOn(Dispatchers.Default)
            .collect { value = it }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val frame = bitmap
        when {
            frame != null -> Image(
                bitmap = frame,
                contentDescription = "Camera live view (HTTP)",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            state.status == LiveViewState.Status.ERROR -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    state.error ?: "Live View unavailable",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                )
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    "Starting HTTP Live View…",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
