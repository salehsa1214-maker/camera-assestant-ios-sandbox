package app.dyrecto.liveview.veric

import app.dyrecto.platform.epochMillis

/**
 * Incremental facade over [VericFrameExtractor]: feeds byte chunks, logs `[VERIC]` diagnostics for
 * every emitted frame and every recovered error, and forwards frames to a synchronous, zero-copy
 * sink.
 *
 * The sink receives a [VericFrameRef] borrowing the parser buffer; it must consume the bytes before
 * returning (e.g. decode, copy, or count). Callers that need to retain a frame call
 * [VericFrameRef.materialize] themselves.
 *
 * @param verbose when true, logs one line per emitted frame (number, JPEG size, header size, bytes
 *   consumed, remaining buffered bytes). Errors are always logged.
 * @param logLine sink for the `[VERIC]`-prefixed diagnostic lines (the app wires this to BleLog).
 */
class VericParser(
    private val onFrame: (VericFrameRef) -> Unit,
    private val verbose: Boolean = true,
    clock: () -> Long = ::epochMillis,
    private val logLine: (String) -> Unit = {},
) {
    private val extractor = VericFrameExtractor(
        onFrame = ::emit,
        onError = { reason -> log("malformed: $reason") },
        clock = clock,
    )

    private fun emit(ref: VericFrameRef) {
        if (verbose) {
            log(
                "frame #${ref.index}: jpeg=${ref.jpegLength}B header=${ref.headerLength}B " +
                    "consumed=${extractor.bytesConsumed} buffered=${extractor.bufferedBytes}B"
            )
        }
        onFrame(ref)
    }

    fun parse(chunk: ByteArray, off: Int, len: Int) = extractor.append(chunk, off, len)
    fun parse(chunk: ByteArray, len: Int) = extractor.append(chunk, 0, len)

    val framesEmitted: Int get() = extractor.framesEmitted
    val errors: Int get() = extractor.errors

    private fun log(m: String) = logLine("[VERIC] $m")
}
