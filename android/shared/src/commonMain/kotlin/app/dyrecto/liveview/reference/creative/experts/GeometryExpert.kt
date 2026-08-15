package app.dyrecto.liveview.reference.creative.experts

import app.dyrecto.liveview.reference.NormalizedRect
import app.dyrecto.liveview.reference.ReferenceAiProfile
import app.dyrecto.liveview.reference.ai.PrimarySubjectType
import app.dyrecto.liveview.reference.ai.SceneMode
import app.dyrecto.liveview.reference.ai.SubjectCategory
import app.dyrecto.liveview.reference.creative.CameraAngle
import app.dyrecto.liveview.reference.creative.CreativeCamera
import app.dyrecto.liveview.reference.creative.CreativeSubject
import app.dyrecto.liveview.reference.creative.CreativeSubjectKind
import app.dyrecto.liveview.reference.creative.HeadroomLevel
import app.dyrecto.liveview.reference.creative.ShotType
import app.dyrecto.liveview.reference.creative.SubjectPlacement

/**
 * Phase 16.1 — subject / shot-type / placement / headroom from geometry. This is the pre-16.1
 * `deriveSubject` / `deriveCamera` / `derivePlacement` / `deriveHeadroom` logic moved verbatim, so
 * its output is byte-identical to Phase 16. Camera angle has no reliable geometric signal today and
 * stays `UNKNOWN` (reserved), never guessed.
 */
object GeometryExpert : SceneExpert {
    override val name = "geometry"

    override fun observe(ctx: CreativeAnalysisContext): CreativeContribution {
        val subject = deriveSubject(ctx.ai, ctx.faceBox)
        val camera = deriveCamera(ctx.subjectBox, ctx.boxIsFace)
        val placement = derivePlacement(ctx.subjectBox)
        val headroom = deriveHeadroom(ctx.subjectBox, subject.kind)
        return CreativeContribution(
            subject = subject,
            camera = camera,
            placement = placement,
            headroom = headroom,
        )
    }

    private fun deriveSubject(ai: ReferenceAiProfile?, faceBox: NormalizedRect?): CreativeSubject {
        if (ai == null) {
            return if (faceBox != null) {
                CreativeSubject(CreativeSubjectKind.PORTRAIT, count = 1, confidence = 0.6f)
            } else {
                CreativeSubject()
            }
        }
        val humanCount = ai.subjects.count { it.category == SubjectCategory.HUMAN }
        val kind = when (ai.primarySubjectType) {
            PrimarySubjectType.PERSON -> if (humanCount >= 2) CreativeSubjectKind.GROUP else CreativeSubjectKind.PORTRAIT
            PrimarySubjectType.ANIMAL -> CreativeSubjectKind.ANIMAL
            PrimarySubjectType.VEHICLE -> CreativeSubjectKind.VEHICLE
            PrimarySubjectType.PRODUCT -> CreativeSubjectKind.PRODUCT
            PrimarySubjectType.FOOD -> CreativeSubjectKind.FOOD
            PrimarySubjectType.BUILDING -> CreativeSubjectKind.SCENE
            PrimarySubjectType.MULTIPLE -> if (humanCount >= 2) CreativeSubjectKind.GROUP else CreativeSubjectKind.SCENE
            PrimarySubjectType.NONE, PrimarySubjectType.UNKNOWN -> kindFromSceneMode(ai.sceneMode)
        }
        val count = when {
            ai.subjects.isNotEmpty() -> ai.subjects.size
            faceBox != null -> 1
            else -> 0
        }
        val confidence = if (ai.primarySubjectConfidence > 0f) ai.primarySubjectConfidence else ai.sceneModeConfidence
        return CreativeSubject(kind, count, confidence.coerceIn(0f, 1f))
    }

    private fun kindFromSceneMode(mode: SceneMode): CreativeSubjectKind = when (mode) {
        SceneMode.HUMAN -> CreativeSubjectKind.PORTRAIT
        SceneMode.ANIMAL -> CreativeSubjectKind.ANIMAL
        SceneMode.VEHICLE -> CreativeSubjectKind.VEHICLE
        SceneMode.PRODUCT -> CreativeSubjectKind.PRODUCT
        SceneMode.FOOD -> CreativeSubjectKind.FOOD
        SceneMode.MULTI_SUBJECT -> CreativeSubjectKind.GROUP
        SceneMode.ARCHITECTURE, SceneMode.LANDSCAPE, SceneMode.STREET,
        SceneMode.INTERIOR, SceneMode.GENERIC_SCENE -> CreativeSubjectKind.SCENE
        SceneMode.UNKNOWN -> CreativeSubjectKind.UNKNOWN
    }

    private fun deriveCamera(subjectBox: NormalizedRect?, boxIsFace: Boolean): CreativeCamera {
        if (subjectBox == null) return CreativeCamera()
        val h = subjectBox.height
        val shotType = if (boxIsFace) {
            // A face box is much smaller than a full-subject box at the same framing.
            when {
                h >= 0.60f -> ShotType.EXTREME_CLOSE_UP
                h >= 0.42f -> ShotType.CLOSE_UP
                h >= 0.28f -> ShotType.MEDIUM_CLOSE_UP
                h >= 0.14f -> ShotType.MEDIUM
                else -> ShotType.WIDE
            }
        } else {
            when {
                h >= 0.85f -> ShotType.EXTREME_CLOSE_UP
                h >= 0.60f -> ShotType.CLOSE_UP
                h >= 0.40f -> ShotType.MEDIUM_CLOSE_UP
                h >= 0.20f -> ShotType.MEDIUM
                else -> ShotType.WIDE
            }
        }
        // Camera angle has no reliable geometric signal today — left UNKNOWN (reserved), never guessed.
        return CreativeCamera(
            shotType = shotType,
            angle = CameraAngle.UNKNOWN,
            shotTypeConfidence = 0.6f,
            angleConfidence = 0f,
        )
    }

    private fun derivePlacement(subjectBox: NormalizedRect?): SubjectPlacement {
        val cx = subjectBox?.centerX ?: return SubjectPlacement.UNKNOWN
        return when {
            cx <= 0.42f -> SubjectPlacement.LEFT_THIRD
            cx >= 0.58f -> SubjectPlacement.RIGHT_THIRD
            else -> SubjectPlacement.CENTER
        }
    }

    private fun deriveHeadroom(subjectBox: NormalizedRect?, kind: CreativeSubjectKind): HeadroomLevel {
        // Headroom is only meaningful for people/animals framed with space above them.
        if (subjectBox == null) return HeadroomLevel.UNKNOWN
        if (kind != CreativeSubjectKind.PORTRAIT && kind != CreativeSubjectKind.GROUP &&
            kind != CreativeSubjectKind.ANIMAL
        ) {
            return HeadroomLevel.UNKNOWN
        }
        return when {
            subjectBox.top < 0.05f -> HeadroomLevel.TIGHT
            subjectBox.top <= 0.20f -> HeadroomLevel.BALANCED
            else -> HeadroomLevel.GENEROUS
        }
    }
}
