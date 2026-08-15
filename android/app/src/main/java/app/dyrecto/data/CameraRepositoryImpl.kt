package app.dyrecto.data

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.net.Network
import app.dyrecto.ble.BleConnectionState
import app.dyrecto.ble.BondState
import app.dyrecto.ble.DiscoveredDevice
import app.dyrecto.camera.CameraDeviceInfo
import app.dyrecto.camera.PtpOpcodes
import app.dyrecto.connection.BleLog
import app.dyrecto.connection.Fx3BleManager
import app.dyrecto.debug.TelemetryEventStore
import app.dyrecto.connection.LiveViewTransport
import app.dyrecto.connection.LiveViewTransportProvider
import app.dyrecto.connection.PtpIpClient
import app.dyrecto.connection.SsdpDiscoverer
import app.dyrecto.connection.SshInfoTlv
import app.dyrecto.connection.SshTunnelTester
import app.dyrecto.connection.WifiProvisioner
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraRepository
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.ConnectionPhase
import app.dyrecto.domain.TelemetryProp
import app.dyrecto.liveview.MonitoringResult
import app.dyrecto.liveview.PushMonitoringSender
import app.dyrecto.ptp.ChannelState
import app.dyrecto.ptp.PtpStatus
import app.dyrecto.ssh.SshEnabled
import app.dyrecto.ssh.SshStatus
import app.dyrecto.telemetry.TimelineEvent
import app.dyrecto.telemetry.TimelineStage
import app.dyrecto.capability.CameraAdapter
import app.dyrecto.capability.CameraTransport
import app.dyrecto.capability.SonyCameraAdapter
import app.dyrecto.capability.SonyControlOpcodes
import app.dyrecto.capability.SonyPropValueCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import kotlin.concurrent.thread

/**
 * Orchestrates the full connection pipeline:
 *
 *   BLE scan → GATT → bond → Sony AP sequence →
 *   CC06/07/0C (Wi-Fi creds) → Android Wi-Fi join →
 *   DHCP gateway discovery → CC17 re-read →
 *   SSH → PTP/IP → GetDeviceInfo
 *
 * This is the ONLY place Android types meet the domain model.
 * No protocol logic lives here — it consumes [Fx3BleManager],
 * [WifiProvisioner], [SshTunnelTester], and [PtpIpClient] output.
 */
class CameraRepositoryImpl(context: Context) :
    CameraRepository, LiveViewTransportProvider, PushMonitoringSender {

    private val appContext = context.applicationContext
    private val bleManager = Fx3BleManager(appContext)
    private val sshTester = SshTunnelTester(appContext)
    private val wifiProvisioner = WifiProvisioner(appContext)
    private val ssdpDiscoverer = SsdpDiscoverer()

    // --- Sequence timestamps for CC17 diagnostic gap reporting (epoch millis, 0 = not reached) ---
    @Volatile private var tsCc0eSuccess = 0L
    @Volatile private var tsCc09Launched = 0L
    @Volatile private var tsDhcpComplete = 0L
    @Volatile private var tsSsdpResolved = 0L
    @Volatile private var tsCc17Read = 0L
    /** True once SSDP resolved a camera UUID — gates CC17 read and the OFF diagnostic. */
    @Volatile private var ssdpResolved = false

    private val _state = MutableStateFlow(CameraConnectionState())
    override val state: StateFlow<CameraConnectionState> = _state.asStateFlow()

    /** Camera-agnostic capability adapter, fed from the same telemetry seam as [state]. */
    private val sonyCameraAdapter = SonyCameraAdapter()
    override val camera: CameraAdapter get() = sonyCameraAdapter

    /** Running raw-property map + device info, so the adapter always sees the full capability set. */
    @Volatile private var lastRawProps: Map<Int, PtpIpClient.SonyProp> = emptyMap()
    @Volatile private var lastDeviceInfo: PtpIpClient.DeviceInfo? = null

    /** Merges a fresh raw-property batch into the running set and rebuilds the capability snapshot. */
    private fun feedCameraAdapter(
        newProps: Map<Int, PtpIpClient.SonyProp>,
        deviceInfo: PtpIpClient.DeviceInfo?,
    ) {
        if (newProps.isNotEmpty()) lastRawProps = lastRawProps + newProps
        if (deviceInfo != null) lastDeviceInfo = deviceInfo
        if (lastRawProps.isNotEmpty()) {
            sonyCameraAdapter.updateFromTelemetry(lastRawProps, lastDeviceInfo, CameraTransport.WIFI)
        }
    }

    /** Camera IP used for SSH — set from DHCP gateway discovery in auto flow. */
    @Volatile private var cameraIp: String = DEFAULT_CAMERA_IP
    @Volatile private var sshRunning = false

    /** Camera Wi-Fi network captured at DHCP discovery; used to bind the Live View socket since the
     *  process is unbound from Wi-Fi after PTP bootstrap (see [LiveViewTransportProvider]). */
    @Volatile private var cameraNetwork: Network? = null

    init {
        wireBleCallbacks()
        wireCameraControlSenders()
    }

    /**
     * Installs the (currently DISABLED) write path into the camera-agnostic control seam. The senders
     * route a validated set/command through the existing generic [PtpIpClient.sendSonyOperation]
     * (proven against 0x9230), but `writeEnabled` stays false — nothing is sent to the camera until
     * the SET/CONTROL opcode + payload are confirmed on FX3 hardware (see [SonyControlOpcodes]).
     */
    private fun wireCameraControlSenders() {
        sonyCameraAdapter.control.propertySender = { code, value ->
            val dataType = sonyCameraAdapter.capabilities.value?.property(code)?.dataType ?: 0x0004
            val payload = SonyPropValueCodec.encode(value, dataType)
            withContext(Dispatchers.IO) {
                sshTester.sendSonyOperation(
                    SonyControlOpcodes.SET_EXT_DEVICE_PROP_VALUE, intArrayOf(code), payload,
                )?.responseCode == SonyControlOpcodes.RC_OK
            }
        }
        sonyCameraAdapter.control.commandSender = { cmd ->
            withContext(Dispatchers.IO) {
                sshTester.sendSonyOperation(
                    SonyControlOpcodes.CONTROL_DEVICE,
                    intArrayOf(SonyControlOpcodes.controlParam(cmd)), null,
                )?.responseCode == SonyControlOpcodes.RC_OK
            }
        }
        // Writable control ENABLED (user-requested read+write from the live view). Each write is
        // still guarded by confirm-to-apply in the UI + local validation, and the result + the next
        // 0x9209 telemetry echo confirm whether the camera accepted it — which also serves as the
        // opcode/payload hardware validation. If a setting doesn't take, the opcode in
        // [SonyControlOpcodes] is the first thing to correct.
        sonyCameraAdapter.control.writeEnabled = true
    }

    override fun setCameraIp(ip: String) {
        cameraIp = ip.trim().ifEmpty { DEFAULT_CAMERA_IP }
        update { copy(ssh = ssh.copy(cameraIp = cameraIp)) }
    }

    override fun startScan() {
        BleLog.line(BleLog.Kind.INFO,
            "[TEL-DIAG] startScan(): resetting pipeline | thread=${Thread.currentThread().name}")
        sshTester.stopSession()
        sshRunning = false
        cameraIp = DEFAULT_CAMERA_IP
        cameraNetwork = null
        ssdpResolved = false
        ssdpDiscoverer.cancel()
        tsCc0eSuccess = 0L; tsCc09Launched = 0L; tsDhcpComplete = 0L
        tsSsdpResolved = 0L; tsCc17Read = 0L
        wifiProvisioner.release()
        _state.value = CameraConnectionState(
            phase = ConnectionPhase.SCANNING,
            running = true,
            ble = app.dyrecto.ble.BleStatus(scanning = true),
            ssh = SshStatus(cameraIp = cameraIp),
        )
        bleManager.start()
    }

    override fun stopScan() {
        bleManager.stopScan()
        update { copy(ble = ble.copy(scanning = false), running = false, phase = ConnectionPhase.IDLE) }
    }

    override fun connectToCamera(address: String) {
        BleLog.line(BleLog.Kind.INFO, "[TEL-DIAG] connectToCamera($address)")
        val selected = _state.value.ble.devices.firstOrNull { it.address == address }
        update {
            copy(
                phase = ConnectionPhase.BLE_FOUND,
                ble = ble.copy(
                    cameraName = selected?.name ?: address,
                    cameraAddress = address,
                    rssi = selected?.rssi,
                    scanning = false,
                ),
            ).addTimeline(TimelineStage.BLE_FOUND, detail = "${selected?.name ?: address} ($address)")
        }
        bleManager.connectTo(address)
    }

    override fun connect() = startScan()

    override fun disconnect() {
        BleLog.line(BleLog.Kind.INFO,
            "[TEL-DIAG] disconnect(): hard reset — old tel=${_state.value.telemetry?.props?.size ?: "null"} | thread=${Thread.currentThread().name}")
        runCatching { sshTester.stopSession() }
        runCatching { bleManager.close() }
        runCatching { ssdpDiscoverer.cancel() }
        runCatching { wifiProvisioner.release() }
        cameraNetwork = null
        lastRawProps = emptyMap()
        lastDeviceInfo = null
        sonyCameraAdapter.reset()
        _state.value = CameraConnectionState(phase = ConnectionPhase.IDLE)
    }

    override fun runOneTimePairing() {
        BleLog.line(BleLog.Kind.INFO, "[PAIR] manual one-time pairing requested")
        bleManager.runOneTimePairing()
    }

    override fun readEe02() = bleManager.readEe02()
    override fun readEe04() = bleManager.readEe04()
    override fun dumpEeState() = bleManager.dumpEeState()

    // ------------------------------------------------------- BLE wiring

    private fun wireBleCallbacks() {

        bleManager.onDeviceFound = { name, address, rssi, matched ->
            update {
                val device = DiscoveredDevice(name, address, rssi, matched,
                    brand = app.dyrecto.domain.CameraBrand.detect(name))
                val devices = (ble.devices.filterNot { it.address == address } + device)
                    .sortedByDescending { it.rssi }
                copy(ble = ble.copy(devices = devices))
            }
        }

        bleManager.onConnectionState = { newState, _ ->
            val mapped = when (newState) {
                BluetoothProfile.STATE_CONNECTED -> BleConnectionState.CONNECTED
                BluetoothProfile.STATE_CONNECTING -> BleConnectionState.CONNECTING
                BluetoothProfile.STATE_DISCONNECTING -> BleConnectionState.DISCONNECTING
                else -> BleConnectionState.DISCONNECTED
            }
            update {
                var s = copy(ble = ble.copy(connectionState = mapped))
                s = when (mapped) {
                    BleConnectionState.CONNECTED -> s.copy(phase = ConnectionPhase.CONNECTED)
                        .addTimeline(TimelineStage.CONNECTED)
                    BleConnectionState.CONNECTING -> s.copy(phase = ConnectionPhase.CONNECTING)
                    else -> s
                }
                s
            }
        }

        bleManager.onBondState = { newState ->
            val mapped = when (newState) {
                BluetoothDevice.BOND_BONDED -> BondState.BONDED
                BluetoothDevice.BOND_BONDING -> BondState.BONDING
                else -> BondState.NONE
            }
            update {
                var s = copy(ble = ble.copy(bondState = mapped))
                if (mapped == BondState.BONDING) s = s.copy(phase = ConnectionPhase.BONDING)
                s
            }
        }

        bleManager.onMtu = { mtu -> update { copy(ble = ble.copy(mtu = mtu)) } }

        bleManager.onServiceFound = { found ->
            update {
                copy(
                    ble = ble.copy(serviceFound = found),
                    phase = if (found) ConnectionPhase.READING_INFO else phase,
                )
            }
        }

        bleManager.onConfirmedRead = { label, value ->
            val text = decodeText(value)
            update {
                val updated = when (label) {
                    "CC0A" -> ble.copy(firmware = text)
                    "CC0B" -> ble.copy(model = text)
                    "CCA1" -> ble.copy(cameraSsid = text)
                    "CC06" -> ble.copy(wifiApSsid = text)
                    "CC07" -> ble.copy(wifiApPassword = text)
                    "CC0C" -> ble.copy(wifiApBssid = text)
                    else -> ble
                }
                copy(ble = updated)
            }
        }

        bleManager.onScanError = { message ->
            update {
                copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = message,
                    ble = ble.copy(scanning = false, error = message),
                )
            }
        }

        // ---- AP sequence phase signals ----

        bleManager.onSmartphoneModeOn = {
            update { copy(phase = ConnectionPhase.SMARTPHONE_MODE) }
        }

        bleManager.onCc0eSuccess = {
            tsCc0eSuccess = System.currentTimeMillis()
        }

        bleManager.onCc09Launched = {
            tsCc09Launched = System.currentTimeMillis()
        }

        bleManager.onWifiApOn = {
            update { copy(phase = ConnectionPhase.AP_CREATING) }
        }

        bleManager.onWifiCredentialsReady = { ssid, password, bssid ->
            BleLog.line(BleLog.Kind.INFO, "[AUTO] credentials ready — launching Wi-Fi join")
            update { copy(phase = ConnectionPhase.WIFI_CREDENTIALS).addTimeline(TimelineStage.AP_CREATED) }
            startWifiJoin(ssid, password, bssid)
        }

        bleManager.onApSequenceFailed = { reason ->
            update {
                copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = "AP sequence failed: $reason",
                )
            }
        }

        bleManager.onWifiFailed = {
            update {
                copy(
                    wifiCredentialsFailed = true,
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = "Wi-Fi credentials unavailable — AP creation may have failed",
                )
            }
        }

        bleManager.onSshDecoded = { r -> onSshDecoded(r) }
    }

    // ------------------------------------------------------- Wi-Fi join

    private fun startWifiJoin(ssid: String, password: String, bssid: String) {
        update { copy(phase = ConnectionPhase.WIFI_JOINING) }

        wifiProvisioner.onAssociated = {
            // No phase change yet — DHCP in progress
        }

        wifiProvisioner.onDhcpPending = {
            // Already logged by WifiProvisioner
        }

        wifiProvisioner.onGatewayDiscovered = { ip, network ->
            cameraIp = ip
            cameraNetwork = network
            tsDhcpComplete = System.currentTimeMillis()
            update {
                copy(
                    discoveredCameraIp = ip,
                    phase = ConnectionPhase.IP_DISCOVERY,
                    ssh = ssh.copy(cameraIp = ip),
                ).addTimeline(TimelineStage.WIFI_JOINED)
                 .addTimeline(TimelineStage.IP_DISCOVERED)
            }
            // Sony order: resolve the camera UUID over SSDP BEFORE reading CC17. The
            // camera only emits its one-time SSH credential once this remote session
            // is live, so CC17 must not be read until SSDP succeeds.
            ssdpResolved = false
            ssdpDiscoverer.discover(
                network = network,
                cameraIp = ip,
                onResolved = { uuid, _ ->
                    tsSsdpResolved = System.currentTimeMillis()
                    ssdpResolved = true
                    BleLog.line(BleLog.Kind.INFO, "[AUTO] SSDP resolved — reading CC17 (uuid=$uuid)")
                    // Open 30-second OBS window BEFORE CC17 so we capture any camera-state
                    // notifications (CC09 / CCA1 / CCA5 / CCA9) that arrive while SSH is starting.
                    bleManager.startObservationWindow()
                    bleManager.retryReadCc17()
                },
                onFailed = { reason ->
                    BleLog.error("[AUTO] SSDP failed: $reason — sequence cannot complete; CC17 not read")
                    update {
                        copy(
                            phase = ConnectionPhase.ERROR,
                            running = false,
                            fatalError = "SSDP UUID resolution failed: $reason",
                        )
                    }
                },
            )
        }

        wifiProvisioner.onFailed = { reason ->
            update {
                copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = "Wi-Fi join failed: $reason",
                )
            }
        }

        wifiProvisioner.connect(ssid, password, bssid)
    }

    // ------------------------------------------------------- CC17 → SSH/PTP

    private fun onSshDecoded(r: SshInfoTlv.Result) {
        val enabled = when (r.state) {
            SshInfoTlv.SshState.ON -> SshEnabled.ON
            SshInfoTlv.SshState.OFF -> SshEnabled.OFF
            SshInfoTlv.SshState.UNKNOWN -> SshEnabled.UNKNOWN
        }

        tsCc17Read = System.currentTimeMillis()

        // Log CC17 state for diagnostics (spec requirement)
        when {
            enabled == SshEnabled.ON && r.sshId.isNotEmpty() ->
                BleLog.line(BleLog.Kind.INFO, "[AUTO] CC17 ON — launching SSH")
            enabled == SshEnabled.UNKNOWN || (r.sshId.isEmpty() && r.sshPass.isEmpty()) ->
                BleLog.error("[AUTO] CC17 state=${r.state} — SSH credentials UNKNOWN/empty")
            enabled == SshEnabled.OFF ->
                BleLog.error("[AUTO] CC17 state=OFF — SSH disabled on camera")
        }

        // Hard diagnostic guard: CC17 still OFF *after* SSDP success means the
        // smartphone-control session did not cause the camera to mint SSH creds.
        if (ssdpResolved && enabled != SshEnabled.ON) {
            BleLog.error("[AUTO] CC17 OFF after SSDP — sequence still incomplete")
            logSequenceTimingGaps()
        }

        update {
            copy(
                phase = ConnectionPhase.CC17_READ,
                ssh = ssh.copy(
                    sshState = enabled,
                    sshId = r.sshId.ifEmpty { null },
                    sshPassword = r.sshPass.ifEmpty { null },
                    fingerprint = r.fingerprint.ifEmpty { null },
                    cameraIp = cameraIp,
                    notes = r.notes,
                ),
            ).addTimeline(TimelineStage.CC17_READ, detail = "SSH ${enabled.name}")
        }

        if (enabled == SshEnabled.ON && r.sshId.isNotEmpty() && r.sshPass.isNotEmpty()) {
            launchSshSession(r.sshId, r.sshPass)
        } else if (enabled == SshEnabled.OFF) {
            BleLog.line(BleLog.Kind.INFO, "[AUTO] SSH OFF — attempting direct PTP/IP to $cameraIp")
            launchDirectPtpSession()
        } else {
            val msg = "SSH credentials not available (CC17 state=${r.state}, id='${r.sshId}') — enable SSH on camera"
            update {
                copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = msg,
                )
            }
        }
    }

    /**
     * Emits the inter-step timing gaps requested for CC17 diagnostics:
     * CC0E success → CC09 state=02 → DHCP complete → SSDP resolved → CC17 read.
     */
    private fun logSequenceTimingGaps() {
        fun gap(from: Long, to: Long): String =
            if (from == 0L || to == 0L) "n/a" else "${to - from}ms"
        BleLog.error(
            "[AUTO] timing gaps: " +
                "CC0E→CC09=${gap(tsCc0eSuccess, tsCc09Launched)}, " +
                "CC09→DHCP=${gap(tsCc09Launched, tsDhcpComplete)}, " +
                "DHCP→SSDP=${gap(tsDhcpComplete, tsSsdpResolved)}, " +
                "SSDP→CC17=${gap(tsSsdpResolved, tsCc17Read)}, " +
                "total CC0E→CC17=${gap(tsCc0eSuccess, tsCc17Read)}"
        )
    }

    /** Push event-driven 0x9209 [1,1] refreshes into the live state for the dashboard. */
    private fun wireTelemetryRefresh() {
        TelemetryEventStore.clear()
        sshTester.onTelemetryRefreshed = { map ->
            BleLog.line(BleLog.Kind.INFO,
                "[TEL-DIAG] onTelemetryRefreshed: mapSize=${map.size} | currentTel=${_state.value.telemetry?.props?.size ?: "null"} | thread=${Thread.currentThread().name} | ts=${System.currentTimeMillis()}")
            if (map.isNotEmpty()) {
                feedCameraAdapter(map, null)
                update {
                    val refreshed = map.toDomain()!!   // non-null: map.isNotEmpty() guaranteed above
                    val existing = telemetry
                    val merged = existing?.copy(props = existing.props + refreshed.props)
                        ?: refreshed
                    TelemetryEventStore.processUpdate(telemetry, merged)
                    copy(
                        telemetry = merged,
                        lastTelemetryUpdateAt = System.currentTimeMillis(),
                        lastSuccessfulCommunicationAt = System.currentTimeMillis(),
                    )
                }
            }
        }
    }

    private fun launchDirectPtpSession() {
        if (sshRunning) return
        wireTelemetryRefresh()
        sshRunning = true
        sshTester.currentTiming = SshTunnelTester.Timing()
        sshTester.markCc17Read()
        update { copy(phase = ConnectionPhase.SSH_CONNECTING) }
        BleLog.line(BleLog.Kind.INFO, "[AUTO] launching direct PTP/IP to $cameraIp")

        thread(name = "camera-direct-ptp") {
            val timing = sshTester.runDirect(cameraIp)
            val ptp = sshTester.lastPtpResult
            applySessionResult(timing, ptp)
            sshRunning = false
        }
    }

    private fun launchSshSession(sshId: String, sshPass: String) {
        if (sshRunning) return
        wireTelemetryRefresh()
        sshRunning = true
        sshTester.currentTiming = SshTunnelTester.Timing()
        sshTester.markCc17Read()
        update { copy(phase = ConnectionPhase.SSH_CONNECTING) }
        BleLog.line(BleLog.Kind.INFO, "[AUTO] launching PTP/IP via SSH to $cameraIp")

        thread(name = "camera-ssh-ptp") {
            val timing = sshTester.run(cameraIp, SSH_PORT, sshId, sshPass)
            val ptp = sshTester.lastPtpResult
            applySessionResult(timing, ptp)
            sshRunning = false
        }
    }

    private fun applySessionResult(t: SshTunnelTester.Timing, ptp: PtpIpClient.SessionResult?) {
        BleLog.line(BleLog.Kind.INFO,
            "[TEL-DIAG] applySessionResult: ptpTel=${ptp?.telemetry?.size ?: "null"} | stateTel=${_state.value.telemetry?.props?.size ?: "null"} | thread=${Thread.currentThread().name} | ts=${System.currentTimeMillis()}")
        feedCameraAdapter(ptp?.telemetry ?: emptyMap(), ptp?.deviceInfo)
        update {
            var s = this

            // ---- SSH session outcome ----
            s = s.copy(
                ssh = s.ssh.copy(
                    connected = t.sshConnectedAt != null,
                    authenticated = t.authSucceeded,
                    keyboardInteractiveSupported = t.keyboardInteractiveSupported,
                    connectMillis = millis(t.sshConnectStartAt, t.sshConnectedAt),
                    authMillis = millis(t.authStartAt, t.authResultAt),
                    lastConnectedAt = t.sshConnectedAt,
                ),
            )
            if (!t.directMode) {
                if (!t.authSucceeded) {
                    return@update s.copy(
                        phase = ConnectionPhase.ERROR,
                        running = false,
                        fatalError = "SSH authentication failed",
                        ssh = s.ssh.copy(error = "Authentication failed"),
                    )
                }
                s = s.copy(phase = ConnectionPhase.SSH_AUTHENTICATED)
                    .addTimeline(TimelineStage.SSH_AUTHENTICATED, t.authResultAt)
            }

            // ---- PTP/IP outcome ----
            if (ptp == null) {
                return@update s.copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = "PTP/IP session produced no result",
                )
            }

            s = s.copy(
                ptp = PtpStatus(
                    tunnelUp = true,
                    commandChannel = if (ptp.initCommandAckOk) ChannelState.OPEN else ChannelState.FAILED,
                    eventChannel = when {
                        ptp.initEventAckOk -> ChannelState.OPEN
                        ptp.initCommandAckOk -> ChannelState.FAILED
                        else -> ChannelState.IDLE
                    },
                    sessionOpen = ptp.openSessionResponseCode == PtpStatus.RC_OK,
                    connectionNumber = ptp.connectionNumber,
                    responderName = ptp.responderName,
                    initCommandAck = ptp.initCommandAckOk,
                    initEventAck = ptp.initEventAckOk,
                    openSessionResponse = ptp.openSessionResponseCode,
                    getDeviceInfoResponse = ptp.getDeviceInfoResponseCode,
                    error = ptp.error,
                ),
            )
            if (ptp.initCommandAckOk) {
                s = s.copy(phase = ConnectionPhase.PTP_INIT)
                    .addTimeline(TimelineStage.PTP_INIT, t.ptpAckAt)
            }
            if (ptp.openSessionResponseCode == PtpStatus.RC_OK) {
                s = s.copy(phase = ConnectionPhase.SESSION_OPEN)
                    .addTimeline(TimelineStage.OPEN_SESSION)
            }

            val di = ptp.deviceInfo
            if (di != null && ptp.getDeviceInfoResponseCode == PtpStatus.RC_OK) {
                s.copy(
                    phase = ConnectionPhase.DEVICE_INFO,
                    running = false,
                    deviceInfo = di.toDomain(),
                    telemetry = ptp.telemetry.toDomain() ?: telemetry,
                    lastTelemetryUpdateAt = if (ptp.telemetry.isNotEmpty()) System.currentTimeMillis() else lastTelemetryUpdateAt,
                    lastSuccessfulCommunicationAt = System.currentTimeMillis(),
                ).addTimeline(TimelineStage.GET_DEVICE_INFO)
            } else if (ptp.openSessionResponseCode == PtpStatus.RC_OK) {
                // Session is established — GetDeviceInfo was rejected but other probes may
                // have succeeded. Logs show the exact first successful op and response codes.
                BleLog.line(BleLog.Kind.INFO,
                    "[AUTO] session open — firstSuccess=${ptp.error ?: "see logs"} GetDeviceInfo rc=0x%04X".format(
                        ptp.getDeviceInfoResponseCode ?: 0))
                s.copy(
                    phase = ConnectionPhase.SESSION_OPEN,
                    running = false,
                    telemetry = ptp.telemetry.toDomain() ?: telemetry,
                    lastTelemetryUpdateAt = if (ptp.telemetry.isNotEmpty()) System.currentTimeMillis() else lastTelemetryUpdateAt,
                )
            } else {
                s.copy(
                    phase = ConnectionPhase.ERROR,
                    running = false,
                    fatalError = ptp.error ?: "GetDeviceInfo did not complete",
                )
            }
        }
    }

    // ------------------------------------------------------- Live View transport

    /**
     * Opens a connected stream to the camera's Live View HTTP endpoint, or null (logged under
     * `[LV]`) when not monitoring or the URL can't be resolved. Pure communication concern — the
     * Live View pipeline owns the parser, decoder, lifecycle, and state.
     *
     * URL discovery (Phase 4A): the Sony `LiveViewUrl` device property (0xD278) is a string carried
     * in the 0x9209 telemetry snapshot, so we read it from the live telemetry map and log it. If it
     * is absent we fail cleanly rather than guessing a URL.
     */
    @Volatile private var _lastLiveViewError: String? = null
    override val lastLiveViewError: String? get() = _lastLiveViewError

    override fun openLiveView(): LiveViewTransport? {
        val tel = _state.value.telemetry
        if (tel == null) {
            _lastLiveViewError = "Not connected to camera"
            BleLog.line(BleLog.Kind.INFO, "[LV] openLiveView: no telemetry yet — not monitoring")
            return null
        }
        val urlStr = tel[LIVE_VIEW_URL]?.rawValue?.trim()
        if (urlStr.isNullOrEmpty()) {
            _lastLiveViewError = "Live View URL unavailable"
            BleLog.error("[LV] LiveViewUrl (0x%04X) not present in telemetry snapshot — cannot start Live View".format(LIVE_VIEW_URL))
            return null
        }
        val parsed = runCatching { URL(urlStr) }.getOrNull()
        if (parsed == null) {
            _lastLiveViewError = "Live View URL is invalid"
            BleLog.error("[LV] LiveViewUrl unparseable: '$urlStr'")
            return null
        }
        // Transport selection by SSH mode:
        //  - SSH=ON (e.g. A7 V): the camera's HTTP Live View is bound to localhost and reachable ONLY
        //    through the SSH tunnel (like PTP/15740), so KEEP the URL's original host (localhost) and
        //    dial it via an SSH direct-tcpip channel below.
        //  - SSH=OFF (e.g. FX3A, direct mode): unchanged — override host to the connected camera IP and
        //    open a plain socket on the camera Wi-Fi.
        val sshMode = _state.value.ssh.sshState == SshEnabled.ON
        val host = if (sshMode) (parsed.host ?: "localhost") else cameraIp
        val port = when {
            parsed.port != -1 -> parsed.port
            parsed.defaultPort != -1 -> parsed.defaultPort
            else -> 80
        }
        val path = parsed.file.ifEmpty { "/" }

        // ---- Full URL diagnostics: log the complete resolution BEFORE any gate/socket so the URL
        //      can be verified at a glance even when we then decline to open the stream. ----
        val decodedQuery = parsed.query?.let {
            runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrDefault(it)
        }
        val urlPropHex = "0x%04X".format(LIVE_VIEW_URL)
        BleLog.line(BleLog.Kind.INFO, "[LV] ── Live View URL resolution ──")
        BleLog.line(BleLog.Kind.INFO, "[LV]   rawUrl ($urlPropHex) = '$urlStr'")
        BleLog.line(BleLog.Kind.INFO,
            "[LV]   parsed: scheme=${parsed.protocol} host=${parsed.host} port=${parsed.port} " +
                "path=${parsed.path} query=${parsed.query ?: "(none)"}")
        if (decodedQuery != null) BleLog.line(BleLog.Kind.INFO, "[LV]   query(decoded)=$decodedQuery")
        BleLog.line(BleLog.Kind.INFO,
            if (sshMode) "[LV]   transform: SSH mode — keeping host ${parsed.host}; dialing via SSH tunnel; port+path kept"
            else "[LV]   transform: host overridden ${parsed.host} → $cameraIp (direct mode); port+path kept")
        BleLog.line(BleLog.Kind.INFO, "[LV]   request target = http://$host:$port$path (${if (sshMode) "SSH tunnel" else "direct socket"})")

        // ---- LiveViewStatus (0xD221) — log the raw value, then apply a conservative gate. ----
        // This is the documented Sony `canGetLiveView` precondition; we only BLOCK the cases where the
        // camera itself reports the stream can't be served (disabled / not supported). Enabled,
        // Undefined, and absent all PROCEED, so we still capture the live 503/success for analysis.
        val lvStatusRaw = tel[CameraTelemetry.LIVE_VIEW]?.rawNumber
        val lvStatusText = when (lvStatusRaw) {
            null -> "absent"
            0L -> "Supported, disabled"
            1L -> "Enabled"
            2L -> "Not supported"
            65535L -> "Undefined"
            else -> "raw=$lvStatusRaw"
        }
        BleLog.line(BleLog.Kind.INFO, "[LV]   LiveViewStatus(0xD221) = $lvStatusText (raw=${lvStatusRaw ?: "absent"})")
        if (lvStatusRaw == 0L || lvStatusRaw == 2L) {
            _lastLiveViewError = "Live View not enabled on camera (status: $lvStatusText)"
            BleLog.error("[LV] camera reports Live View $lvStatusText — not opening stream (would 503 / risk disturbing the session)")
            return null
        }

        return if (sshMode) openLiveViewTunneled(host, port, path) else openLiveViewDirect(host, port, path)
    }

    /** SSH=OFF (FX3A direct mode): plain socket to the camera Wi-Fi. Unchanged verified path. */
    private fun openLiveViewDirect(host: String, port: Int, path: String): LiveViewTransport? = try {
        val socket = cameraNetwork?.socketFactory?.createSocket() ?: Socket()
        socket.connect(InetSocketAddress(host, port), 10_000)
        BleLog.line(BleLog.Kind.INFO,
            "[LV] connected ✔ local=:${socket.localPort} remote=$host:$port net=${cameraNetwork != null}")
        _lastLiveViewError = null
        SocketLiveViewTransport(socket, host, path)
    } catch (e: Exception) {
        _lastLiveViewError = "Could not connect to camera Live View ($host:$port)"
        BleLog.error("[LV] connect failed to $host:$port — ${e.javaClass.simpleName}: ${e.message}")
        null
    }

    /** SSH=ON (A7 V): open the HTTP Live View over an SSH direct-tcpip channel to camera localhost. */
    private fun openLiveViewTunneled(host: String, port: Int, path: String): LiveViewTransport? {
        val channel = sshTester.newTunneledConnection(host, port)
        if (channel == null) {
            _lastLiveViewError = "Live View tunnel unavailable (no live SSH session)"
            BleLog.error("[LV] SSH tunnel channel to $host:$port unavailable — no retained SSH transport")
            return null
        }
        BleLog.line(BleLog.Kind.INFO, "[LV] connected ✔ via SSH tunnel → $host:$port")
        _lastLiveViewError = null
        return TunneledLiveViewTransport(channel, host, path)
    }

    /** Plain-socket [LiveViewTransport] for direct mode. */
    private class SocketLiveViewTransport(
        private val socket: Socket,
        override val host: String,
        override val path: String,
    ) : LiveViewTransport {
        override val input: InputStream get() = socket.getInputStream()
        override val output: OutputStream get() = socket.getOutputStream()
        override fun close() { runCatching { socket.close() } }
    }

    /** SSH-tunneled [LiveViewTransport] (SSH=ON): rides an SSH direct-tcpip [PtpIpClient.Channel]. */
    private class TunneledLiveViewTransport(
        private val channel: PtpIpClient.Channel,
        override val host: String,
        override val path: String,
    ) : LiveViewTransport {
        override val input: InputStream get() = channel.input
        override val output: OutputStream get() = channel.output
        override fun close() { runCatching { channel.close() } }
    }

    // ----------------------------------------------- Push Live View PoC (PushMonitoringSender)

    /**
     * Phone's own IPv4 on the camera Wi-Fi, read from the captured [cameraNetwork]'s LinkProperties.
     * This is the address the camera dials back to push the monitoring stream. Returns null if the
     * network is gone or carries no IPv4.
     */
    override fun phoneIpOnCameraNetwork(): String? {
        val net = cameraNetwork
        if (net == null) {
            BleLog.error("[PUSH-LV] phoneIpOnCameraNetwork: cameraNetwork is null (not joined / post-scan)")
            return null
        }
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val lp = cm.getLinkProperties(net)
        val ip = lp?.linkAddresses
            ?.map { it.address }
            ?.filterIsInstance<java.net.Inet4Address>()
            ?.firstOrNull { !it.isLoopbackAddress }
            ?.hostAddress
        BleLog.line(BleLog.Kind.INFO, "[PUSH-LV] phoneIpOnCameraNetwork = ${ip ?: "(none)"}")
        return ip
    }

    /** Sends SDIO_ControlMonitoring(subCmd) over the live PTP command channel via [SshTunnelTester]. */
    override fun sendControlMonitoring(subCmd: Int, payload: ByteArray?): MonitoringResult? {
        val r = sshTester.sendSonyOperation(PtpIpClient.Monitoring.OPCODE, intArrayOf(subCmd), payload)
        return r?.let { MonitoringResult(it.responseCode, it.data.size, it.responseParams) }
    }

    // ------------------------------------------------------- helpers

    private inline fun update(block: CameraConnectionState.() -> CameraConnectionState) {
        synchronized(this) {
            val old = _state.value
            val new = old.block()
            if (new.telemetry !== old.telemetry) {
                val frame = Thread.currentThread().stackTrace
                    .firstOrNull { it.className.contains("CameraRepositoryImpl") }
                    ?.let { "${it.methodName}:${it.lineNumber}" } ?: "?"
                BleLog.line(BleLog.Kind.INFO,
                    "[TEL-DIAG] update() tel ${old.telemetry?.props?.size ?: "null"} → ${new.telemetry?.props?.size ?: "null"} | caller=$frame | thread=${Thread.currentThread().name} | ts=${System.currentTimeMillis()}")
            }
            _state.value = new
        }
    }

    private fun CameraConnectionState.addTimeline(
        stage: TimelineStage,
        at: Long? = null,
        ok: Boolean = true,
        detail: String? = null,
    ): CameraConnectionState {
        if (timeline.any { it.stage == stage }) return this
        val ts = at ?: System.currentTimeMillis()
        val prev = timeline.maxByOrNull { it.timestamp }
        val dur = prev?.let { ts - it.timestamp }?.coerceAtLeast(0)
        return copy(timeline = timeline + TimelineEvent(stage, ts, dur, ok, detail))
    }

    private fun millis(a: Long?, b: Long?): Long? =
        if (a != null && b != null) (b - a).coerceAtLeast(0) else null

    private fun decodeText(value: ByteArray): String {
        val s = String(value, Charsets.UTF_8)
        return s.filter { it.code in 0x20..0x7E || it.code > 0xA0 }.trim()
    }

    private fun PtpIpClient.DeviceInfo.toDomain(): CameraDeviceInfo = CameraDeviceInfo(
        manufacturer = manufacturer,
        model = model,
        firmwareVersion = deviceVersion,
        serialNumber = serialNumber,
        standardVersion = standardVersion,
        vendorExtensionId = vendorExtensionId,
        vendorExtensionVersion = vendorExtensionVersion,
        vendorExtensionDescription = vendorExtensionDesc,
        functionalMode = functionalMode,
        operations = operationsSupported.map { PtpOpcodes.describe(it) },
        supportedEventCount = eventsSupported.size,
        supportedPropertyCount = devicePropertiesSupported.size,
        captureFormatCount = captureFormats.size,
        imageFormatCount = imageFormats.size,
    )

    /** Maps the protocol-layer telemetry map into the platform-agnostic domain model. */
    private fun Map<Int, PtpIpClient.SonyProp>.toDomain(): CameraTelemetry? {
        if (isEmpty()) return null
        return CameraTelemetry(
            props = mapValues { (code, p) ->
                TelemetryProp(
                    code = code,
                    label = CameraTelemetry.labelFor(code),
                    rawValue = p.currentValueStr ?: p.currentValueRaw?.toString(),
                    dataType = p.dataType,
                    rawNumber = p.currentValueRaw,
                )
            },
        )
    }

    companion object {
        const val DEFAULT_CAMERA_IP = "192.168.122.1"
        const val SSH_PORT = 22
        /** Sony `LiveViewUrl` device-property code (string), carried in the 0x9209 snapshot. */
        const val LIVE_VIEW_URL = 0xD278
    }
}
