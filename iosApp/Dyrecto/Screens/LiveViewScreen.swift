import SwiftUI
import DyrectoShared

/// The user-facing full-screen Live View — an immersive operating mode (port of
/// LiveViewScreen.kt). A passive subscriber of the persistent frame stream owned by the
/// monitoring session, rendered edge-to-edge on a black stage with floating camera-monitor
/// overlays over scrims: REC + battery on top, exposure glance chips on the bottom, plus the
/// analysis overlays (zebra, histogram), the Phase-13 assistant instruction card and the
/// storyboard Current Match chip. Start/Stop drives the push live-view stream.
struct LiveViewScreen: View {
    @ObservedObject var session: MonitoringSessionIos

    @State private var showZebra = false
    @State private var showHistogram = false
    @State private var showGuidance = true
    @State private var showControls = true

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if let frame = session.frame {
                GeometryReader { geo in
                    ZStack {
                        Image(uiImage: frame)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: geo.size.width, height: geo.size.height)
                        if showZebra {
                            ZebraOverlay(session: session)
                                .aspectRatio(frame.size, contentMode: .fit)
                                .frame(width: geo.size.width, height: geo.size.height)
                                .allowsHitTesting(false)
                        }
                    }
                }
                .ignoresSafeArea()
                Scrims()
                OverlayLayer(
                    session: session,
                    showHistogram: showHistogram,
                    showGuidance: showGuidance)
            } else {
                StartingState(active: session.frameStreamActive)
            }

            if showControls {
                VStack {
                    Spacer()
                    ControlBar(
                        session: session,
                        showZebra: $showZebra,
                        showHistogram: $showHistogram,
                        showGuidance: $showGuidance)
                }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture { withAnimation { showControls.toggle() } }
        .toolbar(.hidden, for: .navigationBar) // immersive: no chrome (Android parity)
        .toolbar(.hidden, for: .tabBar)
        .statusBarHidden(true)
    }
}

// MARK: - Scrims

/// Top & bottom gradient scrims so white overlay text stays legible over any frame.
private struct Scrims: View {
    var body: some View {
        VStack(spacing: 0) {
            LinearGradient(
                colors: [Color.black.opacity(0.55), .clear],
                startPoint: .top, endPoint: .bottom)
                .frame(height: 120)
            Spacer()
            LinearGradient(
                colors: [.clear, Color.black.opacity(0.6)],
                startPoint: .top, endPoint: .bottom)
                .frame(height: 140)
        }
        .ignoresSafeArea()
        .allowsHitTesting(false)
    }
}

// MARK: - Floating overlay

private struct OverlayLayer: View {
    @ObservedObject var session: MonitoringSessionIos
    let showHistogram: Bool
    let showGuidance: Bool

    var body: some View {
        let t = session.state.telemetry
        let recValue = telemetryDisplay(t, TelemetryCode.movieRec).lowercased()
        let recording = recValue.contains("rec") && !recValue.contains("standby")
        let recTime = telemetryDisplay(t, TelemetryCode.recTime)
        let timecode = recTime == "—" ? nil : recTime

        VStack(alignment: .leading, spacing: 0) {
            // Top row: REC badge (only while recording) · status chips · battery glance.
            HStack(alignment: .top) {
                if recording {
                    RecBadge(timecode: timecode)
                }
                Spacer()
                StatusChipsRow(session: session)
                Spacer()
                GlanceChip(label: "Battery", value: telemetryDisplay(t, TelemetryCode.battery))
            }

            if let matchChip = currentMatchLabel {
                HStack {
                    Spacer()
                    CurrentMatchChip(text: matchChip, score: session.referenceMatch.overallScore)
                    Spacer()
                }
                .padding(.top, 10)
            }

            Spacer()

            HStack(alignment: .bottom) {
                if showGuidance {
                    InstructionOverlayCard(match: session.referenceMatch)
                }
                Spacer()
                if showHistogram {
                    HistogramPanel(histogram: session.visionContext.histogram)
                }
            }
            .padding(.bottom, 10)

            // Bottom: exposure glance bar.
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    GlanceChip(label: "ISO", value: telemetryDisplay(t, TelemetryCode.iso))
                    GlanceChip(label: "Shutter", value: telemetryDisplay(t, TelemetryCode.shutter))
                    GlanceChip(label: "Aperture", value: telemetryDisplay(t, TelemetryCode.fNumber))
                    GlanceChip(label: "WB", value: telemetryDisplay(t, TelemetryCode.whiteBalance))
                }
            }
        }
        .padding(16)
        .padding(.bottom, 60) // clear the control bar
    }

    /// "Shot N" for the storyboard shot the live frame currently matches best.
    private var currentMatchLabel: String? {
        let match = session.referenceMatch
        guard match.active, let id = match.referenceId,
              let profiles = session.referenceState.session?.profiles,
              let index = profiles.firstIndex(where: { $0.id == id })
        else { return nil }
        return "Shot \(index + 1)"
    }
}

/// fps / frames / exposure verdict chips (camera-monitor status row).
private struct StatusChipsRow: View {
    @ObservedObject var session: MonitoringSessionIos

    var body: some View {
        HStack(spacing: 8) {
            GlanceChip(label: "FPS", value: String(format: "%.1f", session.displayedFps))
            GlanceChip(label: "Frames", value: "\(session.framesDisplayed)")
            if let exposure = session.visionContext.exposure {
                GlanceChip(label: "Exposure", value: exposureVerdictLabel(exposure.exposureState))
            }
        }
    }
}

private func exposureVerdictLabel(_ verdict: ExposureVerdict) -> String {
    // interop: enum entries export lowercase (ExposureVerdict.normal, …); name gives "NORMAL".
    verdict.name.replacingOccurrences(of: "_", with: " ").capitalized
}

/// The storyboard Current Match chip (accent-tinted, score + shot label).
private struct CurrentMatchChip: View {
    let text: String
    let score: Float

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "play.fill")
                .font(.system(size: 10))
            Text("\(text) · \(Int(score * 100))%")
                .font(DyrectoType.labelMedium)
                .fontWeight(.semibold)
        }
        .foregroundColor(DyrectoColor.accent)
        .padding(.horizontal, 10)
        .padding(.vertical, 5)
        .background(Color.black.opacity(0.45))
        .clipShape(Capsule())
        .overlay(Capsule().strokeBorder(DyrectoColor.accent.opacity(0.5), lineWidth: 1))
    }
}

// MARK: - Assistant instruction overlay (Phase 13)

/// Selected instruction + progress % + confidence bucket, floating over the image.
private struct InstructionOverlayCard: View {
    let match: ReferenceMatchResult

    var body: some View {
        if let instruction = InstructionSelector.shared.select(match: match) {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 8) {
                    Image(systemName: actionIcon(instruction.action))
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(DyrectoColor.accent)
                    Text(actionHeadline(instruction.action))
                        .font(DyrectoType.titleSmall)
                        .foregroundColor(.white)
                }
                if !instruction.message.isEmpty {
                    Text(instruction.message)
                        .font(DyrectoType.bodySmall)
                        .foregroundColor(.white.opacity(0.85))
                        .lineLimit(2)
                }
                ThinBar(instruction.progress, color: DyrectoColor.accent)
                    .frame(width: 180)
                Text("\(InstructionFormatter.shared.progressPercent(instruction: instruction)) matched · " +
                     "\(InstructionFormatter.shared.confidenceLabel(instruction: instruction)) confidence")
                    .font(DyrectoType.labelSmall)
                    .foregroundColor(.white.opacity(0.65))
            }
            .padding(12)
            .background(Color.black.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        } else if match.active {
            Text("Hold this framing.")
                .font(DyrectoType.titleSmall)
                .foregroundColor(.white)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.black.opacity(0.55))
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
    }
}

// MARK: - Zebra overlay

/// Diagonal-stripe highlight warning. Preferred source is a mask image the renderer builds from
/// `ZebraResult.mask` off the UI thread (per-pixel work never crosses the Swift↔Kotlin bridge).
/// Until the renderer publishes one, falls back to a coverage badge only — never fake stripes
/// over unknown regions.
private struct ZebraOverlay: View {
    @ObservedObject var session: MonitoringSessionIos

    var body: some View {
        ZStack(alignment: .topLeading) {
            // interop: MonitoringSessionIos is expected to publish `zebraOverlay: UIImage?`
            // (stripes pre-rendered from ZebraResult.mask at the analysis resolution).
            if let overlay = session.zebraOverlay {
                Image(uiImage: overlay)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
            }
            if let zebra = session.visionContext.zebra {
                Text(String(format: "ZEBRA %@ IRE · %.1f%%", zebra.spec.label, zebra.coveragePercentage))
                    .font(DyrectoType.labelSmall)
                    .fontWeight(.semibold)
                    .foregroundColor(DyrectoColor.statusWarn)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.black.opacity(0.45))
                    .clipShape(Capsule())
                    .padding(10)
            }
        }
    }
}

// MARK: - Histogram panel

/// 256-bin luma histogram (HistogramResult) rendered as a compact Canvas bar plot.
private struct HistogramPanel: View {
    let histogram: HistogramResult?

    var body: some View {
        if let histogram {
            let bins = binValues(histogram)
            Canvas { context, size in
                guard let maxValue = bins.max(), maxValue > 0 else { return }
                let barWidth = size.width / CGFloat(bins.count)
                for (i, value) in bins.enumerated() {
                    let h = size.height * CGFloat(value) / CGFloat(maxValue)
                    let rect = CGRect(
                        x: CGFloat(i) * barWidth,
                        y: size.height - h,
                        width: max(barWidth - 0.5, 0.5),
                        height: h)
                    context.fill(Path(rect), with: .color(.white.opacity(0.85)))
                }
            }
            .frame(width: 160, height: 72)
            .padding(8)
            .background(Color.black.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
            .overlay(alignment: .topTrailing) {
                Text(String(format: "μ %.0f", histogram.mean))
                    .font(DyrectoType.monoSmall)
                    .foregroundColor(.white.opacity(0.65))
                    .padding(6)
            }
        }
    }

    /// Downsample the 256 bins to 128 bars (sums of pairs) — enough for a glance panel.
    private func binValues(_ histogram: HistogramResult) -> [Int32] {
        // interop: Kotlin IntArray → KotlinIntArray with get(index:); 128 calls per update is fine
        // (per-frame per-PIXEL loops are what the bridge rules forbid).
        var out = [Int32](repeating: 0, count: 128)
        let count = Int(histogram.bins.size)
        for i in 0..<min(count, 256) {
            out[i / 2] += histogram.bins.get(index: Int32(i))
        }
        return out
    }
}

// MARK: - Controls

private struct ControlBar: View {
    @ObservedObject var session: MonitoringSessionIos
    @Binding var showZebra: Bool
    @Binding var showHistogram: Bool
    @Binding var showGuidance: Bool

    var body: some View {
        HStack(spacing: 10) {
            OverlayToggle(label: "Zebra", systemIcon: "circle.grid.cross", isOn: $showZebra)
            OverlayToggle(label: "Histogram", systemIcon: "chart.bar", isOn: $showHistogram)
            OverlayToggle(label: "Guidance", systemIcon: "wand.and.stars", isOn: $showGuidance)
            Spacer()
            if session.frameStreamActive {
                Button(action: { session.stopPushLiveView() }) {
                    Label("Stop", systemImage: "stop.fill")
                        .font(DyrectoType.labelLarge)
                        .foregroundColor(DyrectoColor.error)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 9)
                        .background(DyrectoColor.error.opacity(0.16))
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            } else {
                Button(action: { session.startPushLiveView() }) {
                    Label("Start", systemImage: "play.fill")
                        .font(DyrectoType.labelLarge)
                        .foregroundColor(DyrectoColor.onPrimary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 9)
                        .background(DyrectoColor.accent)
                        .clipShape(Capsule())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color.black.opacity(0.45))
    }
}

private struct OverlayToggle: View {
    let label: String
    let systemIcon: String
    @Binding var isOn: Bool

    var body: some View {
        Button(action: { isOn.toggle() }) {
            VStack(spacing: 3) {
                Image(systemName: systemIcon)
                    .font(.system(size: 16, weight: .medium))
                Text(label)
                    .font(DyrectoType.labelSmall)
            }
            .foregroundColor(isOn ? DyrectoColor.accent : .white.opacity(0.65))
            .frame(width: 62)
            .padding(.vertical, 6)
            .background(isOn ? DyrectoColor.accent.opacity(0.16) : .clear)
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Pre-frame state

private struct StartingState: View {
    let active: Bool

    var body: some View {
        VStack(spacing: 16) {
            if active {
                ProgressView().tint(.white)
                Text("Starting Live View…")
                    .font(DyrectoType.bodyMedium)
                    .foregroundColor(.white)
            } else {
                Text("Live view stopped")
                    .font(DyrectoType.bodyMedium)
                    .foregroundColor(.white.opacity(0.7))
            }
        }
    }
}
