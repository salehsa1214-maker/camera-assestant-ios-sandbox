package app.dyrecto.liveview.session

import app.dyrecto.domain.CameraTelemetry
import app.dyrecto.domain.ConnectionPhase

/**
 * Unified realtime context (Phase 4D) — the single entry point future overlays, computer vision,
 * and alert logic consume instead of subscribing to the two underlying streams directly.
 *
 * **This is NOT a synchronized capture snapshot.** The rendered-frame stream and the telemetry
 * stream are independent and not timestamp-correlated; this object is a "latest-wins" merge of
 * both. It never claims the frame and telemetry describe the same instant.
 *
 * Deliberately lightweight and platform-neutral: it carries **no [android.graphics.Bitmap]** (the
 * renderer remains the sole bitmap owner — the UI collects the image separately from the render
 * state) and no Compose types. Telemetry is kept in its native structured form ([CameraTelemetry]);
 * formatting for display happens at the UI edge.
 *
 * @param renderSequence  count of distinct *displayed* frames observed (renderer output), NOT camera
 *   frames. Derived by the publisher; see [FrameContextPublisher].
 * @param observedAtMs    wall-clock when WE observed the renderer's latest new frame — not the
 *   camera capture time.
 * @param frameAgeMs      `now - observedAtMs` at publish time.
 * @param frameFps        displayed FPS (mirror of `LiveViewRenderState.displayedFps`).
 * @param latencyMs       mean submit→publish render latency (aggregate, not per-frame).
 * @param renderActive    whether the render decode loop is running.
 * @param telemetry       latest structured Sony 0x9209 snapshot, as-is (null until first refresh).
 * @param telemetryTimestampMs  when telemetry was last refreshed (`lastTelemetryUpdateAt`), or 0.
 * @param connectionPhase latest pipeline phase (for diagnostics/alerts).
 * @param liveViewActive  whether Live View is currently delivering frames.
 */
data class FrameContext(
    // --- frame metadata (NOT the bitmap itself) ---
    val renderSequence: Long = 0,
    val observedAtMs: Long = 0,
    val frameAgeMs: Long = 0,
    val frameFps: Double = 0.0,
    val latencyMs: Double = 0.0,
    val renderActive: Boolean = false,

    // --- telemetry kept in native structured form (no flattening to display Strings) ---
    val telemetry: CameraTelemetry? = null,
    val telemetryTimestampMs: Long = 0,

    // --- connection status (for future diagnostics/alerts; no protocol change) ---
    val connectionPhase: ConnectionPhase = ConnectionPhase.IDLE,
    val liveViewActive: Boolean = false,
)
