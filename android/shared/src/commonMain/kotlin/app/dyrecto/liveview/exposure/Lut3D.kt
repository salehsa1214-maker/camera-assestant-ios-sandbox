package app.dyrecto.liveview.exposure

/**
 * A parsed 3D LUT in the Adobe/Iridas `.cube` text format (pure Kotlin, no Android types — see
 * `Lut3D.parse`). Used by [Lut3DColorTransform] as the Phase 8 Analysis Color Transform for S-Log3.
 *
 * Domain is assumed 0..1 (this codebase's supplied LUT has no `DOMAIN_MIN`/`DOMAIN_MAX` lines, which
 * per the `.cube` spec defaults to 0..1 full-range).
 */
class Lut3D(val size: Int, private val table: FloatArray) {

    init {
        require(size >= 2) { "LUT size must be >= 2, was $size" }
        require(table.size == size * size * size * 3) {
            "table size ${table.size} != ${size * size * size * 3} for size=$size"
        }
    }

    private fun at(ri: Int, gi: Int, bi: Int, channel: Int): Float {
        // .cube ordering: red changes fastest, then green, then blue.
        val index = ((bi * size + gi) * size + ri) * 3 + channel
        return table[index]
    }

    /**
     * Trilinear-interpolated lookup. [r], [g], [b] are normalized 0..1 (out-of-range values are
     * clamped). Returns the transformed RGB, also normalized 0..1.
     */
    fun sample(r: Float, g: Float, b: Float): FloatArray {
        val maxIdx = size - 1
        val rf = r.coerceIn(0f, 1f) * maxIdx
        val gf = g.coerceIn(0f, 1f) * maxIdx
        val bf = b.coerceIn(0f, 1f) * maxIdx

        val r0 = rf.toInt().coerceIn(0, maxIdx)
        val g0 = gf.toInt().coerceIn(0, maxIdx)
        val b0 = bf.toInt().coerceIn(0, maxIdx)
        val r1 = (r0 + 1).coerceAtMost(maxIdx)
        val g1 = (g0 + 1).coerceAtMost(maxIdx)
        val b1 = (b0 + 1).coerceAtMost(maxIdx)

        val rt = rf - r0
        val gt = gf - g0
        val bt = bf - b0

        val out = FloatArray(3)
        for (c in 0 until 3) {
            val c000 = at(r0, g0, b0, c)
            val c100 = at(r1, g0, b0, c)
            val c010 = at(r0, g1, b0, c)
            val c110 = at(r1, g1, b0, c)
            val c001 = at(r0, g0, b1, c)
            val c101 = at(r1, g0, b1, c)
            val c011 = at(r0, g1, b1, c)
            val c111 = at(r1, g1, b1, c)

            val c00 = c000 * (1 - rt) + c100 * rt
            val c10 = c010 * (1 - rt) + c110 * rt
            val c01 = c001 * (1 - rt) + c101 * rt
            val c11 = c011 * (1 - rt) + c111 * rt

            val c0 = c00 * (1 - gt) + c10 * gt
            val c1 = c01 * (1 - gt) + c11 * gt

            out[c] = c0 * (1 - bt) + c1 * bt
        }
        return out
    }

    companion object {
        /**
         * Parses `.cube` text. Ignores `TITLE`, `#` comments, blank lines, and any `DOMAIN_MIN`/
         * `DOMAIN_MAX` lines (this LUT uses the default 0..1 domain). Throws [IllegalArgumentException]
         * on malformed input — callers decide the fallback (see `ExposureConfig.resolveTransform`).
         */
        fun parse(text: String): Lut3D {
            var size = -1
            val values = ArrayList<Float>()

            for (rawLine in text.lineSequence()) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                if (line.startsWith("TITLE", ignoreCase = true)) continue
                if (line.startsWith("DOMAIN_MIN", ignoreCase = true)) continue
                if (line.startsWith("DOMAIN_MAX", ignoreCase = true)) continue
                if (line.startsWith("LUT_3D_SIZE", ignoreCase = true)) {
                    size = line.substringAfter("LUT_3D_SIZE").trim().toInt()
                    continue
                }

                val parts = line.split(Regex("\\s+"))
                require(parts.size == 3) { "Expected 3 floats, got: $line" }
                values.add(parts[0].toFloat())
                values.add(parts[1].toFloat())
                values.add(parts[2].toFloat())
            }

            require(size >= 2) { "LUT_3D_SIZE missing or invalid" }
            val expected = size * size * size * 3
            require(values.size == expected) {
                "Expected $expected values for LUT_3D_SIZE $size, got ${values.size}"
            }

            return Lut3D(size, values.toFloatArray())
        }
    }
}
