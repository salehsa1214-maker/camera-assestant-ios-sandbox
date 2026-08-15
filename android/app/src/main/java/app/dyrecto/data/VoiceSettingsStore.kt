package app.dyrecto.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.dyrecto.liveview.voice.VoiceCooldowns
import app.dyrecto.liveview.voice.VoiceMode
import app.dyrecto.liveview.voice.VoiceSettings
import app.dyrecto.liveview.voice.VoiceSpeechRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.voiceSettingsDataStore by preferencesDataStore(name = "voice_settings")

/**
 * Persists the Phase 14 [VoiceSettings] across restarts, backed by a Preferences DataStore.
 * Same defensive pattern as [AlertConfigStore]: enum reads fall back to the default on any
 * corrupt/unknown stored value, and every unset key resolves to the [VoiceSettings] default
 * (voice OFF), so a fresh install or failed read can never start speaking unexpectedly.
 */
class VoiceSettingsStore(context: Context) {

    private val dataStore = context.applicationContext.voiceSettingsDataStore

    val settings: Flow<VoiceSettings> = dataStore.data.map { prefs ->
        val def = VoiceSettings()
        VoiceSettings(
            enabled = prefs[KEY_ENABLED] ?: def.enabled,
            mode = prefs[KEY_MODE]
                ?.let { name -> runCatching { VoiceMode.valueOf(name) }.getOrNull() }
                ?: def.mode,
            speechRate = prefs[KEY_RATE]
                ?.let { name -> runCatching { VoiceSpeechRate.valueOf(name) }.getOrNull() }
                ?: def.speechRate,
            assistantReminderSeconds = prefs[KEY_REMINDER_SEC]
                ?.let { VoiceCooldowns.clampReminderSeconds(it) }
                ?: def.assistantReminderSeconds,
        )
    }

    suspend fun update(settings: VoiceSettings) {
        dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = settings.enabled
            prefs[KEY_MODE] = settings.mode.name
            prefs[KEY_RATE] = settings.speechRate.name
            prefs[KEY_REMINDER_SEC] =
                VoiceCooldowns.clampReminderSeconds(settings.assistantReminderSeconds)
        }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("voice.enabled")
        val KEY_MODE = stringPreferencesKey("voice.mode")
        val KEY_RATE = stringPreferencesKey("voice.rate")
        val KEY_REMINDER_SEC = intPreferencesKey("voice.assistantReminderSec")
    }
}
