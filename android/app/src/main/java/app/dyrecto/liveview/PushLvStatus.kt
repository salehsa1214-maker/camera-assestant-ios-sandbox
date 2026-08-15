package app.dyrecto.liveview

/**
 * Renderer-free status of the Push Live View PoC, surfaced to the dev screen.
 *
 * This PoC's goal is to PROVE the camera connects back and pushes bytes — not to render frames.
 * So this carries connection/diagnostic state (ports, the Start response, byte counters, outcome),
 * never decoded images.
 */
data class PushLvStatus(
    val phase: Phase = Phase.IDLE,
    val phoneIp: String? = null,
    val videoPort: Int = 0,
    val metaPort: Int = 0,
    /** Human-readable SDIO_ControlMonitoring(Start) response, e.g. "OK" / "DeviceBusy" / "0x2002". */
    val startRcName: String? = null,
    val startRcCode: Int? = null,
    val videoConnected: Boolean = false,
    val metaConnected: Boolean = false,
    val videoBytes: Long = 0,
    val metaBytes: Long = 0,
    /** VERIC frames extracted from the video stream (parse-only; no rendering). */
    val framesParsed: Int = 0,
    /** Free-text outcome / error detail for the UI. */
    val message: String? = null,
) {
    enum class Phase {
        IDLE,        // not started
        LISTENING,   // server sockets bound, ports known
        STARTED,     // SDIO_ControlMonitoring(Start) sent, awaiting connect-back
        CONNECTED,   // camera connected to at least one socket
        RECEIVING,   // bytes flowing
        STOPPED,     // cleanly stopped by user
        ERROR,       // failed (no phoneIp / no connect-back / send failure)
    }
}
