package app.dyrecto.camera

import app.dyrecto.text.hexUpper

/**
 * Human-readable names for PTP operation codes (ISO 15740 standard set + the
 * Sony 0x9xxx vendor extension range). Used to label the searchable operation
 * list on the Device Info screen. Unknown codes fall back to their hex value.
 */
object PtpOpcodes {

    private val names: Map<Int, String> = mapOf(
        // ---- Standard PTP (ISO 15740) ----
        0x1001 to "GetDeviceInfo",
        0x1002 to "OpenSession",
        0x1003 to "CloseSession",
        0x1004 to "GetStorageIDs",
        0x1005 to "GetStorageInfo",
        0x1006 to "GetNumObjects",
        0x1007 to "GetObjectHandles",
        0x1008 to "GetObjectInfo",
        0x1009 to "GetObject",
        0x100A to "GetThumb",
        0x100B to "DeleteObject",
        0x100C to "SendObjectInfo",
        0x100D to "SendObject",
        0x100E to "InitiateCapture",
        0x100F to "FormatStore",
        0x1010 to "ResetDevice",
        0x1011 to "SelfTest",
        0x1012 to "SetObjectProtection",
        0x1013 to "PowerDown",
        0x1014 to "GetDevicePropDesc",
        0x1015 to "GetDevicePropValue",
        0x1016 to "SetDevicePropValue",
        0x1017 to "ResetDevicePropValue",
        0x1018 to "TerminateOpenCapture",
        0x1019 to "MoveObject",
        0x101A to "CopyObject",
        0x101B to "GetPartialObject",
        0x101C to "InitiateOpenCapture",
        0x101D to "StartEnumHandles",
        0x101E to "EnumHandles",
        0x101F to "StopEnumHandles",
        0x1020 to "GetVendorExtensionMaps",
        0x1021 to "GetVendorDeviceInfo",
        0x9801 to "GetObjectPropsSupported",
        0x9802 to "GetObjectPropDesc",
        0x9803 to "GetObjectPropValue",
        0x9805 to "GetObjectPropList",

        // ---- Sony vendor extension (common subset; 0x92xx/0x96xx/0x97xx) ----
        0x9201 to "SDIO_Connect",
        0x9202 to "SDIO_GetExtDeviceInfo",
        0x9203 to "SDIO_GetExtDevicePropInfo",
        0x9205 to "SDIO_ControlDevice",
        0x9207 to "SDIO_GetAllExtDevicePropInfo",
        0x9209 to "SDIO_SetExtDevicePropValue",
        0x920A to "SDIO_GetExtDeviceInfo (alt)",
        0x920C to "SDIO_GetPartialLargeObject",
        0x920D to "SDIO_SetContsCompInfo",
        0x9211 to "SDIO_GetContentsTransferProp",
        0x9212 to "SDIO_GetDisplayStringList",
    )

    fun name(code: Int): String = names[code] ?: "Unknown"

    fun hex(code: Int): String = "0x" + hexUpper(code, 4)

    /** A labelled operation for list display. */
    fun describe(code: Int): PtpOperation = PtpOperation(code, name(code))
}

/** One supported operation, list-display ready. */
data class PtpOperation(val code: Int, val name: String) {
    val hex: String get() = PtpOpcodes.hex(code)
    val isVendor: Boolean get() = code and 0xF000 == 0x9000
    /** Searchable haystack: hex + name. */
    val searchText: String get() = "$hex $name".lowercase()
}
