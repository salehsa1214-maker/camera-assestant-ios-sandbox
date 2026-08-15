# Dyrecto — Phase 1

Production foundation for a Sony FX3/FX3A companion app. Phase 1 goal: **discover →
connect → authenticate → establish PTP/IP session → retrieve camera information →
present it in a clean UI.** No recording controls, no camera commands, no telemetry
streaming yet (deliberately out of scope).

Built on the **verified connection stack** proven in `PROJECT_CHECKPOINT.md`
(BLE provisioning → CC17 SSH info → SSHJ keyboard-interactive auth → PTP/IP over
an SSH tunnel → GetDeviceInfo). That stack was carried over unchanged; only the
package was renamed and **additive** result instrumentation was added (the protocol
bytes/ordering are untouched).

## Architecture (clean, platform-agnostic core)

```
app/src/main/java/app/dyrecto/
├─ connection/   VERIFIED protocol stack (unchanged logic):
│                FxApp · BcFix · Fx3Uuids · Fx3BleManager · SshInfoTlv ·
│                SshTunnelTester · PtpIpClient · BleLog
├─ domain/       Platform-agnostic models + CameraRepository interface
├─ ble/ ssh/ ptp/ camera/ telemetry/   Per-concern domain value types
├─ data/         CameraRepositoryImpl — orchestrates the stack → StateFlow
├─ ui/           Jetpack Compose screens, ViewModel, navigation, theme
└─ debug/        Developer Mode: log store, secret-reveal flags
```

- No protocol code in UI classes; no business logic in the Activity.
- UI/ViewModels depend only on `domain.CameraRepository` (a `StateFlow`), so the
  business/camera/PTP logic is portable to a future iOS implementation. Android
  types are confined to `connection/` and `data/`.

## Screens

Dashboard · Device Discovery · Camera Connection · SSH Information (secrets hidden
by default) · SSH Session · PTP/IP Session · Device Information (searchable PTP
op-code list) · Camera Diagnostics (BLE Found → Connected → CC17 Read → SSH
Authenticated → PTP Init → OpenSession → GetDeviceInfo timeline with
timestamps/durations) · Developer (raw logs, export, secret reveal).

## Security

- Passwords/fingerprint are masked everywhere; revealing requires **Developer
  Mode → Reveal secrets**, an in-memory flag that is never persisted to disk.
- Raw packet logs live only behind the Developer Mode toggle.

## Build

Requires JDK 17 and the Android SDK (compileSdk 34, minSdk 26).

- **Android Studio (recommended):** open the `android/` folder and Run.
- **CLI:** `cd android && ./gradlew :app:assembleDebug`
  (a Gradle wrapper jar is not committed; Android Studio supplies Gradle 8.7,
  or run with a local Gradle 8.7 + JBR/JDK 17).

Verified: `:app:assembleDebug` produces `app/build/outputs/apk/debug/app-debug.apk`.

## Runtime prerequisites (on device)

1. Power on the FX3/FX3A and enable its Bluetooth.
2. Join the camera's Wi-Fi AP from the phone (the app binds sockets to that
   network for the SSH/PTP traffic).
3. Open the app → set Camera IP (default `192.168.122.1`) → **Connect**.
