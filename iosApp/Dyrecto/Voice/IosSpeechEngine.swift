import AVFoundation
import Foundation
import DyrectoShared

/// AVSpeechSynthesizer implementation of the shared `VoiceSpeechEngine` seam — the iOS
/// counterpart of `AndroidTtsSpeechEngine`, honoring the contracts VoiceScheduler depends on:
///
///  - one utterance at a time; the finished listener fires exactly once per COMPLETED utterance;
///  - `stop()` cancels WITHOUT firing the finished listener (utterance-identity guarded);
///  - engine-not-ready utterances are dropped (never queued behind the scheduler's back);
///  - audio routing follows the OS (a connected Bluetooth device wins automatically) and volume
///    follows the media channel — deliberately NO settings for either (Android parity).
///
/// The audio session uses `.playback` + `.duckOthers` with `.mixWithOthers`, and — together with
/// the `audio` background mode — keeps spoken guidance working with the screen locked (the iOS
/// stand-in for the Android foreground service, to be validated on hardware).
final class IosSpeechEngine: NSObject, VoiceSpeechEngine {

    private let synthesizer = AVSpeechSynthesizer()
    private var rate: Float = 1.0
    private var onFinished: (() -> Void)?
    /// Identity of the utterance whose completion may fire the listener; stop() clears it so a
    /// cancelled utterance can never fire (the Android currentUtteranceId guard).
    private var currentUtterance: AVSpeechUtterance?
    private let lock = NSLock()

    override init() {
        super.init()
        synthesizer.delegate = self
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .voicePrompt,
                                    options: [.duckOthers, .mixWithOthers])
            try session.setActive(true)
        } catch {
            logError("audio session activation failed: \(error) — voice may be silent until retried")
        }
    }

    // MARK: VoiceSpeechEngine

    func speak(event: VoiceEvent) {
        lock.lock(); defer { lock.unlock() }
        let utterance = AVSpeechUtterance(string: event.text)
        // AVSpeech's default rate is AVSpeechUtteranceDefaultSpeechRate (~0.5 of its 0..1 range);
        // map the shared multiplier onto it so NORMAL sounds like the platform default.
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * rate
        utterance.prefersAssistiveTechnologySettings = false
        currentUtterance = utterance
        synthesizer.speak(utterance)
    }

    func stop() {
        lock.lock()
        currentUtterance = nil // guard: the cancelled utterance must NOT fire finished
        lock.unlock()
        synthesizer.stopSpeaking(at: .immediate)
    }

    func setSpeechRate(rate: VoiceSpeechRate) {
        lock.lock(); defer { lock.unlock() }
        self.rate = rate.ttsRate
    }

    func setOnUtteranceFinished(listener: @escaping () -> Void) {
        lock.lock(); defer { lock.unlock() }
        onFinished = listener
    }

    func shutdown() {
        stop()
        lock.lock()
        onFinished = nil
        lock.unlock()
    }
}

extension IosSpeechEngine: AVSpeechSynthesizerDelegate {
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer,
                           didFinish utterance: AVSpeechUtterance) {
        finished(utterance)
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer,
                           didCancel utterance: AVSpeechUtterance) {
        // Cancellation comes from stop(); the identity guard below already blocks the callback,
        // but keep the path explicit: cancelled utterances never fire finished.
        lock.lock()
        if currentUtterance === utterance { currentUtterance = nil }
        lock.unlock()
    }

    private func finished(_ utterance: AVSpeechUtterance) {
        lock.lock()
        let shouldFire = currentUtterance === utterance
        if shouldFire { currentUtterance = nil }
        let listener = onFinished
        lock.unlock()
        if shouldFire { listener?() }
    }
}
