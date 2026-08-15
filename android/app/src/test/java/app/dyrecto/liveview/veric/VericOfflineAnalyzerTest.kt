package app.dyrecto.liveview.veric

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

/**
 * Split from VericParserTest when the parser moved to :shared — the offline analyzer stays
 * app-side (java.io.File + BleLog), so its structural-validation test lives here.
 */
class VericOfflineAnalyzerTest {

    /** A VERIC envelope: "VERIC" magic + [pad] filler bytes + a JPEG. */
    private fun envelope(jpeg: ByteArray, pad: Int = 8): ByteArray {
        val magic = "VERIC".toByteArray(Charsets.US_ASCII)
        val filler = ByteArray(pad) { (0x40 + it).toByte() }
        return magic + filler + jpeg
    }

    @Test
    fun offlineAnalyzerSkipsFalseThumbnailEoi() {
        // Build a realistic-ish JPEG with an APP1 segment containing an embedded thumbnail
        // (its own SOI/EOI). The first FFD9 is the thumbnail's; structural validation must skip it.
        val realJpeg = jpegWithThumbnail()
        val env = envelope(realJpeg, pad = 4)
        val report = VericOfflineAnalyzer.analyze(env.inputStream(), exportDir = null)

        assertEquals(1, report.totalFrames)
        assertEquals(realJpeg.size, report.maxJpegSize)
        assertEquals(0, report.parseErrors)
    }

    /**
     * A minimal but structurally valid JPEG: SOI, APP1 segment whose payload embeds a tiny full JPEG
     * (thumbnail), DQT-less SOS with a short entropy run, then the real EOI.
     */
    private fun jpegWithThumbnail(): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(0xFF); out.write(0xD8) // SOI
        // APP1 (FFE1) carrying a thumbnail = a complete inner JPEG inside the segment payload.
        val thumb = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x00, 0x11, 0xFF.toByte(), 0xD9.toByte())
        val app1Len = 2 + thumb.size // length field counts itself + payload
        out.write(0xFF); out.write(0xE1)
        out.write((app1Len shr 8) and 0xFF); out.write(app1Len and 0xFF)
        out.write(thumb)
        // SOS (FFDA) with a 2-byte length (header only) then a short entropy run, then EOI.
        out.write(0xFF); out.write(0xDA)
        out.write(0x00); out.write(0x02) // length = 2 (just the length field, no scan params)
        out.write(0x10); out.write(0x20); out.write(0x30) // entropy bytes (no FF)
        out.write(0xFF); out.write(0xD9) // real EOI
        return out.toByteArray()
    }
}
