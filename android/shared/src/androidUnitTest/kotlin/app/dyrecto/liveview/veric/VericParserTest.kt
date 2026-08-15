package app.dyrecto.liveview.veric

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for the VERIC envelope parser. No Android — mirrors LiveViewClientTest's style:
 * synthetic envelope builders feed the parser, and the sink materializes each emitted [VericFrameRef]
 * (valid only during the callback) into a retainable snapshot for assertions.
 */
class VericParserTest {

    private val SOI = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val EOI = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    /** A synthetic JPEG: SOI + [body] + EOI. */
    private fun jpeg(vararg body: Int): ByteArray =
        SOI + body.map { it.toByte() }.toByteArray() + EOI

    /** A VERIC envelope: "VERIC" magic + [pad] filler bytes + a JPEG. */
    private fun envelope(jpeg: ByteArray, pad: Int = 8): ByteArray {
        val magic = "VERIC".toByteArray(Charsets.US_ASCII)
        val filler = ByteArray(pad) { (0x40 + it).toByte() }
        return magic + filler + jpeg
    }

    /** Collects materialized frames; the parser only logs (BleLog is a no-op under unit tests). */
    private fun collector(): Pair<VericParser, MutableList<VericFrame>> {
        val out = ArrayList<VericFrame>()
        val parser = VericParser(onFrame = { out.add(it.materialize()) }, verbose = false)
        return parser to out
    }

    @Test
    fun singleCompleteFrame() {
        val j = jpeg(1, 2, 3)
        val (parser, frames) = collector()
        val env = envelope(j)
        parser.parse(env, env.size)

        assertEquals(1, frames.size)
        assertArrayEquals(j, frames[0].jpeg)
        assertTrue(String(frames[0].header, Charsets.US_ASCII).startsWith("VERIC"))
        // SOI sits right after the "VERIC" + 8-byte-pad header (13 bytes).
        assertEquals(13L, frames[0].streamOffset)
    }

    @Test
    fun zeroCopyRefSliceMatchesBeforeMaterialize() {
        val j = jpeg(5, 6, 7, 8)
        val env = envelope(j)
        val seen = ArrayList<ByteArray>()
        val parser = VericParser(onFrame = { ref ->
            // Read straight from the borrowed buffer — no materialize() — to prove zero-copy slices.
            seen.add(ref.buf.copyOfRange(ref.jpegOffset, ref.jpegOffset + ref.jpegLength))
        }, verbose = false)
        parser.parse(env, env.size)

        assertEquals(1, seen.size)
        assertArrayEquals(j, seen[0])
    }

    @Test
    fun multipleEnvelopesInOneChunk() {
        val j1 = jpeg(1)
        val j2 = jpeg(2, 2)
        val j3 = jpeg(3, 3, 3)
        val (parser, frames) = collector()
        val stream = envelope(j1, pad = 4) + envelope(j2, pad = 6) + envelope(j3, pad = 2)
        parser.parse(stream, stream.size)

        assertEquals(3, frames.size)
        assertArrayEquals(j1, frames[0].jpeg)
        assertArrayEquals(j2, frames[1].jpeg)
        assertArrayEquals(j3, frames[2].jpeg)
        assertEquals(0, frames[0].index)
        assertEquals(2, frames[2].index)
        frames.forEach { assertTrue(String(it.header, Charsets.US_ASCII).startsWith("VERIC")) }
    }

    @Test
    fun frameSplitAcrossChunksIncludingMarkerSplit() {
        val j = jpeg(0x11, 0x22, 0x33, 0x44)
        val stream = envelope(j, pad = 5)
        val (parser, frames) = collector()
        // Feed one byte at a time — the worst case: every marker is split across appends.
        for (b in stream) parser.parse(byteArrayOf(b), 1)

        assertEquals(1, frames.size)
        assertArrayEquals(j, frames[0].jpeg)
    }

    @Test
    fun postEoiBytesBecomeNextFrameHeader() {
        val j1 = jpeg(0xAA)
        val j2 = jpeg(0xBB)
        val (parser, frames) = collector()
        // Distinct, recognizable header padding on the second envelope.
        val env1 = envelope(j1, pad = 3)
        val env2 = "VERIC".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x7A, 0x7B, 0x7C, 0x7D) + j2
        parser.parse(env1 + env2, (env1 + env2).size)

        assertEquals(2, frames.size)
        // Frame 2's header is exactly the opaque bytes following frame 1's EOI (its whole envelope head).
        val expectedHeader2 = "VERIC".toByteArray(Charsets.US_ASCII) + byteArrayOf(0x7A, 0x7B, 0x7C, 0x7D)
        assertArrayEquals(expectedHeader2, frames[1].header)
    }

    @Test
    fun partialFrameAtEndEmitsNothing() {
        val j = jpeg(1, 2, 3)
        val env = envelope(j)
        val partial = env.copyOfRange(0, env.size - 1) // drop the final EOI byte
        val (parser, frames) = collector()
        parser.parse(partial, partial.size)

        assertEquals(0, frames.size)
    }

    @Test
    fun strayEoiWithNoSoiEmitsNothingAndDoesNotCrash() {
        val (parser, frames) = collector()
        val garbage = "VERIC".toByteArray(Charsets.US_ASCII) + EOI + byteArrayOf(0x00, 0x01)
        parser.parse(garbage, garbage.size)

        assertEquals(0, frames.size)
        assertEquals(0, parser.errors)
    }

    @Test
    fun recoversAfterMalformedData() {
        // A false SOI with no EOI for > MAX_JPEG_BYTES forces a resync, then a valid frame follows.
        val (parser, frames) = collector()
        val j = jpeg(9, 9, 9)

        // Header + a lone SOI followed by a long run of non-marker bytes (no EOI) → oversized → resync.
        parser.parse(SOI, SOI.size)
        val filler = ByteArray(64 * 1024) { 0x55 }
        var pushed = 2
        while (pushed <= VericFrameExtractor.MAX_JPEG_BYTES + filler.size) {
            parser.parse(filler, filler.size)
            pushed += filler.size
        }
        assertTrue("expected an oversized-JPEG error", parser.errors >= 1)

        // Now a clean envelope must still parse.
        val env = envelope(j)
        parser.parse(env, env.size)
        assertEquals(1, frames.size)
        assertArrayEquals(j, frames[0].jpeg)
    }
}
