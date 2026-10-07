package com.example.shared.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

actual class PlatformSensorManager {

    private val locationService = PlatformLocationService()
    private val motionManager = CMMotionManager()

    private val _telemetry = MutableStateFlow(SensorTelemetryData())
    actual val telemetry: StateFlow<SensorTelemetryData> = _telemetry.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            locationService.locationData.collect { loc ->
                _telemetry.value = _telemetry.value.copy(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitude,
                    accuracy = loc.accuracyMeters,
                    speed = loc.speedKmh,
                    isGpsFixed = loc.isGpsFixed,
                    isMockLocation = loc.isMockLocation,
                    distanceTraveledKm = loc.distanceTraveledMeters / 1000.0
                )
            }
        }
    }

    actual fun startListening() {
        locationService.startLocationUpdates()

        if (motionManager.isDeviceMotionAvailable()) {
            motionManager.deviceMotionUpdateInterval = 1.0 / 30.0
            motionManager.startDeviceMotionUpdatesToQueue(
                NSOperationQueue.mainQueue
            ) { motion, _ ->
                motion?.let {
                    val attitude = it.attitude
                    var azimuth = Math.toDegrees(attitude.yaw).toFloat()
                    if (azimuth < 0) azimuth += 360f

                    _telemetry.value = _telemetry.value.copy(
                        azimuthBearing = azimuth,
                        pitch = Math.toDegrees(attitude.pitch).toFloat(),
                        roll = Math.toDegrees(attitude.roll).toFloat()
                    )
                }
            }
        }

        // Barômetro via CMAltimeter
        if (platform.CoreMotion.CMAltimeter.isRelativeAltitudeAvailable()) {
            val altimeter = platform.CoreMotion.CMAltimeter()
            altimeter.startRelativeAltitudeUpdatesToQueue(
                NSOperationQueue.mainQueue
            ) { data, _ ->
                data?.let {
                    _telemetry.value = _telemetry.value.copy(
                        pressureHpa = (it.pressure.floatValue * 10f) // kPa → hPa
                    )
                }
            }
        }
    }

    actual fun stopListening() {
        locationService.stopLocationUpdates()
        if (motionManager.isDeviceMotionActive()) {
            motionManager.stopDeviceMotionUpdates()
        }
    }
}
