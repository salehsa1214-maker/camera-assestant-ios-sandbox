package app.dyrecto.connection

import android.net.Network
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * SSDP / M-SEARCH discovery, mirroring Sony Creators' App `getUuidForWifiRemote`
 * step: after the phone has joined the camera Wi-Fi AP, the app resolves the
 * camera's device UUID over SSDP before it reads CC17 SshInfo. The camera only
 * generates its one-time SSH credential once this remote session is live, so we
 * MUST resolve the UUID before reading CC17.
 *
 * The socket is bound to the camera [Network] so the multicast/unicast probes
 * route over the camera AP (which has no internet capability).
 *
 * Phase-1 success condition: a UUID is resolved from an SSDP response. We do not
 * fetch/parse the full device-description XML here (that belongs to the later
 * PTP/IP work) — resolving the UUID is sufficient to prove the session is live.
 */
class SsdpDiscoverer {

    private val done = AtomicBoolean(false)

    /** Multicast SSDP address. */
    private val ssdpAddr = "239.255.255.250"
    private val ssdpPort = 1900

    /** Search targets we probe with, in order of specificity. */
    private val searchTargets = listOf(
        "urn:schemas-sony-com:service:ScalarWebAPI:1",
        "urn:schemas-upnp-org:device:DigitalImagingDevice:1",
        "ssdp:all",
    )

    /**
     * Runs M-SEARCH on [network] (camera AP). Resolves on the first response that
     * carries a `uuid:` token (USN/LOCATION). Calls [onResolved] with the uuid and
     * the raw response, or [onFailed] after [timeoutMs] with no usable response.
     *
     * Both callbacks fire at most once, on a background thread.
     */
    fun discover(
        network: Network,
        cameraIp: String,
        timeoutMs: Long = 8_000,
        onResolved: (uuid: String, rawResponse: String) -> Unit,
        onFailed: (reason: String) -> Unit,
    ) {
        done.set(false)
        thread(name = "ssdp-msearch", isDaemon = true) {
            BleLog.line(BleLog.Kind.INFO, "[SSDP] starting M-SEARCH on camera network")
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket().apply {
                    reuseAddress = true
                    soTimeout = 1_000
                }
                // Route this socket over the camera AP explicitly.
                runCatching { network.bindSocket(socket) }
                    .onFailure { BleLog.error("[SSDP] bindSocket failed: ${it.message} (continuing on default route)") }

                val multicast = InetAddress.getByName(ssdpAddr)
                val gateway = runCatching { InetAddress.getByName(cameraIp) }.getOrNull()

                val deadline = System.currentTimeMillis() + timeoutMs
                var probeIdx = 0
                val buf = ByteArray(4096)

                while (!done.get() && System.currentTimeMillis() < deadline) {
                    // Send one probe per loop, cycling search targets; multicast + unicast-to-gateway.
                    val st = searchTargets[probeIdx % searchTargets.size]
                    probeIdx++
                    val msearch = buildMSearch(st)
                    runCatching {
                        socket.send(DatagramPacket(msearch, msearch.size, InetSocketAddress(multicast, ssdpPort)))
                        if (gateway != null) {
                            socket.send(DatagramPacket(msearch, msearch.size, InetSocketAddress(gateway, ssdpPort)))
                        }
                    }.onFailure { BleLog.error("[SSDP] send failed: ${it.message}") }

                    // Drain responses for ~1s (soTimeout), then re-probe.
                    val drainUntil = System.currentTimeMillis() + 1_000
                    while (!done.get() && System.currentTimeMillis() < drainUntil) {
                        val packet = DatagramPacket(buf, buf.size)
                        try {
                            socket.receive(packet)
                        } catch (e: java.net.SocketTimeoutException) {
                            break
                        }
                        val from = packet.address?.hostAddress ?: "?"
                        val text = String(packet.data, 0, packet.length, Charsets.US_ASCII)
                        BleLog.line(BleLog.Kind.NOTIFY, "[SSDP] response from $from")
                        val uuid = extractUuid(text)
                        if (uuid != null && done.compareAndSet(false, true)) {
                            BleLog.line(BleLog.Kind.INFO, "[SSDP] uuid resolved = $uuid")
                            onResolved(uuid, text)
                            return@thread
                        }
                    }
                }

                if (done.compareAndSet(false, true)) {
                    BleLog.error("[SSDP] no UUID resolved within ${timeoutMs}ms")
                    onFailed("SSDP M-SEARCH timed out (no camera UUID on $cameraIp)")
                }
            } catch (t: Throwable) {
                if (done.compareAndSet(false, true)) {
                    BleLog.error("[SSDP] error: ${t.message}")
                    onFailed("SSDP error: ${t.message}")
                }
            } finally {
                runCatching { socket?.close() }
            }
        }
    }

    /** Cancels an in-flight discovery; further callbacks are suppressed. */
    fun cancel() {
        done.set(true)
    }

    private fun buildMSearch(st: String): ByteArray =
        ("M-SEARCH * HTTP/1.1\r\n" +
            "HOST: $ssdpAddr:$ssdpPort\r\n" +
            "MAN: \"ssdp:discover\"\r\n" +
            "MX: 1\r\n" +
            "ST: $st\r\n" +
            "\r\n").toByteArray(Charsets.US_ASCII)

    /** Pulls the first `uuid:<id>` token from any SSDP header (USN, LOCATION, etc.). */
    private fun extractUuid(response: String): String? {
        val m = UUID_REGEX.find(response) ?: return null
        return m.groupValues[1]
    }

    private companion object {
        val UUID_REGEX = Regex("uuid:([0-9a-fA-F-]{8,})", RegexOption.IGNORE_CASE)
    }
}
