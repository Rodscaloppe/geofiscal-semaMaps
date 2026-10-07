package com.example.shared.platform

import kotlinx.coroutines.flow.StateFlow

data class CaptureResult(
    val imageBytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int = 0
)

data class CameraStatus(
    val isInitialized: Boolean = false,
    val isTakingPicture: Boolean = false,
    val isFrontCamera: Boolean = false,
    val hasFlash: Boolean = false,
    val isFlashOn: Boolean = false,
    val errorMessage: String? = null
)

expect class PlatformCameraService {
    val cameraStatus: StateFlow<CameraStatus>

    suspend fun capturePhoto(): CaptureResult

    fun toggleCamera()

    fun toggleFlash()

    fun release()
}
