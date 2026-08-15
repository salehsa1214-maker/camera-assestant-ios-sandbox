package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for [VoiceScheduler] — the WHEN rules: settings gate, interruption, cooldowns,
 * continuous guidance sessions, progress silence, obsolete removal, overlap, and the speech gap.
 */
class VoiceSchedulerTest {

    // ---- Settings gate ----

    @Test
    fun `disabled speaks nothing`() {
        val d = VoiceDriver(VoiceSettings(enabled = false))
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.alert(AlertType.RECORDING_STOPPED)
        assertEquals(0, d.engine.spoken.size)
    }

    @Test
    fun `assistant only ignores telemetry`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, mode = VoiceMode.ASSISTANT_ONLY))
        d.alert(AlertType.RECORDING_STOPPED)
        d.assistant(AssistantAction.MOVE_CLOSER)
        assertEquals(listOf("Move closer."), d.spokenTexts)
    }

    @Test
    fun `telemetry only ignores assistant`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, mode = VoiceMode.TELEMETRY_ONLY))
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.alert(AlertType.BATTERY_LOW_20)
        assertEquals(listOf("Battery low."), d.spokenTexts)
    }

    @Test
    fun `both enabled accepts both sources`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.finish()
        d.advance(1_000)
        d.alert(AlertType.BATTERY_LOW_20)
        assertEquals(listOf("Move closer.", "Battery low."), d.spokenTexts)
    }

    @Test
    fun `disabling mid-utterance stops speech and clears the queue`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.alert(AlertType.BATTERY_LOW_20) // queued behind the active utterance
        d.scheduler.updateSettings(VoiceSettings(enabled = false))
        assertEquals(1, d.engine.stops)
        d.advance(10_000)
        assertEquals(1, d.engine.spoken.size) // nothing else ever spoken
    }

    // ---- Interrupt rules ----

    @Test
    fun `critical telemetry interrupts assistant speech`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        assertEquals(listOf("Move closer."), d.spokenTexts)
        d.alert(AlertType.RECORDING_STOPPED)
        assertEquals(1, d.engine.stops)
        assertEquals(listOf("Move closer.", "Recording stopped."), d.spokenTexts)
    }

    @Test
    fun `assistant never interrupts critical telemetry`() {
        val d = VoiceDriver()
        d.alert(AlertType.CONNECTION_LOST)
        d.assistant(AssistantAction.MOVE_CLOSER)
        assertEquals(0, d.engine.stops)
        assertEquals(listOf("Camera disconnected."), d.spokenTexts)
        // Guidance waits for completion + gap, then speaks.
        d.finish()
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS)
        assertEquals(listOf("Camera disconnected.", "Move closer."), d.spokenTexts)
    }

    @Test
    fun `critical does not interrupt another critical`() {
        val d = VoiceDriver()
        d.alert(AlertType.CONNECTION_LOST)
        d.alert(AlertType.RECORDING_STOPPED)
        assertEquals(0, d.engine.stops)
        assertEquals(listOf("Camera disconnected."), d.spokenTexts)
        d.finish() // second critical follows immediately — no gap for critical
        assertEquals(listOf("Camera disconnected.", "Recording stopped."), d.spokenTexts)
    }

    // ---- Overlap ----

    @Test
    fun `no overlapping speech — next waits for finish`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.alert(AlertType.BATTERY_LOW_20) // HIGH but not critical: must wait
        assertEquals(1, d.engine.spoken.size)
        d.finish()
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS)
        assertEquals(2, d.engine.spoken.size)
    }

    // ---- Minimum speech gap ----

    @Test
    fun `different non-critical messages respect the speech gap`() {
        val d = VoiceDriver()
        d.alert(AlertType.BATTERY_LOW_20)
        d.finish()
        d.alert(AlertType.HIGHLIGHT_CLIPPING) // different key, straight after
        assertEquals(1, d.engine.spoken.size) // held by the gap
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS)
        assertEquals(2, d.engine.spoken.size)
    }

    @Test
    fun `critical telemetry bypasses the speech gap`() {
        val d = VoiceDriver()
        d.alert(AlertType.BATTERY_LOW_20)
        d.finish()
        d.alert(AlertType.RECORDING_STOPPED)
        assertEquals(listOf("Battery low.", "Recording stopped."), d.spokenTexts)
    }

    // ---- Telemetry cooldown + duplicate suppression ----

    @Test
    fun `same telemetry alert within cooldown is suppressed`() {
        val d = VoiceDriver()
        d.alert(AlertType.BATTERY_LOW_20)
        d.finish()
        d.advance(1_000)
        d.alert(AlertType.BATTERY_LOW_20)
        assertEquals(1, d.engine.spoken.size)
        assertTrue(d.scheduler.debug.value.suppressedCount > 0)
    }

    @Test
    fun `same telemetry alert re-speaks after cooldown`() {
        val d = VoiceDriver()
        d.alert(AlertType.BATTERY_LOW_20)
        d.finish()
        d.advance(VoiceCooldowns.TELEMETRY_DEFAULT_MS)
        d.alert(AlertType.BATTERY_LOW_20)
        assertEquals(2, d.engine.spoken.size)
    }

    @Test
    fun `critical telemetry has no cooldown on repeated state changes`() {
        val d = VoiceDriver()
        d.alert(AlertType.RECORDING_STOPPED)
        d.finish()
        d.advance(100)
        d.alert(AlertType.RECORDING_STOPPED) // recording flapped: stopped again
        assertEquals(2, d.engine.spoken.size)
    }

    // ---- Continuous assistant guidance sessions ----

    @Test
    fun `improving action stays silent within the reminder interval`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 15))
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.4f)
        d.finish()
        // Progress keeps improving; within the 15s interval the assistant stays silent.
        var progress = 0.4f
        repeat(10) {
            d.advance(500)
            progress += 0.03f
            d.assistant(AssistantAction.MOVE_CLOSER, progress = progress)
        }
        assertEquals(1, d.engine.spoken.size)
    }

    @Test
    fun `stalled progress re-speaks after the reminder interval`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 3))
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.4f)
        d.finish()
        d.advance(3_000) // stall window elapsed AND the 3s reminder floor reached
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.4f) // no improvement
        assertEquals(2, d.engine.spoken.size)
    }

    @Test
    fun `regressing progress re-speaks after the reminder interval`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 3))
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.6f)
        d.finish()
        d.advance(3_000)
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.4f) // error growing again
        assertEquals(2, d.engine.spoken.size)
    }

    @Test
    fun `reminder interval re-speaks an unchanged improving action`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 3))
        d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.10f)
        d.finish()
        // Improving the whole time, but the reminder fires once the 3s interval elapses.
        var progress = 0.10f
        var spokenAt2 = false
        repeat(6) {
            d.advance(1_000)
            progress += 0.05f
            d.assistant(AssistantAction.MOVE_CLOSER, progress = progress)
            if (d.engine.spoken.size == 2) spokenAt2 = true
        }
        assertTrue(spokenAt2)
    }

    @Test
    fun `assistant reminder interval is user configurable`() {
        // Same improving sequence, different intervals: the short one reminds, the long one waits.
        val short = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 2))
        val long = VoiceDriver(VoiceSettings(enabled = true, assistantReminderSeconds = 10))
        for (d in listOf(short, long)) {
            d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.40f)
            d.finish()
            d.advance(3_000) // 3s later, still the same action, slightly improved
            d.assistant(AssistantAction.MOVE_CLOSER, progress = 0.45f)
        }
        assertEquals(2, short.engine.spoken.size) // reminded after 2s
        assertEquals(1, long.engine.spoken.size) // still inside the 10s interval → silent
    }

    @Test
    fun `reminder interval never re-speaks telemetry`() {
        val d = VoiceDriver()
        d.alert(AlertType.HIGHLIGHT_CLIPPING)
        d.finish()
        d.advance(30_000) // far past any reminder/cooldown — no new upstream alert, no speech
        assertEquals(1, d.engine.spoken.size)
    }

    // ---- Obsolete guidance ----

    @Test
    fun `queued instruction that disappears is removed unspoken`() {
        val d = VoiceDriver()
        d.alert(AlertType.CONNECTION_LOST) // occupies the voice (non-interruptible)
        d.assistant(AssistantAction.MOVE_CLOSER) // queued behind it
        d.assistant(null, active = false) // instruction gone before it was spoken
        d.finish()
        d.advance(10_000)
        assertEquals(listOf("Camera disconnected."), d.spokenTexts) // never spoken
    }

    @Test
    fun `action change cancels the previous queued guidance`() {
        val d = VoiceDriver()
        d.alert(AlertType.CONNECTION_LOST)
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.assistant(AssistantAction.PAN_LEFT) // supersedes MOVE_CLOSER before it was spoken
        d.finish()
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS)
        assertEquals(listOf("Camera disconnected.", "Pan left slightly."), d.spokenTexts)
    }

    @Test
    fun `instruction that disappeared and returned bypasses its cooldown`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.finish()
        d.advance(200)
        d.assistant(null) // drift resolved — queues "Reference matched." behind the gap
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS) // gap elapses, it speaks
        d.finish()
        // Returns ~1.8s after "Move closer." was spoken — inside its 3s reminder interval, but
        // the disappearance cleared the tracker, so it speaks immediately (after the gap).
        d.advance(VoiceCooldowns.MIN_SPEECH_GAP_MS)
        d.assistant(AssistantAction.MOVE_CLOSER)
        assertEquals(
            listOf("Move closer.", "Reference matched.", "Move closer."),
            d.spokenTexts,
        )
    }

    @Test
    fun `resolved episode announces reference matched once`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.finish()
        d.advance(1_000)
        d.assistant(null) // matched while monitoring stays active
        assertEquals(listOf("Move closer.", "Reference matched."), d.spokenTexts)
        d.finish()
        d.advance(1_000)
        d.assistant(null) // still matched — no repeat
        assertEquals(2, d.engine.spoken.size)
    }

    @Test
    fun `monitoring stop goes quiet without announcing a match`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.finish()
        d.advance(1_000)
        d.assistant(null, active = false) // monitoring stopped, not matched
        assertEquals(listOf("Move closer."), d.spokenTexts)
    }

    @Test
    fun `unspoken episode does not announce reference matched`() {
        val d = VoiceDriver(VoiceSettings(enabled = true, mode = VoiceMode.TELEMETRY_ONLY))
        d.assistant(AssistantAction.MOVE_CLOSER) // muted — never voiced
        d.scheduler.updateSettings(VoiceSettings(enabled = true, mode = VoiceMode.BOTH))
        d.assistant(null) // resolves without ever having been spoken
        assertEquals(0, d.engine.spoken.size)
    }

    // ---- Reset ----

    @Test
    fun `reset silences and clears everything`() {
        val d = VoiceDriver()
        d.assistant(AssistantAction.MOVE_CLOSER)
        d.alert(AlertType.BATTERY_LOW_20)
        d.scheduler.reset()
        assertEquals(1, d.engine.stops)
        val debug = d.scheduler.debug.value
        assertEquals(null, debug.speaking)
        assertEquals(0, debug.queued.size)
        assertEquals(0, debug.spokenCount)
    }
}
