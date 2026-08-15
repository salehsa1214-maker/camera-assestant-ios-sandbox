package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure JVM tests for [VoiceQueue] — deterministic ordering and same-key merging. */
class VoiceQueueTest {

    private fun event(
        key: VoiceKey,
        priority: VoicePriority = VoicePriority.NORMAL,
        text: String = key.label,
        timestamp: Long = 0L,
    ) = VoiceEvent(
        source = if (key is VoiceKey.Telemetry) VoiceSource.TELEMETRY else VoiceSource.ASSISTANT,
        priority = priority,
        key = key,
        text = text,
        interruptible = true,
        repeatable = true,
        timestamp = timestamp,
    )

    private val moveCloser = VoiceKey.Assistant(AssistantAction.MOVE_CLOSER)
    private val panLeft = VoiceKey.Assistant(AssistantAction.PAN_LEFT)
    private val battery = VoiceKey.Telemetry(AlertType.BATTERY_LOW_10)

    @Test
    fun `critical outranks normal regardless of insertion order`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser, VoicePriority.NORMAL, timestamp = 1))
        q.offer(event(battery, VoicePriority.CRITICAL, timestamp = 2))
        assertEquals(battery, q.poll()?.key)
        assertEquals(moveCloser, q.poll()?.key)
    }

    @Test
    fun `equal priority is FIFO by timestamp then insertion`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser, timestamp = 5))
        q.offer(event(panLeft, timestamp = 5))
        assertEquals(moveCloser, q.poll()?.key)
        assertEquals(panLeft, q.poll()?.key)
        assertNull(q.poll())
    }

    @Test
    fun `same key merges — newer text wins, position kept, size stays one`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser, text = "old", timestamp = 1))
        q.offer(event(panLeft, timestamp = 2))
        q.offer(event(moveCloser, text = "new", timestamp = 3))
        assertEquals(2, q.size)
        val first = q.poll()!!
        assertEquals(moveCloser, first.key) // kept its original (front) position
        assertEquals("new", first.text)
    }

    @Test
    fun `same key with different text is one entry — key is identity, text is presentation`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser, text = "Move closer."))
        q.offer(event(moveCloser, text = "Näher herangehen."))
        assertEquals(1, q.size)
    }

    @Test
    fun `removeByKey and removeAll drop matching entries`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser))
        q.offer(event(panLeft))
        q.offer(event(battery, VoicePriority.CRITICAL))
        q.removeByKey(panLeft)
        q.removeAll { it.source == VoiceSource.ASSISTANT }
        assertEquals(1, q.size)
        assertEquals(battery, q.poll()?.key)
    }

    @Test
    fun `snapshot lists events in speak order without draining`() {
        val q = VoiceQueue()
        q.offer(event(moveCloser, VoicePriority.NORMAL, timestamp = 1))
        q.offer(event(battery, VoicePriority.CRITICAL, timestamp = 2))
        assertEquals(listOf(battery, moveCloser), q.snapshot().map { it.key })
        assertEquals(2, q.size)
    }
}
