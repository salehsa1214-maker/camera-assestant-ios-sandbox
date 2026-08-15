package app.dyrecto.domain

import app.dyrecto.capability.CameraAdapter
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-agnostic contract over the verified connection stack. The UI and
 * ViewModels depend ONLY on this interface, never on Android/BLE/SSH/PTP types,
 * which keeps the business logic portable to a future iOS implementation.
 */
interface CameraRepository {

    /** Live, immutable connection state for the whole pipeline. */
    val state: StateFlow<CameraConnectionState>

    /**
     * Camera-agnostic capability/control adapter. Exposes the normalized capability snapshot,
     * event stream, and control seam without leaking Sony/PTP types (see
     * [app.dyrecto.capability.CameraAdapter]).
     */
    val camera: CameraAdapter

    /** Camera IP for the SSH/PTP layer (default 192.168.122.1). */
    fun setCameraIp(ip: String)

    /**
     * Starts BLE scanning without auto-connecting. Discovered cameras accumulate in
     * [state] as [app.dyrecto.ble.BleStatus.devices]. Call [connectToCamera]
     * once the user selects a device.
     * Requires BLE runtime permissions to already be granted.
     */
    fun startScan()

    /** Stops an in-progress scan without starting a connection. */
    fun stopScan()

    /** Initiates GATT connection to the camera at [address] (a BLE MAC seen during [startScan]). */
    fun connectToCamera(address: String)

    /**
     * Legacy shortcut: starts a scan and immediately auto-connects to the first matching device.
     * Kept for the DeveloperScreen direct-connect path; prefer [startScan] + [connectToCamera].
     */
    fun connect()

    /** Tears down and returns to [ConnectionPhase.IDLE]. */
    fun disconnect()

    /**
     * Manual, one-time camera pairing/registration (EE01 write). Must be invoked
     * explicitly from a Developer action — it is NOT part of Scan/Connect. Requires
     * an active BLE connection to the camera.
     */
    fun runOneTimePairing()

    /** Developer diagnostics: read EE02 / EE04 registration-state fields (no writes). */
    fun readEe02()
    fun readEe04()
    fun dumpEeState()
}
