import Foundation

/// GetDeviceInfo dataset (ISO 15740, LE) — docs/wire-protocol.md §4.4.
struct PtpDeviceInfo {
    let standardVersion: Int
    let vendorExtensionId: Int64
    let vendorExtensionVersion: Int
    let vendorExtensionDescription: String
    let functionalMode: Int
    let operations: [UInt16]
    let events: [UInt16]
    let properties: [UInt16]
    let captureFormats: [UInt16]
    let imageFormats: [UInt16]
    let manufacturer: String
    let model: String
    let deviceVersion: String
    let serialNumber: String

    static func parse(_ data: Data) throws -> PtpDeviceInfo {
        var r = BinaryReader(data)
        let standardVersion = Int(try r.u16())
        let vendorExtensionId = Int64(try r.u32())
        let vendorExtensionVersion = Int(try r.u16())
        let vendorDesc = try r.ptpString()
        let functionalMode = Int(try r.u16())
        let ops = try r.ptpU16Array()
        let events = try r.ptpU16Array()
        let props = try r.ptpU16Array()
        let captureFormats = try r.ptpU16Array()
        let imageFormats = try r.ptpU16Array()
        let manufacturer = try r.ptpString()
        let model = try r.ptpString()
        let deviceVersion = try r.ptpString()
        let serial = try r.ptpString()
        return PtpDeviceInfo(
            standardVersion: standardVersion,
            vendorExtensionId: vendorExtensionId,
            vendorExtensionVersion: vendorExtensionVersion,
            vendorExtensionDescription: vendorDesc,
            functionalMode: functionalMode,
            operations: ops, events: events, properties: props,
            captureFormats: captureFormats, imageFormats: imageFormats,
            manufacturer: manufacturer, model: model,
            deviceVersion: deviceVersion, serialNumber: serial)
    }
}

/// One record of the 0x9209 SDIO_GetAllExtDevicePropInfo dataset — §4.6.
struct SonyProp {
    let code: UInt16
    let dataType: UInt16
    let getSet: UInt8
    let availability: UInt8
    /// Current value as integer (numeric types) — nil for STR props.
    let intValue: Int64?
    /// Current value as string (STR props) — nil otherwise.
    let stringValue: String?

    /// Well-known Sony property codes used by the app layer.
    enum Code {
        static let liveViewUrl: UInt16 = 0xD278
        static let liveViewStatus: UInt16 = 0xD221
        static let monitoringBinaryVersion: UInt16 = 0xE09D
        static let movieRecordingState: UInt16 = 0xD21D
        static let recordingTime: UInt16 = 0xD261
        static let batteryLevelIndicator: UInt16 = 0xD20E
        static let batteryRemaining: UInt16 = 0xD218
        static let batteryRemainMinutes: UInt16 = 0xD038
        static let mediaSlot1Status: UInt16 = 0xD248
        static let mediaSlot1Remaining: UInt16 = 0xD24A
        static let mediaSlot2Status: UInt16 = 0xD256
        static let mediaSlot2Remaining: UInt16 = 0xD258
        static let overheatingState: UInt16 = 0xD251
    }

    /// Parses the full dataset resiliently: on any malformed record, log offset context and
    /// return what parsed so far (Android parity).
    static func parseDataset(_ data: Data, log: DyrectoLog) -> [UInt16: SonyProp] {
        var out = [UInt16: SonyProp]()
        var r = BinaryReader(data)
        do {
            let count = try r.u64()
            for _ in 0..<count {
                let code = try r.u16()
                let dataType = try r.u16()
                let getSet = try r.u8()
                let availability = try r.u8()
                let _ = try readValue(&r, dataType) // default value — discarded like Android
                let current = try readValue(&r, dataType)
                let formFlag = try r.u8()
                try skipForm(&r, dataType, formFlag)

                switch current {
                case .int(let v):
                    out[code] = SonyProp(code: code, dataType: dataType, getSet: getSet,
                                              availability: availability, intValue: v, stringValue: nil)
                case .string(let s):
                    out[code] = SonyProp(code: code, dataType: dataType, getSet: getSet,
                                              availability: availability, intValue: nil, stringValue: s)
                }
            }
        } catch {
            log.line(.error, "telemetry dataset parse stopped at offset \(r.offset)/\(data.count): \(error) — keeping \(out.count) records")
        }
        return out
    }

    private enum Value {
        case int(Int64)
        case string(String)
    }

    private static func readValue(_ r: inout BinaryReader, _ dataType: UInt16) throws -> Value {
        switch dataType {
        case 0x0001: return .int(Int64(try r.i8()))
        case 0x0002: return .int(Int64(try r.u8()))
        case 0x0003: return .int(Int64(try r.i16()))
        case 0x0004: return .int(Int64(try r.u16()))
        case 0x0005: return .int(Int64(try r.i32()))
        case 0x0006: return .int(Int64(try r.u32()))
        case 0x0007: return .int(try r.i64())
        case 0x0008: return .int(Int64(bitPattern: try r.u64()))
        case 0xFFFF: return .string(try r.ptpString())
        default:
            throw BinaryIOError.malformed(String(format: "unknown dataType 0x%04X", dataType))
        }
    }

    private static func skipForm(_ r: inout BinaryReader, _ dataType: UInt16, _ formFlag: UInt8) throws {
        switch formFlag {
        case 0:
            return
        case 1: // range: min, max, step
            for _ in 0..<3 { _ = try readValue(&r, dataType) }
        case 2: // enum: u16 supportedCount ×values, u16 listedCount ×values
            let supported = Int(try r.u16())
            for _ in 0..<supported { _ = try readValue(&r, dataType) }
            let listed = Int(try r.u16())
            for _ in 0..<listed { _ = try readValue(&r, dataType) }
        default:
            throw BinaryIOError.malformed("unknown formFlag \(formFlag)")
        }
    }
}
