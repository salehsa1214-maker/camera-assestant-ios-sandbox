package app.dyrecto.text

/**
 * Uppercase zero-padded hex, matching JVM `"%0<digits>X".format(value)` for 32-bit ints
 * (negative values print as unsigned, wider values are not truncated).
 */
internal fun hexUpper(value: Int, digits: Int): String =
    value.toUInt().toString(16).uppercase().padStart(digits, '0')

/** 64-bit variant, matching JVM `"%0<digits>X".format(longValue)`. */
internal fun hexUpper(value: Long, digits: Int): String =
    value.toULong().toString(16).uppercase().padStart(digits, '0')
