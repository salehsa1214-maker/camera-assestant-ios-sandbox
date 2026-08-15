package app.dyrecto.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * FX3A BLE provisioning sequencer — extended with the Sony AP creation sequence.
 *
 * Verified flow (unchanged):
 *   scan → connect → bond → enable notifications → read device info
 *
 * New Sony AP sequence (additive, runs after device info reads):
 *   EE01 pairing write → CC0E notify enable → CCA3 smartphone mode ON →
 *   wait CC0E ack → CC08 Wi-Fi AP ON → wait CC09 Wi-Fi launched →
 *   read CC06/CC07/CC0C credentials
 *
 * CC17 (SSH info) is NOT read in the initial sequence; it is read via
 * retryReadCc17() after the phone has joined the camera Wi-Fi AP.
 *
 * The GATT operation queue is unchanged — all ops (reads, descriptor writes,
 * characteristic writes) are serialised through the same queue.
 */
@SuppressLint("MissingPermission")
class Fx3BleManager(private val context: Context) {

    private val nameMatches = listOf("FX3", "ILME-FX3", "DSC", "ILCE", "Sony")

    private val handler = Handler(Looper.getMainLooper())
    private val btManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter get() = btManager.adapter

    private var gatt: BluetoothGatt? = null
    private var device: BluetoothDevice? = null
    private var scanning = false

    /** All devices seen during the current scan, keyed by MAC address. */
    private val knownDevices = mutableMapOf<String, BluetoothDevice>()

    /** Serialized GATT op queue — Android allows only one in-flight op. */
    private val opQueue = ConcurrentLinkedQueue<() -> Unit>()
    private var opInFlight = false

    // ---- Observer callbacks ----
    var onSshDecoded: ((SshInfoTlv.Result) -> Unit)? = null
    var onDeviceFound: ((name: String, address: String, rssi: Int, matched: Boolean) -> Unit)? = null
    var onConnectionState: ((newState: Int, status: Int) -> Unit)? = null
    var onMtu: ((mtu: Int) -> Unit)? = null
    var onBondState: ((newState: Int) -> Unit)? = null
    var onServiceFound: ((found: Boolean) -> Unit)? = null
    var onConfirmedRead: ((label: String, value: ByteArray) -> Unit)? = null
    var onScanError: ((message: String) -> Unit)? = null
    var onWifiFailed: (() -> Unit)? = null

    // ---- AP sequence callbacks (new) ----
    /** Fires when CCA3 smartphone mode write is submitted (phase → SMARTPHONE_MODE). */
    var onSmartphoneModeOn: (() -> Unit)? = null
    /** Fires when CC08 Wi-Fi AP ON write is submitted (phase → AP_CREATING). */
    var onWifiApOn: (() -> Unit)? = null
    /** Fires when all three CC06/CC07/CC0C were read successfully. */
    var onWifiCredentialsReady: ((ssid: String, password: String, bssid: String) -> Unit)? = null
    /** Fires on a fatal AP-sequence error (characteristic not found, etc.). */
    var onApSequenceFailed: ((reason: String) -> Unit)? = null
    /** Fires the moment the CC0E smartphone-mode-ack notify is received (success). */
    var onCc0eSuccess: (() -> Unit)? = null
    /** Fires the moment CC09 reports state=02 (Wi-Fi/AP launched). */
    var onCc09Launched: (() -> Unit)? = null

    // ---- AP notification wait flags ----
    @Volatile private var waitingForCC0E = false
    @Volatile private var waitingForCC09WifiLaunch = false
    private var pendingCC0ETimeoutRunnable: Runnable? = null
    private var pendingCC09TimeoutRunnable: Runnable? = null

    // ---- One-time pairing diagnostics ----
    @Volatile private var pairingInProgress = false
    @Volatile private var pairingEe03Seen = false
    private var pendingPairingTimeoutRunnable: Runnable? = null

    // ---- Post-WiFi observation window ----
    @Volatile private var obsWindowActive = false
    @Volatile private var obsWindowStartAt = 0L
    private var obsWindowStopRunnable: Runnable? = null
    private val obsTargets: Set<java.util.UUID> = setOf(
        Fx3Uuids.CC09, Fx3Uuids.CC0F, Fx3Uuids.CC10,
        Fx3Uuids.CCA1, Fx3Uuids.CCA5, Fx3Uuids.CCA9,
    )

    // ---- Wi-Fi credential accumulator (reset on each start()) ----
    @Volatile private var wifiSsidRead = false
    @Volatile private var wifiPassRead = false
    @Volatile private var wifiBssidRead = false
    @Volatile private var wifiSsid: String? = null
    @Volatile private var wifiPassword: String? = null
    @Volatile private var wifiBssid: String? = null

    @Volatile var matchedName: String? = null
        private set
    @Volatile var matchedAddress: String? = null
        private set

    // ---------------------------------------------------------------- scan

    fun start() {
        if (adapter == null || !adapter.isEnabled) {
            val m = "Bluetooth adapter is null or disabled — enable BT and retry"
            BleLog.error(m); onScanError?.invoke(m)
            return
        }
        knownDevices.clear()
        wifiSsidRead = false; wifiPassRead = false; wifiBssidRead = false
        wifiSsid = null; wifiPassword = null; wifiBssid = null
        waitingForCC0E = false; waitingForCC09WifiLaunch = false
        BleLog.line(BleLog.Kind.SCAN, "Starting BLE scan for FX3A (name match: $nameMatches)")
        scanning = true
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        adapter.bluetoothLeScanner.startScan(null, settings, scanCallback)
        handler.postDelayed({
            if (scanning) {
                val m = "Scan finished (30 s). ${knownDevices.size} device(s) found."
                BleLog.line(BleLog.Kind.SCAN, m)
                stopScan()
            }
        }, 30_000)
    }

    fun stopScan() {
        if (!scanning) return
        scanning = false
        runCatching { adapter.bluetoothLeScanner.stopScan(scanCallback) }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val dev = result.device
            val name = dev.name ?: result.scanRecord?.deviceName ?: "(no name)"
            val match = nameMatches.any { name.contains(it, ignoreCase = true) }
            BleLog.line(
                BleLog.Kind.SCAN,
                "found ${dev.address} name='$name' rssi=${result.rssi}" +
                    if (match) "  <-- MATCH" else ""
            )
            knownDevices[dev.address] = dev
            onDeviceFound?.invoke(name, dev.address, result.rssi, match)
        }

        override fun onScanFailed(errorCode: Int) {
            val m = "Scan failed, code=$errorCode"
            BleLog.error(m); onScanError?.invoke(m)
        }
    }

    /**
     * Initiates GATT connection to a specific device address chosen by the user.
     * Must be called after [start] has run and the device appears in the picker.
     */
    fun connectTo(address: String) {
        val dev = knownDevices[address] ?: run {
            BleLog.error("connectTo: address $address not in knownDevices — was it discovered?")
            return
        }
        stopScan()
        device = dev
        matchedAddress = address
        matchedName = dev.name ?: address
        connect(dev)
    }

    // ------------------------------------------------------------- connect

    private fun connect(dev: BluetoothDevice) {
        BleLog.line(BleLog.Kind.CONN, "connecting GATT to ${dev.address} (bondState=${bondName(dev.bondState)})")
        registerBondReceiver()
        gatt = dev.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun close() {
        stopScan()
        cancelPendingTimeouts()
        runCatching { context.unregisterReceiver(bondReceiver) }
        gatt?.close()
        gatt = null
        device = null
    }

    // ----------------------------------------------------------- bond flow

    private fun ensureBondThenDiscover() {
        val d = device ?: return
        when (d.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                BleLog.line(BleLog.Kind.BOND, "already BONDED — discovering services")
                onBondState?.invoke(BluetoothDevice.BOND_BONDED)
                gatt?.discoverServices()
            }
            BluetoothDevice.BOND_BONDING -> {
                BleLog.line(BleLog.Kind.BOND, "bonding already in progress — waiting")
            }
            else -> {
                BleLog.line(BleLog.Kind.BOND, "not bonded — calling createBond()")
                if (!d.createBond()) {
                    BleLog.error("createBond() returned false; discovering anyway")
                    gatt?.discoverServices()
                }
            }
        }
    }

    private val bondReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
            val prev = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
            val now = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
            BleLog.line(BleLog.Kind.BOND, "bond state ${bondName(prev)} -> ${bondName(now)}")
            onBondState?.invoke(now)
            if (now == BluetoothDevice.BOND_BONDED) {
                BleLog.line(BleLog.Kind.BOND, "BONDED — discovering services")
                handler.postDelayed({ gatt?.discoverServices() }, 600)
            }
        }
    }

    private var bondReceiverRegistered = false
    private fun registerBondReceiver() {
        if (bondReceiverRegistered) return
        context.registerReceiver(
            bondReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
        bondReceiverRegistered = true
    }

    // -------------------------------------------------------- GATT callback

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            BleLog.line(BleLog.Kind.CONN, "onConnectionStateChange status=$status state=${stateName(newState)}")
            onConnectionState?.invoke(newState, status)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    BleLog.line(BleLog.Kind.CONN, "CONNECTED — requesting MTU 247")
                    g.requestMtu(247)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    BleLog.line(BleLog.Kind.CONN, "DISCONNECTED")
                    opInFlight = false
                    opQueue.clear()
                    cancelPendingTimeouts()
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            BleLog.line(BleLog.Kind.CONN, "MTU=$mtu status=$status — ensuring bond")
            onMtu?.invoke(mtu)
            ensureBondThenDiscover()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                BleLog.error("service discovery failed status=$status")
                return
            }
            g.services.forEach { svc ->
                BleLog.line(BleLog.Kind.SERVICE, "service ${svc.uuid}")
                svc.characteristics.forEach { ch ->
                    BleLog.line(
                        BleLog.Kind.SERVICE,
                        "  char ${Fx3Uuids.label(ch.uuid)} ${ch.uuid} props=${propString(ch.properties)}"
                    )
                }
            }
            val svc = g.getService(Fx3Uuids.SERVICE)
            if (svc == null) {
                BleLog.error("Provisioning service ${Fx3Uuids.SERVICE} NOT found")
                onServiceFound?.invoke(false)
                return
            }
            BleLog.line(BleLog.Kind.SERVICE, "Provisioning service FOUND — running AP sequence")
            onServiceFound?.invoke(true)
            runProvisioningSequence(g, svc.uuid)
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            status: Int,
        ) {
            @Suppress("DEPRECATION")
            handleRead(ch.uuid, status, ch.value)
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            handleRead(ch.uuid, status, value)
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            status: Int,
        ) {
            val label = Fx3Uuids.label(ch.uuid)
            BleLog.line(
                BleLog.Kind.WRITE,
                "onCharacteristicWrite $label status=$status${if (status != BluetoothGatt.GATT_SUCCESS) " FAILED" else ""}"
            )
            // One-time pairing: open the 10s EE03 observation window once EE01 write is acked.
            if (ch.uuid == Fx3Uuids.EE01 && pairingInProgress) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    startPairingObservationWindow()
                } else {
                    pairingInProgress = false
                    BleLog.error("[PAIR] EE01 write FAILED status=$status — pairing not initiated")
                }
            }
            dequeueNext()
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
        ) {
            handleNotify(ch.uuid, ch.value ?: byteArrayOf())
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            handleNotify(ch.uuid, value)
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            BleLog.line(
                BleLog.Kind.DESC,
                "descriptor write ${Fx3Uuids.label(descriptor.characteristic.uuid)} CCCD status=$status"
            )
            dequeueNext()
        }
    }

    // --------------------------------------------------- notification handler

    private fun handleNotify(uuid: UUID, value: ByteArray) {
        val label = Fx3Uuids.label(uuid)
        BleLog.hex(BleLog.Kind.NOTIFY, "notify $label", value)

        // Pairing notifications (EE service) — explicit tag during one-time pairing.
        if (uuid == Fx3Uuids.EE01 || uuid == Fx3Uuids.EE03 || uuid == Fx3Uuids.EE04) {
            BleLog.line(BleLog.Kind.NOTIFY, "[PAIR] $label notify: ${BleLog.toHex(value)}")
            if (uuid == Fx3Uuids.EE03 && pairingInProgress) {
                pairingEe03Seen = true
                BleLog.line(BleLog.Kind.NOTIFY, "[PAIR] EE03 notification received — pairing activity confirmed")
            }
        }

        // Observation window: log every target notification with elapsed time for post-WiFi analysis
        if (obsWindowActive && uuid in obsTargets) {
            val elapsed = System.currentTimeMillis() - obsWindowStartAt
            BleLog.line(BleLog.Kind.NOTIFY, "[OBS] +${elapsed}ms $label ${BleLog.toHex(value)}")
        }

        // CC0E — smartphone-control-setting result (after CCA3 write).
        // Parsed exactly per Creators' App BluetoothCharacteristicParser:
        //   type=(value[1]<<8)|value[2]; only 0x000B = SmartPhoneControlSetting.
        //   success only if value[3]==0x01; else reason = BE int value[4..7].
        if (waitingForCC0E && uuid == Fx3Uuids.CC0E) {
            val hex = BleLog.toHex(value)
            val res = parseSmartphoneControlResult(value)
            if (res == null) {
                // Not a SmartPhoneControlSetting frame (or too short) — keep waiting.
                BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC0E notify (not SmartPhoneControlSetting): $hex — still waiting")
                return
            }
            waitingForCC0E = false
            pendingCC0ETimeoutRunnable?.let { handler.removeCallbacks(it) }
            pendingCC0ETimeoutRunnable = null
            if (res.success) {
                BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC0E SmartPhoneControlSetting SUCCESS: $hex")
                onCc0eSuccess?.invoke()
                dequeueNext()
            } else {
                BleLog.error("[AP] CC0E SmartPhoneControlSetting FAILED (${res.reason}): $hex — continuing sequence (FX3A warning)")
                dequeueNext()
            }
            return
        }

        // CC09 — Wi-Fi status; log every payload, parse state only when waiting for AP ready.
        // Hardware: state byte is value[3].
        //   04 00 01 01 00 → state=01 (AP creating) — wait
        //   04 00 01 02 00 → state=02 (AP ready)    — proceed
        if (uuid == Fx3Uuids.CC09) {
            val hex = BleLog.toHex(value)
            BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC09 payload: $hex")
            if (waitingForCC09WifiLaunch) {
                val state = if (value.size >= 4) value[3].toInt() and 0xFF else -1
                when (state) {
                    0x01 -> BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC09 state=01 (creating AP) — waiting for state=02")
                    0x02 -> {
                        waitingForCC09WifiLaunch = false
                        pendingCC09TimeoutRunnable?.let { handler.removeCallbacks(it) }
                        pendingCC09TimeoutRunnable = null
                        BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC09 state=02 (AP ready) — reading credentials")
                        onCc09Launched?.invoke()
                        dequeueNext()
                    }
                    else -> BleLog.line(BleLog.Kind.NOTIFY, "[AP] CC09 state=0x${state.toString(16)} (unknown) — waiting")
                }
            }
            return
        }
    }

    // --------------------------------------------------- read handler

    private fun handleRead(uuid: UUID, status: Int, value: ByteArray?) {
        val label = Fx3Uuids.label(uuid)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            BleLog.line(BleLog.Kind.READ, "read $label FAILED status=$status")
            if (uuid == Fx3Uuids.EE02) BleLog.error("[EE] EE02 read FAILED status=$status")
            if (uuid == Fx3Uuids.EE04) BleLog.error("[EE] EE04 read FAILED status=$status")
        } else {
            BleLog.hex(BleLog.Kind.READ, "read $label OK", value)
            if (uuid == Fx3Uuids.EE02) {
                BleLog.line(BleLog.Kind.READ, "[EE] EE02 read result: ${BleLog.toHex(value ?: byteArrayOf())}")
            }
            if (uuid == Fx3Uuids.EE04) {
                BleLog.line(BleLog.Kind.READ, "[EE] EE04 read result: ${BleLog.toHex(value ?: byteArrayOf())}")
            }
            if (value != null && value.isNotEmpty()) {
                when (uuid) {
                    Fx3Uuids.CC06 -> {
                        wifiSsidRead = true
                        wifiSsid = decodeText(value)
                    }
                    Fx3Uuids.CC07 -> {
                        wifiPassRead = true
                        wifiPassword = decodeText(value)
                    }
                    Fx3Uuids.CC0C -> {
                        wifiBssidRead = true
                        wifiBssid = decodeText(value)
                    }
                }
                onConfirmedRead?.invoke(label, value)
                if (uuid == Fx3Uuids.CC17) decodeCc17(value)
            }
        }

        // After CC0C: all three credential chars have been attempted
        if (uuid == Fx3Uuids.CC0C) {
            if (wifiSsidRead && wifiPassRead && wifiBssidRead) {
                val s = wifiSsid ?: ""
                val p = wifiPassword ?: ""
                val b = wifiBssid ?: ""
                BleLog.line(BleLog.Kind.INFO, "[AP] credentials ready ssid='$s' bssid='$b'")
                if (s.isNotEmpty() && p.isNotEmpty() && b.isNotEmpty()) {
                    onWifiCredentialsReady?.invoke(s, p, b)
                } else {
                    BleLog.error("[WiFi] one or more credentials empty after read — AP may not be active")
                    onWifiFailed?.invoke()
                }
            } else {
                BleLog.error(
                    "[WiFi] Camera Wi-Fi credentials unavailable from BLE " +
                        "(ssid=$wifiSsidRead pass=$wifiPassRead bssid=$wifiBssidRead)"
                )
                onWifiFailed?.invoke()
            }
        }

        dequeueNext()
    }

    private fun decodeCc17(value: ByteArray) {
        BleLog.line(BleLog.Kind.READ, "decoding CC17 SSH TLV…")
        val r = SshInfoTlv.decode(value)
        BleLog.line(BleLog.Kind.READ, "  state=${r.state}")
        BleLog.line(BleLog.Kind.READ, "  sshId='${r.sshId}'")
        BleLog.line(BleLog.Kind.READ, "  sshPass='${r.sshPass}'")
        BleLog.line(BleLog.Kind.READ, "  fingerprint='${r.fingerprint}'")
        r.notes.forEach { BleLog.line(BleLog.Kind.READ, "  note: $it") }
        onSshDecoded?.invoke(r)
    }

    // --------------------------------------------------- provisioning sequence

    /**
     * Full connection sequence:
     *   1. Enable CC notify targets (CC03/09/0F/10/16/1B)
     *   2. Read device info (CC0A/CC0B/CCA1)
     *   3. Sony AP sequence:
     *        EE01 pairing write (optional) →
     *        CC0E notify enable →
     *        CCA3 smartphone mode ON →
     *        wait CC0E ack (30s) →
     *        CC08 Wi-Fi AP ON →
     *        wait CC09 Wi-Fi launched (30s) →
     *        read CC06/CC07/CC0C
     *
     * CC17 (SSH info) is intentionally omitted here.
     * Call retryReadCc17() after the phone has joined the camera Wi-Fi.
     */
    private fun runProvisioningSequence(g: BluetoothGatt, serviceUuid: UUID) {
        val svc = g.getService(serviceUuid) ?: return

        // ---- Step 0: EE service diagnostics (read registration state up front) ----
        // Subscribe EE03 permanently and read EE02/EE04 immediately after discovery so we
        // can see the camera's registration state BEFORE any pairing command is sent.
        enqueueEeStateDiagnostics(g, subscribeEe03 = true)

        // ---- Step 1: Enable notifications ----
        BleLog.info("=== STEP: enable notifications (ContinuousConnection / WaitingCameraState) ===")
        Fx3Uuids.NOTIFY_TARGETS.forEach { (lbl, uuid) ->
            val ch = svc.getCharacteristic(uuid)
            if (ch == null) {
                BleLog.line(BleLog.Kind.NOTIFY, "notify target $lbl not present — skipped")
            } else {
                enqueue { enableNotify(g, ch, lbl) }
            }
        }

        // ---- Step 2: Read confirmed device info ----
        BleLog.info("=== STEP: read confirmed device info ===")
        Fx3Uuids.CONFIRMED_READS.forEach { (lbl, uuid) ->
            enqueueRead(g, svc, uuid, lbl)
        }

        // ---- Step 3: Sony AP sequence ----
        BleLog.info("=== STEP: Sony AP sequence ===")
        BleLog.line(BleLog.Kind.INFO, "[AP] starting Sony AP sequence")

        // 3a. (REMOVED) EE01 pairing/registration write.
        //     EE01 = 06 08 01 00 00 00 00 is a ONE-TIME pairing/registration command
        //     (Sony PairingState). It is NOT part of Sony Creators' per-connect
        //     connectViaAp sequence, and re-sending it every connect pushes the camera
        //     back into a pre-registration state where it will not emit SSH credentials.
        //     Use runOneTimePairing() explicitly for first-time registration only.
        BleLog.line(BleLog.Kind.INFO, "[AP] EE01 pairing write intentionally skipped (per-connect path; use runOneTimePairing())")

        // 3b. Enable CC0E notify (smartphone connection result)
        val cc0eCh = svc.getCharacteristic(Fx3Uuids.CC0E)
        if (cc0eCh != null) {
            BleLog.line(BleLog.Kind.INFO, "[AP] CC0E notify enabled")
            enqueue { enableNotify(g, cc0eCh, "CC0E") }
        } else {
            BleLog.line(BleLog.Kind.INFO, "[AP] CC0E not found — skipping notify enable")
        }

        // 3c. Write CCA3 smartphone mode ON
        val cca3Ch = svc.getCharacteristic(Fx3Uuids.CCA3)
        if (cca3Ch != null) {
            enqueue {
                BleLog.line(BleLog.Kind.WRITE, "[AP] CCA3 smartphone mode ON -> 03 00 00 01")
                onSmartphoneModeOn?.invoke()
                writeChar(g, cca3Ch, "CCA3", byteArrayOf(0x03, 0x00, 0x00, 0x01))
            }
        } else {
            BleLog.error("[AP] CCA3 not found — smartphone mode write skipped")
            enqueue {
                onApSequenceFailed?.invoke("CCA3 characteristic not found")
                dequeueNext()
            }
        }

        // 3d. Wait sentinel: block queue until CC0E notify arrives (or 30s timeout)
        enqueue {
            BleLog.line(BleLog.Kind.INFO, "[AP] waiting for CC0E notify (smartphone mode ack, 30s max)…")
            waitingForCC0E = true
            val runnable = Runnable {
                if (waitingForCC0E) {
                    waitingForCC0E = false
                    BleLog.line(BleLog.Kind.INFO, "[AP] CC0E wait timeout — proceeding")
                    dequeueNext()
                }
            }
            pendingCC0ETimeoutRunnable = runnable
            handler.postDelayed(runnable, 30_000)
            // dequeueNext() called by handleNotify (CC0E) or the timeout above
        }

        // 3e. Write CC08 Wi-Fi AP ON
        val cc08Ch = svc.getCharacteristic(Fx3Uuids.CC08)
        if (cc08Ch != null) {
            enqueue {
                BleLog.line(BleLog.Kind.WRITE, "[AP] CC08 Wi-Fi AP ON -> 01")
                onWifiApOn?.invoke()
                writeChar(g, cc08Ch, "CC08", byteArrayOf(0x01))
            }
        } else {
            BleLog.error("[AP] CC08 not found — Wi-Fi AP write skipped")
            enqueue {
                onApSequenceFailed?.invoke("CC08 characteristic not found")
                dequeueNext()
            }
        }

        // 3f. Wait sentinel: block queue until CC09 Wi-Fi-launched notify (or 30s timeout)
        enqueue {
            BleLog.line(BleLog.Kind.INFO, "[AP] CC09 notify enabled")
            BleLog.line(BleLog.Kind.INFO, "[AP] waiting for CC09 notify (Wi-Fi launched, 30s max)…")
            waitingForCC09WifiLaunch = true
            val runnable = Runnable {
                if (waitingForCC09WifiLaunch) {
                    waitingForCC09WifiLaunch = false
                    BleLog.line(BleLog.Kind.INFO, "[AP] CC09 wait timeout — proceeding to credential reads")
                    BleLog.line(BleLog.Kind.INFO, "[AP] Wi-Fi launched or proceeding to credential reads")
                    dequeueNext()
                }
            }
            pendingCC09TimeoutRunnable = runnable
            handler.postDelayed(runnable, 30_000)
        }

        // ---- Step 4: Read Wi-Fi credentials ----
        BleLog.info("=== STEP: read Wi-Fi credentials (CC06/CC07/CC0C) ===")

        val cc06Ch = svc.getCharacteristic(Fx3Uuids.CC06)
        val cc07Ch = svc.getCharacteristic(Fx3Uuids.CC07)
        val cc0cCh = svc.getCharacteristic(Fx3Uuids.CC0C)

        if (cc06Ch != null) {
            enqueue {
                BleLog.line(BleLog.Kind.READ, "[AP] reading CC06 SSID")
                val ok = g.readCharacteristic(cc06Ch)
                if (!ok) { BleLog.error("[AP] readCharacteristic(CC06) returned false"); dequeueNext() }
            }
        } else {
            BleLog.error("[AP] CC06 not found")
        }

        if (cc07Ch != null) {
            enqueue {
                BleLog.line(BleLog.Kind.READ, "[AP] reading CC07 password")
                val ok = g.readCharacteristic(cc07Ch)
                if (!ok) { BleLog.error("[AP] readCharacteristic(CC07) returned false"); dequeueNext() }
            }
        } else {
            BleLog.error("[AP] CC07 not found")
        }

        if (cc0cCh != null) {
            enqueue {
                BleLog.line(BleLog.Kind.READ, "[AP] reading CC0C BSSID")
                val ok = g.readCharacteristic(cc0cCh)
                if (!ok) { BleLog.error("[AP] readCharacteristic(CC0C) returned false"); dequeueNext() }
            }
        } else {
            BleLog.error("[AP] CC0C not found")
            // Trigger failure path if CC0C missing — handleRead won't fire
            onWifiFailed?.invoke()
        }

        dequeueNext()
    }

    /**
     * Re-reads CC17 (SSH info) over BLE. Call this after the phone has joined
     * the camera Wi-Fi AP and the camera IP has been discovered from DHCP.
     *
     * Posts to the main handler so it is safe to call from any thread.
     */
    fun retryReadCc17() {
        handler.post {
            val g = gatt ?: run {
                BleLog.error("[AUTO] retryReadCc17: no active GATT connection")
                onSshDecoded?.invoke(SshInfoTlv.Result(SshInfoTlv.SshState.UNKNOWN, "", "", "", listOf("No GATT")))
                return@post
            }
            val svc = g.getService(Fx3Uuids.SERVICE) ?: run {
                BleLog.error("[AUTO] retryReadCc17: provisioning service not found")
                onSshDecoded?.invoke(SshInfoTlv.Result(SshInfoTlv.SshState.UNKNOWN, "", "", "", listOf("No CC service")))
                return@post
            }
            val ch = svc.getCharacteristic(Fx3Uuids.CC17) ?: run {
                BleLog.error("[AUTO] retryReadCc17: CC17 not found")
                onSshDecoded?.invoke(SshInfoTlv.Result(SshInfoTlv.SshState.UNKNOWN, "", "", "", listOf("CC17 absent")))
                return@post
            }
            BleLog.line(BleLog.Kind.INFO, "[AUTO] launching CC17 re-read (post-Wi-Fi join)")
            enqueue {
                BleLog.line(BleLog.Kind.READ, "reading CC17 sshInfo (post-Wi-Fi join)…")
                val ok = g.readCharacteristic(ch)
                if (!ok) { BleLog.error("readCharacteristic(CC17) returned false"); dequeueNext() }
            }
            dequeueNext()
        }
    }

    /**
     * ONE-TIME pairing / registration write (EE01 = 06 08 01 00 00 00 00).
     *
     * This is Sony's PairingState command. It must be sent only once, during
     * first-time registration of this phone with the camera — never on every
     * Scan/Connect. The normal connect path (runProvisioningSequence) no longer
     * sends it. Call this explicitly from a dedicated "Pair / Register" action.
     */
    fun runOneTimePairing() {
        handler.post {
            val g = gatt ?: run { BleLog.error("[PAIR] runOneTimePairing: no active GATT"); return@post }
            val eeSvc = g.getService(Fx3Uuids.EE_SERVICE) ?: run {
                BleLog.error("[PAIR] EE service (8000ee00) absent — cannot pair")
                return@post
            }
            val ee01Ch = eeSvc.getCharacteristic(Fx3Uuids.EE01) ?: run {
                BleLog.error("[PAIR] EE01 not found — cannot pair")
                return@post
            }
            BleLog.line(BleLog.Kind.INFO, "[PAIR] starting one-time pairing — enabling EE notifications")
            pairingInProgress = true
            pairingEe03Seen = false

            // Subscribe to EE01 (write-ack/notify) and EE03 (pairing notification) BEFORE writing EE01.
            listOf("EE01" to Fx3Uuids.EE01, "EE03" to Fx3Uuids.EE03).forEach { (lbl, u) ->
                eeSvc.getCharacteristic(u)?.let { ch ->
                    enqueue { enableNotify(g, ch, lbl) }
                } ?: BleLog.line(BleLog.Kind.INFO, "[PAIR] $lbl not present — skipping notify")
            }

            // Write EE01. The 10s pairing observation window is started from the
            // onCharacteristicWrite(EE01, status=0) callback.
            enqueue {
                BleLog.line(BleLog.Kind.WRITE, "[PAIR] EE01 one-time pairing write -> 06 08 01 00 00 00 00")
                writeChar(g, ee01Ch, "EE01", byteArrayOf(0x06, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00))
            }

            // Read EE04 after the EE01 write to capture any pairing result payload.
            val ee04Ch = eeSvc.getCharacteristic(Fx3Uuids.EE04)
            if (ee04Ch != null) {
                enqueue {
                    BleLog.line(BleLog.Kind.READ, "[PAIR] reading EE04 (pairing result)…")
                    val ok = g.readCharacteristic(ee04Ch)
                    if (!ok) { BleLog.error("[PAIR] readCharacteristic(EE04) returned false"); dequeueNext() }
                }
            } else {
                BleLog.line(BleLog.Kind.INFO, "[PAIR] EE04 not present — skipping read")
            }
            dequeueNext()
        }
    }

    /** Starts the 10s window that confirms (or refutes) EE01 pairing completion via EE03. */
    private fun startPairingObservationWindow() {
        pendingPairingTimeoutRunnable?.let { handler.removeCallbacks(it) }
        BleLog.line(BleLog.Kind.INFO, "[PAIR] EE01 write acked — opening 10s pairing observation window")
        val runnable = Runnable {
            if (pairingInProgress) {
                pairingInProgress = false
                if (!pairingEe03Seen) {
                    BleLog.error("[PAIR] no EE03 notification after EE01 write — pairing completion not confirmed")
                } else {
                    BleLog.line(BleLog.Kind.INFO, "[PAIR] pairing observation window closed (EE03 was received)")
                }
            }
        }
        pendingPairingTimeoutRunnable = runnable
        handler.postDelayed(runnable, 10_000)
    }

    /**
     * Opens a 30-second observation window. Every notification arriving on the
     * observation targets (CC09 / CC0F / CC10 / CCA1 / CCA5 / CCA9) is logged
     * with an [OBS] tag and milliseconds elapsed since window start.
     *
     * Call immediately after Wi-Fi gateway discovery to capture any camera-state
     * notifications that arrive while the phone is joining the AP.
     * Safe to call from any thread.
     */
    fun startObservationWindow() {
        handler.post {
            obsWindowStopRunnable?.let { handler.removeCallbacks(it) }
            obsWindowActive = true
            obsWindowStartAt = System.currentTimeMillis()
            BleLog.line(BleLog.Kind.INFO, "[OBS] 30-second observation window started (post-WiFi)")
            val stop = Runnable {
                obsWindowActive = false
                BleLog.line(BleLog.Kind.INFO, "[OBS] observation window closed (30 s elapsed)")
            }
            obsWindowStopRunnable = stop
            handler.postDelayed(stop, 30_000)
        }
    }

    // ------------------------------------------------------- EE diagnostics

    /**
     * Enqueues EE-service registration-state diagnostics: optionally subscribe EE03
     * (kept subscribed for the whole connection), then read EE02 and EE04 and log raw
     * hex / failure codes. Used both at service discovery and from Developer actions.
     */
    private fun enqueueEeStateDiagnostics(g: BluetoothGatt, subscribeEe03: Boolean) {
        val eeSvc = g.getService(Fx3Uuids.EE_SERVICE)
        if (eeSvc == null) {
            BleLog.error("[EE] EE service (8000ee00) absent — cannot read EE state")
            return
        }
        if (subscribeEe03) {
            eeSvc.getCharacteristic(Fx3Uuids.EE03)?.let { ch ->
                enqueue { enableNotify(g, ch, "EE03") }
                BleLog.line(BleLog.Kind.INFO, "[EE] subscribing EE03 (permanent for this connection)")
            } ?: BleLog.line(BleLog.Kind.INFO, "[EE] EE03 not present — cannot subscribe")
        }
        enqueueEeRead(g, eeSvc, Fx3Uuids.EE02, "EE02")
        enqueueEeRead(g, eeSvc, Fx3Uuids.EE04, "EE04")
    }

    private fun enqueueEeRead(
        g: BluetoothGatt,
        eeSvc: BluetoothGattService,
        uuid: UUID,
        label: String,
    ) {
        val ch = eeSvc.getCharacteristic(uuid)
        if (ch == null) {
            BleLog.error("[EE] $label not present in EE service — skipping read")
            return
        }
        enqueue {
            BleLog.line(BleLog.Kind.READ, "[EE] reading $label …")
            val ok = g.readCharacteristic(ch)
            if (!ok) { BleLog.error("[EE] readCharacteristic($label) returned false"); dequeueNext() }
        }
    }

    /** Developer action: read EE02 and log hex / failure. */
    fun readEe02() = postEeRead(Fx3Uuids.EE02, "EE02")

    /** Developer action: read EE04 and log hex / failure. */
    fun readEe04() = postEeRead(Fx3Uuids.EE04, "EE04")

    /** Developer action: read EE02 and EE04 and log both in hex. Writes nothing. */
    fun dumpEeState() {
        handler.post {
            val g = gatt ?: run { BleLog.error("[EE] dumpEeState: no active GATT"); return@post }
            val eeSvc = g.getService(Fx3Uuids.EE_SERVICE) ?: run {
                BleLog.error("[EE] dumpEeState: EE service absent")
                return@post
            }
            BleLog.line(BleLog.Kind.INFO, "[EE] === Dump EE State (EE02 + EE04) ===")
            enqueueEeRead(g, eeSvc, Fx3Uuids.EE02, "EE02")
            enqueueEeRead(g, eeSvc, Fx3Uuids.EE04, "EE04")
            dequeueNext()
        }
    }

    private fun postEeRead(uuid: UUID, label: String) {
        handler.post {
            val g = gatt ?: run { BleLog.error("[EE] read $label: no active GATT"); return@post }
            val eeSvc = g.getService(Fx3Uuids.EE_SERVICE) ?: run {
                BleLog.error("[EE] read $label: EE service absent")
                return@post
            }
            enqueueEeRead(g, eeSvc, uuid, label)
            dequeueNext()
        }
    }

    // ------------------------------------------------------- GATT ops

    private fun enableNotify(g: BluetoothGatt, ch: BluetoothGattCharacteristic, label: String) {
        val ok = g.setCharacteristicNotification(ch, true)
        BleLog.line(BleLog.Kind.NOTIFY, "setCharacteristicNotification $label = $ok")
        val cccd = ch.getDescriptor(Fx3Uuids.CCCD)
        if (cccd == null) {
            BleLog.line(BleLog.Kind.DESC, "$label has no CCCD — cannot subscribe")
            dequeueNext()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            g.writeDescriptor(cccd)
        }
        BleLog.line(BleLog.Kind.DESC, "writing CCCD enable-notify for $label")
        // onDescriptorWrite drives the queue forward
    }

    private fun writeChar(g: BluetoothGatt, ch: BluetoothGattCharacteristic, label: String, data: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val result = g.writeCharacteristic(ch, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            BleLog.line(BleLog.Kind.WRITE, "writeCharacteristic $label result=$result")
            if (result != BluetoothGatt.GATT_SUCCESS) {
                BleLog.error("writeCharacteristic($label) initiation failed result=$result")
                dequeueNext()
            }
            // On GATT_SUCCESS the write is pending; onCharacteristicWrite will call dequeueNext()
        } else {
            @Suppress("DEPRECATION")
            ch.value = data
            ch.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            @Suppress("DEPRECATION")
            val ok = g.writeCharacteristic(ch)
            BleLog.line(BleLog.Kind.WRITE, "writeCharacteristic $label ok=$ok")
            if (!ok) {
                BleLog.error("writeCharacteristic($label) returned false")
                dequeueNext()
            }
        }
    }

    private fun enqueueRead(
        g: BluetoothGatt,
        svc: android.bluetooth.BluetoothGattService,
        uuid: UUID,
        label: String,
    ) {
        val ch = svc.getCharacteristic(uuid)
        if (ch == null) {
            BleLog.line(BleLog.Kind.READ, "$label not present in service — skipped")
            return
        }
        enqueue {
            BleLog.line(BleLog.Kind.READ, "reading $label …")
            val ok = g.readCharacteristic(ch)
            if (!ok) {
                BleLog.error("readCharacteristic($label) returned false")
                dequeueNext()
            }
        }
    }

    // ------------------------------------------------------------ op queue

    /** Parsed CC0E SmartPhoneControlSetting result. */
    private data class SmartphoneControlResult(val success: Boolean, val reason: String)

    /**
     * Parses a CC0E frame as a SmartPhoneControlSetting result, exactly per Creators' App
     * BluetoothCharacteristicParser.parseCameraInitialSettingResult:
     *   type = (value[1] << 8) | value[2]; only 0x000B = SmartPhoneControlSetting
     *   success only if value[3] == 0x01
     *   failure if value[3] == 0x00, reason = big-endian int value[4..7]
     *     1 = StatusError, 2 = ParameterError
     * Returns null if the frame is not a SmartPhoneControlSetting result or is too short.
     */
    private fun parseSmartphoneControlResult(v: ByteArray): SmartphoneControlResult? {
        if (v.size < 8) return null
        val type = ((v[1].toInt() and 0xFF) shl 8) or (v[2].toInt() and 0xFF)
        if (type != 0x000B) return null
        if ((v[3].toInt() and 0xFF) == 0x01) return SmartphoneControlResult(true, "Success")
        val reasonCode = ((v[4].toInt() and 0xFF) shl 24) or ((v[5].toInt() and 0xFF) shl 16) or
            ((v[6].toInt() and 0xFF) shl 8) or (v[7].toInt() and 0xFF)
        val reason = when (reasonCode) {
            1 -> "StatusError"
            2 -> "ParameterError"
            else -> "Unknown(0x${reasonCode.toString(16)})"
        }
        return SmartphoneControlResult(false, reason)
    }

    /** Aborts the in-flight AP sequence: clears the queue, cancels waits, surfaces fatal reason. */
    private fun abortApSequence(reason: String) {
        cancelPendingTimeouts()
        opQueue.clear()
        BleLog.error("[AP] AP sequence aborted — $reason")
        onApSequenceFailed?.invoke(reason)
    }

    private fun enqueue(op: () -> Unit) {
        opQueue.add(op)
    }

    private fun dequeueNext() {
        handler.post {
            if (opInFlight) {
                opInFlight = false
            }
            val op = opQueue.poll()
            if (op == null) {
                if (!opInFlight) BleLog.info("=== sequence queue drained ===")
                return@post
            }
            opInFlight = true
            runCatching { op() }.onFailure {
                BleLog.error("op threw: ${it.message}")
                opInFlight = false
            }
        }
    }

    private fun cancelPendingTimeouts() {
        pendingCC0ETimeoutRunnable?.let { handler.removeCallbacks(it) }
        pendingCC0ETimeoutRunnable = null
        pendingCC09TimeoutRunnable?.let { handler.removeCallbacks(it) }
        pendingCC09TimeoutRunnable = null
        waitingForCC0E = false
        waitingForCC09WifiLaunch = false
        obsWindowStopRunnable?.let { handler.removeCallbacks(it) }
        obsWindowStopRunnable = null
        obsWindowActive = false
        pendingPairingTimeoutRunnable?.let { handler.removeCallbacks(it) }
        pendingPairingTimeoutRunnable = null
        pairingInProgress = false
    }

    // --------------------------------------------------------------- utils

    private fun decodeText(value: ByteArray): String {
        val s = String(value, Charsets.UTF_8)
        return s.filter { it.code in 0x20..0x7E || it.code > 0xA0 }.trim()
    }

    private fun bondName(s: Int) = when (s) {
        BluetoothDevice.BOND_NONE -> "BOND_NONE"
        BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
        BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
        else -> "BOND_?($s)"
    }

    private fun stateName(s: Int) = when (s) {
        BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
        BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
        BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
        BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
        else -> "STATE_?($s)"
    }

    private fun propString(p: Int): String {
        val parts = mutableListOf<String>()
        if (p and BluetoothGattCharacteristic.PROPERTY_READ != 0) parts += "READ"
        if (p and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) parts += "WRITE"
        if (p and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) parts += "WRITE_NR"
        if (p and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) parts += "NOTIFY"
        if (p and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) parts += "INDICATE"
        return parts.joinToString("|").ifEmpty { "none" }
    }
}
