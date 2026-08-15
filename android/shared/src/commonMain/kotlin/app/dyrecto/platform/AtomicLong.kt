package app.dyrecto.platform

/**
 * Minimal cross-platform atomic counter — the KMP stand-in for
 * `java.util.concurrent.atomic.AtomicLong` (only the ops the app uses).
 */
expect class AtomicLong(initial: Long) {
    fun getAndIncrement(): Long
    fun set(value: Long)
}
