package app.dyrecto.ios

import app.dyrecto.liveview.veric.VericFrameRef
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes

/**
 * Bulk-materializes the borrowed VERIC JPEG slice as NSData in ONE native copy — the Swift
 * renderer must never loop `KotlinByteArray.get(index:)` per byte on the 30 fps frame path.
 * NSData.dataWithBytes copies, satisfying the zero-copy contract's "consume before the parser
 * reuses its buffer" rule.
 */
fun VericFrameRef.jpegNSData(): NSData = buf.usePinned { pinned ->
    NSData.dataWithBytes(pinned.addressOf(jpegOffset), jpegLength.toULong())
}
