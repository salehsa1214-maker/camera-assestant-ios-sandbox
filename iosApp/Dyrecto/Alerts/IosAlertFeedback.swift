import AudioToolbox
import Foundation
import UIKit
import DyrectoShared

/// iOS implementation of the shared `AlertFeedback` seam — the counterpart of
/// `AndroidAlertFeedback` (in-app sound + haptic):
///
///  - severity selects tone character/intensity, patterns drive repeat count + interval;
///  - non-blocking; a spam guard drops calls that overlap an in-flight pattern;
///  - every hardware interaction is failure-tolerant (system sound / haptics can't crash us);
///  - `playing` mirrors the guard so the Settings test button can disable itself.
///
/// Tones use system sound services (no bundled assets — Android uses ToneGenerator similarly);
/// haptics use UINotificationFeedbackGenerator mapped by severity, pulsed per the pattern.
final class IosAlertFeedback: AlertFeedback {

    private let playingFlow = MutableStateFlowBox(initial: false)
    var playing: Kotlinx_coroutines_coreStateFlow { playingFlow.flow }

    private let queue = DispatchQueue(label: "app.dyrecto.alertfeedback")
    private var isPlaying = false
    private static let pulseMs: Int64 = 120 // Android PULSE_MS parity

    func deliver(severity: AlertSeverity, sound: AlertPattern, vibration: AlertPattern) {
        queue.async {
            // Spam guard: ignore overlapping triggers while a pattern is still playing.
            guard !self.isPlaying else { return }
            self.isPlaying = true
            self.playingFlow.set(true)

            let toneId = Self.toneFor(severity)
            let toneMs = Self.toneDurationFor(severity)

            // Haptic pulses on the main thread (UIKit requirement), same count/interval shape
            // as the Android waveform.
            DispatchQueue.main.async {
                let generator = UINotificationFeedbackGenerator()
                generator.prepare()
                let feedback = Self.hapticFor(severity)
                for i in 0..<Int(vibration.count) {
                    let delay = Double(i) * (Double(Self.pulseMs + vibration.intervalMs) / 1000.0)
                    DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                        generator.notificationOccurred(feedback)
                    }
                }
            }

            // Tone pulses.
            for i in 0..<Int(sound.count) {
                AudioServicesPlaySystemSound(toneId)
                if i < Int(sound.count) - 1 {
                    Thread.sleep(forTimeInterval: Double(Int64(toneMs) + sound.intervalMs) / 1000.0)
                }
            }

            // Let the final tone/vibration finish before clearing the guard (Android parity).
            let vibrationTail = vibration.count > 0
                ? Int64(vibration.count) * Self.pulseMs + max(Int64(vibration.count) - 1, 0) * vibration.intervalMs
                : 0
            let tail = max(sound.count > 0 ? Int64(toneMs) : 0, vibrationTail)
            Thread.sleep(forTimeInterval: Double(tail) / 1000.0)

            self.isPlaying = false
            self.playingFlow.set(false)
        }
    }

    private static func toneFor(_ severity: AlertSeverity) -> SystemSoundID {
        switch severity {
        case .info: return 1057      // short tick
        case .warning: return 1005   // alarm-ish beep
        case .critical: return 1005  // strongest available non-asset tone; haptic carries urgency
        default: return 1057
        }
    }

    private static func toneDurationFor(_ severity: AlertSeverity) -> Int {
        switch severity {
        case .info: return 150
        case .warning: return 200
        case .critical: return 400
        default: return 150
        }
    }

    private static func hapticFor(_ severity: AlertSeverity) -> UINotificationFeedbackGenerator.FeedbackType {
        switch severity {
        case .info: return .success
        case .warning: return .warning
        case .critical: return .error
        default: return .success
        }
    }
}

/// Tiny Swift-side box that owns a Kotlin MutableStateFlow<Boolean> for interop-friendly
/// publication (constructed via the shared AlertStore-style factory pattern is unnecessary —
/// kotlinx exposes the constructor through the exported coroutines API).
final class MutableStateFlowBox {
    let flow: Kotlinx_coroutines_coreMutableStateFlow

    init(initial: Bool) {
        flow = StateFlowFactoryKt.mutableStateFlow(initial: KotlinBoolean(bool: initial))
    }

    func set(_ value: Bool) {
        flow.setValue(KotlinBoolean(bool: value))
    }
}
