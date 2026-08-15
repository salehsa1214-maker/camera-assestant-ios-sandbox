package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction
import app.dyrecto.liveview.instructions.AssistantInstruction

/** Records every engine call; utterance completion is driven manually by the test. */
class FakeSpeechEngine : VoiceSpeechEngine {
    val spoken = mutableListOf<VoiceEvent>()
    var stops = 0
    var rate: VoiceSpeechRate? = null
    private var listener: (() -> Unit)? = null

    override fun speak(event: VoiceEvent) {
        spoken += event
    }

    override fun stop() {
        stops++
    }

    override fun setSpeechRate(rate: VoiceSpeechRate) {
        this.rate = rate
    }

    override fun setOnUtteranceFinished(listener: () -> Unit) {
        this.listener = listener
    }

    override fun shutdown() {}
}

/** Scheduler + fake engine + mutable clock, wired the way the session wires them. */
class VoiceDriver(settings: VoiceSettings = VoiceSettings(enabled = true)) {
    var now = 1_000_000L
    val engine = FakeSpeechEngine()
    val scheduler = VoiceScheduler(engine, { now })

    init {
        scheduler.updateSettings(settings)
    }

    fun instruction(
        action: AssistantAction,
        progress: Float = 0.4f,
    ): AssistantInstruction = AssistantInstruction(
        action = action,
        message = "visual message",
        confidence = 0.9f,
        progress = progress,
        reason = "test",
    )

    fun assistant(action: AssistantAction?, progress: Float = 0.4f, active: Boolean = true) =
        scheduler.onAssistantInstruction(action?.let { instruction(it, progress) }, active)

    fun alert(type: AlertType) = scheduler.onTelemetryAlert(
        Alert(
            id = 1L,
            type = type,
            severity = type.defaultSeverity,
            title = type.title,
            message = "test alert",
            timestamp = now,
        ),
    )

    /** The current utterance finishes naturally. */
    fun finish() = scheduler.onUtteranceFinished()

    /** Advance the clock and let any pending speech-gap re-check fire. */
    fun advance(ms: Long) {
        now += ms
        scheduler.onGapElapsed()
    }

    val spokenTexts: List<String> get() = engine.spoken.map { it.text }
}
