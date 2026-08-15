# Pre-fetchSshInfo BLE Sequence — Sony vs. Ours, and the First Divergence

All evidence from decompiled `jp.co.sony.ips.portalapp` (jadx). The SSH-bearing connect is the
**`connectViaAp` coroutine** (`CameraConnector$connectViaAp$1`), NOT the first-time pairing flow.

---

## 1. Sony's official sequence before `onFetchSshInfo` (connect-via-AP)

`CameraConnector$connectViaAp$1.invokeSuspend` runs exactly **4 suspend steps** (label switch + phase checks):

| # | Suspend fn (CameraConnector) | EnumConnectPhase | BLE state class | Char | R/W/N | Payload / expected | Gate |
|---|---|---|---|---|---|---|---|
| 0 | `isConnectedToSameApWithCamera$2` | `GET_NETWORK_SETTING` | (network-set check; `GettingCameraNetworkSetInfoState` read earlier in pairing) | `CCA1` | Read | parse support-type + string | — |
| 1 | `setSmartPhoneControlSetting$2` | `SET_SMARTPHONE_CONNECT_SETTING` | `SettingSmartPhoneControlSettingState` | enable notif **CC0E**, then write **CCA3** | W+N | write `CCA3 = 03 00 00 01`; wait **CC0E** result type=SmartPhoneControlSetting, success | **`isSupported(SmartPhoneConnectionMenu)` = CC0D bit 11** |
| 2 | `getUuidForWifiRemote$2` | `GET_UUID` | `TurningWifiOnState` → `GettingWifiInfoAfterWifiOnState` → join → SSDP | **CC08**, then **CC06/CC07/CC0C**, then CC09 notif | W+R+N | write `CC08 = 01` *only if* WiFi status≠Launched; read SSID/pass/BSSID; **join camera AP**; SSDP→UUID | — |
| 3 | `access$fetchSshInfo` | `FETCH_SSH_INFO` | `GettingSshInfoState` | **CC17** | Read | parse SshInfo TLV | — |
| → | (then) | `CONNECT_CAMERA` | `startMSearch` | — | — | PTP/IP | — |

Hard code evidence:

`connectViaAp$1` step order (label 2 → 3 → 4):
```java
// after isConnectedToSameApWithCamera (label1) succeeds:
obj = withContext(new CameraConnector$setSmartPhoneControlSetting$2(...));   // label 2
...
obj = withContext(new CameraConnector$getUuidForWifiRemote$2(...));          // label 3  -> returns UUID string
...
atomicReference3.set(EnumConnectPhase.FETCH_SSH_INFO);
this.label = 4;
CameraConnector.access$fetchSshInfo(this);                                   // label 4  -> reads CC17
```

`setSmartPhoneControlSetting$2` is **capability-gated** and is the *only* thing it does:
```java
if (ApBridgeHelper.isSupported(EnumSupportInfo.SmartPhoneConnectionMenu)) {     // CC0D bit 11
    ... onSettingSmartPhoneControlSetting(EnumSmartPhoneControlSettingTemp.ON, cb);  // CCA3 write + CC0E notify
} else {
    safeContinuation.resumeWith(new ApBridgeResult.Error(EnumResultCode.NOT_SUPPORT)); // -> connect ABORTS
}
```

`TurningWifiOnState` (the real CC08 writer) — conditional, inside `GetWifiInfo`:
```java
// ordinal == 1 (WiFi not launched) -> write CC08 = {1}; else moveToNextState() (no write)
findCameraControlCharacteristic(agent,"0000CC08");
agent.requireWriteCharacteristic(cmd, char, WIFI_ON_COMMAND_CHARACTERISTIC /* {1} */);
// on write success + WifiStatus==Launched -> GettingWifiInfoAfterWifiOnState (reads CC06/07/0C)
```

There is **no EE01 pairing write** anywhere in `connectViaAp`. EE01/`PAIRING_COMMAND` lives only in
`PairingState` (one-time registration). `CC11`=SettingFriendlyName, `CC12`=DateFormat (not registration).
`DEVICE_DELETE_SSH_INFO` appears only in `toppage/devicetab/menu/*` (manual "forget camera" UI), never in connect.

---

## 2. Our current sequence

| # | Op | Char | Matches Sony? |
|---|---|---|---|
| 1 | enable notifications | — | ok |
| 2 | read CC0A/CC0B/CC0D/CCA1/CCA2/CCA7 | device info | ok (Sony reads CC0A→CC0B→CCA7→CC0D, CCA2 uuid, CCA1 net) |
| 3 | **write EE01 = 06 08 01 00 00 00 00** | EE01 | ⚠ pairing — one-time registration, not part of SSH connect |
| 4 | enable CC0E notify | CC0E | ok |
| 5 | write CCA3 = 03 00 00 01 | CCA3 | ok (SmartPhoneControl ON) |
| 6 | wait CC0E = 07 00 0B 00 00 00 00 01 | CC0E | ok (SmartPhoneControlSetting success) |
| 7 | write CC08 = 01 | CC08 | ok (WiFi on) |
| 8 | wait CC09 state=02 | CC09 | ok |
| 9 | read CC06/CC07/CC0C | wifi info | ok |
| 10 | join WiFi | — | ok |
| 11 | read CC17 | CC17 | ⚠ see below |

---

## 3. First divergence

**Divergence #1 (root-cause class): we never check the CC0D capability bits the entire SSH path is gated on.**
Sony requires **CC0D bit 11 (SmartPhoneConnectionMenu)** for `setSmartPhoneControlSetting` to run, and
**CC0D bit 8 (SshInfo)** for the SSH path to be selected. If your FX3A's CC0D does not advertise bit 8,
the camera will **never** set CC17 to ON, and the genuine app would not use SSH at all. **This is the first
thing to verify and the most probable reason CC17 = OFF.**

**Divergence #2 (timing): we read CC17 right after the raw WiFi `join` (step 10).** Sony reads CC17 only
**after `getUuidForWifiRemote` succeeds** — i.e., after the phone has joined the camera AP *and* resolved the
camera UUID over SSDP, proving the smartphone-control remote session is fully live. The camera generates the
one-time SSH credential as part of bringing up that session; reading CC17 before it is established returns the
not-yet-generated `OFF` state.

**Divergence #3 (state pollution): we send the EE01 pairing command on every connect (step 3).** Pairing is a
**one-time registration** (`PairingState`), absent from `connectViaAp`. Re-issuing it each session can push the
camera back into a pre-registration state in which it does not emit SSH credentials.

Ranked: **#1 (capability) → #2 (timing/SSDP) → #3 (per-connect pairing).**

### Answers to your specific questions
- **EE01 payload correct?** `06 08 01 00 00 00 00` == `PAIRING_COMMAND_CHARACTERISTIC`. Correct bytes, but it's a
  registration command that should run **once**, not in the SSH connect path.
- **CC02 pairing-write before EE01?** No. Pairing writes `PAIRING_COMMAND` to **EE01** (svc `8000EE00`) and waits
  for an EE01 notification (`PairingState`). CC02 is not a precondition.
- **CC11/CC12 registration required?** No. CC11 = friendly name, CC12 = date format. Not SSH-related.
- **DEVICE_DELETE_SSH_INFO before/after?** No. UI-only ("forget camera"). Not in connect.
- **NetworkSetInfo CCA1 read/parsed before CCA3?** Yes — `GettingCameraNetworkSetInfoState` (phase
  `GET_NETWORK_SETTING`) runs before SmartPhoneControl. You read CCA1 but ensure you actually require its success.
- **CameraUUID CCA2 used in a write payload?** No. CCA2 is read for the device UUID/MAC (`GettingCameraUuidState`),
  used to match the SSDP/MSearch target — not embedded in any BLE write.
- **CCA7 token/hash used in a write payload?** No. CCA7 = identifier (64-byte ASCII, read-only). Not written back.
- **Is SmartphoneControlSetting more than CCA3=03 00 00 01?** No. It is exactly: enable CC0E notify → write
  `CCA3 = 03 00 00 01` → wait CC0E SmartPhoneControlSetting-success. (Gated on CC0D bit 11.)
- **CC08 before or after fetchSshInfo?** **Before.** WiFi-on (CC08) + WiFi-info + join + SSDP all precede CC17.
- **CC17 before AP creation / before join / after join?** **After** WiFi-on and **after** the phone joins the
  camera AP and SSDP resolves the UUID — i.e. CC17 is the last BLE read, but only once the session is live.

---

## 4. Exact code evidence (summary pointers)
- `CameraConnector$connectViaAp$1.java` — 4-step order, phases `GET_NETWORK_SETTING → SET_SMARTPHONE_CONNECT_SETTING → GET_UUID → FETCH_SSH_INFO`.
- `CameraConnector$setSmartPhoneControlSetting$2.java:69` — `isSupported(SmartPhoneConnectionMenu)` gate; writes CCA3 via `onSettingSmartPhoneControlSetting`.
- `SettingSmartPhoneControlSettingState.java:55-79` — CC0E notif enable + `CCA3 = SMARTPHONE_CONTROL_TEMPERATURE_ON {3,0,0,1}` + CC0E success.
- `TurningWifiOnState.java:50-104` — conditional `CC08 = {1}`, then `GettingWifiInfoAfterWifiOnState`.
- `GettingCameraDeviceInfoState.java:185-345` — CC0D capability bitmap; **bit 8 = SshInfo, bit 11 = SmartPhoneConnectionMenu**.
- `GettingSshInfoState.java:53` — `requireReadCameraCharacteristic("0000CC17")` (the read you already have correct).

---

## 5. Exact implementation patch

**Step A — Diagnose CC0D first (do this before anything else):**
Parse CC0D as `<1-byte header>` + repeating `[typeHi, typeLo, flag]` triples.
- Confirm `00 08 01` (SshInfo supported). If absent / flag≠1 → camera has no SSH; stop chasing CC17, connect PTP/IP directly to 192.168.122.1.
- Confirm `00 0B 01` (SmartPhoneConnectionMenu supported). If absent → SmartPhoneControl write is rejected by camera.

**Step B — Reorder to match Sony exactly:**
```
1. (registration, ONCE only — skip on reconnect): pair via EE01 = 06 08 01 00 00 00 00
2. read + verify CC0D bits 8 and 11
3. enable CC0E notify
4. write CCA3 = 03 00 00 01    -> wait CC0E SmartPhoneControlSetting success   [only if bit 11]
5. write CC08 = 01 (if WiFi not already Launched) -> wait CC09 state=Launched(02)
6. read CC06 / CC07 / CC0C
7. join camera Wi-Fi  -> RESOLVE camera UUID via SSDP (confirm session live)   <-- gate
8. ONLY NOW read CC17  -> expect byte[3] == 0x02 + 3 ASCII TLVs
```
Key changes vs. current:
- **Remove the EE01 pairing write from the per-connect path** (run it once at registration).
- **Gate the CC17 read on SSDP-UUID success** (step 7), not just on raw DHCP/join.
- **Add the CC0D bit-8 / bit-11 checks**; if bit 8 is unset, do not use SSH at all (direct PTP/IP).

If, after Steps A+B, CC0D shows bit 8 = 1 and CC17 still returns `03 00 00 01`, the camera's SSH is disabled in
firmware/menu for this body — confirm via the DID `mSshSupport` once on Wi-Fi; if it isn't `"Enable"`, the
official app would use direct PTP/IP, which is then your correct path.
