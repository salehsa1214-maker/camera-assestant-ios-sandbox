package app.dyrecto.debug

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory Developer Mode flags. Normal users never see packet dumps or secrets;
 * a developer enables these from the Developer screen. Process-scoped only
 * (resets on app restart) — deliberately not persisted, so secrets are never
 * written to disk.
 */
object DeveloperState {

    private val _developerMode = MutableStateFlow(false)
    val developerMode: StateFlow<Boolean> = _developerMode.asStateFlow()

    private val _revealSecrets = MutableStateFlow(false)
    val revealSecrets: StateFlow<Boolean> = _revealSecrets.asStateFlow()

    fun setDeveloperMode(enabled: Boolean) {
        _developerMode.value = enabled
        if (!enabled) _revealSecrets.value = false // revoking dev mode re-hides secrets
    }

    fun setRevealSecrets(reveal: Boolean) {
        // Secrets can only be revealed while Developer Mode is on.
        _revealSecrets.value = reveal && _developerMode.value
    }
}
