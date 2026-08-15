package app.dyrecto.domain

import app.dyrecto.capability.PropertyCatalog

/**
 * One device-property value extracted from a 0x9209 telemetry snapshot, ready for display.
 * Raw value only — no Sony enum interpretation yet.
 */
data class TelemetryProp(
    val code: Int,
    val label: String,
    val rawValue: String?,
    val dataType: Int,
    /** Numeric current value, when the camera supplied one. Source for the decoder layer. */
    val rawNumber: Long? = null,
) {
    /** Human-readable decoded value, or null when no confirmed decoder rule applies. */
    val decoded: String? get() = TelemetryDecoder.decode(code, rawNumber)
}

/**
 * Platform-agnostic snapshot of parsed Sony telemetry (0x9209), keyed by property code.
 *
 * This is the domain mirror of the connection layer's `PtpIpClient.SonyProp` map; it exists
 * so the UI/domain never depends on the verified protocol layer. The label table mirrors the
 * connection layer's private `sonyPropName` (which cannot be reused) and is kept in sync here.
 */
data class CameraTelemetry(
    val props: Map<Int, TelemetryProp> = emptyMap(),
) {
    operator fun get(code: Int): TelemetryProp? = props[code]

    companion object {
        // ---- Property codes surfaced on the dashboard ----
        // Exposure
        const val ISO = 0xD21E
        const val FNUMBER = 0x5007
        const val SHUTTER = 0xD20D
        const val WHITE_BALANCE = 0x5005
        const val COLOR_TEMP = 0xD20F
        // Recording
        const val MOVIE_REC = 0xD21D
        const val REC_TIME = 0xD261
        const val REC_RESOLUTION = 0xD024
        const val REC_FPS = 0xD286
        const val FILE_FORMAT_MOVIE = 0xD241
        const val REC_SETTING_MOVIE = 0xD242
        const val SANDQ_MODE = 0xD051
        const val SANDQ_FPS = 0xD052
        const val PROXY_REC = 0xD109
        // Media
        const val SLOT1_STATUS = 0xD248
        const val SLOT1_REMAIN = 0xD24A
        const val SLOT1_SHOTS = 0xD249
        const val SLOT2_STATUS = 0xD256
        const val SLOT2_REMAIN = 0xD258
        const val SLOT2_SHOTS = 0xD257
        // Focus
        const val FOCUS_MODE = 0x500A
        const val FOCUS_MODE_SETTING = 0xD007
        const val FOCUS_TRACKING = 0xE005
        const val FOCUS_TOUCH_SPOT = 0xE004
        const val SUBJECT_AF = 0xD060
        // Power & Thermal
        const val BATTERY = 0xD218
        const val BATTERY_MINUTES = 0xD038
        const val BATTERY_VOLTAGE = 0xD039
        const val BATTERY_TOTAL = 0xD204
        const val OVERHEATING = 0xD251
        const val AUTO_POWER_OFF_TEMP = 0xD049
        // Monitoring
        const val MONITOR_LUT = 0xD04D
        const val MONITOR_CODEC = 0xD0AD
        const val MONITOR_RESOLUTION = 0xD0AE
        const val MONITOR_FPS = 0xD0AF
        // Misc
        const val MONITORING_DELIVERING = 0xE098
        const val LIVE_VIEW = 0xD221

        /**
         * Readable label for a known property code. Delegates to [PropertyCatalog], the single
         * source of truth that also backs the capability layer (previously this table was a
         * hand-synced duplicate of the connection-layer `sonyPropName`).
         */
        fun labelFor(code: Int): String = PropertyCatalog.label(code)
    }
}
