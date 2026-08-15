import Foundation
import UserNotifications
import DyrectoShared

/// iOS counterpart of `AlertNotificationDelivery`: one shared notification identifier, so each
/// new alert REPLACES the previous one (the shade never accumulates; Alert History in-app is the
/// record). Auto-dismiss after the same window; a new alert cancels the pending dismissal first
/// so it always gets a fresh full window. Sound is disabled on the notification itself —
/// IosAlertFeedback owns tones/haptics (one alert system, one sound source).
///
/// Notification permission is cosmetic-only and never gates alerting (Android
/// POST_NOTIFICATIONS parity): if denied, in-app feedback still fires.
final class IosNotificationDelivery {

    private static let alertIdentifier = "app.dyrecto.alert"
    private static let autoDismissSeconds: TimeInterval = 12
    private var dismissTimer: DispatchSourceTimer?
    private let queue = DispatchQueue(label: "app.dyrecto.notifications")

    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge]) { granted, _ in
            logInfo("notification permission granted=\(granted) (cosmetic-only, never gates alerts)")
        }
    }

    func deliver(alert: Alert) {
        queue.async {
            self.dismissTimer?.cancel()
            self.dismissTimer = nil

            let content = UNMutableNotificationContent()
            content.title = alert.title
            content.body = alert.message
            content.userInfo = ["alert_type": alert.type.name]
            content.sound = nil // IosAlertFeedback owns audio

            let request = UNNotificationRequest(
                identifier: Self.alertIdentifier, content: content, trigger: nil)
            let center = UNUserNotificationCenter.current()
            center.removeDeliveredNotifications(withIdentifiers: [Self.alertIdentifier])
            center.add(request) { error in
                if let error { logError("notification post failed: \(error)") }
            }

            let timer = DispatchSource.makeTimerSource(queue: self.queue)
            timer.schedule(deadline: .now() + Self.autoDismissSeconds)
            timer.setEventHandler {
                center.removeDeliveredNotifications(withIdentifiers: [Self.alertIdentifier])
            }
            timer.resume()
            self.dismissTimer = timer
        }
    }
}
