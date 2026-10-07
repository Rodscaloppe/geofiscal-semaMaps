package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
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

data class GpsLocationData(
    val latitude: Double = -15.6014, // Ponto de referência Cuiabá - MT
    val longitude: Double = -56.0979,
    val altitude: Double = 165.0,
    val accuracyMetros: Float = 3.0f,
    val speedKmh: Float = 0f,
    val bearingGraus: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isGpsFixed: Boolean = false,
    val isMockLocation: Boolean = false, // Detecção anti-fraude para perícia ambiental
    val satellitesProvider: String = "FUSED_HIGH_ACCURACY",
    val distanceTraveledMeters: Double = 0.0,
    val fixAgeMs: Long = 0L
)

class FusedLocationService(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _locationData = MutableStateFlow(GpsLocationData())
    val locationData: StateFlow<GpsLocationData> = _locationData.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var previousLocation: Location? = null
    private var totalDistanceTraveled: Double = 0.0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            processNewLocation(location)
        }
    }

    /**
     * Inicia o rastreamento em tempo real de alta precisão via FusedLocationProviderClient.
     * Intervalo de 1 a 2 segundos com deslocamento mínimo de 0.5 metros.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        intervalMs: Long = 1500L,
        minUpdateIntervalMs: Long = 800L,
        minDisplacementMeters: Float = 0.5f
    ) {
        if (_isTracking.value) return

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
                .setMinUpdateIntervalMillis(minUpdateIntervalMs)
                .setMinUpdateDistanceMeters(minDisplacementMeters)
                .setWaitForAccurateLocation(true)
                .setMaxUpdateDelayMillis(intervalMs * 2)
                .build()

            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // Obter a última localização conhecida imediatamente para reduzir latência inicial
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    processNewLocation(loc)
                }
            }

            _isTracking.value = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Interrompe o recebimento de atualizações contínuas de localização.
     */
    fun stopLocationUpdates() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
            _isTracking.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Obtém uma medição pontual de altíssima precisão no instante da captura da foto,
     * utilizando o método getCurrentLocation() com token de cancelamento.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentPrecisionFix(timeoutMs: Long = 5000L): Location? {
        val cts = CancellationTokenSource()
        return try {
            val location = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            if (location != null) {
                processNewLocation(location)
            }
            location
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun processNewLocation(location: Location) {
        val now = System.currentTimeMillis()

        // Verificação anti-fraude pericial (detectar se coordenadas vêm de aplicativo simulador de GPS)
        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }

        // Cálculo de distância percorrida acumulada
        previousLocation?.let { prev ->
            val dist = prev.distanceTo(location).toDouble()
            // Filtrar ruído de GPS quando parado (ex: jitter de sinal)
            if (dist > 1.5 && dist < 500.0) {
                totalDistanceTraveled += dist
            }
        }
        previousLocation = location

        val speedKmh = if (location.hasSpeed()) (location.speed * 3.6f) else 0f
        val fixAge = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000L
        } else {
            now - location.time
        }

        _locationData.value = GpsLocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else _locationData.value.altitude,
            accuracyMetros = if (location.hasAccuracy()) location.accuracy else 5.0f,
            speedKmh = speedKmh,
            bearingGraus = if (location.hasBearing()) location.bearing else _locationData.value.bearingGraus,
            timestamp = location.time.takeIf { it > 0 } ?: now,
            isGpsFixed = true,
            isMockLocation = isMock,
            satellitesProvider = location.provider ?: "fused",
            distanceTraveledMeters = totalDistanceTraveled,
            fixAgeMs = fixAge.coerceAtLeast(0L)
        )
    }

    fun resetDistanceCounter() {
        totalDistanceTraveled = 0.0
        _locationData.value = _locationData.value.copy(distanceTraveledMeters = 0.0)
    }
}
