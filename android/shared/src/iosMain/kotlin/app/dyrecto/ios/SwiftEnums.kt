package app.dyrecto.ios

import app.dyrecto.domain.alerts.AlertPattern
import app.dyrecto.domain.alerts.AlertSeverity
import app.dyrecto.domain.alerts.AlertType
import app.dyrecto.liveview.voice.VoiceMode
import app.dyrecto.liveview.voice.VoiceSpeechRate

/**
 * Swift-facing enum/name bridges: Kotlin enum `entries`/`valueOf` don't export cleanly to
 * ObjC/Swift, so the iOS stores resolve names and enumerate types through these (with the same
 * fall-back-to-default-on-corrupt-value semantics the Android DataStore readers use).
 */
object SwiftEnums {
    fun alertTypes(): List<AlertType> = AlertType.entries

    fun alertSeverityOrNull(name: String): AlertSeverity? =
        AlertSeverity.entries.firstOrNull { it.name == name }

    fun voiceModeOrNull(name: String): VoiceMode? =
        VoiceMode.entries.firstOrNull { it.name == name }

    fun voiceSpeechRateOrNull(name: String): VoiceSpeechRate? =
        VoiceSpeechRate.entries.firstOrNull { it.name == name }

    /** AlertPattern.of clamps count/interval into range (corrupt data degrades gracefully). */
    fun alertPattern(count: Int): AlertPattern = AlertPattern.of(count)
}
