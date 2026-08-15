package app.dyrecto.service

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager

/**
 * Owns the CPU + Wi-Fi locks that keep an established monitoring session alive while the device
 * sleeps. Owned solely by [CameraMonitorService]; acquired exactly once on start and released
 * exactly once on stop.
 *
 * Both locks are created **non-reference-counted** and guarded by [held], so [acquire]/[release]
 * are idempotent — removing the classic "released more times than acquired" crash and the
 * "acquired twice across reconnects, never fully released" leak. Because the service lifetime is
 * bounded to the connect→disconnect window, no lock can ever outlive monitoring.
 */
class MonitoringLocks(context: Context) {

    private val appContext = context.applicationContext
    private var held = false

    private val wakeLock: PowerManager.WakeLock? = runCatching {
        val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_TAG).apply {
            setReferenceCounted(false)
        }
    }.getOrNull()

    private val wifiLock: WifiManager.WifiLock? = runCatching {
        val wm = appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WifiManager.WIFI_MODE_FULL_LOW_LATENCY
        } else {
            @Suppress("DEPRECATION")
            WifiManager.WIFI_MODE_FULL_HIGH_PERF
        }
        wm.createWifiLock(mode, WIFI_TAG).apply {
            setReferenceCounted(false)
        }
    }.getOrNull()

    /** Acquire both locks. No-op if already held. */
    @Synchronized
    fun acquire() {
        if (held) return
        held = true
        runCatching { wakeLock?.acquire() }
        runCatching { wifiLock?.acquire() }
    }

    /** Release both locks. No-op if not held. */
    @Synchronized
    fun release() {
        if (!held) return
        held = false
        runCatching { if (wakeLock?.isHeld == true) wakeLock.release() }
        runCatching { if (wifiLock?.isHeld == true) wifiLock.release() }
    }

    private companion object {
        const val WAKE_TAG = "cameraassistant:monitor"
        const val WIFI_TAG = "cameraassistant:monitor-wifi"
    }
}
