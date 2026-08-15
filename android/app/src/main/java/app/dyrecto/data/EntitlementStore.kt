package app.dyrecto.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.entitlementDataStore by preferencesDataStore(name = "entitlement")

/**
 * Offline cache of the last authoritative Google Play ownership signal for Dyrecto Premium.
 *
 * Written only from authoritative Play signals (PURCHASED purchase-update callback, or a
 * *successful* purchases query — in both directions). Never written on errors, offline, or
 * PENDING, so a cached Premium survives connectivity loss and a refund clears it at the next
 * successful reconcile. An unset key resolves to `false` (fresh install starts Free).
 */
class EntitlementStore(context: Context) {

    private val dataStore = context.applicationContext.entitlementDataStore

    val cachedEntitled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ENTITLED] ?: false
    }

    suspend fun setEntitled(entitled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ENTITLED] = entitled }
    }

    private companion object {
        val KEY_ENTITLED = booleanPreferencesKey("entitlement.premiumOwned")
    }
}
