package app.dyrecto.liveview.voice

/** Which guidance sources may be voiced. Visual alerts are unaffected by this. */
enum class VoiceMode {
    ASSISTANT_ONLY,
    TELEMETRY_ONLY,
    BOTH,
}

/** Speech-rate presets; [ttsRate] is the multiplier the speech backend applies (1.0 = normal). */
enum class VoiceSpeechRate(val ttsRate: Float) {
    SLOW(0.8f),
    NORMAL(1.0f),
    FAST(1.25f),
}

/**
 * User-facing voice guidance settings (persisted by `VoiceSettingsStore`).
 *
 * Voice is OFF by default — it must be an explicit opt-in. Volume follows the media stream and
 * audio routing follows Android's normal behavior (a connected Bluetooth device wins
 * automatically); neither is configured here.
 */
data class VoiceSettings(
    val enabled: Boolean = false,
    val mode: VoiceMode = VoiceMode.BOTH,
    val speechRate: VoiceSpeechRate = VoiceSpeechRate.NORMAL,
    /**
     * Minimum seconds before the SAME unchanged assistant instruction is spoken again — the
     * user gives themselves time to physically react. Only assistant repetition is governed by
     * this; telemetry cadence is transition-based and unaffected. Clamped to
     * [VoiceCooldowns.ASSISTANT_REMINDER_MIN_SECONDS]..[VoiceCooldowns.ASSISTANT_REMINDER_MAX_SECONDS].
     */
    val assistantReminderSeconds: Int = VoiceCooldowns.ASSISTANT_REMINDER_DEFAULT_SECONDS,
) {
    /** The reminder interval as millis, for the scheduler's timing math. */
    val assistantReminderMs: Long get() = assistantReminderSeconds * 1000L

    /** True when [source] is allowed to produce speech under the current settings. */
    fun allows(source: VoiceSource): Boolean = enabled && when (mode) {
        VoiceMode.BOTH -> true
        VoiceMode.ASSISTANT_ONLY -> source == VoiceSource.ASSISTANT
        VoiceMode.TELEMETRY_ONLY -> source == VoiceSource.TELEMETRY
    }
}
