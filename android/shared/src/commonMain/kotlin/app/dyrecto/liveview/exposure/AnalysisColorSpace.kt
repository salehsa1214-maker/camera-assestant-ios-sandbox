package app.dyrecto.liveview.exposure

/**
 * Phase 8: the input profile Histogram/Zebra/ExposureAnalyzer should assume when interpreting the
 * decoded Live View frame. Selected from the Developer screen; resolved to an [AnalysisColorTransform]
 * by [ExposureConfig.resolveTransform].
 *
 * HLG and S-Cinetone are intentionally not modeled yet — no reference transform has been supplied for
 * them, and this codebase does not fabricate approximations (see the Phase 8 plan). `AUTO` currently
 * resolves to [Rec709Transform] because no camera telemetry exposes the active recording gamma/picture
 * profile yet; it becomes meaningful once that detection is added.
 */
enum class AnalysisColorSpace {
    AUTO,
    REC709,
    S_LOG3,
}
