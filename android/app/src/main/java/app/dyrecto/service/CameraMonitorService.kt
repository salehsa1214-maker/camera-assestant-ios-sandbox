package app.dyrecto.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import app.dyrecto.R
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.ConnectionPhase
import app.dyrecto.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps an **established** monitoring session alive across backgrounding
 * and screen-off. It owns no session state — the session lives in [MonitoringSession] (via
 * [MonitoringSessionProvider]). The service's only jobs are to (a) make the process a foreground
 * process so the OS won't kill/throttle it, (b) hold the CPU + Wi-Fi locks ([MonitoringLocks]),
 * and (c) render a persistent monitoring-status notification.
 *
 * Start/stop is driven by [DefaultMonitoringSession] on the active-monitoring edge, so this
 * service never shows a "Connecting…" state — its presence means monitoring is live.
 */
class CameraMonitorService : Service() {

    private val session: MonitoringSession by lazy { MonitoringSessionProvider.current() }
    private val locks: MonitoringLocks by lazy { MonitoringLocks(this) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var renderJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            session.disconnect()
            stopSelf()
            return START_NOT_STICKY
        }

        // Foreground + locks first (must call startForeground promptly). acquire() is idempotent.
        startForegroundCompat(buildNotification(session.state.value))
        locks.acquire()

        if (renderJob == null) {
            // Re-render on every state change, plus a slow tick so the "freshness" line stays
            // honest even when a stalled session emits no new state.
            renderJob = scope.launch {
                launch {
                    session.state.collect { state -> notify(buildNotification(state)) }
                }
                while (isActive) {
                    delay(FRESHNESS_TICK_MS)
                    notify(buildNotification(session.state.value))
                }
            }
        }
        // Dead session can't be rebuilt from a sticky restart, so don't auto-restart if killed.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        renderJob?.cancel()
        renderJob = null
        scope.coroutineContext[Job]?.cancel()
        locks.release()
        super.onDestroy()
    }

    // ----------------------------------------------------------- notification

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun notify(notification: Notification) {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // No-op if POST_NOTIFICATIONS is denied — monitoring is unaffected (notification only).
        runCatching { mgr.notify(NOTIF_ID, notification) }
    }

    private fun buildNotification(state: CameraConnectionState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val disconnectIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, CameraMonitorService::class.java).setAction(ACTION_DISCONNECT),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(primaryStatus(state))
            .setContentText(secondaryLine(state))
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, "Disconnect", disconnectIntent)
            .build()
    }

    /**
     * Primary line = monitoring status. Structured so a future active alert can take over this
     * slot (status would drop to the secondary line). Telemetry like battery is never the headline.
     */
    private fun primaryStatus(state: CameraConnectionState): String = when {
        state.phase == ConnectionPhase.ERROR -> "Connection lost"
        isStalled(state) -> "Telemetry stalled"
        else -> "Connected · monitoring"
    }

    private fun secondaryLine(state: CameraConnectionState): String {
        val parts = mutableListOf<String>()
        recordingLabel(state)?.let { parts += it }
        parts += freshnessLabel(state)
        return parts.joinToString(" · ")
    }

    private fun recordingLabel(state: CameraConnectionState): String? =
        when (state.telemetry?.get(CameraTelemetry.MOVIE_REC)?.rawNumber) {
            1L -> "● Recording"
            0L -> "Idle"
            else -> null
        }

    private fun freshnessLabel(state: CameraConnectionState): String {
        val last = state.lastTelemetryUpdateAt ?: return "Connecting…"
        val ageMs = System.currentTimeMillis() - last
        return when {
            ageMs < FRESH_THRESHOLD_MS -> "Live"
            else -> "Updated ${ageMs / 1000}s ago"
        }
    }

    private fun isStalled(state: CameraConnectionState): Boolean {
        val last = state.lastTelemetryUpdateAt ?: return false
        return System.currentTimeMillis() - last >= STALL_THRESHOLD_MS
    }

    companion object {
        const val ACTION_DISCONNECT = "app.dyrecto.action.DISCONNECT"

        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "camera_monitoring"
        private const val FRESHNESS_TICK_MS = 5_000L
        private const val FRESH_THRESHOLD_MS = 6_000L
        private const val STALL_THRESHOLD_MS = 30_000L

        /** Created once at app startup; safe to call repeatedly. */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Camera monitoring",
                // LOW: silent, persistent status — alerts are the Alert Engine's job, not this.
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Ongoing camera monitoring session status"
                setShowBadge(false)
            }
            mgr.createNotificationChannel(channel)
        }
    }
}
