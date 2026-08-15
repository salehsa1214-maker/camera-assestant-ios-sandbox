import Foundation
import UIKit
#if canImport(TensorFlowLite)
import TensorFlowLite
#endif
import DyrectoShared

/// iOS implementation of the shared `SemanticSceneEngine` — the Phase 16.1 MobileCLIP2-S0
/// image tower, mirroring the Android `MobileClipSemanticEngine` exactly:
///
///  - SAME model file (mobileclip2_s0_image.tflite, fp32) and SAME concept vocabulary JSON,
///    bundled from the Android assets (single source of truth).
///  - Preprocessing: resize to 256×256, **NO mean/std normalization** — raw pixel/255
///    (MobileCLIP2-S0 trains with Normalize(mean=0, std=1); using OpenAI CLIP stats breaks
///    concept scores — the hard-won Android finding).
///  - Input layout auto-detected: NCHW [1,3,H,W] (the shipped model) or NHWC [1,H,W,3].
///  - Output embedding L2-normalized here; cosine scoring by the shared pure ConceptVocabulary.
///  - Fail-safe: any missing/invalid/incompatible model or vocab latches FAILED and observe()
///    returns nil — the creative analyzer degrades to deterministic experts, import still works.
///  - Import-time only: never called per frame.
#if canImport(TensorFlowLite)
final class IosMobileClipEngine: SemanticSceneEngine {

    private static let modelId = "mobileclip2_s0"
    private static let inputSize = 256

    private let lock = NSLock()
    private var interpreter: Interpreter?
    private var vocabulary: ConceptVocabulary?
    private var failed: String?
    private var channelFirst = true
    private var inputWidth = inputSize
    private var inputHeight = inputSize

    var capability: AiCapability {
        lock.lock(); defer { lock.unlock() }
        if let failed { return AiCapability(modelId: Self.modelId, status: .failed, error: failed) }
        if interpreter != nil { return AiCapability(modelId: Self.modelId, status: .ready, error: nil) }
        return AiCapability(modelId: Self.modelId, status: .notLoaded, error: nil)
    }

    private func loaded() -> (Interpreter, ConceptVocabulary)? {
        lock.lock(); defer { lock.unlock() }
        if let i = interpreter, let v = vocabulary { return (i, v) }
        if failed != nil { return nil }
        do {
            guard let modelPath = Bundle.main.path(forResource: "mobileclip2_s0_image", ofType: "tflite") else {
                throw NSError(domain: "app.dyrecto", code: 20, userInfo: [
                    NSLocalizedDescriptionKey: "mobileclip2_s0_image.tflite missing from bundle"])
            }
            var options = Interpreter.Options()
            options.threadCount = 2
            let itp = try Interpreter(modelPath: modelPath, options: options)
            try itp.allocateTensors()

            // Shape discovery: NCHW [1,3,H,W] vs NHWC [1,H,W,3] (Android parity).
            let input = try itp.input(at: 0)
            let dims = input.shape.dimensions
            guard dims.count == 4 else { throw NSError(domain: "app.dyrecto", code: 21, userInfo: [
                NSLocalizedDescriptionKey: "unexpected input rank \(dims)"]) }
            if dims[1] == 3 {
                channelFirst = true
                inputHeight = dims[2]; inputWidth = dims[3]
            } else if dims[3] == 3 {
                channelFirst = false
                inputHeight = dims[1]; inputWidth = dims[2]
            } else {
                throw NSError(domain: "app.dyrecto", code: 22, userInfo: [
                    NSLocalizedDescriptionKey: "input shape \(dims) has no channel-3 axis"])
            }

            guard let vocabUrl = Bundle.main.url(forResource: "concept_vocab_mobileclip2_s0", withExtension: "json"),
                  let vocabText = try? String(contentsOf: vocabUrl, encoding: .utf8),
                  let vocab = ConceptVocabulary.companion.fromJson(text: vocabText) else {
                throw NSError(domain: "app.dyrecto", code: 23, userInfo: [
                    NSLocalizedDescriptionKey: "concept vocabulary missing/invalid"])
            }

            let output = try itp.output(at: 0)
            let outputDim = output.shape.dimensions.reduce(1, *)
            guard Int(vocab.dimensions) == outputDim else {
                throw NSError(domain: "app.dyrecto", code: 24, userInfo: [
                    NSLocalizedDescriptionKey: "vocab dim \(vocab.dimensions) != model output \(outputDim)"])
            }

            interpreter = itp
            vocabulary = vocab
            logInfo("engine \(Self.modelId): READY (\(channelFirst ? "NCHW" : "NHWC") \(inputWidth)x\(inputHeight), dim \(outputDim))")
            return (itp, vocab)
        } catch {
            failed = "\(error)"
            logError("engine \(Self.modelId): FAILED — \(error) (creative analysis degrades to deterministic experts)")
            return nil
        }
    }

    func observe(bitmap: PlatformImage, completionHandler: @escaping (SemanticObservation?, Error?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let (interpreter, vocab) = self.loaded(),
                  let image = self.unwrap(bitmap),
                  let pixels = Self.rgbPixels(of: image, width: self.inputWidth, height: self.inputHeight) else {
                completionHandler(nil, nil)
                return
            }
            do {
                let count = self.inputWidth * self.inputHeight
                var floats = [Float](repeating: 0, count: count * 3)
                if self.channelFirst {
                    // NCHW: all R, then all G, then all B — raw/255, NO mean/std.
                    for i in 0..<count {
                        floats[i] = Float(pixels[i * 4]) / 255.0
                        floats[count + i] = Float(pixels[i * 4 + 1]) / 255.0
                        floats[2 * count + i] = Float(pixels[i * 4 + 2]) / 255.0
                    }
                } else {
                    for i in 0..<count {
                        floats[i * 3] = Float(pixels[i * 4]) / 255.0
                        floats[i * 3 + 1] = Float(pixels[i * 4 + 1]) / 255.0
                        floats[i * 3 + 2] = Float(pixels[i * 4 + 2]) / 255.0
                    }
                }
                let inputData = floats.withUnsafeBufferPointer { Data(buffer: $0) }
                try interpreter.copy(inputData, toInputAt: 0)
                try interpreter.invoke()
                let output = try interpreter.output(at: 0)
                var embedding = output.data.withUnsafeBytes { raw in
                    Array(raw.bindMemory(to: Float.self))
                }

                // L2 normalize (the model outputs a RAW embedding — Android parity).
                let norm = sqrt(embedding.reduce(Float(0)) { $0 + $1 * $1 })
                if norm > 0 { for i in embedding.indices { embedding[i] /= norm } }

                let kEmbedding = KotlinFloatArray(size: Int32(embedding.count))
                for (i, v) in embedding.enumerated() { kEmbedding.set(index: Int32(i), value: v) }

                let concepts = vocab.score(embedding: kEmbedding)
                completionHandler(SemanticObservation(
                    concepts: concepts, embedding: kEmbedding, modelId: Self.modelId), nil)
            } catch {
                logError("mobileclip inference failed: \(error) — returning nil")
                completionHandler(nil, nil)
            }
        }
    }

    private func unwrap(_ platformImage: PlatformImage) -> UIImage? {
        platformImage.handle as? UIImage
    }

    /// Resizes + draws the image into an RGBA8888 buffer (width×height×4). One draw, one buffer —
    /// the CLIP preprocessing path (bilinear resize matches Android's createScaledBitmap filter).
    static func rgbPixels(of image: UIImage, width: Int, height: Int) -> [UInt8]? {
        guard let cg = image.cgImage else { return nil }
        var buffer = [UInt8](repeating: 0, count: width * height * 4)
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        let ok = buffer.withUnsafeMutableBytes { ptr -> Bool in
            guard let ctx = CGContext(
                data: ptr.baseAddress, width: width, height: height,
                bitsPerComponent: 8, bytesPerRow: width * 4, space: colorSpace,
                bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue | CGBitmapInfo.byteOrder32Big.rawValue)
            else { return false }
            ctx.interpolationQuality = .medium
            ctx.draw(cg, in: CGRect(x: 0, y: 0, width: width, height: height))
            return true
        }
        return ok ? buffer : nil
    }
}
#else
/// Simulator smoke-build fallback. MediaPipe already statically bundles TensorFlow Lite, so the
/// standalone Swift wrapper is omitted there to avoid duplicate linker symbols. Semantic analysis
/// is optional by contract and degrades to the deterministic experts when this returns nil.
final class IosMobileClipEngine: SemanticSceneEngine {
    var capability: AiCapability {
        AiCapability(
            modelId: "mobileclip2_s0",
            status: .failed,
            error: "MobileCLIP is disabled in the x86_64 simulator smoke build")
    }

    func observe(
        bitmap: PlatformImage,
        completionHandler: @escaping (SemanticObservation?, Error?) -> Void
    ) {
        completionHandler(nil, nil)
    }
}
#endif
