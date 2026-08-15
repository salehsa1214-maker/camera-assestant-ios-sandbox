@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package app.dyrecto.ios

import app.dyrecto.liveview.vision.FramePixels
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/**
 * iOS [FramePixels] backed by an RGBA8888 byte buffer (the natural output of drawing a decoded
 * CGImage into a CGBitmapContext with kCGImageAlphaPremultipliedLast | kCGBitmapByteOrder32Big).
 *
 * Mirrors the Android hot path's cost profile: ONE bulk copy out of the platform buffer
 * ([fromNSData]'s memcpy stands in for Bitmap.getPixels), then [readArgb] converts byte order in
 * a tight native loop into the caller's reusable IntArray — no per-pixel ObjC bridging.
 */
class RgbaFramePixels(
    private val rgba: ByteArray,
    override val width: Int,
    override val height: Int,
) : FramePixels {

    override val isAvailable: Boolean get() = rgba.size >= width * height * 4

    override fun readArgb(dest: IntArray) {
        val count = width * height
        var s = 0
        for (i in 0 until count) {
            val r = rgba[s].toInt() and 0xFF
            val g = rgba[s + 1].toInt() and 0xFF
            val b = rgba[s + 2].toInt() and 0xFF
            val a = rgba[s + 3].toInt() and 0xFF
            dest[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            s += 4
        }
    }

    companion object {
        /** Wraps a Swift-produced RGBA8888 buffer; the single bulk copy of the frame. */
        fun fromNSData(data: NSData, width: Int, height: Int): RgbaFramePixels {
            val size = data.length.toInt()
            val bytes = ByteArray(size)
            if (size > 0) {
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
            }
            return RgbaFramePixels(bytes, width, height)
        }
    }
}
