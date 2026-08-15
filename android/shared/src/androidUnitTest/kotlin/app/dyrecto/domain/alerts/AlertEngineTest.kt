package app.dyrecto.domain.alerts

import app.dyrecto.camera.CameraDeviceInfo
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.ConnectionPhase
import app.dyrecto.domain.TelemetryProp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [AlertEngine]: transition detection, anti-spam, battery latching, and the
 * Connection Lost gating. No Android dependencies are exercised.
 */
class AlertEngineTest {

    private fun engine() = AlertEngine()

    /** Builds a state carrying the given raw property values (code -> raw number). */
    private fun state(
        vararg props: Pair<Int, Long>,
        connected: Boolean = false,
    ): CameraConnectionState {
        val telemetry = CameraTelemetry(
            props = props.associate { (code, raw) ->
                code to TelemetryProp(
                    code = code,
                    label = CameraTelemetry.labelFor(code),
                    rawValue = raw.toString(),
                    dataType = 0,
                    rawNumber = raw,
                )
            },
        )
        return CameraConnectionState(
            phase = if (connected) ConnectionPhase.DEVICE_INFO else ConnectionPhase.SSH_CONNECTING,
            deviceInfo = if (connected) DUMMY_DEVICE else null,
            telemetry = telemetry,
            lastTelemetryUpdateAt = 1_000L,
        )
    }

    private fun types(alerts: List<Alert>) = alerts.map { it.type }

    // ---- baseline ----

    @Test fun firstSnapshotSeedsWithoutFiring() {
        val e = engine()
        val alerts = e.evaluate(state(CameraTelemetry.MOVIE_REC to 1L))
        assertTrue("first snapshot must not fire", alerts.isEmpty())
    }

    // ---- recording ----

    @Test fun recordingStartAndStop() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.MOVIE_REC to 0L))
        assertEquals(listOf(AlertType.RECORDING_STARTED), types(e.evaluate(state(CameraTelemetry.MOVIE_REC to 1L))))
        assertEquals(listOf(AlertType.RECORDING_STOPPED), types(e.evaluate(state(CameraTelemetry.MOVIE_REC to 0L))))
    }

    @Test fun recordingUnchangedDoesNotSpam() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.MOVIE_REC to 1L))
        assertTrue(e.evaluate(state(CameraTelemetry.MOVIE_REC to 1L)).isEmpty())
    }

    // ---- battery % latching ----

    @Test fun batteryCrosses20OnceThenSilent() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 21L))
        assertEquals(listOf(AlertType.BATTERY_LOW_20), types(e.evaluate(state(CameraTelemetry.BATTERY to 19L))))
        assertTrue("19->18 must not re-fire", e.evaluate(state(CameraTelemetry.BATTERY to 18L)).isEmpty())
    }

    @Test fun batteryCrosses10FiresCritical() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 11L))
        assertEquals(listOf(AlertType.BATTERY_LOW_10), types(e.evaluate(state(CameraTelemetry.BATTERY to 9L))))
        assertTrue("9->8 must not re-fire", e.evaluate(state(CameraTelemetry.BATTERY to 8L)).isEmpty())
    }

    @Test fun rechargeReArmsBatteryLatch() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 21L))
        e.evaluate(state(CameraTelemetry.BATTERY to 19L)) // fires LOW_20
        e.evaluate(state(CameraTelemetry.BATTERY to 50L)) // recovery re-arms
        assertEquals(listOf(AlertType.BATTERY_LOW_20), types(e.evaluate(state(CameraTelemetry.BATTERY to 18L))))
    }

    @Test fun startingLowBatteryDoesNotFireOnSeed() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 5L)) // seed already-low
        assertTrue(e.evaluate(state(CameraTelemetry.BATTERY to 4L)).isEmpty())
    }

    @Test fun batteryBelow5Fires() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 6L))
        assertEquals(listOf(AlertType.BATTERY_LOW_5), types(e.evaluate(state(CameraTelemetry.BATTERY to 4L))))
    }

    @Test fun batteryBelow5DoesNotReFireOnSameLevel() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 6L))
        e.evaluate(state(CameraTelemetry.BATTERY to 4L)) // fires LOW_5
        assertTrue("4->3 must not re-fire", e.evaluate(state(CameraTelemetry.BATTERY to 3L)).isEmpty())
    }

    @Test fun batteryRechargeReArmsBelow5Latch() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 6L))
        e.evaluate(state(CameraTelemetry.BATTERY to 4L)) // fires LOW_5
        e.evaluate(state(CameraTelemetry.BATTERY to 50L)) // recovery
        assertEquals(listOf(AlertType.BATTERY_LOW_5), types(e.evaluate(state(CameraTelemetry.BATTERY to 4L))))
    }

    @Test fun startingBelow5BatteryDoesNotFire() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 3L)) // seed at 3%
        assertTrue(e.evaluate(state(CameraTelemetry.BATTERY to 2L)).isEmpty())
    }

    @Test fun crossing5SkipsLow10And20() {
        // Dropping straight from 6 to 4 should fire only LOW_5, not LOW_10 or LOW_20.
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY to 6L))
        assertEquals(listOf(AlertType.BATTERY_LOW_5), types(e.evaluate(state(CameraTelemetry.BATTERY to 4L))))
    }

    // ---- battery minutes latching ----

    @Test fun batteryTimeBelow10Fires() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 15L))
        val alerts = e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 9L))
        assertEquals(listOf(AlertType.BATTERY_TIME_10_MIN), types(alerts))
        assertEquals("9 min remaining", alerts.single().message)
    }

    @Test fun batteryTimeBelow5Fires() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 9L))
        assertEquals(listOf(AlertType.BATTERY_TIME_5_MIN), types(e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 4L))))
    }

    @Test fun batteryTimeDoesNotReFireOnSameLevel() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 9L))
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 4L)) // fires 5_MIN
        assertTrue(e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 3L)).isEmpty())
    }

    @Test fun startingLowBatteryTimeDoesNotFire() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 3L)) // seed at 3 min
        assertTrue(e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 2L)).isEmpty())
    }

    @Test fun batteryTimeRecoveryReArmsLatch() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 9L))
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 4L)) // fires 5_MIN
        e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 60L)) // recovery
        assertEquals(listOf(AlertType.BATTERY_TIME_5_MIN), types(e.evaluate(state(CameraTelemetry.BATTERY_MINUTES to 4L))))
    }

    // ---- per-slot card ----

    @Test fun perSlotCardRemovalAndInsertion() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L, CameraTelemetry.SLOT2_STATUS to 1L))
        // Slot 2 removed (status 1 OK -> 2 No card)
        val removed = e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L, CameraTelemetry.SLOT2_STATUS to 2L))
        assertEquals(listOf(AlertType.CARD_REMOVED), types(removed))
        assertEquals("Slot 2", removed.single().message)
        // Slot 2 reinserted
        val inserted = e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L, CameraTelemetry.SLOT2_STATUS to 1L))
        assertEquals(listOf(AlertType.CARD_INSERTED), types(inserted))
        assertEquals("Slot 2", inserted.single().message)
    }

    @Test fun cardEnteringErrorStateFiresNoAlert() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L))
        // A card going faulty while still inserted no longer raises its own alert.
        assertTrue(e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 3L)).isEmpty())
    }

    @Test fun cardEnteringLockedStateFiresNoAlert() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L))
        assertTrue(e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 4L)).isEmpty())
    }

    @Test fun cardErrorFromAbsentFiresNoAlert() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 2L)) // no card
        assertTrue(e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 3L)).isEmpty()) // error on insert
    }

    @Test fun cardRecoveryFromErrorFiresCardInserted() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 3L)) // seed in error
        val alerts = e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 1L)) // recovered
        assertEquals(listOf(AlertType.CARD_INSERTED), types(alerts))
    }

    @Test fun cardRemovedFromErrorStateFiresCardRemoved() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 3L)) // seed in error
        val alerts = e.evaluate(state(CameraTelemetry.SLOT1_STATUS to 2L)) // removed
        assertEquals(listOf(AlertType.CARD_REMOVED), types(alerts))
    }

    // ---- thermal ----

    @Test fun overheatingFiresOnTransitionToOverheating() {
        val e = engine()
        e.evaluate(state(CameraTelemetry.OVERHEATING to 0L))
        assertEquals(listOf(AlertType.OVERHEATING), types(e.evaluate(state(CameraTelemetry.OVERHEATING to 2L))))
    }

    // ---- connection lost gating ----

    @Test fun connectionLostNotFiredBeforeEverConnected() {
        val e = engine()
        e.evaluate(state(connected = false)) // disconnected baseline
        // Still never connected -> a disconnected snapshot must not fire.
        assertTrue(e.evaluate(state(connected = false)).isEmpty())
    }

    @Test fun connectionLostFiresAfterSuccessfulConnect() {
        val e = engine()
        e.evaluate(state(connected = true))  // baseline: connected
        val lost = e.evaluate(state(connected = false))
        assertEquals(listOf(AlertType.CONNECTION_LOST), types(lost))
    }

    @Test fun resetClearsState() {
        val e = engine()
        e.evaluate(state(connected = true))
        e.reset()
        // After reset, a disconnected snapshot is a fresh baseline — no Connection Lost.
        e.evaluate(state(connected = false))
        assertTrue(e.evaluate(state(connected = false)).isEmpty())
    }

    private companion object {
        val DUMMY_DEVICE = CameraDeviceInfo(
            manufacturer = "Sony",
            model = "TEST",
            firmwareVersion = "1.0",
            serialNumber = "0",
            standardVersion = 100,
            vendorExtensionId = 0,
            vendorExtensionVersion = 0,
            vendorExtensionDescription = "",
            functionalMode = 0,
            operations = emptyList(),
            supportedEventCount = 0,
            supportedPropertyCount = 0,
            captureFormatCount = 0,
            imageFormatCount = 0,
        )
    }
}
