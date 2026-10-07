package com.example.shared.platform

import kotlinx.coroutines.flow.StateFlow

data class SensorTelemetryData(
    val latitude: Double = -15.6014,
    val longitude: Double = -56.0979,
    val altitude: Double = 165.0,
    val accuracy: Float = 3.0f,
    val speed: Float = 0f,
    val azimuthBearing: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val pressureHpa: Float = 1013.25f,
    val isGpsFixed: Boolean = false,
    val isMockLocation: Boolean = false,
    val distanceTraveledKm: Double = 0.0
)

expect class PlatformSensorManager {
    val telemetry: StateFlow<SensorTelemetryData>
    fun startListening()
    fun stopListening()
}
