package app.dyrecto.capability

import app.dyrecto.connection.PtpIpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Sony implementation of [CameraAdapter]. This is the ONLY place Sony/PTP types are translated
 * into the camera-agnostic capability models — everything above it (UI, alerts, reference, voice)
 * sees only [CameraCapabilities] / [CameraEvent] / [CameraControl] (Rule 4).
 *
 * Fed by `CameraRepositoryImpl` at the same seam it maps telemetry to the domain model. The write
 * path is HW-gated: [control] validates locally but does not touch the camera until writable
 * control is proven on hardware (see [SonyCameraControl]).
 */
class SonyCameraAdapter : CameraAdapter {

    private val _capabilities = MutableStateFlow<CameraCapabilities?>(null)
    override val capabilities = _capabilities.asStateFlow()

    private val _events = MutableSharedFlow<CameraEvent>(extraBufferCapacity = 64)
    override val events: Flow<CameraEvent> = _events

    override val control: SonyCameraControl = SonyCameraControl(capsProvider = { _capabilities.value })

    /**
     * Rebuilds the capability snapshot from a fresh raw telemetry map + device info. Called on every
     * 0x9209 refresh; cheap (reads already-parsed records, no rescan).
     */
    fun updateFromTelemetry(
        props: Map<Int, PtpIpClient.SonyProp>,
        deviceInfo: PtpIpClient.DeviceInfo?,
        transport: CameraTransport = CameraTransport.WIFI,
    ) {
        val info = deviceInfo?.toCameraInfo(transport) ?: CameraInfo(transport = transport)
        val records = props.values.map { it.toRawRecord() }
        val newCaps = CapabilityMapper.build(info, records)
        val prev = _capabilities.value
        _capabilities.value = newCaps
        // Emit normalized property-change events off the diff (drives settings-drift alerts etc.).
        CameraEventDiffer.diff(prev, newCaps).forEach { emit(it) }
    }

    /** Emit a normalized event to consumers (alerts/voice). Non-blocking; drops if buffer is full. */
    fun emit(event: CameraEvent) {
        _events.tryEmit(event)
    }

    /** Clears capabilities on disconnect so stale data never lingers. */
    fun reset() {
        _capabilities.value = null
    }
}

/** SonyProp → portable [RawPropertyRecord]. `form` is already the portable [RawForm]. */
internal fun PtpIpClient.SonyProp.toRawRecord(): RawPropertyRecord = RawPropertyRecord(
    code = propertyCode,
    dataType = dataType,
    getSet = getSet,
    availability = availability,
    currentRaw = currentValueRaw,
    currentText = currentValueStr,
    form = form,
)

/** PTP DeviceInfo → agnostic [CameraInfo]. Empty strings collapse to null (not guessed). */
internal fun PtpIpClient.DeviceInfo.toCameraInfo(transport: CameraTransport): CameraInfo = CameraInfo(
    manufacturer = manufacturer.ifBlank { null },
    model = model.ifBlank { null },
    firmwareVersion = deviceVersion.ifBlank { null },
    protocolVersion = "ext v$vendorExtensionVersion (std 0x%04X)".format(standardVersion),
    serialNumber = serialNumber.ifBlank { null },
    lensInfo = null, // not carried in DeviceInfo; may come from a lens property later
    transport = transport,
)
