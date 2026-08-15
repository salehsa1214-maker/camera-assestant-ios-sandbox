import Foundation
import UIKit
import MediaPipeTasksVision
import DyrectoShared

/// iOS implementations of the shared Phase-10 engine seams, mirroring the Android
/// `MediaPipeEngines` contract exactly: lazy-load on first use; any load/inference-init failure
/// latches the engine FAILED and every later call degrades gracefully (empty/nil) — a broken
/// model downgrades a capability, never crashes. Models are the SAME .tflite files the Android
/// app bundles (project.yml references android/app/src/main/assets/models as resources).
///
/// PlatformImage's iOS actual wraps the producing side's UIImage in `handle`.
private func unwrapImage(_ platformImage: PlatformImage) -> UIImage? {
    platformImage.handle as? UIImage
}

private enum EngineState<T> {
    case notLoaded
    case ready(T)
    case failed(String)
}

// MARK: - Object detection (EfficientDet-Lite0, CPU)

final class IosObjectDetectorEngine: ObjectDetectorEngine {
    private let lock = NSLock()
    private var state: EngineState<MediaPipeTasksVision.ObjectDetector> = .notLoaded
    private static let modelId = "efficientdet_lite0"

    var capability: AiCapability {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .notLoaded: return AiCapability(modelId: Self.modelId, status: .notLoaded, error: nil)
        case .ready: return AiCapability(modelId: Self.modelId, status: .ready, error: nil)
        case .failed(let e): return AiCapability(modelId: Self.modelId, status: .failed, error: e)
        }
    }

    private func detector() -> MediaPipeTasksVision.ObjectDetector? {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .ready(let d): return d
        case .failed: return nil
        case .notLoaded:
            do {
                guard let path = Bundle.main.path(forResource: "efficientdet_lite0", ofType: "tflite") else {
                    throw NSError(domain: "app.dyrecto", code: 10, userInfo: [
                        NSLocalizedDescriptionKey: "efficientdet_lite0.tflite missing from bundle"])
                }
                let options = ObjectDetectorOptions()
                options.baseOptions.modelAssetPath = path
                options.runningMode = .image
                options.maxResults = 8
                options.scoreThreshold = 0.30 // Android MediaPipeEngines threshold
                let d = try MediaPipeTasksVision.ObjectDetector(options: options)
                state = .ready(d)
                logInfo("engine \(Self.modelId): READY")
                return d
            } catch {
                state = .failed("\(error)")
                logError("engine \(Self.modelId): FAILED — \(error)")
                return nil
            }
        }
    }

    func detect(bitmap: PlatformImage, completionHandler: @escaping ([DetectedObject]?, Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let detector = self.detector(),
                  let image = unwrapImage(bitmap),
                  let mpImage = try? MPImage(uiImage: image) else {
                completionHandler([], nil)
                return
            }
            do {
                let result = try detector.detect(image: mpImage)
                let width = Float(image.size.width * image.scale)
                let height = Float(image.size.height * image.scale)
                guard width > 0, height > 0 else {
                    completionHandler([], nil)
                    return
                }
                let objects: [DetectedObject] = result.detections.compactMap { detection in
                    guard let category = detection.categories.first else { return nil }
                    let box = detection.boundingBox
                    // MediaPipe returns pixel-space boxes for MPImage(uiImage:) — normalize 0..1.
                    let rect = NormalizedRect(
                        left: max(0, Float(box.minX) / width),
                        top: max(0, Float(box.minY) / height),
                        right: min(1, Float(box.maxX) / width),
                        bottom: min(1, Float(box.maxY) / height))
                    return DetectedObject(
                        className: category.categoryName ?? "",
                        classId: Int32(category.index),
                        confidence: category.score,
                        boundingBox: rect)
                }
                completionHandler(objects, nil)
            } catch {
                logError("detect failed: \(error) — returning empty (fail-safe contract)")
                completionHandler([], nil)
            }
        }
    }
}

// MARK: - Image embedding (MobileNetV3-Small, CPU)

final class IosEmbeddingEngine: EmbeddingEngine {
    private let lock = NSLock()
    private var state: EngineState<ImageEmbedder> = .notLoaded
    private static let modelId = "mobilenet_v3_small"

    var capability: AiCapability {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .notLoaded: return AiCapability(modelId: Self.modelId, status: .notLoaded, error: nil)
        case .ready: return AiCapability(modelId: Self.modelId, status: .ready, error: nil)
        case .failed(let e): return AiCapability(modelId: Self.modelId, status: .failed, error: e)
        }
    }

    private func embedder() -> ImageEmbedder? {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .ready(let e): return e
        case .failed: return nil
        case .notLoaded:
            do {
                guard let path = Bundle.main.path(forResource: "mobilenet_v3_small", ofType: "tflite") else {
                    throw NSError(domain: "app.dyrecto", code: 11, userInfo: [
                        NSLocalizedDescriptionKey: "mobilenet_v3_small.tflite missing from bundle"])
                }
                let options = ImageEmbedderOptions()
                options.baseOptions.modelAssetPath = path
                options.runningMode = .image
                options.l2Normalize = true // Android parity: cosine-ready vectors
                let e = try ImageEmbedder(options: options)
                state = .ready(e)
                logInfo("engine \(Self.modelId): READY")
                return e
            } catch {
                state = .failed("\(error)")
                logError("engine \(Self.modelId): FAILED — \(error)")
                return nil
            }
        }
    }

    func embed(bitmap: PlatformImage, completionHandler: @escaping (VisualEmbeddingResult?, Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let embedder = self.embedder(),
                  let image = unwrapImage(bitmap),
                  let mpImage = try? MPImage(uiImage: image) else {
                completionHandler(nil, nil)
                return
            }
            do {
                let result = try embedder.embed(image: mpImage)
                guard let embedding = result.embeddingResult.embeddings.first,
                      let floats = embedding.floatEmbedding else {
                    completionHandler(nil, nil)
                    return
                }
                let array = KotlinFloatArray(size: Int32(floats.count))
                for (i, v) in floats.enumerated() {
                    array.set(index: Int32(i), value: v.floatValue)
                }
                completionHandler(VisualEmbeddingResult(
                    embedding: array, modelId: Self.modelId, confidence: 1.0), nil)
            } catch {
                logError("embed failed: \(error) — returning nil (fail-safe contract)")
                completionHandler(nil, nil)
            }
        }
    }
}

// MARK: - Segmentation (DeepLabV3, CPU)

final class IosSegmentationEngine: SegmentationEngine {
    private let lock = NSLock()
    private var state: EngineState<ImageSegmenter> = .notLoaded
    private static let modelId = "deeplab_v3"

    var capability: AiCapability {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .notLoaded: return AiCapability(modelId: Self.modelId, status: .notLoaded, error: nil)
        case .ready: return AiCapability(modelId: Self.modelId, status: .ready, error: nil)
        case .failed(let e): return AiCapability(modelId: Self.modelId, status: .failed, error: e)
        }
    }

    private func segmenter() -> ImageSegmenter? {
        lock.lock(); defer { lock.unlock() }
        switch state {
        case .ready(let s): return s
        case .failed: return nil
        case .notLoaded:
            do {
                guard let path = Bundle.main.path(forResource: "deeplab_v3", ofType: "tflite") else {
                    throw NSError(domain: "app.dyrecto", code: 12, userInfo: [
                        NSLocalizedDescriptionKey: "deeplab_v3.tflite missing from bundle"])
                }
                let options = ImageSegmenterOptions()
                options.baseOptions.modelAssetPath = path
                options.runningMode = .image
                options.shouldOutputCategoryMask = true
                options.shouldOutputConfidenceMasks = false
                let s = try ImageSegmenter(options: options)
                state = .ready(s)
                logInfo("engine \(Self.modelId): READY")
                return s
            } catch {
                state = .failed("\(error)")
                logError("engine \(Self.modelId): FAILED — \(error)")
                return nil
            }
        }
    }

    func segment(bitmap: PlatformImage, completionHandler: @escaping (SegmentationResult?, Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let segmenter = self.segmenter(),
                  let image = unwrapImage(bitmap),
                  let mpImage = try? MPImage(uiImage: image) else {
                completionHandler(nil, nil)
                return
            }
            do {
                let result = try segmenter.segment(image: mpImage)
                guard let mask = result.categoryMask else {
                    completionHandler(nil, nil)
                    return
                }
                // Walk the category mask: non-background (≠0) = subject/foreground. Compute
                // coverage + tight normalized bounds — the same statistics the Android engine
                // derives before handing off to the pure SegmentationManager.
                let width = mask.width
                let height = mask.height
                let data = mask.uint8Data
                var covered = 0
                var minX = width, minY = height, maxX = -1, maxY = -1
                for y in 0..<height {
                    for x in 0..<width {
                        if data[y * width + x] != 0 {
                            covered += 1
                            if x < minX { minX = x }
                            if y < minY { minY = y }
                            if x > maxX { maxX = x }
                            if y > maxY { maxY = y }
                        }
                    }
                }
                let total = width * height
                let coverage = total > 0 ? Float(covered) / Float(total) : 0
                let box: NormalizedRect? = maxX >= minX ? NormalizedRect(
                    left: Float(minX) / Float(width),
                    top: Float(minY) / Float(height),
                    right: Float(maxX + 1) / Float(width),
                    bottom: Float(maxY + 1) / Float(height)) : nil
                completionHandler(SegmentationResult(
                    maskAvailable: covered > 0,
                    coverage: coverage,
                    maskBox: box,
                    source: .model), nil)
            } catch {
                logError("segment failed: \(error) — returning nil (fail-safe contract)")
                completionHandler(nil, nil)
            }
        }
    }
}
