import PhotosUI
import SwiftUI
import DyrectoShared

/// Storyboard (Phase 9/10/15) — full port of ShotReferenceScreen.kt: teach the assistant the
/// shots you want, then let it protect them. The user manages a grid of reference images; while
/// monitoring, the assistant compares each live frame against every shot, picks the best match
/// (Current Match), and tracks how many planned shots are done. Presentation only — it drives
/// the same session state and the same start/stop/clear intents.
struct StoryboardScreen: View {
    @ObservedObject var session: MonitoringSessionIos

    @State private var showHelp = false
    @State private var infoDialog: InfoDialogContent? = nil
    // Add supports selecting MULTIPLE images at once; each becomes a new storyboard shot.
    @State private var addSelection: [PhotosPickerItem] = []
    // Replace is a single pick targeting a specific shot (remembered across the picker round-trip).
    @State private var replaceTarget: String? = nil
    @State private var replaceSelection: PhotosPickerItem? = nil
    @State private var showReplacePicker = false

    var body: some View {
        let referenceState = session.referenceState
        let referenceMatch = session.referenceMatch
        let profile = referenceState.profile
        let profiles = referenceState.session?.profiles ?? []
        let currentMatchId = referenceMatch.active ? referenceMatch.referenceId : nil

        ScrollView {
            VStack(alignment: .leading, spacing: DyrectoSpacing.cardGap) {
                ScreenHeader(title: "Storyboard", subtitle: "Organize the shots you want to capture.") {
                    HeaderIconButton(systemIcon: "questionmark.circle", onClick: { showHelp = true })
                }

                if referenceState.monitoringRequested {
                    LiveMatchCard(match: referenceMatch, frameStreamActive: session.frameStreamActive)
                }

                ReferencesCard(
                    profiles: profiles,
                    analyzing: referenceState.analyzing,
                    error: referenceState.error,
                    addSelection: $addSelection,
                    onReplace: { id in
                        replaceTarget = id
                        showReplacePicker = true
                    },
                    onRemove: { session.removeReference(id: $0) },
                    onClearAll: { session.clearReference() })

                if !profiles.isEmpty {
                    ProgressCard(
                        state: referenceState,
                        currentMatchId: currentMatchId,
                        onSetRule: { session.setCompletionRule($0) },
                        onResetProgress: { session.resetStoryboardProgress() },
                        onInfo: { infoDialog = $0 })
                }

                WhatToMonitorCard(session: session)

                MatchingStrictnessCard(session: session)

                PrimaryActionSection(
                    monitoring: referenceState.monitoringRequested,
                    hasProfile: !profiles.isEmpty,
                    onStart: { session.startReferenceMonitoring() },
                    onStop: { session.stopReferenceMonitoring() })

                if let profile {
                    ReferenceAnalysisCard(creative: profile.creativeScene)
                }
            }
            .padding(.horizontal, DyrectoSpacing.screenHorizontal)
            .padding(.top, 12)
            .padding(.bottom, DyrectoSpacing.bottomInset)
        }
        .background(DyrectoColor.surfaceBase)
        .toolbar(.hidden, for: .navigationBar) // owns its own large header (Android parity)
        .infoDialog($infoDialog)
        .alert("Storyboard", isPresented: $showHelp) {
            Button("Got it", role: .cancel) {}
        } message: {
            Text("1. Add the shots you want to capture.\n\n" +
                 "2. The assistant analyzes each on-device.\n\n" +
                 "3. Choose what to monitor and how strict the match should be.\n\n" +
                 "4. Start monitoring — while the camera streams, the assistant picks the best " +
                 "matching shot for each frame and tracks your progress until every planned " +
                 "shot is captured.")
        }
        .photosPicker(
            isPresented: $showReplacePicker,
            selection: $replaceSelection,
            matching: .images)
        .onChange(of: addSelection) { items in
            guard !items.isEmpty else { return }
            addSelection = []
            Task {
                for item in items {
                    if let image = await loadImage(item) {
                        session.addReferenceImage(image, name: "Shot")
                    }
                }
            }
        }
        .onChange(of: replaceSelection) { item in
            guard let item else { return }
            let id = replaceTarget
            replaceTarget = nil
            replaceSelection = nil
            Task {
                if let id, let image = await loadImage(item) {
                    session.replaceReference(id: id, image: image, name: "Shot")
                }
            }
        }
    }

    private func loadImage(_ item: PhotosPickerItem) async -> UIImage? {
        guard let data = try? await item.loadTransferable(type: Data.self) else { return nil }
        return UIImage(data: data)
    }
}

// MARK: - Live match (monitoring active)

private struct LiveMatchCard: View {
    let match: ReferenceMatchResult
    let frameStreamActive: Bool

    var body: some View {
        let accent = DyrectoColor.accent
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 8) {
                Circle()
                    .fill(frameStreamActive ? DyrectoColor.statusGood : DyrectoColor.textMuted)
                    .frame(width: 10, height: 10)
                Text(frameStreamActive ? "MONITORING LIVE" : "WAITING FOR CAMERA")
                    .font(DyrectoType.labelMedium)
                    .kerning(DyrectoType.labelMediumKerning)
                    .fontWeight(.semibold)
                    .foregroundColor(frameStreamActive ? DyrectoColor.statusGood : DyrectoColor.textMuted)
            }

            if match.active {
                Spacer().frame(height: 16)
                Text("\(Int(match.overallScore * 100))%")
                    .font(DyrectoType.displaySmall)
                    .foregroundColor(DyrectoColor.textPrimary)
                Text("Best match with your storyboard")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
                Spacer().frame(height: 14)
                ThinBar(match.overallScore, color: accent)
                Spacer().frame(height: 12)
                let assistant = InstructionSelector.shared.select(match: match)
                Text(assistant?.message ?? "Hold this framing.")
                    .font(DyrectoType.titleSmall)
                    .foregroundColor(DyrectoColor.textPrimary)
            } else {
                Spacer().frame(height: 8)
                Text("Start streaming from the camera and the assistant will compare each frame " +
                     "with your storyboard.")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(LinearGradient(
            colors: [accent.opacity(0.10), DyrectoColor.surfaceCard],
            startPoint: .top, endPoint: .bottom))
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.premiumCard, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.premiumCard, style: .continuous)
                .strokeBorder(accent.opacity(0.20), lineWidth: 1))
    }
}

// MARK: - Card 1 · References (multi-image grid)

private struct ReferencesCard: View {
    let profiles: [ReferenceProfile]
    let analyzing: Bool
    let error: String?
    @Binding var addSelection: [PhotosPickerItem]
    let onReplace: (String) -> Void
    let onRemove: (String) -> Void
    let onClearAll: () -> Void

    var body: some View {
        PremiumCard {
            CardHeader(systemIcon: "photo", title: "References") {
                if !profiles.isEmpty {
                    Button("Clear all", action: onClearAll)
                        .font(DyrectoType.labelLarge)
                        .foregroundColor(DyrectoColor.accent)
                        .disabled(analyzing)
                }
            }
            Spacer().frame(height: 4)
            Text("Add the shots you want to capture. Tap a shot to replace it.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 16)

            if profiles.isEmpty {
                PhotosPicker(selection: $addSelection, matching: .images) {
                    EmptyImagePlaceholder()
                }
                .disabled(analyzing)
            } else {
                // Responsive 3-column grid.
                LazyVGrid(
                    columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 3),
                    spacing: 10
                ) {
                    ForEach(profiles, id: \.id) { p in
                        ReferenceGridTile(
                            profile: p,
                            enabled: !analyzing,
                            onReplace: { onReplace(p.id) },
                            onRemove: { onRemove(p.id) })
                    }
                }
                Spacer().frame(height: 12)
                PhotosPicker(selection: $addSelection, matching: .images) {
                    AddTileLabel()
                }
                .disabled(analyzing)
            }

            if analyzing {
                Spacer().frame(height: 16)
                HStack(spacing: 12) {
                    ProgressView().tint(DyrectoColor.accent)
                    Text("Analyzing…")
                        .font(DyrectoType.bodyMedium)
                        .foregroundColor(DyrectoColor.textPrimary)
                }
            }

            if let error {
                Spacer().frame(height: 12)
                Text(error)
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.error)
            }
        }
    }
}

private struct ReferenceGridTile: View {
    let profile: ReferenceProfile
    let enabled: Bool
    let onReplace: () -> Void
    let onRemove: () -> Void

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Button(action: onReplace) {
                ZStack {
                    RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                        .fill(DyrectoColor.surfaceElevated)
                    if let thumb = referenceThumbnail(profile.imageUri) {
                        Image(uiImage: thumb)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } else {
                        Image(systemName: "photo")
                            .font(.system(size: 20))
                            .foregroundColor(DyrectoColor.textMuted)
                    }
                }
                .aspectRatio(1, contentMode: .fit)
                .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
            }
            .buttonStyle(.plain)
            .disabled(!enabled)

            // Remove badge (top-right).
            Button(action: onRemove) {
                ZStack {
                    Circle().fill(Color.black.opacity(0.55)).frame(width: 26, height: 26)
                    Image(systemName: "xmark")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.white)
                }
            }
            .buttonStyle(.plain)
            .disabled(!enabled)
            .padding(6)
            .accessibilityLabel("Remove shot")
        }
    }

    /// Decodes the app-private reference thumbnail; nil while absent.
    private func referenceThumbnail(_ imageUri: String?) -> UIImage? {
        guard let imageUri, let url = URL(string: imageUri) else { return nil }
        return UIImage(contentsOfFile: url.path)
    }
}

private struct AddTileLabel: View {
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "plus")
                .font(.system(size: 16, weight: .semibold))
            Text("Add shots")
                .font(DyrectoType.labelLarge)
                .fontWeight(.semibold)
        }
        .foregroundColor(DyrectoColor.accent)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(DyrectoColor.accent.opacity(0.06))
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                .strokeBorder(DyrectoColor.accent.opacity(0.45), lineWidth: 1))
    }
}

private struct EmptyImagePlaceholder: View {
    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Circle().fill(DyrectoColor.accent.opacity(0.16)).frame(width: 56, height: 56)
                Image(systemName: "photo")
                    .font(.system(size: 24))
                    .foregroundColor(DyrectoColor.accent)
            }
            Spacer().frame(height: 14)
            Text("Add the shots you want to capture")
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
                .multilineTextAlignment(.center)
            Spacer().frame(height: 4)
            Text("The assistant analyzes each on-device and alerts you when the live camera drifts away.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
        .padding(.horizontal, 20)
        .background(DyrectoColor.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.cardSmall, style: .continuous))
    }
}

// MARK: - Card 2 · Progress

/// Presentation status of one storyboard shot in the progress list.
private enum ShotStatus { case completed, current, pending }

// Completion-rule stepper bounds (Android HOLD_MIN/MAX, CONFIRM_MIN/MAX).
private let holdMin: Int32 = 1
private let holdMax: Int32 = 30
private let confirmMin: Int32 = 1
private let confirmMax: Int32 = 10

private struct ProgressCard: View {
    let state: ReferenceSessionState
    let currentMatchId: String?
    let onSetRule: (StoryboardCompletionRule) -> Void
    let onResetProgress: () -> Void
    let onInfo: (InfoDialogContent) -> Void

    var body: some View {
        let progress = state.progress
        let rule = state.completionRule
        let profiles = state.session?.profiles ?? []

        PremiumCard {
            CardHeader(systemIcon: "checklist", title: "Progress") {
                if state.completedCount > 0 {
                    Button("Reset", action: onResetProgress)
                        .font(DyrectoType.labelLarge)
                        .foregroundColor(DyrectoColor.accent)
                }
            }
            Spacer().frame(height: 16)

            Text("\(progress.completed) of \(progress.total) shots completed")
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
            Spacer().frame(height: 12)
            BigBar(progress.fraction)

            Spacer().frame(height: 20)
            VStack(alignment: .leading, spacing: 12) {
                ForEach(Array(profiles.enumerated()), id: \.element.id) { index, p in
                    let status: ShotStatus = p.completion.completed
                        ? .completed
                        : (p.id == currentMatchId ? .current : .pending)
                    ShotRow(index: index + 1, status: status)
                }
            }

            Spacer().frame(height: 16)
            LegendRow()

            Spacer().frame(height: 18)
            HairlineDivider()
            Spacer().frame(height: 12)

            CompletionRulesFooter(rule: rule, onSetRule: onSetRule, onInfo: onInfo)
        }
    }
}

private struct ShotRow: View {
    let index: Int
    let status: ShotStatus

    var body: some View {
        let (icon, tint): (String, Color) = {
            switch status {
            case .completed: return ("checkmark.circle.fill", DyrectoColor.statusGood)
            case .current: return ("play.fill", DyrectoColor.accent)
            case .pending: return ("circle", DyrectoColor.textMuted)
            }
        }()
        HStack(spacing: 0) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(tint)
                .frame(width: 22)
            Text("Shot \(index)")
                .font(DyrectoType.bodyLarge)
                .fontWeight(status == .pending ? .regular : .semibold)
                .foregroundColor(status == .pending ? DyrectoColor.textMuted : DyrectoColor.textPrimary)
                .padding(.leading, 12)
            if status == .current {
                Text("Current match")
                    .font(DyrectoType.labelMedium)
                    .foregroundColor(DyrectoColor.accent)
                    .padding(.leading, 10)
            }
            Spacer()
        }
    }
}

private struct LegendRow: View {
    var body: some View {
        HStack(spacing: 16) {
            LegendItem(icon: "checkmark.circle.fill", tint: DyrectoColor.statusGood, label: "Completed")
            LegendItem(icon: "play.fill", tint: DyrectoColor.accent, label: "Current")
            LegendItem(icon: "circle", tint: DyrectoColor.textMuted, label: "Pending")
        }
    }
}

private struct LegendItem: View {
    let icon: String
    let tint: Color
    let label: String

    var body: some View {
        HStack(spacing: 5) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(tint)
            Text(label)
                .font(DyrectoType.labelMedium)
                .foregroundColor(DyrectoColor.textMuted)
        }
    }
}

// MARK: - Completion rules (compact footer inside the Progress card)

private struct CompletionRulesFooter: View {
    let rule: StoryboardCompletionRule
    let onSetRule: (StoryboardCompletionRule) -> Void
    let onInfo: (InfoDialogContent) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Completion Rules")
                .font(DyrectoType.labelLarge)
                .fontWeight(.semibold)
                .foregroundColor(DyrectoColor.textPrimary)
            HStack(alignment: .top, spacing: 16) {
                RuleStepper(
                    label: "Hold",
                    valueText: "\(rule.holdSeconds) s",
                    onInfo: {
                        onInfo(InfoDialogContent(
                            title: "Hold",
                            body: "The minimum amount of time a shot must continuously remain above " +
                                  "the matching threshold before it counts as a successful match."))
                    },
                    onDecrement: { setRule(hold: max(rule.holdSeconds - 1, holdMin), confirm: rule.confirmCount) },
                    onIncrement: { setRule(hold: min(rule.holdSeconds + 1, holdMax), confirm: rule.confirmCount) },
                    decEnabled: rule.holdSeconds > holdMin,
                    incEnabled: rule.holdSeconds < holdMax)
                RuleStepper(
                    label: "Confirm",
                    valueText: "\(rule.confirmCount) x",
                    onInfo: {
                        onInfo(InfoDialogContent(
                            title: "Confirm",
                            body: "How many successful matches are required before this shot is marked " +
                                  "as Completed. A new confirmation is counted only after the shot drops " +
                                  "below the matching threshold and reaches it again."))
                    },
                    onDecrement: { setRule(hold: rule.holdSeconds, confirm: max(rule.confirmCount - 1, confirmMin)) },
                    onIncrement: { setRule(hold: rule.holdSeconds, confirm: min(rule.confirmCount + 1, confirmMax)) },
                    decEnabled: rule.confirmCount > confirmMin,
                    incEnabled: rule.confirmCount < confirmMax)
            }
        }
    }

    private func setRule(hold: Int32, confirm: Int32) {
        // interop: matchThreshold stays fixed (UI edits Hold+Confirm only — Android parity).
        onSetRule(StoryboardCompletionRule(
            holdSeconds: hold, confirmCount: confirm, matchThreshold: rule.matchThreshold))
    }
}

// MARK: - Reference analysis (Phase 16/16.1/16.2)

/// Below this inferred confidence a creative attribute is not shown — honesty over filler.
private let creativeUiConfidenceFloor: Float = 0.5

/// Photographer-facing summary of the reference's Creative Scene Model. Shows only the
/// attributes the assistant could read with confidence, in plain language — never the internal
/// importance weights, never AI jargon. Leads with the SHOT IDENTITY headline (Phase 16.2).
private struct ReferenceAnalysisCard: View {
    let creative: CreativeSceneModel?

    var body: some View {
        // interop: SignaturePresenter object → .shared; nested Insight data class exports as
        // SignaturePresenterInsight with title/text.
        let insights = SignaturePresenter.shared.insights(model: creative)
        let rows = creative.map(analysisRows) ?? []

        PremiumCard {
            CardHeader(systemIcon: "sparkles", title: "Reference Analysis")
            Spacer().frame(height: 16)

            if rows.isEmpty && insights.isEmpty {
                Text("The assistant couldn't read enough from this reference to describe it yet. " +
                     "Replace the image with a clearer shot to re-analyze.")
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
            } else {
                // LEAD with the shot identity — what makes THIS shot unique — then the signature,
                // structure and lighting. Plain language only.
                ForEach(Array(insights.enumerated()), id: \.offset) { index, insight in
                    if index > 0 { Spacer().frame(height: 14) }
                    if insight.title == "Shot Identity" {
                        IdentityHeadline(text: insight.text)
                    } else {
                        InsightRow(title: insight.title, text: insight.text)
                    }
                }

                // The scene classification is SUPPORTING metadata — de-emphasized, below identity.
                if !rows.isEmpty {
                    if !insights.isEmpty {
                        Spacer().frame(height: 18)
                        HairlineDivider(alpha: 0.4)
                        Spacer().frame(height: 10)
                        Text("DETAILS")
                            .font(DyrectoType.labelSmall)
                            .kerning(DyrectoType.labelSmallKerning)
                            .foregroundColor(DyrectoColor.textMuted)
                        Spacer().frame(height: 12)
                    }
                    ForEach(Array(rows.enumerated()), id: \.offset) { index, row in
                        if index > 0 { Spacer().frame(height: 10) }
                        MetadataRow(label: row.0, value: row.1)
                    }
                }
            }
        }
    }
}

/// The primary line: the shot's unique identity, emphasized above the supporting rows.
private struct IdentityHeadline: View {
    let text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("SHOT IDENTITY")
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .foregroundColor(DyrectoColor.accent)
            Text(text)
                .font(DyrectoType.titleMedium)
                .foregroundColor(DyrectoColor.textPrimary)
        }
    }
}

/// A titled, full-width sentence insight (Visual Signature / Scene Structure / Lighting Relationship).
private struct InsightRow: View {
    let title: String
    let text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title.uppercased())
                .font(DyrectoType.labelSmall)
                .kerning(DyrectoType.labelSmallKerning)
                .foregroundColor(DyrectoColor.accent)
            Text(text)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textPrimary)
        }
    }
}

/// A de-emphasized metadata row (scene classification) — supporting context below the identity.
private struct MetadataRow: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(label)
                .font(DyrectoType.labelSmall)
                .foregroundColor(DyrectoColor.textMuted)
            Text(value)
                .font(DyrectoType.bodyMedium)
                .foregroundColor(DyrectoColor.textMuted)
        }
    }
}

/// The visible label/value pairs for the analysis card — only confidently-inferred attributes.
private func analysisRows(_ m: CreativeSceneModel) -> [(String, String)] {
    var rows: [(String, String)] = []
    if m.subject.confidence >= creativeUiConfidenceFloor,
       let label = subjectKindLabel(m.subject.kind) { rows.append(("Subject", label)) }
    if m.camera.shotTypeConfidence >= creativeUiConfidenceFloor,
       let label = shotTypeLabel(m.camera.shotType) { rows.append(("Shot Type", label)) }
    if m.lighting.confidence >= creativeUiConfidenceFloor,
       let label = lightingLabel(m.lighting) { rows.append(("Lighting", label)) }
    if m.composition.confidence >= creativeUiConfidenceFloor {
        if let label = placementLabel(m.composition.placement) { rows.append(("Composition", label)) }
        if m.composition.negativeSpace == .high { rows.append(("Negative Space", "Prominent")) }
    }
    if m.color.confidence >= creativeUiConfidenceFloor,
       let label = colorTemperatureLabel(m.color.temperature) { rows.append(("Color", label)) }
    if m.style.confidence >= creativeUiConfidenceFloor,
       let label = moodLabel(m.style.mood) { rows.append(("Mood", label)) }
    // Camera angle & depth of field only appear once a future model infers them confidently.
    if m.camera.angleConfidence >= creativeUiConfidenceFloor,
       let label = cameraAngleLabel(m.camera.angle) { rows.append(("Camera Angle", label)) }
    if m.depth.confidence >= creativeUiConfidenceFloor,
       let label = depthOfFieldLabel(m.depth.depthOfField) { rows.append(("Depth of Field", label)) }
    return rows
}

// interop: Kotlin enum entries export lowercase (CreativeSubjectKind.portrait, …).
private func subjectKindLabel(_ kind: CreativeSubjectKind) -> String? {
    switch kind {
    case .portrait: return "Portrait"
    case .group: return "Group"
    case .animal: return "Animal"
    case .vehicle: return "Vehicle"
    case .product: return "Product"
    case .food: return "Food"
    case .scene: return "Scene"
    default: return nil
    }
}

private func shotTypeLabel(_ shot: ShotType) -> String? {
    switch shot {
    case .extremeCloseUp: return "Extreme Close-up"
    case .closeUp: return "Close-up"
    case .mediumCloseUp: return "Medium Close-up"
    case .medium: return "Medium"
    case .wide: return "Wide"
    default: return nil
    }
}

private func lightingLabel(_ lighting: CreativeLighting) -> String? {
    // interop: Kotlin Boolean? exports as KotlinBoolean?.
    if lighting.backlightHint?.boolValue == true { return "Backlit" }
    switch lighting.key {
    case .lowKey: return "Low Key"
    case .highKey: return "High Key"
    case .balanced: return "Balanced"
    default: return nil
    }
}

private func placementLabel(_ placement: SubjectPlacement) -> String? {
    switch placement {
    case .leftThird: return "Left Third"
    case .center: return "Centered"
    case .rightThird: return "Right Third"
    default: return nil
    }
}

private func colorTemperatureLabel(_ temp: ColorTemperature) -> String? {
    switch temp {
    case .warm: return "Warm"
    case .cool: return "Cool"
    case .neutral: return "Neutral"
    default: return nil
    }
}

private func moodLabel(_ mood: Mood) -> String? {
    switch mood {
    case .warmCinematic: return "Warm Cinematic"
    case .brightAiry: return "Bright & Airy"
    case .darkMoody: return "Dark & Moody"
    case .neutral: return "Neutral"
    default: return nil
    }
}

private func cameraAngleLabel(_ angle: CameraAngle) -> String? {
    switch angle {
    case .eyeLevel: return "Eye Level"
    case .highAngle: return "High Angle"
    case .lowAngle: return "Low Angle"
    default: return nil
    }
}

private func depthOfFieldLabel(_ dof: DepthOfField) -> String? {
    switch dof {
    case .shallow: return "Shallow"
    case .deep: return "Deep"
    default: return nil
    }
}

// MARK: - What to Monitor

/// One monitor option's presentation resolved from its ReferenceMonitorOptions flag.
private struct MonitorTileSpec: Identifiable {
    let id: String
    let label: String
    let systemIcon: String
    let checked: Bool
    let enabled: Bool
    let onChange: (Bool) -> Void

    init(_ label: String, _ systemIcon: String, checked: Bool, enabled: Bool,
         onChange: @escaping (Bool) -> Void) {
        self.id = label
        self.label = label
        self.systemIcon = systemIcon
        self.checked = checked
        self.enabled = enabled
        self.onChange = onChange
    }
}

private struct WhatToMonitorCard: View {
    @ObservedObject var session: MonitoringSessionIos

    var body: some View {
        let referenceState = session.referenceState
        let profile = referenceState.profile
        let options = profile?.options ?? ReferenceMonitorOptions()
        let hasSubject = profile?.subject != nil
        let ai = profile?.ai
        let strategy = ai?.strategy ?? .humanStrategy
        // interop: ReferenceSignalApplicability object → .shared; Kotlin Set<Enum> bridges to Set.
        let applicable = ReferenceSignalApplicability.shared.signalsFor(strategy: strategy)
        let subjectCapable = ai != nil ? (ai?.primarySubjectBox != nil) : hasSubject

        PremiumCard {
            CardHeader(systemIcon: "eye", title: "What to Monitor")
            Spacer().frame(height: 4)
            Text("Choose what aspects you want the assistant to monitor. Applies to every shot.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)

            if let gateMessage = gateMessage(profile: profile, subjectCapable: subjectCapable,
                                             applicable: applicable) {
                Spacer().frame(height: 12)
                Text(gateMessage)
                    .font(DyrectoType.bodySmall)
                    .foregroundColor(DyrectoColor.textMuted)
            }

            let tiles = buildTiles(
                profile: profile, options: options, ai: ai, applicable: applicable,
                hasSubject: hasSubject, subjectCapable: subjectCapable)

            Spacer().frame(height: 16)
            LazyVGrid(
                columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 2),
                spacing: 10
            ) {
                ForEach(tiles) { tile in
                    MonitorTile(spec: tile)
                }
            }
        }
    }

    private func gateMessage(
        profile: ReferenceProfile?, subjectCapable: Bool, applicable: Set<ReferenceSignal>
    ) -> String? {
        if profile == nil { return "Add a reference image to choose what to monitor." }
        if !subjectCapable && applicable.contains(.subjectPosition) {
            return "No subject was detected in the first shot — subject-based monitoring " +
                   "(position, size, presence) is unavailable."
        }
        return nil
    }

    private func buildTiles(
        profile: ReferenceProfile?, options: ReferenceMonitorOptions, ai: ReferenceAiProfile?,
        applicable: Set<ReferenceSignal>, hasSubject: Bool, subjectCapable: Bool
    ) -> [MonitorTileSpec] {
        var tiles: [MonitorTileSpec] = []
        tiles.append(MonitorTileSpec(
            "Exposure", "sparkles",
            checked: options.monitorExposure, enabled: profile != nil
        ) { setOption(options, monitorExposure: $0) })
        tiles.append(MonitorTileSpec(
            "White Balance", "drop",
            checked: options.monitorWhiteBalance, enabled: profile != nil
        ) { setOption(options, monitorWhiteBalance: $0) })
        if applicable.contains(.subjectPosition) {
            tiles.append(MonitorTileSpec(
                "Framing (Position)", "crop",
                checked: options.monitorSubjectPosition, enabled: profile != nil && subjectCapable
            ) { setOption(options, monitorSubjectPosition: $0) })
        }
        if applicable.contains(.subjectSize) {
            tiles.append(MonitorTileSpec(
                "Subject Size (Distance)", "ruler",
                checked: options.monitorSubjectSize, enabled: profile != nil && subjectCapable
            ) { setOption(options, monitorSubjectSize: $0) })
        }
        if applicable.contains(.subjectPresence), ai != nil {
            tiles.append(MonitorTileSpec(
                "Subject Presence", "person",
                checked: options.monitorSubjectPresence, enabled: subjectCapable
            ) { setOption(options, monitorSubjectPresence: $0) })
        }
        if applicable.contains(.composition), let ai {
            tiles.append(MonitorTileSpec(
                "Composition", "square.grid.3x3",
                checked: options.monitorComposition, enabled: !ai.compositionSignature.isEmpty
            ) { setOption(options, monitorComposition: $0) })
        }
        if applicable.contains(.visualSimilarity), let ai {
            tiles.append(MonitorTileSpec(
                "Visual Similarity", "sparkles",
                checked: options.monitorVisualSimilarity, enabled: ai.embeddingId != nil
            ) { setOption(options, monitorVisualSimilarity: $0) })
        }
        if applicable.contains(.headroom) {
            tiles.append(MonitorTileSpec(
                "Headroom", "arrow.up.to.line",
                checked: options.monitorHeadroom, enabled: profile != nil && hasSubject
            ) { setOption(options, monitorHeadroom: $0) })
        }
        if applicable.contains(.facePresence) {
            tiles.append(MonitorTileSpec(
                "Face Presence", "face.smiling",
                checked: options.monitorFacePresence, enabled: profile != nil && hasSubject
            ) { setOption(options, monitorFacePresence: $0) })
        }
        if applicable.contains(.eyeVisibility) {
            tiles.append(MonitorTileSpec(
                "Eye Visibility", "eye",
                checked: options.monitorEyeVisibility, enabled: profile != nil && hasSubject
            ) { setOption(options, monitorEyeVisibility: $0) })
        }
        return tiles
    }

    private func setOption(
        _ o: ReferenceMonitorOptions,
        monitorExposure: Bool? = nil,
        monitorSubjectPosition: Bool? = nil,
        monitorSubjectSize: Bool? = nil,
        monitorWhiteBalance: Bool? = nil,
        monitorHeadroom: Bool? = nil,
        monitorFacePresence: Bool? = nil,
        monitorEyeVisibility: Bool? = nil,
        monitorSubjectPresence: Bool? = nil,
        monitorComposition: Bool? = nil,
        monitorVisualSimilarity: Bool? = nil,
        tolerance: ReferenceTolerance? = nil
    ) {
        // interop: Kotlin data-class copy exports as doCopy(ALL params) — constructing a fresh
        // instance with every field is equivalent and clearer from Swift.
        session.setReferenceOptions(ReferenceMonitorOptions(
            monitorExposure: monitorExposure ?? o.monitorExposure,
            monitorSubjectPosition: monitorSubjectPosition ?? o.monitorSubjectPosition,
            monitorSubjectSize: monitorSubjectSize ?? o.monitorSubjectSize,
            monitorWhiteBalance: monitorWhiteBalance ?? o.monitorWhiteBalance,
            monitorFraming: o.monitorFraming,
            monitorHeadroom: monitorHeadroom ?? o.monitorHeadroom,
            monitorFacePresence: monitorFacePresence ?? o.monitorFacePresence,
            monitorEyeVisibility: monitorEyeVisibility ?? o.monitorEyeVisibility,
            monitorSubjectPresence: monitorSubjectPresence ?? o.monitorSubjectPresence,
            monitorComposition: monitorComposition ?? o.monitorComposition,
            monitorVisualSimilarity: monitorVisualSimilarity ?? o.monitorVisualSimilarity,
            tolerance: tolerance ?? o.tolerance))
    }
}

private struct MonitorTile: View {
    let spec: MonitorTileSpec

    var body: some View {
        let active = spec.checked && spec.enabled
        let background = active ? DyrectoColor.accent : DyrectoColor.surfaceElevated
        let content: Color = active ? .black : DyrectoColor.textPrimary
        let alpha = spec.enabled ? 1.0 : 0.4
        Button(action: { spec.onChange(!spec.checked) }) {
            HStack(spacing: 12) {
                Image(systemName: spec.systemIcon)
                    .font(.system(size: 15))
                    .foregroundColor(content.opacity(alpha))
                Text(spec.label)
                    .font(DyrectoType.bodyMedium)
                    .fontWeight(.medium)
                    .foregroundColor(content.opacity(alpha))
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 14)
            .frame(height: 64)
            .frame(maxWidth: .infinity)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(!spec.enabled)
    }
}

// MARK: - Matching strictness

private struct MatchingStrictnessCard: View {
    @ObservedObject var session: MonitoringSessionIos

    var body: some View {
        let options = session.referenceState.profile?.options ?? ReferenceMonitorOptions()
        let enabled = session.referenceState.profile != nil
        let ordered: [(ReferenceTolerance, String, String)] = [
            (.loose, "Loose", "More flexible"),
            (.medium, "Medium", "Balanced"),
            (.strict, "Strict", "More precise"),
        ]

        PremiumCard {
            CardHeader(systemIcon: "scope", title: "Matching Strictness")
            Spacer().frame(height: 4)
            Text("How strict the assistant should be when comparing to the reference.")
                .font(DyrectoType.bodySmall)
                .foregroundColor(DyrectoColor.textMuted)
            Spacer().frame(height: 16)
            HStack(alignment: .top, spacing: 10) {
                ForEach(ordered, id: \.1) { tolerance, title, subtitle in
                    StrictnessSegment(
                        title: title, subtitle: subtitle,
                        selected: options.tolerance == tolerance,
                        enabled: enabled
                    ) {
                        session.setReferenceOptions(ReferenceMonitorOptions(
                            monitorExposure: options.monitorExposure,
                            monitorSubjectPosition: options.monitorSubjectPosition,
                            monitorSubjectSize: options.monitorSubjectSize,
                            monitorWhiteBalance: options.monitorWhiteBalance,
                            monitorFraming: options.monitorFraming,
                            monitorHeadroom: options.monitorHeadroom,
                            monitorFacePresence: options.monitorFacePresence,
                            monitorEyeVisibility: options.monitorEyeVisibility,
                            monitorSubjectPresence: options.monitorSubjectPresence,
                            monitorComposition: options.monitorComposition,
                            monitorVisualSimilarity: options.monitorVisualSimilarity,
                            tolerance: tolerance))
                    }
                }
            }
        }
    }
}

private struct StrictnessSegment: View {
    let title: String
    let subtitle: String
    let selected: Bool
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        let alpha = enabled ? 1.0 : 0.45
        Button(action: onClick) {
            VStack(spacing: 2) {
                Text(title)
                    .font(DyrectoType.titleSmall)
                    .foregroundColor((selected ? DyrectoColor.accent : DyrectoColor.textPrimary).opacity(alpha))
                Text(subtitle)
                    .font(DyrectoType.labelSmall)
                    .foregroundColor(DyrectoColor.textMuted.opacity(alpha))
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .padding(.horizontal, 8)
            .background(selected ? DyrectoColor.accent.opacity(0.12) : DyrectoColor.surfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: DyrectoRadius.tile, style: .continuous)
                    .strokeBorder(selected ? DyrectoColor.accent.opacity(0.55) : .clear, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

// MARK: - Primary action

private struct PrimaryActionSection: View {
    let monitoring: Bool
    let hasProfile: Bool
    let onStart: () -> Void
    let onStop: () -> Void

    var body: some View {
        VStack(spacing: 10) {
            GradientActionButton(
                monitoring: monitoring, enabled: hasProfile,
                onClick: monitoring ? onStop : onStart)
            HStack(spacing: 6) {
                Image(systemName: "camera.metering.center.weighted")
                    .font(.system(size: 12))
                    .foregroundColor(DyrectoColor.textMuted)
                Text("Monitoring will run while the camera is streaming.")
                    .font(DyrectoType.labelMedium)
                    .foregroundColor(DyrectoColor.textMuted)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

private struct GradientActionButton: View {
    let monitoring: Bool
    let enabled: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 10) {
                Image(systemName: monitoring ? "stop.fill" : "play.fill")
                    .font(.system(size: 18, weight: .semibold))
                Text(monitoring ? "Stop Monitoring" : "Start Monitoring")
                    .font(DyrectoType.titleMedium)
                    .fontWeight(.bold)
            }
            .foregroundColor(enabled ? .white : DyrectoColor.textMuted)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 18)
            .background(
                Group {
                    if !enabled {
                        DyrectoColor.surfaceElevated
                    } else if monitoring {
                        LinearGradient(
                            colors: [DyrectoColor.error, DyrectoColor.error.opacity(0.75)],
                            startPoint: .leading, endPoint: .trailing)
                    } else {
                        LinearGradient(
                            colors: [DyrectoColor.accent, DyrectoColor.secondary],
                            startPoint: .leading, endPoint: .trailing)
                    }
                })
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}
