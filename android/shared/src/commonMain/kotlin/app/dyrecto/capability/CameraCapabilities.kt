package app.dyrecto.capability

import kotlinx.serialization.Serializable

/**
 * A complete, camera-agnostic snapshot of what a connected camera can do right now: its identity,
 * every property it reports (known and unknown), and the resolved high-level feature set. This is
 * the single object the app reasons about instead of model-specific logic.
 *
 * [properties] is a list (not a map) so it serializes cleanly for the capability report; use
 * [property] / [byCode] for lookups.
 */
@Serializable
data class CameraCapabilities(
    val schemaVersion: Int = CapabilitySchema.VERSION,
    val info: CameraInfo = CameraInfo.UNKNOWN,
    val properties: List<CameraProperty> = emptyList(),
    val features: Set<CameraFeature> = emptySet(),
) {
    /** Codes the camera reported that the app does not yet recognize (preserved, not discarded). */
    val unknownPropertyCodes: List<Int> get() = properties.filter { !it.known }.map { it.code }

    private val byCode: Map<Int, CameraProperty> by lazy { properties.associateBy { it.code } }

    fun property(code: Int): CameraProperty? = byCode[code]

    fun supports(feature: CameraFeature): Boolean = feature in features

    companion object {
        val EMPTY = CameraCapabilities()
    }
}
