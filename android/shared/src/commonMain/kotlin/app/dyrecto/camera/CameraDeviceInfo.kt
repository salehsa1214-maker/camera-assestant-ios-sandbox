package app.dyrecto.camera

import app.dyrecto.text.hexUpper

/**
 * Platform-agnostic, display-ready mirror of the PTP DeviceInfo dataset returned
 * by the verified GetDeviceInfo flow. The connection layer's raw parse result is
 * mapped into this so the UI never depends on the protocol package.
 */
data class CameraDeviceInfo(
    val manufacturer: String,
    val model: String,
    val firmwareVersion: String,   // PTP "deviceVersion"
    val serialNumber: String,
    val standardVersion: Int,
    val vendorExtensionId: Long,
    val vendorExtensionVersion: Int,
    val vendorExtensionDescription: String,
    val functionalMode: Int,
    val operations: List<PtpOperation>,
    val supportedEventCount: Int,
    val supportedPropertyCount: Int,
    val captureFormatCount: Int,
    val imageFormatCount: Int,
) {
    val supportedOperationCount: Int get() = operations.size
    // e.g. 110 → "1.10" (same digits as the old "%.2f".format(standardVersion / 100.0))
    val standardVersionText: String
        get() = "${standardVersion / 100}.${(standardVersion % 100).toString().padStart(2, '0')}"
    val vendorExtensionIdHex: String get() = "0x" + hexUpper(vendorExtensionId, 8)
}
