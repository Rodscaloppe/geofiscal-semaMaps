package com.example.shared.platform

import kotlinx.coroutines.flow.StateFlow

data class LocationData(
    val latitude: Double = -15.6014,
    val longitude: Double = -56.0979,
    val altitude: Double = 165.0,
    val accuracyMeters: Float = 3.0f,
    val speedKmh: Float = 0f,
    val bearingDegrees: Float = 0f,
    val timestamp: Long = 0L,
    val isGpsFixed: Boolean = false,
    val isMockLocation: Boolean = false,
    val provider: String = "unknown",
    val distanceTraveledMeters: Double = 0.0,
    val fixAgeMs: Long = 0L
)

expect class PlatformLocationService {
    val locationData: StateFlow<LocationData>
    val isTracking: StateFlow<Boolean>

    fun startLocationUpdates(
        intervalMs: Long = 1500L,
        minDisplacementMeters: Float = 0.5f
    )

    fun stopLocationUpdates()

    suspend fun getCurrentPrecisionFix(): LocationData?

    fun resetDistanceCounter()
}
