package app.dyrecto.liveview

/**
 * Immutable Live View pipeline state, published by [LiveViewSession]. Renderer-neutral: the frame is
 * a [LiveViewFrame] (encoded JPEG), never a decoded/Android UI object.
 */
data class LiveViewState(
    val status: Status = Status.IDLE,
    val frame: LiveViewFrame? = null,
    val error: String? = null,
) {
    enum class Status { IDLE, STARTING, STREAMING, ERROR }
}
