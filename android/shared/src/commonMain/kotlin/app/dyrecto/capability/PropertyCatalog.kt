package app.dyrecto.capability

import app.dyrecto.text.hexUpper

/**
 * Single source of truth for known Sony device-property codes → human labels. Consolidates the
 * label tables that were previously duplicated across the connection layer and the telemetry
 * domain model. Unknown codes resolve to a synthesized `0x####` label and are reported as unknown
 * (never dropped — Rule 3).
 */
object PropertyCatalog {
    private val labels: Map<Int, String> = mapOf(
        0xD20E to "Battery Level Indicator",
        0xD218 to "Battery Remaining",
        0xD038 to "Battery Remaining (minutes)",
        0xD039 to "Battery Remaining (voltage)",
        0xD204 to "Total Battery Remaining",
        0xD21E to "ISO Sensitivity",
        0xD023 to "ISO Current Sensitivity",
        0x5007 to "F-Number",
        0xD000 to "T-Number",
        0xD20D to "Shutter Speed",
        0xD016 to "Shutter Speed Value",
        0xD017 to "Shutter Speed Current Value",
        0x5005 to "White Balance",
        0xD00C to "White Balance Mode Setting",
        0xD20F to "Color Temperature",
        0x500A to "Focus Mode",
        0xD007 to "Focus Mode Setting",
        0xE044 to "Focus Mode Status",
        0xE004 to "Focus Touch Spot Status",
        0xE005 to "Focus Tracking Status",
        0xD21D to "Movie Recording State",
        0xD261 to "Recording Time",
        0xE010 to "Recorder Main Status",
        0xD248 to "Media SLOT1 Status",
        0xD24A to "Media SLOT1 Remaining Time",
        0xD256 to "Media SLOT2 Status",
        0xD258 to "Media SLOT2 Remaining Time",
        0xD251 to "Device Overheating State",
        0xD049 to "Auto Power-OFF Temperature",
        0xD221 to "Live View Status",
        0xD060 to "Subject Recognition AF",
        0xE098 to "Monitoring Delivering Status",
        0xD024 to "Recording Resolution",
        0xD286 to "Recording Frame Rate",
        0xD241 to "File Format Movie",
        0xD242 to "Recording Setting Movie",
        0xD051 to "S&Q Mode Setting",
        0xD052 to "S&Q Frame Rate",
        0xD109 to "Proxy Record Setting",
        0xD249 to "Media SLOT1 Remaining Shots",
        0xD257 to "Media SLOT2 Remaining Shots",
        0xD04D to "Monitor LUT Setting (All)",
        0xD0AD to "Video Stream Codec",
        0xD0AE to "Video Stream Resolution",
        0xD0AF to "Video Stream Frame Rate",
    )

    /** Non-null only for codes the app recognizes. */
    fun knownLabel(code: Int): String? = labels[code]

    /** Label for any code — known label or a synthesized `0x####`. */
    fun label(code: Int): String = labels[code] ?: ("0x" + hexUpper(code, 4))

    fun isKnown(code: Int): Boolean = labels.containsKey(code)

    val knownCodes: Set<Int> get() = labels.keys
}
