package app.dyrecto.debug

import app.dyrecto.connection.BleLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Mirrors the verified stack's [BleLog] stream into an observable list for the
 * Developer screen. This is the ONLY UI surface that sees raw protocol logging;
 * it lives behind the Developer Mode toggle.
 */
object LogStore {

    private const val MAX_LINES = 5000

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    /** Hook BleLog once (call from Application.onCreate). */
    fun attach() {
        BleLog.listener = { line ->
            _lines.update { current ->
                val next = current + line
                if (next.size > MAX_LINES) next.takeLast(MAX_LINES) else next
            }
        }
    }

    fun clear() {
        _lines.value = emptyList()
    }

    /** Full dump for export/share. */
    fun export(): String = BleLog.dump()
}
