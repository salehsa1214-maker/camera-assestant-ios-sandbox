package app.dyrecto.connection

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tiny append-only event log. Every protocol event is recorded here and mirrored
 * to Logcat (tag FX3BLE). The Developer screen observes [listener] to render lines
 * live; normal users never see it.
 *
 * VERIFIED STACK — carried over from the probe unchanged (package only).
 */
object BleLog {

    enum class Kind { INFO, SCAN, CONN, BOND, SERVICE, READ, WRITE, DESC, NOTIFY, ERROR }

    data class Entry(val ts: Long, val kind: Kind, val msg: String)

    private const val TAG = "FX3BLE"
    private val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val entries = CopyOnWriteArrayList<Entry>()

    /** UI hook; set to receive each new formatted line. */
    @Volatile
    var listener: ((String) -> Unit)? = null

    fun line(kind: Kind, msg: String) {
        val e = Entry(System.currentTimeMillis(), kind, msg)
        entries.add(e)
        val formatted = format(e)
        when (kind) {
            Kind.ERROR -> Log.e(TAG, formatted)
            else -> Log.d(TAG, formatted)
        }
        listener?.invoke(formatted)
    }

    fun info(m: String) = line(Kind.INFO, m)
    fun error(m: String) = line(Kind.ERROR, m)

    /** Helper to log raw bytes consistently as hex + ascii. */
    fun hex(kind: Kind, prefix: String, bytes: ByteArray?) {
        if (bytes == null) {
            line(kind, "$prefix <null>")
            return
        }
        line(kind, "$prefix len=${bytes.size} hex=${toHex(bytes)} ascii=${toAscii(bytes)}")
    }

    fun toHex(bytes: ByteArray): String =
        bytes.joinToString(" ") { "%02X".format(it) }

    private fun toAscii(bytes: ByteArray): String =
        bytes.joinToString("") { b ->
            val c = b.toInt() and 0xFF
            if (c in 0x20..0x7E) c.toChar().toString() else "."
        }

    private fun format(e: Entry): String =
        "${fmt.format(Date(e.ts))} [${e.kind}] ${e.msg}"

    fun dump(): String = entries.joinToString("\n") { format(it) }
}
