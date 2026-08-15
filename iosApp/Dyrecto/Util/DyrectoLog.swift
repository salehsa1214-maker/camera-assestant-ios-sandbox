import Foundation
import os

/// Append-only protocol/event log mirroring the Android `BleLog`: every connection event is
/// recorded, mirrored to the unified logger, and streamed to the Developer screen. Raw protocol
/// lines are only *shown* in Developer Mode, but always recorded (same policy as Android).
final class DyrectoLog: ObservableObject {
    static let shared = DyrectoLog()

    enum Kind: String {
        case info = "INFO", scan = "SCAN", conn = "CONN", bond = "BOND", service = "SERVICE"
        case read = "READ", write = "WRITE", desc = "DESC", notify = "NOTIFY", error = "ERROR"
    }

    struct Entry: Identifiable {
        let id = UUID()
        let timestamp: Date
        let kind: Kind
        let message: String
    }

    @Published private(set) var entries: [Entry] = []

    private let logger = Logger(subsystem: "app.dyrecto.ios", category: "FX3")
    private let queue = DispatchQueue(label: "app.dyrecto.log")
    private static let maxEntries = 4000

    func line(_ kind: Kind, _ message: String) {
        let entry = Entry(timestamp: Date(), kind: kind, message: message)
        switch kind {
        case .error: logger.error("\(kind.rawValue, privacy: .public) \(message, privacy: .public)")
        default: logger.debug("\(kind.rawValue, privacy: .public) \(message, privacy: .public)")
        }
        queue.async {
            DispatchQueue.main.async {
                self.entries.append(entry)
                if self.entries.count > Self.maxEntries {
                    self.entries.removeFirst(self.entries.count - Self.maxEntries)
                }
            }
        }
    }

    func clear() {
        DispatchQueue.main.async { self.entries.removeAll() }
    }
}

func logInfo(_ m: String) { DyrectoLog.shared.line(.info, m) }
func logError(_ m: String) { DyrectoLog.shared.line(.error, m) }
