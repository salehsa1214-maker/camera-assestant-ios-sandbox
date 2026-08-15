package app.dyrecto.connection

import net.schmizz.sshj.common.SecurityUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

/**
 * Replaces Android's stripped-down "BC" provider (com.android.org.bouncycastle,
 * which has no EC) with the full BouncyCastle, so SSHJ's EC key-exchange /
 * ECDSA host-key paths stop throwing:
 *
 *   TransportException: no such algorithm: EC for provider BC
 *
 * Per SSH_HANDSHAKE_BC_FIX.md §5b. Must run ONCE, before any SSHJ call.
 *
 * VERIFIED STACK — carried over from the probe unchanged (package only).
 */
object BcFix {

    @Volatile private var installed = false

    /** Idempotent. Throws IllegalStateException if BC is not the real one. */
    @Synchronized
    fun install() {
        if (installed) return

        // 1) Drop Android's stripped BC and install the full one at top priority.
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)

        // 2) Make SSHJ use BC explicitly (prevents it re-registering the wrong one).
        SecurityUtils.setRegisterBouncyCastle(true)
        SecurityUtils.setSecurityProvider("BC")

        // 3) Verify it is the real provider and fail clearly otherwise.
        val provider = Security.getProvider("BC")
        val className = provider?.javaClass?.name ?: "<null>"
        BleLog.info("[BCFIX] BC provider class = $className")

        if (provider == null || !className.startsWith("org.bouncycastle")) {
            val msg = "[BCFIX] FAILED: BC is '$className', expected org.bouncycastle.* — " +
                "full BouncyCastle did not take over; SSHJ EC will still fail"
            BleLog.error(msg)
            throw IllegalStateException(msg)
        }

        BleLog.info("[BCFIX] full BouncyCastle installed at priority 1 — EC available")
        installed = true
    }
}
