package app.dyrecto.capability

/**
 * Version of the normalized capability model (NOT the camera firmware or wire protocol).
 *
 * Everything the app persists or exports (capability reports, reference-linked settings) carries
 * this stamp so a future schema revision can be recognized and migrated without redesigning the
 * capability system. Consumers must tolerate an unknown (higher) version by treating unrecognized
 * fields as [app.dyrecto.capability.CameraProperty.known] == false rather than failing.
 *
 * v1 — initial tri-model (CameraProperty / CameraFeature / CameraEvent) + CameraInfo.
 */
object CapabilitySchema {
    const val VERSION: Int = 1
}
