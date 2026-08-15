package app.dyrecto.liveview.reference

import app.dyrecto.domain.alerts.Alert
import app.dyrecto.domain.alerts.AlertIdGenerator
import app.dyrecto.domain.alerts.AlertType

/**
 * Pure, stateless translator of confirmed Shot Reference drift transitions into [Alert]s
 * (Phase 9). Sibling of `ExposureAlertRules`/`SceneAlertRules` — same single-fire latch idiom,
 * same shared [AlertIdGenerator] — so reference alerts flow through the *one* existing alert
 * lifecycle (store, config gate, feedback, notification). No second alert system.
 *
 * Behavior:
 *  - a drift alert fires exactly once, on the edge into [ReferenceSignalResult.confirmedDrift];
 *    it never repeats while the drift stays confirmed;
 *  - when a fired signal recovers, one [AlertType.REFERENCE_RECOVERED] fires with a
 *    signal-specific message — or a single grouped "Reference match restored." when several
 *    signals recover in the same tick (no recovery spam);
 *  - recovery never fires for a drift that was never confirmed;
 *  - when monitoring goes inactive, latches clear silently (no alerts).
 *
 * The first active tick only seeds the latches from the current confirmed state, so starting
 * monitoring against an already-drifted scene doesn't fire a burst — it fires on the *next*
 * confirmed transition, mirroring `ExposureAlertRules`.
 *
 * Phase 12: [ReferenceSignalResult.confirmedDrift] is now a PERCEPTUAL decision — the Human
 * Perception Layer replaced `matched` upstream, and [ReferenceSignalResult.message] already
 * carries severity wording ("noticeably brighter"). This translator stays raw-value-free.
 */
object ReferenceAlertRules {

    /** New alerts produced this tick plus the [ReferenceAlertState] to carry into the next call. */
    data class Result(val alerts: List<Alert>, val state: ReferenceAlertState)

    fun evaluate(
        state: ReferenceAlertState,
        match: ReferenceMatchResult,
        idGen: AlertIdGenerator,
    ): Result {
        if (!match.active) {
            // Monitoring stopped (or paused): clear latches silently.
            return Result(emptyList(), ReferenceAlertState())
        }

        if (!state.seeded || state.referenceId != match.referenceId) {
            // First tick for this reference: seed latches, never fire.
            return Result(
                emptyList(),
                ReferenceAlertState(
                    seeded = true,
                    referenceId = match.referenceId,
                    fired = ReferenceSignal.entries
                        .filter { match.signal(it).confirmedDrift }
                        .toSet(),
                ),
            )
        }

        val now = match.updatedAtMs
        val alerts = mutableListOf<Alert>()
        val fired = state.fired.toMutableSet()
        val recovered = mutableListOf<ReferenceSignal>()

        for (signal in ReferenceSignal.entries) {
            val s = match.signal(signal)
            if (s.confirmedDrift) {
                if (signal !in fired) {
                    fired += signal
                    // Phase 13: alerts speak in operator instructions when a translation exists;
                    // the perceptual description remains the fallback (and lives on in s.message).
                    val text = s.instruction?.message ?: s.message
                    alerts += alert(driftType(signal), text.ifBlank { driftType(signal).title }, now, idGen)
                }
            } else if (signal in fired && s.enabled && s.state == ReferenceSignalState.NORMAL) {
                // Recovery is the confirmed edge back to NORMAL (never fires mid-RECOVERING and
                // never for a drift that was not confirmed — the latch guarantees it was).
                fired -= signal
                recovered += signal
            }
        }

        when {
            recovered.size == 1 ->
                alerts += alert(AlertType.REFERENCE_RECOVERED, recoveryMessage(recovered[0]), now, idGen)
            recovered.size > 1 ->
                alerts += alert(AlertType.REFERENCE_RECOVERED, "Reference match restored.", now, idGen)
        }

        return Result(
            alerts = alerts,
            state = ReferenceAlertState(seeded = true, referenceId = state.referenceId, fired = fired),
        )
    }

    private fun driftType(signal: ReferenceSignal): AlertType = when (signal) {
        ReferenceSignal.EXPOSURE -> AlertType.REFERENCE_EXPOSURE_DRIFT
        ReferenceSignal.WHITE_BALANCE -> AlertType.REFERENCE_WHITE_BALANCE_DRIFT
        ReferenceSignal.SUBJECT_POSITION -> AlertType.REFERENCE_SUBJECT_POSITION_DRIFT
        ReferenceSignal.SUBJECT_SIZE -> AlertType.REFERENCE_SUBJECT_SIZE_DRIFT
        ReferenceSignal.HEADROOM -> AlertType.REFERENCE_HEADROOM_DRIFT
        ReferenceSignal.FACE_PRESENCE -> AlertType.REFERENCE_FACE_MISSING
        ReferenceSignal.EYE_VISIBILITY -> AlertType.REFERENCE_EYES_MISSING
        ReferenceSignal.SUBJECT_PRESENCE -> AlertType.REFERENCE_SUBJECT_MISSING
        ReferenceSignal.COMPOSITION -> AlertType.REFERENCE_COMPOSITION_DRIFT
        ReferenceSignal.VISUAL_SIMILARITY -> AlertType.REFERENCE_VISUAL_MISMATCH
    }

    private fun recoveryMessage(signal: ReferenceSignal): String = when (signal) {
        ReferenceSignal.EXPOSURE -> "Reference exposure restored."
        ReferenceSignal.WHITE_BALANCE -> "White balance restored."
        ReferenceSignal.SUBJECT_POSITION -> "Subject position restored."
        ReferenceSignal.SUBJECT_SIZE -> "Subject size restored."
        ReferenceSignal.HEADROOM -> "Headroom restored."
        ReferenceSignal.FACE_PRESENCE -> "Face is visible again."
        ReferenceSignal.EYE_VISIBILITY -> "Eyes are visible again."
        ReferenceSignal.SUBJECT_PRESENCE -> "Reference subject is back."
        ReferenceSignal.COMPOSITION -> "Composition matches the reference again."
        ReferenceSignal.VISUAL_SIMILARITY -> "The scene matches the reference again."
    }

    private fun alert(type: AlertType, message: String, now: Long, idGen: AlertIdGenerator): Alert =
        Alert(
            id = idGen.next(),
            type = type,
            severity = type.defaultSeverity,
            title = type.title,
            message = message,
            timestamp = now,
        )
}

/**
 * Immutable lifecycle state for [ReferenceAlertRules], owned and threaded by the coordinator
 * (`DefaultMonitoringSession`). A fresh instance (all defaults) is the session-reset state.
 */
data class ReferenceAlertState(
    val seeded: Boolean = false,
    /** The reference the latches belong to — a new reference re-seeds instead of misfiring. */
    val referenceId: String? = null,
    /** Signals whose confirmed drift has already fired (single-fire latch per signal). */
    val fired: Set<ReferenceSignal> = emptySet(),
)
