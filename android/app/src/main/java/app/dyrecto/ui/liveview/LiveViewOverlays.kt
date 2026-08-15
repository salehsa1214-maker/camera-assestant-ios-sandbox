package app.dyrecto.ui.liveview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.liveview.exposure.FalseColorScale
import app.dyrecto.liveview.exposure.OverlayConfig
import app.dyrecto.liveview.vision.results.FocusPeakingResult
import app.dyrecto.liveview.vision.results.WaveformResult

// ─────────────────────────────── Overlay toggle rail ───────────────────────────────

/**
 * Monitoring-overlay toggles for the live view (Waveform / False Color / Focus Peaking). Reads and
 * writes the process-scoped [OverlayConfig] so the Vision worker starts/stops computing each overlay
 * immediately.
 *
 * @param vertical stack in a column (landscape right-edge bar) vs a row (portrait top bar).
 * @param compact short labels + tighter sizing so the toggles fit the narrow landscape side bar.
 */
@Composable
fun OverlayToggleRail(vertical: Boolean, compact: Boolean, modifier: Modifier = Modifier) {
    val waveform by OverlayConfig.waveform.collectAsState()
    val falseColor by OverlayConfig.falseColor.collectAsState()
    val peaking by OverlayConfig.focusPeaking.collectAsState()
    // Dyrecto Premium: analysis overlays are premium. Free users keep the chips visible (locked)
    // and tapping opens the purchase sheet instead of toggling.
    val premium by app.dyrecto.billing.EntitlementProvider.isPremium.collectAsState()
    fun gated(set: (Boolean) -> Unit): (Boolean) -> Unit =
        if (premium) set else ({ _ -> app.dyrecto.ui.components.PremiumSheetController.show() })
    val lock = if (premium) "" else "🔒 "

    // Full words in both orientations. `compact` only trims sizing very slightly for the side bar.
    val chips: @Composable () -> Unit = {
        OverlayChip("${lock}Waveform", waveform, compact, gated { OverlayConfig.setWaveform(it) })
        OverlayChip("${lock}False Color", falseColor, compact, gated { OverlayConfig.setFalseColor(it) })
        OverlayChip("${lock}Peaking", peaking, compact, gated { OverlayConfig.setFocusPeaking(it) })
    }
    if (vertical) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    } else {
        Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}

@Composable
private fun OverlayChip(label: String, checked: Boolean, compact: Boolean, onChange: (Boolean) -> Unit) {
    val bg = if (checked) Color(0xFF2E7D32).copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.5f)
    Text(
        text = label,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = if (compact) 13.sp else 14.sp,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onChange(!checked) }
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 8.dp else 9.dp,
            ),
    )
}

// ─────────────────────────────── Overlay render layers ───────────────────────────────

/**
 * Focus-peaking overlay: draws the peaked-edge mask over the frame. The mask bitmap has the same
 * aspect ratio as the source frame (cols×rows = downsampled frame), so drawing it with the same
 * [ContentScale.Fit] letterboxes identically to the frame Image and lines up automatically.
 */
@Composable
fun FocusPeakingLayer(result: FocusPeakingResult?, modifier: Modifier = Modifier) {
    if (result?.mask == null) return
    val bmp = remember(result) { focusPeakingBitmap(result) } ?: return
    Image(
        bitmap = bmp.asImageBitmap(),
        contentDescription = "Focus peaking overlay",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

/** Waveform monitor panel (bottom-left), drawn from the per-frame [WaveformResult]. */
@Composable
fun WaveformPanel(result: WaveformResult?, modifier: Modifier = Modifier) {
    if (result == null || result.maxIntensity <= 0) return
    val bmp = remember(result) { waveformBitmap(result) }
    Box(
        modifier
            .width(160.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(4.dp),
    ) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Waveform",
            modifier = Modifier.size(width = 152.dp, height = 84.dp),
            contentScale = ContentScale.FillBounds,
        )
    }
}

// ─────────────────────────────── Bitmap builders ───────────────────────────────

/** Green luma waveform: rows = luma (bright at top), columns = image column, intensity = brightness. */
internal fun waveformBitmap(wf: WaveformResult): Bitmap {
    val w = wf.columns
    val h = wf.bins
    val px = IntArray(w * h)
    val max = wf.maxIntensity.coerceAtLeast(1)
    for (c in 0 until w) {
        for (b in 0 until h) {
            val v = wf.intensity[c * h + b]
            if (v > 0) {
                val a = (255 * v / max).coerceIn(0, 255)
                // Flip vertically: bright luma (high b) drawn near the top.
                px[(h - 1 - b) * w + c] = (a shl 24) or 0x00FF66
            }
        }
    }
    return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
}

/** Peaked-edge mask → opaque peaking colour where set, transparent elsewhere. */
internal fun focusPeakingBitmap(fp: FocusPeakingResult): Bitmap? {
    val mask = fp.mask ?: return null
    val w = fp.cols
    val h = fp.rows
    if (w <= 0 || h <= 0 || mask.size < w * h) return null
    val px = IntArray(w * h)
    val color = 0xFFFF2D55.toInt() // vivid pink-red, high-visibility peaking
    for (i in 0 until w * h) if (mask[i].toInt() != 0) px[i] = color
    return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
}

/**
 * Recolours a frame to false colour by mapping each pixel's BT.709 luma through
 * [FalseColorScale.lumaToColor]. Full-frame pass — call off the main thread (see LiveViewScreen).
 */
internal fun falseColorBitmap(src: Bitmap): Bitmap {
    val w = src.width
    val h = src.height
    val px = IntArray(w * h)
    src.getPixels(px, 0, w, 0, 0, w, h)
    val lut = FalseColorScale.lumaToColor
    var i = 0
    val n = px.size
    while (i < n) {
        val p = px[i]
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        val luma = (54 * r + 183 * g + 19 * b) shr 8 // BT.709, matches LuminanceAnalyzer
        px[i] = (0xFF shl 24) or (lut[luma] and 0xFFFFFF)
        i++
    }
    return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
}
