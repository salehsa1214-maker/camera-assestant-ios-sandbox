package app.dyrecto.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val clock = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
private val clockShort = SimpleDateFormat("HH:mm:ss", Locale.US)

fun formatTime(ts: Long?): String = ts?.let { clock.format(Date(it)) } ?: "—"
fun formatTimeShort(ts: Long?): String = ts?.let { clockShort.format(Date(it)) } ?: "—"

fun formatDuration(ms: Long?): String = when {
    ms == null -> "—"
    ms < 1000 -> "${ms} ms"
    else -> "%.2f s".format(ms / 1000.0)
}
