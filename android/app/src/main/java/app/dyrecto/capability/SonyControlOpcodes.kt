package app.dyrecto.capability

/**
 * Sony SDIO control opcodes for the WRITE path (setting properties / issuing commands).
 *
 * ⚠️ UNVERIFIED — HARDWARE VALIDATION REQUIRED. The operation *names* (`SDIO_SetExtDevicePropValue`,
 * `SDIO_ControlDevice`) are confirmed present in Sony's Monitor & Control native protocol library,
 * but their exact numeric opcodes and DataOut payload layouts are NOT confirmed against the FX3.
 * Public references disagree on the numbering (e.g. Dyrecto's own display table and various RE notes
 * map 0x9205/0x9207/0x9209 differently), and the telemetry GET path proves only the read side.
 *
 * These constants therefore drive a DISABLED write path (`SonyCameraControl.writeEnabled = false`).
 * Before enabling any write, confirm on the FX3 (via the Capability Explorer): send one benign
 * `SET` and verify the camera acks (RC_OK) AND the value echoes back in the next 0x9209 snapshot.
 * Only then flip `writeEnabled` and correct these constants if needed.
 */
object SonyControlOpcodes {
    /** SDIO_SetExtDevicePropValue — set one device property. UNVERIFIED numeric value. */
    const val SET_EXT_DEVICE_PROP_VALUE = 0x9207

    /** SDIO_ControlDevice — issue a device command (record, zoom, focus push…). UNVERIFIED. */
    const val CONTROL_DEVICE = 0x9205

    /** PTP OperationResponse OK. */
    const val RC_OK = 0x2001

    /** Maps a normalized [CameraCommand] to its SDIO_ControlDevice sub-command parameter.
     *  UNVERIFIED sub-command values — placeholders until confirmed on hardware. */
    fun controlParam(command: CameraCommand): Int = when (command) {
        CameraCommand.RECORD_START -> 1
        CameraCommand.RECORD_STOP -> 2
        CameraCommand.AF_PUSH -> 3
        CameraCommand.ZOOM_IN -> 4
        CameraCommand.ZOOM_OUT -> 5
        CameraCommand.ZOOM_STOP -> 6
    }
}
