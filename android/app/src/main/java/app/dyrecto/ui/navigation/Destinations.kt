package app.dyrecto.ui.navigation

/** Navigation routes + display titles for the app. */
enum class Destination(val route: String, val title: String) {
    DASHBOARD("dashboard", "Dashboard"),
    CAMERA_DETAILS("camera_details", "Camera Details"),
    SETTINGS("settings", "Settings"),
    DISCOVERY("discovery", "Device Discovery"),
    CONNECTION("connection", "Camera Connection"),
    SSH_INFO("ssh_info", "SSH Information"),
    SSH_SESSION("ssh_session", "SSH Session"),
    PTP_SESSION("ptp_session", "PTP/IP Session"),
    DEVICE_INFO("device_info", "Device Information"),
    DIAGNOSTICS("diagnostics", "Camera Diagnostics"),
    DEVELOPER("developer", "Developer"),
    CAMERA_PICKER("camera_picker", "Select Camera"),
    TELEMETRY_EXPLORER("telemetry_explorer", "Telemetry Explorer"),
    CAPABILITY_EXPLORER("capability_explorer", "Capability Explorer"),
    LIVE_VIEW("live_view", "Live View"),
    LIVE_VIEW_HTTP("live_view_http", "HTTP Live View (legacy)"),
    PUSH_LIVE_VIEW_POC("push_live_view_poc", "Live View Diagnostics"),
    ALERT_HISTORY("alert_history", "Alerts"),
    ALERT_SETTINGS("alert_settings", "Alert Control Center"),
    SHOT_REFERENCE("shot_reference", "Storyboard"),
}
