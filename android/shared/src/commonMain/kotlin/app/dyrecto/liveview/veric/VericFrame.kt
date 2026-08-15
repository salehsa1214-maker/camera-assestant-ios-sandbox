package app.dyrecto.liveview.veric

/**
 * Frame contract for the VERIC stream parser, in two layers so the hot path avoids per-frame copies.
 *
 * The pushed stream is a sequence of envelopes:
 *
 *     [opaque VERIC header][JPEG FFD8…FFD9][opaque VERIC header][JPEG FFD8…FFD9]…
 *
 * The parser is JPEG-anchored (it locates the embedded `FFD8`…`FFD9`) and treats everything before
 * each SOI as an opaque header blob (it always includes the ASCII "VERIC" magic plus an as-yet
 * un-reverse-engineered header). The header is preserved verbatim for later analysis but never
 * interpreted here.
 *
 * Two representations:
 *  - [VericFrameRef] — zero-copy, runtime: offsets into the parser's own buffer. The borrowed bytes
 *    are valid ONLY for the duration of the emitting callback (the parser reuses/compacts its buffer
 *    afterwards). A later renderer can feed `buf, jpegOffset, jpegLength` straight into
 *    `BitmapFactory.decodeByteArray` / `ImageDecoder` with no intermediate allocation, which matters
 *    at 30–60 FPS.
 *  - [VericFrame] — materialized copy: built only when something needs to retain the bytes (offline
 *    export, tests) via [VericFrameRef.materialize]. Never allocated on the runtime path.
 */
class VericFrameRef(
    /** 0-based emission order within this parser's lifetime. */
    @JvmField val index: Int,
    /** Wall-clock at emission. The stream may carry a PTS inside [headerLength]; not pinned yet. */
    @JvmField val timestampMs: Long,
    /** Absolute byte offset of the JPEG SOI within the whole stream. */
    @JvmField val streamOffset: Long,
    /** The parser buffer. Borrowed — valid only during the emitting callback. Do not retain. */
    @JvmField val buf: ByteArray,
    /** Offset of `FFD8` (SOI) within [buf]. */
    @JvmField val jpegOffset: Int,
    /** Length of the JPEG, SOI..EOI inclusive. */
    @JvmField val jpegLength: Int,
    /** Offset of the opaque header (bytes preceding the SOI for this envelope) within [buf]. */
    @JvmField val headerOffset: Int,
    /** Length of the opaque header (includes the "VERIC" magic). */
    @JvmField val headerLength: Int,
) {
    /** Copies the borrowed slices into a retainable [VericFrame]. Use off the runtime hot path. */
    fun materialize(): VericFrame = VericFrame(
        index = index,
        timestampMs = timestampMs,
        streamOffset = streamOffset,
        jpeg = buf.copyOfRange(jpegOffset, jpegOffset + jpegLength),
        header = buf.copyOfRange(headerOffset, headerOffset + headerLength),
    )
}

/**
 * Immutable, retainable copy of a parsed VERIC frame. Built via [VericFrameRef.materialize] only
 * when persistence/export needs it.
 */
data class VericFrame(
    val index: Int,
    val timestampMs: Long,
    val streamOffset: Long,
    /** Complete JPEG, `FFD8`…`FFD9` inclusive. */
    val jpeg: ByteArray,
    /** Opaque header bytes preceding the JPEG (includes "VERIC" magic). Never interpreted here. */
    val header: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VericFrame) return false
        return index == other.index &&
            timestampMs == other.timestampMs &&
            streamOffset == other.streamOffset &&
            jpeg.contentEquals(other.jpeg) &&
            header.contentEquals(other.header)
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + timestampMs.hashCode()
        result = 31 * result + streamOffset.hashCode()
        result = 31 * result + jpeg.contentHashCode()
        result = 31 * result + header.contentHashCode()
        return result
    }
}
