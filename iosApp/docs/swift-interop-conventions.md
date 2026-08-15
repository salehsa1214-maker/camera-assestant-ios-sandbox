# Dyrecto iOS — Swift ↔ KMP interop conventions (binding for all Swift code)

The shared Kotlin framework is `DyrectoShared` (import DyrectoShared). Kotlin classes export
WITHOUT package prefixes — check for name collisions before declaring Swift types (e.g. Swift
wire-parsing types are named `SonyProp`/`PtpDeviceInfo` because `TelemetryProp` is taken by the
shared domain class).

Interop rules:
1. **Kotlin default args vanish** in ObjC export — call sites must pass every parameter
   (e.g. `VericParser(onFrame:verbose:clock:logLine:)`).
2. **Kotlin suspend funs** export as `...(arg, completionHandler:)`. Swift classes CONFORM to
   shared interfaces (ObjC protocols) by implementing the completion-handler method
   (see IosObjectDetectorEngine).
3. **Primitives**: Kotlin `Int` → `Int32`, `Long` → `Int64`/`KotlinLong` (boxed in generics /
   nullable positions), `Boolean?` → `KotlinBoolean?`. Kotlin data-class `copy` exports as
   `doCopy(...)` with ALL parameters.
4. **Enums**: Kotlin enum entries export as lowercase static members (`ConnectionPhase.scanning`).
   `entries`/`valueOf` don't export — use `SwiftEnums` (shared iosMain bridge) to enumerate/parse.
5. **Collections**: Kotlin `List<T>` → `[T]` bridged; `Map<Int, T>` → `[KotlinInt: T]`.
   `IntArray/ByteArray/FloatArray` → `KotlinIntArray/KotlinByteArray/KotlinFloatArray` with
   `get(index:)`/`set(index:value:)` — NEVER loop these per-pixel from Swift (ObjC bridge per
   call). Per-pixel work goes into shared iosMain Kotlin (see IosExposureModule, RgbaFramePixels).
6. **Flows**: observe via the shared `FlowWatcher` (iosMain) — `watch { }` delivers on main;
   `close()` cancels. Create MutableStateFlows via `StateFlowFactoryKt`.
7. **Kotlin objects** → `.shared` singleton accessor (`SshInfoTlv.shared`, `VoiceCooldowns.shared`,
   `SwiftEnums.shared`, `PtpOpcodes.shared`, `AnalysisRegionRegistry.shared`).
8. **companion** members → `.companion` (`CameraTelemetry.companion.labelFor(code:)`,
   `AlertConfig.companion.default(type:)`, `ConceptVocabulary.companion.fromJson(text:)`).
9. **PlatformImage (iOS actual)**: `PlatformImage(handle: uiImage)`; engines unwrap
   `handle as? UIImage`. The producing side wraps a UIImage.
10. **Threading**: shared pure classes are NOT thread-safe unless documented — mirror the
    Android ownership (single vision worker drives per-frame classes; ReferenceMonitor state is
    session-scope confined).
11. **Logging**: use `logInfo/logError` (DyrectoLog) — the Developer screen streams it.
12. **Data → KotlinByteArray**: `KotlinByteArray.from(data:)` (Fx3BleManager.swift extension);
    small payloads only. Large buffers go through NSData into iosMain Kotlin.

House style: match the Android file's structure and KDoc comments when porting a class 1:1;
keep constants verbatim; never invent behavior not present on the Android side.
