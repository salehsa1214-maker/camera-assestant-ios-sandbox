package app.dyrecto.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Thin wrapper around the Play Billing client for the single one-time non-consumable
 * [PRODUCT_ID]. Owns connection lifecycle, product-details lookup, the purchase flow, the
 * acknowledge sweep, and purchase queries — and reports every *authoritative* signal to [sink]
 * (which is [EntitlementProvider] feeding [EntitlementReconciler]). No entitlement decisions are
 * made here.
 */
class BillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val sink: Sink,
) : PurchasesUpdatedListener {

    /** Authoritative billing signals, consumed by [EntitlementProvider]. */
    interface Sink {
        /** A PURCHASED purchase for [PRODUCT_ID] arrived (purchase flow or query). */
        fun onPurchaseCompleted()

        /** A purchases query returned OK. [owned] = PURCHASED purchase present. */
        fun onQuerySuccess(owned: Boolean, pending: Boolean)

        /** A purchases query failed; state/cache must not change. */
        fun onQueryFailure()

        /** The user's purchase is PENDING (never entitles). */
        fun onPurchasePending()

        /** Transient purchase-flow error worth surfacing (never USER_CANCELED). */
        fun onPurchaseError(message: String)
    }

    private val appContext = context.applicationContext

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    /** Localized price of the buy offer, or null while unknown/unavailable. */
    private val _formattedPrice = MutableStateFlow<String?>(null)
    val formattedPrice: StateFlow<String?> = _formattedPrice.asStateFlow()

    /** True when setup reported BILLING_UNAVAILABLE / no Play services. */
    private val _billingUnavailable = MutableStateFlow(false)
    val billingUnavailable: StateFlow<Boolean> = _billingUnavailable.asStateFlow()

    private val connecting = AtomicBoolean(false)
    @Volatile private var retryDelayMs = 1_000L

    fun connect() {
        if (client.isReady || !connecting.compareAndSet(false, true)) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting.set(false)
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    retryDelayMs = 1_000L
                    _billingUnavailable.value = false
                    refreshProductDetails()
                    refreshPurchases()
                } else {
                    if (result.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                        _billingUnavailable.value = true
                    }
                    scheduleRetry()
                }
            }

            override fun onBillingServiceDisconnected() {
                // enableAutoServiceReconnection handles re-establishing the connection.
                connecting.set(false)
            }
        })
    }

    private fun scheduleRetry() {
        val delayMs = retryDelayMs
        retryDelayMs = (retryDelayMs * 2).coerceAtMost(32_000L)
        scope.launch {
            delay(delayMs)
            connect()
        }
    }

    /** Re-query product details (price). Safe to call any time; no-ops until connected. */
    fun refreshProductDetails() {
        if (!client.isReady) return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        client.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = detailsResult.productDetailsList.firstOrNull()
                _productDetails.value = details
                _formattedPrice.value = details?.let { selectBuyOffer(it)?.formattedPrice }
            }
        }
    }

    /**
     * Query current ownership and report the outcome to [sink]. Also runs the acknowledge sweep
     * (any PURCHASED-but-unacknowledged purchase gets acknowledged; retried on every call, which
     * covers Play's 3-day acknowledgement window as long as the app is opened).
     */
    fun refreshPurchases() {
        if (!client.isReady) {
            connect()
            sink.onQueryFailure()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                sink.onQueryFailure()
                return@queryPurchasesAsync
            }
            val ours = purchases.filter { PRODUCT_ID in it.products }
            val owned = ours.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            val pending = ours.any { it.purchaseState == Purchase.PurchaseState.PENDING }
            ours.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                .forEach(::acknowledge)
            sink.onQuerySuccess(owned = owned, pending = pending)
        }
    }

    /** Launch the Play purchase flow for [PRODUCT_ID]. */
    fun launchPurchase(activity: Activity) {
        val details = _productDetails.value
        if (details == null || !client.isReady) {
            connect()
            refreshProductDetails()
            sink.onPurchaseError("Google Play isn't ready yet. Check your connection and try again.")
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply {
                // Billing 8: one-time products may carry multiple purchase options/offers.
                // Pass the selected buy offer's token when one exists; the single-option
                // no-offer catalog shape needs none.
                selectBuyOffer(details)?.offerToken
                    ?.takeIf { it.isNotBlank() }
                    ?.let { setOfferToken(it) }
            }
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            handleFlowError(result)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val ours = purchases.orEmpty().filter { PRODUCT_ID in it.products }
                when {
                    ours.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> {
                        ours.filter {
                            it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged
                        }.forEach(::acknowledge)
                        sink.onPurchaseCompleted()
                    }
                    ours.any { it.purchaseState == Purchase.PurchaseState.PENDING } ->
                        sink.onPurchasePending()
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refreshPurchases()
            else -> handleFlowError(result)
        }
    }

    private fun handleFlowError(result: BillingResult) {
        Log.w(TAG, "Purchase flow error ${result.responseCode}: ${result.debugMessage}")
        sink.onPurchaseError(
            when (result.responseCode) {
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                    "Google Play billing isn't available on this device."
                BillingClient.BillingResponseCode.NETWORK_ERROR,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
                    "Couldn't reach Google Play. Check your connection and try again."
                else -> "The purchase couldn't be completed. Please try again."
            }
        )
    }

    private fun acknowledge(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                // Retried automatically by the sweep in refreshPurchases() on next connect/resume.
                Log.w(TAG, "acknowledge failed ${result.responseCode}: ${result.debugMessage}")
            }
        }
    }

    /**
     * Pick the buy offer to display/purchase. Billing 8 exposes a list of one-time purchase
     * offers; prefer a discounted offer Play marked the user eligible for, else the base offer.
     * Falls back to the legacy single-offer accessor when the list is empty (older catalog shape).
     */
    private fun selectBuyOffer(details: ProductDetails): ProductDetails.OneTimePurchaseOfferDetails? {
        val offers = details.oneTimePurchaseOfferDetailsList
        if (offers.isNullOrEmpty()) {
            @Suppress("DEPRECATION")
            return details.oneTimePurchaseOfferDetails
        }
        // Eligible discount offers (Play only returns offers the user can use) sort before the
        // base offer by price so the user always sees the best price they qualify for.
        return offers.minByOrNull { it.priceAmountMicros }
    }

    companion object {
        const val PRODUCT_ID = "dyrecto_unlock"
        private const val TAG = "BillingManager"
    }
}
