# CC17 SshInfo Retrieval — Root Cause & Fix (from Creators' App decompile)

Decompiled `jp.co.sony.ips.portalapp` (Creators' App). All findings below are from real
bytecode in `btconnection/internal/state/*` and `camera/CameraConnector*`.

---

## 0. THE HEADLINE: `03 00 00 01` is NOT an error — it is a valid TLV meaning **SSH = OFF**

CC17 is parsed in `GettingSshInfoState.onGattCharacteristicRead()`. The exact layout:

```
byte[0] = total length of the RS payload that follows the length byte
byte[1..2] = data type, big-endian  ((b[1]<<8)+b[2]) — MUST be 0x0000
byte[3]    = RS0 = SSH On/Off status:  0x00=Unknown, 0x01=OFF, 0x02=ON
  -- if RS0 == 0x02 (ON), the following TLVs are present: --
byte[4]    = len(RS1)  then RS1 = SSH username (ASCII)
byte[..]   = len(RS2)  then RS2 = SSH password (ASCII)
byte[..]   = len(RS3)  then RS3 = SSH fingerprint (ASCII)
```

Decode of your `03 00 00 01`:
- `0x03` length = 3 bytes follow
- `0x00 0x00` data type = 0 ✅ (valid, well-formed)
- `0x01` RS0 = **OFF**

So the camera is replying **correctly** and saying **"SSH is currently OFF, I have no credentials for you."**
The parser then builds an **empty** `BluetoothCameraSshInfo(OFF,"","","")`. There is no corruption,
no encryption, no split, no compression. Your BLE read is working perfectly. The problem is **camera-side state**, not your read.

**Important consequence:** CC17 is a **read-only** characteristic. There is **NO BLE write anywhere in the
app that turns SSH on**. `GetSshInfo` (EnumBluetoothCommand #13) is purely `requireReadCameraCharacteristic("0000CC17")`.
SSH On/Off is internal camera state — you cannot flip it with a magic write. You make the camera turn it ON by
doing the *correct preceding sequence* (and the camera must actually support SSH).

---

## 1. Does your camera even support SSH? (READ CC0D FIRST — this is the real gate)

Before any SSH logic, the app reads a **capability bitmap from characteristic `0000CC0D`**
(`GettingCameraDeviceInfoState`, case 2). Format after a 1-byte header: repeating **3-byte records**
`[typeHi, typeLo, supportedFlag]`. Feature **type 0x0008 = SshInfo**; supported when its flag byte == `0x01`.

```
CC0D = <hdr> , (00 00 <flag>)=MediaInfo, (00 01 <flag>)=BatteryInfo, ... , (00 08 <flag>)=SshInfo, ...
```

Decision logic (`CameraConnector$cameraManagerListener$1.cameraFound`, `SingleCameraManager.addCamera`):

```
useSSH =  DID.mSshSupport == ENABLE           // from camera DeviceDescription XML (read over Wi-Fi)
       AND cameraInfoStore.isSupported(SshInfo) // CC0D feature 8 flag == 1
       AND CC17.sshOnOff == ON                  // RS0 == 0x02
If any is false  ->  app builds a PLAIN PtpIpCamera and talks PTP/IP DIRECTLY (no SSH tunnel).
```

**This means: if your FX3A does not advertise SSH (CC0D feature 8 != 1, or DID mSshSupport != "Enable"),
then CC17 will be OFF forever and the genuine app does NOT use SSH at all — it connects PTP/IP directly to
192.168.122.1. In that case your entire SSH blocker disappears: skip SSH and open PTP/IP directly.**

So **step 1 is diagnostic, not code:** read CC0D, find the `00 08 xx` triple, check `xx`.
- `xx == 01` → camera supports SSH; OFF means you're reading it at the wrong time / wrong preconditions (Section 2).
- `xx == 00` or triple absent → camera does **not** use SSH → go direct PTP/IP, stop chasing CC17.

---

## 2. The exact BLE sequence the app runs before CC17 returns ON

Phase order proven from `CameraConnector.cancel()` `EnumConnectPhase` switch (cases 1→7) and the
state classes. For a **Connect-via-AP** session:

```
1. GettingCameraNetworkSetInfo   READ  CC A1            (network-set capability)
2. SettingSmartPhoneControlSetting WRITE CCA3 = {3,0,0,1}  (notify result on CC0E)   <-- enables smartphone remote-control session
3. GettingCameraUUid             READ  (uuid char)
4. fetchWifiInfo                 READ  CC06 SSID / CC07 pass / CC0C BSSID   (you already do this)
5. fetchSshInfo                  READ  CC17            <-- THIS read; only ON after steps 1-3 done
6. (Wi-Fi join to the AP)
7. MSearch / SSDP on Wi-Fi  ->  fetch DID XML  ->  open PTP/IP (SSH-tunneled if useSSH)
```

Key facts about step 2 (`SettingSmartPhoneControlSettingState`):
- Enable notifications on **CC0E** (write CCCD descriptor = `01 00`).
- Write **CCA3** with `SMARTPHONE_CONTROL_TEMPERATURE_ON_COMMAND_CHARACTERISTIC = {0x03,0x00,0x00,0x01}`
  (the `_OFF_` variant is `{0x03,0x00,0x00,0x00}`).
- Wait for a **CC0E notification** parsed as `EnumInitialSettingResultDataType.SmartPhoneControlSetting` + success
  before proceeding. Do **not** read CC17 until this success arrives.

**Most likely cause of your OFF:** you are reading CC17 immediately after CC09=02 / AP up, but **before**
completing the `SettingSmartPhoneControlSetting` write on **CCA3** (+ its CC0E success notification) and the
device-info/UUID reads. The camera only generates one-time SSH credentials once the smartphone-control session
is properly established.

---

## 3. EXACT FIX — ordered checklist

1. **Confirm capability (one-time):** read **CC0D**, locate triple `00 08 <flag>`. If `<flag> != 01`, the camera
   has no SSH — connect PTP/IP directly to `192.168.122.1` and stop here.
2. Ensure notifications are enabled (write `01 00` to the CCCD descriptor) on **CC0E** *before* the control write.
3. **Write CCA3 = `03 00 00 01`** (SmartPhoneControl ON). Wait for the **CC0E** notification indicating
   `SmartPhoneControlSetting` success.
4. Perform the device-info / UUID reads if your camera lists them (CC0A fw, CC0B model, CCA7 identifier, CC0D) and
   the AP-info reads (CC06/CC07/CC0C) you already do.
5. **Only now read CC17.** Expect `... byte[3] == 0x02` (ON) followed by 3 length-prefixed ASCII fields:
   username, password, fingerprint.
6. Join Wi-Fi, then SSH using those credentials (port comes from the camera's DID/`SshOneTimeConnectionInfoObject`,
   not hard-coded 22), then PTP/IP over the tunnel.

These credentials are **one-time** (`SshOneTimeConnectionInfoObject`): the camera may regenerate/rotate them per
session, so re-read CC17 each connection; never cache.

---

## 4. Fallback diagnostics if CC17 still returns `03 00 00 01` (OFF)

Run these in order:

1. **Re-read CC0D and verify feature 8 flag.** If not `01` → camera genuinely doesn't do SSH. Switch to direct PTP/IP.
   This is the single most common explanation for a permanent OFF.
2. **Verify the CCA3 write actually succeeded** — you must receive the **CC0E success notification**
   (`EnumInitialSettingResultDataType.SmartPhoneControlSetting`). If you never enabled CC0E notifications, the
   camera may still flip SSH but you'll race the read; gate the CC17 read on that notification.
3. **Check ordering:** CC17 must be read *after* steps 1–3 of Section 2, not right after CC09=02.
4. **Check the camera menu / firmware:** SSH-mode is firmware/region gated on some bodies. If CC0D says supported
   but CC17 stays OFF after the full sequence, the camera's "remote control" / "SSH" may be disabled in its menu,
   or the firmware predates SSH support → use direct PTP/IP.
5. **DID cross-check (after Wi-Fi):** once on Wi-Fi, fetch the DeviceDescription XML and inspect `mSshSupport`.
   If it is not `"Enable"`, the app would ignore SSH regardless of CC17 → direct PTP/IP is the intended path.
6. **Confirm you read the right handle:** CC17 = 16-bit `0x CC17` inside service `8000CC00-CC00-FFFF-FFFF-FFFFFFFFFFFF`.
   A `03 00 00 01` of length 4 with type `0000` is definitely the SSH-info characteristic responding, so this is
   almost certainly correct — but verify you're not reading a neighbor handle.

---

## 5. One-line summary

`03 00 00 01` = **"SSH OFF"**, a correct well-formed reply — not an error and not a partial TLV. CC17 is read-only;
no write turns SSH on. The camera emits ON (`...02` + 3 ASCII TLVs) only after the **CCA3 SmartPhoneControl-ON
write + CC0E success**, and only if **CC0D feature 8 = 1** and **DID mSshSupport = Enable**. If those capability
flags are not set, this camera/firmware does not use SSH and the app talks PTP/IP directly to 192.168.122.1 —
in which case the CC17 blocker is moot.
