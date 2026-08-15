package app.dyrecto.connection

import java.io.InputStream
import java.io.OutputStream

/**
 * A duplex byte stream to the camera's Live View HTTP endpoint, already connected. Android-free
 * (java.io only), the same spirit as [PtpIpClient.Channel] — the Live View pipeline reads/writes
 * raw bytes and never sees how the transport was opened (plain socket, Wi-Fi-bound, etc.).
 */
interface LiveViewTransport {
    val input: InputStream
    val output: OutputStream
    /** Host to send in the HTTP `Host:` header. */
    val host: String
    /** Request path (and query) to GET. */
    val path: String
    fun close()
}

/**
 * Opens a [LiveViewTransport] on demand. Implemented by the camera-communication layer (which knows
 * the camera IP, the Wi-Fi network, and the resolved Live View URL). The Live View pipeline depends
 * only on this seam, so it owns no networking and no URL knowledge.
 */
interface LiveViewTransportProvider {
    /**
     * Resolves the Live View URL and opens a connected stream to it, or returns null (logging the
     * reason under the `[LV]` tag) when monitoring isn't active or the URL can't be resolved.
     */
    fun openLiveView(): LiveViewTransport?

    /**
     * Human-readable reason the most recent [openLiveView] returned null, for UI display
     * (e.g. "Live View not enabled on camera"). Null after a successful open.
     */
    val lastLiveViewError: String?
}
