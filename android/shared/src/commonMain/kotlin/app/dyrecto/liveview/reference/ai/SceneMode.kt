package app.dyrecto.liveview.reference.ai

/**
 * High-level understanding of what kind of scene an image is. Derived from detected objects,
 * embedding availability, and scene statistics — there is deliberately NO dedicated scene
 * classifier model (one may be introduced later only if derivation proves insufficient).
 *
 * ARCHITECTURE and LANDSCAPE are reserved: the current detector vocabulary (COCO) has no
 * building/mountain classes, so they cannot yet be assigned by detection alone. Both use the
 * same signal set as GENERIC_SCENE, so no monitoring capability is lost while they are reserved.
 */
enum class SceneMode {
    HUMAN,
    ANIMAL,
    VEHICLE,
    PRODUCT,
    FOOD,
    ARCHITECTURE,
    LANDSCAPE,
    STREET,
    INTERIOR,
    MULTI_SUBJECT,
    GENERIC_SCENE,
    UNKNOWN,
}
