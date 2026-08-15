import Foundation
import DyrectoShared

/// UserDefaults-backed counterpart of the Android `AlertConfigStore` (DataStore): every
/// `AlertType` resolves to `AlertConfig.default` with persisted overrides applied; enum reads
/// fall back to the default on any corrupt/unknown stored value; a newly added alert type needs
/// no migration. Same key naming scheme as Android for auditability.
final class AlertConfigStoreIos: ObservableObject {

    @Published private(set) var configs: [AlertType: AlertConfig] = [:]
    private let defaults = UserDefaults.standard

    init() { reload() }

    private func key(_ type: AlertType, _ field: String) -> String {
        "alert.\(type.name).\(field)"
    }

    func reload() {
        var out = [AlertType: AlertConfig]()
        for type in SwiftEnums.shared.alertTypes() {
            let def = AlertConfig.companion.default(type: type)
            let enabled = defaults.object(forKey: key(type, "enabled")) as? Bool ?? def.enabled
            let severity = (defaults.string(forKey: key(type, "severity"))
                .flatMap { name in SwiftEnums.shared.alertSeverityOrNull(name: name) })
                ?? def.severity
            let sound = (defaults.object(forKey: key(type, "soundCount")) as? Int)
                .map { SwiftEnums.shared.alertPattern(count: Int32($0)) } ?? def.soundPattern
            let vibe = (defaults.object(forKey: key(type, "vibeCount")) as? Int)
                .map { SwiftEnums.shared.alertPattern(count: Int32($0)) } ?? def.vibrationPattern
            out[type] = AlertConfig(
                alertType: type, enabled: enabled, severity: severity,
                soundPattern: sound, vibrationPattern: vibe)
        }
        DispatchQueue.main.async { self.configs = out }
    }

    func update(_ config: AlertConfig) {
        let type = config.alertType
        defaults.set(config.enabled, forKey: key(type, "enabled"))
        defaults.set(config.severity.name, forKey: key(type, "severity"))
        defaults.set(Int(config.soundPattern.count), forKey: key(type, "soundCount"))
        defaults.set(Int(config.vibrationPattern.count), forKey: key(type, "vibeCount"))
        reload()
    }

    func reset(_ type: AlertType) {
        for field in ["enabled", "severity", "soundCount", "vibeCount"] {
            defaults.removeObject(forKey: key(type, field))
        }
        reload()
    }

    func resetAll() {
        for type in SwiftEnums.shared.alertTypes() { reset(type) }
    }
}

/// UserDefaults-backed counterpart of `VoiceSettingsStore`: voice OFF by default (explicit
/// opt-in); reminder seconds clamped through the shared VoiceCooldowns rule; enum fallbacks on
/// corrupt values. Deliberately NO output-device or volume settings (OS routing wins) — the
/// user's documented Android decision carries over.
final class VoiceSettingsStoreIos: ObservableObject {

    @Published private(set) var settings = VoiceSettings(
        enabled: false, mode: VoiceMode.all, speechRate: VoiceSpeechRate.normal,
        assistantReminderSeconds: 3)

    private let defaults = UserDefaults.standard

    private enum Keys {
        static let enabled = "voice.enabled"
        static let mode = "voice.mode"
        static let rate = "voice.rate"
        static let reminderSec = "voice.assistantReminderSec"
    }

    init() { reload() }

    func reload() {
        let def = VoiceSettings(
            enabled: false, mode: VoiceMode.all, speechRate: VoiceSpeechRate.normal,
            assistantReminderSeconds: 3)
        let enabled = defaults.object(forKey: Keys.enabled) as? Bool ?? def.enabled
        let mode = defaults.string(forKey: Keys.mode)
            .flatMap { name in SwiftEnums.shared.voiceModeOrNull(name: name) } ?? def.mode
        let rate = defaults.string(forKey: Keys.rate)
            .flatMap { name in SwiftEnums.shared.voiceSpeechRateOrNull(name: name) } ?? def.speechRate
        let reminder = (defaults.object(forKey: Keys.reminderSec) as? Int)
            .map { VoiceCooldowns.shared.clampReminderSeconds(seconds: Int32($0)) }
            ?? def.assistantReminderSeconds
        let next = VoiceSettings(
            enabled: enabled, mode: mode, speechRate: rate, assistantReminderSeconds: reminder)
        DispatchQueue.main.async { self.settings = next }
    }

    func update(_ settings: VoiceSettings) {
        defaults.set(settings.enabled, forKey: Keys.enabled)
        defaults.set(settings.mode.name, forKey: Keys.mode)
        defaults.set(settings.speechRate.name, forKey: Keys.rate)
        defaults.set(Int(VoiceCooldowns.shared.clampReminderSeconds(
            seconds: settings.assistantReminderSeconds)), forKey: Keys.reminderSec)
        reload()
    }
}
