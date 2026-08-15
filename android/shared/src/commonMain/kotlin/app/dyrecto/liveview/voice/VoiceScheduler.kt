package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.liveview.instructions.AssistantAction
import app.dyrecto.liveview.instructions.AssistantInstruction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Decides WHEN to speak — never WHAT (Phase 14). Both inputs already carry final decisions:
 * the assistant instruction is the Phase 13 selector's single verdict per frame, and telemetry
 * alerts have passed the existing AlertConfig gate. This class only sequences them:
 *
 *  - **One active utterance**, ever. New events wait in [VoiceQueue] unless the interrupt rule
 *    applies: a CRITICAL telemetry event cuts off an interruptible utterance; CRITICAL events
 *    themselves are never interruptible, so assistant guidance can never cut off critical
 *    telemetry.
 *  - **Assistant guidance is a continuous session per [AssistantAction]**, not a stream of
 *    events. An unchanged action re-speaks ONLY on stall, regression, or the long reminder
 *    timeout — cooldown expiry alone never triggers a repeat; the per-key cooldown is a rate
 *    limiter. While the operator's progress keeps improving, the assistant stays silent.
 *  - **Telemetry is transition-spoken**: each alert the upstream producers fire (they are
 *    already transition-based) speaks at most once, cooldown-floored against flapping; an
 *    unchanged telemetry state is never reminded.
 *  - **Obsolete guidance dies unspoken**: when the instruction disappears or changes, its queued
 *    event is removed. If a guided episode resolves while monitoring is active, one
 *    "Reference matched." is spoken instead.
 *  - **Minimum speech gap** ([VoiceCooldowns.MIN_SPEECH_GAP_MS]) separates ANY two utterances so
 *    different messages can't machine-gun back to back; critical telemetry bypasses it.
 *
 * Pure Kotlin: time comes from the injected [clock]; the gap re-check is delegated to the owner
 * via [onScheduleRecheck] (the scheduler has no timer of its own). All entry points are
 * synchronized — TTS completion callbacks, the vision-rate instruction collector, and the alert
 * seam may call in from different threads.
 */
class VoiceScheduler(
    private val engine: VoiceSpeechEngine,
    private val clock: () -> Long,
) {

    /** Set by the owner: schedule [onGapElapsed] after the given delay (speech-gap re-check). */
    var onScheduleRecheck: ((delayMs: Long) -> Unit)? = null

    private var settings = VoiceSettings()
    private val queue = VoiceQueue()
    private val cooldowns = CooldownTracker(clock)

    private var speaking: VoiceEvent? = null
    private var lastUtteranceEndedAt = 0L
    private var gapRecheckScheduled = false

    // ---- Assistant guidance session (one per unchanged action) ----
    private var currentAction: AssistantAction? = null
    private var bestProgress = 0f
    private var bestProgressAt = 0L
    /** True once the current drift episode has actually been voiced (gates "Reference matched."). */
    private var episodeSpoken = false

    // ---- Observability ----
    private var suppressedCount = 0
    private var lastSuppressedReason: String? = null
    private var lastSpoken: VoiceEvent? = null
    private var spokenCount = 0

    private val _debug = MutableStateFlow(VoiceDebugState())
    val debug: StateFlow<VoiceDebugState> = _debug.asStateFlow()

    fun updateSettings(s: VoiceSettings) {
        if (s == settings) return
        settings = s
        cooldowns.assistantCooldownMs = s.assistantReminderMs
        if (!s.enabled) {
            stopSpeaking()
            queue.clear()
            clearAssistantSession()
        } else {
            if (!s.allows(VoiceSource.ASSISTANT)) {
                queue.removeAll { it.source == VoiceSource.ASSISTANT }
                if (speaking?.source == VoiceSource.ASSISTANT) stopSpeaking()
                clearAssistantSession()
            }
            if (!s.allows(VoiceSource.TELEMETRY)) {
                queue.removeAll { it.source == VoiceSource.TELEMETRY }
                if (speaking?.source == VoiceSource.TELEMETRY) stopSpeaking()
            }
        }
        maybeSpeakNext()
    }

    /**
     * Per-frame assistant verdict (the Phase 13 selector's single instruction, or null while
     * everything matches / monitoring is off). [monitoringActive] distinguishes "all matched"
     * (announce once) from "monitoring stopped" (just go quiet).
     */
    fun onAssistantInstruction(instruction: AssistantInstruction?, monitoringActive: Boolean) {
        val action = instruction?.action?.takeIf { it != AssistantAction.NONE }
        val text = action?.let { VoiceTemplates.forAction(it) }
        if (action == null || text == null) {
            endGuidance(monitoringActive)
            return
        }
        if (!settings.allows(VoiceSource.ASSISTANT)) {
            // Muted: carry no session state, so re-enabling starts a fresh episode.
            if (currentAction != null) {
                queue.removeAll { it.source == VoiceSource.ASSISTANT }
                clearAssistantSession()
                maybeSpeakNext()
            }
            return
        }

        val key = VoiceKey.Assistant(action)
        val now = clock()
        if (action != currentAction) {
            // Action changed: previous guidance is obsolete — cancel it, never speak it late.
            currentAction?.let { prev ->
                queue.removeAll { it.source == VoiceSource.ASSISTANT }
                cooldowns.markDisappeared(VoiceKey.Assistant(prev))
            }
            queue.removeByKey(VoiceKey.ReferenceMatched)
            currentAction = action
            bestProgress = instruction.progress
            bestProgressAt = now
            if (cooldowns.ready(key)) {
                queue.offer(assistantEvent(key, text, now))
            } else {
                suppress("cooldown ${key.label}")
            }
            maybeSpeakNext()
            return
        }

        // Unchanged action: one continuous guidance session. Silent while improving.
        val progress = instruction.progress
        var respeak = false
        if (progress > bestProgress + VoiceCooldowns.PROGRESS_EPSILON) {
            bestProgress = progress
            bestProgressAt = now
        } else if (progress < bestProgress - VoiceCooldowns.PROGRESS_EPSILON) {
            respeak = true // regressing — the error is growing again
        } else if (now - bestProgressAt >= VoiceCooldowns.PROGRESS_STALL_WINDOW_MS) {
            respeak = true // stalled — no meaningful improvement for a while
        }
        if (!respeak) {
            // Periodic reminder backstop: a still-drifting action is re-announced once the
            // user-configurable Assistant Reminder Interval has elapsed since it was last spoken.
            respeak = cooldowns.ready(key)
        }
        if (!respeak) return
        if (speaking?.key == key) return
        // Floor: never repeat the same unchanged instruction sooner than the reminder interval.
        if (!cooldowns.ready(key)) return
        queue.offer(assistantEvent(key, text, now))
        // Restart the trend baseline so the same stall doesn't re-trigger every frame.
        bestProgress = progress
        bestProgressAt = now
        maybeSpeakNext()
    }

    /**
     * Phase 15: a storyboard shot just completed. A one-off positive milestone on the assistant
     * channel — it does not touch the continuous guidance session, obeys the assistant mute, and is
     * cooldown-floored so a burst of completions doesn't machine-gun.
     */
    fun onShotCompleted() {
        if (!settings.allows(VoiceSource.ASSISTANT)) return
        val key = VoiceKey.ShotCompleted
        if (speaking?.key == key || !cooldowns.ready(key)) return
        queue.offer(
            VoiceEvent(
                source = VoiceSource.ASSISTANT,
                priority = VoicePriority.NORMAL,
                key = key,
                text = VoiceTemplates.SHOT_COMPLETED,
                interruptible = true,
                repeatable = true,
                timestamp = clock(),
            ),
        )
        maybeSpeakNext()
    }

    /** One alert from the existing delivery seam (already AlertConfig-gated upstream). */
    fun onTelemetryAlert(alert: Alert) {
        val text = VoiceTemplates.forAlert(alert.type) ?: return // not a voiced type
        if (!settings.allows(VoiceSource.TELEMETRY)) {
            suppress("telemetry muted ${alert.type.name}")
            return
        }
        val key = VoiceKey.Telemetry(alert.type)
        val critical = VoiceTemplates.isCriticalTelemetry(alert.type)
        if (speaking?.key == key) {
            suppress("already speaking ${key.label}")
            return
        }
        if (!critical && !cooldowns.ready(key)) {
            suppress("cooldown ${key.label}")
            return
        }
        queue.offer(
            VoiceEvent(
                source = VoiceSource.TELEMETRY,
                priority = VoiceTemplates.telemetryPriority(alert.type),
                key = key,
                text = text,
                interruptible = !critical,
                repeatable = true,
                timestamp = clock(),
            ),
        )
        maybeSpeakNext()
    }

    /** From the speech backend: the current utterance ran to completion (or failed). */
    fun onUtteranceFinished() {
        if (speaking == null) return // stale callback after stop()/reset()
        speaking = null
        lastUtteranceEndedAt = clock()
        maybeSpeakNext()
    }

    /** From the owner's timer: the minimum speech gap requested earlier has elapsed. */
    fun onGapElapsed() {
        gapRecheckScheduled = false
        maybeSpeakNext()
    }

    /** New session / disconnect: silence everything and forget all voice state. */
    fun reset() {
        stopSpeaking()
        queue.clear()
        cooldowns.reset()
        clearAssistantSession()
        lastUtteranceEndedAt = 0L
        gapRecheckScheduled = false
        suppressedCount = 0
        lastSuppressedReason = null
        lastSpoken = null
        spokenCount = 0
        publish()
    }

    // ---- Internals (all called under the object lock) ----

    private fun assistantEvent(key: VoiceKey, text: String, now: Long) = VoiceEvent(
        source = VoiceSource.ASSISTANT,
        priority = VoicePriority.NORMAL,
        key = key,
        text = text,
        interruptible = true,
        repeatable = true,
        timestamp = now,
    )

    /** The instruction went away: cancel unspoken guidance; announce the resolve if it was voiced. */
    private fun endGuidance(monitoringActive: Boolean) {
        val action = currentAction ?: return
        queue.removeAll { it.source == VoiceSource.ASSISTANT }
        cooldowns.markDisappeared(VoiceKey.Assistant(action))
        val announce = monitoringActive && episodeSpoken &&
            settings.allows(VoiceSource.ASSISTANT) && cooldowns.ready(VoiceKey.ReferenceMatched)
        clearAssistantSession()
        if (announce) {
            queue.offer(
                VoiceEvent(
                    source = VoiceSource.ASSISTANT,
                    priority = VoicePriority.NORMAL,
                    key = VoiceKey.ReferenceMatched,
                    text = VoiceTemplates.REFERENCE_MATCHED,
                    interruptible = true,
                    repeatable = true,
                    timestamp = clock(),
                ),
            )
        }
        maybeSpeakNext()
    }

    private fun clearAssistantSession() {
        currentAction = null
        bestProgress = 0f
        bestProgressAt = 0L
        episodeSpoken = false
    }

    /** Cancels the active utterance without a finished callback (see [VoiceSpeechEngine.stop]). */
    private fun stopSpeaking() {
        if (speaking == null) return
        engine.stop()
        speaking = null
        lastUtteranceEndedAt = clock()
    }

    private fun maybeSpeakNext() {
        val head = queue.peek() ?: run { publish(); return }
        val now = clock()
        val current = speaking
        if (current != null) {
            if (head.priority == VoicePriority.CRITICAL && current.interruptible) {
                stopSpeaking() // critical telemetry interrupts; nothing else ever does
            } else {
                publish()
                return
            }
        }
        if (head.priority != VoicePriority.CRITICAL && lastUtteranceEndedAt != 0L) {
            val sinceEnd = now - lastUtteranceEndedAt
            if (sinceEnd < VoiceCooldowns.MIN_SPEECH_GAP_MS) {
                if (!gapRecheckScheduled) {
                    gapRecheckScheduled = true
                    onScheduleRecheck?.invoke(VoiceCooldowns.MIN_SPEECH_GAP_MS - sinceEnd)
                }
                publish()
                return
            }
        }
        val event = queue.poll() ?: return
        speaking = event
        cooldowns.markSpoken(event.key)
        if (event.source == VoiceSource.ASSISTANT && event.key is VoiceKey.Assistant) {
            episodeSpoken = true
        }
        lastSpoken = event
        spokenCount++
        engine.speak(event)
        publish()
    }

    private fun suppress(reason: String) {
        suppressedCount++
        lastSuppressedReason = reason
        publish()
    }

    private fun publish() {
        _debug.value = VoiceDebugState(
            enabled = settings.enabled,
            speaking = speaking?.text,
            queued = queue.snapshot().map { it.key.label },
            cooldownsRemainingMs = cooldowns.snapshotRemaining().mapKeys { it.key.label },
            suppressedCount = suppressedCount,
            lastSuppressedReason = lastSuppressedReason,
            lastSpoken = lastSpoken?.text,
            spokenCount = spokenCount,
        )
    }
}
