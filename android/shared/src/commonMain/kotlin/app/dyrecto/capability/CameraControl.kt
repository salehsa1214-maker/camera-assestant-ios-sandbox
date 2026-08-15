package app.dyrecto.capability

/** A discrete camera action (not a property set) — routed to a control opcode by the adapter. */
enum class CameraCommand {
    RECORD_START,
    RECORD_STOP,
    AF_PUSH,
    ZOOM_IN,
    ZOOM_OUT,
    ZOOM_STOP,
}

/** Outcome of a control request. */
sealed interface ControlResult {
    data object Success : ControlResult
    /** The request was rejected locally before hitting the wire (see [ControlRejection]). */
    data class Rejected(val rejection: ControlRejection) : ControlResult
    /** The request reached the camera but failed there. */
    data class Failed(val reason: String) : ControlResult
    /** Writable control is present but disabled pending hardware validation (Rule: HW-gated). */
    data object NotEnabled : ControlResult
}

/** Why a property set was rejected by [CameraControlValidator]. */
enum class ControlRejection {
    UNKNOWN_PROPERTY,
    NOT_WRITABLE,
    NOT_AVAILABLE,
    OUT_OF_RANGE,
}

/**
 * Camera-agnostic control seam. Implemented by the adapter; consumed by the app. Every set is
 * validated against the current [CameraCapabilities] BEFORE the wire (see [CameraControlValidator]).
 */
interface CameraControl {
    /** Latest known capabilities, used for validation; null before the first snapshot. */
    val capabilities: CameraCapabilities?

    suspend fun setProperty(code: Int, value: Long): ControlResult

    suspend fun invokeCommand(command: CameraCommand): ControlResult
}

/**
 * Pure validation of a property set against a capabilities snapshot. Returns null when the set is
 * permissible, or the reason it must be rejected. Unit-testable with no transport.
 */
object CameraControlValidator {
    fun validate(caps: CameraCapabilities?, code: Int, value: Long): ControlRejection? {
        val prop = caps?.property(code) ?: return ControlRejection.UNKNOWN_PROPERTY
        if (!prop.writable) return ControlRejection.NOT_WRITABLE
        if (!prop.available) return ControlRejection.NOT_AVAILABLE
        if (!prop.valueSet.permits(value)) return ControlRejection.OUT_OF_RANGE
        return null
    }
}
