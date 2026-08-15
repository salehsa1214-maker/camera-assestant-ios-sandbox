import Foundation
import NetworkExtension
import SystemConfiguration.CaptiveNetwork

/// iOS port of the Android `WifiProvisioner`: joins the camera's AP and reports the camera IP.
///
/// Parity notes:
///  - Android uses WifiNetworkSpecifier + bindProcessToNetwork; iOS uses NEHotspotConfiguration
///    (requires the HotspotConfiguration entitlement). There is no process-wide bind on iOS —
///    instead every socket the app opens is pinned to Wi-Fi via
///    `NWParameters.requiredInterfaceType = .wifi` (see PtpIpClient/SsdpDiscoverer).
///  - Camera IP = gateway of the joined interface; Android reads the default route. iOS has no
///    public route-table API, so we derive it from the phone's own DHCP address (camera AP
///    hands out x.y.z.n with the camera at x.y.z.1) and fall back to the documented default
///    192.168.122.1. Hardware validation point: confirm the FX3 gateway derivation.
final class WifiJoiner {

    static let defaultCameraIp = "192.168.122.1"
    private let log = DyrectoLog.shared

    struct JoinResult {
        let cameraIp: String
        let phoneIp: String
        let interfaceName: String
    }

    func join(ssid: String, password: String,
              timeout: TimeInterval = 30,
              completion: @escaping (Result<JoinResult, Error>) -> Void) {
        log.line(.conn, "wifi join \(ssid)")
        let config = NEHotspotConfiguration(ssid: ssid, passphrase: password, isWEP: false)
        config.joinOnce = false
        config.lifeTimeInDays = 1

        NEHotspotConfigurationManager.shared.apply(config) { [weak self] error in
            guard let self else { return }
            if let error = error as NSError? {
                // "already associated" is success.
                if error.domain == NEHotspotConfigurationErrorDomain,
                   error.code == NEHotspotConfigurationError.alreadyAssociated.rawValue {
                    self.log.line(.conn, "wifi: already associated")
                } else {
                    self.log.line(.error, "wifi join failed: \(error.localizedDescription)")
                    completion(.failure(error))
                    return
                }
            }
            // DHCP settle: poll for a Wi-Fi IPv4 up to the timeout (Android waits for
            // onLinkPropertiesChanged; iOS association completes before DHCP finishes).
            self.awaitWifiAddress(deadline: Date().addingTimeInterval(timeout), completion: completion)
        }
    }

    func removeConfiguration(ssid: String) {
        NEHotspotConfigurationManager.shared.removeConfiguration(forSSID: ssid)
    }

    private func awaitWifiAddress(deadline: Date,
                                  completion: @escaping (Result<JoinResult, Error>) -> Void) {
        if let (name, ip) = Self.wifiIPv4() {
            let cameraIp = Self.gatewayGuess(fromPhoneIp: ip) ?? Self.defaultCameraIp
            log.line(.conn, "wifi up: if=\(name) phone=\(ip) camera=\(cameraIp)")
            completion(.success(JoinResult(cameraIp: cameraIp, phoneIp: ip, interfaceName: name)))
            return
        }
        guard Date() < deadline else {
            completion(.failure(NSError(domain: "app.dyrecto", code: 1, userInfo: [
                NSLocalizedDescriptionKey: "Wi-Fi joined but no IPv4 lease within timeout",
            ])))
            return
        }
        DispatchQueue.global().asyncAfter(deadline: .now() + 0.5) {
            self.awaitWifiAddress(deadline: deadline, completion: completion)
        }
    }

    /// The phone's IPv4 on the Wi-Fi interface (en0), via getifaddrs.
    static func wifiIPv4() -> (interface: String, ip: String)? {
        var ifaddr: UnsafeMutablePointer<ifaddrs>?
        guard getifaddrs(&ifaddr) == 0, let first = ifaddr else { return nil }
        defer { freeifaddrs(ifaddr) }
        var ptr: UnsafeMutablePointer<ifaddrs>? = first
        while let p = ptr {
            defer { ptr = p.pointee.ifa_next }
            let name = String(cString: p.pointee.ifa_name)
            guard name == "en0",
                  let sa = p.pointee.ifa_addr,
                  sa.pointee.sa_family == UInt8(AF_INET) else { continue }
            var addr = sa.withMemoryRebound(to: sockaddr_in.self, capacity: 1) { $0.pointee.sin_addr }
            var buf = [CChar](repeating: 0, count: Int(INET_ADDRSTRLEN))
            inet_ntop(AF_INET, &addr, &buf, socklen_t(INET_ADDRSTRLEN))
            let ip = String(cString: buf)
            if !ip.isEmpty && ip != "0.0.0.0" { return (name, ip) }
        }
        return nil
    }

    /// Camera-AP gateway heuristic: same /24 with host .1 (the FX3 AP is 192.168.122.1/24 and
    /// hands the phone 192.168.122.x). Returns nil for non-/24-shaped addresses.
    static func gatewayGuess(fromPhoneIp ip: String) -> String? {
        let parts = ip.split(separator: ".")
        guard parts.count == 4 else { return nil }
        return "\(parts[0]).\(parts[1]).\(parts[2]).1"
    }
}
