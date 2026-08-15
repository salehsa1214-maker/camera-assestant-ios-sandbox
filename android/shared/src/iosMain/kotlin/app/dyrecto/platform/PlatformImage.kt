package app.dyrecto.platform

/**
 * iOS side of the opaque platform image handle. The Swift frame/import pipeline wraps its native
 * image (UIImage / CVPixelBuffer / CGImage — whatever the producing side holds) and the Swift
 * engine implementations unwrap it; Kotlin common code only passes it through.
 */
actual class PlatformImage(val handle: Any)
