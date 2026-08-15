package app.dyrecto.liveview.reference

import kotlinx.serialization.json.Json

/**
 * Pure JSON codec for the reference session + embedding files (Phase 10). Kept free of any
 * file/Android concern so round-trips are plain JVM-testable; [ReferenceRepository] owns where
 * the bytes live.
 *
 * Decoding is defensive: any malformed payload returns null ("no stored data") instead of
 * throwing, and unknown keys are ignored so future schema additions stay backward-readable.
 */
object ReferenceProfileCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encodeSession(session: ReferenceSession): String =
        json.encodeToString(ReferenceSession.serializer(), session)

    fun decodeSession(text: String): ReferenceSession? =
        runCatching { json.decodeFromString(ReferenceSession.serializer(), text) }.getOrNull()

    fun encodeEmbedding(embedding: ReferenceEmbedding): String =
        json.encodeToString(ReferenceEmbedding.serializer(), embedding)

    fun decodeEmbedding(text: String): ReferenceEmbedding? =
        runCatching { json.decodeFromString(ReferenceEmbedding.serializer(), text) }.getOrNull()
}
