package app.dyrecto.capability

/**
 * Resolves the camera-agnostic [CameraFeature] set from the observed properties + transport. Pure
 * and best-effort: a feature is asserted only when there is positive evidence (a known property is
 * present, or writable for control features). Absence of evidence = feature absent, never guessed.
 *
 * This is the ONE place property codes map onto features; onboarding a new model that reports a
 * new code = extend this table, nothing else.
 */
object FeatureResolver {

    // Property codes that gate each feature (from PropertyCatalog / CameraTelemetry).
    private const val LIVE_VIEW = 0xD221
    private const val ISO = 0xD21E
    private const val SHUTTER = 0xD20D
    private const val FNUMBER = 0x5007
    private const val WHITE_BALANCE = 0x5005
    private const val FOCUS_MODE = 0x500A
    private const val FOCUS_TOUCH_SPOT = 0xE004
    private const val FOCUS_TRACKING = 0xE005
    private const val SUBJECT_AF = 0xD060
    private const val MOVIE_REC = 0xD21D
    private const val REC_RESOLUTION = 0xD024
    private const val FILE_FORMAT_MOVIE = 0xD241
    private const val SANDQ_MODE = 0xD051
    private const val PROXY_REC = 0xD109
    private const val MONITOR_LUT = 0xD04D
    private const val COLOR_TEMP = 0xD20F

    fun resolve(properties: List<CameraProperty>, transport: CameraTransport): Set<CameraFeature> {
        val byCode = properties.associateBy { it.code }
        fun present(code: Int) = byCode.containsKey(code)
        fun writable(code: Int) = byCode[code]?.writable == true

        val features = mutableSetOf<CameraFeature>()

        if (present(LIVE_VIEW)) {
            features += CameraFeature.LIVE_VIEW
            // Overlays/markers are rendered on-device from the live-view frame; available whenever
            // live view is (matches how the histogram/zebra pipeline already works).
            features += CameraFeature.WAVEFORM
            features += CameraFeature.MARKERS
        }

        if (writable(ISO)) features += CameraFeature.ISO_CONTROL
        if (writable(SHUTTER)) features += CameraFeature.SHUTTER_CONTROL
        if (writable(FNUMBER)) features += CameraFeature.IRIS_CONTROL
        if (writable(WHITE_BALANCE) || writable(COLOR_TEMP)) features += CameraFeature.WHITE_BALANCE_CONTROL
        if (present(FOCUS_MODE)) features += CameraFeature.FOCUS_CONTROL
        if (present(FOCUS_TOUCH_SPOT)) features += CameraFeature.TOUCH_FOCUS
        if (present(SUBJECT_AF) || present(FOCUS_TRACKING)) {
            features += CameraFeature.SUBJECT_RECOGNITION
            features += CameraFeature.EYE_AF
            features += CameraFeature.FACE_AF
        }
        if (present(MOVIE_REC)) features += CameraFeature.RECORD_CONTROL
        if (present(REC_RESOLUTION) || present(FILE_FORMAT_MOVIE)) features += CameraFeature.REC_FORMAT
        if (present(SANDQ_MODE)) features += CameraFeature.SANDQ
        if (present(PROXY_REC)) features += CameraFeature.PROXY_RECORDING
        if (present(MONITOR_LUT)) features += CameraFeature.PICTURE_PROFILE

        if (transport == CameraTransport.USB) features += CameraFeature.USB

        return features
    }
}
