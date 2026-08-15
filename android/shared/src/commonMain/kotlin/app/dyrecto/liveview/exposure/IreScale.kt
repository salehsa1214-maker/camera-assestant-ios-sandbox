package app.dyrecto.liveview.exposure

/**
 * Shared luma ↔ IRE mapping for the monitoring overlays (False Color, Waveform scale). Uses the same
 * studio-swing convention documented for [ZebraEngine] (`README-sony-re.md`): code 16 → 0 IRE,
 * code 235 → 100 IRE, so overlay readings line up with the camera's IRE numbers. Full-range white
 * (luma 255) maps to ~109 IRE (super-white), matching Sony's over-100 behaviour.
 */
object IreScale {
    /** luma code (0..255) → IRE (may exceed 100 for super-white / go below 0 for sub-black). */
    fun lumaToIre(luma: Int): Int = ((luma - 16) * 100) / 219

    /** IRE → luma code (inverse), clamped to 0..255. */
    fun ireToLuma(ire: Int): Int = (16 + ire * 219 / 100).coerceIn(0, 255)
}
