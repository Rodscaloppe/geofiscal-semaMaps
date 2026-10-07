package com.example.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSObject
import platform.darwin.NSObject as DarwinNSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
        previousLat?.let { pLat ->
            previousLng?.let { pLng ->
                val dist = com.example.shared.geo.GeoCalculations.haversineDistanceMeters(
                    pLat, pLng, location.coordinate.latitude, location.coordinate.longitude
                )
                if (dist in 1.5..500.0) totalDistance += dist
            }
        }
        previousLat = location.coordinate.latitude
        previousLng = location.coordinate.longitude

        val speedKmh = if (location.speed >= 0) (location.speed * 3.6).toFloat() else 0f
        val bearing = if (location.course >= 0) location.course.toFloat() else 0f

        _locationData.value = LocationData(
            latitude = location.coordinate.latitude,
            longitude = location.coordinate.longitude,
            altitude = location.altitude,
            accuracyMeters = location.horizontalAccuracy.toFloat(),
            speedKmh = speedKmh,
            bearingDegrees = bearing,
            timestamp = (location.timestamp.timeIntervalSince1970 * 1000).toLong(),
            isGpsFixed = location.horizontalAccuracy >= 0,
            isMockLocation = false,
            provider = "CoreLocation",
            distanceTraveledMeters = totalDistance,
            fixAgeMs = 0L
        )
    }
}
