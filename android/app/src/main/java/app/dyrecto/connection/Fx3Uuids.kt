package app.dyrecto.connection

import java.util.UUID

/**
 * UUIDs for the Sony FX3A (ILME-FX3A) BLE provisioning service.
 *
 * The app resolves characteristics by 16-bit prefix match against the 128-bit
 * service 8000CC00-… (O3/C2207e.java). nRF Connect's "CCxx" short labels are
 * therefore the standard 16-bit UUIDs 0000CCxx-0000-1000-8000-00805F9B34FB.
 *
 * VERIFIED STACK — carried over from the probe unchanged (package only).
 * AP-sequence UUIDs added (EE01, CC0E, CCA3, CC08) — no existing UUIDs touched.
 */
object Fx3Uuids {

    /** Primary provisioning service (CC service). */
    val SERVICE: UUID = UUID.fromString("8000cc00-cc00-ffff-ffff-ffffffffffff")

    /** Secondary EE service — carries pairing/RemotePowerOn characteristic EE01. */
    val EE_SERVICE: UUID = UUID.fromString("8000ee00-ee00-ffff-ffff-ffffffffffff")

    /** Standard Client Characteristic Configuration Descriptor (CCCD). */
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private fun cc(shortHex: String): UUID =
        UUID.fromString("0000$shortHex-0000-1000-8000-00805f9b34fb")

    private fun ee(shortHex: String): UUID =
        UUID.fromString("0000$shortHex-0000-1000-8000-00805f9b34fb")

    // ---- Confirmed readable characteristics ----
    val CC0A = cc("cc0a")   // Firmware version  (confirmed: 2.02)
    val CC0B = cc("cc0b")   // Model name        (confirmed: ILME-FX3A)
    val CC0D = cc("cc0d")   // Device info field (GettingCameraDeviceInfoState.java:51)
    val CCA1 = cc("cca1")   // Camera Wi-Fi SSID / NetworkSetInfo (NOTIFY+READ)
    val CCA2 = cc("cca2")   // Camera UUID — GettingCameraUuid (Q3/e.java:28); used in SshInfo capability check
    val CCA7 = cc("cca7")   // Device info field (GettingCameraDeviceInfoState.java:47,114)

    // ---- Sony AP sequence — WRITE + NOTIFY characteristics ----
    val EE01 = ee("ee01")   // Pairing / RemotePowerOn association write + result notify (EE service)
    val EE02 = ee("ee02")   // EE registration-state field (read for diagnostics)
    val EE03 = ee("ee03")   // Pairing-related notify (subscribed permanently after connect)
    val EE04 = ee("ee04")   // EE registration-state / pairing result field (read for diagnostics)
    val CC08 = cc("cc08")   // Wi-Fi Access Point ON/OFF write
    val CC09 = cc("cc09")   // Wi-Fi status notify (ContinuousConnection / AfterPairing + Wi-Fi state)
    val CC0E = cc("cc0e")   // Smartphone Connection result notify (subscribe + receive ack)
    val CCA3 = cc("cca3")   // Smartphone Connection ON/OFF write

    // ---- GetWifiInfo (sequential reads after AP creation) ----
    val CC06 = cc("cc06")   // Wi-Fi SSID
    val CC07 = cc("cc07")   // Wi-Fi password
    val CC0C = cc("cc0c")   // Wi-Fi BSSID

    // ---- GetSshInfo (READ, bond-gated) ----
    val CC17 = cc("cc17")   // BluetoothCameraSshInfo (TLV) — bond-gated

    // ---- Observation-only — unknown purpose, no writes ----
    val CC0F = cc("cc0f")   // Unknown — subscribed; observation only
    val CC10 = cc("cc10")   // Unknown — subscribed; observation only
    val CCA5 = cc("cca5")   // Unknown — observation only
    val CCA9 = cc("cca9")   // Unknown — observation only

    // ---- Notify characteristics to subscribe to ----
    val NOTIFY_TARGETS: List<Pair<String, UUID>> = listOf(
        "CC03" to cc("cc03"),
        "CC09" to CC09,         // ContinuousConnection / AfterPairing keep-alive + Wi-Fi state
        "CC0F" to CC0F,         // Unknown — subscribed
        "CC10" to CC10,         // Unknown — subscribed
        "CC16" to cc("cc16"),
        "CC1B" to cc("cc1b"),
        "CCA1" to CCA1,         // NetworkSetInfo — NOTIFY+READ; may carry camera-state updates post-WiFi
        "CCA5" to CCA5,         // Unknown — observation only; CCCD write skipped gracefully if absent
        "CCA9" to CCA9,         // Unknown — observation only; CCCD write skipped gracefully if absent
    )

    /** Reads attempted in app order, with human labels for the log. */
    val CONFIRMED_READS: List<Pair<String, UUID>> = listOf(
        "CC0A firmware"    to CC0A,
        "CC0B model"       to CC0B,
        "CC0D deviceInfo"  to CC0D,  // GettingCameraDeviceInfoState.java:51
        "CCA1 cameraSSID"  to CCA1,
        "CCA2 cameraUUID"  to CCA2,  // GettingCameraUuid — feeds SshInfo capability check
        "CCA7 deviceInfo"  to CCA7,  // GettingCameraDeviceInfoState.java:47,114
    )

    /** Map any UUID back to a short label for logging. */
    fun label(uuid: UUID): String {
        val s = uuid.toString()
        return when {
            s.startsWith("0000cc", ignoreCase = true) -> "CC" + s.substring(6, 8).uppercase()
            s.startsWith("0000ee", ignoreCase = true) -> "EE" + s.substring(6, 8).uppercase()
            else -> s
        }
    }
}
