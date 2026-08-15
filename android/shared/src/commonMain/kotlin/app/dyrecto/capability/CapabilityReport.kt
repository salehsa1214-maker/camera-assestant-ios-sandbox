package app.dyrecto.capability

import app.dyrecto.text.hexUpper
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A serializable, camera-agnostic snapshot of everything discovered during a session — the export
 * artifact that makes onboarding a new camera model fast (no raw protocol logs needed). Contains
 * only normalized models, no Sony/PTP types.
 */
@Serializable
data class CapabilityReport(
    val schemaVersion: Int,
    val generatedAtMs: Long,
    val info: CameraInfo,
    val properties: List<CameraProperty>,
    val features: List<String>,
    /** Distinct event kinds observed during the session (display strings). */
    val observedEvents: List<String> = emptyList(),
    /** Codes reported but not recognized by [PropertyCatalog]. */
    val unknownPropertyCodes: List<Int> = emptyList(),
    /** Unknown event descriptions observed during the session. */
    val unknownEvents: List<String> = emptyList(),
)

/** Builds and serializes [CapabilityReport]s. Pure; the caller supplies the timestamp. */
object CapabilityReporter {

    private val json = Json { prettyPrint = true; encodeDefaults = true }

    fun build(
        caps: CameraCapabilities,
        generatedAtMs: Long,
        observedEvents: List<String> = emptyList(),
        unknownEvents: List<String> = emptyList(),
    ): CapabilityReport = CapabilityReport(
        schemaVersion = caps.schemaVersion,
        generatedAtMs = generatedAtMs,
        info = caps.info,
        properties = caps.properties,
        features = caps.features.map { it.name }.sorted(),
        observedEvents = observedEvents,
        unknownPropertyCodes = caps.unknownPropertyCodes,
        unknownEvents = unknownEvents,
    )

    fun toJson(report: CapabilityReport): String = json.encodeToString(report)

    /** Human-readable summary for quick review / pasting into an issue. */
    fun toMarkdown(report: CapabilityReport): String = buildString {
        appendLine("# Camera Capability Report")
        appendLine()
        appendLine("- Schema version: ${report.schemaVersion}")
        appendLine("- Generated at (epoch ms): ${report.generatedAtMs}")
        appendLine("- Manufacturer: ${report.info.manufacturer ?: "—"}")
        appendLine("- Model: ${report.info.model ?: "—"}")
        appendLine("- Firmware: ${report.info.firmwareVersion ?: "—"}")
        appendLine("- Protocol: ${report.info.protocolVersion ?: "—"}")
        appendLine("- Serial: ${report.info.serialNumber ?: "—"}")
        appendLine("- Lens: ${report.info.lensInfo ?: "—"}")
        appendLine("- Transport: ${report.info.transport}")
        appendLine()
        appendLine("## Features (${report.features.size})")
        report.features.forEach { appendLine("- $it") }
        appendLine()
        appendLine("## Properties (${report.properties.size})")
        appendLine("| Code | Label | Type | W | Avail | Current | Values |")
        appendLine("|---|---|---|---|---|---|---|")
        report.properties.sortedBy { it.code }.forEach { p ->
            val values = when (val v = p.valueSet) {
                is PropertyValueSet.None -> "—"
                is PropertyValueSet.Range -> "${v.min}..${v.max} / ${v.step}"
                is PropertyValueSet.Enum -> "{${v.values.joinToString(",")}}"
            }
            val current = p.currentText ?: p.currentRaw?.toString() ?: "—"
            val known = if (p.known) "" else " *(unknown)*"
            appendLine(
                "| 0x${hexUpper(p.code, 4)} | ${p.label}$known | 0x${hexUpper(p.dataType, 4)} | " +
                    "${if (p.writable) "✓" else ""} | ${if (p.available) "✓" else ""} | $current | $values |",
            )
        }
        if (report.unknownPropertyCodes.isNotEmpty()) {
            appendLine()
            appendLine("## Unknown property codes (${report.unknownPropertyCodes.size})")
            report.unknownPropertyCodes.forEach { appendLine("- 0x${hexUpper(it, 4)}") }
        }
        if (report.observedEvents.isNotEmpty()) {
            appendLine()
            appendLine("## Observed events")
            report.observedEvents.forEach { appendLine("- $it") }
        }
        if (report.unknownEvents.isNotEmpty()) {
            appendLine()
            appendLine("## Unknown events")
            report.unknownEvents.forEach { appendLine("- $it") }
        }
    }
}
