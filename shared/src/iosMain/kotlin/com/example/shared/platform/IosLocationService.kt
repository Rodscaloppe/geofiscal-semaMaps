@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.shared.platform

import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

actual class PlatformLocationService {

    private val locationManager = CLLocationManager()

    private val _locationData = MutableStateFlow(LocationData())
    actual val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    actual val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var previousLat: Double? = null
    private var previousLng: Double? = null
    private var totalDistance: Double = 0.0

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>
        ) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
            processLocation(location)
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: platform.Foundation.NSError
        ) {
            _locationData.value = _locationData.value.copy(isGpsFixed = false)
        }
    }

    init {
        locationManager.delegate = delegate
    }

    actual fun startLocationUpdates(intervalMs: Long, minDisplacementMeters: Float) {
        if (_isTracking.value) return

        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = minDisplacementMeters.toDouble()
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false

        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()

        _isTracking.value = true
    }

    actual fun stopLocationUpdates() {
        locationManager.stopUpdatingLocation()
        _isTracking.value = false
    }

    actual suspend fun getCurrentPrecisionFix(): LocationData? {
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestLocation()
        return _locationData.value.takeIf { it.isGpsFixed }
    }

    actual fun resetDistanceCounter() {
        totalDistance = 0.0
        _locationData.value = _locationData.value.copy(distanceTraveledMeters = 0.0)
    }

    private fun processLocation(location: CLLocation) {
        val (lat, lng) = location.coordinate.useContents { latitude to longitude }

        previousLat?.let { pLat ->
            previousLng?.let { pLng ->
                val dist = com.example.shared.geo.GeoCalculations.haversineDistanceMeters(
                    pLat, pLng, lat, lng
                )
                if (dist in 1.5..500.0) totalDistance += dist
            }
        }
        previousLat = lat
        previousLng = lng

        val speedKmh = if (location.speed >= 0) (location.speed * 3.6).toFloat() else 0f
        val bearing = if (location.course >= 0) location.course.toFloat() else 0f
        val timestampMs = try {
            (location.timestamp.timeIntervalSince1970 * 1000.0).toLong()
        } catch (_: Exception) {
            0L
        }

        _locationData.value = LocationData(
            latitude = lat,
            longitude = lng,
            altitude = location.altitude,
            accuracyMeters = location.horizontalAccuracy.toFloat(),
            speedKmh = speedKmh,
            bearingDegrees = bearing,
            timestamp = timestampMs,
            isGpsFixed = location.horizontalAccuracy >= 0,
            isMockLocation = false,
            provider = "CoreLocation",
            distanceTraveledMeters = totalDistance,
            fixAgeMs = 0L
        )
    }
}
