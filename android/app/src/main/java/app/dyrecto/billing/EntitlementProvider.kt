package app.dyrecto.billing

import android.app.Activity
import android.content.Context
import app.dyrecto.BuildConfig
import app.dyrecto.data.EntitlementStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * The single process-scoped source of truth for Dyrecto Premium. Wires
 * [BillingManager] → [EntitlementReconciler] → [EntitlementState], with the offline cache in
 * [EntitlementStore]. Initialized once from `DyrectoApp.onCreate`; UI reads [isPremium] /
 * [state] via `collectAsState`, engine seams read [isPremium].value.
 *
 * Debug-only entitlement override: the branch is on the compile-time constant
 * [BuildConfig.DEBUG], so R8 strips the override path from release builds entirely — the toggle
 * UI additionally lives only on the Developer screen (route unregistered in release).
 */
object EntitlementProvider {

    enum class RestoreOutcome { UNAVAILABLE, NETWORK_ERROR, NONE_FOUND, PENDING, RESTORED }

    /** Debug builds only: null = real state, true/false = forced Premium/Free. */
    val debugOverride = MutableStateFlow<Boolean?>(null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<EntitlementState>(EntitlementState.Unknown)
    val state: StateFlow<EntitlementState> = _state.asStateFlow()

    private val _purchasePending = MutableStateFlow(false)
    val purchasePending: StateFlow<Boolean> = _purchasePending.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    lateinit var isPremium: StateFlow<Boolean>
        private set

    private lateinit var store: EntitlementStore
    private lateinit var billing: BillingManager

    /** Fires exactly once per authoritative Premium→Free transition (refund/revocation). */
    private val _downgradeEdge = MutableStateFlow(0L)
    val downgradeEdge: StateFlow<Long> = _downgradeEdge.asStateFlow()

    val formattedPrice: StateFlow<String?> get() = billing.formattedPrice
    val billingUnavailable: StateFlow<Boolean> get() = billing.billingUnavailable

    fun init(context: Context) {
        val app = context.applicationContext
        store = EntitlementStore(app)
        billing = BillingManager(app, scope, sink)

        isPremium = if (BuildConfig.DEBUG) {
            combine(_state, debugOverride) { real, override -> override ?: real.isPremium }
        } else {
            _state.map { it.isPremium }
        }.stateIn(scope, SharingStarted.Eagerly, false)

        scope.launch {
            // Cache first (instant offline entitlement), then reconcile against Play.
            apply(EntitlementReconciler.Event.CacheLoaded(store.cachedEntitled.first()))
            billing.connect()
        }
    }

    /** ON_RESUME hook: re-run the ownership query + acknowledge sweep. */
    fun refresh() {
        if (!::billing.isInitialized) return
        billing.refreshPurchases()
        if (billing.formattedPrice.value == null) billing.refreshProductDetails()
    }

    fun launchPurchase(activity: Activity) {
        _lastError.value = null
        billing.launchPurchase(activity)
    }

    fun clearError() {
        _lastError.value = null
    }

    /**
     * Explicit Restore Purchases with a distinct outcome per state (never conflated). Runs one
     * query and interprets the next authoritative/failed signal.
     */
    suspend fun restore(): RestoreOutcome {
        if (billing.billingUnavailable.value) return RestoreOutcome.UNAVAILABLE
        val outcome = withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                restoreWaiter = { cont.takeIf { c -> c.isActive }?.resume(it) }
                billing.refreshPurchases()
            }
        }
        restoreWaiter = null
        return outcome ?: RestoreOutcome.NETWORK_ERROR
    }

    @Volatile
    private var restoreWaiter: ((RestoreOutcome) -> Unit)? = null

    private val sink = object : BillingManager.Sink {
        override fun onPurchaseCompleted() {
            _purchasePending.value = false
            scope.launch {
                // Cache written BEFORE state emission: a kill right after purchase still
                // cold-starts as Premium(CACHE).
                apply(EntitlementReconciler.Event.PurchaseCompleted)
            }
        }

        override fun onQuerySuccess(owned: Boolean, pending: Boolean) {
            _purchasePending.value = pending && !owned
            scope.launch {
                apply(EntitlementReconciler.Event.QuerySuccess(owned))
                restoreWaiter?.invoke(
                    when {
                        owned -> RestoreOutcome.RESTORED
                        pending -> RestoreOutcome.PENDING
                        else -> RestoreOutcome.NONE_FOUND
                    }
                )
            }
        }

        override fun onQueryFailure() {
            scope.launch {
                apply(EntitlementReconciler.Event.QueryFailure)
                restoreWaiter?.invoke(
                    if (billing.billingUnavailable.value) RestoreOutcome.UNAVAILABLE
                    else RestoreOutcome.NETWORK_ERROR
                )
            }
        }

        override fun onPurchasePending() {
            _purchasePending.value = true
        }

        override fun onPurchaseError(message: String) {
            _lastError.value = message
        }
    }

    private suspend fun apply(event: EntitlementReconciler.Event) {
        val previous = _state.value
        val result = EntitlementReconciler.reduce(previous, event)
        result.writeCache?.let { store.setEntitled(it) }
        _state.value = result.state
        if (EntitlementReconciler.isDowngradeEdge(previous, result.state)) {
            _downgradeEdge.value = _downgradeEdge.value + 1
        }
    }
}
