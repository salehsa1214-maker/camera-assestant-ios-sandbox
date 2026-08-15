import CoreBluetooth

/// UUID constants for the Sony FX3 BLE surface — byte-for-byte the Android `Fx3Uuids`.
/// See docs/wire-protocol.md §1.
enum Fx3Uuids {
    static let provisioningService = CBUUID(string: "8000cc00-cc00-ffff-ffff-ffffffffffff")
    static let pairingService = CBUUID(string: "8000ee00-ee00-ffff-ffff-ffffffffffff")
    static let cccd = CBUUID(string: "00002902-0000-1000-8000-00805f9b34fb")

    /// Expands a 16-bit short code to the full Sony characteristic UUID.
    static func characteristic(_ short: UInt16) -> CBUUID {
        CBUUID(string: String(format: "0000%04x-0000-1000-8000-00805f9b34fb", short))
    }

    // Provisioning ("CC") characteristics.
    static let cc03Notify = characteristic(0xCC03)
    static let cc06WifiSsid = characteristic(0xCC06)
    static let cc07WifiPassword = characteristic(0xCC07)
    static let cc08WifiApOn = characteristic(0xCC08)
    static let cc09WifiStatus = characteristic(0xCC09)
    static let cc0aFirmware = characteristic(0xCC0A)
    static let cc0bModel = characteristic(0xCC0B)
    static let cc0cBssid = characteristic(0xCC0C)
    static let cc0dDeviceInfo = characteristic(0xCC0D)
    static let cc0eControlResult = characteristic(0xCC0E)
    static let cc0fNotify = characteristic(0xCC0F)
    static let cc10Notify = characteristic(0xCC10)
    static let cc16Notify = characteristic(0xCC16)
    static let cc17SshInfo = characteristic(0xCC17)
    static let cc1bNotify = characteristic(0xCC1B)
    static let cca1CameraSsid = characteristic(0xCCA1)
    static let cca2CameraUuid = characteristic(0xCCA2)
    static let cca3SmartphoneControl = characteristic(0xCCA3)
    static let cca5Info = characteristic(0xCCA5)
    static let cca7DeviceInfo = characteristic(0xCCA7)
    static let cca9Info = characteristic(0xCCA9)

    // Pairing ("EE") characteristics.
    static let ee01Pairing = characteristic(0xEE01)
    static let ee02Registration = characteristic(0xEE02)
    static let ee03PairingNotify = characteristic(0xEE03)
    static let ee04Registration = characteristic(0xEE04)

    // Write values (verbatim from the verified Android stack).
    static let smartphoneControlOn = Data([0x03, 0x00, 0x00, 0x01])
    static let wifiApOn = Data([0x01])
    static let pairingRequest = Data([0x06, 0x08, 0x01, 0x00, 0x00, 0x00, 0x00])

    /// Scan name filters (case-insensitive contains), same list the Android scanner uses.
    static let nameFilters = ["FX3", "ILME-FX3", "DSC", "ILCE", "Sony"]
}
