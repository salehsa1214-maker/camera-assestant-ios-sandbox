package app.dyrecto.liveview.scene

import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.TelemetryProp
import app.dyrecto.liveview.session.FrameContext
import app.dyrecto.liveview.vision.results.ExposureResult
import app.dyrecto.liveview.vision.results.ExposureVerdict
import app.dyrecto.liveview.vision.results.EyeDetectionResult
import app.dyrecto.liveview.vision.results.FaceDetectionResult
import app.dyrecto.liveview.vision.results.VisionContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure unit tests for [SceneAnalyzer]. No Android types are constructed (no Bitmap/Rect), so
 * these run on the plain JVM like the other [app.dyrecto.liveview.vision] tests.
 */
class SceneAnalyzerTest {

    /** Build a [CameraTelemetry] from (code -> rawNumber) pairs. */
    private fun telemetry(vararg props: Pair<Int, Long>): CameraTelemetry =
        CameraTelemetry(
            props.associate { (code, raw) ->
                code to TelemetryProp(
                    code = code,
                    label = CameraTelemetry.labelFor(code),
                    rawValue = raw.toString(),
                    dataType = 0,
                    rawNumber = raw,
                )
            },
        )

    /**
     * Builds an [ExposureResult] the way the Phase 6+7 pipeline would. Phase 7:
     * [SceneAnalyzer]'s `isHighlightClipped` / `isShadowClipped` now read `highlightConfirmed` /
     * `shadowConfirmed` (the debounced state-machine output), not the single-frame
     * `highlightDetected` / `shadowDetected` — so these tests drive `*Confirmed` directly, mirroring
     * whatever [app.dyrecto.liveview.exposure.ExposureStateMachine] would have
     * produced after persistence/recovery.
     */
    private fun exposure(
        highlightCoverage: Float = 0f,
        shadowCoverage: Float = 0f,
        highlightConfirmed: Boolean = highlightCoverage > 1f,
        shadowConfirmed: Boolean = shadowCoverage > 1f,
    ) = ExposureResult(
        moduleId = "exposure",
        highlightDetected = highlightCoverage > 1f,
        shadowDetected = shadowCoverage > 1f,
        highlightCoverage = highlightCoverage,
        shadowCoverage = shadowCoverage,
        exposureState = ExposureVerdict.NORMAL, // not read by SceneAnalyzer
        exposureConfidence = 1f,
        highlightConfirmed = highlightConfirmed,
        shadowConfirmed = shadowConfirmed,
    )

    private fun faces(n: Int) =
        FaceDetectionResult("faceeye", n, emptyList(), 0f, 0L)

    private fun eyes(n: Int) =
        EyeDetectionResult("faceeye", n, emptyList(), 0f, 0L)

    @Test fun highlightOnly() {
        val ctx = SceneAnalyzer.analyze(
            FrameContext(),
            VisionContext(exposure = exposure(highlightCoverage = 40f)),
            1L,
        )
        assertTrue(ctx.exposure.isHighlightClipped)
        assertFalse(ctx.exposure.isShadowClipped)
        assertFalse(ctx.face.hasVisibleFace)
        assertEquals(SceneState.HIGHLIGHT_PRESENT, ctx.overallState)
    }

    @Test fun shadowOnly() {
        val ctx = SceneAnalyzer.analyze(
            FrameContext(),
            VisionContext(exposure = exposure(shadowCoverage = 30f)),
            1L,
        )
        assertTrue(ctx.exposure.isShadowClipped)
        assertFalse(ctx.exposure.isHighlightClipped)
        assertEquals(SceneState.SHADOW_PRESENT, ctx.overallState)
    }

    @Test fun facePresent() {
        val ctx = SceneAnalyzer.analyze(
            FrameContext(),
            VisionContext(faces = faces(1), eyes = eyes(2)),
            1L,
        )
        assertTrue(ctx.face.hasVisibleFace)
        assertTrue(ctx.face.hasVisibleEyes)
        assertEquals(1, ctx.face.facesDetected)
        assertEquals(2, ctx.face.eyesDetected)
        assertEquals(SceneState.FACE_VISIBLE, ctx.overallState)
    }

    @Test fun noFace() {
        val ctx = SceneAnalyzer.analyze(FrameContext(), VisionContext(), 1L)
        assertFalse(ctx.face.hasVisibleFace)
        assertFalse(ctx.face.hasVisibleEyes)
        assertEquals(SceneState.NORMAL, ctx.overallState)
    }

    @Test fun recordingState() {
        val recording = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.MOVIE_REC to 1L)),
            VisionContext(),
            1L,
        )
        assertTrue(recording.recording.isRecording)
        assertEquals(SceneState.RECORDING, recording.overallState)

        val notRecording = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.MOVIE_REC to 0L)),
            VisionContext(),
            1L,
        )
        assertFalse(notRecording.recording.isRecording)

        val absent = SceneAnalyzer.analyze(FrameContext(), VisionContext(), 1L)
        assertFalse(absent.recording.isRecording)
    }

    @Test fun recordingDurationAvailability() {
        val withTime = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.REC_TIME to 1234L)),
            VisionContext(),
            1L,
        )
        assertTrue(withTime.recording.recordingDurationAvailable)

        val withoutTime = SceneAnalyzer.analyze(FrameContext(), VisionContext(), 1L)
        assertFalse(withoutTime.recording.recordingDurationAvailable)
    }

    @Test fun batteryLow() {
        val low = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.BATTERY to 15L)),
            VisionContext(),
            1L,
        )
        assertEquals(15, low.battery.batteryLevel)
        assertTrue(low.battery.isBatteryLow)
        assertEquals(SceneState.LOW_BATTERY, low.overallState)

        val high = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.BATTERY to 72L)),
            VisionContext(),
            1L,
        )
        assertEquals(72, high.battery.batteryLevel)
        assertFalse(high.battery.isBatteryLow)

        val unknown = SceneAnalyzer.analyze(FrameContext(), VisionContext(), 1L)
        assertNull(unknown.battery.batteryLevel)
        assertFalse(unknown.battery.isBatteryLow)
    }

    @Test fun storageLow() {
        // 2L = "No card" / error class on SLOT1_STATUS.
        val critical = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.SLOT1_STATUS to 2L)),
            VisionContext(),
            1L,
        )
        assertTrue(critical.storage.isStorageCritical)
        assertEquals(SceneState.STORAGE_LOW, critical.overallState)

        // 1L = OK.
        val ok = SceneAnalyzer.analyze(
            FrameContext(telemetry = telemetry(CameraTelemetry.SLOT1_STATUS to 1L)),
            VisionContext(),
            1L,
        )
        assertFalse(ok.storage.isStorageCritical)

        // Zero remaining is also critical.
        val zeroRemain = SceneAnalyzer.analyze(
            FrameContext(
                telemetry = telemetry(
                    CameraTelemetry.SLOT1_STATUS to 1L,
                    CameraTelemetry.SLOT1_REMAIN to 0L,
                ),
            ),
            VisionContext(),
            1L,
        )
        assertTrue(zeroRemain.storage.isStorageCritical)
    }

    @Test fun combinedInputs() {
        // Recording + face + highlight present, healthy battery/storage.
        val ctx = SceneAnalyzer.analyze(
            FrameContext(
                telemetry = telemetry(
                    CameraTelemetry.MOVIE_REC to 1L,
                    CameraTelemetry.BATTERY to 80L,
                    CameraTelemetry.SLOT1_STATUS to 1L,
                ),
            ),
            VisionContext(
                exposure = exposure(highlightCoverage = 8f, shadowCoverage = 3f),
                faces = faces(1),
                eyes = eyes(2),
            ),
            42L,
        )
        assertEquals(42L, ctx.updatedAtMs)
        assertTrue(ctx.recording.isRecording)
        assertTrue(ctx.face.hasVisibleFace)
        assertTrue(ctx.exposure.isHighlightClipped)
        assertFalse(ctx.battery.isBatteryLow)
        assertFalse(ctx.storage.isStorageCritical)
        // Selection order: recording wins over face/highlight when battery+storage are fine.
        assertEquals(SceneState.RECORDING, ctx.overallState)
    }

    @Test fun latestValuesReplacePrevious() {
        val first = SceneAnalyzer.analyze(
            FrameContext(),
            VisionContext(exposure = exposure(highlightCoverage = 10f)),
            1L,
        )
        assertEquals(10f, first.exposure.highlightPercentage, 0.001f)

        val second = SceneAnalyzer.analyze(
            FrameContext(),
            VisionContext(exposure = exposure(highlightCoverage = 90f)),
            2L,
        )
        assertEquals(90f, second.exposure.highlightPercentage, 0.001f)
        assertEquals(2L, second.updatedAtMs)
    }
}
