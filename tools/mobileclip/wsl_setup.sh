#!/usr/bin/env bash
# No-root setup in WSL: uv -> standalone Python 3.11 -> venv -> ai-edge-torch (Linux-native).
set -e
export PATH="$HOME/.local/bin:$HOME/.cargo/bin:$PATH"

if ! command -v uv >/dev/null 2>&1; then
  echo "installing uv..."
  curl -LsSf https://astral.sh/uv/install.sh | sh
fi
export PATH="$HOME/.local/bin:$HOME/.cargo/bin:$PATH"
uv --version

uv python install 3.11
uv venv --python 3.11 "$HOME/mcvenv"
source "$HOME/mcvenv/bin/activate"

# CPU torch (avoid pulling multi-GB CUDA wheels for a one-shot conversion).
uv pip install --index-strategy unsafe-best-match \
  --extra-index-url https://download.pytorch.org/whl/cpu \
  ai-edge-torch ai-edge-litert torch open_clip_torch transformers numpy pillow

python - <<'PY'
import torch, ai_edge_torch
print("torch", torch.__version__)
print("ai_edge_torch OK")
PY
