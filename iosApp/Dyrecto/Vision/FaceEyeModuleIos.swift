import Foundation
import UIKit
import Vision
import DyrectoShared

/// Vision-framework port of the Android `FaceAndEyeDetectionModule` (ML Kit): runs ONE face
/// landmark pass per analyzed frame and produces two independent results — the shared
/// `FaceDetectionResult` and `EyeDetectionResult` — exactly like the Android module (a single
/// inference feeding both VisionContext slots).
///
/// Coordinate conversion: Vision returns NORMALIZED boxes/landmarks with a BOTTOM-LEFT origin;
/// the shared results expect TOP-LEFT pixel space of the analyzed image — every rect/point is
/// flipped and scaled here at the module boundary (the same place ML Kit rects were adapted on
/// Android).
///
/// Confidence: Android's ML Kit FAST mode exposes no per-face score and uses a conservative
/// 0.8 constant; the same constant is used here for cross-platform threshold parity.
final class FaceEyeModule: VisionModuleIos {

    let id = "face-and-eye"

    private static let confidence: Float = 0.8 // Android CONFIDENCE parity
    private static let eyeRadius = 10          // Android EYE_RADIUS parity (px)

    func analyze(_ request: FrameAnalysisRequestIos) throws -> [DyrectoShared.VisionResult] {
        let startMs = SharedFactory.nowMs()
        let (faceResult, eyeResult) = try Self.detect(
            cgImage: request.cgImage,
            width: request.width,
            height: request.height,
            moduleId: id,
            startMs: startMs)
        return [faceResult, eyeResult]
    }

    /// One synchronous landmark pass, shared with `ReferenceAnalyzerIos` (which needs a fresh
    /// face pass over the picked image without touching the live module instance).
    static func detect(
        cgImage: CGImage,
        width: Int,
        height: Int,
        moduleId: String,
        startMs: Int64
    ) throws -> (FaceDetectionResult, EyeDetectionResult) {
        let request = VNDetectFaceLandmarksRequest()
        let handler = VNImageRequestHandler(cgImage: cgImage, orientation: .up, options: [:])
        try handler.perform([request])
        let observations = request.results ?? []

        let w = CGFloat(width)
        let h = CGFloat(height)

        // ---- Face result ----
        var faceBoxes: [FaceBox] = []
        for face in observations {
            // Normalized bottom-left box → top-left pixel rect.
            let bb = face.boundingBox
            let left = Int32((bb.minX * w).rounded())
            let right = Int32((bb.maxX * w).rounded())
            let top = Int32(((1.0 - bb.maxY) * h).rounded())
            let bottom = Int32(((1.0 - bb.minY) * h).rounded())
            faceBoxes.append(FaceBox(
                rect: PixelRect(left: left, top: top, right: right, bottom: bottom),
                confidence: confidence))
        }
        let faceResult = FaceDetectionResult(
            moduleId: moduleId,
            facesDetected: Int32(observations.count),
            boundingBoxes: faceBoxes,
            averageConfidence: faceBoxes.isEmpty ? 0 : confidence,
            analysisTimeMs: SharedFactory.nowMs() - startMs,
            sourceWidth: Int32(width),
            sourceHeight: Int32(height))

        // ---- Eye result — leftEye + rightEye landmark centers, radius-10 rect around each ----
        var eyeBoxes: [EyeBox] = []
        for face in observations {
            guard let landmarks = face.landmarks else { continue }
            for region in [landmarks.leftEye, landmarks.rightEye] {
                guard let region, region.pointCount > 0 else { continue }
                // pointsInImage returns bottom-left-origin IMAGE coordinates; average the eye
                // contour to its center, then flip y into top-left pixel space.
                let points = region.pointsInImage(imageSize: CGSize(width: w, height: h))
                var sumX: CGFloat = 0
                var sumY: CGFloat = 0
                for p in points { sumX += p.x; sumY += p.y }
                let cx = Int(( sumX / CGFloat(points.count)).rounded())
                let cy = Int((h - sumY / CGFloat(points.count)).rounded())
                eyeBoxes.append(EyeBox(
                    rect: PixelRect(
                        left: Int32(cx - eyeRadius), top: Int32(cy - eyeRadius),
                        right: Int32(cx + eyeRadius), bottom: Int32(cy + eyeRadius)),
                    confidence: confidence))
            }
        }
        let eyeResult = EyeDetectionResult(
            moduleId: moduleId,
            eyesDetected: Int32(eyeBoxes.count),
            boundingBoxes: eyeBoxes,
            averageConfidence: eyeBoxes.isEmpty ? 0 : confidence,
            analysisTimeMs: SharedFactory.nowMs() - startMs)

        return (faceResult, eyeResult)
    }
}
