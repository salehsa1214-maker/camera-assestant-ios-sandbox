package app.dyrecto.liveview.reference

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

private val Context.shotReferenceDataStore by preferencesDataStore(name = "shot_reference")

/**
 * Local persistence for the [ReferenceSession] (Phase 10). The session (profiles + options +
 * AI understanding) is one JSON file; each embedding is its own versioned JSON file; the
 * reference image is copied into app-private storage so the thumbnail survives the source URI
 * being revoked. Fully local: no cloud, no accounts, no upload.
 *
 * Writes are atomic (.tmp + rename). Reads are defensive: malformed data reads as "nothing
 * stored" rather than crashing, and a pre-Phase-10 install is migrated for free — [loadSession]
 * falls back to the legacy flat DataStore profile and wraps it into a one-profile session with
 * `ai = null`.
 */
class ReferenceRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.shotReferenceDataStore

    /** App-private directory holding the copied reference image + JSON files. */
    private val storageDir: File get() = File(appContext.filesDir, "shot_reference")

    private val sessionFile: File get() = File(storageDir.apply { mkdirs() }, "session.json")

    private fun embeddingFileFor(id: String): File =
        File(storageDir.apply { mkdirs() }, "embedding_$id.json")

    /** Destination file for the reference image copy of profile [id]. */
    fun imageFileFor(id: String): File = File(storageDir.apply { mkdirs() }, "reference_$id.jpg")

    /** Compresses [bitmap] into the app-private image file for [id]; returns its URI string. */
    fun persistImage(bitmap: Bitmap, id: String): String {
        val file = imageFileFor(id)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
        }
        return android.net.Uri.fromFile(file).toString()
    }

    // ---- Session (Phase 10, JSON) ----

    suspend fun saveSession(session: ReferenceSession) = withContext(Dispatchers.IO) {
        writeAtomically(sessionFile, ReferenceProfileCodec.encodeSession(session))
    }

    /**
     * Restores the stored session: JSON first, then the legacy Phase 9 DataStore profile wrapped
     * into a one-profile session. Null when nothing (valid) is stored.
     */
    suspend fun loadSession(): ReferenceSession? {
        val fromJson = withContext(Dispatchers.IO) {
            runCatching { sessionFile.takeIf { it.isFile }?.readText() }.getOrNull()
                ?.let { ReferenceProfileCodec.decodeSession(it) }
        }
        if (fromJson != null) return fromJson

        val legacy = loadLegacyProfile() ?: return null
        return ReferenceSession(
            id = "session-${legacy.id}",
            name = null,
            createdAtMs = legacy.createdAtMs,
            profiles = listOf(legacy),
            activeProfileId = legacy.id,
        )
    }

    suspend fun saveEmbedding(embedding: ReferenceEmbedding) = withContext(Dispatchers.IO) {
        writeAtomically(embeddingFileFor(embedding.id), ReferenceProfileCodec.encodeEmbedding(embedding))
    }

    suspend fun loadEmbedding(id: String): ReferenceEmbedding? = withContext(Dispatchers.IO) {
        runCatching { embeddingFileFor(id).takeIf { it.isFile }?.readText() }.getOrNull()
            ?.let { ReferenceProfileCodec.decodeEmbedding(it) }
    }

    /** Deletes the embedding file for [id] (Phase 15: removing/replacing a storyboard shot). */
    suspend fun deleteEmbedding(id: String) = withContext(Dispatchers.IO) {
        runCatching { embeddingFileFor(id).delete() }
        Unit
    }

    private fun writeAtomically(target: File, content: String) {
        val tmp = File(target.parentFile, "${target.name}.tmp")
        tmp.writeText(content)
        if (!tmp.renameTo(target)) {
            // Windows-style rename-over-existing failure: replace explicitly.
            target.delete()
            tmp.renameTo(target)
        }
    }

    // ---- Legacy Phase 9 profile (flat DataStore keys, read-only) ----

    /** Reads a pre-Phase-10 profile from the legacy flat DataStore keys (never written anymore). */
    private suspend fun loadLegacyProfile(): ReferenceProfile? {
        val p = dataStore.data.first()
        val id = p[KEY_ID] ?: return null
        return runCatching {
            val subject = if (p[KEY_SUBJECT_PRESENT] == true) {
                ReferenceSubjectProfile(
                    normalizedCenterX = p[KEY_SUBJECT_CX]!!,
                    normalizedCenterY = p[KEY_SUBJECT_CY]!!,
                    normalizedWidth = p[KEY_SUBJECT_W]!!,
                    normalizedHeight = p[KEY_SUBJECT_H]!!,
                    normalizedArea = p[KEY_SUBJECT_AREA]!!,
                )
            } else null
            val face = if (p[KEY_FACE_PRESENT] == true) {
                ReferenceFaceProfile(
                    faceDetected = p[KEY_FACE_DETECTED] ?: false,
                    eyesDetected = p[KEY_FACE_EYES] ?: false,
                    faceBox = subject?.let {
                        NormalizedRect(
                            left = it.normalizedCenterX - it.normalizedWidth / 2f,
                            top = it.normalizedCenterY - it.normalizedHeight / 2f,
                            right = it.normalizedCenterX + it.normalizedWidth / 2f,
                            bottom = it.normalizedCenterY + it.normalizedHeight / 2f,
                        )
                    },
                    eyeCount = p[KEY_FACE_EYE_COUNT] ?: 0,
                )
            } else null
            ReferenceProfile(
                id = id,
                name = p[KEY_NAME] ?: "Reference",
                createdAtMs = p[KEY_CREATED_AT] ?: 0L,
                imageUri = p[KEY_IMAGE_URI],
                width = p[KEY_WIDTH] ?: 0,
                height = p[KEY_HEIGHT] ?: 0,
                exposure = ReferenceExposureProfile(
                    mean = p[KEY_EXP_MEAN]!!,
                    median = p[KEY_EXP_MEDIAN]!!,
                    p95 = p[KEY_EXP_P95]!!,
                    p99 = p[KEY_EXP_P99]!!,
                    highlightCoverage = p[KEY_EXP_HIGHLIGHT]!!,
                    shadowCoverage = p[KEY_EXP_SHADOW]!!,
                ),
                color = ReferenceColorProfile(
                    avgR = p[KEY_COLOR_R]!!,
                    avgG = p[KEY_COLOR_G]!!,
                    avgB = p[KEY_COLOR_B]!!,
                    warmthScore = p[KEY_COLOR_WARMTH]!!,
                    tintScore = p[KEY_COLOR_TINT]!!,
                ),
                subject = subject,
                face = face,
                options = ReferenceMonitorOptions(
                    monitorExposure = p[KEY_OPT_EXPOSURE] ?: true,
                    monitorSubjectPosition = p[KEY_OPT_POSITION] ?: true,
                    monitorSubjectSize = p[KEY_OPT_SIZE] ?: true,
                    monitorWhiteBalance = p[KEY_OPT_WB] ?: true,
                    monitorFraming = p[KEY_OPT_FRAMING] ?: false,
                    monitorHeadroom = p[KEY_OPT_HEADROOM] ?: true,
                    monitorFacePresence = p[KEY_OPT_FACE] ?: true,
                    monitorEyeVisibility = p[KEY_OPT_EYES] ?: true,
                    tolerance = p[KEY_OPT_TOLERANCE]
                        ?.let { name -> runCatching { ReferenceTolerance.valueOf(name) }.getOrNull() }
                        ?: ReferenceTolerance.MEDIUM,
                ),
            )
        }.getOrNull()
    }

    /** Removes the stored session, embeddings, legacy keys, and copied reference images. */
    suspend fun clear() {
        dataStore.edit { it.clear() }
        withContext(Dispatchers.IO) {
            storageDir.listFiles()?.forEach { it.delete() }
        }
    }

    private companion object {
        const val IMAGE_QUALITY = 90

        val KEY_ID = stringPreferencesKey("id")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_CREATED_AT = longPreferencesKey("createdAtMs")
        val KEY_IMAGE_URI = stringPreferencesKey("imageUri")
        val KEY_WIDTH = intPreferencesKey("width")
        val KEY_HEIGHT = intPreferencesKey("height")

        val KEY_EXP_MEAN = floatPreferencesKey("exposure.mean")
        val KEY_EXP_MEDIAN = floatPreferencesKey("exposure.median")
        val KEY_EXP_P95 = floatPreferencesKey("exposure.p95")
        val KEY_EXP_P99 = floatPreferencesKey("exposure.p99")
        val KEY_EXP_HIGHLIGHT = floatPreferencesKey("exposure.highlightCoverage")
        val KEY_EXP_SHADOW = floatPreferencesKey("exposure.shadowCoverage")

        val KEY_COLOR_R = floatPreferencesKey("color.avgR")
        val KEY_COLOR_G = floatPreferencesKey("color.avgG")
        val KEY_COLOR_B = floatPreferencesKey("color.avgB")
        val KEY_COLOR_WARMTH = floatPreferencesKey("color.warmthScore")
        val KEY_COLOR_TINT = floatPreferencesKey("color.tintScore")

        val KEY_SUBJECT_PRESENT = booleanPreferencesKey("subject.present")
        val KEY_SUBJECT_CX = floatPreferencesKey("subject.centerX")
        val KEY_SUBJECT_CY = floatPreferencesKey("subject.centerY")
        val KEY_SUBJECT_W = floatPreferencesKey("subject.width")
        val KEY_SUBJECT_H = floatPreferencesKey("subject.height")
        val KEY_SUBJECT_AREA = floatPreferencesKey("subject.area")

        val KEY_FACE_PRESENT = booleanPreferencesKey("face.present")
        val KEY_FACE_DETECTED = booleanPreferencesKey("face.detected")
        val KEY_FACE_EYES = booleanPreferencesKey("face.eyesDetected")
        val KEY_FACE_EYE_COUNT = intPreferencesKey("face.eyeCount")

        val KEY_OPT_EXPOSURE = booleanPreferencesKey("options.exposure")
        val KEY_OPT_POSITION = booleanPreferencesKey("options.position")
        val KEY_OPT_SIZE = booleanPreferencesKey("options.size")
        val KEY_OPT_WB = booleanPreferencesKey("options.whiteBalance")
        val KEY_OPT_FRAMING = booleanPreferencesKey("options.framing")
        val KEY_OPT_HEADROOM = booleanPreferencesKey("options.headroom")
        val KEY_OPT_FACE = booleanPreferencesKey("options.facePresence")
        val KEY_OPT_EYES = booleanPreferencesKey("options.eyeVisibility")
        val KEY_OPT_TOLERANCE = stringPreferencesKey("options.tolerance")
    }
}
