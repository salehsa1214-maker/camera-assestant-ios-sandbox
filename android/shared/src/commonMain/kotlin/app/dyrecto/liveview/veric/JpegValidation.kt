package app.dyrecto.liveview.veric

/**
 * Minimal structural JPEG validation — portable (no `javax.imageio`, so it runs on Android too).
 *
 * Used by the offline analyzer to reject a *false* `FFD9` boundary: a JPEG with an embedded EXIF
 * thumbnail carries the thumbnail's own `FFD8`/`FFD9` inside its APP1 segment, so the first `FFD9`
 * after the main SOI is the thumbnail's, not the image's. Walking the segment markers by their
 * length fields jumps past the whole APP1 (and thus past the thumbnail), so a candidate that ends at
 * the thumbnail's EOI fails to terminate cleanly and is rejected; scanning then continues to the
 * real EOI.
 */
object JpegValidation {

    /** True if `buf[start, endExclusive)` is a structurally complete JPEG (SOI … SOS entropy … EOI). */
    fun isCompleteJpeg(buf: ByteArray, start: Int, endExclusive: Int): Boolean {
        if (endExclusive - start < 4) return false
        if (u8(buf, start) != 0xFF || u8(buf, start + 1) != 0xD8) return false
        if (u8(buf, endExclusive - 2) != 0xFF || u8(buf, endExclusive - 1) != 0xD9) return false

        var i = start + 2
        while (i < endExclusive - 1) {
            if (u8(buf, i) != 0xFF) return false // expected a marker prefix
            // Skip fill bytes (0xFF padding before a marker).
            var sec = i + 1
            while (sec < endExclusive && u8(buf, sec) == 0xFF) sec++
            if (sec >= endExclusive) return false
            val m = u8(buf, sec)
            i = sec + 1
            when {
                m == 0xD9 -> return i == endExclusive // EOI must land exactly at the candidate end
                m == 0x01 || m in 0xD0..0xD7 -> { /* standalone marker, no length payload */ }
                m == 0xDA -> {
                    // Start Of Scan: skip its header by length, then scan entropy data to the next marker.
                    if (i + 1 >= endExclusive) return false
                    i += u16(buf, i)
                    while (i < endExclusive - 1) {
                        if (u8(buf, i) == 0xFF) {
                            val n = u8(buf, i + 1)
                            if (n == 0x00 || n in 0xD0..0xD7) { i += 2; continue } // stuffing / restart
                            break // a real marker (expect EOI) — handled by the outer loop
                        }
                        i++
                    }
                }
                else -> {
                    // Generic segment with a 2-byte length.
                    if (i + 1 >= endExclusive) return false
                    i += u16(buf, i)
                }
            }
        }
        return false
    }

    private fun u8(b: ByteArray, i: Int): Int = b[i].toInt() and 0xFF
    private fun u16(b: ByteArray, i: Int): Int = (u8(b, i) shl 8) or u8(b, i + 1)
}
