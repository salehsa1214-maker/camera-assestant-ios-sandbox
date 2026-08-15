package app.dyrecto.billing

/**
 * Pure state reducer for the Premium entitlement. All Play/billing signals funnel through
 * [reduce]; the Android layer ([EntitlementProvider]/[BillingManager]) only produces events and
 * executes the returned effects. Keeping this pure makes the cache/downgrade semantics
 * JVM-unit-testable without a billing client.
 *
 * Semantics (locked in the plan):
 *  - Cache writes happen only on authoritative Play signals, in both directions:
 *    PURCHASED callback / successful query with ownership → write true;
 *    successful query WITHOUT ownership → write false. Errors and PENDING never write.
 *  - [EntitlementState.Free] is authoritative only when produced by a successful query, so the
 *    Premium→Free cleanup edge can key on it.
 *  - [Event.CacheLoaded] only resolves [EntitlementState.Unknown]; it can never downgrade a
 *    state already established by Play (guards the Unknown→Premium(CACHE) startup path against
 *    races with an early query result).
 */
object EntitlementReconciler {

    sealed interface Event {
        /** Local DataStore cache read finished at startup. */
        data class CacheLoaded(val entitled: Boolean) : Event

        /** PurchasesUpdatedListener delivered a PURCHASED purchase for the product. */
        data object PurchaseCompleted : Event

        /** A purchases query returned OK. [owned] = product present in PURCHASED state. */
        data class QuerySuccess(val owned: Boolean) : Event

        /** A purchases query failed (network / service unavailable / error). */
        data object QueryFailure : Event
    }

    /** New state plus the cache write to perform (null = don't touch the cache). */
    data class Result(val state: EntitlementState, val writeCache: Boolean? = null)

    fun reduce(current: EntitlementState, event: Event): Result = when (event) {
        is Event.CacheLoaded ->
            if (current is EntitlementState.Unknown) {
                Result(
                    if (event.entitled) {
                        EntitlementState.Premium(EntitlementState.Premium.Source.CACHE)
                    } else {
                        EntitlementState.Free(authoritative = false)
                    }
                )
            } else {
                Result(current) // Play already spoke; a late cache read changes nothing.
            }

        Event.PurchaseCompleted ->
            Result(EntitlementState.Premium(EntitlementState.Premium.Source.PLAY), writeCache = true)

        is Event.QuerySuccess ->
            if (event.owned) {
                Result(
                    EntitlementState.Premium(EntitlementState.Premium.Source.PLAY),
                    writeCache = true,
                )
            } else {
                Result(EntitlementState.Free(authoritative = true), writeCache = false)
            }

        Event.QueryFailure -> Result(current) // never touches state or cache
    }

    /**
     * True when moving [previous] → [next] must run the Premium→Free cleanup (stop reference
     * monitoring, silence voice, drop overlays). Only an authoritative revocation qualifies —
     * never Unknown transitions or process recreation.
     */
    fun isDowngradeEdge(previous: EntitlementState, next: EntitlementState): Boolean =
        previous is EntitlementState.Premium &&
            next is EntitlementState.Free &&
            next.authoritative
}
