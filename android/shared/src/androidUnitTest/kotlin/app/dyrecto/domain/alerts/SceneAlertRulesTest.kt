package app.dyrecto.domain.alerts

import app.dyrecto.liveview.scene.ExposureState
import app.dyrecto.liveview.scene.FaceState
import app.dyrecto.liveview.scene.RecordingState
import app.dyrecto.liveview.scene.SceneConnectionState
import app.dyrecto.liveview.scene.SceneContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [SceneAlertRules]: acquisition-gated face tracking and Eyes Not Visible.
 * (Highlight/shadow clipping rules moved to `ExposureAlertRulesTest` in Phase 7.) Lifecycle
 * [SceneAlertState] is threaded by the test exactly as the coordinator does. No Android
 * dependencies are exercised.
 */
class SceneAlertRulesTest {

    private val idGen = AlertIdGenerator()

    /** Drives one tick, returns the alerts, and advances the threaded state. */
    private inner class Driver(var state: SceneAlertState = SceneAlertState()) {
        fun feed(scene: SceneContext): List<Alert> {
            val r = SceneAlertRules.evaluate(state, scene, idGen)
            state = r.state
            return r.alerts
        }
    }

    private fun scene(
        face: Boolean = false,
        eyes: Boolean = false,
        liveView: Boolean = true,
        recording: Boolean = true,
        now: Long = 1_000L,
    ) = SceneContext(
        updatedAtMs = now,
        exposure = ExposureState(),
        face = FaceState(hasVisibleFace = face, hasVisibleEyes = eyes),
        recording = RecordingState(isRecording = recording),
        connection = SceneConnectionState(liveViewActive = liveView),
    )

    private fun types(alerts: List<Alert>) = alerts.map { it.type }

    // ---- baseline ----

    @Test fun firstSnapshotSeedsWithoutFiring() {
        val d = Driver()
        assertTrue(d.feed(scene(face = true)).isEmpty())
    }

    // ---- face acquisition gating ----

    @Test fun faceNeverAcquiredNeverFires() {
        // Empty scene (landscape): live view + recording, but a face never appears.
        val d = Driver()
        d.feed(scene(face = false))
        assertTrue(d.feed(scene(face = false)).isEmpty())
        assertTrue(d.feed(scene(face = false)).isEmpty())
    }

    @Test fun faceLostFiresOnlyAfterAcquisition() {
        val d = Driver()
        d.feed(scene(face = false))        // seed
        assertTrue("acquisition is silent", d.feed(scene(face = true)).isEmpty())
        assertEquals(listOf(AlertType.FACE_LOST), types(d.feed(scene(face = false))))
    }

    @Test fun faceLostDoesNotFireWhenNotRecording() {
        val d = Driver()
        d.feed(scene(face = true, recording = false))  // composing: no acquisition
        assertTrue(d.feed(scene(face = false, recording = false)).isEmpty())
    }

    @Test fun faceLostDoesNotFireWhenLiveViewInactive() {
        val d = Driver()
        d.feed(scene(face = true, liveView = false))
        assertTrue(d.feed(scene(face = false, liveView = false)).isEmpty())
    }

    @Test fun faceLostReArmsRequiringFreshAcquisition() {
        val d = Driver()
        d.feed(scene(face = false))
        d.feed(scene(face = true))                       // acquire
        assertEquals(listOf(AlertType.FACE_LOST), types(d.feed(scene(face = false))))
        // No re-fire on a second face-absent frame: tracking already reset.
        assertTrue(d.feed(scene(face = false)).isEmpty())
        // Re-acquire, then lose again -> fires once more.
        d.feed(scene(face = true))
        assertEquals(listOf(AlertType.FACE_LOST), types(d.feed(scene(face = false))))
    }

    @Test fun trackingResetWhenLiveViewEndsNoFaceLost() {
        val d = Driver()
        d.feed(scene(face = false))
        d.feed(scene(face = true))                       // acquire
        assertTrue("LV end drops tracking silently", d.feed(scene(face = true, liveView = false)).isEmpty())
        // Face now absent but tracking was already dropped -> no Face Lost.
        assertTrue(d.feed(scene(face = false, liveView = false)).isEmpty())
    }

    // ---- eyes not visible ----

    @Test fun eyesNotVisibleFiresOnlyWhileTrackingActive() {
        val d = Driver()
        d.feed(scene(face = false))
        d.feed(scene(face = true, eyes = true))          // acquire with eyes
        assertEquals(
            listOf(AlertType.EYES_NOT_VISIBLE),
            types(d.feed(scene(face = true, eyes = false))),
        )
        assertTrue("still no eyes must not re-fire", d.feed(scene(face = true, eyes = false)).isEmpty())
        d.feed(scene(face = true, eyes = true))          // eyes return re-arm
        assertEquals(
            listOf(AlertType.EYES_NOT_VISIBLE),
            types(d.feed(scene(face = true, eyes = false))),
        )
    }

    @Test fun eyesNotEvaluatedWithoutAcquiredFace() {
        val d = Driver()
        d.feed(scene(face = false, eyes = false))
        // No face acquired -> eyes rule inert.
        assertTrue(d.feed(scene(face = false, eyes = false)).isEmpty())
    }

    @Test fun unchangedSceneDoesNotSpam() {
        val d = Driver()
        d.feed(scene(face = false))
        d.feed(scene(face = true, eyes = true))   // acquire with eyes, no alert
        // Repeats of the same tracked-with-eyes state never fire.
        assertTrue(d.feed(scene(face = true, eyes = true)).isEmpty())
    }
}
