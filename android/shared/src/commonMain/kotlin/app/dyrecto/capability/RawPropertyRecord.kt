package app.dyrecto.capability

/**
 * Portable, protocol-neutral representation of one raw device-property record as read off the wire
 * (PTP `GetAllExtDevicePropInfo` / 0x9209). The Sony adapter fills this from its own parsed record;
 * [CapabilityMapper] turns a list of these into a [CameraCapabilities] snapshot.
 *
 * Keeping this type in commonMain lets the mapping + feature resolution be unit-tested without any
 * Android/PTP transport, and keeps all Sony-specific parsing quarantined in the adapter.
 */
data class RawPropertyRecord(
    val code: Int,
    val dataType: Int,
    /** PTP get/set flag: 0 = read-only, 1 = read/write. */
    val getSet: Int,
    /** PTP availability/enabled flag: 0 = not settable in current state, non-zero = enabled. */
    val availability: Int,
    val currentRaw: Long?,
    val currentText: String? = null,
    val form: RawForm = RawForm.None,
)

/** The capability form that followed a property's current value on the wire. */
sealed interface RawForm {
    data object None : RawForm
    data class Range(val min: Long, val max: Long, val step: Long) : RawForm
    data class Enum(val values: List<Long>) : RawForm
}
