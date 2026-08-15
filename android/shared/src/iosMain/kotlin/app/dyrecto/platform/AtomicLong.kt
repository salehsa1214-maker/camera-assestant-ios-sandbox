package app.dyrecto.platform

import kotlin.concurrent.AtomicLong as NativeAtomicLong

actual class AtomicLong actual constructor(initial: Long) {
    private val delegate = NativeAtomicLong(initial)

    actual fun getAndIncrement(): Long {
        while (true) {
            val current = delegate.value
            if (delegate.compareAndSet(current, current + 1)) return current
        }
    }

    actual fun set(value: Long) {
        delegate.value = value
    }
}
