package app.dyrecto.liveview.instructions

/**
 * The corrective camera action the assistant recommends (Phase 13). Purely presentational — the
 * perceptual verdict upstream already decided WHAT is wrong; an action only says what the
 * operator should DO about it.
 *
 * ZOOM_IN/ZOOM_OUT are reserved: size drift currently maps to MOVE_CLOSER/MOVE_BACK because the
 * pipeline cannot distinguish a lens move from a dolly move yet.
 */
enum class AssistantAction {
    PAN_LEFT,
    PAN_RIGHT,
    TILT_UP,
    TILT_DOWN,
    MOVE_CLOSER,
    MOVE_BACK,
    ZOOM_IN,
    ZOOM_OUT,
    INCREASE_EXPOSURE,
    REDUCE_EXPOSURE,
    COOL_WHITE_BALANCE,
    WARM_WHITE_BALANCE,
    WAIT_FOR_SUBJECT,
    REFRAME,
    MATCH_REFERENCE,
    NONE,
}

/**
 * One translated operator instruction — the final output of the Assistant Instruction Layer.
 * No business logic lives here; [InstructionTranslator] builds it from the existing perceptual
 * verdict and [InstructionSelector] picks the one to surface.
 */
data class AssistantInstruction(
    val action: AssistantAction,
    /** Operator-facing sentence, e.g. "Pan the camera left slightly." */
    val message: String,
    /** 0..1 — the perceptual confidence that the underlying difference exists. */
    val confidence: Float,
    /** 0..1 progress toward matching the reference (1 = matched); reuses the signal score. */
    val progress: Float,
    /** The original perceptual description this instruction was translated from (diagnostics). */
    val reason: String,
)
