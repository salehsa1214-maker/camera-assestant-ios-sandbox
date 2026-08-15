package app.dyrecto.capability

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The single boundary between Dyrecto's camera-agnostic core and a specific camera platform.
 * Everything above the transport/protocol layer depends only on this interface plus the four
 * normalized models ([CameraInfo], [CameraProperty]/[CameraCapabilities], [CameraEvent],
 * [CameraControl]) — never on Sony/PTP types (Rule 4).
 *
 * `SonyCameraAdapter` (app module) is the first and today only implementation; it quarantines all
 * PTP/IP, SDIO opcode, CC17/BLE, 0x9209 and VERIC specifics. A future non-Sony platform is a new
 * adapter with zero core changes.
 */
interface CameraAdapter {
    /** Live capability snapshot; null until the first `GetAllExtDevicePropInfo` is parsed. */
    val capabilities: StateFlow<CameraCapabilities?>

    /** Normalized asynchronous events (recording, battery, media, thermal, property changes…). */
    val events: Flow<CameraEvent>

    /** The control seam; validates against [capabilities] before the wire. */
    val control: CameraControl
}
