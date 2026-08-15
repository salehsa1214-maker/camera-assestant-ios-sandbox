package app.dyrecto.domain.alerts

/**
 * A reusable delivery pattern: how many times a channel fires ([count]) and the gap between
 * repetitions ([intervalMs]). Today the UI only exposes [count] (0–10 beeps / pulses); [intervalMs]
 * carries a sensible default and is reserved for future expansion (short-short-long sequences,
 * voice, custom rhythms) without forcing a model redesign.
 *
 * Validation lives here, at the model level — not only in the sliders. The primary [constructor]
 * enforces the supported range so an out-of-range value can never enter the system through a future
 * import, API, cloud sync, or migration. Use [of] on any untrusted/persisted input: it clamps into
 * range instead of throwing, so corrupt stored data degrades gracefully rather than crashing.
 */
data class AlertPattern(val count: Int, val intervalMs: Long = DEFAULT_INTERVAL_MS) {

    init {
        require(count in MIN_COUNT..MAX_COUNT) {
            "count $count out of range $MIN_COUNT..$MAX_COUNT"
        }
        require(intervalMs in MIN_INTERVAL_MS..MAX_INTERVAL_MS) {
            "intervalMs $intervalMs out of range $MIN_INTERVAL_MS..$MAX_INTERVAL_MS"
        }
    }

    companion object {
        const val MIN_COUNT = 0
        const val MAX_COUNT = 10
        const val MIN_INTERVAL_MS = 0L
        const val MAX_INTERVAL_MS = 5_000L
        const val DEFAULT_INTERVAL_MS = 250L

        /** A silent/no-op pattern. */
        val NONE = AlertPattern(0)

        /**
         * Lenient factory for untrusted input (persisted prefs, imports, sync, migrations): clamps
         * [count] and [intervalMs] into the supported range instead of throwing.
         */
        fun of(count: Int, intervalMs: Long = DEFAULT_INTERVAL_MS): AlertPattern =
            AlertPattern(
                count.coerceIn(MIN_COUNT, MAX_COUNT),
                intervalMs.coerceIn(MIN_INTERVAL_MS, MAX_INTERVAL_MS),
            )
    }
}
