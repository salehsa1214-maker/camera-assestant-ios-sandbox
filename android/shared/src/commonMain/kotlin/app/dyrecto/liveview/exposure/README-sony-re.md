# Sony Histogram / Zebra — reverse-engineering notes (Phase 6)

Source: `proremote 2.6.0` ("Monitor & Control"), decompiled with jadx. These notes justify every
"Sony behaviour" decision in this package. **Rule:** recover Sony where recoverable; where the
behaviour lives in native code / on the camera, implement a clearly-labelled fallback and say so.

## What Sony actually does (recovered)

`com.sony.promobile.external.monitorassist.MonitorAssist`:

- Histogram and Zebra are computed in a **native `.so`**, not in Java:
  - `native void maRenderBitmapHistogram(Bitmap)`
  - `native void maRenderBitmapZebra(Bitmap, int typeId, int p1, int p2, int w, int h)`
- Pixels are handed to native as the **camera Y plane** (not RGB):
  - `storePixels(YuvBufferType {YUV422, YUV420_NV21}, YRangeType, yBuf, u, v, uv, w, h)`
  - `YRangeType { FULL(0-255), LIMITED_16_255, VIDEO_16_235 }` → luma is video/studio-swing,
    16–235 ≈ 0–100 IRE. Zebra thresholds are expressed in that Y-code / IRE space.
- `ZebraType { Zebra1(0), Zebra2(1) }` — two independent zebra patterns.
  Params set via `w(on, ZebraType, i10 -> p1, i11 -> p2)`:
  - **Zebra1 = range band**: p1 = center level, p2 = range → `|Y - center| <= range`.
    (PTP props `MONITOR_ASSIST_ZEBRA1_THRESHOLD` + `MONITOR_ASSIST_ZEBRA1_RANGE`)
  - **Zebra2 = level**: p1 = threshold → `Y >= threshold`.
    (PTP prop `MONITOR_ASSIST_ZEBRA2_THRESHOLD`)
- Histogram bucket count / modes are **camera-reported at runtime**
  (`com.sonymobile.statistics.info.histogramBucketCount`, `availableHistogramModes`); the app just
  renders camera-provided bucket data. There is no hardcoded bucket count in the app.

## What we implement here (and why the fallback)

Our app has only the **decoded live-view JPEG as ARGB-8888** (see `liveview/render`), not the camera
Y plane. So:

- **Luminance**: computed from RGB with **BT.709** integer coefficients — DOCUMENTED FALLBACK, because
  Sony's exact luma is the camera Y plane processed in native code we cannot read. BT.709 is the modern
  HD primary and the closest faithful reconstruction of a Rec.709 Y' from RGB.
- **IRE mapping**: we reuse Sony's **16–235 studio-swing** convention (`VIDEO_16_235`) so our zebra
  thresholds line up with Sony's IRE numbers.
- **Histogram bins**: fixed **256** (per Phase 6 spec) since the camera bucket count is not available
  to our local pipeline. Documented fallback.
- **Zebra model**: we implement both recovered types — `ZebraSpec.Range` (Zebra1) and
  `ZebraSpec.Level` (Zebra2) — in IRE. Preset levels (70 / 95 / 100 + Custom) are the Phase 6 set;
  exact Sony preset arrays are camera-dependent and not hardcoded in the app.
