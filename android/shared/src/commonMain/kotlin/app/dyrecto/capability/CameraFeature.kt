package app.dyrecto.capability

/**
 * Higher-level, camera-agnostic capabilities the rest of the app gates on. UI and logic switch on
 * these — never on raw property codes. Resolved at runtime by [FeatureResolver] from the observed
 * properties + transport, so a model that doesn't report a given property simply lacks the feature.
 *
 * Adding support for a new camera is mostly mapping its (previously unknown) properties/commands
 * onto these features — a data change in the resolver, not a protocol change.
 */
enum class CameraFeature(val displayName: String) {
    LIVE_VIEW("Live View"),
    AUDIO("Audio Monitoring"),
    WAVEFORM("Waveform / Overlays"),
    TOUCH_FOCUS("Touch Focus"),
    EYE_AF("Eye AF"),
    FACE_AF("Face AF"),
    SUBJECT_RECOGNITION("Subject Recognition"),
    ISO_CONTROL("ISO / Gain Control"),
    SHUTTER_CONTROL("Shutter Control"),
    IRIS_CONTROL("Iris / Aperture Control"),
    ND_CONTROL("ND Filter Control"),
    WHITE_BALANCE_CONTROL("White Balance Control"),
    FOCUS_CONTROL("Focus Control"),
    ZOOM_CONTROL("Zoom Control"),
    PTZ("Pan / Tilt / Zoom"),
    PICTURE_PROFILE("Picture Profile / Look"),
    RECORD_CONTROL("Record Start / Stop"),
    REC_FORMAT("Recording Format"),
    SANDQ("Slow & Quick Motion"),
    PROXY_RECORDING("Proxy Recording"),
    MARKERS("Markers / Framing Guides"),
    MULTI_CAMERA("Multi-Camera"),
    USB("USB Transport"),
}
