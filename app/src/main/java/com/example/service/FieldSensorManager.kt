package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SensorTelemetry(
    val latitude: Double = -15.6014, // Ponto de referência Cuiabá - MT
    val longitude: Double = -56.0979,
    val altitude: Double = 165.0,
    val accuracy: Float = 3.0f,
    val speed: Float = 0f,
    val azimuthBearing: Float = 0f, // 0-360 degrees
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val pressureHpa: Float = 1013.25f,
    val isGpsFixed: Boolean = false,
    val isMockLocation: Boolean = false,
    val distanceTraveledKm: Double = 0.0
)

class FieldSensorManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : SensorEventListener {

    val fusedLocationService = FusedLocationService(context)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _telemetry = MutableStateFlow(SensorTelemetry())
    val telemetry: StateFlow<SensorTelemetry> = _telemetry.asStateFlow()

    private var rotationSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

        // Observa as coordenadas GPS emitidas pelo FusedLocationProviderClient em tempo real
        scope.launch {
            fusedLocationService.locationData.collect { loc ->
                _telemetry.value = _telemetry.value.copy(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    altitude = loc.altitude,
                    accuracy = loc.accuracyMetros,
                    speed = loc.speedKmh,
                    isGpsFixed = loc.isGpsFixed,
                    isMockLocation = loc.isMockLocation,
                    distanceTraveledKm = loc.distanceTraveledMeters / 1000.0
                )
            }
        }
    }

    fun startListening() {
        // Inicia GPS em tempo real de alta precisão
        fusedLocationService.startLocationUpdates(
            intervalMs = 1500L,
            minUpdateIntervalMs = 800L,
            minDisplacementMeters = 0.5f
        )

        // Inicia sensores inerciais de bússola e orientação
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        pressureSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        fusedLocationService.stopLocationUpdates()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                val pitchDeg = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val rollDeg = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                _telemetry.value = _telemetry.value.copy(
                    azimuthBearing = azimuthDeg,
                    pitch = pitchDeg,
                    roll = rollDeg
                )
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                _telemetry.value = _telemetry.value.copy(pressureHpa = pressure)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
