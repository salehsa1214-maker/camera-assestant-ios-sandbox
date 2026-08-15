package app.dyrecto.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.dyrecto.billing.EntitlementProvider
import app.dyrecto.billing.EntitlementProvider.RestoreOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Dyrecto Premium UX kit. Presentation only — every premium capability is ALSO guarded at an
 * engine seam ([app.dyrecto.service.DefaultMonitoringSession] / [EntitlementProvider]); these
 * composables just make the boundary visible and open the purchase flow.
 */
object PremiumSheetController {
    /** True while the purchase sheet should be shown. Any screen may call [show]. */
    val visible = MutableStateFlow(false)
    fun show() { visible.value = true }
    fun dismiss() { visible.value = false }
}

/** Small lock chip marking a premium feature for free users. */
@Composable
fun LockBadge(modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = "Premium feature",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(11.dp),
        )
        Text(
            "PREMIUM",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/**
 * Wrap a premium action's onClick: premium users pass through, free users get the purchase
 * sheet. Use for tap targets that must stay visible but locked.
 */
@Composable
fun premiumGated(action: () -> Unit): () -> Unit {
    val premium by EntitlementProvider.isPremium.collectAsState()
    return if (premium) action else ({ PremiumSheetController.show() })
}

/**
 * The one purchase surface: hosted once at AppRoot, shown via [PremiumSheetController]. Reads
 * price from Play [ProductDetails] (never hardcoded) and keeps Restore outcomes distinct.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSheetHost() {
    val visible by PremiumSheetController.visible.collectAsState()
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val premium by EntitlementProvider.isPremium.collectAsState()
    val price by EntitlementProvider.formattedPrice.collectAsState()
    val pending by EntitlementProvider.purchasePending.collectAsState()
    val purchaseError by EntitlementProvider.lastError.collectAsState()
    val unavailable by EntitlementProvider.billingUnavailable.collectAsState()
    var restoreMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            EntitlementProvider.clearError()
            PremiumSheetController.dismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Unlock Dyrecto",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Get the complete camera assistant toolkit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(16.dp))

            when {
                premium -> {
                    Text(
                        "Dyrecto is unlocked — thank you!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                pending -> {
                    StatusText("Your payment is still processing in Google Play. Premium unlocks automatically once it completes.")
                }
                unavailable -> {
                    StatusText("Google Play billing isn't available on this device.")
                }
                else -> {
                    Text(
                        price?.let { "$it — one-time purchase" } ?: "Loading price from Google Play…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "No subscription.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    SheetButton(
                        label = "Unlock Dyrecto",
                        enabled = price != null,
                        primary = true,
                    ) {
                        (context as? Activity)?.let { EntitlementProvider.launchPurchase(it) }
                    }
                    if (price == null) {
                        StatusText("Connect to the internet to purchase.")
                    }
                }
            }

            purchaseError?.let { StatusText(it, error = true) }
            restoreMessage?.let { StatusText(it) }

            if (!premium) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Already purchased? Restore purchases",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            restoreMessage = "Checking with Google Play…"
                            scope.launch {
                                restoreMessage = restoreOutcomeMessage(EntitlementProvider.restore())
                            }
                        }
                        .padding(8.dp),
                )
            }
        }
    }
}

/** One message per distinct restore outcome — never conflated. */
fun restoreOutcomeMessage(outcome: RestoreOutcome): String = when (outcome) {
    RestoreOutcome.UNAVAILABLE -> "Google Play billing isn't available on this device."
    RestoreOutcome.NETWORK_ERROR -> "Couldn't reach Google Play. Check your connection and try again."
    RestoreOutcome.NONE_FOUND -> "No previous purchase found for this Google account."
    RestoreOutcome.PENDING -> "Your payment is still processing in Google Play."
    RestoreOutcome.RESTORED -> "Purchase restored."
}

@Composable
private fun StatusText(text: String, error: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun SheetButton(label: String, enabled: Boolean, primary: Boolean, onClick: () -> Unit) {
    val bg = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) bg else bg.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}
