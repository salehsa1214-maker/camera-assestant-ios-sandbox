package app.dyrecto.domain.alerts

/**
 * Groups [AlertType]s into the sections shown in the Alert Control Center. The screen is generated
 * by iterating these categories and filtering alert types by [AlertType.category], so a new alert
 * type appears automatically under the right heading with no UI changes.
 */
enum class AlertCategory(val title: String) {
    RECORDING("Recording"),
    BATTERY("Battery"),
    MEDIA("Media"),
    THERMAL("Thermal"),
    CONNECTION("Connection"),
    EXPOSURE("Exposure"),
    FACE("Face"),
    REFERENCE("Reference"),
}
