package com.example.shared.platform

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

actual class PlatformLocationService(context: Context) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _locationData = MutableStateFlow(LocationData())
    actual val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    actual val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var previousLat: Double? = null
    private var previousLng: Double? = null
    private var totalDistance: Double = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            processLocation(location)
        }
    }

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdates(intervalMs: Long, minDisplacementMeters: Float) {
        if (_isTracking.value) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(minDisplacementMeters)
            .setWaitForAccurateLocation(true)
            .build()

        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) processLocation(loc)
        }
        _isTracking.value = true
    }

    actual fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
        _isTracking.value = false
    }

    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentPrecisionFix(): LocationData? {
        val cts = CancellationTokenSource()
        return try {
            val loc = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            if (loc != null) {
                processLocation(loc)
                _locationData.value
            } else null
        } catch (_: Exception) { null }
    }

    actual fun resetDistanceCounter() {
        totalDistance = 0.0
        _locationData.value = _locationData.value.copy(distanceTraveledMeters = 0.0)
    }

    private fun processLocation(location: android.location.Location) {
        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION") location.isFromMockProvider
        }

        previousLat?.let { pLat ->
            previousLng?.let { pLng ->
                val dist = com.example.shared.geo.GeoCalculations.haversineDistanceMeters(
                    pLat, pLng, location.latitude, location.longitude
                )
                if (dist in 1.5..500.0) totalDistance += dist
            }
        }
        previousLat = location.latitude
        previousLng = location.longitude

        val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
        val fixAge = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000L
        } else {
            System.currentTimeMillis() - location.time
        }

        _locationData.value = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else _locationData.value.altitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 5.0f,
            speedKmh = speedKmh,
            bearingDegrees = if (location.hasBearing()) location.bearing else 0f,
            timestamp = location.time.takeIf { it > 0 } ?: System.currentTimeMillis(),
            isGpsFixed = true,
            isMockLocation = isMock,
            provider = location.provider ?: "fused",
            distanceTraveledMeters = totalDistance,
            fixAgeMs = fixAge.coerceAtLeast(0L)
        )
    }
}
