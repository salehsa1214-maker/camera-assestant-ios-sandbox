package app.dyrecto.liveview.veric

import app.dyrecto.platform.epochMillis

/**
 * Incremental, allocation-light VERIC envelope scanner.
 *
 * Models the stream as repeated envelopes — `[opaque header][JPEG FFD8…FFD9]` — rather than doing a
 * naive global SOI/EOI sweep. After a JPEG's EOI, the following bytes are the NEXT envelope's header
 * candidate, never discarded as garbage. This keeps the parser JPEG-anchored while preserving the
 * VERIC envelope structure for later reverse-engineering.
 *
 * Zero-copy: emitted frames are [VericFrameRef]s pointing into [buf]. The callback MUST consume the
 * borrowed slice synchronously; the buffer is compacted only after the callback returns.
 *
 * Tolerant of arbitrary chunk boundaries: a JPEG — or even either two-byte marker — may straddle two
 * [append] calls. The scanner never assumes a marker is wholly contained in one chunk.
 *
 * Never throws. On malformed input ([MAX_JPEG_BYTES] exceeded with no EOI) it logs via [onError],
 * resyncs to the next `FFD8`, and continues.
 *
 * Not thread-safe: drive it from a single reader thread (as [app.dyrecto.liveview.PushLiveViewSession] does per socket).
 */
class VericFrameExtractor(
    private val onFrame: (VericFrameRef) -> Unit,
    private val onError: (String) -> Unit = {},
    private val clock: () -> Long = ::epochMillis,
    /**
     * Decides whether a candidate `FFD8`…`FFD9` slice (`buf[jpegStart, jpegEndExclusive)`) is a real
     * frame. Default accepts the first EOI — the lightweight runtime behavior. The offline analyzer
     * supplies a structural/decoder check so a false EOI from an embedded EXIF thumbnail is skipped
     * and scanning continues to the next `FFD9`.
     */
    private val acceptFrame: (buf: ByteArray, jpegStart: Int, jpegEndExclusive: Int) -> Boolean =
        { _, _, _ -> true },
) {
    private enum class State { IN_HEADER, IN_JPEG }

    private var buf = ByteArray(64 * 1024)
    private var size = 0            // valid bytes in buf
    private var streamBase = 0L     // absolute stream offset of buf[0]

    private var state = State.IN_HEADER
    private var headerStart = 0     // index in buf where the current envelope's header begins
    private var jpegStart = 0       // index of SOI (valid only when IN_JPEG)
    private var scanPos = 0         // next index to examine

    private var nextIndex = 0
    var framesEmitted = 0; private set
    var errors = 0; private set
    /** Bytes appended into the parser over its lifetime. */
    var bytesConsumed = 0L; private set

    /** Bytes currently held but not yet emitted (pending header + in-progress JPEG). */
    val bufferedBytes: Int get() = size - headerStart

    /** Feeds a chunk into the parser, emitting any frames that complete. */
    fun append(chunk: ByteArray, off: Int, len: Int) {
        if (len <= 0) return
        ensureCapacity(size + len)
        chunk.copyInto(buf, destinationOffset = size, startIndex = off, endIndex = off + len)
        size += len
        bytesConsumed += len
        scan()
    }

    fun append(chunk: ByteArray, len: Int) = append(chunk, 0, len)

    private fun scan() {
        var progressed = true
        while (progressed) {
            progressed = when (state) {
                State.IN_HEADER -> scanForSoi()
                State.IN_JPEG -> scanForEoi()
            }
        }
    }

    /** Looks for `FFD8`. Returns true if it transitioned to IN_JPEG (more scanning may be possible). */
    private fun scanForSoi(): Boolean {
        val i = indexOfMarker(0xD8.toByte(), from = maxOf(scanPos, headerStart))
        if (i < 0) {
            // No SOI yet. Keep the trailing byte in case it's the 0xFF of a straddling marker.
            scanPos = maxOf(headerStart, size - 1)
            return false
        }
        jpegStart = i
        state = State.IN_JPEG
        scanPos = i + 2
        return true
    }

    /** Looks for `FFD9`. Returns true if a frame was emitted (and the buffer compacted). */
    private fun scanForEoi(): Boolean {
        val j = indexOfMarker(0xD9.toByte(), from = scanPos)
        if (j < 0) {
            // Oversized JPEG with no EOI ⇒ treat the SOI as a false boundary, resync.
            if (size - jpegStart > MAX_JPEG_BYTES) {
                errors++
                onError(
                    "oversized JPEG: no EOI within ${size - jpegStart}B from SOI@${streamBase + jpegStart}" +
                        " — resyncing to next FFD8"
                )
                // Skip past the false SOI; everything from there is header again.
                headerStart = jpegStart + 2
                scanPos = jpegStart + 2
                state = State.IN_HEADER
                return true
            }
            scanPos = maxOf(scanPos, size - 1)
            return false
        }
        val jpegEnd = j + 2 // exclusive

        if (!acceptFrame(buf, jpegStart, jpegEnd)) {
            // False boundary (e.g. embedded thumbnail EOI). Keep scanning for the next FFD9.
            scanPos = jpegEnd
            return true
        }

        val ref = VericFrameRef(
            index = nextIndex,
            timestampMs = clock(),
            streamOffset = streamBase + jpegStart,
            buf = buf,
            jpegOffset = jpegStart,
            jpegLength = jpegEnd - jpegStart,
            headerOffset = headerStart,
            headerLength = jpegStart - headerStart,
        )
        nextIndex++
        framesEmitted++
        onFrame(ref) // synchronous — the borrowed slice is valid only here

        // Compact: drop everything up to and including this JPEG; the rest is the next header.
        compactFrom(jpegEnd)
        state = State.IN_HEADER
        headerStart = 0
        scanPos = 0
        return size > 0
    }

    /**
     * Finds `FF <second>` at or after [from], guarding against a marker split across the buffer end.
     * Returns the index of the `FF`, or -1.
     */
    private fun indexOfMarker(second: Byte, from: Int): Int {
        var i = maxOf(from, 0)
        val last = size - 1
        while (i < last) {
            if (buf[i] == 0xFF.toByte() && buf[i + 1] == second) return i
            i++
        }
        return -1
    }

    /** Drops buf[0, upTo), shifting the remainder to the front and advancing [streamBase]. */
    private fun compactFrom(upTo: Int) {
        val remaining = size - upTo
        if (remaining > 0) buf.copyInto(buf, destinationOffset = 0, startIndex = upTo, endIndex = upTo + remaining)
        size = remaining
        streamBase += upTo
    }

    private fun ensureCapacity(needed: Int) {
        if (needed <= buf.size) return
        var newCap = buf.size
        while (newCap < needed) newCap = newCap shl 1
        buf = buf.copyOf(newCap)
    }

    companion object {
        /** A live-view JPEG is well under this; exceeding it without an EOI means a false SOI. */
        const val MAX_JPEG_BYTES = 4 * 1024 * 1024
    }
}
