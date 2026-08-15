package app.dyrecto.liveview.vision.results

/**
 * Marker interface for every Vision module result.
 *
 * [moduleId] identifies the producing [app.dyrecto.liveview.vision.VisionModule],
 * enabling diagnostics, routing, and future Assistant/persistence integration without coupling
 * consumers to a concrete type.
 */
interface VisionResult {
    /** The [app.dyrecto.liveview.vision.VisionModule.id] of the producing module. */
    val moduleId: String
}
