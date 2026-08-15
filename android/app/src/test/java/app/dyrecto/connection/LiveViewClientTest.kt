package app.dyrecto.connection

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Pure-JVM tests for the Sony Live View frame parser. No Android — mirrors the AlertEngineTest
 * style. Feeds a synthetic HTTP-200 response with hand-built frames and asserts the emitted JPEG
 * slices match the headers exactly.
 */
class LiveViewClientTest {

    /** Builds one Sony Live View frame: 16-byte LE header + reserved + JPEG + metadata. */
    private fun frame(jpeg: ByteArray, reserved: Int, meta: ByteArray): ByteArray {
        val offsetToImage = 16 + reserved
        val offsetToMeta = offsetToImage + jpeg.size
        val header = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(offsetToImage)
            putInt(jpeg.size)
            putInt(offsetToMeta)
            putInt(meta.size)
        }.array()
        return header + ByteArray(reserved) + jpeg + meta
    }

    private fun httpResponse(body: ByteArray): ByteArray {
        val head = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\n\r\n"
        return head.toByteArray(Charsets.US_ASCII) + body
    }

    /** Wraps [body] in HTTP/1.1 chunked transfer-encoding using fixed-size chunks. */
    private fun chunked(body: ByteArray, chunkSize: Int): ByteArray {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < body.size) {
            val n = minOf(chunkSize, body.size - i)
            out.write(("%x".format(n) + "\r\n").toByteArray(Charsets.US_ASCII))
            out.write(body, i, n)
            out.write("\r\n".toByteArray(Charsets.US_ASCII))
            i += n
        }
        out.write("0\r\n\r\n".toByteArray(Charsets.US_ASCII))
        return out.toByteArray()
    }

    private fun httpChunkedResponse(body: ByteArray, chunkSize: Int): ByteArray {
        val head = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: application/octet-stream\r\n" +
            "Transfer-Encoding: chunked\r\n\r\n"
        return head.toByteArray(Charsets.US_ASCII) + chunked(body, chunkSize)
    }

    @Test
    fun deChunksAndParsesFramesAcrossChunkBoundaries() {
        val jpeg1 = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3, 0xFF.toByte(), 0xD9.toByte())
        val jpeg2 = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 9, 8, 0xFF.toByte(), 0xD9.toByte())
        val meta1 = byteArrayOf(0x10, 0x20, 0x30)
        val meta2 = byteArrayOf(0x40, 0x50)

        val body = frame(jpeg1, reserved = 4, meta = meta1) +
            frame(jpeg2, reserved = 0, meta = meta2)

        // Tiny chunks so every frame is split across many chunk boundaries — the exact condition
        // that desynced a raw (non-de-chunking) reader onto a JPEG EOI tail.
        val input = httpChunkedResponse(body, chunkSize = 5).inputStream()
        val output = ByteArrayOutputStream()

        val received = ArrayList<ByteArray>()
        val client = LiveViewClient(input, output, host = "cam", path = "/liveviewstream") { buf, off, len ->
            received.add(buf.copyOfRange(off, off + len))
        }

        assertTrue(client.run())
        assertEquals(2, received.size)
        assertArrayEquals(jpeg1, received[0])
        assertArrayEquals(jpeg2, received[1])
    }

    @Test
    fun parsesTwoFramesAndExtractsJpegSlices() {
        val jpeg1 = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 1, 2, 3, 0xFF.toByte(), 0xD9.toByte())
        val jpeg2 = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 9, 8, 0xFF.toByte(), 0xD9.toByte())
        val meta1 = byteArrayOf(0x10, 0x20, 0x30)
        val meta2 = byteArrayOf(0x40, 0x50)

        val body = frame(jpeg1, reserved = 4, meta = meta1) +
            frame(jpeg2, reserved = 0, meta = meta2)

        val input = httpResponse(body).inputStream()
        val output = ByteArrayOutputStream()

        val received = ArrayList<ByteArray>()
        val client = LiveViewClient(input, output, host = "cam", path = "/liveview") { buf, off, len ->
            received.add(buf.copyOfRange(off, off + len))
        }

        val started = client.run()

        assertTrue("stream should reach HTTP 200", started)
        assertEquals(2, received.size)
        assertArrayEquals(jpeg1, received[0])
        assertArrayEquals(jpeg2, received[1])

        // Request line should be a GET to the given path with the Host header.
        val sent = output.toString("US-ASCII")
        assertTrue(sent.startsWith("GET /liveview HTTP/1.1\r\n"))
        assertTrue(sent.contains("Host: cam\r\n"))
    }

    @Test
    fun nonOkResponseReturnsFalseAndEmitsNoFrames() {
        val input = "HTTP/1.1 404 Not Found\r\n\r\n".toByteArray(Charsets.US_ASCII).inputStream()
        val received = ArrayList<ByteArray>()
        val client = LiveViewClient(input, ByteArrayOutputStream(), "cam", "/x") { buf, off, len ->
            received.add(buf.copyOfRange(off, off + len))
        }

        assertEquals(false, client.run())
        assertTrue(received.isEmpty())
    }
}
