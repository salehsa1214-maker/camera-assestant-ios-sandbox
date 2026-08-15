# MobileCLIP2-S0 assets (Phase 16.1)

The Creative Scene Understanding semantic expert (`MobileClipSemanticEngine`) needs two bundled
assets. Both are produced by the scripts here and committed under `android/app/src/main/assets/`.
The app is fail-safe: if either asset is missing/invalid, the semantic expert latches FAILED and the
analyzer falls back to its deterministic experts.

| Asset | App path | Size | Produced by |
|---|---|---|---|
| Image tower (TFLite, fp32) | `assets/models/mobileclip2_s0_image.tflite` | ~44 MB | `convert_wsl.py` |
| Concept vocabulary (JSON) | `assets/creative/concept_vocab_mobileclip2_s0.json` | ~0.25 MB | `build_concept_vocab.py` |

Model: `hf-hub:timm/MobileCLIP2-S0-OpenCLIP` (512-d, 256×256 input). Only the **image tower** ships and
runs on device; the **text tower** runs offline only, to precompute the concept vectors.

## What actually works (verified on this machine)

The two hard-won findings, baked into `MobileClipSemanticEngine`:
1. **No mean/std normalization.** MobileCLIP2-S0's preprocess is `Normalize(mean=[0,0,0], std=[1,1,1])`
   — raw 0..1 pixels, NOT the OpenAI CLIP mean/std. `CLIP_MEAN/STD` are `[0,0,0]`/`[1,1,1]`.
2. **PyTorch→TFLite via ai-edge-torch, on Linux.** `onnx2tf` on Windows mistranslates the MCi
   token-mixer (NCHW↔NHWC channel juggling). `ai-edge-torch` (renamed `litert-torch`) converts the
   PyTorch model directly and preserves semantics — but it needs Linux (`torch_xla`). We run it in
   **WSL** via `uv` (no root needed). Conversion parity vs PyTorch = **cosine 1.0**.

The exported TFLite input is NCHW `[1,3,256,256]`; the engine auto-detects channel-first. The model
outputs a RAW embedding; the engine L2-normalizes.

## Regenerate

**Concept vocabulary (Windows or Linux, Python 3.11):**
```bash
py -3.11 -m venv .venv && .venv/Scripts/pip install open_clip_torch transformers torch
.venv/Scripts/python build_concept_vocab.py --out ../../android/app/src/main/assets/creative/concept_vocab_mobileclip2_s0.json
```

**Image tower TFLite (WSL / Linux):**
```bash
bash wsl_setup.sh     # uv -> standalone Python 3.11 -> venv -> torch + open_clip + ai-edge-torch
bash run_convert.sh   # runs convert_wsl.py: loads the model, converts, writes the .tflite, asserts parity>0.99
```

`convert_wsl.py` writes straight into the app assets and fails hard if parity < 0.99. Curate the
concept prompt list in `build_concept_vocab.py` against real reference imagery — the phrasing
determines how discriminative each concept is. Concept cosine thresholds live in `SemanticExpert`
(first-pass; calibrate on hardware).

Intermediates (`.venv/`, `onnx/`, `tflite_out/`, `*.log`) are gitignored.
