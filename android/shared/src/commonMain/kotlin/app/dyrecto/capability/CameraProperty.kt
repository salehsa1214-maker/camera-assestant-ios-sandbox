package app.dyrecto.capability

import kotlinx.serialization.Serializable

/**
 * The set of legal values a property may take, as reported by the camera's own capability form
 * (PTP `FORM_RANGE` / `FORM_ENUM`). [None] means the camera advertised no constraint.
 *
 * Values are carried as raw `Long`s (the on-wire numeric encoding); interpretation into
 * human units is a presentation concern and lives outside the capability layer.
 */
@Serializable
sealed interface PropertyValueSet {
    @Serializable
    data object None : PropertyValueSet

    /** Inclusive numeric range with a step, from a PTP `FORM_RANGE`. */
    @Serializable
    data class Range(val min: Long, val max: Long, val step: Long) : PropertyValueSet {
        /** True when [value] lies on the advertised grid (min..max, aligned to step). */
        fun contains(value: Long): Boolean {
            if (value < min || value > max) return false
            if (step <= 0L) return true
            return (value - min) % step == 0L
        }
    }

    /** Explicit list of supported values, from a PTP `FORM_ENUM`. */
    @Serializable
    data class Enum(val values: List<Long>) : PropertyValueSet {
        fun contains(value: Long): Boolean = values.contains(value)
    }

    /** True when [value] is permitted by this value set (always true for [None]). */
    fun permits(value: Long): Boolean = when (this) {
        is None -> true
        is Range -> contains(value)
        is Enum -> contains(value)
    }
}

/**
 * A single normalized camera property — the low-level state/writable-setting model, built at
 * runtime from one PTP `GetAllExtDevicePropInfo` (0x9209) record. Camera-agnostic: nothing here
 * knows it came from Sony.
 *
 * Forward-compatibility (Rule 3): a property whose [code] is not in the app's known table is still
 * fully preserved here with [known] == false and a synthesized [label]; it is never discarded.
 */
@Serializable
data class CameraProperty(
    /** PTP device-property code (e.g. 0xD21E = ISO). */
    val code: Int,
    /** PTP datatype code (ISO 15740), as reported. */
    val dataType: Int,
    /** Numeric current value when the property is numeric; null for string-typed values. */
    val currentRaw: Long?,
    /** String current value when the property is string-typed (PTP `DT_STR`); else null. */
    val currentText: String? = null,
    /** True when the camera reports the property as settable (PTP get/set flag == 1). */
    val writable: Boolean,
    /** True when the property is usable in the camera's current state (availability flag != 0). */
    val available: Boolean,
    /** Legal values advertised by the camera's capability form. */
    val valueSet: PropertyValueSet = PropertyValueSet.None,
    /** Human-readable label; synthesized as `0x####` when [known] is false. */
    val label: String,
    /** False when [code] is not recognized by the app's label table (still fully preserved). */
    val known: Boolean = true,
    /** Raw get/set flag as reported, kept for diagnostics. */
    val getSetRaw: Int = 0,
    /** Raw availability flag as reported, kept for diagnostics. */
    val availabilityRaw: Int = 0,
)
