package app.dyrecto.platform

import kotlin.system.getTimeMillis
import kotlin.system.getTimeNanos

actual fun epochMillis(): Long = getTimeMillis()

actual fun nanoTime(): Long = getTimeNanos()
