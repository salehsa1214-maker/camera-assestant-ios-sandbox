# Sony Creators' App — Post-Wi-Fi Communication: Reverse-Engineering Report

**Target package:** `jp.co.sony.ips.portalapp` (Sony "Creators' App", successor to Imaging Edge Mobile)
**APKs analyzed:** `sony-base.apk` (dex), `sony-arm64.apk` (native libs), `sony-en`, `sony-xxhdpi`
**Method:** dex string extraction + native lib enumeration + on-device logcat (`ble.txt`, `sony.txt`, `full.txt`)
**Camera:** Sony FX3A class (smartphone/AP mode over BLE provisioning)

---

## 0. Executive answer (TL;DR)

After Wi-Fi association, the app does **NOT** speak PTP/IP directly to the camera, and does **NOT** use the legacy Sony Camera Remote HTTP/JSON-RPC API. The flow is:

```
BLE provisioning ──► reads SSH credentials from BLE (CC17 / "SshInfo")
        │
        ▼
Join camera Wi-Fi AP (192.168.122.x, gateway 192.168.122.1)
        │
        ▼
SSH connection to camera  (libssh2, native)   ◄── auth = username/password/fingerprint from BLE
        │
        ▼
SSH tunnel / forwarded channel ("Tunnel")
        │
        ▼
PTP/IP over the SSH channel  (InitCommandRequest → InitEventRequest → SDIO_Connect → SDIO_OpenSession)
        │
        ▼
Sony SDIO vendor operations: live view, device control, settings, playback
```

This **exactly matches the architecture in your CLAUDE.md** (`BLE → SSH tunnel → PTP/IP`). Your existing `connection/` stack is on the right track.

---

## A. Network Protocols

### Transport stack (evidence from dex class names)

| Layer | Implementation | Evidence (class/string) |
|---|---|---|
| SSH transport | **libssh2** (native C) | `lib/arm64-v8a/libssh2.so`, `lib/arm64-v8a/libssh_module.so` |
| SSH JNI bridge | Sony native wrapper | `com.sony.promobile.cbmexternal.ssh.SshClient`, `ISshJniListener` |
| SSH app layer | Kotlin wrappers | `jp.co.sony.ips.portalapp.ssh.SshClientWrapper`, `ssh.SshSupportClient`, `camera.SshSupportCamera` |
| Tunnel | SSH channel forward | strings `Tunnel`/`TUNNEL`, `connectChannel`, `connectSocket` |
| Camera protocol | **PTP/IP** (Sony SDIO vendor ext.) | `ptpip.PtpIpManager`, `ptpip.BasePtpManager`, `camera.PtpIpCamera`, `PtpClientWrapper` |
| Loopback | tunnel endpoint | string `127.0.0.1` |

### Key finding: NOT the protocols you may have expected
- ❌ **Sony Camera Remote API (HTTP/JSON-RPC on :8080 / `sony/camera`)** — *not present*. No `camera.api`, no `liveview.JPEG` HTTP stream, no `getEvent`/`startRecMode` JSON methods. That API belonged to older PlayMemories/CameraRemoteAPIbeta cameras. This camera uses PTP/IP instead.
- ❌ **Plain PTP/IP on TCP 15740** — the string `15740` was **not** found. PTP/IP is **not** exposed in the clear; it is wrapped inside the SSH channel.
- ❌ **No Java SSH library** (JSch / SSHJ / Apache MINA / Ganymed) — SSH is the native `libssh2`. That is why dependency scans for those names return nothing.
- ❌ No gRPC, no WebSocket for camera control. OkHttp/Retrofit/Cronet exist but are used only for **cloud** features (account, Creators' Cloud upload, `csx/bda` action-log analytics) — not camera control.

### Ports
- **SSH port:** not hard-coded to 22 in the camera-control path; it is delivered **from the camera over BLE** in the SSH-info blob. The parser exposes a `Port` / `sshPort` field (`BluetoothCameraSshInfoParser`, `SshOneTimeConnectionInfoObject.Port`). Treat the port as dynamic — read it from BLE, do not assume 22.
- **PTP/IP:** runs **inside** the SSH channel (loopback `127.0.0.1` forwarded endpoint), so there is no externally visible PTP port to target.

### Service discovery
- **None of the classic mechanisms.** No mDNS/Bonjour, no SSDP/UPnP for the control path. The camera's address is already known (`192.168.122.1`, the AP gateway) and the credentials/port arrive over BLE. Discovery is effectively "the gateway IP + BLE-supplied SSH info."

---

## B. Sony Camera Communication

### Camera discovery
Not network discovery — the camera is the Wi-Fi AP gateway. Identity/UUID is obtained over **BLE** before Wi-Fi (`GettingCameraUuidState`, `GettingCameraDeviceInfoState`, `GettingSerialState`).

### Session creation (PTP/IP handshake)
Standard PTP/IP init followed by Sony's SDIO vendor session, all over the SSH channel:
1. `InitCommandRequest` (sends client **GUID** + **friendlyName** + **ProtocolVersion**)
2. `InitCommandAck`
3. `InitEventRequest` / `InitEventAck` (second connection = event channel)
4. `SDIO_Connect` → `SDIO_ConnectState`
5. `SDIO_OpenSession` → `SDIO_OpenSessionState`
6. `SDIO_GetExtDeviceInfo` / `SDIO_GetVendorCodeVersion` (capability negotiation)
7. `SDIO_GetAllExtDevicePropInfo` (full property snapshot)

### Live view startup
- `SDIO_SetExtDevicePropValue` to enable live view, then repeated `GetLiveView` / `LiveViewImage` (`ptpip.mtp` getters: `ScnDataGetter`, `ThumbnailGetter`). Live view is a **pulled JPEG stream over PTP**, not an HTTP MJPEG stream.

### Remote control startup
- After `SDIO_OpenSession` + property sync, control flows through `SDIO_ControlDevice` and `SDIO_SetExtDevicePropValue`. Button objects live in `jp.co.sony.ips.portalapp.ptpip.button.*`: `S1Button` (half-press AF), `S2Button` (shutter), `AELButton`, `AFLButton`, `FELButton`, `AWBLButton`, `RemoteKeyUp`, `NearFarPlus/Minus`, `FocusStepFar`, `RequestOneShooting`, `WiFiPowerOff`.

---

## C. BLE Relationship

**BLE is used for BOTH provisioning AND the SSH-credential handoff, and stays connected during the session.**

- BLE-side state machine (pre-Wi-Fi) includes `GettingSshInfoState` → this is where the app **fetches SSH info from the camera over BLE** (your CC17). Related: `FETCH_SSH_INFO`, `GetSshInfo`, `DEVICE_DELETE_SSH_INFO`, `EnumBluetoothSshInfoError`, `BluetoothCameraSshInfo` / `BluetoothCameraSshInfoParser`.
- The GATT services seen in `ble.txt` confirm Sony's vendor GATT (`8000cc00-cc00-…` = the "CC" service family that holds CCxx characteristics, plus `dd00`, `ee00`, `ff00`, `bb00`).
- `sony.txt` logcat shows the app re-registering GATT and holding a foreground `BluetoothNotificationChannel` service **while** Wi-Fi/DNS activity happens → **BLE link is maintained after Wi-Fi connects** (used for keep-alive, power-off `WiFiPowerOff`, and re-provisioning).

**Conclusion:** BLE is not "provisioning only." It is the credential channel for SSH and remains active as a side-channel during the Wi-Fi/PTP session.

---

## D. SSH Investigation

| Question | Answer | Evidence |
|---|---|---|
| Does the app read CC17 / SSH info? | **Yes** | `GettingSshInfoState`, `BluetoothCameraSshInfoParser`, `FETCH_SSH_INFO` |
| Uses SSH? | **Yes** | `libssh2.so`, `libssh_module.so`, `SshClient` (JNI), `SshClientWrapper` |
| Uses SFTP/SCP? | **No** (control path) | no sftp/scp control classes; transfer uses PTP `GetObject` |
| SSH library | **libssh2 (native C)**, not Java | `lib/arm64-v8a/libssh2.so` |
| JSch / Apache MINA / SSHJ | **Not used** | absent from dex |

**SSH credential fields parsed from BLE** (from `SshOneTimeConnectionInfoObject` Realm schema): `Username`, `Password`, `Port`, `Fingerprint`, `PublicKey`, `PrivateKey`. Stored as **one-time** connection info (`OneTimeConnectionInfoObject`, `OneTimeConnectState`) in a Realm DB and intended to be ephemeral — matches your "never persist secrets" model. Auth is username/password (+ host-key/fingerprint verification), optionally key-based.

---

## E. Camera API Mapping

All control = PTP/IP (Sony SDIO vendor extension) over the SSH channel. Concrete PTP operations / property writes:

| Action | Mechanism | PTP operation | Transport |
|---|---|---|---|
| Start Live View | enable prop, then poll | `SDIO_SetExtDevicePropValue` + `GetLiveView`/`LiveViewImage` | PTP/IP over SSH |
| Stop Live View | disable prop | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| Half Press (AF) | `S1Button` | `SDIO_ControlDevice` (S1 control code) | PTP/IP over SSH |
| Full Press (shutter) | `S2Button` / `RequestOneShooting` | `SDIO_ControlDevice` (S2 control code) | PTP/IP over SSH |
| Focus Point | focus-area prop | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| ISO | device prop | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| Shutter Speed | device prop | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| Aperture | device prop | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| White Balance | device prop (+ `AWBLButton`) | `SDIO_SetExtDevicePropValue` | PTP/IP over SSH |
| Start/Stop Recording | movie button | `SDIO_ControlDevice` | PTP/IP over SSH |
| Read all settings | snapshot | `SDIO_GetAllExtDevicePropInfo` | PTP/IP over SSH |
| Pull images/thumbnails | object transfer | `GetObjectHandles`/`GetObjectInfo`/`GetObject`/`GetPartialObject`, `SDIO_GetContentsData` | PTP/IP over SSH |

> Exact 16-bit control codes for S1/S2/etc. are computed inside the `button.*` classes and `SDIO_ControlDevice` payloads; recover them by dumping the `ptpip.button` constants (they are standard Sony SDIO control codes, e.g. S1/S2 toggles 0x0001/0x0002-style "down/up" values).

---

## F. MOST IMPORTANT — First network request after Wi-Fi

1. **Exact sequence:**
   Wi-Fi associated → DHCP → app opens **SSH** to the camera gateway using BLE-supplied credentials → SSH auth → open a forwarded **channel/tunnel** → over that channel send the **PTP/IP `InitCommandRequest`**.

2. **Exact protocol:** SSH (libssh2) first; **the first camera application request is PTP/IP `InitCommandRequest`** sent through the SSH channel.

3. **Exact endpoint:** TCP to the camera gateway **`192.168.122.1`** on the **SSH port supplied over BLE** (`SshInfo.Port` — read it, don't hard-code 22). PTP/IP then targets the tunnel's loopback endpoint (`127.0.0.1` forwarded to the camera's internal PTP service).

4. **Exact port:** SSH = dynamic (from BLE SSH-info `Port` field). PTP/IP = inside the SSH channel (no separate external port).

5. **Exact payload (PTP/IP `InitCommandRequest`, little-endian):**
   ```
   uint32  Length            (total packet length)
   uint32  PacketType    = 0x00000001   (Init_Command_Request)
   byte[16] GUID                          (client GUID)
   wchar[]  FriendlyName     (UTF-16LE, NUL-terminated; the phone/app name)
   uint32  ProtocolVersion  = 0x00010000
   ```
   The camera replies `Init_Command_Ack` (PacketType `0x00000002`) carrying the **ConnectionNumber** and the camera's GUID/friendly name. The app then opens a second SSH-tunneled socket and sends `Init_Event_Request` (`0x00000003`) with that ConnectionNumber, then proceeds to `SDIO_Connect` → `SDIO_OpenSession`.

---

## Practical guidance for your implementation

1. **Don't look for an HTTP API or port 15740.** Implement an SSH client (you can use libssh2 or a JVM lib like SSHJ since you're not bound to Sony's native lib) and tunnel PTP/IP through it.
2. **Read SSH `Port`, `Username`, `Password`, `Fingerprint` from BLE** (CC17 / SshInfo) — confirmed fields. Verify the host-key fingerprint that BLE gives you.
3. **Open a direct-tcpip / local-forward channel** to the camera's internal PTP/IP service, then run a normal PTP/IP `InitCommandRequest` handshake.
4. **After OpenSession**, drive everything via `SDIO_GetAllExtDevicePropInfo`, `SDIO_SetExtDevicePropValue`, and `SDIO_ControlDevice`; live view = `GetLiveView` polling loop.
5. **Keep BLE connected** during the session.

### Artifacts produced
- Decompiled string dumps: `platform-tools/extracted/classes{,2,3}.dex.str` (searchable).
- Native libs of interest: `libssh2.so`, `libssh_module.so` (SSH), `libmonitor_protocol*.so` (Monitor & Control / PTP), `librealm-jni.so` (one-time SSH-info store).
