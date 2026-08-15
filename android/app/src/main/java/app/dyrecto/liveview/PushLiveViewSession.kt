package app.dyrecto.liveview

import app.dyrecto.connection.BleLog
import app.dyrecto.connection.PtpIpClient
import app.dyrecto.liveview.render.LiveViewFrameRenderer
import app.dyrecto.liveview.render.LiveViewRenderState
import app.dyrecto.liveview.veric.VericParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Developer-only Push Live View PoC (NOT production).
 *
 * Exercises Sony's confirmed push model discovered by runtime RE of Monitor & Control on the FX3:
 * the phone opens two local TCP listen sockets (video + metadata), tells the camera where to deliver
 * via SDIO_ControlMonitoring(Start=1) over the live PTP command channel, and the camera connects
 * back and pushes JPEG frames. KeepAlive=3 every 30 s; Stop=2 on teardown.
 *
 * Goal of THIS phase: prove the camera connects back and pushes bytes - not to render. On connect it
 * dumps the first bytes, continuously saves the full stream to a file (capped) for offline analysis,
 * runs a JPEG SOI/EOI log-only diagnostic, and stops there. Rendering is a later phase.
 *
 * The Start DataOut layout ([encodeStartPayload]) now matches the Sony monitoring serializers
 * (Lr4/d wrapper + Lr4/c receiver) recovered by Hermes, byte-for-byte.
 *
 * Threading mirrors LiveViewSession: owned threads do the blocking I/O; [start]/[stop] are
 * non-blocking and idempotent, guarded by a generation counter.
 *
 * @param captureDir directory for push_lv_video.bin / push_lv_meta.bin (e.g. getExternalFilesDir).
 */
class PushLiveViewSession(
    private val sender: PushMonitoringSender,
    private val captureDir: File?,
) {
    private val _status = MutableStateFlow(PushLvStatus())
    val status: StateFlow<PushLvStatus> = _status.asStateFlow()

    /** Decodes pushed JPEG frames into bitmaps for the UI. Driven from the video drain loop only. */
    private val renderer = LiveViewFrameRenderer()
    val renderState: StateFlow<LiveViewRenderState> = renderer.state

    @Volatile private var generation = 0
    @Volatile private var orchestrator: Thread? = null
    @Volatile private var videoServer: ServerSocket? = null
    @Volatile private var metaServer: ServerSocket? = null
    @Volatile private var videoSock: Socket? = null
    @Volatile private var metaSock: Socket? = null
    @Volatile private var videoReader: Thread? = null
    @Volatile private var metaReader: Thread? = null
    @Volatile private var keepAlive: ScheduledExecutorService? = null

    /** Per-delivery handle the camera returns in the Start (sub=1) OperationResponse param[0]. Echoed
     *  in the KeepAlive and Stop DataOut payloads (see [encodeDeliveryPayload]) so the camera scopes
     *  them to THIS delivery — without it, KeepAlive is rejected (NG_Invalid_Args) and the delivery's
     *  ~60s idle timer closes the video socket, and Stop can't free the delivery slot. */
    @Volatile private var deliveryId: Int? = null

    /** Starts the PoC if not already running. Non-blocking; safe to call repeatedly. */
    @Synchronized
    fun start() {
        if (orchestrator != null) {
            log("start ignored - already running")
            return
        }
        val gen = ++generation
        _status.value = PushLvStatus(phase = PushLvStatus.Phase.LISTENING)
        orchestrator = thread(name = "push-lv") { runPoc(gen) }
    }

    /** Stops the PoC: Stop=2, close sockets, cancel KeepAlive. Non-blocking; idempotent. */
    @Synchronized
    fun stop() {
        val gen = ++generation // invalidate in-flight work
        val vs = videoServer; val ms = metaServer
        val vk = videoSock; val mk = metaSock
        val ka = keepAlive; val orch = orchestrator
        videoServer = null; metaServer = null; videoSock = null; metaSock = null
        keepAlive = null; orchestrator = null; videoReader = null; metaReader = null
        update { copy(phase = PushLvStatus.Phase.STOPPED, message = "Stopped by user") }
        // Teardown off the caller's thread - Stop=2 does PTP I/O that must not block the UI.
        thread(name = "push-lv-stop") {
            runCatching { ka?.shutdownNow() }
            val id = deliveryId
            val stopPayload = id?.let { encodeDeliveryPayload(it) }
            log("sending SDIO_ControlMonitoring(Stop=2) deliveryId=${id ?: "(none)"}")
            val r = runCatching { sender.sendControlMonitoring(PtpIpClient.Monitoring.STOP, stopPayload) }.getOrNull()
            log("Stop=2 rc=${r?.name ?: "no-response"} monitoringResult=${r?.monitoringResultName ?: "?"}")
            runCatching { vk?.close() }
            runCatching { mk?.close() }
            runCatching { vs?.close() }
            runCatching { ms?.close() }
            runCatching { orch?.interrupt() }
            log("[gen=$gen] teardown complete")
        }
    }

    private fun runPoc(gen: Int) {
        // 1. Resolve the phone's IP on the camera network (the camera dials this back).
        val phoneIp = sender.phoneIpOnCameraNetwork()
        if (phoneIp == null) {
            fail(gen, "No phone IP on camera network - is the camera Wi-Fi joined?")
            return
        }

        // 2. Bind two TCP listen sockets on the camera-network interface (port 0 = OS-selected).
        val vServer: ServerSocket
        val mServer: ServerSocket
        try {
            vServer = ServerSocket().apply { bind(InetSocketAddress(phoneIp, 0)) }
            mServer = ServerSocket().apply { bind(InetSocketAddress(phoneIp, 0)) }
        } catch (e: Exception) {
            fail(gen, "Could not bind listen sockets on $phoneIp: ${e.javaClass.simpleName}: ${e.message}")
            return
        }
        if (gen != generation) { runCatching { vServer.close() }; runCatching { mServer.close() }; return }
        videoServer = vServer; metaServer = mServer
        val videoPort = vServer.localPort
        val metaPort = mServer.localPort
        val audioPort = 0
        vServer.soTimeout = ACCEPT_TIMEOUT_MS
        mServer.soTimeout = ACCEPT_TIMEOUT_MS
        log("[video] listening on $phoneIp:$videoPort")
        log("[meta]  listening on $phoneIp:$metaPort")
        update {
            copy(phase = PushLvStatus.Phase.LISTENING, phoneIp = phoneIp,
                videoPort = videoPort, metaPort = metaPort)
        }

        // 3. Encode the Start DataOut and 4. send SDIO_ControlMonitoring(Start=1).
        val payload = encodeStartPayload(phoneIp, videoPort, audioPort, metaPort)
        log("Start payload (${payload.size}B): ${hex(payload, payload.size)}")
        log("sending SDIO_ControlMonitoring(Start=1)")
        val startRes = runCatching {
            sender.sendControlMonitoring(PtpIpClient.Monitoring.START, payload)
        }.getOrNull()
        if (startRes == null) {
            log("Start: NO RESPONSE (command channel closed)")
        } else {
            log("Start rc=${startRes.name} (0x%04X) monitoringResult=${startRes.monitoringResultName} dataIn=${startRes.dataLen}B"
                .format(startRes.responseCode))
        }
        // Capture the per-delivery handle (Start response param[0]) — echoed in KeepAlive/Stop payloads.
        deliveryId = startRes?.deliveryId
        log("captured deliveryId=${deliveryId ?: "(none)"} (from Start respParams=${startRes?.responseParams?.joinToString(",", "[", "]") ?: "?"})")
        update {
            copy(phase = PushLvStatus.Phase.STARTED,
                startRcName = startRes?.name ?: "no-response", startRcCode = startRes?.responseCode)
        }

        // 5. KeepAlive=3 every 30 s (single scheduled executor; no retry/reconnect).
        keepAlive = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "push-lv-keepalive") }.also {
            it.scheduleAtFixedRate({
                if (gen != generation) return@scheduleAtFixedRate
                // Echo the delivery handle in the DataOut payload (per the recovered Sony serializer) so
                // the camera scopes this keepalive to our delivery and resets its ~60s idle timer.
                val id = deliveryId
                if (id == null) {
                    log("KeepAlive skipped - no deliveryId captured from Start")
                    return@scheduleAtFixedRate
                }
                log("sending SDIO_ControlMonitoring(KeepAlive=3) deliveryId=$id")
                val r = runCatching {
                    sender.sendControlMonitoring(PtpIpClient.Monitoring.KEEP_ALIVE, encodeDeliveryPayload(id))
                }.getOrNull()
                log("KeepAlive rc=${r?.name ?: "no-response"} monitoringResult=${r?.monitoringResultName ?: "?"}")
            }, KEEPALIVE_PERIOD_S, KEEPALIVE_PERIOD_S, TimeUnit.SECONDS)
        }

        // 6. Accept + drain both sockets on their own threads (Outcome A vs B per socket).
        videoReader = thread(name = "push-lv-video") { acceptAndDrain(gen, "video", vServer, "push_lv_video.bin", isVideo = true) }
        metaReader = thread(name = "push-lv-meta") { acceptAndDrain(gen, "meta", mServer, "push_lv_meta.bin", isVideo = false) }
    }

    /** Accepts one connection on [server], then drains it to a capped file while logging lifecycle. */
    private fun acceptAndDrain(gen: Int, tag: String, server: ServerSocket, fileName: String, isVideo: Boolean) {
        log("[$tag] waiting for accept (timeout=${ACCEPT_TIMEOUT_MS}ms)")
        val sock: Socket = try {
            server.accept()
        } catch (e: SocketTimeoutException) {
            log("[$tag] NO CONNECT-BACK within ${ACCEPT_TIMEOUT_MS}ms - Outcome B (candidate layout likely incomplete)")
            if (gen == generation) update {
                copy(phase = PushLvStatus.Phase.ERROR,
                    message = "$tag: camera did not connect back (Start rc=${startRcName}). " +
                        "Candidate DataOut layout likely incomplete.")
            }
            return
        } catch (e: Exception) {
            if (gen == generation) log("[$tag] accept failed: ${e.javaClass.simpleName}: ${e.message}")
            return
        }
        if (gen != generation) { runCatching { sock.close() }; return }
        if (isVideo) videoSock = sock else metaSock = sock
        val remote = "${sock.inetAddress?.hostAddress}:${sock.port}"
        log("[$tag] ACCEPTED from $remote  <- camera connected back (Outcome A)")
        update {
            if (isVideo) copy(phase = PushLvStatus.Phase.CONNECTED, videoConnected = true, message = "Camera connected ($tag)")
            else copy(phase = PushLvStatus.Phase.CONNECTED, metaConnected = true, message = "Camera connected ($tag)")
        }

        val acceptedAt = System.currentTimeMillis()
        var total = 0L
        var eofReason = "remote EOF"
        var firstDumpDone = false
        val capFile = captureDir?.let { File(it, fileName) }
        val fos = runCatching { capFile?.let { FileOutputStream(it, /*append=*/false) } }.getOrNull()
        if (capFile != null) log("[$tag] capturing to ${capFile.absolutePath} (cap ${CAPTURE_CAP_BYTES / 1024}KB)")
        // VERIC parse tap (video only): count frames AND feed the renderer for live display.
        var frames = 0
        val parser = if (isVideo) VericParser(
            onFrame = { ref ->
                frames++
                update { copy(framesParsed = frames) }
                renderer.submit(ref) // copies the JPEG out of the borrowed buffer; never blocks
            },
            logLine = { m -> BleLog.line(BleLog.Kind.INFO, m) },
        ) else null
        if (isVideo) renderer.start()
        val buf = ByteArray(64 * 1024)
        try {
            val input = sock.getInputStream()
            while (gen == generation) {
                val n = input.read(buf)
                if (n < 0) { eofReason = "remote EOF"; break }
                if (n == 0) continue
                if (!firstDumpDone) {
                    log("[$tag] first ${minOf(n, FIRST_DUMP_BYTES)} bytes: ${hex(buf, minOf(n, FIRST_DUMP_BYTES))}")
                    scanJpegMarkers(tag, buf, n)
                    firstDumpDone = true
                }
                // Append to the capped capture file (keep counting past the cap, stop writing).
                if (fos != null && total < CAPTURE_CAP_BYTES) {
                    val room = (CAPTURE_CAP_BYTES - total).toInt()
                    runCatching { fos.write(buf, 0, minOf(n, room)) }
                }
                // Feed the VERIC parser (passive; never let it break the drain loop).
                if (parser != null) runCatching { parser.parse(buf, n) }
                total += n
                update {
                    if (isVideo) copy(phase = PushLvStatus.Phase.RECEIVING, videoBytes = total)
                    else copy(phase = PushLvStatus.Phase.RECEIVING, metaBytes = total)
                }
                if (total % (256 * 1024) < n) log("[$tag] received $total bytes")
            }
            if (gen != generation) eofReason = "stopped by user"
        } catch (e: Exception) {
            eofReason = "error: ${e.javaClass.simpleName}: ${e.message}"
        } finally {
            if (isVideo) renderer.stop()
            runCatching { fos?.flush(); fos?.close() }
            runCatching { sock.close() }
            val durMs = System.currentTimeMillis() - acceptedAt
            val tput = if (durMs > 0) total * 1000.0 / durMs else 0.0
            log("[$tag] closed - total=$total bytes, duration=${durMs}ms, avg=%.1f B/s, eof=%s".format(tput, eofReason))
            if (parser != null) log("[$tag] VERIC frames parsed=${parser.framesEmitted} errors=${parser.errors}")
        }
    }

    /**
     * Start DataOut for SDIO_ControlMonitoring - little-endian. Matches the Sony monitoring
     * serializers recovered by Hermes byte-for-byte:
     *   - wrapper Lr4/d : u16 protocolVersion, u16 reserved, u32 receiverCount, receiverRecords[]
     *     (8-byte header - NOT the 16-byte playback wrapper Lv4/l with two extra u32s)
     *   - receiver Lr4/c (protocolVersion >= 101):
     *       u16 ipLength | ipBytes(+NUL) | u32 field1 | u32 field2 | u32 field3 |
     *       u16 deliveryType | u8 qualityLevel | u16 protocolType
     * Enum values per Hermes: deliveryType JPEG=1 (Lr4/c$a), qualityLevel LEVEL_3=3 (Lr4/c$c),
     * protocolType TCP=2 (Lr4/c$b). field1/2/3 carry video/audio(=0)/meta, matching the order seen
     * in the live Sony delivery config.
     *
     * Residual unknown: the exact runtime protocolVersion (n5()). Hermes proves the >=101 gate
     * selects the full delivery+quality+protocol tail, so PROTOCOL_VERSION = 101 (the minimal value
     * producing the recovered layout). If the camera expects a higher version this is the one field
     * to revisit - everything else is byte-exact.
     */
    private fun encodeStartPayload(phoneIp: String, videoPort: Int, audioPort: Int, metaPort: Int): ByteArray {
        val ipBytes = (phoneIp + Char(0)).toByteArray(Charsets.US_ASCII) // NUL-terminated ASCII
        // Receiver (Lr4/c, v>=101) = ipLen + ip + 3xu32 + u16 delivery + u8 quality + u16 protocol
        val recordLen = 2 + ipBytes.size + 4 + 4 + 4 + 2 + 1 + 2
        // Wrapper (Lr4/d) = u16 version + u16 reserved + u32 count  (8-byte header, no extra u32s)
        val total = 2 + 2 + 4 + recordLen
        val bb = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN)
        // ---- Wrapper (Lr4/d) ----
        bb.putShort(PROTOCOL_VERSION.toShort()) // u16 protocolVersion (>=101 selects full receiver tail)
        bb.putShort(0)                          // u16 reserved (zero)
        bb.putInt(1)                            // u32 receiverCount
        // ---- ReceiverRecord (Lr4/c, version >= 101) ----
        bb.putShort(ipBytes.size.toShort())     // u16 ipLength (incl. NUL)
        bb.put(ipBytes)                         // ipBytes + NUL
        bb.putInt(videoPort)                    // u32 field1 (video)
        bb.putInt(audioPort)                    // u32 field2 (audio = 0)
        bb.putInt(metaPort)                     // u32 field3 (meta)
        bb.putShort(DELIVERY_JPEG.toShort())    // u16 deliveryType  (JPEG = 1)
        bb.put(QUALITY_LEVEL_3.toByte())        // u8  qualityLevel  (LEVEL_3 = 3)
        bb.putShort(PROTOCOL_TCP.toShort())     // u16 protocolType  (TCP = 2)
        return bb.array()
    }

    /**
     * DataOut payload for KeepAlive (sub=3) and Stop (sub=2), recovered byte-for-byte from the Sony
     * proremote serializer (r4.C2374b wrapper + r4.C2373a/e record). Little-endian, 12 bytes:
     *   u16 protocolVersion | u16 reserved=0 | u32 receiverCount=1 | u32 deliveryId
     * The delivery handle is the id the camera assigns in the Start (sub=1) response param[0].
     * Version reuses [PROTOCOL_VERSION] (101), which the camera already accepts for Start (Sony reads
     * it from the MonitoringBinaryVersion device prop; not worth a telemetry dependency for this).
     */
    private fun encodeDeliveryPayload(deliveryId: Int): ByteArray {
        val bb = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
        bb.putShort(PROTOCOL_VERSION.toShort()) // u16 protocolVersion
        bb.putShort(0)                          // u16 reserved (zero)
        bb.putInt(1)                            // u32 receiverCount
        bb.putInt(deliveryId)                   // u32 deliveryId
        return bb.array()
    }

    /** Log-only diagnostic: report JPEG SOI(FFD8)/EOI(FFD9) marker offsets in the first chunk. */
    private fun scanJpegMarkers(tag: String, buf: ByteArray, len: Int) {
        var soi = -1; var eoi = -1
        var i = 0
        while (i < len - 1) {
            if (buf[i] == 0xFF.toByte()) {
                val b = buf[i + 1]
                if (soi < 0 && b == 0xD8.toByte()) soi = i
                if (b == 0xD9.toByte()) eoi = i
            }
            i++
        }
        log("[$tag] JPEG scan: SOI(FFD8)@${if (soi >= 0) soi else "none"} EOI(FFD9)@${if (eoi >= 0) eoi else "none"}")
    }

    private fun fail(gen: Int, msg: String) {
        log("ERROR: $msg")
        if (gen == generation) update { copy(phase = PushLvStatus.Phase.ERROR, message = msg) }
        synchronized(this) { if (gen == generation) orchestrator = null }
    }

    private inline fun update(block: PushLvStatus.() -> PushLvStatus) {
        synchronized(_status) { _status.value = _status.value.block() }
    }

    private fun hex(buf: ByteArray, len: Int): String {
        val sb = StringBuilder(len * 3)
        for (i in 0 until len) sb.append("%02X ".format(buf[i]))
        return sb.toString().trim()
    }

    private fun log(m: String) = BleLog.line(BleLog.Kind.INFO, "[PUSH-LV] $m")

    companion object {
        private const val ACCEPT_TIMEOUT_MS = 8_000
        // 15s (not 30s): halves the gap between keepalive attempts so a single lost attempt
        // (occasional command-channel contention with the telemetry liveness poke) still leaves
        // a second attempt well before the camera's own idle timeout on the video/meta sockets.
        private const val KEEPALIVE_PERIOD_S = 15L
        private const val CAPTURE_CAP_BYTES = 1_048_576L // 1 MB per file
        private const val FIRST_DUMP_BYTES = 64
        // ---- Enum / version values recovered from Sony (Hermes: Lr4/d, Lr4/c, Lr4/c$a/$b/$c) ----
        private const val PROTOCOL_VERSION = 101  // Lr4/d protocolVersion; >=101 selects full receiver tail
        private const val PROTOCOL_TCP = 2        // Lr4/c$b TCP = 2
        private const val DELIVERY_JPEG = 1       // Lr4/c$a JPEG = 1
        private const val QUALITY_LEVEL_3 = 3     // Lr4/c$c LEVEL_3 = 3
    }
}
