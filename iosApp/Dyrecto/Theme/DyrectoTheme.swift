import SwiftUI

/// The exact Android palette (ui/theme/Theme.kt) mapped to SwiftUI. The app is dark-first —
/// these are the DarkColors values; the premium idiom (near-black base, layered elevation,
/// hairline borders) carries over 1:1.
enum DyrectoColor {
    // Brand palette — a calm "studio" blue/teal with clear status accents.
    static let accent = Color(hex: 0x4FC3F7)          // primary
    static let accentDark = Color(hex: 0x0288D1)
    static let onPrimary = Color(hex: 0x00121A)
    static let secondary = Color(hex: 0x80CBC4)

    static let statusGood = Color(hex: 0x36D399)
    static let statusWarn = Color(hex: 0xFFC24B)
    static let statusBad = Color(hex: 0xFF5A52)       // also the error color
    static let statusIdle = Color(hex: 0x8A93A0)

    // ── Dark surface system (near-black base, layered elevation) ──────────────
    static let surfaceBase = Color(hex: 0x0B0E13)     // app background — near black
    static let surfaceCard = Color(hex: 0x161B22)     // primary card / elevated surface
    static let surfaceElevated = Color(hex: 0x1E252E) // raised tiles, chips on a card
    static let hairline = Color(hex: 0x2A323C)        // subtle divider / outline
    static let textPrimary = Color(hex: 0xEEF2F6)     // onBackground / onSurface
    static let textMuted = Color(hex: 0x98A2B0)       // onSurfaceVariant

    static let error = statusBad

    /// The restrained secondary (purple) accent used only for the Voice Guidance shortcut.
    static let voiceAccent = Color(hex: 0x8C7BF0)
}

extension Color {
    /// 0xRRGGBB literal → Color (the Android `Color(0xFF......)` values without the alpha byte).
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: 1.0)
    }
}

/// The Android `CameraTypography` scale (ui/theme/Type.kt) mapped to SF equivalents:
/// large tight numerals for glanceable metrics, quieter tracked-out labels for captions/chips.
enum DyrectoType {
    /// Hero numerals (34sp bold, -0.5 tracking) — e.g. the big match % value.
    static let displaySmall = Font.system(size: 34, weight: .bold)
    /// 26sp bold, -0.25 tracking — large screen headers ("Dashboard", "Storyboard").
    static let headlineMedium = Font.system(size: 26, weight: .bold)
    /// 22sp semibold — guidance headline, metric tiles.
    static let headlineSmall = Font.system(size: 22, weight: .semibold)
    /// 20sp semibold — card titles (CardHeader).
    static let titleLarge = Font.system(size: 20, weight: .semibold)
    /// 16sp semibold — row titles, primary values.
    static let titleMedium = Font.system(size: 16, weight: .semibold)
    /// 14sp semibold — segment labels, compact values (M3 default titleSmall).
    static let titleSmall = Font.system(size: 14, weight: .semibold)
    /// 16sp regular — prominent body rows.
    static let bodyLarge = Font.system(size: 16)
    /// 14sp regular — the standard body text.
    static let bodyMedium = Font.system(size: 14)
    /// 12sp regular — card descriptions, secondary copy.
    static let bodySmall = Font.system(size: 12)
    /// 14sp medium — buttons, pill labels.
    static let labelLarge = Font.system(size: 14, weight: .medium)
    /// 12sp medium, 0.8 tracking — section eyebrows / metric labels (uppercased at call site).
    static let labelMedium = Font.system(size: 12, weight: .medium)
    /// 11sp medium, 0.5 tracking — tiny captions.
    static let labelSmall = Font.system(size: 11, weight: .medium)

    static let mono = Font.system(size: 12, design: .monospaced)
    static let monoSmall = Font.system(size: 10, design: .monospaced)

    /// The Android labelMedium letterSpacing (0.8sp) as SwiftUI kerning.
    static let labelMediumKerning: CGFloat = 0.8
    static let labelSmallKerning: CGFloat = 0.5
}

/// Shared layout rhythm (Android `Spacing` + the redesigned screens' constants).
enum DyrectoSpacing {
    static let screen: CGFloat = 16
    static let section: CGFloat = 12
    static let item: CGFloat = 8
    /// The redesigned top-level screens use 20dp horizontal padding + 24dp between cards.
    static let screenHorizontal: CGFloat = 20
    static let cardGap: CGFloat = 24
    /// Bottom inset the top-level screens reserve so content clears the tab bar comfortably.
    static let bottomInset: CGFloat = 24
}

/// Corner radii from the Android components.
enum DyrectoRadius {
    static let premiumCard: CGFloat = 24   // PremiumCard / hero cards
    static let guidanceCard: CGFloat = 28  // the Dashboard guidance hero
    static let card: CGFloat = 20          // SectionCard-style containers / alert cards
    static let cardSmall: CGFloat = 16     // MetricTile / NavListRow / buttons
    static let tile: CGFloat = 14          // grid tiles, segments, monitor tiles
    static let chipIcon: CGFloat = 11      // the 36pt icon badge in CardHeader
    static let stepper: CGFloat = 12
}
