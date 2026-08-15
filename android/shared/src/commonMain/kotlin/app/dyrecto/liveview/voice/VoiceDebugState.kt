package app.dyrecto.liveview.voice

/**
 * Read-only observability snapshot for the Developer "Voice" card. Published by
 * [VoiceScheduler] on every scheduling mutation (not per frame — silent ticks don't republish).
 */
data class VoiceDebugState(
    val enabled: Boolean = false,
    /** Text currently being spoken, or null while silent. */
    val speaking: String? = null,
    /** Pending event key labels in speak order. */
    val queued: List<String> = emptyList(),
    /** Remaining cooldown millis per key label still inside its window. */
    val cooldownsRemainingMs: Map<String, Long> = emptyMap(),
    /** Events dropped by the settings gate / cooldown / duplicate rules since reset. */
    val suppressedCount: Int = 0,
    val lastSuppressedReason: String? = null,
    /** Text of the most recently spoken event. */
    val lastSpoken: String? = null,
    val spokenCount: Int = 0,
)
