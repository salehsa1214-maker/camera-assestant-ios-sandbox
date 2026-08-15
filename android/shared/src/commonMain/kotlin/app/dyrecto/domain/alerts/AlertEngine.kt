package app.dyrecto.domain.alerts

import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.platform.epochMillis
import app.dyrecto.domain.CameraTelemetry

/**
 * Pure, platform-agnostic detector that turns a stream of [CameraConnectionState] snapshots into
 * [Alert]s. It observes telemetry transitions only — an alert fires once per meaningful state
 * change, never repeatedly on unchanged refreshes.
 *
 * Stateful by design: it remembers the previous relevant inputs (and a few latches) between
 * [evaluate] calls. It holds no Android types and never touches the protocol layer, so it is fully
 * unit-testable. Generation is decoupled from history ([AlertStore]) and delivery ([AlertFeedback]);
 * future delivery channels plug in at the call site, not here.
 *
 * Lifecycle is session-owned: callers invoke [reset] at the start of a new connection session.
 *
 * Alert ids come from a shared [AlertIdGenerator] so this engine and other producers (e.g.
 * [SceneAlertRules]) draw from one globally-ordered id space; the generator's own lifecycle is owned
 * by the coordinator, not by [reset].
 */
class AlertEngine(private val idGen: AlertIdGenerator = AlertIdGenerator()) {

    /** Relevant slice of a [CameraConnectionState], extracted for transition detection. */
    data class AlertInputs(
        val recording: Boolean?,
        val batteryPct: Int?,
        val batteryMinutes: Int?,
        /** Raw Sony slot status: 1=OK, 2=no card, 3=card error, 4=locked/DB error, null=unknown. */
        val slot1Status: Int?,
        val slot2Status: Int?,
        val overheating: Boolean?,
        val connected: Boolean,
    )

    private var previous: AlertInputs? = null

    // Battery % anti-spam latches: each threshold fires once until the level recovers above it.
    private var below20Fired = false
    private var below10Fired = false
    private var below5Fired = false

    // Battery minutes latches: same single-fire pattern as the % latches.
    private var minutesBelow10Fired = false
    private var minutesBelow5Fired = false

    // Connection Lost is only eligible once a successful connection has happened this session.
    private var everConnected = false

    /**
     * Clears all remembered state. Call when a new connection session begins. The shared
     * [AlertIdGenerator] is intentionally NOT reset here — the coordinator owns its lifecycle so all
     * producers stay on one id sequence.
     */
    fun reset() {
        previous = null
        below20Fired = false
        below10Fired = false
        below5Fired = false
        minutesBelow10Fired = false
        minutesBelow5Fired = false
        everConnected = false
    }

    /**
     * Diffs [state] against the previously seen inputs and returns any alerts the transition
     * produced (empty when nothing meaningful changed). The first call after construction/[reset]
     * only seeds the baseline — it never fires alerts, avoiding a burst at connect time.
     */
    fun evaluate(state: CameraConnectionState): List<Alert> {
        val now = state.lastTelemetryUpdateAt ?: epochMillis()
        val current = extractInputs(state)

        if (current.connected) everConnected = true

        val prev = previous
        previous = current

        // First snapshot: seed baseline only.
        if (prev == null) {
            seedBatteryLatches(current.batteryPct, current.batteryMinutes)
            return emptyList()
        }

        val alerts = mutableListOf<Alert>()

        // ---- Recording ----
        if (prev.recording == false && current.recording == true) {
            alerts += alert(AlertType.RECORDING_STARTED, "Recording in progress", now)
        } else if (prev.recording == true && current.recording == false) {
            alerts += alert(AlertType.RECORDING_STOPPED, "Recording has stopped", now)
        }

        // ---- Media (per-slot) ----
        addCardAlerts(prev.slot1Status, current.slot1Status, slot = 1, now, alerts)
        addCardAlerts(prev.slot2Status, current.slot2Status, slot = 2, now, alerts)

        // ---- Thermal ----
        if (prev.overheating == false && current.overheating == true) {
            alerts += alert(AlertType.OVERHEATING, "Camera is overheating", now)
        }

        // ---- Connection ----
        if (everConnected && prev.connected && !current.connected) {
            alerts += alert(AlertType.CONNECTION_LOST, "Lost connection to the camera", now)
        }

        // ---- Battery (latched thresholds) ----
        addBatteryAlerts(current.batteryPct, current.batteryMinutes, now, alerts)

        return alerts
    }

    private fun addCardAlerts(
        prev: Int?,
        current: Int?,
        slot: Int,
        now: Long,
        out: MutableList<Alert>,
    ) {
        if (prev == null || current == null || prev == current) return
        val prevOk = prev == 1
        val prevAbsent = prev == 2
        val prevError = prev == 3 || prev == 4
        val currentOk = current == 1
        val currentAbsent = current == 2

        when {
            // Card physically removed (OK → absent, or error → absent)
            (prevOk || prevError) && currentAbsent ->
                out += alert(AlertType.CARD_REMOVED, "Slot $slot", now)

            // Card inserted and working (absent → OK, or error → OK)
            (prevAbsent || prevError) && currentOk ->
                out += alert(AlertType.CARD_INSERTED, "Slot $slot", now)

            // A card entering an error state while still inserted no longer raises its own alert.
        }
    }

    private fun addBatteryAlerts(pct: Int?, minutes: Int?, now: Long, out: MutableList<Alert>) {
        // ---- Battery % ladder ----
        if (pct != null) {
            // Recovery re-arms each latch so a recharge can alert again on the next drop.
            if (pct > 20) below20Fired = false
            if (pct > 10) below10Fired = false
            if (pct > 5) below5Fired = false

            when {
                pct <= 5 && !below5Fired -> {
                    below5Fired = true
                    below10Fired = true // crossing 5 implies already below 10 and 20
                    below20Fired = true
                    out += alert(AlertType.BATTERY_LOW_5, "Battery at $pct%", now)
                }
                pct <= 10 && !below10Fired -> {
                    below10Fired = true
                    below20Fired = true
                    out += alert(AlertType.BATTERY_LOW_10, "Battery at $pct%", now)
                }
                pct <= 20 && !below20Fired -> {
                    below20Fired = true
                    out += alert(AlertType.BATTERY_LOW_20, "Battery at $pct%", now)
                }
            }
        }

        // ---- Battery minutes ladder ----
        if (minutes != null) {
            if (minutes > 10) minutesBelow10Fired = false
            if (minutes > 5) minutesBelow5Fired = false

            when {
                minutes <= 5 && !minutesBelow5Fired -> {
                    minutesBelow5Fired = true
                    minutesBelow10Fired = true
                    out += alert(AlertType.BATTERY_TIME_5_MIN, "$minutes min remaining", now)
                }
                minutes <= 10 && !minutesBelow10Fired -> {
                    minutesBelow10Fired = true
                    out += alert(AlertType.BATTERY_TIME_10_MIN, "$minutes min remaining", now)
                }
            }
        }
    }

    /** Seeds battery latches from the first-seen levels so a starting-low battery doesn't fire. */
    private fun seedBatteryLatches(pct: Int?, minutes: Int?) {
        if (pct != null) {
            if (pct <= 20) below20Fired = true
            if (pct <= 10) below10Fired = true
            if (pct <= 5) below5Fired = true
        }
        if (minutes != null) {
            if (minutes <= 10) minutesBelow10Fired = true
            if (minutes <= 5) minutesBelow5Fired = true
        }
    }

    private fun alert(type: AlertType, message: String, now: Long): Alert =
        Alert(
            id = idGen.next(),
            type = type,
            severity = type.defaultSeverity,
            title = type.title,
            message = message,
            timestamp = now,
        )

    private fun extractInputs(state: CameraConnectionState): AlertInputs {
        val t = state.telemetry
        return AlertInputs(
            recording = boolFrom(t, CameraTelemetry.MOVIE_REC) { it == 1L },
            batteryPct = numberFrom(t, CameraTelemetry.BATTERY)?.toInt(),
            batteryMinutes = numberFrom(t, CameraTelemetry.BATTERY_MINUTES)?.toInt(),
            slot1Status = numberFrom(t, CameraTelemetry.SLOT1_STATUS)?.toInt(),
            slot2Status = numberFrom(t, CameraTelemetry.SLOT2_STATUS)?.toInt(),
            overheating = boolFrom(t, CameraTelemetry.OVERHEATING) { it == 2L },
            connected = state.isFullyConnected,
        )
    }

    private fun numberFrom(t: CameraTelemetry?, code: Int): Long? = t?.get(code)?.rawNumber

    /** Maps a property's raw number through [predicate]; null when the property is absent. */
    private fun boolFrom(t: CameraTelemetry?, code: Int, predicate: (Long) -> Boolean): Boolean? =
        numberFrom(t, code)?.let(predicate)
}
