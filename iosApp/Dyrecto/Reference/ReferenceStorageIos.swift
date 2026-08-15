import Foundation
import UIKit
import DyrectoShared

/// Local persistence for the `ReferenceSession` — the iOS port of `ReferenceRepository`.
///
/// Layout mirrors Android exactly (same file names, same JSON via the shared
/// `ReferenceProfileCodec`, so a storyboard round-trips identically across platforms):
///   Documents/shot_reference/session.json         — the session (profiles + options + completion)
///   Documents/shot_reference/embedding_<id>.json  — one versioned JSON per embedding
///   Documents/shot_reference/reference_<id>.jpg   — app-private copy of each reference image
///
/// Writes are atomic (.tmp + replace). Reads are defensive: malformed data reads as "nothing
/// stored" rather than crashing. There is deliberately NO legacy Phase 9 DataStore path — iOS
/// starts fresh on the session JSON format.
final class ReferenceStorageIos {

    private static let imageQuality: CGFloat = 0.90 // Android IMAGE_QUALITY = 90

    private let fileManager = FileManager.default
    private let log = DyrectoLog.shared

    /// App-private directory holding the copied reference images + JSON files.
    private var storageDir: URL {
        let documents = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let dir = documents.appendingPathComponent("shot_reference", isDirectory: true)
        try? fileManager.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    private var sessionFile: URL { storageDir.appendingPathComponent("session.json") }

    private func embeddingFile(for id: String) -> URL {
        storageDir.appendingPathComponent("embedding_\(id).json")
    }

    /// Destination file for the reference image copy of profile [id].
    func imageFile(for id: String) -> URL {
        storageDir.appendingPathComponent("reference_\(id).jpg")
    }

    /// Compresses [image] into the app-private image file for [id]; returns its URI string.
    func persistImage(_ image: UIImage, id: String) -> String? {
        guard let jpeg = image.jpegData(compressionQuality: Self.imageQuality) else { return nil }
        let file = imageFile(for: id)
        do {
            try jpeg.write(to: file, options: .atomic)
            return file.absoluteString
        } catch {
            log.line(.error, "reference image persist failed: \(error)")
            return nil
        }
    }

    // MARK: Session (JSON via the shared codec)

    func saveSession(_ session: ReferenceSession) {
        writeAtomically(to: sessionFile, content: ReferenceProfileCodec.shared.encodeSession(session: session))
    }

    /// Restores the stored session, or nil when nothing (valid) is stored.
    func loadSession() -> ReferenceSession? {
        guard let text = try? String(contentsOf: sessionFile, encoding: .utf8) else { return nil }
        return ReferenceProfileCodec.shared.decodeSession(text: text)
    }

    func saveEmbedding(_ embedding: ReferenceEmbedding) {
        writeAtomically(to: embeddingFile(for: embedding.id),
                        content: ReferenceProfileCodec.shared.encodeEmbedding(embedding: embedding))
    }

    func loadEmbedding(id: String) -> ReferenceEmbedding? {
        guard let text = try? String(contentsOf: embeddingFile(for: id), encoding: .utf8) else {
            return nil
        }
        return ReferenceProfileCodec.shared.decodeEmbedding(text: text)
    }

    /// Deletes the embedding file for [id] (removing/replacing a storyboard shot).
    func deleteEmbedding(id: String) {
        try? fileManager.removeItem(at: embeddingFile(for: id))
    }

    /// Deletes the copied reference image for shot [id].
    func deleteImage(id: String) {
        try? fileManager.removeItem(at: imageFile(for: id))
    }

    /// Removes the stored session, embeddings, and copied reference images.
    func clear() {
        guard let files = try? fileManager.contentsOfDirectory(
            at: storageDir, includingPropertiesForKeys: nil) else { return }
        for file in files {
            try? fileManager.removeItem(at: file)
        }
    }

    // MARK: Atomic writes (tmp + replace — the Android writeAtomically parity)

    private func writeAtomically(to target: URL, content: String) {
        let tmp = target.deletingLastPathComponent()
            .appendingPathComponent(target.lastPathComponent + ".tmp")
        do {
            try content.write(to: tmp, atomically: true, encoding: .utf8)
            if fileManager.fileExists(atPath: target.path) {
                _ = try fileManager.replaceItemAt(target, withItemAt: tmp)
            } else {
                try fileManager.moveItem(at: tmp, to: target)
            }
        } catch {
            log.line(.error, "reference persist failed (\(target.lastPathComponent)): \(error)")
            try? fileManager.removeItem(at: tmp)
        }
    }
}
