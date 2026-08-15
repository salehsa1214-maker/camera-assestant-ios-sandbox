package app.dyrecto.liveview.vision.modules

import app.dyrecto.liveview.exposure.AdaptiveSampler
import app.dyrecto.liveview.exposure.AnalysisRegion
import app.dyrecto.liveview.exposure.AnalysisRegionRegistry
import app.dyrecto.liveview.exposure.ColorAccumulator
import app.dyrecto.liveview.exposure.RegionLumaStats
import app.dyrecto.liveview.exposure.ExposureAnalyzer
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.ExposureStateMachine
import app.dyrecto.liveview.exposure.FalseColorEngine
import app.dyrecto.liveview.exposure.FocusPeakingEngine
import app.dyrecto.liveview.exposure.HistogramEngine
import app.dyrecto.liveview.exposure.LumaField
import app.dyrecto.liveview.exposure.LuminanceAnalyzer
import app.dyrecto.liveview.exposure.OverlayConfig
import app.dyrecto.liveview.exposure.WaveformEngine
import app.dyrecto.liveview.exposure.ZebraEngine
import app.dyrecto.liveview.exposure.ZebraSpec
import app.dyrecto.liveview.vision.FrameAnalysisRequest
import app.dyrecto.liveview.vision.VisionModule
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.SubjectExposureResult
import app.dyrecto.liveview.vision.results.VisionResult

/**
 * The single Vision module for exposure analysis (Phase 6). Replaces the temporary `HighlightModule`
 * + `ShadowModule`.
 *
 * It performs the *one* per-frame pixel scan and fans the resulting shared
 * [app.dyrecto.liveview.exposure.LumaField] out to the pure engines, emitting three
 * results in one pass:
 *  - [app.dyrecto.liveview.vision.results.HistogramResult]
 *  - [app.dyrecto.liveview.vision.results.ZebraResult]
 *  - [app.dyrecto.liveview.vision.results.ExposureResult]
 *
 * A single module (not one per tool) is required by the "one scan, shared luminance" mandate: the
 * pipeline runs modules independently, so two modules would each rescan the bitmap.
 *
 * Owns all reusable buffers (pixel array, [LumaField], engine working buffers) and the
 * [AdaptiveSampler]. Runs only on the Vision worker thread (single-threaded), so the mutable state
 * here needs no synchronization.
 */
class ExposureModule(
    /**
     * Supplies the active zebra threshold per frame. Defaults to the UI-configurable
     * [ExposureConfig] (100 IRE clipping until changed); the zebra coverage also drives highlight
     * detection. Injectable for tests.
     */
    private val zebraSpecProvider: () -> ZebraSpec = { ExposureConfig.zebraSpec.value },
    /**
     * Phase 12: supplies the current subject region (normalized) for region exposure stats.
     * Defaults to [AnalysisRegionRegistry], written by the reference monitor. Null = no region
     * → no [SubjectExposureResult] emitted, zero extra work.
     */
    private val subjectRegionProvider: () -> AnalysisRegion? = { AnalysisRegionRegistry.subjectRegion },
) : VisionModule {

    override val id: String = "exposure"

    private val lumaField = LumaField()
    private val histogramEngine = HistogramEngine(id)
    private val zebraEngine = ZebraEngine(id)
    /** Phone-side monitoring overlays; run only when enabled via [OverlayConfig] (else zero cost). */
    private val waveformEngine = WaveformEngine(id)
    private val falseColorEngine = FalseColorEngine(id)
    private val focusPeakingEngine = FocusPeakingEngine(id)
    private val sampler = AdaptiveSampler()
    /** Phase 9: average-RGB accumulator fused into the same scan (Shot Reference color input). */
    private val colorAccumulator = ColorAccumulator()
    /** Phase 7: turns the per-frame verdict into confirmed, debounced highlight/shadow loss. */
    private val exposureState = ExposureStateMachine()

    /** Reusable ARGB scratch buffer for [app.dyrecto.liveview.vision.FramePixels.readArgb]; grows, never shrinks. */
    private var pixels: IntArray = IntArray(0)

    override suspend fun analyze(request: FrameAnalysisRequest): List<VisionResult> {
        if (!request.isAvailable) return emptyList()

        val width = request.width
        val height = request.height
        if (width <= 0 || height <= 0) return emptyList()

        val startNs = System.nanoTime()

        val needed = width * height
        if (pixels.size < needed) pixels = IntArray(needed)
        // One bulk read of the frame (no frame object copy) through the portable FramePixels
        // boundary — on Android this is the same Bitmap.getPixels call as before. The stride below
        // reduces the O(pixels) luma/histogram/zebra compute — the dominant cost — on slower devices.
        request.readArgb(pixels)

        val stride = sampler.stride
        val luma = LuminanceAnalyzer.analyze(
            pixels, width, height, stride, lumaField, ExposureConfig.resolveTransform(),
            colorAccumulator,
        )

        val histogram = histogramEngine.compute(luma)
        val zebra = zebraEngine.compute(luma, zebraSpecProvider())
        val exposure = exposureState.update(ExposureAnalyzer.analyze(histogram, zebra))
        val colorStats = ColorStatsResult(
            moduleId = id,
            avgR = colorAccumulator.avgR,
            avgG = colorAccumulator.avgG,
            avgB = colorAccumulator.avgB,
            sampleCount = colorAccumulator.sampleCount,
        )

        // Phase 12: subject-region exposure stats from the SAME pixel buffer (region-only walk,
        // never a second Bitmap read). Emitted only while a region is registered.
        val region = subjectRegionProvider()
        val subjectExposure = region?.let {
            RegionLumaStats.compute(
                argb = pixels, width = width, height = height, region = it,
                stride = stride, transform = ExposureConfig.resolveTransform(),
            )
        }?.let { SubjectExposureResult(moduleId = id, stats = it) }

        val results = ArrayList<VisionResult>(8)
        results.add(histogram)
        results.add(zebra)
        results.add(exposure)
        results.add(colorStats)
        subjectExposure?.let { results.add(it) }

        // Monitoring overlays reuse the SAME LumaField — computed only when the user turned them on,
        // so a disabled overlay costs nothing and doesn't consume the AdaptiveSampler budget.
        if (OverlayConfig.waveform.value) results.add(waveformEngine.compute(luma))
        if (OverlayConfig.falseColor.value) results.add(falseColorEngine.compute(luma))
        if (OverlayConfig.focusPeaking.value) results.add(focusPeakingEngine.compute(luma, generateMask = true))

        // Feed the whole-frame processing time (incl. any overlays) back into the adaptive stride controller.
        sampler.record(System.nanoTime() - startNs)

        return results
    }
}
