package app.dyrecto.liveview.reference.creative.semantic

import app.dyrecto.liveview.reference.creative.CreativeAspect

/**
 * Phase 16.1 — the output of the MobileCLIP semantic expert for one reference image, produced ONCE
 * at import. This is transient runtime input to `CreativeSceneAnalyzer`; only the distilled results
 * (concepts / relationships / signature) are persisted on the `CreativeSceneModel`.
 *
 * MobileCLIP is an information source, not a decision maker: this type carries evidence only. It
 * never produces alerts, instructions, priorities, or reasoning decisions directly — the analyzer
 * decides what (if anything) to do with these concepts.
 */
data class SemanticObservation(
    /** Scored concepts, sorted by [ConceptScore.score] descending. */
    val concepts: List<ConceptScore>,
    /** L2-normalized image embedding (512-d for MobileCLIP2-S0), or null if unavailable. */
    val embedding: FloatArray?,
    val modelId: String,
) {
    /** Concepts at or above [minScore], preserving the sorted order. */
    fun topConcepts(minScore: Float, limit: Int): List<ConceptScore> =
        concepts.asSequence().filter { it.score >= minScore }.take(limit).toList()

    /** Highest-scoring concept for [aspect] at or above [minScore], or null. */
    fun bestFor(aspect: CreativeAspect, minScore: Float): ConceptScore? =
        concepts.firstOrNull { it.aspect == aspect && it.score >= minScore }

    /** Highest-scoring concept in [family] at or above [minScore], or null. */
    fun bestInFamily(family: String, minScore: Float): ConceptScore? =
        concepts.firstOrNull { it.family == family && it.score >= minScore }

    // Identity-based equals/hashCode are meaningless for a FloatArray field; this type is only ever
    // compared by reference in practice. Explicit overrides silence the array-in-data-class warning.
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

/**
 * One scored concept: the cosine similarity of the reference's image embedding to a precomputed
 * concept text embedding from the bundled vocabulary. [aspect] links the concept to a creative
 * aspect when it maps to one; [family] groups related concepts (e.g. "shot_type", "lighting").
 */
data class ConceptScore(
    val label: String,
    val score: Float,
    val aspect: CreativeAspect?,
    val family: String,
)
