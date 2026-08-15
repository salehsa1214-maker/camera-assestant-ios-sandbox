package app.dyrecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.dyrecto.ui.theme.Hairline
import app.dyrecto.ui.theme.SurfaceBase
import app.dyrecto.ui.theme.SurfaceCard
import app.dyrecto.ui.theme.TextMuted

/** One tab in the [FloatingNavBar]: a route to switch to, plus its icon and label. */
data class NavBarItem(val route: String, val icon: ImageVector, val label: String)

/**
 * Bottom content inset the top-level screens should reserve so their last items scroll clear of the
 * floating island (which overlays the content rather than reserving its own layout slot). Sized to
 * the island's visual height + its float margins, plus a little breathing room.
 */
val FloatingNavBarInset = 96.dp

/**
 * A floating "glass island" bottom navigation bar. It sits detached from the screen edges (margins
 * around it reveal the app background, so it reads as floating), with a translucent frosted surface,
 * a soft top sheen, a hairline light edge, and a drop shadow — the same premium, glassy vocabulary
 * as the redesigned cards.
 *
 * Selection is animated: the active tab expands into an accent-tinted pill that reveals its label,
 * its icon lifts to full scale and its colour springs to the accent; the previous tab collapses its
 * label back to an icon. Pure presentation — it only reports taps via [onSelect]; it owns no state
 * and drives exactly the same route switching the old Material `NavigationBar` did.
 */
@Composable
fun FloatingNavBar(
    items: List<NavBarItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A straight, edge-to-edge bottom bar — no rounded corners, flush to all edges.
    val shape = RectangleShape
    // A glassy bottom bar. A scrim above it dissolves the scrolling content into the app background
    // (SurfaceBase) so there is no hard slab — content appears to fade out at a line as it passes
    // under the glass.
    Box(
        modifier
            .fillMaxWidth()
            .height(FloatingNavBarInset + 28.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.12f to Color.Transparent,
                        0.42f to SurfaceBase,
                        1f to SurfaceBase,
                    ),
                ),
        )
        Row(
            Modifier
                // Edge to edge: no side or bottom margins, flush to the screen bottom.
                .fillMaxWidth()
                .shadow(elevation = 14.dp, shape = shape, clip = false)
                .clip(shape)
                // Translucent frosted base + a very faint top-down sheen for the glass read.
                .background(SurfaceCard.copy(alpha = 0.82f))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.03f), Color.White.copy(alpha = 0f)),
                    ),
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Hairline.copy(alpha = 0.20f)),
                    ),
                    shape,
                )
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                NavPill(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onSelect(item.route) },
                )
            }
        }
    }
}

@Composable
private fun NavPill(
    item: NavBarItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    // Colour + scale spring toward the selected state; the label expands/collapses in place.
    val tint by animateColorAsState(
        if (selected) accent else TextMuted,
        animationSpec = tween(durationMillis = 240),
        label = "navTint",
    )
    val pillBg by animateColorAsState(
        if (selected) accent.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(durationMillis = 240),
        label = "navPillBg",
    )
    val iconScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (selected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navIconScale",
    )

    Row(
        Modifier
            .clip(CircleShape)
            .background(pillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale),
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(animationSpec = tween(240)) + fadeIn(tween(240)),
            exit = shrinkHorizontally(animationSpec = tween(200)) + fadeOut(tween(140)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Text(
                    item.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = tint,
                    maxLines = 1,
                )
            }
        }
    }
}
