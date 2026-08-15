package app.dyrecto.liveview.voice

/**
 * The speech backend seam (Phase 14). Everything above this interface — scheduler, queue,
 * settings, UI — is backend-independent; only an implementation may touch a TTS API.
 * `AndroidTtsSpeechEngine` (app layer, Android TextToSpeech) is the first backend; a future
 * offline engine (e.g. Piper) is a new implementation and nothing else.
 *
 * Contract:
 *  - [speak] starts [VoiceEvent.text] immediately, replacing any current utterance at the
 *    backend level (the scheduler already guarantees at most one active utterance).
 *  - The finished listener fires exactly once per [speak] that runs to natural completion OR
 *    fails; it does NOT fire for an utterance cancelled by [stop] — the caller who stopped it
 *    already knows.
 */
interface VoiceSpeechEngine {

    fun speak(event: VoiceEvent)

    /** Cancels the current utterance, if any, WITHOUT firing the finished listener. */
    fun stop()

    fun setSpeechRate(rate: VoiceSpeechRate)

    /** Registers the single completion listener (called from an arbitrary thread). */
    fun setOnUtteranceFinished(listener: () -> Unit)

    /** Releases backend resources; the engine is unusable afterwards. */
    fun shutdown()
}
