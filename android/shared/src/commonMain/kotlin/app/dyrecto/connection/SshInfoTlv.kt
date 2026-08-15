package app.dyrecto.connection

/**
 * Decoder for the CC17 BluetoothCameraSshInfo read value.
 *
 * Layout (from O3/C2204b.d / P3/C2250a.g, BLE_CHARACTERISTIC_MAP.md):
 *   b[0]            = total length (validator: should equal last index)
 *   b[3]            = SSH On/Off  (0=Unknown, 1=OFF, 2=ON)
 *                       if b[3] != 2 -> no credentials are returned
 *   b[4]            = L_id   -> sshId   = b[5 .. 5+L_id-1]      (ASCII)
 *   idx_p = 5 + L_id
 *   b[idx_p]        = L_pass -> sshPass = b[idx_p+1 ..]         (ASCII)
 *   idx_f = idx_p + 1 + L_pass
 *   b[idx_f]        = L_fp   -> fingerprint = b[idx_f+1 ..]     (ASCII)
 *
 * VERIFIED STACK — carried over from the probe unchanged (package only).
 */
object SshInfoTlv {

    enum class SshState(val code: Int) {
        UNKNOWN(0), OFF(1), ON(2);

        companion object {
            fun from(b: Int) = entries.firstOrNull { it.code == b } ?: UNKNOWN
        }
    }

    data class Result(
        val state: SshState,
        val sshId: String,
        val sshPass: String,
        val fingerprint: String,
        val notes: List<String>,
    )

    fun decode(b: ByteArray): Result {
        val notes = mutableListOf<String>()

        if (b.size < 4) {
            return Result(SshState.UNKNOWN, "", "", "",
                listOf("value too short (${b.size} bytes) to contain SSH header"))
        }

        val declaredLen = b[0].toInt() and 0xFF
        if (declaredLen != b.size - 1 && declaredLen != b.size) {
            notes += "b[0]=$declaredLen does not match actual length ${b.size} (continuing best-effort)"
        }

        val state = SshState.from(b[3].toInt() and 0xFF)
        if (state != SshState.ON) {
            notes += "b[3]=${b[3].toInt() and 0xFF} → SSH is $state; camera returns no credentials in this state"
            return Result(state, "", "", "", notes)
        }

        var idx = 4
        val (sshId, idAfter) = readField(b, idx, "sshId", notes) ?: return fail(state, notes)
        idx = idAfter

        val (sshPass, passAfter) = readField(b, idx, "sshPass", notes) ?: return fail(state, notes)
        idx = passAfter

        val (fp, _) = readField(b, idx, "fingerprint", notes)
            ?: return Result(state, sshId, sshPass, "",
                notes.apply { add("fingerprint field missing/truncated") })

        return Result(state, sshId, sshPass, fp, notes)
    }

    /** Reads [len][bytes] starting at [pos]; returns (value, nextIndex) or null on overrun. */
    private fun readField(
        b: ByteArray,
        pos: Int,
        name: String,
        notes: MutableList<String>,
    ): Pair<String, Int>? {
        if (pos >= b.size) {
            notes += "$name: length byte at index $pos is past end of value"
            return null
        }
        val len = b[pos].toInt() and 0xFF
        val start = pos + 1
        val end = start + len
        if (end > b.size) {
            notes += "$name: declared length $len overruns value (end=$end, size=${b.size})"
            return null
        }
        // US-ASCII decode, KMP-portable: 0x00–0x7F map 1:1; anything else becomes U+FFFD —
        // exactly what the JVM's String(bytes, Charsets.US_ASCII) produced here before.
        val value = buildString(len) {
            for (i in start until end) {
                val byte = b[i].toInt() and 0xFF
                append(if (byte < 0x80) byte.toChar() else '�')
            }
        }
        return value to end
    }

    private fun fail(state: SshState, notes: List<String>) =
        Result(state, "", "", "", notes)
}
