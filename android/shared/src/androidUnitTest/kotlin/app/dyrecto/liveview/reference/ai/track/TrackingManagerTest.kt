package app.dyrecto.liveview.reference.ai.track

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.ai.SubjectMatcher
import app.dyrecto.liveview.reference.ai.box
import app.dyrecto.liveview.reference.ai.subject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises TrackingManager against a scripted fake ObjectTracker — proving the manager depends
 * only on the interface, never on NCC specifics.
 */
class TrackingManagerTest {

    private class FakeTracker : ObjectTracker {
        override val id = "fake"

        class Handle : TrackHandle

        var nextConfidence = 1f
        var nextBox: NormalizedRect? = null
        var initCount = 0
        var refreshCount = 0
        var releaseCount = 0
        var lastInitBox: NormalizedRect? = null

        override fun init(frame: LumaFrame, box: NormalizedRect): TrackHandle {
            initCount++
            lastInitBox = box
            nextBox = box
            return Handle()
        }

        override fun update(handle: TrackHandle, frame: LumaFrame): TrackUpdate =
            TrackUpdate(nextBox!!, nextConfidence)

        override fun refresh(handle: TrackHandle, frame: LumaFrame, box: NormalizedRect) {
            refreshCount++
            nextBox = box
        }

        override fun release(handle: TrackHandle) {
            releaseCount++
        }
    }

    private val frame = LumaFrame(16, 16, IntArray(256))
    private val expectedBox = box(0.3f, 0.3f, 0.6f, 0.8f)
    private val expectation = SubjectMatcher.Expected(
        category = SubjectCategory.HUMAN,
        boundingBox = expectedBox,
        rawLabel = "person",
    )

    private fun manager(tracker: FakeTracker = FakeTracker()) =
        TrackingManager(tracker) to tracker

    @Test
    fun `no expectation means no detector trigger`() {
        val (manager, _) = manager()
        assertEquals(
            DetectorTrigger.NONE,
            manager.detectorTrigger(monitoringActive = true, nowMs = 0, null, null),
        )
    }

    @Test
    fun `expectation without a track triggers INIT`() {
        val (manager, _) = manager()
        manager.expectSubject(expectation)
        assertEquals(
            DetectorTrigger.INIT,
            manager.detectorTrigger(monitoringActive = true, nowMs = 0, null, null),
        )
    }

    @Test
    fun `matching detection initializes a track with an identity`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        val detection = subject(SubjectCategory.HUMAN, expectedBox, confidence = 0.9f, rawLabel = "person")
        val tracked = manager.onDetections(frame, listOf(detection), nowMs = 1_000)
        assertNotNull(tracked)
        assertTrue(tracked!!.tracked)
        assertTrue(tracked.trackId > 0)
        assertEquals(1, tracker.initCount)
        assertTrue(manager.hasActiveTrack)
        // Fresh verify: no trigger due.
        assertEquals(
            DetectorTrigger.NONE,
            manager.detectorTrigger(monitoringActive = true, nowMs = 1_500, 0.95f, 2f),
        )
    }

    @Test
    fun `tracker frames carry monitoring between detector passes`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        tracker.nextBox = box(0.35f, 0.3f, 0.65f, 0.8f)
        val tracked = manager.onTrackerFrame(frame)
        assertNotNull(tracked)
        assertEquals(0.5f, tracked!!.boundingBox.centerX, 1e-3f)
        assertTrue(tracked.tracked)
    }

    @Test
    fun `persistent low confidence declares loss and asks for re-detection`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        tracker.nextConfidence = 0.1f
        var lastResult = manager.onTrackerFrame(frame)
        repeat(TrackPolicyConfig().lossFramesToDeclare - 1) {
            lastResult = manager.onTrackerFrame(frame)
        }
        assertNull("track should be lost after persistent low confidence", lastResult)
        assertFalse(manager.hasActiveTrack)
        // Lost track → the policy asks to re-detect.
        val trigger = manager.detectorTrigger(monitoringActive = true, nowMs = 1_000, null, null)
        assertEquals(DetectorTrigger.INIT, trigger)
    }

    @Test
    fun `single low-confidence flicker does not lose the track`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        tracker.nextConfidence = 0.1f
        manager.onTrackerFrame(frame)
        tracker.nextConfidence = 0.9f
        val recovered = manager.onTrackerFrame(frame)
        assertNotNull(recovered)
        assertTrue(manager.hasActiveTrack)
    }

    @Test
    fun `detection pass with no match reports subject missing`() {
        val (manager, _) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        val result = manager.onDetections(
            frame,
            listOf(subject(SubjectCategory.ANIMAL, expectedBox, rawLabel = "dog")),
            nowMs = 6_000,
        )
        assertNull(result)
        assertFalse(manager.hasActiveTrack)
    }

    @Test
    fun `verification refreshes the template instead of re-initializing`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        manager.onDetections(
            frame,
            listOf(subject(SubjectCategory.HUMAN, box(0.32f, 0.3f, 0.62f, 0.8f))),
            nowMs = 6_000,
        )
        assertEquals(1, tracker.initCount)
        assertEquals(1, tracker.refreshCount)
    }

    @Test
    fun `verify heartbeat becomes due after the interval`() {
        val (manager, _) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        assertEquals(
            DetectorTrigger.PERIODIC_VERIFY,
            manager.detectorTrigger(monitoringActive = true, nowMs = TrackPolicyConfig().verifyIntervalMs, 0.95f, 1f),
        )
    }

    @Test
    fun `scene change asks for re-detection`() {
        val (manager, _) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        assertEquals(
            DetectorTrigger.SCENE_CHANGE,
            manager.detectorTrigger(monitoringActive = true, nowMs = 1_000, 0.4f, null),
        )
    }

    @Test
    fun `changing the expected category resets the track`() {
        val (manager, tracker) = manager()
        manager.expectSubject(expectation)
        manager.onDetections(frame, listOf(subject(SubjectCategory.HUMAN, expectedBox)), nowMs = 0)
        manager.expectSubject(
            SubjectMatcher.Expected(SubjectCategory.VEHICLE, expectedBox, rawLabel = "car"),
        )
        assertFalse(manager.hasActiveTrack)
        assertEquals(1, tracker.releaseCount)
    }
}
