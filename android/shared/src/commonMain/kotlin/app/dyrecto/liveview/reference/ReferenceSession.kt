package app.dyrecto.liveview.reference

import kotlinx.serialization.Serializable

/**
 * A reference session owns the [ReferenceProfile]s the assistant can compare against (Phase 10).
 *
 * List-shaped FROM DAY ONE even though the first release keeps exactly 0..1 profiles: multi-shot
 * workflows (automotive, real estate, products, weddings, storyboard matching) later become
 * "add profile / switch active" with no schema or API break. Comparison always runs against
 * [activeProfile].
 */
@Serializable
data class ReferenceSession(
    val id: String,
    val name: String? = null,
    val createdAtMs: Long,
    val profiles: List<ReferenceProfile> = emptyList(),
    val activeProfileId: String? = null,
    /** Phase 15: storyboard-wide completion rule (hold/confirm/threshold), persisted here. */
    val completionRule: StoryboardCompletionRule = StoryboardCompletionRule(),
    val schemaVersion: Int = 1,
) {
    val activeProfile: ReferenceProfile?
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.firstOrNull()

    /** v1 semantics: one profile per session — adding replaces, and activates, the profile. */
    fun withProfile(profile: ReferenceProfile): ReferenceSession =
        copy(profiles = listOf(profile), activeProfileId = profile.id)

    fun withUpdatedActiveProfile(profile: ReferenceProfile): ReferenceSession =
        copy(profiles = profiles.map { if (it.id == profile.id) profile else it })

    // ---- Phase 15: storyboard (multi-shot) operations ----

    /** Appends [profile] as a new storyboard shot; the first one added becomes the active shot. */
    fun addProfile(profile: ReferenceProfile): ReferenceSession =
        copy(
            profiles = profiles + profile,
            activeProfileId = activeProfileId ?: profile.id,
        )

    /** Removes shot [id]; re-points [activeProfileId] to the first remaining shot if needed. */
    fun removeProfile(id: String): ReferenceSession {
        val remaining = profiles.filterNot { it.id == id }
        return copy(
            profiles = remaining,
            activeProfileId = if (activeProfileId == id) remaining.firstOrNull()?.id else activeProfileId,
        )
    }

    /** Replaces the shot carrying [profile.id] in place (order preserved). */
    fun withUpdatedProfile(profile: ReferenceProfile): ReferenceSession =
        copy(profiles = profiles.map { if (it.id == profile.id) profile else it })

    /** Swaps shot [oldId] for [profile] in place (position preserved), re-pointing the active id. */
    fun withReplacedProfile(oldId: String, profile: ReferenceProfile): ReferenceSession =
        copy(
            profiles = profiles.map { if (it.id == oldId) profile else it },
            activeProfileId = if (activeProfileId == oldId) profile.id else activeProfileId,
        )

    /** Storyboard-wide: applies [options] to every shot (monitor settings are not per-shot). */
    fun withOptionsForAll(options: ReferenceMonitorOptions): ReferenceSession =
        copy(profiles = profiles.map { it.copy(options = options) })

    fun withCompletionRule(rule: StoryboardCompletionRule): ReferenceSession =
        copy(completionRule = rule)

    /** Clears every shot's completion progress (the explicit "reset progress" action). */
    fun withProgressReset(): ReferenceSession =
        copy(profiles = profiles.map { it.copy(completion = ShotCompletion()) })

    /** Number of shots marked completed. */
    val completedCount: Int get() = profiles.count { it.completion.completed }
}

/**
 * Phase 15: the rule that decides when a storyboard shot flips to Completed. All three values are
 * user-facing defaults tunable post-hardware: the live match must stay at or above [matchThreshold]
 * continuously for [holdSeconds], and that must happen [confirmCount] independent times (each
 * separated by the match dropping below the threshold). Persisted with the [ReferenceSession].
 */
@Serializable
data class StoryboardCompletionRule(
    val holdSeconds: Int = 5,
    val confirmCount: Int = 1,
    val matchThreshold: Float = 0.80f,
) {
    val holdMs: Long get() = holdSeconds.toLong() * 1000L
}
