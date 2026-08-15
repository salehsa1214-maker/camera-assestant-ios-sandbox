package app.dyrecto.liveview.exposure

/**
 * A zebra threshold specification, in IRE (0..100), mirroring Sony's two zebra types (Phase 6).
 *
 * Recovered from `proremote` (see `README-sony-re.md`): `ZebraType { Zebra1, Zebra2 }`, where Zebra1
 * is a range band around a center level and Zebra2 is a single lower-limit level. [ZebraEngine] maps
 * luma → IRE using Sony's 16–235 studio-swing convention before comparing.
 */
sealed interface ZebraSpec {
    /** Human/diagnostic label, e.g. "100" or "70±5". */
    val label: String

    /**
     * Zebra2 — single level: a pixel matches when its IRE is `>= level`.
     * @param level IRE threshold (0..100+; values up to 109 allowed for over-white like Sony).
     */
    data class Level(val level: Int) : ZebraSpec {
        override val label: String get() = level.toString()
    }

    /**
     * Zebra1 — range band: a pixel matches when `|IRE - center| <= range`.
     * @param center center IRE level.
     * @param range half-width of the band in IRE.
     */
    data class Range(val center: Int, val range: Int) : ZebraSpec {
        override val label: String get() = "$center±$range"
    }

    companion object {
        /** Phase 6 preset set (fallback; exact Sony presets are camera-reported — see notes). */
        val PRESET_70: ZebraSpec = Level(70)
        val PRESET_95: ZebraSpec = Level(95)
        val PRESET_100: ZebraSpec = Level(100)
    }
}
