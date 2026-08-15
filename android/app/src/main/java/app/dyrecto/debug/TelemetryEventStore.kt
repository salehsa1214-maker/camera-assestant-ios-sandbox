package app.dyrecto.debug

import app.dyrecto.domain.CameraTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger

/**
 * Isolated telemetry event history for the Telemetry Explorer developer tool.
 *
 * Completely separate from [LogStore] and [BleLog]. Fed by a single side-call in
 * [app.dyrecto.data.CameraRepositoryImpl.wireTelemetryRefresh] — no other
 * coupling to the protocol stack. All mutations are thread-safe (lock-free StateFlow updates).
 */
object TelemetryEventStore {

    private const val MAX_EVENTS = 1000

    private val batchCounter = AtomicInteger(0)

    // ── Public StateFlows ───────────────────────────────────────────────────

    private val _events = MutableStateFlow<List<TelemetryEvent>>(emptyList())
    /** Newest-first, capped at [MAX_EVENTS]. */
    val events: StateFlow<List<TelemetryEvent>> = _events.asStateFlow()

    private val _propLastChanged = MutableStateFlow<Map<Int, Long>>(emptyMap())
    /** Maps property code → epoch ms of its most recent FIRST_SEEN or CHANGED event. */
    val propLastChanged: StateFlow<Map<Int, Long>> = _propLastChanged.asStateFlow()

    private val _changedDuringSession = MutableStateFlow<Set<Int>>(emptySet())
    /** Property codes that have fired at least one CHANGED event (not just FIRST_SEEN). */
    val changedDuringSession: StateFlow<Set<Int>> = _changedDuringSession.asStateFlow()

    // ── API ─────────────────────────────────────────────────────────────────

    /**
     * Diffs [old] against [new] and records one [TelemetryEvent] per changed property.
     * All events from a single call share the same [TelemetryEvent.batchId].
     *
     * Called from inside the repository's synchronized update block; must not block.
     *
     * @param old  telemetry snapshot BEFORE the merge (null on first fetch)
     * @param new  merged snapshot that was just committed to state
     */
    fun processUpdate(old: CameraTelemetry?, new: CameraTelemetry) {
        val now = System.currentTimeMillis()
        val batchId = batchCounter.incrementAndGet()
        val oldProps = old?.props ?: emptyMap()

        val newEvents = mutableListOf<TelemetryEvent>()
        val newLastChanged = mutableMapOf<Int, Long>()
        val newChanged = mutableSetOf<Int>()

        for ((code, newProp) in new.props) {
            val oldProp = oldProps[code]
            val isFirstSeen = oldProp == null
            val hasValueChanged = !isFirstSeen && oldProp!!.rawValue != newProp.rawValue

            if (isFirstSeen || hasValueChanged) {
                val eventType = if (isFirstSeen) TelemetryEvent.EventType.FIRST_SEEN
                                else TelemetryEvent.EventType.CHANGED
                newEvents += TelemetryEvent(
                    timestamp = now,
                    batchId = batchId,
                    propertyCode = code,
                    propertyLabel = newProp.label,
                    rawValueBefore = oldProp?.rawValue,
                    rawValueAfter = newProp.rawValue,
                    decodedValue = newProp.decoded,
                    eventType = eventType,
                )
                newLastChanged[code] = now
                if (eventType == TelemetryEvent.EventType.CHANGED) newChanged += code
            }
        }

        if (newEvents.isEmpty()) return

        _events.update { current ->
            val combined = newEvents + current
            if (combined.size > MAX_EVENTS) combined.take(MAX_EVENTS) else combined
        }
        _propLastChanged.update { it + newLastChanged }
        if (newChanged.isNotEmpty()) _changedDuringSession.update { it + newChanged }
    }

    /** Resets all state. Called at the start of each new connection session and on user clear. */
    fun clear() {
        _events.value = emptyList()
        _propLastChanged.value = emptyMap()
        _changedDuringSession.value = emptySet()
        batchCounter.set(0)
    }
}
