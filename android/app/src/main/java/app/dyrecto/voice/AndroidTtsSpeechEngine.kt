package app.dyrecto.voice

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import app.dyrecto.liveview.voice.VoiceEvent
import app.dyrecto.liveview.voice.VoiceSpeechEngine
import app.dyrecto.liveview.voice.VoiceSpeechRate

/**
 * Phase 14 speech backend: Android [TextToSpeech]. The ONLY file in the app allowed to import
 * `android.speech.tts` — everything above it talks to [VoiceSpeechEngine], so a future offline
 * backend (e.g. Piper) is a new implementation of that interface and nothing else.
 *
 * Behavior notes:
 *  - **Init is async.** Utterances requested before the engine is ready are DROPPED with a log —
 *    guidance is only useful in the moment, and stale speech is worse than a missed sentence.
 *    The scheduler's cooldown/reminder rules naturally re-offer still-relevant guidance.
 *  - **Finished-listener contract** (see [VoiceSpeechEngine]): fires on natural completion or
 *    error, NEVER for an utterance cancelled by [stop] — [stop] clears the current utterance id
 *    first, so the resulting onStop callback is ignored.
 *  - **Audio**: assistance-guidance attributes on the media stream (media volume governs it, and
 *    it ducks playing media). Routing is left entirely to Android — a connected Bluetooth
 *    device wins automatically; there is deliberately no output-device selection.
 */
class AndroidTtsSpeechEngine(context: Context) : VoiceSpeechEngine {

    private val lock = Any()
    private var ready = false
    private var failed = false
    private var pendingRate: VoiceSpeechRate? = null
    /** Utterance id we expect callbacks for; null = nothing active / cancelled by stop(). */
    private var currentUtteranceId: String? = null
    private var utteranceSeq = 0L
    private var onFinished: (() -> Unit)? = null

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        synchronized(lock) {
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                configure()
                pendingRate?.let { applyRate(it) }
                pendingRate = null
            } else {
                failed = true
                Log.w(TAG, "TextToSpeech init failed (status=$status) — voice guidance unavailable")
            }
        }
    }

    private fun configure() {
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) = finished(utteranceId)

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finished(utteranceId)

            override fun onError(utteranceId: String?, errorCode: Int) = finished(utteranceId)

            // onStop is NOT overridden to call finished(): stop() already cleared the current id,
            // and the default implementation is a no-op — cancelled utterances stay silent.
        })
    }

    /** Completion path: only the utterance we still consider current may notify upward. */
    private fun finished(utteranceId: String?) {
        val notify = synchronized(lock) {
            if (utteranceId != null && utteranceId == currentUtteranceId) {
                currentUtteranceId = null
                true
            } else {
                false
            }
        }
        if (notify) onFinished?.invoke()
    }

    override fun speak(event: VoiceEvent) {
        synchronized(lock) {
            if (!ready) {
                if (!failed) Log.d(TAG, "TTS not ready — dropping utterance: ${event.text}")
                return
            }
            val id = "voice-${utteranceSeq++}"
            currentUtteranceId = id
            // QUEUE_FLUSH: the scheduler guarantees one active utterance; flushing keeps the
            // backend honest even if a previous utterance is somehow still draining.
            tts.speak(event.text, TextToSpeech.QUEUE_FLUSH, null, id)
        }
    }

    override fun stop() {
        synchronized(lock) {
            currentUtteranceId = null // ignore the upcoming onStop/onDone for the cancelled id
            if (ready) tts.stop()
        }
    }

    override fun setSpeechRate(rate: VoiceSpeechRate) {
        synchronized(lock) {
            if (ready) applyRate(rate) else pendingRate = rate
        }
    }

    private fun applyRate(rate: VoiceSpeechRate) {
        tts.setSpeechRate(rate.ttsRate)
    }

    override fun setOnUtteranceFinished(listener: () -> Unit) {
        onFinished = listener
    }

    override fun shutdown() {
        synchronized(lock) {
            currentUtteranceId = null
            ready = false
        }
        tts.shutdown()
    }
}

private const val TAG = "VoiceTts"
