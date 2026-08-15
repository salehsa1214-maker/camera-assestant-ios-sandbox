package app.dyrecto.liveview.voice

/**
 * Spoken-urgency tier of a [VoiceEvent]. Ordinal order IS the queue order (CRITICAL first).
 *
 * CRITICAL is reserved for interrupting telemetry (recording stopped, battery critical, card
 * gone, disconnect, overheating): it bypasses cooldowns and the minimum speech gap, and is the
 * only tier allowed to cut off an in-progress utterance. Assistant guidance is NORMAL — it can
 * never interrupt anything.
 */
enum class VoicePriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW,
}
