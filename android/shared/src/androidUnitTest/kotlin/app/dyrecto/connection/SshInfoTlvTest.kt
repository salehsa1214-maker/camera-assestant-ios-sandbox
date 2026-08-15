package app.dyrecto.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Added with the KMP move: SshInfoTlv's field decode swapped the JVM
 * `String(bytes, Charsets.US_ASCII)` for a portable loop — these tests pin the TLV layout and
 * prove the portable decode is byte-for-byte equivalent to the JVM US-ASCII decoder it replaced
 * (including the U+FFFD replacement for non-ASCII bytes).
 */
class SshInfoTlvTest {

    /** Builds a CC17 value: header + state + [len][bytes] fields for id/pass/fingerprint. */
    private fun cc17(state: Int, vararg fields: ByteArray): ByteArray {
        val body = ByteArray(3) // b[1], b[2] padding after the length byte
        var v = byteArrayOf(0) + body.copyOfRange(0, 2) + byteArrayOf(state.toByte())
        for (f in fields) v += byteArrayOf(f.size.toByte()) + f
        v[0] = (v.size - 1).toByte() // declared length = last index
        return v
    }

    @Test
    fun decodesOnStateCredentials() {
        val r = SshInfoTlv.decode(
            cc17(2, "user1".encodeToByteArray(), "pw!42".encodeToByteArray(), "AA:BB:CC".encodeToByteArray()),
        )
        assertEquals(SshInfoTlv.SshState.ON, r.state)
        assertEquals("user1", r.sshId)
        assertEquals("pw!42", r.sshPass)
        assertEquals("AA:BB:CC", r.fingerprint)
    }

    @Test
    fun offStateReturnsNoCredentials() {
        val r = SshInfoTlv.decode(cc17(1))
        assertEquals(SshInfoTlv.SshState.OFF, r.state)
        assertEquals("", r.sshId)
        assertTrue(r.notes.isNotEmpty())
    }

    @Test
    fun portableAsciiDecodeMatchesJvmUsAsciiDecoderIncludingNonAscii() {
        // Byte values 0x01..0xFF in one field (255 max — the TLV length is a single byte): the
        // portable decode must equal what the old JVM String(bytes, US_ASCII) produced.
        val raw = ByteArray(255) { (it + 1).toByte() }
        val r = SshInfoTlv.decode(cc17(2, raw, "p".encodeToByteArray(), "f".encodeToByteArray()))
        val jvm = String(raw, Charsets.US_ASCII)
        assertEquals(jvm, r.sshId)
        assertEquals("p", r.sshPass)
        assertEquals("f", r.fingerprint)
    }

    @Test
    fun truncatedFieldIsReportedNotThrown() {
        // Declared field length overruns the value → best-effort failure with a note.
        val v = byteArrayOf(6, 0, 0, 2, 10, 'a'.code.toByte())
        val r = SshInfoTlv.decode(v)
        assertEquals(SshInfoTlv.SshState.ON, r.state)
        assertEquals("", r.sshId)
        assertTrue(r.notes.any { "overruns" in it })
    }
}
