package com.example.shared.platform

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

actual class PlatformSensorManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : SensorEventListener {

    private val locationService = PlatformLocationService(context)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _telemetry = MutableStateFlow(SensorTelemetryData())
    actual val telemetry: StateFlow<SensorTelemetryData> = _telemetry.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

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
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    actual fun stopListening() {
        locationService.stopLocationUpdates()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                _telemetry.value = _telemetry.value.copy(
                    azimuthBearing = azimuth,
                    pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
                    roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                )
            }
            Sensor.TYPE_PRESSURE -> {
                _telemetry.value = _telemetry.value.copy(pressureHpa = event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
