package app.dyrecto.liveview.voice

/**
 * The pending spoken messages — a pure, fully deterministic ordered set. Order: priority first
 * ([VoicePriority] ordinal), then event timestamp, then insertion sequence, so two runs with the
 * same inputs always speak in the same order. Same-key offers merge (the newer event replaces
 * the older one in place, keeping its queue position) — the queue never holds two entries for
 * one semantic key.
 *
 * Holds only what is WAITING; the single active utterance lives in [VoiceScheduler].
 */
class VoiceQueue {

    private data class Entry(var event: VoiceEvent, val seq: Long)

    private val entries = mutableListOf<Entry>()
    private var nextSeq = 0L

    val size: Int get() = entries.size

    /** Adds [event], or replaces the pending event with the same key (merge, position kept). */
    fun offer(event: VoiceEvent) {
        val existing = entries.firstOrNull { it.event.key == event.key }
        if (existing != null) {
            // Keep the original timestamp so the merged event keeps its queue position.
            existing.event = event.copy(timestamp = existing.event.timestamp)
        } else {
            entries += Entry(event, nextSeq++)
        }
    }

    /** The next event to speak, without removing it. */
    fun peek(): VoiceEvent? = entries.minWithOrNull(ORDER)?.event

    /** Removes and returns the next event to speak. */
    fun poll(): VoiceEvent? {
        val head = entries.minWithOrNull(ORDER) ?: return null
        entries.remove(head)
        return head.event
    }

    fun removeByKey(key: VoiceKey) {
        entries.removeAll { it.event.key == key }
    }

    fun removeAll(predicate: (VoiceEvent) -> Boolean) {
        entries.removeAll { predicate(it.event) }
    }

    /** Pending events in speak order (Developer card). */
    fun snapshot(): List<VoiceEvent> = entries.sortedWith(ORDER).map { it.event }

    fun clear() = entries.clear()

    private companion object {
        val ORDER = compareBy<Entry>(
            { it.event.priority.ordinal },
            { it.event.timestamp },
            { it.seq },
        )
    }
}
