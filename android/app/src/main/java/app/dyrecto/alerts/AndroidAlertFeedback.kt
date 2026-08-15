package app.dyrecto.alerts

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import app.dyrecto.domain.alerts.AlertPattern
import app.dyrecto.domain.alerts.AlertSeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * In-app sound + haptic delivery for V1. Lightweight and local: tones come from the system
 * [ToneGenerator] (no bundled assets, no TTS/voice), vibration from [Vibrator]/[VibrationEffect].
 *
 * The number of beeps / vibration pulses and the gap between them are driven by the configured
 * [AlertPattern]s; [AlertSeverity] still selects the tone character/intensity:
 *  - INFO     → light beep
 *  - WARNING  → short beep
 *  - CRITICAL → strong alert tone
 *
 * Playback is non-blocking — multi-beep sequences run on an internal coroutine scope, so [deliver]
 * returns immediately. A spam guard ([playing]) drops any call that arrives while a pattern is still
 * in flight, so rapid taps (e.g. the Test button) never stack overlapping sounds/vibrations.
 *
 * Every interaction with hardware is wrapped in [runCatching] so a missing vibrator or a busy tone
 * generator can never crash alert delivery.
 */
class AndroidAlertFeedback(context: Context) : AlertFeedback {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _playing = MutableStateFlow(false)
    override val playing: StateFlow<Boolean> = _playing.asStateFlow()

    private var job: Job? = null

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    // Lazily created and reused; STREAM_NOTIFICATION keeps alerts off the media volume.
    private val toneGenerator: ToneGenerator? by lazy {
        runCatching {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME)
        }.getOrNull()
    }

    override fun deliver(severity: AlertSeverity, sound: AlertPattern, vibration: AlertPattern) {
        // Spam guard: ignore overlapping triggers while a pattern is still playing.
        if (_playing.value) return
        if (sound.count <= 0 && vibration.count <= 0) return

        _playing.value = true

        // Vibration is a single waveform call (the OS plays the whole pulse train).
        vibratePulses(vibration)

        job = scope.launch {
            try {
                val toneType = toneFor(severity)
                val toneMs = toneDurationFor(severity)
                repeat(sound.count) { i ->
                    playTone(toneType, toneMs)
                    if (i < sound.count - 1) delay(toneMs + sound.intervalMs)
                }
                // Let the final tone finish before clearing the guard. Also covers the
                // sound-silent / vibration-only case so the guard reflects the vibration length.
                val tail = maxOf(
                    if (sound.count > 0) toneMs.toLong() else 0L,
                    vibrationDurationMs(vibration),
                )
                delay(tail)
            } finally {
                _playing.value = false
            }
        }
    }

    private fun toneFor(severity: AlertSeverity): Int = when (severity) {
        AlertSeverity.INFO -> ToneGenerator.TONE_PROP_BEEP
        AlertSeverity.WARNING -> ToneGenerator.TONE_PROP_BEEP
        AlertSeverity.CRITICAL -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
    }

    private fun toneDurationFor(severity: AlertSeverity): Int = when (severity) {
        AlertSeverity.INFO -> 150
        AlertSeverity.WARNING -> 200
        AlertSeverity.CRITICAL -> 400
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        runCatching { toneGenerator?.startTone(toneType, durationMs) }
    }

    /** Builds a `[0, on, gap, on, gap, ...]` waveform for [pattern]'s pulse count. */
    private fun vibratePulses(pattern: AlertPattern) {
        if (pattern.count <= 0) return
        val timings = ArrayList<Long>(pattern.count * 2)
        timings.add(0L) // initial delay
        repeat(pattern.count) { i ->
            timings.add(PULSE_MS)
            if (i < pattern.count - 1) timings.add(pattern.intervalMs)
        }
        runCatching {
            vibrator?.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), -1))
        }
    }

    private fun vibrationDurationMs(pattern: AlertPattern): Long {
        if (pattern.count <= 0) return 0L
        return pattern.count * PULSE_MS + (pattern.count - 1).coerceAtLeast(0) * pattern.intervalMs
    }

    private companion object {
        const val TONE_VOLUME = 90 // 0..100
        const val PULSE_MS = 120L
    }
}
