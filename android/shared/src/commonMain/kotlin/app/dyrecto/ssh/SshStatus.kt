package app.dyrecto.ssh

/** Platform-agnostic view of the SSH provisioning info (CC17) + live session. */

enum class SshEnabled { UNKNOWN, OFF, ON }

data class SshStatus(
    // ---- From CC17 (provisioning info) ----
    val sshState: SshEnabled = SshEnabled.UNKNOWN,
    val sshId: String? = null,
    /** Sensitive — never shown unless Developer "reveal" is enabled. */
    val sshPassword: String? = null,
    val fingerprint: String? = null,
    val cameraIp: String? = null,
    val notes: List<String> = emptyList(),

    // ---- From the live SSH session ----
    val connected: Boolean = false,
    val authenticated: Boolean = false,
    val keyboardInteractiveSupported: Boolean = false,
    val connectMillis: Long? = null,
    val authMillis: Long? = null,
    val lastConnectedAt: Long? = null,
    val error: String? = null,
) {
    val hasCredentials: Boolean get() = !sshId.isNullOrEmpty() && !sshPassword.isNullOrEmpty()
}
