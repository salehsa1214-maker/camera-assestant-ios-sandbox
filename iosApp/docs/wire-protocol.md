# Sony FX3 Wire-Protocol Spec (Swift port source of truth)

Extracted 2026-07-08 from the verified Android stack (`app.dyrecto.connection.*`,
`PushLiveViewSession`, `CameraRepositoryImpl`) + RE memory notes. All multi-byte integers
**little-endian unless stated**. The Swift implementation in `Dyrecto/Connection/` mirrors this
file; if hardware testing reveals a divergence, fix the Swift side to match Android behavior.

Two modes share everything above transport:
- **SSH mode** (CC17 SSH=ON): PTP/IP over SSH `direct-tcpip` to `localhost:15740`; SSH client
  disconnects right after PTP bootstrap → no long-lived telemetry/live view in this mode.
- **Direct mode** (CC17 SSH=OFF): plain TCP to `cameraIp:15740`; sustains the event listener,
  liveness, Live View and Push LV.

## 1. BLE (Fx3BleManager)

Services: provisioning `8000cc00-cc00-ffff-ffff-ffffffffffff`, pairing `8000ee00-ee00-ffff-ffff-ffffffffffff`.
CCCD `00002902-0000-1000-8000-00805f9b34fb`. Characteristics = 16-bit shorts expanded
`0000XXXX-0000-1000-8000-00805f9b34fb`:

CC03 notify | CC06 SSID read | CC07 password read | CC08 AP ON write `01` | CC09 wifi-status notify |
CC0A firmware read | CC0B model read | CC0C BSSID read | CC0D capability read | CC0E control-result notify |
CC0F/CC10/CC16/CC1B notify-only | CC17 SSH TLV read (bond-gated) | CCA1 camera SSID notify+read |
CCA2 camera UUID read | CCA3 smartphone-control write `03 00 00 01` | CCA5/CCA7/CCA9 info |
EE01 pairing write `06 08 01 00 00 00 00` + notify | EE02/EE04 registration reads | EE03 pairing notify.

Scan: no HW filter; name contains (case-insensitive) any of `FX3, ILME-FX3, DSC, ILCE, Sony`;
auto-stop 30 s. Connect: LE transport → MTU 247 → ensure bond (create if needed; 600 ms settle
after BONDED) → discover services. ALL GATT ops in a single serialized queue.

Provisioning order: subscribe EE03, read EE02+EE04 → enable notify CC03, CC09, CC0F, CC10, CC16,
CC1B, CCA1, CCA5, CCA9 (skip missing) → confirmed reads CC0A, CC0B, CC0D, CCA1, CCA2, CCA7 →
enable notify CC0E → write CCA3 `03 00 00 01` (phase SMARTPHONE_MODE) → wait CC0E ack ≤30 s
(proceed on timeout) → write CC08 `01` (phase AP_CREATING) → wait CC09 "launched" ≤30 s →
read CC06, CC07, CC0C → credentials ready.

CC0E parse: ≥8 bytes; `type=(v[1]<<8)|v[2]` must be `0x000B` else keep waiting; success `v[3]==1`;
failure reason = big-endian u32 v[4..7] (1=Status, 2=Parameter); failure logged, sequence continues.
CC09 parse: state byte v[3]: `01`=creating (wait), `02`=ready (proceed).

CC17 is read ONLY after Wi-Fi join + SSDP resolution (the camera mints the one-time SSH credential
only once an SSDP remote session exists). Decode via shared `SshInfoTlv`. `03 00 00 01` = SSH OFF.

One-time pairing (explicit user action, NEVER per-connect): subscribe EE01+EE03 → write EE01
`06 08 01 00 00 00 00` → read EE04; 10 s EE03 observation window. Re-sending EE01 per connect
resets registration and the camera stops minting SSH creds.

## 2. Wi-Fi join + discovery

Join camera AP (SSID+WPA2 pass, BSSID optional). iOS: `NEHotspotConfiguration(ssid:passphrase:isWEP:false)`,
`joinOnce=false`. Camera IP = gateway of the joined interface; default fallback `192.168.122.1`.
All camera sockets must route over the Wi-Fi interface (Network.framework
`requiredInterfaceType = .wifi`; for listeners bind to the Wi-Fi interface's IPv4).

SSDP (REQUIRED before CC17 read): multicast `239.255.255.250:1900`, socket timeout 1000 ms,
overall 8000 ms. Cycle STs: `urn:schemas-sony-com:service:ScalarWebAPI:1`,
`urn:schemas-upnp-org:device:DigitalImagingDevice:1`, `ssdp:all`. Send each probe TWICE:
multicast AND unicast to `cameraIp:1900`. Request (CRLF):
```
M-SEARCH * HTTP/1.1
HOST: 239.255.255.250:1900
MAN: "ssdp:discover"
MX: 1
ST: <target>
```
Success = any response containing regex `uuid:([0-9a-fA-F-]{8,})`. Device XML not fetched.
iOS note: multicast needs the `com.apple.developer.networking.multicast` entitlement; the
unicast probe to `cameraIp:1900` is the fallback when it's absent.

## 3. SSH (SshTunnel)

Port 22, connect/read timeout 10 s, promiscuous host-key policy, user=`sshId`,
**keyboard-interactive ONLY** — answer every prompt with `sshPass` verbatim (no password-auth
fallback). Used for two `direct-tcpip` channels to `localhost:15740` (command+event), then the
SSH client disconnects after PTP bootstrap (SSH mode has no steady-state features).

## 4. PTP/IP (PtpIpClient)

Framing: `u32 totalLen | u32 type | body`. Types: 1 INIT_CMD_REQ, 2 INIT_CMD_ACK, 3 INIT_EVT_REQ,
4 INIT_EVT_ACK, 5 INIT_FAIL, 6 OP_REQUEST, 7 OP_RESPONSE, 8 EVENT, 9 START_DATA, 10 DATA,
11 CANCEL, 12 END_DATA.

InitCommandRequest: 16-byte random GUID + UTF-16LE `"CameraAssistant"` + `0x0000` + u32 `0x00010000`.
InitCommandAck body: u32 connectionNumber | 16B responder GUID | UTF-16LE name (NUL-terminated).
InitEventRequest: 12 bytes `len|3|connectionNumber` → InitEventAck.
OperationRequest: `u32 dataPhase (1=in/none, 2=out) | u16 opcode | u32 tid | u32 params...`.
OperationResponse body: `u16 rc | u32 tid | u32 params...` (nParams=(len-6)/4).
StartData 20B `len|9|tid|u64 totalLen`; Data/EndData body `u32 tid | payload`.
Event body: `u16 code` at 0, params from offset 6.
DataOut op = OP_REQUEST(phase 2) + StartData + EndData(payload), flush.

Bootstrap: cmd channel init → event channel init → OpenSession `0x1002` tid 0 params `[1]` →
GetDeviceInfo `0x1001` tid 2 → SDIO bootstrap (in order, tids increment):
`0x9201 [1,0,0]`, `0x9201 [2,0,0]`, `0x9202 [300,1]`, `0x9201 [3,0,0]`, `0x9209 [0,1]` (bulk
telemetry snapshot). Non-OK handshake rc = log-and-continue; only socket death aborts.

Response consumption: read until OP_RESPONSE accumulating data phases; EVENT packets on the cmd
channel are logged+skipped; stale OP_RESPONSE with `tid ∈ [1, requested)` is dropped (resync guard).

GetDeviceInfo dataset (LE): u16 stdVersion | u32 vendorExtId | u16 vendorExtVer | str vendorDesc |
u16 functionalMode | u16[] ops | u16[] events | u16[] props | u16[] captureFormats |
u16[] imageFormats | str manufacturer | str model | str deviceVersion | str serial.
(`u16[]` = u32 count + count×u16; `str` = u8 charCount + UTF-16LE chars, trim trailing spaces.)

0x9209 dataset: `u64 recordCount` then per record:
`u16 propCode | u16 dataType | u8 getSet | u8 availability | value(default) | value(current) |
u8 formFlag | form`. dataTypes: 1/2 i8/u8, 3/4 i16/u16, 5/6 i32/u32, 7/8 i64/u64, 0xFFFF STR
(u8 charCount + UTF-16LE). formFlag 0 none; 1 range (3 values); 2 enum (u16 count ×values, then
u16 listedCount ×values). Parse resiliently; stop on malformed record keeping prior records.

Key props: 0xD278 LiveViewUrl (STR), 0xD221 LiveViewStatus, 0xE09D MonitoringBinaryVersion,
0xD21D MovieRecordingState, 0xD261 RecordingTime, 0xD20E/0xD218/0xD038 battery, 0xD248/0xD24A/
0xD256/0xD258 media slots, 0xD251 overheating, plus ISO/shutter/aperture/WB/focus codes.

Timeouts: bootstrap read deadline 10 s; steady-state 3 s; socket poll 2 s. Event channel reads
block indefinitely. Packet length sanity: reject <8 or >8_000_000.

Steady state (direct mode only): event listener owns the sockets; on EVENT → (150 ms debounce) →
`0x9209 [1,1]` refresh on cmd channel → reparse → publish. Liveness daemon: every **5 s** send
`0x9209 [1,1]` (camera closes idle PTP after ~10 s). All cmd-channel I/O + tid allocation behind
ONE lock. Unexpected read-loop exception → restart loop up to 3×; EOF → session over (no
transport reconnect; recovery = fresh connect flow).

RC constants: 0x2001 OK, 0x2002 GeneralError, 0x2003 SessionNotOpen, 0x2005 OpNotSupported,
0x201A DeviceBusy, 0x201F SessionAlreadyOpen.

## 5. Push Live View (0x9230, direct mode only)

Flow: bind two TCP listeners on (phoneWifiIp, port 0) → videoPort/metaPort (audio 0), accept
timeout 8 s → send Start → capture `deliveryId = responseParam[0]` → KeepAlive every **15 s** →
camera dials back from cameraIp to videoPort pushing ~816 KB/s VERIC stream → Stop(2) + close.

0x9230 op: ONE op param = subcommand (1 Start / 2 Stop / 3 KeepAlive), DataOut payload:
wrapper `u16 protocolVersion=101 | u16 0 | u32 count` + records.

Start record (count=1): `u16 ipLen(ascii+NUL) | ip ASCII | 0x00 | u32 videoPort | u32 audioPort=0 |
u32 metaPort | u16 deliveryType=1(JPEG) | u8 quality=3 | u16 protocolType=2(TCP)`.

KeepAlive/Stop payload (12B): `u16 101 | u16 0 | u32 1 | u32 deliveryId`.

CRITICAL: response has TWO params — `param[0]=deliveryId`, `param[1]=result`; rc 0x2001 only
means "received". result: 0 OK, 1 SystemError, 2 LimitOver, 3 Excluded, 4 OtherTypeProcessing,
5 MonitoringStopped, 6 Invalid_Args, 7 HighTemperature, 8 Streaming. Omitting the deliveryId
payload ⇒ result 6 and the delivery's ~60 s idle timer kills the socket.

Video stream is VERIC-framed (ASCII `VERIC`, JPEG SOI at ~0x50): feed chunks into the shared
`VericParser`.

## 6. Live View HTTP pull (LiveViewClient, alternative path)

URL from prop 0xD278; direct mode: replace host with cameraIp (keep port/path). Gate: prop
0xD221 raw 0 or 2 ⇒ blocked; 1/absent ⇒ proceed. Request:
`GET <path> HTTP/1.1\r\nHost: <host>\r\nConnection: keep-alive\r\n\r\n`. Require 200; support
chunked. Frames: 16-byte LE header `u32 offsetToImage | u32 imageSize | u32 offsetToMeta |
u32 metaSize`, then padding to firstOffset, JPEG, metadata. Sanity: offsetToImage≥16,
0<imageSize≤8MB. firstOffset = offsetToMeta if in 1..<offsetToImage else offsetToImage.

## 7. Phase machine

IDLE → SCANNING → BLE_FOUND → CONNECTING → CONNECTED → BONDING → READING_INFO →
SMARTPHONE_MODE → AP_CREATING → WIFI_CREDENTIALS → WIFI_JOINING → IP_DISCOVERY → CC17_READ →
(SSH_CONNECTING → SSH_AUTHENTICATED →) PTP_INIT → SESSION_OPEN → DEVICE_INFO; ERROR from anywhere.
Direct mode skips the SSH steps. After IP_DISCOVERY: SSDP resolve → 30 s observation window +
CC17 re-read. SSDP-resolved-but-SSH-not-ON = incomplete session error.

Constants: DEFAULT_CAMERA_IP 192.168.122.1 · SSH 22 · PTP 15740 · MTU 247 · bond settle 600 ms ·
BLE sentinels 30 s · pairing window 10 s · SSDP 8 s · liveness 5 s · push keepalive 15 s ·
event debounce 150 ms · push protocolVersion 101.
