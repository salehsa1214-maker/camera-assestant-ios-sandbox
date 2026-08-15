package app.dyrecto.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.dyrecto.domain.alerts.AlertConfig
import app.dyrecto.domain.alerts.AlertPattern
import app.dyrecto.domain.alerts.AlertSeverity
import app.dyrecto.domain.alerts.AlertType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.alertConfigDataStore by preferencesDataStore(name = "alert_config")

/**
 * Persists per-[AlertType] [AlertConfig] across app restarts, backed by a Preferences DataStore.
 *
 * Design goals:
 *  - **Automatic discovery:** [configs] always emits an entry for every [AlertType], falling back to
 *    [AlertConfig.default] for any unset key. A newly added alert type needs no migration — it shows
 *    up with its defaults immediately.
 *  - **Defensive reads:** sound/vibration counts are rebuilt with the clamping [AlertPattern.of], and
 *    severity parsing falls back to the default, so corrupt or out-of-range stored values can never
 *    crash the read path or enter the system.
 *  - **Future-friendly:** new per-alert settings are added as one more key + one more default, with
 *    no schema rewrite.
 */
class AlertConfigStore(context: Context) {

    private val dataStore = context.applicationContext.alertConfigDataStore

    /** Every [AlertType], with persisted overrides applied over [AlertConfig.default]. */
    val configs: Flow<Map<AlertType, AlertConfig>> = dataStore.data.map { prefs ->
        AlertType.entries.associateWith { type ->
            val def = AlertConfig.default(type)
            val enabled = prefs[enabledKey(type)] ?: def.enabled
            val severity = prefs[severityKey(type)]
                ?.let { name -> runCatching { AlertSeverity.valueOf(name) }.getOrNull() }
                ?: def.severity
            val sound = prefs[soundKey(type)]?.let { AlertPattern.of(it) } ?: def.soundPattern
            val vibe = prefs[vibeKey(type)]?.let { AlertPattern.of(it) } ?: def.vibrationPattern
            AlertConfig(
                alertType = type,
                enabled = enabled,
                severity = severity,
                soundPattern = sound,
                vibrationPattern = vibe,
            )
        }
    }

    /** Persists [config], overwriting the four stored fields for its alert type. */
    suspend fun update(config: AlertConfig) {
        val type = config.alertType
        dataStore.edit { prefs ->
            prefs[enabledKey(type)] = config.enabled
            prefs[severityKey(type)] = config.severity.name
            prefs[soundKey(type)] = config.soundPattern.count
            prefs[vibeKey(type)] = config.vibrationPattern.count
        }
    }

    /** Drops the stored overrides for [type] so it reverts to [AlertConfig.default]. */
    suspend fun restoreDefault(type: AlertType) {
        dataStore.edit { prefs ->
            prefs.remove(enabledKey(type))
            prefs.remove(severityKey(type))
            prefs.remove(soundKey(type))
            prefs.remove(vibeKey(type))
        }
    }

    /** Clears all stored overrides so every alert type reverts to its default. */
    suspend fun restoreAllDefaults() {
        dataStore.edit { it.clear() }
    }

    private fun enabledKey(type: AlertType) = booleanPreferencesKey("${type.name}.enabled")
    private fun severityKey(type: AlertType) = stringPreferencesKey("${type.name}.severity")
    private fun soundKey(type: AlertType) = intPreferencesKey("${type.name}.sound")
    private fun vibeKey(type: AlertType) = intPreferencesKey("${type.name}.vibe")
}
