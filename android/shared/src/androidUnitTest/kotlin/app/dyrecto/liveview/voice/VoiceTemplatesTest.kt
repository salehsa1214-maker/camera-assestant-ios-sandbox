package app.dyrecto.liveview.voice

import app.dyrecto.domain.alerts.AlertCategory
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.instructions.AssistantAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM tests for the phrase tables + telemetry voice classification. */
class VoiceTemplatesTest {

    @Test
    fun `every assistant action except NONE has a short phrase`() {
        for (action in AssistantAction.entries) {
            val phrase = VoiceTemplates.forAction(action)
            if (action == AssistantAction.NONE) {
                assertNull(phrase)
            } else {
                assertNotNull("no phrase for $action", phrase)
                assertTrue("phrase for $action must end as one sentence", phrase!!.endsWith("."))
            }
        }
    }

    @Test
    fun `critical telemetry set matches the interrupt spec`() {
        val critical = AlertType.entries.filter { VoiceTemplates.isCriticalTelemetry(it) }.toSet()
        assertEquals(
            setOf(
                AlertType.RECORDING_STOPPED,
                AlertType.BATTERY_LOW_10,
                AlertType.BATTERY_LOW_5,
                AlertType.BATTERY_TIME_5_MIN,
                AlertType.CARD_REMOVED,
                AlertType.OVERHEATING,
                AlertType.CONNECTION_LOST,
            ),
            critical,
        )
    }

    @Test
    fun `critical telemetry maps to CRITICAL priority, the rest to HIGH`() {
        for (type in AlertType.entries) {
            val expected =
                if (VoiceTemplates.isCriticalTelemetry(type)) VoicePriority.CRITICAL
                else VoicePriority.HIGH
            assertEquals(expected, VoiceTemplates.telemetryPriority(type))
        }
    }

    @Test
    fun `reference drift alerts and recoveries are never voiced`() {
        // Voiced reference-category exceptions: camera-authoritative alerts with no corrective
        // assistant action to double them (storyboard reminder + camera-settings drift).
        val voicedReferenceAlerts = setOf(
            AlertType.STORYBOARD_INCOMPLETE,
            AlertType.SETTINGS_DRIFT,
            AlertType.PICTURE_PROFILE_CHANGED,
            AlertType.FRAME_RATE_MISMATCH,
        )
        for (type in AlertType.entries) {
            if (type in voicedReferenceAlerts) continue
            if (type.category == AlertCategory.REFERENCE) {
                assertNull("reference alert $type must be visual-only", VoiceTemplates.forAlert(type))
            }
        }
        assertNull(VoiceTemplates.forAlert(AlertType.HIGHLIGHT_RECOVERED))
        assertNull(VoiceTemplates.forAlert(AlertType.SHADOW_RECOVERED))
        assertNull(VoiceTemplates.forAlert(AlertType.CARD_INSERTED))
    }

    @Test
    fun `storyboard incomplete reminder is voiced and is a warning`() {
        val phrase = VoiceTemplates.forAlert(AlertType.STORYBOARD_INCOMPLETE)
        assertNotNull("storyboard reminder must be spoken", phrase)
        assertTrue(phrase!!.endsWith("."))
        // Not critical — it must not interrupt, and it obeys the telemetry cooldown.
        assertFalse(VoiceTemplates.isCriticalTelemetry(AlertType.STORYBOARD_INCOMPLETE))
        assertEquals(
            app.dyrecto.domain.alerts.AlertSeverity.WARNING,
            AlertType.STORYBOARD_INCOMPLETE.defaultSeverity,
        )
    }

    @Test
    fun `every critical telemetry type has a phrase`() {
        for (type in AlertType.entries.filter { VoiceTemplates.isCriticalTelemetry(it) }) {
            assertNotNull("critical $type must be speakable", VoiceTemplates.forAlert(type))
        }
    }

    @Test
    fun `voice cooldowns are zero only for critical telemetry`() {
        for (type in AlertType.entries) {
            val cooldown = VoiceCooldowns.forTelemetry(type)
            if (VoiceTemplates.isCriticalTelemetry(type)) {
                assertEquals(0L, cooldown)
            } else {
                assertFalse(cooldown == 0L)
            }
        }
    }
}
