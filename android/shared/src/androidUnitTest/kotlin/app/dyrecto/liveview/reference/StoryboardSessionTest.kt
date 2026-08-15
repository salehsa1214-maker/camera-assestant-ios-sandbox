package app.dyrecto.liveview.reference

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 15: the storyboard (multi-shot) operations on [ReferenceSession] — the persistence-shaping
 * helpers the monitor and UI rely on. Pure data, no Android.
 */
class StoryboardSessionTest {

    private fun profile(id: String, completed: Boolean = false, confirmations: Int = 0) =
        ReferenceProfile(
            id = id,
            name = "Shot $id",
            createdAtMs = 1L,
            imageUri = "file:///data/$id.jpg",
            width = 1280,
            height = 720,
            exposure = ReferenceExposureProfile(120f, 118f, 231f, 245f, 2.5f, 0.4f),
            color = ReferenceColorProfile(140f, 130f, 120f, 20f, -5f),
            subject = null,
            face = null,
            options = ReferenceMonitorOptions(),
            completion = ShotCompletion(completed = completed, confirmations = confirmations),
        )

    private fun session(vararg ids: String) = ReferenceSession(
        id = "s1",
        createdAtMs = 1L,
        profiles = ids.map { profile(it) },
        activeProfileId = ids.firstOrNull(),
    )

    @Test
    fun `addProfile appends and the first added becomes active`() {
        val empty = ReferenceSession(id = "s1", createdAtMs = 1L)
        val one = empty.addProfile(profile("a"))
        assertEquals(listOf("a"), one.profiles.map { it.id })
        assertEquals("a", one.activeProfileId)

        val two = one.addProfile(profile("b"))
        assertEquals(listOf("a", "b"), two.profiles.map { it.id })
        assertEquals("a", two.activeProfileId) // active unchanged when appending
    }

    @Test
    fun `removeProfile drops the shot and re-points active when needed`() {
        val s = session("a", "b", "c")
        val afterA = s.removeProfile("a")
        assertEquals(listOf("b", "c"), afterA.profiles.map { it.id })
        assertEquals("b", afterA.activeProfileId) // was "a" → first remaining

        val afterC = s.removeProfile("c")
        assertEquals("a", afterC.activeProfileId) // active "a" untouched

        val empty = session("a").removeProfile("a")
        assertTrue(empty.profiles.isEmpty())
        assertNull(empty.activeProfileId)
    }

    @Test
    fun `withReplacedProfile preserves order and re-points active`() {
        val s = session("a", "b", "c")
        val replaced = s.withReplacedProfile("b", profile("b2"))
        assertEquals(listOf("a", "b2", "c"), replaced.profiles.map { it.id })
        assertEquals("a", replaced.activeProfileId)

        val replacedActive = s.withReplacedProfile("a", profile("a2"))
        assertEquals("a2", replacedActive.activeProfileId)
    }

    @Test
    fun `withOptionsForAll applies to every shot`() {
        val s = session("a", "b", "c")
        val opts = ReferenceMonitorOptions(monitorExposure = false, monitorWhiteBalance = false)
        val updated = s.withOptionsForAll(opts)
        assertTrue(updated.profiles.all { it.options == opts })
    }

    @Test
    fun `completedCount and progress reset`() {
        val s = ReferenceSession(
            id = "s1",
            createdAtMs = 1L,
            profiles = listOf(
                profile("a", completed = true, confirmations = 1),
                profile("b", completed = false, confirmations = 0),
                profile("c", completed = true, confirmations = 2),
            ),
        )
        assertEquals(2, s.completedCount)

        val reset = s.withProgressReset()
        assertEquals(0, reset.completedCount)
        assertTrue(reset.profiles.all { !it.completion.completed && it.completion.confirmations == 0 })
    }

    @Test
    fun `completion rule is carried on the session and round-trips`() {
        val s = session("a").withCompletionRule(StoryboardCompletionRule(holdSeconds = 8, confirmCount = 3))
        assertEquals(8, s.completionRule.holdSeconds)
        assertEquals(3, s.completionRule.confirmCount)
        val decoded = ReferenceProfileCodec.decodeSession(ReferenceProfileCodec.encodeSession(s))
        assertEquals(s.completionRule, decoded!!.completionRule)
        // Completion state persists per shot too.
        assertFalse(decoded.profiles.first().completion.completed)
    }
}
