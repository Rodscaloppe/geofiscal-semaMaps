package com.example.shared.platform

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformCameraService(private val context: Context) {

    private val _cameraStatus = MutableStateFlow(CameraStatus())
    actual val cameraStatus: StateFlow<CameraStatus> = _cameraStatus.asStateFlow()

    actual suspend fun capturePhoto(): CaptureResult {
        throw UnsupportedOperationException(
            "Use CameraCaptureService from the app module for Android camera capture"
        )
    }

    actual fun toggleCamera() {
        _cameraStatus.value = _cameraStatus.value.copy(
            isFrontCamera = !_cameraStatus.value.isFrontCamera
        )
    }

    actual fun toggleFlash() {
        _cameraStatus.value = _cameraStatus.value.copy(
            isFlashOn = !_cameraStatus.value.isFlashOn
        )
    }

    actual fun release() {
        _cameraStatus.value = CameraStatus()
    }
}
