package app.dyrecto

import android.app.Application
import app.dyrecto.connection.BcFix
import app.dyrecto.connection.BleLog
import app.dyrecto.debug.LogStore
import app.dyrecto.alerts.AlertNotificationDelivery
import app.dyrecto.billing.EntitlementProvider
import app.dyrecto.liveview.AnalysisLutBootstrap
import app.dyrecto.service.CameraMonitorService
import app.dyrecto.service.MonitoringSessionProvider

/**
 * Product Application entry point.
 *
 * Order matters: install the FULL BouncyCastle BEFORE any SSHJ call can occur
 * (exactly as the verified stack requires), then attach the developer log store
 * so every protocol line is captured from the very first startup event.
 */
class DyrectoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LogStore.attach()
        runCatching { BcFix.install() }
            .onFailure { BleLog.error("BcFix at startup failed: ${it.message}") }
        // Process-scoped session ownership + the foreground-service notification channel.
        MonitoringSessionProvider.init(this)
        // Dyrecto Premium entitlement (Play Billing, offline cache, no trial).
        EntitlementProvider.init(this)
        CameraMonitorService.createNotificationChannel(this)
        AlertNotificationDelivery.createChannel(this)
        // Phase 8: load the Sony S-Log3 monitoring LUT for the Analysis Color Transform.
        AnalysisLutBootstrap.installSLog3Lut(this)
    }
}
