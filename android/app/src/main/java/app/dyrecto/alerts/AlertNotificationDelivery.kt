package app.dyrecto.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.dyrecto.R
import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertSeverity
import app.dyrecto.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Posts a transient heads-up notification for each alert. One notification ID ([ALERT_NOTIF_ID])
 * is shared by all alerts, so every new alert replaces the previous one — the notification shade
 * never accumulates old events. The in-app Alert History screen is the authoritative record.
 *
 * Auto-dismiss uses a tracked [Job] rather than [android.app.Notification.timeoutAfter]:
 * when a new alert arrives, [autoDismissJob] is cancelled before the notification is posted,
 * giving the incoming notification a fresh full [AUTO_DISMISS_MS] window — the older timer
 * can never fire early and cut short the newer notification.
 *
 * The [camera_alerts] channel is HIGH importance (heads-up banner + lock-screen), with
 * vibration and sound disabled because [AndroidAlertFeedback] owns those patterns.
 */
class AlertNotificationDelivery(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var autoDismissJob: Job? = null

    fun deliver(alert: Alert) {
        // Cancel any pending auto-dismiss for the previous notification before posting the
        // new one, so the incoming alert always gets a fresh full AUTO_DISMISS_MS window.
        autoDismissJob?.cancel()

        val contentIntent = PendingIntent.getActivity(
            appContext,
            0,
            Intent(appContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setSmallIcon(iconFor(alert.severity))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            // Tag with the alert type so future code can key off the type name rather
            // than display text (e.g. for richer notification templates).
            .addExtras(android.os.Bundle().apply { putString("alert_type", alert.type.name) })
            .build()

        runCatching { notificationManager.notify(ALERT_NOTIF_ID, notification) }

        autoDismissJob = scope.launch {
            delay(AUTO_DISMISS_MS)
            runCatching { notificationManager.cancel(ALERT_NOTIF_ID) }
        }
    }

    /**
     * Maps severity to a notification small icon. All three levels currently share
     * [R.drawable.ic_stat_monitor]; when severity-specific drawables are added, only
     * this function needs to change.
     */
    private fun iconFor(severity: AlertSeverity): Int = when (severity) {
        AlertSeverity.CRITICAL -> R.drawable.ic_stat_monitor
        AlertSeverity.WARNING  -> R.drawable.ic_stat_monitor
        AlertSeverity.INFO     -> R.drawable.ic_stat_monitor
    }

    companion object {
        const val ALERT_NOTIF_ID = 1002
        const val CHANNEL_ID = "camera_alerts"
        private const val AUTO_DISMISS_MS = 8_000L

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Camera alerts",
                // HIGH: shows a heads-up banner while screen is on and a lock-screen
                // notification while the screen is off.
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Transient notifications for camera alert events"
                // Sound and vibration are owned by AndroidAlertFeedback; disabling them here
                // prevents double-firing from the notification channel.
                setSound(null, null)
                enableVibration(false)
                setShowBadge(true)
            }
            mgr.createNotificationChannel(channel)
        }
    }
}
