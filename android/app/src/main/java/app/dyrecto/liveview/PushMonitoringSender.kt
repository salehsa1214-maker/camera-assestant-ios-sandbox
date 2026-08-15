package app.dyrecto.liveview

/**
 * Seam the Push Live View PoC depends on, mirroring [LiveViewTransportProvider].
 *
 * Implemented by the repository (the only place Android/connection types are allowed). It hands the
 * PoC the two things it needs without leaking `android.net.Network` / PTP types into this package:
 *   - the phone's own IPv4 on the camera Wi-Fi (the receiver address the camera will dial back), and
 *   - a way to send `SDIO_ControlMonitoring(subCmd)` over the live PTP command channel.
 */
interface PushMonitoringSender {

    /** Phone's own IPv4 address on the camera network, or null if it can't be resolved. */
    fun phoneIpOnCameraNetwork(): String?

    /**
     * Sends `SDIO_ControlMonitoring` (opcode 0x9230) with the single operation parameter `[subCmd]`
     * and an optional little-endian DataOut [payload]. Returns the camera's response, or null if there
     * is no live command channel.
     *
     * Per the recovered Sony serializer, all per-delivery data (the delivery handle for KeepAlive/Stop,
     * the receiver config for Start) travels in [payload]; the sub-command is the only operation param.
     */
    fun sendControlMonitoring(subCmd: Int, payload: ByteArray?): MonitoringResult?
}

/** Minimal, connection-type-free result of a `SDIO_ControlMonitoring` exchange. */
data class MonitoringResult(
    val responseCode: Int,
    val dataLen: Int,
    /** OperationResponse parameters. Per the Sony serializer: param[0] = delivery handle (the id the
     *  camera assigns at Start), param[1] = monitoring result code (see [monitoringResultName]). */
    val responseParams: IntArray = IntArray(0),
) {
    /** The per-delivery handle from a Start response (param[0]), or null if none was returned. */
    val deliveryId: Int? get() = responseParams.firstOrNull()

    /** Monitoring result code (response param[1]); null if the camera returned fewer than 2 params. */
    val monitoringResultCode: Int? get() = responseParams.getOrNull(1)

    /** True only if BOTH the PTP transport ack (0x2001) AND the monitoring result (param[1]) are OK.
     *  The transport [ok] alone is NOT success — the camera rejects e.g. a bad KeepAlive with rc=0x2001
     *  but monitoring result = NG_Invalid_Args(6). */
    val monitoringOk: Boolean get() = ok && monitoringResultCode == 0

    /** Human name for the monitoring result code (Sony EnumC0579p). */
    val monitoringResultName: String get() = when (monitoringResultCode) {
        null -> "(no result param)"
        0 -> "OK"
        1 -> "NG_StartFailed_SystemError"
        2 -> "NG_StartFailed_LimitOver"
        3 -> "NG_StartFailed_Excluded"
        4 -> "NG_StartFailed_OtherTypeProcessing"
        5 -> "NG_MonitoringStopped"
        6 -> "NG_Invalid_Args"
        7 -> "NG_MonitoringStopped_HighTemperature"
        8 -> "NG_MonitoringStopped_Streaming"
        else -> "NG_Unknown(${monitoringResultCode})"
    }

    val ok: Boolean get() = responseCode == RC_OK
    val name: String get() = when (responseCode) {
        RC_OK -> "OK"
        0x2002 -> "GeneralError"
        0x2003 -> "SessionNotOpen"
        0x2005 -> "OperationNotSupported"
        0x2006 -> "ParameterNotSupported"
        0x200A -> "DevicePropNotSupported"
        0x201A -> "DeviceBusy"
        else -> "0x%04X".format(responseCode)
    }
    companion object { const val RC_OK = 0x2001 }
}
