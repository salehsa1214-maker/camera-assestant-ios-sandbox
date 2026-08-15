package app.dyrecto.liveview.reference.ai.track

import app.dyrecto.liveview.reference.NormalizedRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NccTemplateTrackerTest {

    private val width = 160
    private val height = 120

    /** Flat background with a textured square whose top-left is at (x0, y0). */
    private fun frameWithSquare(x0: Int, y0: Int, size: Int = 24): LumaFrame {
        val luma = IntArray(width * height) { 20 }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val fx = x0 + x
                val fy = y0 + y
                if (fx in 0 until width && fy in 0 until height) {
                    // Texture so NCC has variance to correlate against.
                    luma[fy * width + fx] = 120 + (x % 8) * 12 + (y % 8) * 6
                }
            }
        }
        return LumaFrame(width, height, luma)
    }

    private fun boxAt(x0: Int, y0: Int, size: Int = 24) = NormalizedRect(
        left = x0.toFloat() / width,
        top = y0.toFloat() / height,
        right = (x0 + size).toFloat() / width,
        bottom = (y0 + size).toFloat() / height,
    )

    @Test
    fun `static subject tracks in place with high confidence`() {
        val tracker = NccTemplateTracker()
        val frame = frameWithSquare(60, 40)
        val handle = tracker.init(frame, boxAt(60, 40))
        assertNotNull(handle)
        val update = tracker.update(handle!!, frame)
        assertTrue("confidence ${update.confidence}", update.confidence > 0.9f)
        assertEquals(boxAt(60, 40).centerX, update.box.centerX, 0.02f)
        assertEquals(boxAt(60, 40).centerY, update.box.centerY, 0.02f)
    }

    @Test
    fun `moving subject is followed`() {
        val tracker = NccTemplateTracker()
        val handle = tracker.init(frameWithSquare(60, 40), boxAt(60, 40))!!
        val moved = tracker.update(handle, frameWithSquare(68, 45))
        assertTrue("confidence ${moved.confidence}", moved.confidence > 0.9f)
        assertEquals(boxAt(68, 45).centerX, moved.box.centerX, 0.02f)
        assertEquals(boxAt(68, 45).centerY, moved.box.centerY, 0.02f)
    }

    @Test
    fun `subject leaving the frame collapses confidence`() {
        val tracker = NccTemplateTracker()
        val handle = tracker.init(frameWithSquare(60, 40), boxAt(60, 40))!!
        val emptyFrame = LumaFrame(width, height, IntArray(width * height) { 20 })
        val update = tracker.update(handle, emptyFrame)
        assertTrue("confidence ${update.confidence}", update.confidence < 0.2f)
    }

    @Test
    fun `refresh re-anchors the template at a new position`() {
        val tracker = NccTemplateTracker()
        val handle = tracker.init(frameWithSquare(60, 40), boxAt(60, 40))!!
        tracker.refresh(handle, frameWithSquare(20, 70), boxAt(20, 70))
        val update = tracker.update(handle, frameWithSquare(22, 72))
        assertTrue(update.confidence > 0.9f)
        assertEquals(boxAt(22, 72).centerX, update.box.centerX, 0.03f)
    }

    @Test
    fun `degenerate box refuses to init`() {
        val tracker = NccTemplateTracker()
        val frame = frameWithSquare(60, 40)
        assertNull(tracker.init(frame, NormalizedRect(0.5f, 0.5f, 0.5f, 0.5f)))
    }
}
