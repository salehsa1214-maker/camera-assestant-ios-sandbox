package app.dyrecto.billing

import app.dyrecto.billing.EntitlementReconciler.Event
import app.dyrecto.billing.EntitlementState.Free
import app.dyrecto.billing.EntitlementState.Premium
import app.dyrecto.billing.EntitlementState.Unknown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementReconcilerTest {

    private val premiumCache = Premium(Premium.Source.CACHE)
    private val premiumPlay = Premium(Premium.Source.PLAY)

    // --- startup / cache semantics ---

    @Test
    fun `cache loaded entitled resolves Unknown to Premium CACHE without cache write`() {
        val r = EntitlementReconciler.reduce(Unknown, Event.CacheLoaded(true))
        assertEquals(premiumCache, r.state)
        assertNull(r.writeCache)
    }

    @Test
    fun `cache loaded not entitled resolves Unknown to non-authoritative Free`() {
        val r = EntitlementReconciler.reduce(Unknown, Event.CacheLoaded(false))
        assertEquals(Free(authoritative = false), r.state)
        assertNull(r.writeCache)
    }

    @Test
    fun `late cache read never downgrades a state Play established`() {
        val r = EntitlementReconciler.reduce(premiumPlay, Event.CacheLoaded(false))
        assertEquals(premiumPlay, r.state)
        assertNull(r.writeCache)
    }

    // --- purchase callback ---

    @Test
    fun `PURCHASED callback entitles and writes cache true from any state`() {
        for (start in listOf<EntitlementState>(Unknown, Free(false), Free(true), premiumCache)) {
            val r = EntitlementReconciler.reduce(start, Event.PurchaseCompleted)
            assertEquals(premiumPlay, r.state)
            assertEquals(true, r.writeCache)
        }
    }

    // --- query outcomes ---

    @Test
    fun `successful query with ownership upgrades cache-premium to play-premium and rewrites cache`() {
        val r = EntitlementReconciler.reduce(premiumCache, Event.QuerySuccess(owned = true))
        assertEquals(premiumPlay, r.state)
        assertEquals(true, r.writeCache)
    }

    @Test
    fun `successful query without ownership revokes and clears cache`() {
        val r = EntitlementReconciler.reduce(premiumCache, Event.QuerySuccess(owned = false))
        assertEquals(Free(authoritative = true), r.state)
        assertEquals(false, r.writeCache)
    }

    @Test
    fun `query failure never changes state or cache`() {
        for (start in listOf<EntitlementState>(Unknown, Free(false), Free(true), premiumCache, premiumPlay)) {
            val r = EntitlementReconciler.reduce(start, Event.QueryFailure)
            assertEquals(start, r.state)
            assertNull(r.writeCache)
        }
    }

    // --- downgrade edge discipline ---

    @Test
    fun `downgrade edge fires only on authoritative Premium to Free`() {
        assertTrue(EntitlementReconciler.isDowngradeEdge(premiumCache, Free(true)))
        assertTrue(EntitlementReconciler.isDowngradeEdge(premiumPlay, Free(true)))
    }

    @Test
    fun `downgrade edge never fires on Unknown transitions or non-authoritative Free`() {
        assertFalse(EntitlementReconciler.isDowngradeEdge(Unknown, Free(false)))
        assertFalse(EntitlementReconciler.isDowngradeEdge(Unknown, premiumCache))
        assertFalse(EntitlementReconciler.isDowngradeEdge(premiumCache, Unknown))
        assertFalse(EntitlementReconciler.isDowngradeEdge(premiumCache, Free(false)))
        assertFalse(EntitlementReconciler.isDowngradeEdge(Free(true), Free(true)))
    }

    @Test
    fun `process recreation sequence Unknown to PremiumCache to PremiumPlay never crosses a downgrade`() {
        var state: EntitlementState = Unknown
        val seen = mutableListOf<Boolean>()
        for (event in listOf(Event.CacheLoaded(true), Event.QuerySuccess(owned = true))) {
            val r = EntitlementReconciler.reduce(state, event)
            seen += EntitlementReconciler.isDowngradeEdge(state, r.state)
            state = r.state
        }
        assertEquals(premiumPlay, state)
        assertFalse(seen.any { it })
    }

    @Test
    fun `refund reconcile sequence downgrades exactly once`() {
        var state: EntitlementState = Unknown
        var edges = 0
        for (event in listOf(
            Event.CacheLoaded(true),          // Premium(CACHE)
            Event.QueryFailure,               // offline — stays Premium(CACHE)
            Event.QuerySuccess(owned = false), // refund seen — Free(authoritative)
            Event.QuerySuccess(owned = false), // repeat query — no second edge
        )) {
            val r = EntitlementReconciler.reduce(state, event)
            if (EntitlementReconciler.isDowngradeEdge(state, r.state)) edges++
            state = r.state
        }
        assertEquals(Free(authoritative = true), state)
        assertEquals(1, edges)
    }
}
