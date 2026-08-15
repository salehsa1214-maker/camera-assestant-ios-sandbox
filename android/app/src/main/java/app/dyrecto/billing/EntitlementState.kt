package app.dyrecto.billing

/**
 * Dyrecto Premium entitlement, derived from Google Play ownership of the one-time
 * non-consumable product [BillingManager.PRODUCT_ID].
 *
 * State discipline (see plan / CLAUDE.md):
 *  - [Unknown] exists only between process start and the first local cache read. The UI treats it
 *    like "don't show premium-specific chrome yet"; it is never a downgrade source.
 *  - [Premium] carries its evidence source. CACHE means "last authoritative Play signal said
 *    owned"; PLAY means "a successful query this process confirmed ownership".
 *  - [Free.authoritative] is true only when a *successful* Play purchases query reported no
 *    ownership. The Premium→Free cleanup edge fires ONLY on an authoritative Free, so process
 *    recreation (Unknown → Premium(CACHE)) or transient billing errors can never trigger it.
 */
sealed interface EntitlementState {
    data object Unknown : EntitlementState

    data class Free(val authoritative: Boolean) : EntitlementState

    data class Premium(val source: Source) : EntitlementState {
        enum class Source { PLAY, CACHE }
    }

    val isPremium: Boolean get() = this is Premium
}
