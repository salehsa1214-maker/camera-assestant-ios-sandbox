package app.dyrecto.domain.alerts

/**
 * User-configurable behaviour for a single [AlertType]. The Alert Engine still *detects* events on
 * its own; this controls how a detected alert is *delivered*: whether it fires at all ([enabled]),
 * at what [severity] (a user override of [AlertType.defaultSeverity]), and the [soundPattern] /
 * [vibrationPattern] (0–10 beeps / pulses).
 *
 * Kept a flat data class on purpose: future per-alert settings (history visibility, notification or
 * voice preferences, …) can be added as new fields with defaults, and the persistence layer absorbs
 * them by adding one key + one default value — no schema redesign.
 */
data class AlertConfig(
    val alertType: AlertType,
    val enabled: Boolean,
    val severity: AlertSeverity,
    val soundPattern: AlertPattern,
    val vibrationPattern: AlertPattern,
) {
    companion object {
        /**
         * The shipped default for [type]. Enabled, severity from the type, and sound/vibration
         * counts seeded from severity so out-of-the-box behaviour matches Alert Engine v1:
         * INFO → silent, WARNING → 1, CRITICAL → 3.
         */
        fun default(type: AlertType): AlertConfig {
            val count = when (type.defaultSeverity) {
                AlertSeverity.INFO -> 0
                AlertSeverity.WARNING -> 1
                AlertSeverity.CRITICAL -> 3
            }
            return AlertConfig(
                alertType = type,
                enabled = true,
                severity = type.defaultSeverity,
                soundPattern = AlertPattern(count),
                vibrationPattern = AlertPattern(count),
            )
        }
    }
}
