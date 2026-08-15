import Foundation
import Network

/// SSDP UUID resolution — the credential-mint gate before the CC17 re-read
/// (docs/wire-protocol.md §2.2). Mirrors the Android `SsdpDiscoverer` behavior:
/// cycle the three search targets, each probe sent to the multicast group AND unicast to the
/// camera IP, drain ~1 s per probe, 8 s overall; success = first `uuid:` match.
///
/// iOS note: the multicast leg needs the `com.apple.developer.networking.multicast`
/// entitlement. Without it the NWConnectionGroup fails — we log and continue with the unicast
/// probes only, which the camera also answers (it is a single known host).
final class SsdpDiscoverer {

    private let log = DyrectoLog.shared
    private let queue = DispatchQueue(label: "app.dyrecto.ssdp")

    private static let multicastHost = NWEndpoint.Host("239.255.255.250")
    private static let port = NWEndpoint.Port(rawValue: 1900)!
    private static let searchTargets = [
        "urn:schemas-sony-com:service:ScalarWebAPI:1",
        "urn:schemas-upnp-org:device:DigitalImagingDevice:1",
        "ssdp:all",
    ]
    private static let overallTimeout: TimeInterval = 8.0

    private var finished = false

    func discover(cameraIp: String, completion: @escaping (Result<String, Error>) -> Void) {
        finished = false
        let deadline = Date().addingTimeInterval(Self.overallTimeout)
        log.line(.info, "SSDP: resolving camera UUID (gate for CC17)")

        let finish: (Result<String, Error>) -> Void = { [weak self] result in
            guard let self, !self.finished else { return }
            self.finished = true
            completion(result)
        }

        // Multicast group (entitlement-gated).
        var group: NWConnectionGroup?
        if let multicast = try? NWMulticastGroup(for: [.hostPort(host: Self.multicastHost, port: Self.port)]) {
            let params = NWParameters.udp
            params.requiredInterfaceType = .wifi
            let g = NWConnectionGroup(with: multicast, using: params)
            g.setReceiveHandler(maximumMessageSize: 4096, rejectOversizedMessages: true) { _, content, _ in
                if let content, let uuid = Self.extractUuid(from: content) {
                    self.log.line(.info, "SSDP: uuid \(uuid) (multicast)")
                    finish(.success(uuid))
                }
            }
            g.stateUpdateHandler = { state in
                if case .failed(let e) = state {
                    self.log.line(.info, "SSDP multicast unavailable (\(e)) — unicast probes only")
                }
            }
            g.start(queue: queue)
            group = g
        } else {
            log.line(.info, "SSDP: multicast group creation failed — unicast probes only")
        }

        // Unicast listener connection to the camera.
        let unicastParams = NWParameters.udp
        unicastParams.requiredInterfaceType = .wifi
        let unicast = NWConnection(host: NWEndpoint.Host(cameraIp), port: Self.port, using: unicastParams)
        func receiveLoop() {
            unicast.receiveMessage { content, _, _, error in
                if let content, let uuid = Self.extractUuid(from: content) {
                    self.log.line(.info, "SSDP: uuid \(uuid) (unicast)")
                    finish(.success(uuid))
                    return
                }
                if error == nil, !self.finished { receiveLoop() }
            }
        }
        unicast.stateUpdateHandler = { state in
            if case .ready = state { receiveLoop() }
        }
        unicast.start(queue: queue)

        // Probe loop: one ST per iteration, sent both ways, ~1 s apart, until deadline.
        var targetIndex = 0
        func probe() {
            guard !finished else { cleanup(); return }
            guard Date() < deadline else {
                cleanup()
                finish(.failure(NSError(domain: "app.dyrecto", code: 2, userInfo: [
                    NSLocalizedDescriptionKey: "SSDP: no uuid within \(Int(Self.overallTimeout)) s",
                ])))
                return
            }
            let st = Self.searchTargets[targetIndex % Self.searchTargets.count]
            targetIndex += 1
            let request = Data((
                "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 1\r\n" +
                "ST: \(st)\r\n\r\n"
            ).utf8)
            group?.send(content: request) { _ in }
            unicast.send(content: request, completion: .contentProcessed { _ in })
            self.log.line(.info, "SSDP: M-SEARCH ST=\(st)")
            self.queue.asyncAfter(deadline: .now() + 1.0, execute: probe)
        }
        func cleanup() {
            group?.cancel()
            unicast.cancel()
        }
        queue.async(execute: probe)

        // Hard deadline safety.
        queue.asyncAfter(deadline: .now() + Self.overallTimeout + 0.5) {
            if !self.finished { cleanup() }
        }
    }

    static func extractUuid(from data: Data) -> String? {
        guard let text = String(data: data, encoding: .utf8) else { return nil }
        guard let range = text.range(of: "uuid:([0-9a-fA-F-]{8,})",
                                     options: [.regularExpression, .caseInsensitive]) else { return nil }
        return String(text[range].dropFirst("uuid:".count))
    }
}
