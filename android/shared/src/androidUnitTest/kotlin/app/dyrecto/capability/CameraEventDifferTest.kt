package app.dyrecto.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pins the pure snapshot-diff that produces the agnostic CameraEvent stream. */
class CameraEventDifferTest {

    private fun caps(vararg pairs: Pair<Int, Long?>): CameraCapabilities {
        val records = pairs.map { (code, cur) -> RawPropertyRecord(code, 0x0004, 1, 1, cur) }
        return CapabilityMapper.build(CameraInfo(), records)
    }

    @Test
    fun noEventsFromNullBaseline() {
        assertTrue(CameraEventDiffer.diff(null, caps(0xD21E to 100L)).isEmpty())
    }

    @Test
    fun emitsPropertyChangedForChangedValues() {
        val old = caps(0xD21E to 100L, 0x5007 to 280L)
        val new = caps(0xD21E to 200L, 0x5007 to 280L)
        val events = CameraEventDiffer.diff(old, new)
        assertEquals(1, events.size)
        val e = events.single() as CameraEvent.PropertyChanged
        assertEquals(0xD21E, e.code)
        assertEquals(100L, e.oldRaw)
        assertEquals(200L, e.newRaw)
    }

    @Test
    fun newlyAppearingPropertyIsNotAChange() {
        val old = caps(0xD21E to 100L)
        val new = caps(0xD21E to 100L, 0x5007 to 280L)
        assertTrue(CameraEventDiffer.diff(old, new).isEmpty())
    }
}
