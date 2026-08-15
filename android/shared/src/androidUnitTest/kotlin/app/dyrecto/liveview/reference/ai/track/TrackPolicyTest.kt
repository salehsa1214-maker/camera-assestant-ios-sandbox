package app.dyrecto.liveview.reference.ai.track

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPolicyTest {

    private val config = TrackPolicyConfig()

    private fun input(
        monitoringActive: Boolean = true,
        hasActiveTrack: Boolean = true,
        lowConfidenceFrames: Int = 0,
        msSinceVerify: Long = 0,
        embeddingSimilarity: Float? = null,
        lumaDelta: Float? = null,
    ) = TrackPolicy.Input(
        monitoringActive = monitoringActive,
        hasActiveTrack = hasActiveTrack,
        consecutiveLowConfidenceFrames = lowConfidenceFrames,
        msSinceLastVerify = msSinceVerify,
        embeddingSimilarityToVerified = embeddingSimilarity,
        meanLumaDeltaSinceVerified = lumaDelta,
    )

    @Test
    fun `inactive monitoring never triggers the detector`() {
        assertEquals(
            DetectorTrigger.NONE,
            TrackPolicy.decide(input(monitoringActive = false, hasActiveTrack = false), config),
        )
    }

    @Test
    fun `missing track triggers INIT`() {
        assertEquals(DetectorTrigger.INIT, TrackPolicy.decide(input(hasActiveTrack = false), config))
    }

    @Test
    fun `confirmed loss triggers recovery`() {
        assertEquals(
            DetectorTrigger.LOSS_RECOVERY,
            TrackPolicy.decide(input(lowConfidenceFrames = config.lossFramesToDeclare), config),
        )
    }

    @Test
    fun `one low-confidence frame does not trigger recovery`() {
        assertEquals(DetectorTrigger.NONE, TrackPolicy.decide(input(lowConfidenceFrames = 1), config))
    }

    @Test
    fun `embedding similarity drop triggers scene change`() {
        assertEquals(
            DetectorTrigger.SCENE_CHANGE,
            TrackPolicy.decide(input(embeddingSimilarity = 0.5f), config),
        )
    }

    @Test
    fun `mean luma shift triggers scene change`() {
        assertEquals(
            DetectorTrigger.SCENE_CHANGE,
            TrackPolicy.decide(input(lumaDelta = 80f), config),
        )
    }

    @Test
    fun `stable scene with recent verify triggers nothing`() {
        assertEquals(
            DetectorTrigger.NONE,
            TrackPolicy.decide(
                input(msSinceVerify = 1_000, embeddingSimilarity = 0.95f, lumaDelta = 3f),
                config,
            ),
        )
    }

    @Test
    fun `verify heartbeat fires after the interval`() {
        assertEquals(
            DetectorTrigger.PERIODIC_VERIFY,
            TrackPolicy.decide(input(msSinceVerify = config.verifyIntervalMs), config),
        )
    }

    @Test
    fun `loss outranks scene change and heartbeat`() {
        assertEquals(
            DetectorTrigger.LOSS_RECOVERY,
            TrackPolicy.decide(
                input(
                    lowConfidenceFrames = config.lossFramesToDeclare,
                    embeddingSimilarity = 0.1f,
                    msSinceVerify = 100_000,
                ),
                config,
            ),
        )
    }
}
