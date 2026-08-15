# Dyrecto iOS — Mac bring-up guide

Everything in this directory was authored on a Windows host, where Apple toolchains cannot run.
The Kotlin shared module, the Android app, and all JVM tests are verified green; the Swift
sources and `iosMain` Kotlin are **written to production completeness but have never been
compiled**. The first Mac session is therefore: generate → resolve deps → compile (fixing
mechanical interop/API mismatches) → deploy → hardware-validate. There is no architectural or
feature work left by design.

## Prerequisites

- macOS with Xcode 15+ (iOS 16 SDK), an Apple Developer account.
- JDK 17 (or Android Studio's JBR) for Gradle.
- Homebrew tools: `brew install xcodegen cocoapods`.

## Bring-up steps

1. **Shared framework smoke test** (first Kotlin/Native compile of `commonMain` + `iosMain`):
   ```sh
   cd android
   ./gradlew :shared:compileKotlinIosSimulatorArm64
   ```
   Fix anything the K/N compiler flags in `shared/src/iosMain/` (written blind on Windows —
   likely candidates: `kotlin.concurrent.AtomicLong` member names, cinterop signatures in
   `RgbaFramePixels`/`IosExposureModule`).

2. **Generate the Xcode project + pods**:
   ```sh
   cd ../iosApp
   xcodegen generate
   pod install
   open Dyrecto.xcworkspace
   ```

3. **Compile the app.** Expected fix classes (all mechanical, flagged with `// interop:`
   comments in-source):
   - exported Kotlin signature mismatches (label/order of parameters, `doCopy`, boxed types);
   - MediaPipe/TFLite/NMSSH pod API drift vs the versions pinned in the Podfile;
   - XcodeGen resource-path adjustments for the Android-asset references in `project.yml`.
   - App icon: `Dyrecto/Assets.xcassets/AppIcon.appiconset` uses the single-size 1024×1024
     format (Xcode 14+); verify it compiles and shows on the home screen. Regenerate assets
     with `python tools/icons/generate_icons.py` at the repo root if the artwork changes.

4. **Signing & entitlements**: set your team; `HotspotConfiguration` works with a standard
   paid account. `com.apple.developer.networking.multicast` requires a (free) entitlement
   request from Apple — until granted, REMOVE that key from `Dyrecto.entitlements`; SSDP
   falls back to unicast probes against the camera IP automatically (see SsdpDiscoverer).

5. **Deploy to iPhone** and validate against the FX3 in this order (mirrors the Android
   bring-up history):
   1. BLE scan → connect → provisioning reads (Developer screen shows the GATT log).
   2. One-time Pair/Register (EE01) if the camera was never registered to this phone.
   3. AP creation → Wi-Fi join → SSDP → CC17 (expect SSH OFF → direct mode).
   4. PTP bootstrap → Device Info + telemetry dashboard; leave idle 60 s (liveness must hold).
   5. Push live view: frames render; keepalive result stays 0 past the 60 s mark
      (the deliveryId regression test).
   6. Storyboard: import references (creative analysis card populates), start monitoring,
      verify drift instructions + voice.
   7. Background behavior: screen off during monitoring — BLE + audio background modes are
      declared, but iOS lifetime limits are THE known platform risk; measure how long
      telemetry/voice survive and iterate (this is hardware-only territory).

## Layout

- `project.yml` — XcodeGen manifest (framework search paths point at the Gradle-built
  `DyrectoShared`; a pre-build phase runs `embedAndSignAppleFrameworkForXcode`).
- `Podfile` — MediaPipeTasksVision, TensorFlowLiteSwift, NMSSH.
- `Dyrecto/Connection/` — full Swift port of the verified wire stack (see docs/wire-protocol.md).
- `Dyrecto/Engines/` — MediaPipe + MobileCLIP implementations of the shared engine seams.
- `Dyrecto/Vision|Reference|Session/` — orchestration ports (vision pipeline, reference
  monitor, monitoring session) driving the shared KMP core.
- `Dyrecto/Screens|Components|Theme/` — SwiftUI app (Dyrecto visual parity, native idioms).
- `docs/wire-protocol.md` — byte-level protocol spec (source of truth for the Swift port).
- `docs/swift-interop-conventions.md` — binding Swift↔KMP rules for all contributors.

Model/LUT assets are referenced straight from `android/app/src/main/assets/` (single source of
truth) via `project.yml` resource entries — nothing is duplicated in this directory.
