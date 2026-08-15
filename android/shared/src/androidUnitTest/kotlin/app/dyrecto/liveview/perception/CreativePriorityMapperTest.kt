package app.dyrecto.liveview.perception

import app.dyrecto.liveview.reference.ReferenceSignal
import app.dyrecto.liveview.reference.creative.CreativeAspect
import app.dyrecto.liveview.reference.creative.CreativePriority
import app.dyrecto.liveview.reference.creative.CreativeSceneModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure JVM tests for the creative-intent → signal multiplier mapping. */
class CreativePriorityMapperTest {

    private fun model(vararg priorities: Pair<CreativeAspect, Float>) = CreativeSceneModel(
        importance = priorities.associate { (a, w) -> a to CreativePriority(w, "test") },
    )

    @Test
    fun `empty model yields neutral multiplier for every signal`() {
        val empty = CreativeSceneModel()
        for (signal in ReferenceSignal.entries) {
            assertEquals("signal=$signal", 1f, CreativePriorityMapper.multiplierFor(signal, empty), 1e-6f)
        }
    }

    @Test
    fun `high creative weight raises the signal multiplier above one`() {
        val m = model(CreativeAspect.COLOR to 1.0f)
        assertTrue(CreativePriorityMapper.multiplierFor(ReferenceSignal.WHITE_BALANCE, m) > 1f)
    }

    @Test
    fun `low creative weight lowers the signal multiplier below one`() {
        val m = model(CreativeAspect.COLOR to 0.0f)
        assertTrue(CreativePriorityMapper.multiplierFor(ReferenceSignal.WHITE_BALANCE, m) < 1f)
    }

    @Test
    fun `neutral creative weight maps to one`() {
        val m = model(CreativeAspect.LIGHTING to 0.5f)
        assertEquals(1f, CreativePriorityMapper.multiplierFor(ReferenceSignal.EXPOSURE, m), 1e-6f)
    }

    @Test
    fun `composition averages composition and negative-space aspects`() {
        val m = model(
            CreativeAspect.COMPOSITION to 1.0f,
            CreativeAspect.NEGATIVE_SPACE to 1.0f,
        )
        // Both high → strong boost.
        assertTrue(CreativePriorityMapper.multiplierFor(ReferenceSignal.COMPOSITION, m) > 1.2f)
    }

    @Test
    fun `a signal with no contributing aspect present stays neutral`() {
        // Only LIGHTING supplied; WHITE_BALANCE depends on COLOR → unaffected.
        val m = model(CreativeAspect.LIGHTING to 1.0f)
        assertEquals(1f, CreativePriorityMapper.multiplierFor(ReferenceSignal.WHITE_BALANCE, m), 1e-6f)
    }
}
