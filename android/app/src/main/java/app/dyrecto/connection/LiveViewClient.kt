package app.dyrecto.connection

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Minimal Sony Live View HTTP client, run over an already-opened transport (a plain TCP socket to
 * the camera in direct mode). Platform-agnostic — knows nothing about Android, SSH, or PTP.
 *
 * Mirrors the verified Sony Monitor & Control startup path
 * (see sony_live_view_startup_analysis.md §7–8):
 *   HTTP GET <path>  →  200  →  endless body of frames, each:
 *     [16-byte little-endian header][reserved/padding][JPEG][focal/frame metadata]
 *   header = uint32 offsetToImage, uint32 imageSize, uint32 offsetToMeta, uint32 metaSize
 *
 * Sony reads the body through `HttpURLConnection`, which transparently decodes
 * `Transfer-Encoding: chunked`. We read the socket directly, so we must de-chunk the body ourselves
 * when the camera uses chunked encoding — otherwise the chunk-size lines corrupt frame alignment
 * after the first chunk (observed as a desync onto a JPEG EOI tail a few frames in).
 *
 * This phase (4A) only extracts the JPEG payload of each frame and hands it to [onJpegFrame];
 * the focal/frame metadata block is read past but NOT parsed (future phases). Newest-frame
 * discipline is the owner's job — this class just emits frames in order as they arrive.
 */
class LiveViewClient(
    private val input: InputStream,
    private val output: OutputStream,
    private val host: String,
    private val path: String,
    /** Invoked in order for each frame. [buf] is reused — copy out what you keep. */
    private val onJpegFrame: (buf: ByteArray, offset: Int, length: Int) -> Unit,
) {

    @Volatile private var running = false

    /** Reused frame buffer; grows only when a frame exceeds the current capacity. */
    private var frameBuf = ByteArray(INITIAL_BUFFER)

    /** Body stream the frame loop reads from: the raw socket, or a de-chunking wrapper. Set after
     *  the response headers are parsed. */
    private var body: InputStream = input

    /**
     * Blocking — call on a dedicated thread. Performs the HTTP GET, validates the 200 response,
     * then loops reading frames until [stop] is called or the stream ends/errors. Never throws out
     * of the loop; on any read error or EOF it returns cleanly so the owner can update state.
     *
     * @return true if at least the HTTP 200 was reached (stream started), false otherwise.
     */
    fun run(): Boolean {
        running = true
        return try {
            sendRequest()
            if (!readHttpResponseHeader()) {
                logResponseBodySnippet()
                err("HTTP response was not 200 — aborting Live View stream")
                return false
            }
            log("HTTP 200 — entering frame loop")
            frameLoop()
            true
        } catch (e: Exception) {
            if (running) err("stream exception: ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    /** Stops the frame loop and closes the streams. Idempotent; safe from any thread. */
    fun stop() {
        running = false
        runCatching { input.close() }
        runCatching { output.close() }
    }

    // -------------------------------------------------------------- HTTP

    private fun sendRequest() {
        // Single continuous stream: keep the connection open (unlike Sony's per-frame "close").
        val req = buildString {
            append("GET ").append(path).append(" HTTP/1.1\r\n")
            append("Host: ").append(host).append("\r\n")
            append("Connection: keep-alive\r\n")
            append("\r\n")
        }
        log("→ GET $path  Host: $host")
        output.write(req.toByteArray(Charsets.US_ASCII))
        output.flush()
    }

    /**
     * Reads the HTTP status line + headers up to (and including) the blank CRLF line, byte by byte
     * (the body that follows is binary and must not be buffered through a reader). Logs every header
     * and selects the body decoder (raw vs chunked). Returns true iff the status code is 200.
     */
    private fun readHttpResponseHeader(): Boolean {
        val statusLine = readLine() ?: run { err("no HTTP status line"); return false }
        log("← $statusLine")
        val ok = statusLine.startsWith("HTTP/") && statusLine.contains(" 200")

        var chunked = false
        while (true) {
            val line = readLine() ?: break
            if (line.isEmpty()) break
            log("←   $line")
            val lower = line.lowercase()
            if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) chunked = true
        }

        body = if (chunked) {
            log("body uses Transfer-Encoding: chunked — de-chunking enabled")
            ChunkedInputStream(input)
        } else {
            input
        }
        return ok
    }

    /** Best-effort: reads and logs a short snippet of the (error) response body for diagnostics. */
    private fun logResponseBodySnippet() {
        runCatching {
            val snippet = ByteArray(256)
            var n = 0
            while (n < snippet.size) {
                val r = body.read(snippet, n, snippet.size - n)
                if (r < 0) break
                n += r
            }
            if (n > 0) {
                val text = String(snippet, 0, n, Charsets.US_ASCII)
                    .replace(Regex("[^\\x20-\\x7E]"), ".")
                log("← body[$n]: $text")
            }
        }
    }

    /** Reads one CRLF-terminated header line as ASCII (without the trailing CRLF). Null on EOF. */
    private fun readLine(): String? {
        val sb = StringBuilder()
        while (true) {
            val b = input.read()
            if (b < 0) return if (sb.isEmpty()) null else sb.toString()
            if (b == '\n'.code) {
                if (sb.isNotEmpty() && sb.last() == '\r') sb.setLength(sb.length - 1)
                return sb.toString()
            }
            sb.append(b.toChar())
        }
    }

    // -------------------------------------------------------------- frames

    private fun frameLoop() {
        var seq = 0L
        var totalBytes = 0L
        while (running) {
            val header = readFully(16) ?: run {
                if (running) log("frame header EOF — stream ended")
                return
            }
            val headerHex = if (seq < 3) header.copyOf(16).joinToString(" ") { "%02X".format(it) } else null
            val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
            val offsetToImage = bb.int.toLong() and 0xFFFFFFFFL
            val imageSize = bb.int.toLong() and 0xFFFFFFFFL
            val offsetToMeta = bb.int.toLong() and 0xFFFFFFFFL
            val metaSize = bb.int.toLong() and 0xFFFFFFFFL

            // Sanity-check the offsets so a desync can't drive a huge allocation/read.
            if (offsetToImage < 16 || imageSize <= 0 || imageSize > MAX_FRAME ||
                metaSize < 0 || metaSize > MAX_FRAME
            ) {
                err("bad frame header at byteOffset=$totalBytes (after $seq good frames): " +
                    "imgOff=$offsetToImage imgSize=$imageSize metaOff=$offsetToMeta metaSize=$metaSize" +
                    (headerHex?.let { " hdr=[$it]" } ?: "") + " — aborting")
                return
            }
            totalBytes += 16

            // Reserved/padding sits between the 16-byte header and whichever block comes first.
            val firstOffset = if (offsetToMeta in 1 until offsetToImage) offsetToMeta else offsetToImage
            val reserved = (firstOffset - 16).coerceAtLeast(0)
            val bodyLen = reserved + imageSize + metaSize
            if (bodyLen > MAX_FRAME) {
                err("frame body too large ($bodyLen bytes) — aborting")
                return
            }

            val frame = readFully(bodyLen.toInt()) ?: run {
                if (running) log("frame body EOF — stream ended")
                return
            }
            totalBytes += bodyLen

            // JPEG begins at its own declared offset — NOT necessarily right after the reserved block.
            // Some bodies (e.g. Sony A7 V) place the metadata block BEFORE the image, so the image sits
            // past both the reserved padding and the meta block. Derive the offset from offsetToImage so
            // both orderings work. For image-first bodies (e.g. FX3A) offsetToImage-16 == reserved, so
            // this is byte-identical to the previous behavior. Offsets are relative to the frame start,
            // hence the -16 for the header already consumed.
            val jpegOffset = (offsetToImage - 16).toInt()
            onJpegFrame(frame, jpegOffset, imageSize.toInt())
            seq++
            if (seq <= 3 || seq % 100L == 0L) {
                log("frame #$seq jpeg=$imageSize bytes (imgOff=$offsetToImage reserved=$reserved " +
                    "metaOff=$offsetToMeta meta=$metaSize)" + (headerHex?.let { " hdr=[$it]" } ?: ""))
            }
        }
    }

    /**
     * Reads exactly [n] bytes into the reused [frameBuf] (growing it if needed) and returns it.
     * Returns null on EOF/short read. The returned array is the shared buffer — only [0, n) is
     * valid and it is overwritten on the next call.
     */
    private fun readFully(n: Int): ByteArray? {
        if (n > frameBuf.size) frameBuf = ByteArray(n)
        var off = 0
        while (off < n) {
            val r = try {
                body.read(frameBuf, off, n - off)
            } catch (e: Exception) {
                if (running) err("read error after $off/$n bytes: ${e.message}")
                return null
            }
            if (r < 0) return null
            off += r
        }
        return frameBuf
    }

    private fun log(m: String) = BleLog.line(BleLog.Kind.INFO, "[LV] $m")
    private fun err(m: String) = BleLog.error("[LV] $m")

    /**
     * Decodes HTTP/1.1 `Transfer-Encoding: chunked` bodies: `<hex-size>[;ext]\r\n<data>\r\n` repeated,
     * terminated by a zero-size chunk. Mirrors what `HttpURLConnection` does for Sony. Only the
     * chunk payload bytes are surfaced to [read]; chunk-size lines and CRLF separators are consumed
     * internally.
     */
    private class ChunkedInputStream(private val src: InputStream) : InputStream() {
        private var remaining = 0L
        private var eof = false
        private var firstChunk = true

        override fun read(): Int {
            val b = ByteArray(1)
            return if (read(b, 0, 1) == -1) -1 else b[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (eof) return -1
            if (len == 0) return 0
            if (remaining == 0L) {
                if (!firstChunk) readLineRaw() // trailing CRLF after the previous chunk's data
                firstChunk = false
                val size = readChunkSize()
                if (size <= 0L) { consumeTrailer(); eof = true; return -1 }
                remaining = size
            }
            val toRead = minOf(len.toLong(), remaining).toInt()
            val n = src.read(b, off, toRead)
            if (n < 0) { eof = true; return -1 }
            remaining -= n
            return n
        }

        private fun readChunkSize(): Long {
            val line = readLineRaw().substringBefore(';').trim()
            if (line.isEmpty()) return -1
            return line.toLongOrNull(16) ?: -1
        }

        /** Reads the trailer headers (if any) up to the terminating blank line. */
        private fun consumeTrailer() {
            while (true) {
                if (readLineRaw().isEmpty()) break
            }
        }

        /** Reads one CRLF-terminated line from the underlying stream (without the CRLF). */
        private fun readLineRaw(): String {
            val sb = StringBuilder()
            while (true) {
                val c = src.read()
                if (c < 0) break
                if (c == '\n'.code) {
                    if (sb.isNotEmpty() && sb.last() == '\r') sb.setLength(sb.length - 1)
                    break
                }
                sb.append(c.toChar())
            }
            return sb.toString()
        }
    }

    companion object {
        /** Initial reused-buffer size; matches Sony's 512 KB pool buffer (analysis §12). */
        private const val INITIAL_BUFFER = 512_000
        /** Hard cap to reject a desynced/garbage header before allocating/reading. */
        private const val MAX_FRAME = 8_000_000
    }
}
