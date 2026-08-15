package app.dyrecto.liveview.render

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [FrameConflator] — the Android-free core of the live-view renderer. Covers the
 * single-slot "latest frame wins" behavior, drop accounting, and windowed FPS math with a fake clock.
 */
class FrameConflatorTest {

    private fun bytes(vararg v: Int) = v.map { it.toByte() }.toByteArray()

    @Test
    fun takeLatest_returnsNewest_andCountsOverwriteAsDrop() {
        val c = FrameConflator()
        c.submit(bytes(1))
        c.submit(bytes(2)) // overwrites un-taken (1) -> one drop
        c.submit(bytes(3)) // overwrites un-taken (2) -> one drop

        assertArrayEquals(bytes(3), c.takeLatest())
        assertEquals(2, c.droppedFrames)
    }

    @Test
    fun takeLatest_clearsSlot() {
        val c = FrameConflator()
        c.submit(bytes(7))
        assertTrue(c.pending)
        assertArrayEquals(bytes(7), c.takeLatest())
        assertFalse(c.pending)
        assertNull(c.takeLatest())
    }

    @Test
    fun keepingUp_dropsNothing() {
        val c = FrameConflator()
        repeat(5) {
            c.submit(bytes(it))
            c.takeLatest() // consumed before the next submit
        }
        assertEquals(0, c.droppedFrames)
    }

    @Test
    fun snapshot_computesWindowedFps_andAverages() {
        var now = 1_000L
        val c = FrameConflator(clock = { now })

        // 3 received, 2 decoded (10ms, 20ms), 2 displayed (100ms, 140ms) over a 1s window.
        c.submit(bytes(1)); c.submit(bytes(2)); c.submit(bytes(3))
        c.onDecoded(10); c.onDecoded(20)
        c.onDisplayed(100); c.onDisplayed(140)
        now = 2_000L // 1000ms elapsed

        val m = c.snapshot()
        assertEquals(3.0, m.receivedFps, 1e-6)
        assertEquals(2.0, m.decodedFps, 1e-6)
        assertEquals(2.0, m.displayedFps, 1e-6)
        assertEquals(15.0, m.avgDecodeMs, 1e-6)
        assertEquals(120.0, m.avgLatencyMs, 1e-6)
    }

    @Test
    fun snapshot_resetsPerWindowAverages_butDropIsCumulative() {
        var now = 0L
        val c = FrameConflator(clock = { now })
        c.submit(bytes(1)); c.submit(bytes(2)) // 1 drop
        c.onDecoded(50)
        now = 1_000L
        c.snapshot()

        now = 2_000L
        val m = c.snapshot() // empty window
        assertEquals(0.0, m.decodedFps, 1e-6)
        assertEquals(0.0, m.avgDecodeMs, 1e-6) // per-window average reset
        assertEquals(1, m.droppedFrames)       // cumulative
    }

    @Test
    fun reset_zeroesEverything() {
        val c = FrameConflator()
        c.submit(bytes(1)); c.submit(bytes(2))
        c.onDecoded(5)
        c.reset()
        assertFalse(c.pending)
        assertEquals(0, c.droppedFrames)
        assertEquals(0.0, c.snapshot().decodedFps, 1e-6)
    }
}
