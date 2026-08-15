package app.dyrecto.platform

actual class AtomicLong actual constructor(initial: Long) {
    private val delegate = java.util.concurrent.atomic.AtomicLong(initial)
    actual fun getAndIncrement(): Long = delegate.getAndIncrement()
    actual fun set(value: Long) = delegate.set(value)
}
