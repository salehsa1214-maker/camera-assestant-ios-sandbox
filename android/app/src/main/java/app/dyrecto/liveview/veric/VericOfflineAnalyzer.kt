package app.dyrecto.liveview.veric

import app.dyrecto.connection.BleLog
import java.io.File
import java.io.InputStream

/** Summary statistics from an offline parse of a captured VERIC stream. */
data class VericReport(
    val totalFrames: Int,
    val totalBytes: Long,
    val avgJpegSize: Int,
    val minJpegSize: Int,
    val maxJpegSize: Int,
    val parseErrors: Int,
    /** Offline captures have no real-time cadence, so these are only set when a duration is known. */
    val durationMs: Long? = null,
    val fpsEstimate: Double? = null,
    val note: String = "",
) {
    fun pretty(): String = buildString {
        appendLine("VERIC offline report:")
        appendLine("  frames        = $totalFrames")
        appendLine("  stream bytes  = $totalBytes")
        appendLine("  jpeg size     = avg ${avgJpegSize}B, min ${minJpegSize}B, max ${maxJpegSize}B")
        appendLine("  parse errors  = $parseErrors")
        appendLine("  duration      = ${durationMs?.let { "${it}ms" } ?: "unknown (offline)"}")
        appendLine("  fps estimate  = ${fpsEstimate?.let { "%.1f".format(it) } ?: "n/a (offline)"}")
        if (note.isNotEmpty()) appendLine("  note          = $note")
    }.trimEnd()
}

/**
 * Offline + on-device validator for the VERIC stream parser.
 *
 * Streams a captured `push_lv_video.bin` through [VericParser] with a stronger, validation-aware
 * frame acceptor (rejects a false `FFD9` from an embedded thumbnail — see [JpegValidation]) and
 * reports frame statistics. Can optionally export each frame as a JPEG plus its opaque header
 * (raw `.bin` + human-readable `.txt`) for reverse-engineering the VERIC envelope.
 *
 * This does NOT decode to Bitmap and does NOT touch the runtime socket path.
 */
object VericOfflineAnalyzer {

    private const val DEFAULT_CAPTURE_NAME = "push_lv_video.bin"

    /**
     * Parses [input] fully, optionally exporting frames to [exportDir].
     *
     * @param exportDir if non-null, writes `frame_NNNN.jpg`, `frame_NNNN.header.bin`,
     *   `frame_NNNN.header.txt` for every emitted frame.
     */
    fun analyze(input: InputStream, exportDir: File? = null): VericReport {
        exportDir?.mkdirs()

        var total = 0
        var min = Int.MAX_VALUE
        var max = 0
        var sum = 0L

        val extractor = VericFrameExtractor(
            onFrame = { ref ->
                total++
                val len = ref.jpegLength
                if (len < min) min = len
                if (len > max) max = len
                sum += len
                if (exportDir != null) exportFrame(exportDir, ref.materialize())
            },
            onError = { reason -> BleLog.line(BleLog.Kind.INFO, "[VERIC] offline malformed: $reason") },
            acceptFrame = { buf, start, end -> JpegValidation.isCompleteJpeg(buf, start, end) },
        )

        val buf = ByteArray(64 * 1024)
        var streamBytes = 0L
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            if (n == 0) continue
            streamBytes += n
            extractor.append(buf, 0, n)
        }

        return VericReport(
            totalFrames = total,
            totalBytes = streamBytes,
            avgJpegSize = if (total > 0) (sum / total).toInt() else 0,
            minJpegSize = if (total > 0) min else 0,
            maxJpegSize = max,
            parseErrors = extractor.errors,
            note = "offline static capture — fps/duration are not measurable without per-frame PTS",
        )
    }

    /**
     * Reads `captureDir/push_lv_video.bin`, runs [analyze], logs the report under `[VERIC]`, and
     * optionally exports frames. Safe to call from a background thread on device.
     */
    fun runOnDevice(captureDir: File, exportDir: File? = null): VericReport {
        val file = File(captureDir, DEFAULT_CAPTURE_NAME)
        if (!file.exists()) {
            val msg = "capture not found: ${file.absolutePath} — run Push LV first"
            BleLog.line(BleLog.Kind.ERROR, "[VERIC] $msg")
            return VericReport(0, 0, 0, 0, 0, 0, note = msg)
        }
        BleLog.line(BleLog.Kind.INFO, "[VERIC] analyzing ${file.absolutePath} (${file.length()}B)")
        val report = file.inputStream().use { analyze(it, exportDir) }
        report.pretty().lineSequence().forEach { BleLog.line(BleLog.Kind.INFO, "[VERIC] $it") }
        return report
    }

    private fun exportFrame(dir: File, frame: VericFrame) {
        val n = "%04d".format(frame.index + 1)
        runCatching { File(dir, "frame_$n.jpg").writeBytes(frame.jpeg) }
        runCatching { File(dir, "frame_$n.header.bin").writeBytes(frame.header) }
        runCatching { File(dir, "frame_$n.header.txt").writeText(headerDump(frame)) }
    }

    /** Human-readable header dump: length, VERIC magic, hex + ASCII columns. RE convenience only. */
    fun headerDump(frame: VericFrame): String = buildString {
        val h = frame.header
        appendLine("frame index     : ${frame.index}")
        appendLine("stream offset   : ${frame.streamOffset} (0x%X)".format(frame.streamOffset))
        appendLine("jpeg length     : ${frame.jpeg.size}")
        appendLine("header length   : ${h.size}")
        val magic = indexOfVeric(h)
        appendLine("VERIC magic     : ${if (magic >= 0) "present @ offset $magic" else "absent"}")
        appendLine("hex dump:")
        var i = 0
        while (i < h.size) {
            val end = minOf(i + 16, h.size)
            val hex = StringBuilder()
            val asc = StringBuilder()
            for (j in i until i + 16) {
                if (j < end) {
                    hex.append("%02X ".format(h[j]))
                    val c = h[j].toInt() and 0xFF
                    asc.append(if (c in 0x20..0x7E) c.toChar() else '.')
                } else {
                    hex.append("   ")
                }
            }
            appendLine("  %04X  %s |%s|".format(i, hex.toString().trimEnd(), asc))
            i += 16
        }
    }.trimEnd()

    private val VERIC = "VERIC".toByteArray(Charsets.US_ASCII)

    private fun indexOfVeric(h: ByteArray): Int {
        outer@ for (i in 0..h.size - VERIC.size) {
            for (k in VERIC.indices) if (h[i + k] != VERIC[k]) continue@outer
            return i
        }
        return -1
    }

    /**
     * Desktop entry point. `args[0]` = path to a pulled push_lv_video.bin, `args[1]` (optional) =
     * export directory for per-frame JPEG + header files.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("usage: VericOfflineAnalyzer <push_lv_video.bin> [exportDir]")
            return
        }
        val input = File(args[0])
        if (!input.exists()) {
            println("no such file: ${input.absolutePath}")
            return
        }
        val exportDir = args.getOrNull(1)?.let { File(it) }
        val report = input.inputStream().use { analyze(it, exportDir) }
        println(report.pretty())
        if (exportDir != null) println("exported ${report.totalFrames} frame(s) to ${exportDir.absolutePath}")
    }
}
