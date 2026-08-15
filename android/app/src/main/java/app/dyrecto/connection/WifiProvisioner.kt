package app.dyrecto.connection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Joins the camera's DIRECT-* Wi-Fi AP using WifiNetworkSpecifier (Android 10+)
 * and discovers the camera IP address from the DHCP gateway via LinkProperties.
 *
 * Requires Android 10 (API 29). Returns an error on older devices.
 *
 * Caller must call release() when the network is no longer needed
 * (e.g. on disconnect) to unregister the network callback.
 */
@SuppressLint("MissingPermission", "NewApi")
class WifiProvisioner(private val context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val gatewayFound = AtomicBoolean(false)

    /** Fires when the Wi-Fi association succeeds (before DHCP completes). */
    var onAssociated: (() -> Unit)? = null
    /** Fires when DHCP is known to be in progress (right after association). */
    var onDhcpPending: (() -> Unit)? = null
    /** Fires once when the default-route gateway IP is discovered from LinkProperties. */
    var onGatewayDiscovered: ((ip: String, network: Network) -> Unit)? = null
    /** Fires if the network cannot be obtained within the timeout or is unavailable. */
    var onFailed: ((reason: String) -> Unit)? = null

    /**
     * Requests the Android system to join [ssid] / WPA2 [password] / [bssid].
     *
     * This is a peer-to-peer (non-internet) join: the request explicitly removes
     * the internet capability so Android won't reject it for lacking internet.
     * Process sockets are bound to this network on [onAssociated] so SSH/PTP
     * can reach the camera.
     */
    fun connect(ssid: String, password: String, bssid: String) {
        BleLog.line(BleLog.Kind.INFO, "[WiFi] requesting network ssid='$ssid' bssid='$bssid'")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val reason = "WifiNetworkSpecifier requires Android 10+ (API 29); device is API ${Build.VERSION.SDK_INT}"
            BleLog.error("[WiFi] $reason")
            onFailed?.invoke(reason)
            return
        }

        gatewayFound.set(false)

        val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .apply {
                runCatching {
                    setBssid(android.net.MacAddress.fromString(bssid))
                }.onFailure {
                    BleLog.error("[WiFi] could not parse BSSID '$bssid': ${it.message} — connecting without BSSID filter")
                }
            }
            .build()

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                BleLog.line(BleLog.Kind.INFO, "[WiFi] associated")
                // Bind process sockets to this (internet-less) network immediately
                // so SSH/PTP connections route through the camera AP.
                val bound = cm.bindProcessToNetwork(network)
                BleLog.line(BleLog.Kind.INFO, "[WiFi] bound process to camera network (ok=$bound)")
                onAssociated?.invoke()
                BleLog.line(BleLog.Kind.INFO, "[WiFi] DHCP in progress")
                onDhcpPending?.invoke()
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                val gateway = linkProperties.routes
                    .firstOrNull { it.isDefaultRoute && it.gateway != null }
                    ?.gateway
                    ?.hostAddress

                if (gateway != null && gatewayFound.compareAndSet(false, true)) {
                    BleLog.line(BleLog.Kind.INFO, "[WiFi] DHCP complete gateway=$gateway")
                    BleLog.line(BleLog.Kind.INFO, "[AUTO] cameraIp discovered=$gateway")
                    onGatewayDiscovered?.invoke(gateway, network)
                }
            }

            override fun onUnavailable() {
                BleLog.error("[WiFi] network unavailable — camera AP join failed")
                onFailed?.invoke("Camera Wi-Fi network unavailable (check SSID/password and that camera AP is active)")
            }

            override fun onLost(network: Network) {
                BleLog.line(BleLog.Kind.INFO, "[WiFi] network lost")
                if (!gatewayFound.get()) {
                    onFailed?.invoke("Camera Wi-Fi lost before IP was discovered")
                }
            }
        }

        networkCallback = callback
        // 30-second timeout; onUnavailable fires if join doesn't succeed in time
        cm.requestNetwork(request, callback, 30_000)
    }

    /** Unregisters the network callback. Call when the session ends. */
    fun release() {
        networkCallback?.let {
            runCatching { cm.unregisterNetworkCallback(it) }
            networkCallback = null
        }
        runCatching { cm.bindProcessToNetwork(null) }
    }
}
