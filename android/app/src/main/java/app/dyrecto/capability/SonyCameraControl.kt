package app.dyrecto.capability

/**
 * Sony implementation of the [CameraControl] seam. Validates every request against the current
 * capability snapshot BEFORE the wire. The actual write path (SDIO_SetExtDevicePropValue /
 * SDIO_ControlDevice) is HW-gated: until [writeEnabled] is turned on (Tier B, after FX3 confirms
 * the opcode + payload encoding) a validated request returns [ControlResult.NotEnabled] and nothing
 * is sent to the camera.
 */
class SonyCameraControl(
    private val capsProvider: () -> CameraCapabilities?,
    /** Flipped on in Tier B once writable control is proven on hardware. */
    var writeEnabled: Boolean = false,
    /** Wired in Tier B: performs the actual SDIO_SetExtDevicePropValue. Returns true on camera ack. */
    var propertySender: (suspend (code: Int, value: Long) -> Boolean)? = null,
    /** Wired in Tier B: performs the actual SDIO_ControlDevice command. Returns true on camera ack. */
    var commandSender: (suspend (command: CameraCommand) -> Boolean)? = null,
) : CameraControl {

    override val capabilities: CameraCapabilities? get() = capsProvider()

    override suspend fun setProperty(code: Int, value: Long): ControlResult {
        CameraControlValidator.validate(capabilities, code, value)?.let {
            return ControlResult.Rejected(it)
        }
        val sender = propertySender
        if (!writeEnabled || sender == null) return ControlResult.NotEnabled
        return if (sender(code, value)) ControlResult.Success
        else ControlResult.Failed("camera rejected set 0x%04X=$value".format(code))
    }

    override suspend fun invokeCommand(command: CameraCommand): ControlResult {
        val sender = commandSender
        if (!writeEnabled || sender == null) return ControlResult.NotEnabled
        return if (sender(command)) ControlResult.Success
        else ControlResult.Failed("camera rejected command $command")
    }
}
