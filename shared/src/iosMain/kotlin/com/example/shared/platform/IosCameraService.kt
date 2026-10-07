package com.example.shared.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVMediaTypeVideo
import platform.Foundation.NSData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PlatformCameraService {

    private var captureSession: AVCaptureSession? = null
    private var photoOutput: AVCapturePhotoOutput? = null
    private var currentDevice: AVCaptureDevice? = null

    private val _cameraStatus = MutableStateFlow(CameraStatus())
    actual val cameraStatus: StateFlow<CameraStatus> = _cameraStatus.asStateFlow()

    fun initializeSession(useFrontCamera: Boolean = false) {
        val session = AVCaptureSession()
        session.sessionPreset = AVCaptureSessionPresetPhoto

        val position = if (useFrontCamera) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: run {
            _cameraStatus.value = _cameraStatus.value.copy(
                errorMessage = "Câmera não disponível neste dispositivo"
            )
            return
        }
        currentDevice = device

        val input = try {
            AVCaptureDeviceInput.deviceInputWithDevice(device, null) ?: return
        } catch (_: Exception) { return }

        if (session.canAddInput(input)) {
            session.addInput(input)
        }

        val output = AVCapturePhotoOutput()
        if (session.canAddOutput(output)) {
            session.addOutput(output)
            photoOutput = output
        }

        captureSession = session
        session.startRunning()

        _cameraStatus.value = CameraStatus(
            isInitialized = true,
            isFrontCamera = useFrontCamera,
            hasFlash = device.hasFlash,
            isFlashOn = false
        )
    }

    actual suspend fun capturePhoto(): CaptureResult {
        val output = photoOutput ?: throw IllegalStateException("AVCapturePhotoOutput não inicializado")

        _cameraStatus.value = _cameraStatus.value.copy(isTakingPicture = true)

        // The actual photo capture on iOS needs the AVCapturePhotoCaptureDelegate
        // which is implemented in the Swift layer (iosApp) for proper UIImage handling
        return suspendCoroutine { continuation ->
            _cameraStatus.value = _cameraStatus.value.copy(isTakingPicture = false)
            continuation.resumeWithException(
                UnsupportedOperationException(
                    "Photo capture must be called from Swift via AVCapturePhotoCaptureDelegate"
                )
            )
        }
    }

    actual fun toggleCamera() {
        val isFront = !_cameraStatus.value.isFrontCamera
        captureSession?.stopRunning()
        initializeSession(useFrontCamera = isFront)
    }

    actual fun toggleFlash() {
        val device = currentDevice ?: return
        if (!device.hasFlash) return

        val nextState = !_cameraStatus.value.isFlashOn
        _cameraStatus.value = _cameraStatus.value.copy(isFlashOn = nextState)
    }

    actual fun release() {
        captureSession?.stopRunning()
        captureSession = null
        photoOutput = null
        currentDevice = null
        _cameraStatus.value = CameraStatus()
    }
}
