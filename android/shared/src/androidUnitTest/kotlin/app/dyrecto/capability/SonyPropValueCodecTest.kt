package app.dyrecto.capability

import org.junit.Assert.assertArrayEquals
import org.junit.Test

/** Pins the little-endian PTP value encoding used by the HW-gated write path. */
class SonyPropValueCodecTest {
    @Test
    fun encodesByDatatypeWidthLittleEndian() {
        assertArrayEquals(byteArrayOf(0x7F), SonyPropValueCodec.encode(0x7F, 0x0002)) // UINT8
        assertArrayEquals(byteArrayOf(0x34, 0x12), SonyPropValueCodec.encode(0x1234, 0x0004)) // UINT16
        assertArrayEquals(
            byteArrayOf(0x78, 0x56, 0x34, 0x12),
            SonyPropValueCodec.encode(0x12345678, 0x0006), // UINT32
        )
        assertArrayEquals(
            byteArrayOf(0x01, 0, 0, 0, 0, 0, 0, 0),
            SonyPropValueCodec.encode(1, 0x0008), // UINT64
        )
    }
}
