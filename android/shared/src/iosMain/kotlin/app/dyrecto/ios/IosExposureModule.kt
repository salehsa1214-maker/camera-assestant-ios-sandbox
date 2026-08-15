@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package app.dyrecto.ios

import app.dyrecto.liveview.exposure.AdaptiveSampler
import app.dyrecto.liveview.exposure.AnalysisRegionRegistry
import app.dyrecto.liveview.exposure.ColorAccumulator
import app.dyrecto.liveview.exposure.ExposureAnalyzer
import app.dyrecto.liveview.exposure.ExposureConfig
import app.dyrecto.liveview.exposure.ExposureStateMachine
import app.dyrecto.liveview.exposure.HistogramEngine
import app.dyrecto.liveview.exposure.LumaField
import app.dyrecto.liveview.exposure.LuminanceAnalyzer
import app.dyrecto.liveview.exposure.RegionLumaStats
import app.dyrecto.liveview.vision.results.ColorStatsResult
import app.dyrecto.liveview.vision.results.SubjectExposureResult
import app.dyrecto.liveview.vision.results.VisionResult
import app.dyrecto.liveview.exposure.ZebraEngine
import app.dyrecto.platform.nanoTime
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/**
 * iOS exposure module — a line-by-line mirror of the Android `ExposureModule` orchestration,
 * kept in Kotlin/Native so the entire per-frame hot path (RGBA→ARGB conversion + luma scan +
 * histogram/zebra/verdict + subject-region stats) runs in compiled native code with ZERO
 * per-pixel Swift↔Kotlin bridging. The Swift vision pipeline hands in one RGBA8888 NSData per
 * analyzed frame and receives the same VisionResult list Android's module emits.
 *
 * One scan, shared LumaField, reusable buffers, adaptive stride — all Android parity.
 * Not thread-safe: drive from the single vision worker, exactly like Android.
 */
class IosExposureModule {

    val id: String = "exposure"

    private val lumaField = LumaField()
    private val histogramEngine = HistogramEngine(id)
    private val zebraEngine = ZebraEngine(id)
    private val sampler = AdaptiveSampler()
    private val colorAccumulator = ColorAccumulator()
    private val exposureState = ExposureStateMachine()

    /** Reusable ARGB scratch buffer; grows, never shrinks (Android parity). */
    private var pixels: IntArray = IntArray(0)
    /** Reusable RGBA byte staging buffer for the single memcpy out of NSData. */
    private var rgba: ByteArray = ByteArray(0)

    fun analyze(rgbaData: NSData, width: Int, height: Int): List<VisionResult> {
        if (width <= 0 || height <= 0) return emptyList()
        val byteCount = width * height * 4
        if (rgbaData.length.toInt() < byteCount) return emptyList()

        val startNs = nanoTime()

        // ONE bulk copy of the frame (the getPixels stand-in), then a native conversion loop.
        if (rgba.size < byteCount) rgba = ByteArray(byteCount)
        rgba.usePinned { pinned ->
            memcpy(pinned.addressOf(0), rgbaData.bytes, byteCount.toULong())
        }
        val needed = width * height
        if (pixels.size < needed) pixels = IntArray(needed)
        var s = 0
        for (i in 0 until needed) {
            val r = rgba[s].toInt() and 0xFF
            val g = rgba[s + 1].toInt() and 0xFF
            val b = rgba[s + 2].toInt() and 0xFF
            val a = rgba[s + 3].toInt() and 0xFF
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            s += 4
        }

        val stride = sampler.stride
        val luma = LuminanceAnalyzer.analyze(
            pixels, width, height, stride, lumaField, ExposureConfig.resolveTransform(),
            colorAccumulator,
        )

        val histogram = histogramEngine.compute(luma)
        val zebra = zebraEngine.compute(luma, ExposureConfig.zebraSpec.value)
        val exposure = exposureState.update(ExposureAnalyzer.analyze(histogram, zebra))
        val colorStats = ColorStatsResult(
            moduleId = id,
            avgR = colorAccumulator.avgR,
            avgG = colorAccumulator.avgG,
            avgB = colorAccumulator.avgB,
            sampleCount = colorAccumulator.sampleCount,
        )

        // Subject-region stats from the SAME pixel buffer (Phase 12) — region set by the
        // reference monitor through the shared AnalysisRegionRegistry, identical to Android.
        val region = AnalysisRegionRegistry.subjectRegion
        val subjectExposure = region?.let {
            RegionLumaStats.compute(
                argb = pixels, width = width, height = height, region = it,
                stride = stride, transform = ExposureConfig.resolveTransform(),
            )
        }?.let { SubjectExposureResult(moduleId = id, stats = it) }

        sampler.record(nanoTime() - startNs)

        return if (subjectExposure != null) {
            listOf(histogram, zebra, exposure, colorStats, subjectExposure)
        } else {
            listOf(histogram, zebra, exposure, colorStats)
        }
    }
}
