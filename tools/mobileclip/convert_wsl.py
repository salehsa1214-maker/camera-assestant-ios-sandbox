"""WSL/Linux: convert the MobileCLIP2-S0 image tower to TFLite via ai-edge-torch (litert-torch).

Direct PyTorch->TFLite (no ONNX/onnx2tf channel juggling). Exports the RAW image embedding; the
Android engine L2-normalizes. Writes straight into the app assets and validates parity vs PyTorch.
"""
import os
import numpy as np
import torch
import open_clip
import litert_torch as ai_edge_torch  # ai-edge-torch renamed to litert-torch
from ai_edge_litert.interpreter import Interpreter

OUT = "/mnt/c/Users/saleh/Desktop/camera assistant/android/app/src/main/assets/models/mobileclip2_s0_image.tflite"
NAME = "hf-hub:timm/MobileCLIP2-S0-OpenCLIP"


class ImageTower(torch.nn.Module):
    def __init__(self, m):
        super().__init__()
        self.m = m

    def forward(self, x):
        return self.m.encode_image(x)  # raw 512-d embedding; engine normalizes


def main():
    model, _, _ = open_clip.create_model_and_transforms(NAME)
    model.eval()
    tower = ImageTower(model).eval()

    sample = (torch.rand(1, 3, 256, 256),)
    with torch.no_grad():
        ref = tower(*sample).numpy()[0]
    assert ref.shape[-1] == 512, ref.shape

    edge = ai_edge_torch.convert(tower, sample)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    edge.export(OUT)
    print("wrote", OUT, os.path.getsize(OUT), "bytes")

    interp = Interpreter(model_path=OUT)
    interp.allocate_tensors()
    ind = interp.get_input_details()[0]
    outd = interp.get_output_details()[0]
    x = sample[0].numpy().astype(np.float32)
    if list(ind["shape"]) != list(x.shape):
        x = np.transpose(x, (0, 2, 3, 1))  # NHWC fallback
    interp.set_tensor(ind["index"], x)
    interp.invoke()
    got = interp.get_tensor(outd["index"])[0]

    r = ref / (np.linalg.norm(ref) + 1e-9)
    g = got / (np.linalg.norm(got) + 1e-9)
    cos = float(np.dot(r, g))
    print("tflite input shape", list(ind["shape"]), "output dim", got.shape[-1])
    print("PARITY cosine vs PyTorch:", round(cos, 6))
    assert cos > 0.99, "parity FAILED — do not ship"
    print("PARITY OK")


if __name__ == "__main__":
    main()
