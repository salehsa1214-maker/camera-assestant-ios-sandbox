package app.dyrecto.liveview.reference.ai.semantic

import app.dyrecto.liveview.reference.ai.SubjectCategory

/**
 * [SemanticMapper] for the COCO-80 vocabulary (paired with the EfficientDet-Lite0 detector).
 *
 * This object is the ONLY place COCO label strings exist in the codebase. Mapping is by label
 * (not class index) because label strings are stable across COCO-trained model exports while
 * index order is not guaranteed. Unknown labels map to [SubjectCategory.UNKNOWN].
 */
object CocoSemanticMapper : SemanticMapper {
    override val id: String = "coco-80"

    private val byLabel: Map<String, SubjectCategory> = buildMap {
        put("person", SubjectCategory.HUMAN)

        for (label in listOf(
            "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
        )) put(label, SubjectCategory.ANIMAL)

        for (label in listOf(
            "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
        )) put(label, SubjectCategory.VEHICLE)

        for (label in listOf(
            "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza",
            "donut", "cake", "bowl", "wine glass", "cup", "fork", "knife", "spoon",
        )) put(label, SubjectCategory.FOOD)

        for (label in listOf(
            "chair", "couch", "bed", "dining table", "toilet", "bench",
        )) put(label, SubjectCategory.FURNITURE)

        for (label in listOf(
            "tv", "laptop", "mouse", "remote", "keyboard", "cell phone", "microwave", "oven",
            "toaster", "refrigerator", "hair drier",
        )) put(label, SubjectCategory.ELECTRONICS)

        for (label in listOf("potted plant")) put(label, SubjectCategory.NATURE)

        // Portable/product-like items: the "held or placed object" group.
        for (label in listOf(
            "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard",
            "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "book", "clock", "vase", "scissors", "teddy bear",
            "toothbrush",
        )) put(label, SubjectCategory.PRODUCT)

        // Street furniture reads as scene context, not a followable product.
        for (label in listOf(
            "traffic light", "fire hydrant", "stop sign", "parking meter",
        )) put(label, SubjectCategory.UNKNOWN)
    }

    override fun map(rawLabel: String, classId: Int): SubjectCategory =
        byLabel[rawLabel.lowercase().trim()] ?: SubjectCategory.UNKNOWN
}
