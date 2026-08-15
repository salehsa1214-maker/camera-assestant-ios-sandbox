package app.dyrecto.connection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.DirectConnection
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive
import net.schmizz.sshj.userauth.method.PasswordResponseProvider
import net.schmizz.sshj.userauth.password.PasswordUtils
import java.io.InputStream
import java.io.OutputStream

/**
 * Establishes the verified SSH transport from CC17-decoded sshId/sshPass and,
 * while BLE stays connected, performs SSH auth to the camera and the two direct
 * channels to localhost:15740 (PTP/IP), recording timestamps at each phase.
 *
 * Runs entirely in-app (SSHJ) — NOT Windows OpenSSH.
 *
 * VERIFIED STACK — connect/auth/channel logic carried over UNCHANGED. The only
 * addition is [lastPtpResult], which captures the structured PTP session result
 * (previously discarded) so the product layer can render it. Behaviour is
 * unchanged.
 */
class SshTunnelTester(private val context: Context) {

    /** All times in epoch millis; null until reached. Monotonic deltas via nanos. */
    data class Timing(
        var cc17ReadAt: Long? = null,
        var sshConnectStartAt: Long? = null,
        var sshConnectedAt: Long? = null,
        var authStartAt: Long? = null,
        var authResultAt: Long? = null,
        var authSucceeded: Boolean = false,
        var keyboardInteractiveTried: Boolean = false,
        var keyboardInteractiveSupported: Boolean = false,
        var ptpInitSentAt: Long? = null,
        var ptpAckAt: Long? = null,
        var ptpAckType: Int? = null,
        var directMode: Boolean = false,
    )

    /** Captured structured PTP/IP result from the most recent run (additive). */
    @Volatile
    var lastPtpResult: PtpIpClient.SessionResult? = null
        private set

    /** Wired to the active PtpIpClient so event-driven refreshes reach the product layer. */
    var onTelemetryRefreshed: ((Map<Int, PtpIpClient.SonyProp>) -> Unit)? = null

    /** The client owning the live event listener, if any. */
    @Volatile
    private var activeClient: PtpIpClient? = null

    /** The SSH transport backing the live session's PTP channels (SSH=ON path only). Retained past
     *  run()'s return so the event listener + live-view command channel survive on the tunnel; torn
     *  down explicitly in [stopSession]. Never set on the direct (SSH=OFF) path. */
    @Volatile
    private var activeSsh: SSHClient? = null

    /** Serializes the run()-finally hand-off of [activeSsh] against a concurrent [stopSession] so the
     *  SSH transport is disconnected exactly once and never retained without an owner. */
    private val lifecycleLock = Any()

    /** Set by [stopSession]; read by run()'s finally to skip retaining a tunnel that's being torn down
     *  in the race window between the listener starting and the finally assigning [activeSsh]. */
    @Volatile
    private var teardownRequested = false

    /**
     * Ends the live session: stops the event-driven telemetry listener (closing its sockets) and
     * disconnects the retained SSH transport (SSH=ON path). Idempotent; safe on the direct path
     * (where [activeSsh] is always null, so it reduces to the old stop-listener behavior).
     */
    fun stopSession() = synchronized(lifecycleLock) {
        teardownRequested = true
        runCatching { activeClient?.stopEventListener() } // channels first
        runCatching { activeSsh?.disconnect() }           // then the transport it rides on
        runCatching { activeSsh?.close() }
        activeSsh = null
        activeClient = null
    }

    /**
     * Additive PoC hook (Push Live View): forwards a Sony SDIO operation to the live [PtpIpClient]
     * over its already-open command channel. Returns null if there is no active client/channel.
     */
    fun sendSonyOperation(
        opcode: Int,
        params: IntArray,
        dataOut: ByteArray?,
    ): PtpIpClient.OpResult? = activeClient?.sendSonyOperation(opcode, params, dataOut)

    /**
     * Opens a fresh SSH direct-tcpip channel to [host]:[port] on the camera side, over the retained
     * live session's SSH transport (SSH=ON bodies only, e.g. the A7 V). This is the same primitive
     * the PTP session uses for its command/event channels ([newDirectConnection]); used to reach the
     * camera's localhost-only HTTP Live View endpoint through the tunnel.
     *
     * Returns null when there is no live SSH transport (the direct/SSH=OFF path never sets [activeSsh],
     * so the FX3 flow is unaffected) or while a teardown is in progress. The returned channel rides the
     * same SSH client and is closed when the caller closes it or when [stopSession] disconnects the
     * transport.
     */
    fun newTunneledConnection(host: String, port: Int): PtpIpClient.Channel? =
        synchronized(lifecycleLock) {
            val ssh = activeSsh
            if (ssh == null || teardownRequested) return@synchronized null
            runCatching {
                val dc: DirectConnection = ssh.newDirectConnection(host, port)
                object : PtpIpClient.Channel {
                    override val input: InputStream get() = dc.inputStream
                    override val output: OutputStream get() = dc.outputStream
                    override fun close() { runCatching { dc.close() } }
                }
            }.getOrNull()
        }

    /** Called when CC17 was decoded; record the instant credentials became known. */
    fun markCc17Read(t: Timing = currentTiming) {
        t.cc17ReadAt = System.currentTimeMillis()
        BleLog.line(BleLog.Kind.INFO, "[TIMING] CC17 read/decoded at ${t.cc17ReadAt}")
    }

    @Volatile
    var currentTiming = Timing()

    /**
     * Blocking — call on a background thread. Performs:
     *   connect → force keyboard-interactive auth (fallback password) →
     *   direct channel to localhost:15740 → PTP-IP Init → read ACK → OpenSession →
     *   GetDeviceInfo.
     */
    fun run(cameraIp: String, port: Int, sshId: String, sshPass: String): Timing {
        val t = currentTiming
        if (t.cc17ReadAt == null) markCc17Read(t)

        // Reap any prior live session before starting a new one (defensive against a reconnect that
        // didn't route through stopSession), then arm the retain path for this run.
        stopSession()
        teardownRequested = false

        // Ensure the FULL BouncyCastle is in place before SSHJ touches EC crypto.
        try {
            BcFix.install()
        } catch (e: Exception) {
            BleLog.error("Aborting SSH test — BC provider not usable: ${e.message}")
            return t
        }

        bindToWifiNetwork()

        val cfg = DefaultConfig()
        val ssh = SSHClient(cfg)
        ssh.connectTimeout = 10_000
        ssh.timeout = 10_000
        ssh.addHostKeyVerifier(PromiscuousVerifier()) // accept camera host key

        // True once the event listener has taken ownership of the PTP channels: the tunnel must then
        // outlive run() (the listener + liveness + live-view command channel ride it) and is torn down
        // only in stopSession(). Sampled from the FRESH client below — never from the activeClient
        // field, which may point at a prior/torn-down client.
        var sessionLive = false
        try {
            log("SSH connect -> $cameraIp:$port (id='$sshId')")
            t.sshConnectStartAt = System.currentTimeMillis()
            ssh.connect(cameraIp, port)
            t.sshConnectedAt = System.currentTimeMillis()
            log("[TIMING] TCP+transport up in ${delta(t.sshConnectStartAt, t.sshConnectedAt)} ms")

            authenticate(ssh, sshId, sshPass, t)

            if (!ssh.isAuthenticated) {
                log("AUTH FAILED — not attempting tunnel")
                return t
            }

            log("[TIMING] AUTH OK in ${delta(t.authStartAt, t.authResultAt)} ms " +
                "(total CC17->auth = ${delta(t.cc17ReadAt, t.authResultAt)} ms)")

            val client = runPtpSession(ssh, t)
            sessionLive = client.isEventListenerActive
        } catch (e: Exception) {
            t.authResultAt = t.authResultAt ?: System.currentTimeMillis()
            BleLog.error("SSH test exception: ${e.javaClass.simpleName}: ${e.message}")
            e.stackTrace.take(6).forEach { BleLog.error("    at $it") }
        } finally {
            synchronized(lifecycleLock) {
                if (sessionLive && !teardownRequested) {
                    // Listener owns the channels — keep the tunnel alive for the session.
                    activeSsh = ssh
                    log("SSH transport retained — event listener owns the session")
                } else {
                    // Failure / no listener / teardown already requested — clean up now.
                    runCatching { ssh.disconnect() }
                    runCatching { ssh.close() }
                }
            }
            unbindNetwork() // unconditional — established sockets survive process unbind
        }
        return t
    }

    /**
     * Blocking — call on a background thread. Connects directly to cameraIp:15740 via a
     * plain TCP socket (no SSH), then runs the full PTP/IP sequence. Used when CC17
     * reports SSH=OFF — the FX3A in smartphone-connect mode exposes PTP/IP directly.
     */
    fun runDirect(cameraIp: String): Timing {
        val t = currentTiming.also { it.directMode = true }
        if (t.cc17ReadAt == null) markCc17Read(t)
        bindToWifiNetwork()
        try {
            val client = PtpIpClient { _, _ ->
                BleLog.line(BleLog.Kind.INFO, "[PTP/TCP] target=$cameraIp:15740")
                BleLog.line(BleLog.Kind.INFO, "[PTP/TCP] socket connect start")
                val socket = java.net.Socket()
                try {
                    socket.connect(java.net.InetSocketAddress(cameraIp, 15740), 10_000)
                    // Short read poll so PtpIpClient.readFully's 10s deadline is actually reachable:
                    // a blocking read on a hung camera otherwise sticks until the socket dies (~46s),
                    // which collapses the PTP/Wi-Fi/BLE stack. The poll just makes inp.read()
                    // interruptible; readFully re-loops on SocketTimeoutException. Does not bound a
                    // single operation — only how often the deadline is checked.
                    socket.soTimeout = PTP_READ_POLL_MS
                    BleLog.line(BleLog.Kind.INFO,
                        "[PTP/TCP] connected ✔  local=:${socket.localPort}  remote=$cameraIp:${socket.port}  soTimeout=${PTP_READ_POLL_MS}ms")
                } catch (e: java.net.SocketTimeoutException) {
                    BleLog.error("[PTP/TCP] TIMEOUT — no response from $cameraIp:15740 after 10s")
                    throw e
                } catch (e: java.net.ConnectException) {
                    BleLog.error("[PTP/TCP] REFUSED — $cameraIp:15740 rejected connection: ${e.message}")
                    throw e
                } catch (e: Exception) {
                    BleLog.error("[PTP/TCP] FAILED — ${e.javaClass.simpleName}: ${e.message}")
                    throw e
                }
                object : PtpIpClient.Channel {
                    override val input: java.io.InputStream  get() = socket.getInputStream()
                    override val output: java.io.OutputStream get() = socket.getOutputStream()
                    override fun close() {
                        BleLog.line(BleLog.Kind.INFO,
                            "[PTP/TCP] closing socket  connected=${socket.isConnected}  closed=${socket.isClosed}")
                        runCatching { socket.close() }
                    }
                }
            }
            client.onTelemetryRefreshed = onTelemetryRefreshed
            activeClient = client
            log("Direct PTP/IP -> $cameraIp:15740")
            val ok = client.run(t)
            lastPtpResult = client.result
            log(if (ok) "Direct PTP/IP OK ✔" else "Direct PTP/IP did not complete GetDeviceInfo")
        } catch (e: Exception) {
            BleLog.error("[PTP/TCP] top-level exception: ${e.javaClass.simpleName}: ${e.message}")
            e.stackTrace.take(6).forEach { BleLog.error("    at $it") }
        } finally {
            unbindNetwork()
        }
        return t
    }

    // ----------------------------------------------------------- auth

    private fun authenticate(ssh: SSHClient, user: String, pass: String, t: Timing) {
        t.authStartAt = System.currentTimeMillis()
        log("[TIMING] auth start at ${t.authStartAt}")

        // Keyboard-interactive ONLY (per SSH_HANDSHAKE_BC_FIX.md §5c): the server
        // offers only keyboard-interactive; PasswordResponseProvider answers each
        // prompt with sshPass verbatim. No authPassword fallback.
        t.keyboardInteractiveTried = true
        try {
            log("keyboard-interactive auth (PasswordResponseProvider)…")
            val ki = AuthKeyboardInteractive(
                PasswordResponseProvider(PasswordUtils.createOneOff(pass.toCharArray()))
            )
            ssh.auth(user, ki)
            t.keyboardInteractiveSupported = true
            t.authSucceeded = ssh.isAuthenticated
            t.authResultAt = System.currentTimeMillis()
            log("keyboard-interactive result: authenticated=${ssh.isAuthenticated}")
        } catch (e: Exception) {
            t.authResultAt = System.currentTimeMillis()
            val unsupported = e.message?.contains("keyboard-interactive", ignoreCase = true) == true ||
                e.message?.contains("not available", ignoreCase = true) == true
            t.keyboardInteractiveSupported = !unsupported
            BleLog.error("keyboard-interactive failed: ${e.javaClass.simpleName}: ${e.message} " +
                "(supported≈${t.keyboardInteractiveSupported})")
        }
    }

    // ----------------------------------------------------- PTP/IP session

    /**
     * Hands the authenticated SSH client to [PtpIpClient] as a channel factory.
     * PTP/IP needs TWO channels to localhost:15740 (command + event); each is a
     * fresh SSHJ direct connection. SSH/auth code above is untouched.
     */
    private fun runPtpSession(ssh: SSHClient, t: Timing): PtpIpClient {
        val client = PtpIpClient { host, port ->
            val dc: DirectConnection = ssh.newDirectConnection(host, port)
            object : PtpIpClient.Channel {
                override val input: InputStream get() = dc.inputStream
                override val output: OutputStream get() = dc.outputStream
                override fun close() { runCatching { dc.close() } }
            }
        }
        client.onTelemetryRefreshed = onTelemetryRefreshed
        activeClient = client
        val ok = client.run(t)
        lastPtpResult = client.result
        log(if (ok) "PTP/IP session reached GetDeviceInfo OK ✔" else "PTP/IP session did not complete GetDeviceInfo")
        return client
    }

    // ------------------------------------------------- network binding

    /** Force this process's sockets onto the (internet-less) camera Wi-Fi AP. */
    private fun bindToWifiNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.allNetworks.firstOrNull { n ->
            cm.getNetworkCapabilities(n)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
        if (net != null) {
            val bound = cm.bindProcessToNetwork(net)
            log("bound process to Wi-Fi network=$net ok=$bound")
        } else {
            BleLog.error("no Wi-Fi network found to bind — ensure phone joined the camera AP")
        }
    }

    private fun unbindNetwork() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        runCatching { cm.bindProcessToNetwork(null) }
    }

    private fun delta(a: Long?, b: Long?): Long =
        if (a != null && b != null) b - a else -1

    private fun log(m: String) = BleLog.line(BleLog.Kind.INFO, "[SSH] $m")

    private companion object {
        /** Direct PTP socket read-poll interval (ms). Keeps blocking reads interruptible so the
         *  PtpIpClient read deadline can fire instead of hanging until the socket dies. */
        const val PTP_READ_POLL_MS = 2_000
    }
}
