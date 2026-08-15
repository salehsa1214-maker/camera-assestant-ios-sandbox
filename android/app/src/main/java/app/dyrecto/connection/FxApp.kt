package app.dyrecto.connection

import android.app.Application

/**
 * Installs the full BouncyCastle provider before any SSHJ call can occur.
 *
 * VERIFIED STACK — carried over from the probe unchanged (package only).
 * Registered as android:name in the manifest; BC fix MUST run first.
 */
class FxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        runCatching { BcFix.install() }
            .onFailure { BleLog.error("BcFix at startup failed: ${it.message}") }
    }
}
