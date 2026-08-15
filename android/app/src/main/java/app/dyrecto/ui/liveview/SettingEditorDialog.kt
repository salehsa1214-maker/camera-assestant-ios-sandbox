package app.dyrecto.ui.liveview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.dyrecto.capability.CameraProperty
import app.dyrecto.capability.ControlRejection
import app.dyrecto.capability.ControlResult
import app.dyrecto.capability.PropertyValueSet
import app.dyrecto.domain.TelemetryDecoder
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A tap-to-edit control for a single camera setting. The value pops up big and the user scrubs a
 * horizontal slider left/right through the camera's own advertised legal values (enum stops or a
 * range) — no dropdown. Releasing the slider applies that value through the validated control seam
 * (the deliberate release is the confirm-to-apply step); the outcome — including "camera rejected"
 * or "not adjustable now" — is shown inline. A successful change is confirmed when the new value
 * echoes back in telemetry (the caller's readout updates).
 */
@Composable
fun SettingEditorDialog(
    label: String,
    property: CameraProperty?,
    onApply: suspend (Long) -> ControlResult,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (property == null || !property.writable) {
                    Text(
                        if (property == null) "This setting isn't reported by the camera."
                        else "This setting is read-only on the camera right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    DismissRow(onDismiss)
                    return@Column
                }

                val values = remember(property) { selectableValues(property) }
                if (values.isEmpty()) {
                    Text(
                        "The camera reported no selectable values for this setting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    DismissRow(onDismiss)
                    return@Column
                }

                if (!property.available) {
                    Text(
                        "Not adjustable in the camera's current mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                val scope = rememberCoroutineScope()
                var status by remember { mutableStateOf<String?>(null) }

                val startIndex = remember(property, values) {
                    values.indexOf(property.currentRaw).let { if (it >= 0) it else values.size / 2 }
                }
                var index by remember { mutableFloatStateOf(startIndex.toFloat()) }
                val current = values[index.roundToInt().coerceIn(values.indices)]

                // Big live value readout while scrubbing.
                Text(
                    text = TelemetryDecoder.decode(property.code, current) ?: current.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                )

                Slider(
                    value = index,
                    onValueChange = { index = it },
                    valueRange = 0f..(values.size - 1).coerceAtLeast(1).toFloat(),
                    steps = (values.size - 2).coerceAtLeast(0),
                    onValueChangeFinished = {
                        val picked = values[index.roundToInt().coerceIn(values.indices)]
                        scope.launch {
                            status = "Applying…"
                            status = describe(onApply(picked))
                        }
                    },
                )
                Text(
                    "Slide to change · release to apply",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                status?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
                }
                DismissRow(onDismiss)
            }
        }
    }
}

/** Flattens the capability form into an ordered list the slider scrubs through (ascending). */
private fun selectableValues(property: CameraProperty): List<Long> = when (val vs = property.valueSet) {
    is PropertyValueSet.Enum -> vs.values.sorted()
    is PropertyValueSet.Range -> {
        val step = max(vs.step, 1L)
        val out = ArrayList<Long>()
        var v = vs.min
        // Cap the generated stop count so a huge range stays a smooth, bounded slider.
        while (v <= vs.max && out.size < 512) { out.add(v); v += step }
        if (out.isEmpty()) listOf(vs.min) else out
    }
    is PropertyValueSet.None -> emptyList()
}

@Composable
private fun DismissRow(onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onDismiss) { Text("Close") }
    }
}

private fun describe(result: ControlResult): String = when (result) {
    is ControlResult.Success -> "Applied — confirming from the camera…"
    is ControlResult.NotEnabled -> "Camera control is disabled."
    is ControlResult.Failed -> "Camera rejected the change: ${result.reason}"
    is ControlResult.Rejected -> when (result.rejection) {
        ControlRejection.UNKNOWN_PROPERTY -> "Not a known camera property."
        ControlRejection.NOT_WRITABLE -> "This setting is read-only."
        ControlRejection.NOT_AVAILABLE -> "Not adjustable in the current mode."
        ControlRejection.OUT_OF_RANGE -> "Value out of the allowed range."
    }
}
