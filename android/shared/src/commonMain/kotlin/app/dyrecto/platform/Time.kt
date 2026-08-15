package app.dyrecto.platform

/** Wall-clock epoch milliseconds — the KMP stand-in for `System.currentTimeMillis()`. */
expect fun epochMillis(): Long

/** Monotonic nanoseconds for duration measurement — the KMP stand-in for `System.nanoTime()`. */
expect fun nanoTime(): Long
