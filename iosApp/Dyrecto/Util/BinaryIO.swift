import Foundation

/// Little-endian binary reader over `Data`, mirroring the Android stack's ByteBuffer(LE) use.
/// All PTP/IP and Sony SDIO payloads are LE unless noted (see docs/wire-protocol.md).
struct BinaryReader {
    let data: Data
    private(set) var offset: Int

    init(_ data: Data, offset: Int = 0) {
        self.data = data
        self.offset = offset
    }

    var remaining: Int { data.count - offset }

    mutating func u8() throws -> UInt8 {
        guard remaining >= 1 else { throw BinaryIOError.underflow(needed: 1, remaining: remaining) }
        defer { offset += 1 }
        return data[data.startIndex + offset]
    }

    mutating func u16() throws -> UInt16 {
        guard remaining >= 2 else { throw BinaryIOError.underflow(needed: 2, remaining: remaining) }
        let v = UInt16(data[data.startIndex + offset])
            | UInt16(data[data.startIndex + offset + 1]) << 8
        offset += 2
        return v
    }

    mutating func u32() throws -> UInt32 {
        guard remaining >= 4 else { throw BinaryIOError.underflow(needed: 4, remaining: remaining) }
        var v: UInt32 = 0
        for i in 0..<4 { v |= UInt32(data[data.startIndex + offset + i]) << (8 * i) }
        offset += 4
        return v
    }

    mutating func u64() throws -> UInt64 {
        guard remaining >= 8 else { throw BinaryIOError.underflow(needed: 8, remaining: remaining) }
        var v: UInt64 = 0
        for i in 0..<8 { v |= UInt64(data[data.startIndex + offset + i]) << (8 * i) }
        offset += 8
        return v
    }

    mutating func i8() throws -> Int8 { Int8(bitPattern: try u8()) }
    mutating func i16() throws -> Int16 { Int16(bitPattern: try u16()) }
    mutating func i32() throws -> Int32 { Int32(bitPattern: try u32()) }
    mutating func i64() throws -> Int64 { Int64(bitPattern: try u64()) }

    mutating func bytes(_ count: Int) throws -> Data {
        guard remaining >= count else { throw BinaryIOError.underflow(needed: count, remaining: remaining) }
        let sub = data.subdata(in: (data.startIndex + offset)..<(data.startIndex + offset + count))
        offset += count
        return sub
    }

    /// PTP string: u8 charCount, then charCount UTF-16LE code units (includes the NUL when the
    /// camera sends one). Trailing NULs/spaces trimmed, matching the Android parser.
    mutating func ptpString() throws -> String {
        let charCount = Int(try u8())
        if charCount == 0 { return "" }
        let raw = try bytes(charCount * 2)
        var units = [UInt16]()
        units.reserveCapacity(charCount)
        var i = raw.startIndex
        while i < raw.endIndex {
            let lo = UInt16(raw[i])
            let hi = i + 1 < raw.endIndex ? UInt16(raw[i + 1]) : 0
            units.append(lo | (hi << 8))
            i += 2
        }
        var s = String(decoding: units, as: UTF16.self)
        while s.hasSuffix("\0") { s.removeLast() }
        while s.hasSuffix(" ") { s.removeLast() }
        return s
    }

    /// PTP u16 array: u32 count, then count × u16.
    mutating func ptpU16Array() throws -> [UInt16] {
        let count = Int(try u32())
        guard count >= 0, count <= remaining / 2 else {
            throw BinaryIOError.malformed("u16 array count \(count) exceeds remaining \(remaining)")
        }
        var out = [UInt16]()
        out.reserveCapacity(count)
        for _ in 0..<count { out.append(try u16()) }
        return out
    }
}

/// Little-endian binary writer.
struct BinaryWriter {
    private(set) var data = Data()

    mutating func u8(_ v: UInt8) { data.append(v) }

    mutating func u16(_ v: UInt16) {
        data.append(UInt8(truncatingIfNeeded: v))
        data.append(UInt8(truncatingIfNeeded: v >> 8))
    }

    mutating func u32(_ v: UInt32) {
        for i in 0..<4 { data.append(UInt8(truncatingIfNeeded: v >> (8 * UInt32(i)))) }
    }

    mutating func u64(_ v: UInt64) {
        for i in 0..<8 { data.append(UInt8(truncatingIfNeeded: v >> (8 * UInt64(i)))) }
    }

    mutating func raw(_ bytes: Data) { data.append(bytes) }

    /// UTF-16LE encode + NUL terminator (PTP/IP init name encoding).
    mutating func utf16leNulTerminated(_ s: String) {
        for unit in s.utf16 {
            data.append(UInt8(truncatingIfNeeded: unit))
            data.append(UInt8(truncatingIfNeeded: unit >> 8))
        }
        u16(0)
    }
}

enum BinaryIOError: Error, CustomStringConvertible {
    case underflow(needed: Int, remaining: Int)
    case malformed(String)

    var description: String {
        switch self {
        case .underflow(let n, let r): return "binary underflow: needed \(n), remaining \(r)"
        case .malformed(let m): return "malformed data: \(m)"
        }
    }
}
