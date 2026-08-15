package app.dyrecto.connection

import java.io.InputStream
import java.io.OutputStream
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import app.dyrecto.capability.RawForm

/**
 * Minimal PTP/IP (ISO 15740) initiator, run over an already-established transport
 * (the SSH direct channel to camera localhost:15740). Knows nothing about SSH/BLE.
 *
 * Flow implemented:
 *   Command Connection : InitCommandRequest -> InitCommandAck(connNo, GUID, name)
 *   Event   Connection : InitEventRequest(connNo) -> InitEventAck
 *   Command Connection : OpenSession(1)  -> OperationResponse
 *                        GetDeviceInfo    -> StartData/Data/EndData -> OperationResponse
 *
 * Every packet is dumped: type, opcode, transactionId, responseCode, payload len, raw hex.
 *
 * VERIFIED STACK — the wire protocol (packets sent, ordering, framing, parsing
 * offsets) is carried over UNCHANGED. The only additions are the [SessionResult]
 * /[DeviceInfo] capture: parsed values that were previously logged-only are now
 * also returned via [result], so the product layer can render them structurally.
 * No extra bytes are sent and no read logic is altered.
 */
class PtpIpClient(
    /** Opens a fresh duplex channel to (host, port) over the transport. */
    private val connect: (host: String, port: Int) -> Channel,
) {

    /** Transport channel abstraction so PTP code never sees SSHJ types. */
    interface Channel {
        val input: InputStream
        val output: OutputStream
        fun close()
    }

    // ---- Structured, additive result surface (logging is unchanged) ----

    /** Parsed PTP DeviceInfo (ISO 15740 dataset). All fields display-ready. */
    data class DeviceInfo(
        val standardVersion: Int,
        val vendorExtensionId: Long,
        val vendorExtensionVersion: Int,
        val vendorExtensionDesc: String,
        val functionalMode: Int,
        val operationsSupported: List<Int>,
        val eventsSupported: List<Int>,
        val devicePropertiesSupported: List<Int>,
        val captureFormats: List<Int>,
        val imageFormats: List<Int>,
        val manufacturer: String,
        val model: String,
        val deviceVersion: String,
        val serialNumber: String,
    )

    /** Outcome of one PTP/IP session attempt; mirrors what the run logs. */
    data class SessionResult(
        var initCommandAckOk: Boolean = false,
        var connectionNumber: Int? = null,
        var responderGuidHex: String? = null,
        var responderName: String? = null,
        var initEventAckOk: Boolean = false,
        var openSessionResponseCode: Int? = null,
        var getDeviceInfoResponseCode: Int? = null,
        var deviceInfo: DeviceInfo? = null,
        var getStorageIdsResponseCode: Int? = null,
        var storageIds: List<Long>? = null,
        var firstSuccessfulOp: String? = null,
        // ---- Sony SDIO telemetry (additive) ----
        var sdioConnectResponseCodes: List<Int> = emptyList(),
        var getAllExtDevicePropResponseCode: Int? = null,
        var telemetryRecordCount: Long? = null,
        var telemetry: Map<Int, SonyProp> = emptyMap(),
        var error: String? = null,
    )

    /** One device-property record from a 0x9209 (GetAllExtDevicePropInfo) dataset. */
    data class SonyProp(
        val propertyCode: Int,
        val dataType: Int,
        val getSet: Int,
        val availability: Int,
        val currentValueRaw: Long?,
        val currentValueStr: String?,
        val formFlag: Int,
        /** Legal-value set parsed from the form payload (RANGE/ENUM). Previously discarded. */
        val form: RawForm = RawForm.None,
    )

    /** Populated as [run] progresses; read after run() returns. */
    val result = SessionResult()

    /** Name of the step currently executing — used in failure diagnostics. */
    private var currentStage = "none"

    /** Running count of bytes received on the current read; updated by readFully. */
    private var lastBytesRx = 0

    // ---- PTP/IP packet types ----
    private object Pk {
        const val INIT_CMD_REQ = 1
        const val INIT_CMD_ACK = 2
        const val INIT_EVT_REQ = 3
        const val INIT_EVT_ACK = 4
        const val INIT_FAIL = 5
        const val OP_REQUEST = 6
        const val OP_RESPONSE = 7
        const val EVENT = 8
        const val START_DATA = 9
        const val DATA = 10
        const val CANCEL = 11
        const val END_DATA = 12
    }

    // ---- PTP operation / response codes ----
    private val OP_GET_DEVICE_INFO       = 0x1001
    private val OP_OPEN_SESSION          = 0x1002
    private val OP_GET_STORAGE_IDS       = 0x1004
    private val OP_GET_DEVICE_PROP_DESC  = 0x1014
    private val OP_GET_DEVICE_PROP_VALUE = 0x1015
    private val RC_OK = 0x2001

    // ---- Sony SDIO vendor operations (Monitor & Control telemetry path) ----
    private val OP_SDIO_CONNECT                  = 0x9201
    private val OP_SDIO_GET_EXT_DEVICE_INFO      = 0x9202
    private val OP_SDIO_GET_ALL_EXT_DEVPROP_INFO = 0x9209

    // ---- Sony SDIO push-monitoring (Push Live View PoC; opcode confirmed via runtime RE) ----
    /** SDIO_ControlMonitoring — opcode + sub-commands. Public so the PoC layer can address it
     *  through the generic [sendSonyOperation] without re-declaring the constant. */
    object Monitoring {
        const val OPCODE = 0x9230
        const val START = 1
        const val STOP = 2
        const val KEEP_ALIVE = 3
    }

    // ---- PTP datatype codes (ISO 15740) used by the 0x9209 dataset ----
    private val DT_INT8 = 0x0001; private val DT_UINT8 = 0x0002
    private val DT_INT16 = 0x0003; private val DT_UINT16 = 0x0004
    private val DT_INT32 = 0x0005; private val DT_UINT32 = 0x0006
    private val DT_INT64 = 0x0007; private val DT_UINT64 = 0x0008
    private val DT_STR = 0xFFFF

    // ---- 0x9209 form flags ----
    private val FORM_NONE = 0; private val FORM_RANGE = 1; private val FORM_ENUM = 2

    /** Known Sony telemetry property codes, in summary-log order. */
    private val TELEMETRY_CODES = intArrayOf(
        0xD20E, 0xD218, 0xD038, 0xD039,          // battery
        0xD21E, 0xD023,                          // ISO
        0x5007, 0xD000,                          // aperture / iris
        0xD20D, 0xD016, 0xD017,                  // shutter
        0x5005, 0xD00C, 0xD20F,                  // white balance
        0x500A, 0xD007, 0xE044, 0xE004, 0xE005,  // focus
        0xD21D, 0xD261, 0xE010,                  // recording
        0xD248, 0xD24A, 0xD256, 0xD258,          // media slots
        0xD251, 0xD049, 0xD221, 0xD060, 0xE098,  // thermal / live state
    )

    /** Probed when DeviceInfo carries no property list (Sony FX3 common subset). */
    private val FALLBACK_PROPS = intArrayOf(
        0x5001,  // BatteryLevel
        0x5004,  // CompressionSetting
        0x5005,  // WhiteBalance
        0x5007,  // FNumber
        0x500D,  // ShutterSpeed
    )

    private val SESSION_ID = 1

    /** Legacy generic probe (StorageIDs/PropDesc/PropValue) — bypassed in favour
     *  of the Sony SDIO telemetry path. Flip to re-enable for diagnostics. */
    private val runLegacyProbe = false

    data class Packet(val type: Int, val body: ByteArray)
    data class OpResult(
        val responseCode: Int,
        val transactionId: Int,
        val data: ByteArray,
        /** PTP OperationResponse parameters (the u32s after responseCode+transactionId). Previously
         *  discarded; surfaced for the Push Live View delivery-handle experiment (Start returns a
         *  per-delivery id in param[0]). Empty for responses that carry no parameters. */
        val responseParams: IntArray = IntArray(0),
    )

    private var commandCh: Channel? = null
    private var eventCh: Channel? = null

    // ---- Event-driven telemetry refresh (additive; matches Sony Monitor & Control) ----

    /** Invoked on the listener thread after each successful 0x9209 [1,1] refresh. */
    var onTelemetryRefreshed: ((Map<Int, SonyProp>) -> Unit)? = null

    /** True once the event listener has taken explicit ownership of the sockets;
     *  while true, run()'s finally must NOT close them — the listener owns them. */
    @Volatile private var ownershipTransferred = false

    /** Guards: at most one listener, one in-flight refresh, one socket close. */
    private val listenerActive = AtomicBoolean(false)
    private val refreshInProgress = AtomicBoolean(false)
    private val socketsClosed = AtomicBoolean(false)

    /** True once the event listener has taken ownership of the channels. Only meaningful on a
     *  fresh, never-stopped client (listenerActive is not reset by stopEventListener) — which is
     *  exactly how SshTunnelTester.run()'s finally reads it to decide whether to retain the tunnel. */
    val isEventListenerActive: Boolean get() = listenerActive.get()

    /** Serializes ALL command-channel PTP I/O (sendOperation). ReentrantLock (not a plain
     *  monitor) purely to get a nanoTime() hook around lock() for temporary contention
     *  instrumentation (see [CommandChannelStats]) — every caller still blocks exactly as
     *  a bare `synchronized` would; no scheduling behavior has changed yet. */
    private val ptpLock = ReentrantLock()

    @Volatile private var listening = false
    @Volatile private var firstEventReceived = false
    private var eventThread: Thread? = null

    /** Periodic session-liveness poke. The event listener blocks on the event channel, so when the
     *  camera is idle (no events) nothing touches the connection and the camera closes it after
     *  ~10 s. This timer sends the SAME known-good 0x9209 [1,1] telemetry refresh on the COMMAND
     *  channel every [LIVENESS_INTERVAL_MS] while listening, keeping the session alive. No new
     *  protocol bytes, no reconnect — just the existing refresh op on a conservative interval. */
    @Volatile private var livenessThread: Thread? = null
    private val LIVENESS_INTERVAL_MS = 5_000L

    /** Read deadline (see [readFully]). The first connect can legitimately be slow, so bootstrap
     *  reads get a generous window; once the session is established a stalled command op (e.g. the
     *  camera briefly going quiet on the command channel while a monitoring stream is torn down) is
     *  abandoned much sooner so it can't freeze telemetry for long. Set true by [startEventListener].
     *
     *  Do NOT try to fix Push Live View's ~60s socket death by widening this deadline. TWO variants
     *  were tried on hardware and BOTH failed: (1) a global 3s→8s widen cascaded into a 16144ms stall
     *  on a frequent 0x9209 read; (2) a per-path 8s budget for [sendSonyOperation] alone still saw the
     *  KeepAlive time out AND starved a background liveness tick for the full 8s. Crucially, the camera
     *  DOES reply rc=OK to every 0x9230 KeepAlive (arriving ~100ms after we give up, so a [RESYNC]
     *  drops it) yet STILL closes the video push socket at ~60s — i.e. completing the KeepAlive is not
     *  what keeps the stream alive. The 60s death is unrelated to this read budget; see the
     *  push-lv-keepalive-investigation memory (leading hypothesis: metadata receiver never connects). */
    @Volatile private var steadyState = false
    private val BOOTSTRAP_READ_TIMEOUT_MS = 10_000L
    private val STEADY_READ_TIMEOUT_MS = 3_000L

    /** Set by [readFully] on each null return: true = gave up on the read deadline (socket likely
     *  still alive, camera just briefly unresponsive), false = real EOF (peer closed the socket).
     *  Lets [refreshTelemetry] treat a transient stall as non-fatal instead of tearing the session
     *  down. Read immediately after a null op result, under the same ptpLock/refresh serialization. */
    @Volatile private var lastReadTimedOut = false

    /** Set true ONLY by stopEventListener(); lets the loop tell an intentional stop apart
     *  from an unexpected thread termination (so we never "auto-restart" a deliberate stop). */
    @Volatile private var stopRequested = false

    /** Bounded auto-restart budget for confirmed UNEXPECTED listener-thread termination
     *  (an exception thrown in the read loop while the channel is still open). Diagnostics only
     *  otherwise — no watchdog, keepalive, socket-timeout, or reconnect behavior is added here. */
    private val maxListenerRestarts = 3

    /** Next free transaction id. Written once at end of bootstrap, then read/incremented
     *  ONLY on the listener thread (single-thread-after-transfer). */
    @Volatile private var nextTid = 0

    /** Coalesces the burst of property-change events Sony emits for one user action. */
    @Volatile private var lastRefreshMs = 0L

    /** Runs the whole sequence. Returns true if GetDeviceInfo response == OK. */
    fun run(t: SshTunnelTester.Timing): Boolean {
        try {
            // ---------- 1. Command Connection + InitCommandRequest ----------
            currentStage = "TCP-command-connect"
            log("opening COMMAND channel -> localhost:15740")
            val cmd = connect("localhost", 15740).also { commandCh = it }

            currentStage = "InitCommandRequest"
            val initReq = buildInitCommandRequest("CameraAssistant")
            t.ptpInitSentAt = System.currentTimeMillis()
            dumpOut("InitCommandRequest", initReq)
            cmd.output.write(initReq); cmd.output.flush()

            currentStage = "await-InitCommandAck"
            val ack = readPacket(cmd.input)
            if (ack == null) {
                err("FAILED at $currentStage — channel closed / 15740 unreachable (lastRx=$lastBytesRx bytes)")
                result.error = "no InitCommandAck (15740 unreachable)"
                return false
            }
            t.ptpAckAt = System.currentTimeMillis()
            t.ptpAckType = ack.type
            dumpIn("InitResponse", ack)
            if (ack.type == Pk.INIT_FAIL) {
                err("FAILED at $currentStage — INIT_FAIL (type 5): ${hex(ack.body)}")
                result.error = "InitCommand FAIL"
                return false
            }
            if (ack.type != Pk.INIT_CMD_ACK) {
                err("FAILED at $currentStage — expected InitCommandAck(2), got type=${ack.type}")
                result.error = "unexpected init response type=${ack.type}"
                return false
            }
            val connNo = leInt(ack.body, 0)
            val guidHex = hex(ack.body.copyOfRange(4, minOf(20, ack.body.size)))
            result.initCommandAckOk = true
            result.connectionNumber = connNo
            result.responderGuidHex = guidHex
            result.responderName = parseResponderName(ack.body)
            log("InitCommandAck ✔ connectionNumber=$connNo responderGUID=$guidHex")

            // ---------- 2. Event Connection + InitEventRequest ----------
            currentStage = "TCP-event-connect"
            log("opening EVENT channel -> localhost:15740")
            val evt = connect("localhost", 15740).also { eventCh = it }

            currentStage = "InitEventRequest"
            val evtReq = buildInitEventRequest(connNo)
            dumpOut("InitEventRequest", evtReq)
            evt.output.write(evtReq); evt.output.flush()

            currentStage = "await-InitEventAck"
            val evtAck = readPacket(evt.input)
            if (evtAck == null) {
                err("FAILED at $currentStage — event channel closed (lastRx=$lastBytesRx bytes)")
                result.error = "no InitEventAck"
                return false
            }
            dumpIn("InitEventResponse", evtAck)
            if (evtAck.type != Pk.INIT_EVT_ACK) {
                err("FAILED at $currentStage — expected InitEventAck(4), got type=${evtAck.type}")
                if (evtAck.type == Pk.INIT_FAIL) err("event INIT FAIL: ${hex(evtAck.body)}")
                result.error = "unexpected event ack type=${evtAck.type}"
                return false
            }
            result.initEventAckOk = true
            log("InitEventAck ✔ — command + event channels established")

            // ---------- 3a. OpenSession ----------
            currentStage = "OpenSession"
            log("=== OpenSession (op=0x1002 session=$SESSION_ID tid=0) ===")
            val open = sendOperation(
                cmd, opcode = OP_OPEN_SESSION, transactionId = 0,
                params = intArrayOf(SESSION_ID), dataOut = null,
            )
            if (open == null) {
                err("FAILED at $currentStage — no response packet (lastRx=$lastBytesRx bytes)")
                dumpFramingHint()
                result.error = "OpenSession: no response"
                return false
            }
            logOpVerbose("OpenSession", OP_OPEN_SESSION, open)
            result.openSessionResponseCode = open.responseCode
            if (open.responseCode != RC_OK) {
                err("FAILED at $currentStage — responseCode=0x%04X".format(open.responseCode))
                dumpFramingHint()
                result.error = "OpenSession rc=0x%04X".format(open.responseCode)
                return false
            }
            log("OpenSession OK (0x2001) ✔")

            // ---- Post-session probe: try each standard op; continue on rejection,
            //      abort only if the socket closes (sendOperation returns null). ----
            var tid = 2
            var anyOpSucceeded = false

            // ---------- GetDeviceInfo ----------
            currentStage = "GetDeviceInfo"
            log("=== GetDeviceInfo op=${opName(OP_GET_DEVICE_INFO)} (0x%04X) tid=$tid ===".format(OP_GET_DEVICE_INFO))
            val info = sendOperation(cmd, OP_GET_DEVICE_INFO, tid++, IntArray(0), null)
            if (info == null) {
                err("FAILED at $currentStage — socket closed (lastRx=$lastBytesRx bytes)")
                result.error = "socket closed during GetDeviceInfo"
                return false
            }
            logOpVerbose("GetDeviceInfo", OP_GET_DEVICE_INFO, info)
            result.getDeviceInfoResponseCode = info.responseCode
            if (info.responseCode == RC_OK) {
                anyOpSucceeded = true
                if (result.firstSuccessfulOp == null) result.firstSuccessfulOp = "GetDeviceInfo"
                if (info.data.isNotEmpty()) {
                    log("  payload ${info.data.size} bytes: ${hex(info.data)}")
                    result.deviceInfo = parseDeviceInfo(info.data)
                }
            } else {
                log("  GetDeviceInfo rejected: ${rcName(info.responseCode)} — continuing probe")
            }

            // ---------- Sony SDIO telemetry bootstrap (0x9201/0x9202/0x9209) ----------
            log("ENTER Sony SDIO telemetry path")
            when (val sdio = runSonyTelemetry(cmd, tid)) {
                null -> {
                    // socket aborted mid-bootstrap; runSonyTelemetry already logged + set error
                    return false
                }
                else -> {
                    tid = sdio
                    nextTid = tid
                    if (result.telemetry.isNotEmpty() || result.telemetryRecordCount != null) {
                        anyOpSucceeded = true
                        if (result.firstSuccessfulOp == null)
                            result.firstSuccessfulOp = "SDIO_GetAllExtDevicePropInfo"
                    }
                }
            }

            // ---------- legacy generic probe (bypassed unless runLegacyProbe) ----------
            if (!runLegacyProbe) log("legacy probe skipped because runLegacyProbe=false")
            if (runLegacyProbe) {
            // ---------- GetStorageIDs ----------
            currentStage = "GetStorageIDs"
            log("=== GetStorageIDs op=${opName(OP_GET_STORAGE_IDS)} (0x%04X) tid=$tid ===".format(OP_GET_STORAGE_IDS))
            val storIds = sendOperation(cmd, OP_GET_STORAGE_IDS, tid++, IntArray(0), null)
            if (storIds == null) {
                err("FAILED at $currentStage — socket closed (lastRx=$lastBytesRx bytes)")
                result.error = result.error ?: "socket closed during GetStorageIDs"
                return false
            }
            logOpVerbose("GetStorageIDs", OP_GET_STORAGE_IDS, storIds)
            result.getStorageIdsResponseCode = storIds.responseCode
            if (storIds.responseCode == RC_OK) {
                anyOpSucceeded = true
                if (result.firstSuccessfulOp == null) result.firstSuccessfulOp = "GetStorageIDs"
                if (storIds.data.isNotEmpty()) {
                    log("  payload ${storIds.data.size} bytes: ${hex(storIds.data)}")
                    parseStorageIds(storIds.data)
                }
            } else {
                log("  GetStorageIDs rejected: ${rcName(storIds.responseCode)} — continuing probe")
            }

            // ---------- DeviceProp probe ----------
            val propsToProbe = result.deviceInfo?.devicePropertiesSupported
                ?.take(5)?.toIntArray() ?: FALLBACK_PROPS
            log("=== probing ${propsToProbe.size} device properties ===")

            for (propCode in propsToProbe) {
                val propHex = "0x%04X".format(propCode)
                val propLabel = propName(propCode)

                // GetDevicePropDesc
                currentStage = "GetDevicePropDesc($propHex)"
                log("--- GetDevicePropDesc prop=$propHex $propLabel tid=$tid ---")
                val desc = sendOperation(cmd, OP_GET_DEVICE_PROP_DESC, tid++, intArrayOf(propCode), null)
                if (desc == null) {
                    err("FAILED at $currentStage — socket closed (lastRx=$lastBytesRx bytes)")
                    return false
                }
                logOpVerbose("GetDevicePropDesc($propHex)", OP_GET_DEVICE_PROP_DESC, desc)
                if (desc.responseCode == RC_OK) {
                    anyOpSucceeded = true
                    if (result.firstSuccessfulOp == null) result.firstSuccessfulOp = "GetDevicePropDesc($propHex)"
                    if (desc.data.isNotEmpty()) {
                        log("  desc payload ${desc.data.size} bytes: ${hex(desc.data)}")
                        parseDevicePropDesc(propCode, desc.data)
                    }
                } else {
                    log("  GetDevicePropDesc($propHex) rejected: ${rcName(desc.responseCode)}")
                }

                // GetDevicePropValue
                currentStage = "GetDevicePropValue($propHex)"
                log("--- GetDevicePropValue prop=$propHex $propLabel tid=$tid ---")
                val value = sendOperation(cmd, OP_GET_DEVICE_PROP_VALUE, tid++, intArrayOf(propCode), null)
                if (value == null) {
                    err("FAILED at $currentStage — socket closed (lastRx=$lastBytesRx bytes)")
                    return false
                }
                logOpVerbose("GetDevicePropValue($propHex)", OP_GET_DEVICE_PROP_VALUE, value)
                if (value.responseCode == RC_OK) {
                    anyOpSucceeded = true
                    if (result.firstSuccessfulOp == null) result.firstSuccessfulOp = "GetDevicePropValue($propHex)"
                    if (value.data.isNotEmpty()) {
                        log("  value payload ${value.data.size} bytes: ${hex(value.data)}")
                    }
                } else {
                    log("  GetDevicePropValue($propHex) rejected: ${rcName(value.responseCode)}")
                }
            }
            } // end if (runLegacyProbe)

            if (anyOpSucceeded) {
                log("=== probe complete — firstSuccess=${result.firstSuccessfulOp} ✔ ===")
            } else {
                err("=== probe complete — session open but every operation was rejected ===")
            }

            // Keep the channels open and listen for unsolicited Sony property-change
            // events; each event triggers an incremental 0x9209 [1,1] refresh. Only
            // start when we actually have a telemetry snapshot to refresh.
            if (result.telemetry.isNotEmpty()) startEventListener()

            return anyOpSucceeded
        } catch (e: Exception) {
            err("FAILED at $currentStage")
            err("Exception=${e.javaClass.simpleName}")
            err("Message=${e.message}")
            err("LastRx=$lastBytesRx bytes")
            e.stackTrace.take(6).forEach { err("    at $it") }
            result.error = "${e.javaClass.simpleName}: ${e.message}"
            return false
        } finally {
            // If the event listener took ownership of the sockets, it (and only it)
            // is responsible for closing them — leave them open here.
            if (!ownershipTransferred) closeChannels()
        }
    }

    // ----------------------------------------- event-driven telemetry refresh

    /** Closes both channels exactly once (shared by run()'s finally and the listener). */
    private fun closeChannels() {
        if (socketsClosed.compareAndSet(false, true)) {
            runCatching { eventCh?.close() }
            runCatching { commandCh?.close() }
            log("sockets closed")
        }
    }

    /**
     * Transfers socket ownership to a background listener thread that reads the PTP/IP
     * event channel and, on each unsolicited event, fires an incremental 0x9209 [1,1]
     * refresh. Event-driven only — no polling, no timers. Idempotent.
     */
    private fun startEventListener() {
        if (!listenerActive.compareAndSet(false, true)) return
        ownershipTransferred = true
        listening = true
        steadyState = true // bootstrap done — shorten the command-read deadline
        stopRequested = false
        firstEventReceived = false
        log("listener started")
        eventThread = thread(name = "ptp-event-listener") { eventLoop() }
        startLivenessLoop()
        // One-shot diagnostic (NOT polling/refresh): warn once if the camera stays silent.
        thread(name = "ptp-event-watchdog", isDaemon = true) {
            runCatching { Thread.sleep(30_000) }
            if (listening && !firstEventReceived) {
                log("No PTP event received yet — listener still active")
            }
        }
    }

    /** Outcome of one run of the inner read loop (no exception). */
    private enum class PumpResult { EOF, STOPPED }

    /**
     * Owns the event-channel read loop. Distinguishes the three terminal conditions so the
     * caller can react correctly:
     *  - returns [PumpResult.EOF]      → readPacket got null (socket closed by peer / EOF)
     *  - returns [PumpResult.STOPPED]  → `listening` went false (intentional stop, or a refresh
     *                                    that detected a dead socket)
     *  - throws                        → unexpected failure in the loop body (the channel itself
     *                                    may still be alive, e.g. a parse/dispatch bug)
     */
    private fun pumpEvents(evt: Channel): PumpResult {
        while (listening) {
            log("[STALL-DIAG] awaiting event read (blocking) ts=${System.currentTimeMillis()}")
            val p = readPacket(evt.input, blocking = true) ?: return PumpResult.EOF
            log("[STALL-DIAG] event read returned type=${p.type} bodyLen=${p.body.size} ts=${System.currentTimeMillis()}")
            when (p.type) {
                Pk.EVENT -> {
                    firstEventReceived = true
                    log("event packet raw hex=${hex(p.body)}")
                    val eventCode = if (p.body.size >= 2) leShort(p.body, 0) else 0
                    val params = ArrayList<Int>()
                    var off = 6
                    while (off + 4 <= p.body.size) { params.add(leInt(p.body, off)); off += 4 }
                    log("event code=0x%04X params=%s".format(eventCode, params.joinToString(",")))
                    if (shouldRefresh()) refreshTelemetry()
                    else log("refresh skipped (debounced)")
                }
                else -> dumpIn("Event-channel type=${p.type}", p)
            }
        }
        return PumpResult.STOPPED
    }

    /**
     * Drives [pumpEvents] and clearly logs how the listener thread terminates. A confirmed
     * UNEXPECTED termination (an exception thrown while the channel is still open and no stop
     * was requested) is auto-restarted on the SAME channel, up to [maxListenerRestarts] times —
     * this eliminates a code-level loop-death bug as a permanent failure mode WITHOUT adding any
     * watchdog/keepalive/socket-timeout/reconnect behavior. EOF (socket closed) is NOT restarted,
     * since that would require re-establishing the connection.
     */
    private fun eventLoop() {
        val evt = eventCh
        if (evt == null) { listening = false; return }
        var restarts = 0
        var cleanStop = false
        try {
            loop@ while (listening) {
                val result = try {
                    pumpEvents(evt)
                } catch (e: Exception) {
                    if (stopRequested) {
                        log("[STALL-DIAG] listener exception during requested stop: ${e.javaClass.simpleName}: ${e.message}")
                        break@loop
                    }
                    err("[STALL-DIAG] LISTENER THREAD TERMINATED UNEXPECTEDLY: ${e.javaClass.name}: ${e.message}")
                    e.stackTrace.take(6).forEach { err("    at $it") }
                    when {
                        socketsClosed.get() -> {
                            err("[STALL-DIAG] event channel already closed — cannot restart, stopping listener")
                            break@loop
                        }
                        restarts >= maxListenerRestarts -> {
                            err("[STALL-DIAG] restart budget ($maxListenerRestarts) exhausted — stopping listener")
                            break@loop
                        }
                        else -> {
                            restarts++
                            err("[STALL-DIAG] auto-restarting event read loop ($restarts/$maxListenerRestarts) on existing channel")
                            continue@loop
                        }
                    }
                }
                when (result) {
                    PumpResult.EOF -> {
                        err("[STALL-DIAG] event channel returned EOF (socket closed by peer) — stopping listener")
                        break@loop
                    }
                    PumpResult.STOPPED -> {
                        cleanStop = stopRequested
                        log("[STALL-DIAG] listener loop stopped (listening=false, stopRequested=$stopRequested)")
                        break@loop
                    }
                }
            }
        } finally {
            listening = false
            stopLivenessLoop()
            closeChannels()
            log("listener stopped (cleanStop=$cleanStop, restarts=$restarts)")
        }
    }

    /** Starts the periodic liveness poke (idempotent). Runs only while [listening]. Also drives
     *  the TEMP [CommandChannelStats] snapshot every 6th tick (~30s at the 5s interval) — piggy-
     *  backing on this existing thread rather than starting a new one for temporary diagnostics. */
    private fun startLivenessLoop() {
        if (livenessThread != null) return
        livenessThread = thread(name = "ptp-liveness", isDaemon = true) {
            log("[LIVENESS] loop started (interval=${LIVENESS_INTERVAL_MS}ms)")
            var tick = 0
            while (listening) {
                try {
                    Thread.sleep(LIVENESS_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    break
                }
                if (!listening) break
                livenessTick()
                if (++tick % 6 == 0) CommandChannelStats.snapshotAndLog(::log)
            }
            log("[LIVENESS] loop stopped")
        }
    }

    /** Stops the liveness loop (idempotent). */
    private fun stopLivenessLoop() {
        livenessThread?.interrupt()
        livenessThread = null
    }

    /**
     * One liveness poke: sends the existing known-good 0x9209 [1,1] telemetry refresh on the command
     * channel, serialized through [ptpLock] (via [sendOperation]) and the [refreshInProgress] guard so
     * it never overlaps an event-driven refresh. Re-parses telemetry exactly like [refreshTelemetry].
     * Never throws. Does NOT change protocol bytes or add reconnect logic.
     */
    private fun livenessTick() {
        val ch = commandCh
        if (!listening || ch == null) {
            log("[LIVENESS] skipped — channel unavailable (listening=$listening, ch=${ch != null})")
            return
        }
        if (!refreshInProgress.compareAndSet(false, true)) {
            log("[LIVENESS] tick skipped — a refresh is already in progress")
            return
        }
        val tid = nextTid++
        log("[LIVENESS] tick — sending 0x9209 [1,1] tid=$tid")
        try {
            val r = sendOperation(ch, OP_SDIO_GET_ALL_EXT_DEVPROP_INFO, tid, intArrayOf(1, 1), null)
            when {
                r == null && lastReadTimedOut ->
                    err("[LIVENESS] FAILURE tid=$tid — transient timeout, keep going (camera briefly unresponsive)")
                r == null -> err("[LIVENESS] FAILURE tid=$tid — no response (socket closed)")
                r.responseCode != RC_OK || r.data.isEmpty() ->
                    err("[LIVENESS] non-OK tid=$tid rc=${rcName(r.responseCode)} (0x%04X) len=${r.data.size}".format(r.responseCode))
                else -> {
                    val map = parseAllExtDevicePropInfo(r.data)
                    result.telemetry = map
                    log("[LIVENESS] OK tid=$tid rc=OK props=${map.size}")
                    runCatching { onTelemetryRefreshed?.invoke(map) }
                }
            }
        } catch (e: Exception) {
            err("[LIVENESS] exception tid=$tid — ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            refreshInProgress.set(false)
        }
    }

    /** Event-gated debounce — only consulted when an event arrives (no background ticking). */
    private fun shouldRefresh(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastRefreshMs < 150) return false
        lastRefreshMs = now
        return true
    }

    /**
     * Sends 0x9209 [1,1] and re-parses with the existing (unchanged) parser. Runs only on
     * the listener thread. Never throws; on a non-fatal failure it keeps listening, on a
     * dead socket it stops the loop. At most one refresh runs at a time.
     */
    private fun refreshTelemetry() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            log("[STALL-DIAG] refresh skipped — one already in progress")
            return
        }
        val startedAt = System.currentTimeMillis()
        try {
            log("[STALL-DIAG] refresh started (0x9209 [1,1]) ts=$startedAt")
            val r = sendOperation(commandCh!!, OP_SDIO_GET_ALL_EXT_DEVPROP_INFO, nextTid++, intArrayOf(1, 1), null)
            val elapsed = System.currentTimeMillis() - startedAt
            when {
                r == null && lastReadTimedOut -> {
                    // Transient: the camera went briefly quiet on the command channel (e.g. while a
                    // monitoring/live-view stream is torn down). Don't kill the session — skip this
                    // refresh and let the next event/liveness tick recover. The ptpLock is released
                    // in finally, so telemetry resumes within a tick instead of stalling for long.
                    err("[STALL-DIAG] refresh timed out after ${elapsed}ms — keep listening (camera briefly unresponsive)")
                }
                r == null -> {
                    err("[STALL-DIAG] refresh failed — socket closed (after ${elapsed}ms)")
                    listening = false   // real EOF: loop will exit and close sockets
                }
                r.responseCode != RC_OK || r.data.isEmpty() -> {
                    err("[STALL-DIAG] refresh failed rc=${rcName(r.responseCode)} (0x%04X) len=${r.data.size} after ${elapsed}ms — keep listening"
                        .format(r.responseCode))
                }
                else -> {
                    val map = parseAllExtDevicePropInfo(r.data)
                    result.telemetry = map
                    log("[STALL-DIAG] refresh success — payloadLen=${r.data.size} recordCount=${result.telemetryRecordCount} props=${map.size} after ${elapsed}ms")
                    val cbResult = runCatching { onTelemetryRefreshed?.invoke(map) }
                    if (cbResult.isFailure) {
                        err("[STALL-DIAG] onTelemetryRefreshed callback threw: ${cbResult.exceptionOrNull()?.javaClass?.simpleName}: ${cbResult.exceptionOrNull()?.message}")
                    } else {
                        log("[STALL-DIAG] onTelemetryRefreshed invoked ok")
                    }
                }
            }
        } catch (e: Exception) {
            err("[STALL-DIAG] refresh failed — exception ${e.javaClass.simpleName}: ${e.message} after ${System.currentTimeMillis() - startedAt}ms")
        } finally {
            refreshInProgress.set(false)
        }
    }

    /**
     * Stops the listener and closes both sockets exactly once. Safe to call from the UI/main
     * thread — never blocks (no join here). Idempotent.
     */
    fun stopEventListener() {
        stopRequested = true
        listening = false
        stopLivenessLoop()
        closeChannels()
        runCatching { eventThread?.interrupt() }
    }

    // ----------------------------------------------------- operation exchange

    /**
     * Sends an Operation Request and consumes the full response: optional data
     * phase (StartData / Data* / EndData) followed by the OperationResponse(7).
     * Async Event(8) packets on the command channel are logged and skipped.
     */
    private fun sendOperation(
        ch: Channel,
        opcode: Int,
        transactionId: Int,
        params: IntArray,
        dataOut: ByteArray?,
    ): OpResult? {
        val waitStart = System.nanoTime()
        ptpLock.lock()
        val waitNs = System.nanoTime() - waitStart
        val holdStart = System.nanoTime()
        try {
            return sendOperationLocked(ch, opcode, transactionId, params, dataOut)
        } finally {
            CommandChannelStats.recordBackground(waitNs, System.nanoTime() - holdStart)
            ptpLock.unlock()
        }
    }

    /**
     * Additive PoC hook (Push Live View): sends a Sony SDIO operation on the live command channel
     * and returns its [OpResult], or null if the channel is closed. Reuses [ptpLock] so it serializes
     * with the event-listener's telemetry refresh, and takes its transaction id from [nextTid] (the
     * same counter the listener uses) inside that lock. Safe to call from any thread.
     *
     * This does NOT change the verified telemetry path — it only borrows the existing send machinery.
     */
    fun sendSonyOperation(opcode: Int, params: IntArray, dataOut: ByteArray?): OpResult? {
        val ch = commandCh ?: run {
            err("[PUSH-LV] sendSonyOperation op=0x%04X — command channel is null (not connected)".format(opcode))
            return null
        }
        val waitStart = System.nanoTime()
        ptpLock.lock()
        val waitNs = System.nanoTime() - waitStart
        val holdStart = System.nanoTime()
        try {
            val tid = nextTid++
            log("[PUSH-LV] sendSonyOperation op=0x%04X tid=%d waitedForLockMs=%d params=%s dataOut=%dB"
                .format(opcode, tid, waitNs / 1_000_000, params.joinToString(",", "[", "]"), dataOut?.size ?: 0))
            val r = sendOperationLocked(ch, opcode, tid, params, dataOut)
            if (r == null && lastReadTimedOut)
                err("[PUSH-LV] op=0x%04X tid=%d — transient timeout, keep going (camera briefly unresponsive)".format(opcode, tid))
            else if (r == null) err("[PUSH-LV] op=0x%04X tid=%d — no response (socket closed)".format(opcode, tid))
            else log("[PUSH-LV] op=0x%04X tid=%d rc=%s (0x%04X) dataIn=%dB respParams=%s"
                .format(opcode, tid, rcName(r.responseCode), r.responseCode, r.data.size,
                    r.responseParams.joinToString(",", "[", "]")))
            return r
        } finally {
            CommandChannelStats.recordHighPriority(waitNs, System.nanoTime() - holdStart)
            ptpLock.unlock()
        }
    }

    /** Public name lookup for SDIO response codes (used by the Push LV PoC reporting). */
    fun responseCodeName(code: Int): String = rcName(code)

    /** Body of [sendOperation]; always called while holding [ptpLock]. */
    private fun sendOperationLocked(
        ch: Channel,
        opcode: Int,
        transactionId: Int,
        params: IntArray,
        dataOut: ByteArray?,
    ): OpResult? {
        val req = buildOperationRequest(opcode, transactionId, params, dataPhaseOut = dataOut != null)
        dumpOut("OperationRequest op=0x%04X tid=%d".format(opcode, transactionId), req)
        ch.output.write(req); ch.output.flush()

        if (dataOut != null) {
            val sd = buildStartData(transactionId, dataOut.size.toLong())
            val ed = buildEndData(transactionId, dataOut)
            dumpOut("StartData", sd); ch.output.write(sd)
            dumpOut("EndData", ed); ch.output.write(ed)
            ch.output.flush()
        }

        val collected = ArrayList<Byte>()
        var expectedDataLen = -1L
        while (true) {
            val p = readPacket(ch.input) ?: return null
            when (p.type) {
                Pk.START_DATA -> {
                    expectedDataLen = if (p.body.size >= 12) leLong(p.body, 4) else -1L
                    dumpIn("StartData (totalLen=$expectedDataLen)", p)
                }
                Pk.DATA -> {
                    dumpIn("Data", p)
                    appendDataPayload(collected, p.body)
                }
                Pk.END_DATA -> {
                    dumpIn("EndData", p)
                    appendDataPayload(collected, p.body)
                }
                Pk.EVENT -> dumpIn("Event (async, skipped)", p)
                Pk.OP_RESPONSE -> {
                    dumpIn("OperationResponse", p)
                    val rc = leShort(p.body, 0)
                    val tid = leInt(p.body, 2)
                    // Re-sync guard: if a prior op timed out and we abandoned its read, the camera may
                    // deliver that late response now. It carries an OLDER transaction id — drop it
                    // (and any data we accumulated for it) and keep reading for OUR tid, instead of
                    // returning a stale result and going permanently off-by-one. Happy path (tid ==
                    // transactionId) is unchanged.
                    if (tid != transactionId && tid in 1 until transactionId) {
                        err("[RESYNC] dropping stale OperationResponse tid=$tid rc=0x%04X (awaiting $transactionId)".format(rc))
                        collected.clear()
                        expectedDataLen = -1L
                        continue
                    }
                    // Parse the OperationResponse parameters: body = responseCode(2) + tid(4) + params(4 each).
                    val nParams = if (p.body.size > 6) (p.body.size - 6) / 4 else 0
                    val params = IntArray(nParams) { leInt(p.body, 6 + it * 4) }
                    return OpResult(rc, tid, collected.toByteArray(), params)
                }
                else -> dumpIn("Unexpected type=${p.type}", p)
            }
        }
    }

    /** Data/EndData body = transactionId(4) + payload. */
    private fun appendDataPayload(into: ArrayList<Byte>, body: ByteArray) {
        if (body.size > 4) for (i in 4 until body.size) into.add(body[i])
    }

    // ------------------------------------------------------- packet builders

    private fun buildInitCommandRequest(name: String): ByteArray {
        val guid = randomGuid()
        val nameBytes = name.toByteArray(Charsets.UTF_16LE) + byteArrayOf(0, 0)
        val len = 8 + 16 + nameBytes.size + 4
        return ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(len); putInt(Pk.INIT_CMD_REQ)
            put(guid); put(nameBytes); putInt(0x00010000)
        }.array()
    }

    private fun buildInitEventRequest(connectionNumber: Int): ByteArray {
        val len = 12
        return ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(len); putInt(Pk.INIT_EVT_REQ); putInt(connectionNumber)
        }.array()
    }

    private fun buildOperationRequest(
        opcode: Int, transactionId: Int, params: IntArray, dataPhaseOut: Boolean,
    ): ByteArray {
        val len = 4 + 4 + 4 + 2 + 4 + params.size * 4
        return ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(len)
            putInt(Pk.OP_REQUEST)
            putInt(if (dataPhaseOut) 2 else 1)     // DataPhaseInfo: 1=in/none, 2=out
            putShort(opcode.toShort())
            putInt(transactionId)
            params.forEach { putInt(it) }
        }.array()
    }

    private fun buildStartData(transactionId: Int, totalLen: Long): ByteArray =
        ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(20); putInt(Pk.START_DATA); putInt(transactionId); putLong(totalLen)
        }.array()

    private fun buildEndData(transactionId: Int, payload: ByteArray): ByteArray {
        val len = 12 + payload.size
        return ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN).apply {
            putInt(len); putInt(Pk.END_DATA); putInt(transactionId); put(payload)
        }.array()
    }

    private fun randomGuid(): ByteArray =
        ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN).apply {
            val u = UUID.randomUUID(); putLong(u.mostSignificantBits); putLong(u.leastSignificantBits)
        }.array()

    // -------------------------------------------------------- packet reading

    /** Reads one PTP/IP packet: length(4) + type(4) + body(length-8).
     *  [blocking]=true waits indefinitely for the first bytes (event channel); the default
     *  applies the 10-second bootstrap deadline (behavior unchanged). */
    private fun readPacket(inp: InputStream, blocking: Boolean = false): Packet? {
        val header = readFully(inp, 8, blocking) ?: return null
        val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val length = bb.int
        val type = bb.int
        if (length < 8 || length > 8_000_000) {
            err("bad packet length=$length type=$type header=${hex(header)}")
            return Packet(type, ByteArray(0))
        }
        val body = if (length == 8) ByteArray(0) else (readFully(inp, length - 8) ?: return null)
        return Packet(type, body)
    }

    private fun readFully(inp: InputStream, n: Int, blocking: Boolean = false): ByteArray? {
        val buf = ByteArray(n); var off = 0
        lastReadTimedOut = false
        val timeoutMs = if (steadyState) STEADY_READ_TIMEOUT_MS else BOOTSTRAP_READ_TIMEOUT_MS
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs)
        while (off < n) {
            if (!blocking && System.nanoTime() > deadline) {
                lastBytesRx = off
                lastReadTimedOut = true
                err("read timeout at $currentStage ($off/$n bytes received, ${timeoutMs}ms)")
                return null
            }
            val r = try {
                inp.read(buf, off, n - off)
            } catch (e: SocketTimeoutException) {
                // The socket's SO_TIMEOUT poll fired with no data this round. This is what makes the
                // 10s deadline above reachable: without it, a hung camera would block inp.read()
                // indefinitely (observed: a stuck telemetry refresh held the command channel ~46s
                // until the socket died, collapsing the whole PTP/Wi-Fi/BLE stack). Blocking reads
                // (event channel) wait indefinitely by design; non-blocking reads (command ops) loop
                // until the deadline, then give up cleanly. No bytes are lost — they stay buffered.
                continue
            }
            if (r < 0) {
                lastBytesRx = off
                return if (off == 0) null else buf.copyOf(off)
            }
            off += r
            lastBytesRx = off
        }
        return buf
    }

    // ------------------------------------------------------- DeviceInfo parse

    private fun parseDeviceInfo(d: ByteArray): DeviceInfo? {
        return try {
            val r = Reader(d)
            val stdVer = r.u16()
            val vendorExt = r.u32()
            val vendorVer = r.u16()
            val vendorDesc = r.str()
            val funcMode = r.u16()
            val ops = r.u16Array()
            val events = r.u16Array()
            val props = r.u16Array()
            val capFmts = r.u16Array()
            val imgFmts = r.u16Array()
            val manufacturer = r.str()
            val model = r.str()
            val deviceVersion = r.str()
            val serial = r.str()
            log("DeviceInfo: stdVer=$stdVer vendorExt=0x%08X vendorVer=$vendorVer".format(vendorExt))
            log("  vendorDesc='$vendorDesc' funcMode=$funcMode")
            log("  manufacturer='$manufacturer' model='$model' deviceVersion='$deviceVersion' serial='$serial'")
            log("  opsSupported=${ops.size} eventsSupported=${events.size} propsSupported=${props.size}")
            log("  ops=${ops.joinToString(",") { "0x%04X".format(it) }}")
            DeviceInfo(
                standardVersion = stdVer,
                vendorExtensionId = vendorExt.toLong() and 0xFFFFFFFFL,
                vendorExtensionVersion = vendorVer,
                vendorExtensionDesc = vendorDesc,
                functionalMode = funcMode,
                operationsSupported = ops.toList(),
                eventsSupported = events.toList(),
                devicePropertiesSupported = props.toList(),
                captureFormats = capFmts.toList(),
                imageFormats = imgFmts.toList(),
                manufacturer = manufacturer,
                model = model,
                deviceVersion = deviceVersion,
                serialNumber = serial,
            )
        } catch (e: Exception) {
            err("DeviceInfo parse error (${e.message}) — raw above for manual ISO 15740 decode")
            null
        }
    }

    /** Best-effort responder name from InitCommandAck: connNo(4)+GUID(16)+UTF16LE name. */
    private fun parseResponderName(body: ByteArray): String? = try {
        if (body.size <= 20) null else {
            val sb = StringBuilder()
            var i = 20
            while (i + 1 < body.size) {
                val lo = body[i].toInt() and 0xFF
                val hi = body[i + 1].toInt() and 0xFF
                val code = lo or (hi shl 8)
                if (code == 0) break
                sb.append(code.toChar())
                i += 2
            }
            sb.toString().ifEmpty { null }
        }
    } catch (e: Exception) { null }

    /** Little-endian PTP dataset reader. */
    private class Reader(val b: ByteArray) {
        var p = 0
        fun u8(): Int { return b[p++].toInt() and 0xFF }
        fun u16(): Int { val v = (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8); p += 2; return v }
        fun u32(): Int { val v = ByteBuffer.wrap(b, p, 4).order(ByteOrder.LITTLE_ENDIAN).int; p += 4; return v }
        fun u16Array(): IntArray { val n = u32(); return IntArray(n) { u16() } }
        fun str(): String {
            val n = b[p].toInt() and 0xFF; p += 1
            if (n == 0) return ""
            val charBytes = (n * 2)
            val s = String(b, p, charBytes, Charsets.UTF_16LE).trimEnd(' ')
            p += charBytes
            return s
        }
    }

    // ------------------------------------------------------- StorageIDs / PropDesc parse

    private fun parseStorageIds(d: ByteArray) = try {
        val r = Reader(d)
        val count = r.u32()
        val ids = (0 until count).map { r.u32().toLong() and 0xFFFFFFFFL }
        log("  StorageIDs: count=$count ids=${ids.joinToString(",") { "0x%08X".format(it) }}")
        result.storageIds = ids
    } catch (e: Exception) {
        err("  StorageIDs parse error: ${e.message}")
    }

    private fun parseDevicePropDesc(propCode: Int, d: ByteArray) = try {
        val r = Reader(d)
        val code = r.u16()
        val dataType = r.u16()
        val getSet = r.u8()
        log("  PropDesc 0x%04X %s: dataType=0x%04X access=%s".format(
            code, propName(code), dataType, if (getSet == 0) "GET" else "GET/SET"))
    } catch (e: Exception) {
        err("  PropDesc parse error for 0x%04X: ${e.message}".format(propCode))
    }

    // ------------------------------------------------ Sony SDIO telemetry path

    /**
     * Runs the Sony Monitor & Control telemetry bootstrap after a successful
     * OpenSession:
     *   SDIO_Connect[1] -> SDIO_Connect[2] -> SDIO_GetExtDeviceInfo[300,1]
     *   -> SDIO_Connect[3] -> SDIO_GetAllExtDevicePropInfo[0,1] (parse)
     *
     * Reuses [sendOperation] unchanged (full BEGIN/END/FAIL + raw hex logging is
     * already done there). Returns the next free transaction id on completion, or
     * null if the socket aborted (caller returns false). A non-OK response on a
     * handshake op is logged but not fatal — only a socket abort is.
     */
    private fun runSonyTelemetry(cmd: Channel, startTid: Int): Int? {
        var tid = startTid
        val connectRcs = ArrayList<Int>()

        fun sdioStep(label: String, opcode: Int, params: IntArray): OpResult? {
            currentStage = label
            log("=== $label op=${opName(opcode)} (0x%04X) params=${params.joinToString(",")} tid=$tid ==="
                .format(opcode))
            val r = sendOperation(cmd, opcode, tid++, params, null)
            if (r == null) {
                err("FAILED at $currentStage — socket closed (lastRx=$lastBytesRx bytes)")
                result.error = result.error ?: "socket closed during $label"
                return null
            }
            logOpVerbose(label, opcode, r)
            if (r.responseCode != RC_OK) {
                err("  $label non-OK: ${rcName(r.responseCode)} (0x%04X) — continuing".format(r.responseCode))
            }
            if (r.data.isNotEmpty()) log("  $label payload ${r.data.size} bytes: ${hex(r.data)}")
            return r
        }

        // 1. SDIO_Connect [1,0,0]
        val c1 = sdioStep("SDIO_Connect[1]", OP_SDIO_CONNECT, intArrayOf(1, 0, 0)) ?: return null
        connectRcs.add(c1.responseCode)
        // 2. SDIO_Connect [2,0,0]
        val c2 = sdioStep("SDIO_Connect[2]", OP_SDIO_CONNECT, intArrayOf(2, 0, 0)) ?: return null
        connectRcs.add(c2.responseCode)
        // 3. SDIO_GetExtDeviceInfo [300,1]
        val ext = sdioStep("SDIO_GetExtDeviceInfo", OP_SDIO_GET_EXT_DEVICE_INFO, intArrayOf(300, 1)) ?: return null
        // 4. SDIO_Connect [3,0,0]
        val c3 = sdioStep("SDIO_Connect[3]", OP_SDIO_CONNECT, intArrayOf(3, 0, 0)) ?: return null
        connectRcs.add(c3.responseCode)
        result.sdioConnectResponseCodes = connectRcs

        // 5. SDIO_GetAllExtDevicePropInfo [0,1] — the bulk telemetry snapshot
        val all = sdioStep("SDIO_GetAllExtDevicePropInfo", OP_SDIO_GET_ALL_EXT_DEVPROP_INFO, intArrayOf(0, 1))
            ?: return null
        result.getAllExtDevicePropResponseCode = all.responseCode
        if (all.responseCode == RC_OK && all.data.isNotEmpty()) {
            val map = parseAllExtDevicePropInfo(all.data)
            result.telemetry = map
            logTelemetrySummary(map)
        } else {
            err("  SDIO_GetAllExtDevicePropInfo returned no usable payload (rc=${rcName(all.responseCode)}, len=${all.data.size})")
        }
        return tid
    }

    /**
     * Parses a 0x9209 GetAllExtDevicePropInfo dataset:
     *   uint64_le record_count, then record_count device-property records.
     * Resilient: on any record failure it logs the exact offset/context and stops,
     * returning whatever was parsed so far. Never throws.
     */
    private fun parseAllExtDevicePropInfo(d: ByteArray): Map<Int, SonyProp> {
        val out = LinkedHashMap<Int, SonyProp>()
        val r = SonyReader(d)
        val recordCount: Long = try {
            r.u64()
        } catch (e: Exception) {
            err("0x9209 parse: cannot read record_count (len=${d.size}): ${e.message}")
            return out
        }
        result.telemetryRecordCount = recordCount
        log("0x9209 payloadLen=${d.size} record_count=$recordCount")

        for (i in 0 until recordCount) {
            val recStart = r.p
            try {
                val code = r.u16()
                val dataType = r.u16()
                val getSet = r.u8()
                val availability = r.u8()
                // default/base value, then current value (type-dependent)
                readValue(r, dataType)
                val current = readValue(r, dataType)
                val formFlag = r.u8()
                val form = readForm(r, dataType, formFlag)

                val curRaw = current as? Long
                val curStr = current as? String
                val prop = SonyProp(code, dataType, getSet, availability, curRaw, curStr, formFlag, form)
                out[code] = prop
                log("  rec[$i] code=0x%04X %s dataType=%s avail=%d current=%s form=%s".format(
                    code, sonyPropName(code), dataTypeName(dataType), availability,
                    curStr ?: curRaw?.toString() ?: "?", formName(formFlag)))
            } catch (e: Exception) {
                val remaining = d.size - recStart
                val ctxEnd = minOf(d.size, recStart + 32)
                err("0x9209 parse FAILED at record $i offset=$recStart: ${e.message}")
                err("  expected device-property record; remaining=$remaining bytes")
                err("  surrounding hex=${hex(d.copyOfRange(recStart.coerceIn(0, d.size), ctxEnd))}")
                err("  returning ${out.size} record(s) parsed before failure")
                break
            }
        }
        return out
    }

    /** Reads one type-dependent value; returns Long for numerics or String for STR. */
    private fun readValue(r: SonyReader, dataType: Int): Any = when (dataType) {
        DT_INT8   -> r.i8().toLong()
        DT_UINT8  -> r.u8().toLong()
        DT_INT16  -> r.i16().toLong()
        DT_UINT16 -> r.u16().toLong()
        DT_UINT32 -> r.u32().toLong() and 0xFFFFFFFFL
        DT_INT32  -> r.u32().toLong()
        DT_UINT64, DT_INT64 -> r.u64()
        DT_STR    -> r.sonyStr()
        else -> throw IllegalStateException("unknown dataType=0x%04X".format(dataType))
    }

    /**
     * Consumes the form payload following the current value, per form flag, and RETAINS it as a
     * portable [RawForm] (previously this was `skipForm`, which discarded the legal-value set).
     * Byte consumption is identical to the old skip path — same reads, same order — so the wire
     * parsing is unchanged; only the previously-thrown-away values are now returned.
     */
    private fun readForm(r: SonyReader, dataType: Int, formFlag: Int): RawForm = when (formFlag) {
        FORM_NONE -> RawForm.None
        FORM_RANGE -> {
            val min = readValue(r, dataType) as? Long
            val max = readValue(r, dataType) as? Long
            val step = readValue(r, dataType) as? Long
            if (min != null && max != null && step != null) RawForm.Range(min, max, step) else RawForm.None
        }
        FORM_ENUM -> {
            val supportedCount = r.u16()
            val supported = ArrayList<Long>(supportedCount)
            repeat(supportedCount) { (readValue(r, dataType) as? Long)?.let { supported.add(it) } }
            // Second list (settable subset) is consumed to stay byte-aligned; the supported list is
            // the enumeration we expose as the value set.
            val listed = r.u16(); repeat(listed) { readValue(r, dataType) }
            if (supported.isNotEmpty()) RawForm.Enum(supported) else RawForm.None
        }
        else -> throw IllegalStateException("unknown form flag=$formFlag")
    }

    private fun logTelemetrySummary(map: Map<Int, SonyProp>) {
        log("=== Sony telemetry summary (${map.size} props parsed) ===")
        var known = 0
        for (code in TELEMETRY_CODES) {
            val p = map[code] ?: continue
            known++
            val v = p.currentValueStr ?: p.currentValueRaw?.toString() ?: "?"
            log("  ${sonyPropName(code)} (0x%04X) = $v [${dataTypeName(p.dataType)}]".format(code))
        }
        if (known == 0) err("  no known telemetry properties present in dataset")
        else log("=== telemetry: $known known propert${if (known == 1) "y" else "ies"} extracted ✔ ===")
    }

    /** Little-endian reader for the Sony 0x9209 dataset (separate from PTP [Reader]). */
    private class SonyReader(val b: ByteArray) {
        var p = 0
        fun u8(): Int { return b[p++].toInt() and 0xFF }
        fun i8(): Int { return b[p++].toInt() }
        fun u16(): Int { val v = (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8); p += 2; return v }
        fun i16(): Int { return u16().toShort().toInt() }
        fun u32(): Int { val v = ByteBuffer.wrap(b, p, 4).order(ByteOrder.LITTLE_ENDIAN).int; p += 4; return v }
        fun u64(): Long { val v = ByteBuffer.wrap(b, p, 8).order(ByteOrder.LITTLE_ENDIAN).long; p += 8; return v }
        fun sonyStr(): String {
            val n = b[p].toInt() and 0xFF; p += 1
            if (n == 0) return ""
            val charBytes = n * 2
            val s = String(b, p, charBytes, Charsets.UTF_16LE)
            p += charBytes
            return s.trimEnd(' ')
        }
    }

    // ----------------------------------------------------------------- logs

    private fun logOpVerbose(label: String, opcode: Int, r: OpResult) {
        log("$label: op=${opName(opcode)} (0x%04X) rc=${rcName(r.responseCode)} (0x%04X) tid=${r.transactionId} payloadLen=${r.data.size}"
            .format(opcode, r.responseCode))
    }

    private fun dumpOut(label: String, bytes: ByteArray) =
        log("→ $label len=${bytes.size} hex=${hex(bytes)}")

    private fun dumpIn(label: String, p: Packet) =
        log("← $label type=${p.type} bodyLen=${p.body.size} hex=${hex(p.body)}")

    private fun dumpFramingHint() {
        err("OpenSession failed — compare against ISO 15740 PTP/IP Operation Request framing:")
        err("  [uint32 length][uint32 type=6][uint32 dataPhase][uint16 opcode][uint32 tid][params…]")
        err("  OperationResponse: [uint32 length][uint32 type=7][uint16 rc][uint32 tid][params…]")
        err("  If responder closed instead of replying, the InitCommandAck connectionNumber")
        err("  may need echoing, or OpenSession sessionId must be non-zero (we use $SESSION_ID).")
    }

    private fun opName(code: Int): String = when (code) {
        OP_GET_DEVICE_INFO       -> "GetDeviceInfo"
        OP_OPEN_SESSION          -> "OpenSession"
        OP_GET_STORAGE_IDS       -> "GetStorageIDs"
        OP_GET_DEVICE_PROP_DESC  -> "GetDevicePropDesc"
        OP_GET_DEVICE_PROP_VALUE -> "GetDevicePropValue"
        OP_SDIO_CONNECT                  -> "SDIO_Connect"
        OP_SDIO_GET_EXT_DEVICE_INFO      -> "SDIO_GetExtDeviceInfo"
        OP_SDIO_GET_ALL_EXT_DEVPROP_INFO -> "SDIO_GetAllExtDevicePropInfo"
        else -> "0x%04X".format(code)
    }

    private fun dataTypeName(dt: Int): String = when (dt) {
        DT_INT8 -> "INT8"; DT_UINT8 -> "UINT8"
        DT_INT16 -> "INT16"; DT_UINT16 -> "UINT16"
        DT_INT32 -> "INT32"; DT_UINT32 -> "UINT32"
        DT_INT64 -> "INT64"; DT_UINT64 -> "UINT64"
        DT_STR -> "STR"
        else -> "0x%04X".format(dt)
    }

    private fun formName(f: Int): String = when (f) {
        FORM_NONE -> "None"; FORM_RANGE -> "Range"; FORM_ENUM -> "Enum"
        else -> "0x%02X".format(f)
    }

    private fun sonyPropName(code: Int): String = when (code) {
        0xD20E -> "BatteryLevelIndicator"
        0xD218 -> "BatteryRemaining"
        0xD038 -> "BatteryRemainingInMinutes"
        0xD039 -> "BatteryRemainingInVoltage"
        0xD21E -> "ISOSensitivity"
        0xD023 -> "ISOCurrentSensitivity"
        0x5007 -> "FNumber"
        0xD000 -> "TNumber"
        0xD20D -> "ShutterSpeed"
        0xD016 -> "ShutterSpeedValue"
        0xD017 -> "ShutterSpeedCurrentValue"
        0x5005 -> "WhiteBalance"
        0xD00C -> "WhiteBalanceModeSetting"
        0xD20F -> "ColorTemperature"
        0x500A -> "FocusMode"
        0xD007 -> "FocusModeSetting"
        0xE044 -> "FocusModeStatus"
        0xE004 -> "FocusTouchSpotStatus"
        0xE005 -> "FocusTrackingStatus"
        0xD21D -> "MovieRecordingState"
        0xD261 -> "RecordingTime"
        0xE010 -> "RecorderMainStatus"
        0xD248 -> "MediaSLOT1Status"
        0xD24A -> "MediaSLOT1RemainingShootingTime"
        0xD256 -> "MediaSLOT2Status"
        0xD258 -> "MediaSLOT2RemainingShootingTime"
        0xD251 -> "DeviceOverheatingState"
        0xD049 -> "AutoPowerOFFTemperature"
        0xD221 -> "LiveViewStatus"
        0xD060 -> "SubjectRecognitionAF"
        0xE098 -> "MonitoringDeliveringStatus"
        else -> "0x%04X".format(code)
    }

    private fun rcName(code: Int): String = when (code) {
        0x2001 -> "OK"
        0x2002 -> "GeneralError"
        0x2003 -> "SessionNotOpen"
        0x2004 -> "InvalidTransactionID"
        0x2005 -> "OperationNotSupported"
        0x2006 -> "ParameterNotSupported"
        0x200A -> "DevicePropNotSupported"
        0x201A -> "DeviceBusy"
        0x201F -> "SessionAlreadyOpen"
        0x2020 -> "TransactionCancelled"
        else -> "0x%04X".format(code)
    }

    private fun propName(code: Int): String = when (code) {
        0x5001 -> "BatteryLevel"
        0x5004 -> "CompressionSetting"
        0x5005 -> "WhiteBalance"
        0x5007 -> "FNumber"
        0x500D -> "ShutterSpeed"
        0x500E -> "ExposureProgramMode"
        0x5010 -> "ExposureBiasCompensation"
        else -> "0x%04X".format(code)
    }

    private fun hex(b: ByteArray) = if (b.isEmpty()) "(empty)" else BleLog.toHex(b)
    private fun leShort(b: ByteArray, off: Int) =
        (b[off].toInt() and 0xFF) or ((b[off + 1].toInt() and 0xFF) shl 8)
    private fun leInt(b: ByteArray, off: Int) =
        ByteBuffer.wrap(b, off, 4).order(ByteOrder.LITTLE_ENDIAN).int
    private fun leLong(b: ByteArray, off: Int) =
        ByteBuffer.wrap(b, off, 8).order(ByteOrder.LITTLE_ENDIAN).long

    private fun log(m: String) = BleLog.line(BleLog.Kind.INFO, "[PTP] $m")
    private fun err(m: String) = BleLog.error("[PTP] $m")
}

/**
 * TEMP INSTRUMENTATION — measures command-channel contention to validate (or disprove) the
 * hypothesis that high-frequency background telemetry refreshes starve Push Live View's
 * KeepAlive of timely access to the shared PTP command channel. Changes NO scheduling
 * behavior by itself: every caller still blocks on [PtpIpClient.ptpLock] exactly as it did
 * under the old `synchronized` — this only times the existing blocking behavior.
 *
 * "High priority" = [PtpIpClient.sendSonyOperation] (Push Live View Start/Stop/KeepAlive).
 * "Background" = [PtpIpClient.sendOperation] (periodic liveness tick + event-triggered
 * telemetry refresh). Remove this object (and its two call sites) once the hypothesis is
 * confirmed/refuted and any resulting scheduling fix has landed and been verified.
 */
private object CommandChannelStats {
    private val highPriorityOps = AtomicInteger(0)
    private val backgroundOps = AtomicInteger(0)
    private val backgroundSkipped = AtomicInteger(0) // always 0 until a skip-on-busy path exists
    // Plain (non-atomic) bucket counters: a rare lost increment under cross-thread contention
    // is an acceptable trade-off for this temporary diagnostic pass, not worth the extra
    // synchronization for numbers only used to eyeball a distribution.
    private val highPriorityWaitBuckets = IntArray(6) // <10ms,<100ms,<1s,<3s,<8s,>=8s
    private val backgroundWaitBuckets = IntArray(6)
    private val highPriorityWaitMaxNs = AtomicLong(0)
    private val backgroundWaitMaxNs = AtomicLong(0)
    private val highPriorityWaitTotalNs = AtomicLong(0)
    private val backgroundWaitTotalNs = AtomicLong(0)
    private val highPriorityHoldTotalNs = AtomicLong(0)
    private val backgroundHoldTotalNs = AtomicLong(0)
    @Volatile private var windowStartMs = System.currentTimeMillis()

    private fun bucketIndex(ns: Long): Int {
        val ms = ns / 1_000_000
        return when {
            ms < 10 -> 0
            ms < 100 -> 1
            ms < 1_000 -> 2
            ms < 3_000 -> 3
            ms < 8_000 -> 4
            else -> 5
        }
    }

    fun recordHighPriority(waitNs: Long, holdNs: Long) {
        highPriorityOps.incrementAndGet()
        highPriorityWaitBuckets[bucketIndex(waitNs)]++
        highPriorityWaitTotalNs.addAndGet(waitNs)
        highPriorityHoldTotalNs.addAndGet(holdNs)
        highPriorityWaitMaxNs.updateAndGet { current -> maxOf(current, waitNs) }
    }

    fun recordBackground(waitNs: Long, holdNs: Long) {
        backgroundOps.incrementAndGet()
        backgroundWaitBuckets[bucketIndex(waitNs)]++
        backgroundWaitTotalNs.addAndGet(waitNs)
        backgroundHoldTotalNs.addAndGet(holdNs)
        backgroundWaitMaxNs.updateAndGet { current -> maxOf(current, waitNs) }
    }

    /** Not wired to any real skip logic yet — kept so the log line's shape matches what a
     *  future skip-on-busy fix would populate, without changing this pass's behavior. */
    fun recordBackgroundSkipped() {
        backgroundSkipped.incrementAndGet()
    }

    /** Logs one compact snapshot line and resets all counters for the next window. */
    fun snapshotAndLog(log: (String) -> Unit) {
        val hOps = highPriorityOps.getAndSet(0)
        val bOps = backgroundOps.getAndSet(0)
        val bSkipped = backgroundSkipped.getAndSet(0)
        val hWaitTotal = highPriorityWaitTotalNs.getAndSet(0)
        val bWaitTotal = backgroundWaitTotalNs.getAndSet(0)
        val hHoldTotal = highPriorityHoldTotalNs.getAndSet(0)
        val bHoldTotal = backgroundHoldTotalNs.getAndSet(0)
        val hWaitMax = highPriorityWaitMaxNs.getAndSet(0)
        val bWaitMax = backgroundWaitMaxNs.getAndSet(0)
        val windowMs = System.currentTimeMillis() - windowStartMs
        windowStartMs = System.currentTimeMillis()

        val hWaitAvgMs = if (hOps > 0) hWaitTotal / hOps / 1_000_000 else 0
        val bWaitAvgMs = if (bOps > 0) bWaitTotal / bOps / 1_000_000 else 0
        val hHoldAvgMs = if (hOps > 0) hHoldTotal / hOps / 1_000_000 else 0
        val bHoldAvgMs = if (bOps > 0) bHoldTotal / bOps / 1_000_000 else 0

        log(
            "[STATS] window=%dms highPriorityOps=%d bgOps=%d bgSkipped=%d ".format(windowMs, hOps, bOps, bSkipped) +
                "highPriorityWaitMs[avg/max]=%d/%d bgWaitMs[avg/max]=%d/%d ".format(hWaitAvgMs, hWaitMax / 1_000_000, bWaitAvgMs, bWaitMax / 1_000_000) +
                "highPriorityHoldMs[avg]=%d bgHoldMs[avg]=%d ".format(hHoldAvgMs, bHoldAvgMs) +
                "highPriorityWaitBuckets(<10/<100/<1s/<3s/<8s/>=8s)ms=%s bgWaitBuckets=%s"
                    .format(highPriorityWaitBuckets.joinToString("/"), backgroundWaitBuckets.joinToString("/")),
        )
        for (i in highPriorityWaitBuckets.indices) highPriorityWaitBuckets[i] = 0
        for (i in backgroundWaitBuckets.indices) backgroundWaitBuckets[i] = 0
    }
}
