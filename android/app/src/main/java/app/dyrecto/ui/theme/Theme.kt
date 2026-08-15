package app.dyrecto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand palette — a calm "studio" blue/teal with clear status accents.
private val Accent = Color(0xFF4FC3F7)
private val AccentDark = Color(0xFF0288D1)

val StatusGood = Color(0xFF36D399)
val StatusWarn = Color(0xFFFFC24B)
val StatusBad = Color(0xFFFF5A52)
val StatusIdle = Color(0xFF8A93A0)

// ── Dark surface system (near-black base, layered elevation) ──────────────────
// Exposed so components build elevated cards / hairlines / scrims off one source.
val SurfaceBase = Color(0xFF0B0E13)       // app background — near black
val SurfaceCard = Color(0xFF161B22)       // primary card / elevated surface
val SurfaceElevated = Color(0xFF1E252E)   // raised tiles, chips on a card
val Hairline = Color(0xFF2A323C)          // subtle divider / outline
val TextPrimary = Color(0xFFEEF2F6)
val TextMuted = Color(0xFF98A2B0)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF00121A),
    secondary = Color(0xFF80CBC4),
    background = SurfaceBase,
    surface = SurfaceCard,
    surfaceVariant = SurfaceElevated,
    outline = Hairline,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    error = StatusBad,
)

private val LightColors = lightColorScheme(
    primary = AccentDark,
    secondary = Color(0xFF00796B),
    background = Color(0xFFF6F8FA),
    surface = Color(0xFFFFFFFF),
    error = StatusBad,
)

@Composable
fun DyrectoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CameraTypography,
        content = content,
    )
}
