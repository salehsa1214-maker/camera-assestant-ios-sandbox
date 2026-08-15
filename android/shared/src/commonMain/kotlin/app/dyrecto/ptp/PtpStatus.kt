package app.dyrecto.ptp

import app.dyrecto.text.hexUpper

/** Platform-agnostic view of the PTP/IP session (ISO 15740 over the SSH tunnel). */

enum class ChannelState { IDLE, OPEN, FAILED }

data class PtpStatus(
    val tunnelUp: Boolean = false,
    val commandChannel: ChannelState = ChannelState.IDLE,
    val eventChannel: ChannelState = ChannelState.IDLE,
    val sessionOpen: Boolean = false,
    val connectionNumber: Int? = null,
    val responderName: String? = null,
    // Raw PTP response codes (e.g. 0x2001 = OK).
    val initCommandAck: Boolean? = null,
    val initEventAck: Boolean? = null,
    val openSessionResponse: Int? = null,
    val getDeviceInfoResponse: Int? = null,
    val error: String? = null,
) {
    companion object {
        const val RC_OK = 0x2001
        fun rcText(rc: Int?): String = when (rc) {
            null -> "—"
            RC_OK -> "OK (0x2001)"
            else -> "0x" + hexUpper(rc, 4)
        }
    }
}
