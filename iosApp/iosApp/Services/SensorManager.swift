import Foundation
import CoreMotion
import Combine

class SensorTelemetryManager: ObservableObject {
    private let motionManager = CMMotionManager()
    private let altimeter = CMAltimeter()

    @Published var azimuth: Double = 0.0
    @Published var pitch: Double = 0.0
    @Published var roll: Double = 0.0
    @Published var pressureHpa: Double = 1013.25

    func startListening() {
        // Device Motion (gyro + accelerometer + magnetometer fusion)
        if motionManager.isDeviceMotionAvailable {
            motionManager.deviceMotionUpdateInterval = 1.0 / 30.0
            motionManager.startDeviceMotionUpdates(
                using: .xMagneticNorthZVertical,
                to: .main
            ) { [weak self] motion, _ in
                guard let motion = motion else { return }
                var heading = motion.attitude.yaw * 180.0 / .pi
                if heading < 0 { heading += 360 }
                self?.azimuth = heading
                self?.pitch = motion.attitude.pitch * 180.0 / .pi
                self?.roll = motion.attitude.roll * 180.0 / .pi
            }
        }

        // Barometric altimeter
        if CMAltimeter.isRelativeAltitudeAvailable() {
            altimeter.startRelativeAltitudeUpdates(to: .main) { [weak self] data, _ in
                guard let data = data else { return }
                // pressure is in kPa, convert to hPa
                self?.pressureHpa = data.pressure.doubleValue * 10.0
            }
        }
    }

    func stopListening() {
        if motionManager.isDeviceMotionActive {
            motionManager.stopDeviceMotionUpdates()
        }
        altimeter.stopRelativeAltitudeUpdates()
    }
}
