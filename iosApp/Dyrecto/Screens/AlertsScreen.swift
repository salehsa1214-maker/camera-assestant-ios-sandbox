import SwiftUI
import DyrectoShared

/// Alerts — the premium, end-user alerts screen (port of AlertHistoryScreen.kt). Top to bottom
/// it answers: what needs attention right now (Active), and what already happened (Recent
/// History). A "Configure Alerts" button opens the existing Alert Control Center.
///
/// Pure presentation over the shared `AlertStore` flow — no logic, no storage change. The
/// Active/History split is a UI-only view over the same immutable alert list: "Active" is
/// derived as the newest still-unresolved (WARNING/CRITICAL) alert per category; a newer
/// INFO/recovery alert for a category (e.g. "Reference Match Restored", "Recording Started")
/// means that category is no longer active.
struct AlertsScreen: View {
    @ObservedObject var session: MonitoringSessionIos
    let onOpenControlCenter: () -> Void

    @EnvironmentObject private var services: AppServices

    private enum AlertTab { case active, history }
    /// How many history rows to reveal initially, and per "Load Older" tap.
    private static let historyPage = 6

    @State private var tab: AlertTab = .active
    @State private var visibleHistory = AlertsScreen.historyPage

    var body: some View {
        let alerts = services.alertHistory.alerts
        let active = deriveActiveAlerts(alerts)
        let activeIds = Set(active.map(\.id))
        // Recent History on the Active tab lists past events only; History shows the full log.
        let pastHistory = alerts.filter { !activeIds.contains($0.id) }

        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.cardGap) {
                ScreenHeader(title: "Alerts", subtitle: "Stay informed about important events.")

                HStack(spacing: 12) {
                    PillSegmentedControl(
                        labels: ["Active", "History"],
                        selectedIndex: tab == .active ? 0 : 1
                    ) { index in
                        tab = index == 0 ? .active : .history
                        visibleHistory = Self.historyPage
                    }
                    PillButton(
                        text: "Configure Alerts",
                        systemIcon: "bell",
                        onClick: onOpenControlCenter)
                }

                if alerts.isEmpty {
                    EmptyAlerts()
                } else if tab == .active {
                    ActiveAlertsSection(active: active)
                    if !active.isEmpty { AttentionCard() }
                    HistorySection(
                        title: "Recent History",
                        alerts: pastHistory,
                        visible: visibleHistory,
                        onLoadOlder: { visibleHistory += Self.historyPage },
                        onClear: { services.alertHistory.clear() })
                } else {
                    HistorySection(
                        title: "All History",
                        alerts: alerts,
                        visible: visibleHistory,
                        onLoadOlder: { visibleHistory += Self.historyPage },
                        onClear: { services.alertHistory.clear() })
                }
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.top, 12)
            .padding(.bottom, DyrectoSpacing.bottomInset)
        }
        .background(DyrectoColor.surfaceBase)
        .toolbar(.hidden, for: .navigationBar) // owns its own large header (Android parity)
    }
}

// MARK: - Active alerts

private struct ActiveAlertsSection: View {
    let active: [DyrectoAlert]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Active Alerts (\(active.count))")
                .font(DyrectoType.titleLarge)
                .fontWeight(.bold)
                .foregroundColor(DyrectoColor.textPrimary)
            if active.isEmpty {
                AllCaughtUpCard()
            } else {
                ForEach(active, id: \.id) { ActiveAlertCard(alert: $0) }
            }
        }
    }
}

private struct ActiveAlertCard: View {
    let alert: DyrectoAlert

    var body: some View {
        let accent = statusLevel(for: alert.severity).color
        HStack(alignment: .center) {
            ZStack {
                Circle().fill(accent.opacity(0.18)).frame(width: 52, height: 52)
                Image(systemName: alertIcon(alert))
                    .font(.system(size: 20))
                    .foregroundColor(accent)
            }
            VStack(alignment: .leading, spacing: 0) {
                Text(alert.title)
                    .font(DyrectoType.titleMedium)
                    .fontWeight(.bold)
                    .foregroundColor(DyrectoColor.textPrimary)
                if !alert.message.isEmpty {
                    Text(alert.message)
                        .font(DyrectoType.bodyMedium)
                        .foregroundColor(DyrectoColor.textMuted)
                        .lineLimit(2)
                        .padding(.top, 2)
                }
                Spacer().frame(height: 8)
                SeverityBadge(severity: alert.severity)
            }
            .padding(.leading, 14)
            Spacer()
            VStack(alignment: .trailing) {
                Text(formatTimeShort(alert.timestamp))
                    .font(DyrectoType.labelMedium)
                    .foregroundColor(DyrectoColor.textMuted)
                Spacer().frame(height: 24)
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(accent)
            }
        }
        .padding(16)
        .background(LinearGradient(
            colors: [accent.opacity(0.16), accent.opacity(0.05)],
            startPoint: .leading, endPoint: .trailing))
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .strokeBorder(accent.opacity(0.28), lineWidth: 1))
    }
}

/// The small informational card shown beneath the active alerts.
private struct AttentionCard: View {
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "info.circle")
                .font(.system(size: 17))
                .foregroundColor(DyrectoColor.accent)
            Text("These alerts need your attention right now.")
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(DyrectoColor.accent.opacity(0.07))
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous)
                .strokeBorder(DyrectoColor.accent.opacity(0.18), lineWidth: 1))
    }
}

/// Shown on the Active tab when nothing is currently active but history exists.
private struct AllCaughtUpCard: View {
    var body: some View {
        HStack {
            ZStack {
                Circle().fill(DyrectoColor.statusGood.opacity(0.16)).frame(width: 46, height: 46)
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 20))
                    .foregroundColor(DyrectoColor.statusGood)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text("You're all caught up")
                    .font(DyrectoType.titleMedium)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text("No alerts need your attention right now.")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .padding(.leading, 14)
            Spacer()
        }
        .padding(20)
        .background(DyrectoColor.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous)
                .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
    }
}

// MARK: - History

private struct HistorySection: View {
    let title: String
    let alerts: [DyrectoAlert]
    let visible: Int
    let onLoadOlder: () -> Void
    let onClear: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(title)
                    .font(DyrectoType.titleLarge)
                    .fontWeight(.bold)
                    .foregroundColor(DyrectoColor.textPrimary)
                Spacer()
                if !alerts.isEmpty {
                    Button("Clear History", action: onClear)
                        .font(DyrectoType.labelLarge)
                        .fontWeight(.semibold)
                        .foregroundColor(DyrectoColor.accent)
                }
            }

            if alerts.isEmpty {
                Text("Nothing here yet. Past events will appear in this list.")
                    .font(DyrectoType.bodyMedium)
                    .foregroundColor(DyrectoColor.textMuted)
                    .padding(.leading, 2)
                    .padding(.bottom, 4)
            } else {
                let shown = Array(alerts.prefix(visible))
                VStack(spacing: 0) {
                    ForEach(Array(shown.enumerated()), id: \.element.id) { index, alert in
                        HistoryRow(alert: alert)
                        if index < shown.count - 1 {
                            Rectangle()
                                .fill(DyrectoColor.hairline.opacity(0.6))
                                .frame(height: 1)
                                .padding(.leading, 68)
                        }
                    }
                    if alerts.count > visible {
                        Rectangle()
                            .fill(DyrectoColor.hairline.opacity(0.6))
                            .frame(height: 1)
                            .padding(.leading, 16)
                        Button(action: onLoadOlder) {
                            HStack(spacing: 6) {
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 15, weight: .semibold))
                                Text("Load Older")
                                    .font(DyrectoType.labelLarge)
                                    .fontWeight(.semibold)
                            }
                            .foregroundColor(DyrectoColor.accent)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .background(DyrectoColor.surfaceCard)
                .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: DyrectoRadius.card, style: .continuous)
                        .strokeBorder(DyrectoColor.hairline, lineWidth: 1))
            }
        }
    }
}

private struct HistoryRow: View {
    let alert: DyrectoAlert

    var body: some View {
        let accent = statusLevel(for: alert.severity).color
        HStack {
            ZStack {
                Circle().fill(accent.opacity(0.14)).frame(width: 38, height: 38)
                Image(systemName: alertIcon(alert))
                    .font(.system(size: 15))
                    .foregroundColor(accent)
            }
            VStack(alignment: .leading, spacing: 1) {
                Text(alert.title)
                    .font(DyrectoType.titleSmall)
                    .foregroundColor(DyrectoColor.textPrimary)
                    .lineLimit(1)
                if !alert.message.isEmpty {
                    Text(alert.message)
                        .font(DyrectoType.bodySmall)
                        .foregroundColor(DyrectoColor.textMuted)
                        .lineLimit(1)
                }
            }
            .padding(.leading, 14)
            Spacer()
            Text(formatTimeShort(alert.timestamp))
                .font(DyrectoType.labelMedium)
                .foregroundColor(DyrectoColor.textMuted)
                .padding(.leading, 8)
            Image(systemName: "chevron.right")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(DyrectoColor.textMuted.opacity(0.6))
                .padding(.leading, 4)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

// MARK: - Empty state

private struct EmptyAlerts: View {
    var body: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle().fill(DyrectoColor.surfaceElevated).frame(width: 88, height: 88)
                Image(systemName: "bell")
                    .font(.system(size: 34))
                    .foregroundColor(DyrectoColor.textMuted)
            }
            Text("No alerts yet")
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
            Text("Recording, battery, media, thermal, connection and reference events will appear here.")
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 48)
    }
}

// MARK: - Presentation-only helpers (no logic / no telemetry change)

/// Derives the currently-active alerts from the full session log for display only. Groups by
/// category, keeps the newest alert per category (ids are monotonic), and treats a category as
/// active only while its newest alert is unresolved (WARNING/CRITICAL) — a newer INFO/recovery
/// alert (e.g. "Reference Match Restored", "Recording Started") clears it. Never mutates the store.
func deriveActiveAlerts(_ all: [DyrectoAlert]) -> [DyrectoAlert] {
    var newestPerCategory: [AlertCategory: DyrectoAlert] = [:]
    for alert in all {
        let category = alert.type.category
        if let existing = newestPerCategory[category] {
            if alert.id > existing.id { newestPerCategory[category] = alert }
        } else {
            newestPerCategory[category] = alert
        }
    }
    return newestPerCategory.values
        .filter { $0.severity != .info }
        .sorted { $0.id > $1.id }
}

/// A glanceable icon for an alert, chosen by category (with a couple of direction/recovery
/// cases) — SF equivalents of the Android Material icons.
// interop: Kotlin enum entries export as lowercase static members (AlertType.recordingStopped…).
func alertIcon(_ alert: DyrectoAlert) -> String {
    switch alert.type {
    case .recordingStopped, .highlightRecovered, .shadowRecovered, .referenceRecovered:
        return "checkmark.circle.fill"
    case .referenceExposureDrift:
        return "arrow.up"
    case .shadowClipping:
        return "arrow.down"
    default:
        return categoryIcon(alert.type.category)
    }
}

/// A glanceable icon per alert category (mirrors the Alerts screen's icon vocabulary).
func categoryIcon(_ category: AlertCategory) -> String {
    switch category {
    case .recording: return "record.circle.fill"
    case .battery: return "battery.25"
    case .media: return "sdcard"
    case .thermal: return "thermometer.medium"
    case .connection: return "wifi.slash"
    case .exposure: return "sun.max"
    case .face: return "face.smiling"
    case .reference: return "camera.metering.center.weighted"
    default: return "bell"
    }
}
