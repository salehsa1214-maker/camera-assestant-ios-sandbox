package app.dyrecto.domain.alerts

/**
 * How urgently an alert must reach the user. Drives visual prominence, sound, and haptic
 * behaviour. Platform-agnostic — the delivery layer (sound/haptic/notifications) maps each
 * level to a concrete behaviour, so this enum stays free of Android types.
 */
enum class AlertSeverity { INFO, WARNING, CRITICAL }
