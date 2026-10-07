import Foundation
import CoreLocation
import Combine

class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let clManager = CLLocationManager()

    @Published var latitude: Double = -15.6014
    @Published var longitude: Double = -56.0979
    @Published var altitude: Double = 165.0
    @Published var accuracy: Double = 3.0
    @Published var speedKmh: Double = 0.0
    @Published var course: Double = 0.0
    @Published var isTracking: Bool = false
    @Published var isGpsFixed: Bool = false

    private var previousLocation: CLLocation?
    private(set) var totalDistanceMeters: Double = 0.0

    override init() {
        super.init()
        clManager.delegate = self
        clManager.desiredAccuracy = kCLLocationAccuracyBest
        clManager.distanceFilter = 0.5
        clManager.allowsBackgroundLocationUpdates = true
        clManager.pausesLocationUpdatesAutomatically = false
    }

    func startTracking() {
        clManager.requestWhenInUseAuthorization()
        clManager.startUpdatingLocation()
        isTracking = true
    }

    func stopTracking() {
        clManager.stopUpdatingLocation()
        isTracking = false
    }

    func requestPrecisionFix() {
        clManager.desiredAccuracy = kCLLocationAccuracyBest
        clManager.requestLocation()
    }

    func resetDistance() {
        totalDistanceMeters = 0.0
        previousLocation = nil
    }

    // MARK: - CLLocationManagerDelegate

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }

        // Distance tracking with GPS noise filter
        if let prev = previousLocation {
            let dist = prev.distance(from: location)
            if dist > 1.5 && dist < 500.0 {
                totalDistanceMeters += dist
            }
        }
        previousLocation = location

        latitude = location.coordinate.latitude
        longitude = location.coordinate.longitude
        altitude = location.altitude
        accuracy = location.horizontalAccuracy
        speedKmh = location.speed >= 0 ? location.speed * 3.6 : 0
        course = location.course >= 0 ? location.course : 0
        isGpsFixed = location.horizontalAccuracy >= 0
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        isGpsFixed = false
        print("CoreLocation error: \(error.localizedDescription)")
    }
}
