package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertType

/**
 * Every voice timing constant lives here (Phase 14). Cooldowns are voice-only — visual alerts
 * keep their own debounce and are unaffected.
 *
 * Three distinct mechanisms, deliberately separate:
 *  - **assistant reminder interval**: the minimum time before the SAME unchanged assistant
 *    instruction is spoken again. User-configurable (see [VoiceSettings.assistantReminderSeconds]);
 *    the scheduler feeds it into [CooldownTracker.assistantCooldownMs]. It is both the floor for
 *    every assistant repeat trigger (stall / regression) AND the periodic-reminder backstop while
 *    the action still drifts.
 *  - **telemetry cooldown**: floors how soon the same non-critical telemetry alert may repeat
 *    (flap damping). Critical telemetry has none — every state change speaks.
 *  - **[MIN_SPEECH_GAP_MS]**: silence floor between ANY two utterances (different messages
 *    included) so guidance sounds calm rather than machine-gunned. Critical telemetry bypasses it.
 */
object VoiceCooldowns {

    /** Default / min / max for the user-configurable assistant reminder interval (seconds). */
    const val ASSISTANT_REMINDER_DEFAULT_SECONDS = 3
    const val ASSISTANT_REMINDER_MIN_SECONDS = 1
    const val ASSISTANT_REMINDER_MAX_SECONDS = 15

    /** Floor between repeats of the same non-critical telemetry alert (flap damping). */
    const val TELEMETRY_DEFAULT_MS = 5_000L

    /** Critical telemetry: no cooldown — every state change speaks. */
    const val CRITICAL_MS = 0L

    /** Silence enforced after any utterance before the next non-critical one may start. */
    const val MIN_SPEECH_GAP_MS = 800L

    /** Progress must improve by at least this much to count as "the operator is correcting". */
    const val PROGRESS_EPSILON = 0.02f

    /** No new best progress for this long ⇒ the correction has stalled. */
    const val PROGRESS_STALL_WINDOW_MS = 2_500L

    /** Clamp any candidate reminder interval into the supported range. */
    fun clampReminderSeconds(seconds: Int): Int =
        seconds.coerceIn(ASSISTANT_REMINDER_MIN_SECONDS, ASSISTANT_REMINDER_MAX_SECONDS)

    fun forTelemetry(type: AlertType): Long =
        if (VoiceTemplates.isCriticalTelemetry(type)) CRITICAL_MS else TELEMETRY_DEFAULT_MS
}

/**
 * Per-key last-spoken bookkeeping behind the cooldown rules. Pure; time comes from the injected
 * [clock]. [markDisappeared] deliberately CLEARS the entry: an instruction that disappeared and
 * later returned starts fresh and may speak immediately.
 *
 * Assistant (and the "Reference matched." announcement) use [assistantCooldownMs], which the
 * scheduler keeps in sync with the user's reminder-interval setting; telemetry uses the fixed
 * [VoiceCooldowns.forTelemetry] table.
 */
class CooldownTracker(private val clock: () -> Long) {

    /** The assistant repeat interval in millis — updated from settings by the scheduler. */
    var assistantCooldownMs: Long =
        VoiceCooldowns.ASSISTANT_REMINDER_DEFAULT_SECONDS * 1000L

    private val lastSpoken = mutableMapOf<VoiceKey, Long>()

    private fun durationFor(key: VoiceKey): Long = when (key) {
        is VoiceKey.Telemetry -> VoiceCooldowns.forTelemetry(key.type)
        is VoiceKey.Assistant, VoiceKey.ReferenceMatched, VoiceKey.ShotCompleted -> assistantCooldownMs
    }

    /** True when [key] is outside its cooldown window (or has never been spoken / was cleared). */
    fun ready(key: VoiceKey): Boolean {
        val spokenAt = lastSpoken[key] ?: return true
        return clock() - spokenAt >= durationFor(key)
    }

    fun markSpoken(key: VoiceKey) {
        lastSpoken[key] = clock()
    }

    /** When [key] was last spoken, or null if never / cleared by [markDisappeared]. */
    fun lastSpokenAt(key: VoiceKey): Long? = lastSpoken[key]

    /** The event vanished: forget it, so a later return bypasses the cooldown. */
    fun markDisappeared(key: VoiceKey) {
        lastSpoken.remove(key)
    }

    /** Remaining cooldown millis per key still inside its window (Developer card). */
    fun snapshotRemaining(): Map<VoiceKey, Long> {
        val now = clock()
        return lastSpoken.mapNotNull { (key, spokenAt) ->
            val remaining = durationFor(key) - (now - spokenAt)
            if (remaining > 0) key to remaining else null
        }.toMap()
    }

    fun reset() = lastSpoken.clear()
}
