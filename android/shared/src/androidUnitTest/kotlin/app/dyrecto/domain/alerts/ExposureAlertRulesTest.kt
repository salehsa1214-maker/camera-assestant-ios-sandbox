package app.dyrecto.domain.alerts

import app.dyrecto.liveview.scene.ExposureState
import app.dyrecto.liveview.scene.SceneContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [ExposureAlertRules]: single-fire on confirmed clipping, recovered fires
 * once and only after a prior confirmed clip, re-arm after a full clip/recover cycle. Lifecycle
 * [ExposureAlertState] is threaded by the test exactly as the coordinator does. Feeds
 * [SceneContext.ExposureState.isHighlightClipped] / `isShadowClipped` directly — the same
 * *confirmed* signal [app.dyrecto.liveview.scene.SceneAnalyzer] now derives from
 * [app.dyrecto.liveview.exposure.ExposureStateMachine] — so no Vision types are
 * constructed here.
 */
class ExposureAlertRulesTest {

    private val idGen = AlertIdGenerator()

    /** Drives one tick, returns the alerts, and advances the threaded state. */
    private inner class Driver(var state: ExposureAlertState = ExposureAlertState()) {
        fun feed(scene: SceneContext): List<Alert> {
            val r = ExposureAlertRules.evaluate(state, scene, idGen)
            state = r.state
            return r.alerts
        }
    }

    private fun scene(
        highlight: Boolean = false,
        shadow: Boolean = false,
        now: Long = 1_000L,
    ) = SceneContext(
        updatedAtMs = now,
        exposure = ExposureState(isHighlightClipped = highlight, isShadowClipped = shadow),
    )

    private fun types(alerts: List<Alert>) = alerts.map { it.type }

    // ---- baseline ----

    @Test fun firstSnapshotSeedsWithoutFiring() {
        val d = Driver()
        assertTrue(d.feed(scene(highlight = true, shadow = true)).isEmpty())
    }

    // ---- highlight clipping / recovery ----

    @Test fun highlightFiresOnceThenRecoversOnceThenReArms() {
        val d = Driver()
        d.feed(scene(highlight = false)) // seed
        assertEquals(listOf(AlertType.HIGHLIGHT_CLIPPING), types(d.feed(scene(highlight = true))))
        assertTrue("still confirmed must not re-fire", d.feed(scene(highlight = true)).isEmpty())
        assertEquals(listOf(AlertType.HIGHLIGHT_RECOVERED), types(d.feed(scene(highlight = false))))
        assertTrue("already recovered must not re-fire", d.feed(scene(highlight = false)).isEmpty())
        // Re-arm: a fresh clip/recover cycle fires both again.
        assertEquals(listOf(AlertType.HIGHLIGHT_CLIPPING), types(d.feed(scene(highlight = true))))
        assertEquals(listOf(AlertType.HIGHLIGHT_RECOVERED), types(d.feed(scene(highlight = false))))
    }

    @Test fun highlightRecoveredNeverFiresWithoutPriorConfirmedClip() {
        val d = Driver()
        d.feed(scene(highlight = false)) // seed, never clipped
        assertTrue(d.feed(scene(highlight = false)).isEmpty())
        assertTrue(d.feed(scene(highlight = false)).isEmpty())
    }

    // ---- shadow detail loss / recovery ----

    @Test fun shadowFiresOnceThenRecoversOnceThenReArms() {
        val d = Driver()
        d.feed(scene(shadow = false))
        assertEquals(listOf(AlertType.SHADOW_CLIPPING), types(d.feed(scene(shadow = true))))
        assertTrue(d.feed(scene(shadow = true)).isEmpty())
        assertEquals(listOf(AlertType.SHADOW_RECOVERED), types(d.feed(scene(shadow = false))))
        assertTrue(d.feed(scene(shadow = false)).isEmpty())
        assertEquals(listOf(AlertType.SHADOW_CLIPPING), types(d.feed(scene(shadow = true))))
        assertEquals(listOf(AlertType.SHADOW_RECOVERED), types(d.feed(scene(shadow = false))))
    }

    @Test fun shadowRecoveredNeverFiresWithoutPriorConfirmedClip() {
        val d = Driver()
        d.feed(scene(shadow = false))
        assertTrue(d.feed(scene(shadow = false)).isEmpty())
    }

    // ---- coexistence & seeding already-confirmed state ----

    @Test fun bothChannelsFireIndependentlyInOneTick() {
        val d = Driver()
        d.feed(scene(highlight = false, shadow = false))
        val alerts = d.feed(scene(highlight = true, shadow = true))
        assertEquals(2, alerts.size)
        assertTrue(alerts.map { it.type }.containsAll(
            listOf(AlertType.HIGHLIGHT_CLIPPING, AlertType.SHADOW_CLIPPING),
        ))
    }

    @Test fun alreadyConfirmedAtSeedDoesNotBurstThenRecoversOnce() {
        val d = Driver()
        // Both channels already confirmed the moment Live View starts: seed silently.
        d.feed(scene(highlight = true, shadow = true))
        assertTrue(d.feed(scene(highlight = true, shadow = true)).isEmpty())
        // Recovery still fires exactly once since the latch was seeded true.
        val alerts = d.feed(scene(highlight = false, shadow = false))
        assertEquals(2, alerts.size)
        assertTrue(alerts.map { it.type }.containsAll(
            listOf(AlertType.HIGHLIGHT_RECOVERED, AlertType.SHADOW_RECOVERED),
        ))
    }

    @Test fun unchangedConfirmedStateDoesNotSpam() {
        val d = Driver()
        d.feed(scene(highlight = true, shadow = true)) // seed (already confirmed) -> latches set true
        // Repeats of the same confirmed state never fire (seeded latches stay set).
        assertTrue(d.feed(scene(highlight = true, shadow = true)).isEmpty())
        assertTrue(d.feed(scene(highlight = true, shadow = true)).isEmpty())
    }
}
