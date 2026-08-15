package app.dyrecto.capability

/**
 * Normalized asynchronous camera events — camera-agnostic. Produced by the adapter from the PTP
 * event channel and telemetry diffs; consumed by the alert/perception/voice layers, which never
 * see raw Sony telemetry.
 *
 * Forward-compatibility (Rule 3): an event that cannot be mapped to a named case is surfaced as
 * [Unknown] with its raw payload preserved, never dropped.
 */
sealed interface CameraEvent {
    data object RecordingStarted : CameraEvent
    data object RecordingStopped : CameraEvent

    /** Battery percentage changed. [percent] in 0..100 when known. */
    data class BatteryChanged(val percent: Int?, val minutesRemaining: Int? = null) : CameraEvent

    /** A storage slot's state changed (remaining time / shots / status). */
    data class StorageChanged(val slot: Int) : CameraEvent
    data class MediaInserted(val slot: Int) : CameraEvent
    data class MediaRemoved(val slot: Int) : CameraEvent

    /** Attached lens changed; [info] is a display string when the camera reports one. */
    data class LensChanged(val info: String?) : CameraEvent

    /** Thermal state escalated. [level] is a coarse 0 (normal) .. n (severe) when known. */
    data class ThermalWarning(val level: Int?) : CameraEvent

    data object ConnectionLost : CameraEvent

    /** A device property changed value. Carries the property [code] and old/new raw values. */
    data class PropertyChanged(val code: Int, val oldRaw: Long?, val newRaw: Long?) : CameraEvent

    data object FocusChanged : CameraEvent

    /** An event the adapter could not classify. Raw bytes/code preserved for the Explorer. */
    data class Unknown(val code: Int, val raw: ByteArray?) : CameraEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Unknown) return false
            if (code != other.code) return false
            if (raw != null) {
                if (other.raw == null) return false
                if (!raw.contentEquals(other.raw)) return false
            } else if (other.raw != null) return false
            return true
        }

        override fun hashCode(): Int = 31 * code + (raw?.contentHashCode() ?: 0)
    }
}
