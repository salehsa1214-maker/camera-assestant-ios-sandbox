package app.dyrecto.domain

/**
 * Platform-agnostic, additive decoder that turns raw Sony 0x9209 property values into
 * human-readable display strings. It does NOT touch the protocol layer or the 0x9209 parser;
 * it only interprets values already extracted into [TelemetryProp.rawNumber].
 *
 * Rules are derived from the verified Sony "Monitor & Control" decompile
 * (sony_monitor_control_0x9209_value_interpretation.md). [decode] returns null whenever no
 * confirmed rule applies for a property code, so callers fall back to showing the raw value.
 */
object TelemetryDecoder {

    private const val UNDEFINED_U16 = 65535L
    private const val UNDEFINED_U8 = 255L

    /** Returns a decoded display string for [code], or null when undecodable. */
    fun decode(code: Int, raw: Long?): String? {
        if (raw == null) return null
        return when (code) {
            CameraTelemetry.FNUMBER -> "f/" + (raw / 100.0)
            CameraTelemetry.BATTERY -> "$raw%"
            CameraTelemetry.BATTERY_TOTAL -> "$raw%"
            CameraTelemetry.BATTERY_MINUTES -> "${raw} min"
            CameraTelemetry.SHUTTER -> shutterSpeed(raw)
            CameraTelemetry.COLOR_TEMP ->
                if (raw > 0 && raw != UNDEFINED_U16) "${raw}K" else null
            CameraTelemetry.MOVIE_REC -> movieRecordingState(raw)
            CameraTelemetry.LIVE_VIEW -> liveViewStatus(raw)
            CameraTelemetry.SLOT1_STATUS, CameraTelemetry.SLOT2_STATUS -> mediaSlotStatus(raw)
            CameraTelemetry.OVERHEATING -> overheatingState(raw)
            CameraTelemetry.FOCUS_MODE_SETTING -> focusModeSetting(raw)
            CameraTelemetry.FOCUS_TOUCH_SPOT -> focusTouchSpotStatus(raw)
            CameraTelemetry.FOCUS_TRACKING -> focusTrackingStatus(raw)
            CameraTelemetry.SUBJECT_AF -> subjectRecognitionAf(raw)
            CameraTelemetry.MONITORING_DELIVERING -> monitoringDeliveringStatus(raw)
            CameraTelemetry.WHITE_BALANCE -> whiteBalance(raw)
            CameraTelemetry.REC_FPS -> recordingFrameRate(raw)
            CameraTelemetry.FILE_FORMAT_MOVIE -> fileFormatMovie(raw)
            CameraTelemetry.REC_SETTING_MOVIE -> recordingSettingMovie(raw)
            CameraTelemetry.MONITOR_LUT -> monitorLutSetting(raw)
            CameraTelemetry.AUTO_POWER_OFF_TEMP -> autoPowerOffTemp(raw)
            else -> null
        }
    }

    /** 0xD20D ShutterSpeed — packed high16/low16. */
    private fun shutterSpeed(raw: Long): String? {
        if (raw == 0L || raw == -1L) return "—"
        val high16 = (raw shr 16) and 0xFFFF
        val low16 = raw and 0xFFFF
        return when {
            high16 == 1L -> "1/$low16 s"
            low16 == 10L -> "${high16 / 10.0} s"
            else -> null
        }
    }

    /** 0xD21D MovieRecordingState. */
    private fun movieRecordingState(raw: Long): String? = when (raw) {
        UNDEFINED_U8 -> "Undefined"
        0L -> "Not Recording"
        1L -> "Recording"
        2L -> "Recording Failed"
        3L -> "Waiting Record"
        else -> null
    }

    /** 0xD221 LiveViewStatus. */
    private fun liveViewStatus(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        0L -> "Supported, disabled"
        1L -> "Enabled"
        2L -> "Not supported"
        else -> null
    }

    /** 0xD248 / 0xD256 Media slot status. */
    private fun mediaSlotStatus(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        1L -> "OK"
        2L -> "No card"
        3L -> "Card error"
        4L -> "Recognizing / locked / DB error"
        else -> null
    }

    /** 0xD251 DeviceOverheatingState. */
    private fun overheatingState(raw: Long): String? = when (raw) {
        UNDEFINED_U8 -> "Undefined"
        0L -> "Not Overheating"
        1L -> "Pre Overheating"
        2L -> "Overheating"
        else -> null
    }

    /** 0xD007 FocusModeSetting. */
    private fun focusModeSetting(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        1L -> "Automatic"
        2L -> "Manual"
        else -> null
    }

    /** 0xE004 FocusTouchSpotStatus. */
    private fun focusTouchSpotStatus(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        1L -> "Stopped"
        2L -> "Running"
        else -> null
    }

    /** 0xE005 FocusTrackingStatus. */
    private fun focusTrackingStatus(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        1L -> "Off"
        2L -> "Focusing"
        3L -> "Tracking"
        else -> null
    }

    /** 0xD060 SubjectRecognitionAF. */
    private fun subjectRecognitionAf(raw: Long): String? = when (raw) {
        UNDEFINED_U16 -> "Undefined"
        1L -> "Off"
        2L -> "Face/Eye Only AF"
        3L -> "Face/Eye Priority AF"
        4L -> "Animal/Bird Priority AF"
        5L -> "Animal Priority AF"
        6L -> "Bird Priority AF"
        else -> null
    }

    /** 0xE098 MonitoringDeliveringStatus. */
    private fun monitoringDeliveringStatus(raw: Long): String? = when (raw) {
        1L -> "RTSP"
        2L -> "VERIC"
        UNDEFINED_U16 -> "None"
        else -> null
    }

    /**
     * 0xD286 RecordingFrameRateSetting. Sony decodes on the low byte (`z4.I0`,
     * `(value & 255) == enum`). Progressive 0x00–0x17, interlaced 0x41–0x57.
     */
    private fun recordingFrameRate(raw: Long): String? = when (raw and 0xFF) {
        1L -> "120p"
        2L -> "100p"
        3L -> "60p"
        4L -> "50p"
        5L -> "30p"
        6L -> "25p"
        7L -> "24p"
        8L -> "23.98p"
        9L -> "29.97p"
        10L -> "59.94p"
        11L -> "19.98p"
        12L -> "14.99p"
        13L -> "12.50p"
        14L -> "12p"
        15L -> "11.99p"
        16L -> "10p"
        17L -> "9.99p"
        18L -> "6p"
        19L -> "5.99p"
        20L -> "5p"
        21L -> "4.995p"
        22L -> "24p"
        23L -> "119.88p"
        65L -> "120i"
        66L -> "100i"
        67L -> "60i"
        68L -> "50i"
        69L -> "30i"
        70L -> "25i"
        71L -> "24i"
        72L -> "23.98i"
        73L -> "29.97i"
        74L -> "59.94i"
        75L -> "19.98i"
        76L -> "14.99i"
        77L -> "12.50i"
        78L -> "12i"
        79L -> "11.99i"
        80L -> "10i"
        81L -> "9.99i"
        82L -> "6i"
        83L -> "5.99i"
        84L -> "5i"
        85L -> "4.995i"
        86L -> "24i"
        87L -> "119.88i"
        else -> null
    }

    /**
     * 0xD241 FileFormatMovie. Sony decodes on the low byte (`z4.G`); `65535` is the
     * Undefined sentinel returned as fallback, not a low-byte match.
     */
    private fun fileFormatMovie(raw: Long): String? {
        if (raw == UNDEFINED_U16) return "Undefined"
        return when (raw and 0xFF) {
            1L -> "DVD"
            2L -> "M2PS"
            3L -> "AVCHD"
            4L -> "MP4"
            5L -> "DV"
            6L -> "XAVC"
            7L -> "MXF"
            8L -> "XAVC S 4K"
            9L -> "XAVC S HD"
            10L -> "XAVC HS 8K"
            11L -> "XAVC HS 4K"
            12L -> "XAVC S-L 4K"
            13L -> "XAVC S-L HD"
            14L -> "XAVC S-I 4K"
            15L -> "XAVC S-I HD"
            16L -> "XAVC I"
            17L -> "XAVC L"
            18L -> "XAVC Proxy"
            19L -> "XAVC HS HD"
            20L -> "XAVC S-I DCI 4K"
            21L -> "XAVC H-I HQ"
            22L -> "XAVC H-I SQ"
            23L -> "XAVC H-L"
            24L -> "X-OCN XT"
            25L -> "X-OCN ST"
            26L -> "X-OCN LT"
            27L -> "XAVC HS-L 422"
            28L -> "XAVC HS-L 420"
            29L -> "XAVC S-L 422"
            30L -> "XAVC S-L 420"
            31L -> "XAVC S-I 422"
            32L -> "MPEG HD 422"
            33L -> "X-OCN C1"
            34L -> "X-OCN C2"
            else -> null
        }
    }

    /**
     * 0xD242 RecordingSettingMovie. Sony decodes on the low 16 bits (`z4.J0`,
     * `(65535 & raw)`).
     */
    private fun recordingSettingMovie(raw: Long): String? = when (raw and 0xFFFF) {
        1L -> "60p 50M"
        2L -> "30p 50M"
        3L -> "24p 50M"
        4L -> "50p 50M"
        5L -> "25p 50M"
        6L -> "60i 24M(FX)"
        7L -> "50i 24M(FX)"
        8L -> "60i 17M(FH)"
        9L -> "50i 17M(FH)"
        10L -> "60p 28M(PS)"
        11L -> "50p 28M(PS)"
        12L -> "24p 24M(FX)"
        13L -> "25p 24M(FX)"
        14L -> "24p 17M(FH)"
        15L -> "25p 17M(FH)"
        16L -> "120p 50M (1280x720)"
        17L -> "100p 50M (1280x720)"
        18L -> "1920x1080 30p 16M"
        19L -> "1920x1080 25p 16M"
        20L -> "1280x720 30p 6M"
        21L -> "1280x720 25p 6M"
        22L -> "1920x1080 60p 28M"
        23L -> "1920x1080 50p 28M"
        24L -> "60p 25M / XAVC S HD"
        25L -> "50p 25M / XAVC S HD"
        26L -> "30p 16M / XAVC S HD"
        27L -> "25p 16M / XAVC S HD"
        28L -> "120p 100M (1920x1080) / XAVC S HD"
        29L -> "100p 100M (1920x1080) / XAVC S HD"
        30L -> "120p 60M (1920x1080) / XAVC S HD"
        31L -> "100p 60M (1920x1080) / XAVC S HD"
        32L -> "30p 100M / XAVC S 4K"
        33L -> "25p 100M / XAVC S 4K"
        34L -> "24p 100M / XAVC S 4K"
        35L -> "30p 60M / XAVC S 4K"
        36L -> "25p 60M / XAVC S 4K"
        37L -> "24p 60M / XAVC S 4K"
        38L -> "600M 422 10bit"
        39L -> "500M 422 10bit"
        40L -> "400M 420 10bit"
        41L -> "300M 422 10bit"
        42L -> "280M 422 10bit"
        43L -> "250M 422 10bit"
        44L -> "240M 422 10bit"
        45L -> "222M 422 10bit"
        46L -> "200M 422 10bit"
        47L -> "200M 420 10bit"
        48L -> "200M 420 8bit"
        49L -> "185M 422 10bit"
        50L -> "150M 420 10bit"
        51L -> "150M 420 8bit"
        52L -> "140M 422 10bit"
        53L -> "111M 422 10bit"
        54L -> "100M 422 10bit"
        55L -> "100M 420 10bit"
        56L -> "100M 420 8bit"
        57L -> "93M 422 10bit"
        58L -> "89M 422 10bit"
        59L -> "75M 420 10bit"
        60L -> "60M 420 8bit"
        61L -> "50M 422 10bit"
        62L -> "50M 420 10bit"
        63L -> "50M 420 8bit"
        64L -> "45M 420 10bit"
        65L -> "30M 420 10bit"
        66L -> "25M 420 8bit"
        67L -> "16M 420 8bit"
        68L -> "520M 422 10bit"
        69L -> "260M 422 10bit"
        else -> null
    }

    /** 0xD04D MonitorLUTSettingAllLine. Sony decodes on the low byte (`z4.EnumC2742j0`). */
    private fun monitorLutSetting(raw: Long): String? = when (raw and 0xFF) {
        1L -> "Off"
        2L -> "On"
        else -> null
    }

    /** 0xD049 AutoPowerOFFTemperature (`z4.EnumC2729d`). */
    private fun autoPowerOffTemp(raw: Long): String? = when (raw) {
        UNDEFINED_U8 -> "Undefined"
        1L -> "Standard"
        2L -> "High"
        else -> null
    }

    /** 0x5005 WhiteBalance. */
    private fun whiteBalance(raw: Long): String? = when (raw) {
        0L -> "Undefined"
        1L -> "Manual"
        2L -> "AWB"
        3L -> "One-push Automatic"
        4L -> "Daylight"
        5L -> "Fluorescent"
        6L -> "Tungsten"
        7L -> "Flash"
        32784L -> "Cloudy"
        32785L -> "Shade"
        32786L -> "Color Temperature"
        32800L -> "Custom1"
        32801L -> "Custom2"
        32802L -> "Custom3"
        32803L -> "Custom"
        32816L -> "Underwater Auto"
        else -> null
    }
}
