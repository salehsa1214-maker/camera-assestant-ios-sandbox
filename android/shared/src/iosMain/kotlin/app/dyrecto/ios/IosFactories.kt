package app.dyrecto.ios

import app.dyrecto.domain.CameraConnectionState

/** Swift bridge for Kotlin constructors whose default arguments are not exported through ObjC. */
fun emptyCameraConnectionState(): CameraConnectionState = CameraConnectionState()
