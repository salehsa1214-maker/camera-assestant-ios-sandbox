package app.dyrecto.liveview.reference

/**
 * UI-facing status of the Shot Reference / Storyboard assistant: the reference session (Phase 10 —
 * profiles are session-owned; Phase 15 makes 0..N shots a first-class storyboard), whether the user
 * has monitoring switched on, and transient analysis progress/errors. [profile] stays as the
 * active-profile accessor so existing single-reference consumers keep working unchanged.
 *
 * (Split out of ReferenceMonitorManager.kt in the KMP move — the manager stays app-side.)
 */
data class ReferenceSessionState(
    val session: ReferenceSession? = null,
    /** User pressed Start Monitoring (persists across disconnects within the process). */
    val monitoringRequested: Boolean = false,
    /** A picked image is currently being decoded/analyzed. */
    val analyzing: Boolean = false,
    /** Last analysis error, cleared by the next successful action. */
    val error: String? = null,
) {
    val profile: ReferenceProfile? get() = session?.activeProfile

    // ---- Phase 15: storyboard progress (derived from the persisted per-shot completion) ----
    val totalCount: Int get() = session?.profiles?.size ?: 0
    val completedCount: Int get() = session?.completedCount ?: 0
    val progress: StoryboardProgress get() = StoryboardProgress(completedCount, totalCount)
    val completionRule: StoryboardCompletionRule
        get() = session?.completionRule ?: StoryboardCompletionRule()
}
