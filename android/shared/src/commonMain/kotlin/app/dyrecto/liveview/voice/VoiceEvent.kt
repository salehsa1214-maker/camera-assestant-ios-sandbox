package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction

/** Which existing guidance stream produced a [VoiceEvent]. Voice never adds a third source. */
enum class VoiceSource {
    ASSISTANT,
    TELEMETRY,
}

/**
 * Stable semantic identity of a voice event — NEVER derived from the spoken text. All dedupe,
 * cooldown, merge, and obsolete-removal logic keys on this; the phrase itself is presentation
 * only and may change (translations, rewording, a different speech backend) without affecting
 * scheduling behavior.
 */
sealed interface VoiceKey {
    /** Human-readable stable label for the Developer voice card. */
    val label: String

    data class Assistant(val action: AssistantAction) : VoiceKey {
        override val label: String get() = "assistant:${action.name}"
    }

    data class Telemetry(val type: AlertType) : VoiceKey {
        override val label: String get() = "telemetry:${type.name}"
    }

    /** The one positive assistant announcement — spoken when a guided drift episode resolves. */
    data object ReferenceMatched : VoiceKey {
        override val label: String get() = "assistant:REFERENCE_MATCHED"
    }

    /** Phase 15: spoken once when a storyboard shot flips to Completed (optional milestone cue). */
    data object ShotCompleted : VoiceKey {
        override val label: String get() = "assistant:SHOT_COMPLETED"
    }
}

/**
 * One candidate spoken message. Pure data — the [VoiceScheduler] decides whether and when it is
 * actually voiced.
 *
 * @param interruptible  false only for critical telemetry: a non-interruptible utterance is never
 *                       cut off, and CRITICAL events themselves are the only ones allowed to cut
 *                       off an interruptible one.
 * @param repeatable     whether the same key may be spoken again later (subject to the
 *                       scheduler's cooldown/session rules).
 * @param timestamp      clock millis when the event was created (queue FIFO tiebreak).
 */
data class VoiceEvent(
    val source: VoiceSource,
    val priority: VoicePriority,
    val key: VoiceKey,
    val text: String,
    val interruptible: Boolean,
    val repeatable: Boolean,
    val timestamp: Long,
)
