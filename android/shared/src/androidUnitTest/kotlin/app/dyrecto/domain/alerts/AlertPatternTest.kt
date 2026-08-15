package app.dyrecto.domain.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Model-level validation tests for [AlertPattern]. The supported range is enforced by the data
 * class itself (strict constructor throws), and the lenient [AlertPattern.of] factory clamps —
 * the contract relied on by the persistence/import paths so out-of-range values can't enter the
 * system.
 */
class AlertPatternTest {

    @Test
    fun strictConstructor_acceptsInRange() {
        assertEquals(0, AlertPattern(0).count)
        assertEquals(10, AlertPattern(10).count)
    }

    @Test
    fun strictConstructor_rejectsCountAboveMax() {
        assertThrows(IllegalArgumentException::class.java) { AlertPattern(11) }
    }

    @Test
    fun strictConstructor_rejectsNegativeCount() {
        assertThrows(IllegalArgumentException::class.java) { AlertPattern(-1) }
    }

    @Test
    fun strictConstructor_rejectsIntervalOutOfRange() {
        assertThrows(IllegalArgumentException::class.java) { AlertPattern(1, intervalMs = -1) }
        assertThrows(IllegalArgumentException::class.java) {
            AlertPattern(1, intervalMs = AlertPattern.MAX_INTERVAL_MS + 1)
        }
    }

    @Test
    fun of_clampsCountIntoRange() {
        assertEquals(10, AlertPattern.of(99).count)
        assertEquals(0, AlertPattern.of(-5).count)
        assertEquals(7, AlertPattern.of(7).count)
    }

    @Test
    fun of_clampsIntervalIntoRange() {
        assertEquals(AlertPattern.MAX_INTERVAL_MS, AlertPattern.of(1, intervalMs = 999_999).intervalMs)
        assertEquals(AlertPattern.MIN_INTERVAL_MS, AlertPattern.of(1, intervalMs = -10).intervalMs)
    }
}
