package app.dyrecto.capability

/**
 * Turns raw, protocol-neutral property records (from the adapter) into the normalized
 * [CameraCapabilities] snapshot. Pure and camera-agnostic — unit-testable without any transport.
 *
 * Rule 3 (forward compatibility): unknown codes are mapped to [CameraProperty] with `known=false`
 * and a synthesized label, never discarded.
 */
object CapabilityMapper {

    /** PTP `DT_STR`. */
    private const val DT_STR = 0xFFFF

    fun toProperty(record: RawPropertyRecord): CameraProperty {
        val knownLabel = PropertyCatalog.knownLabel(record.code)
        return CameraProperty(
            code = record.code,
            dataType = record.dataType,
            currentRaw = record.currentRaw,
            currentText = record.currentText,
            writable = record.getSet == 1,
            available = record.availability != 0,
            valueSet = record.form.toValueSet(),
            label = knownLabel ?: PropertyCatalog.label(record.code),
            known = knownLabel != null,
            getSetRaw = record.getSet,
            availabilityRaw = record.availability,
        )
    }

    /** Builds a full snapshot: maps every record, then resolves the high-level feature set. */
    fun build(info: CameraInfo, records: List<RawPropertyRecord>): CameraCapabilities {
        val properties = records.map { toProperty(it) }
        val features = FeatureResolver.resolve(properties, info.transport)
        return CameraCapabilities(
            schemaVersion = CapabilitySchema.VERSION,
            info = info,
            properties = properties,
            features = features,
        )
    }

    private fun RawForm.toValueSet(): PropertyValueSet = when (this) {
        is RawForm.None -> PropertyValueSet.None
        is RawForm.Range -> PropertyValueSet.Range(min, max, step)
        is RawForm.Enum -> PropertyValueSet.Enum(values)
    }
}
