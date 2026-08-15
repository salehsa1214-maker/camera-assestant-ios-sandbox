package app.dyrecto.capability

/**
 * Derives normalized [CameraEvent]s by diffing two consecutive capability snapshots. Pure and
 * camera-agnostic: it emits generic [CameraEvent.PropertyChanged] for every value that changed, so
 * downstream consumers (settings-drift alerts, the Capability Explorer's event log) never touch raw
 * telemetry. Sony-specific semantic classification (e.g. which rec-state value means "recording")
 * stays out of here — that interpretation belongs to the alert/reference layer that owns the meaning.
 *
 * A property that only *appears* (new code) or *disappears* is not reported as a change: forward
 * compatibility means we surface it in the snapshot, not as a spurious value event.
 */
object CameraEventDiffer {
    fun diff(old: CameraCapabilities?, new: CameraCapabilities): List<CameraEvent> {
        if (old == null) return emptyList()
        val prev = old.properties.associateBy { it.code }
        val events = ArrayList<CameraEvent>()
        for (p in new.properties) {
            val before = prev[p.code] ?: continue
            if (before.currentRaw != p.currentRaw) {
                events.add(CameraEvent.PropertyChanged(p.code, before.currentRaw, p.currentRaw))
            }
        }
        return events
    }
}
