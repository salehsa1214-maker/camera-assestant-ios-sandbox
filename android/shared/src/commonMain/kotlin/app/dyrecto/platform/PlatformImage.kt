package app.dyrecto.platform

/**
 * Opaque handle to the platform's decoded image type — `android.graphics.Bitmap` on Android
 * (via `actual typealias`, so Android call sites and engine implementations are source-identical),
 * a CVPixelBuffer/UIImage wrapper on iOS. Common code only passes it through to platform engine
 * implementations; all pixel READING in common code goes through
 * [app.dyrecto.liveview.vision.FramePixels] instead.
 */
expect class PlatformImage
