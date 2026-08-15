package app.dyrecto.capability

/**
 * Encodes a numeric property value into the little-endian DataOut payload a Sony
 * `SDIO_SetExtDevicePropValue` carries, sized by the property's PTP datatype (ISO 15740). Pure and
 * testable; used by the (HW-gated) write path in the Sony adapter.
 *
 * Only the numeric datatypes are handled — string-valued sets are not attempted here.
 */
object SonyPropValueCodec {
    // PTP datatype codes.
    private const val DT_INT8 = 0x0001
    private const val DT_UINT8 = 0x0002
    private const val DT_INT16 = 0x0003
    private const val DT_UINT16 = 0x0004
    private const val DT_INT32 = 0x0005
    private const val DT_UINT32 = 0x0006
    private const val DT_INT64 = 0x0007
    private const val DT_UINT64 = 0x0008

    fun encode(value: Long, dataType: Int): ByteArray = when (dataType) {
        DT_INT8, DT_UINT8 -> byteArrayOf(value.toByte())
        DT_INT16, DT_UINT16 -> le(value, 2)
        DT_INT32, DT_UINT32 -> le(value, 4)
        DT_INT64, DT_UINT64 -> le(value, 8)
        else -> le(value, 4) // best-effort fallback for unknown numeric widths
    }

    private fun le(value: Long, bytes: Int): ByteArray {
        val out = ByteArray(bytes)
        var v = value
        for (i in 0 until bytes) {
            out[i] = (v and 0xFF).toByte()
            v = v shr 8
        }
        return out
    }
}
