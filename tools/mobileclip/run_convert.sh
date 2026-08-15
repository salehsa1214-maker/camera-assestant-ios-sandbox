#!/usr/bin/env bash
set -e
export HF_HUB_DISABLE_SYMLINKS_WARNING=1
export PATH="$HOME/.local/bin:$PATH"
"$HOME/mcvenv/bin/python" "/mnt/c/Users/saleh/Desktop/camera assistant/tools/mobileclip/convert_wsl.py"
