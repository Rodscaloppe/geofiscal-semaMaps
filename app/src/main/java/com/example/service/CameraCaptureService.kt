package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CameraState(
    val isInitialized: Boolean = false,
    val isTakingPicture: Boolean = false,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val hasTorch: Boolean = false,
    val isTorchOn: Boolean = false,
    val errorMessage: String? = null
)

class CameraCaptureService(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    /**
     * Inicializa e vincula o ciclo de vida do CameraX à PreviewView do Compose.
     */
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = _cameraState.value.lensFacing
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val provider = cameraProvider ?: return@addListener

                provider.unbindAll()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(_cameraState.value.flashMode)
                    .build()

                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                val hasTorch = camera?.cameraInfo?.hasFlashUnit() == true
                _cameraState.value = _cameraState.value.copy(
                    isInitialized = true,
                    lensFacing = lensFacing,
                    hasTorch = hasTorch,
                    errorMessage = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _cameraState.value = _cameraState.value.copy(
                    isInitialized = false,
                    errorMessage = "Falha ao vincular câmera: ${e.message}"
                )
            }
        }, mainExecutor)
    }

    /**
     * Alterna entre a câmera traseira e frontal.
     */
    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val newLens = if (_cameraState.value.lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCamera(lifecycleOwner, previewView, newLens)
    }

    /**
     * Alterna o modo do flash da câmera.
     */
    fun toggleFlash() {
        val currentMode = _cameraState.value.flashMode
        val newMode = when (currentMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = newMode
        _cameraState.value = _cameraState.value.copy(flashMode = newMode)
    }

    /**
     * Alterna a lanterna contínua (Torch).
     */
    fun toggleTorch() {
        camera?.let { cam ->
            val nextState = !_cameraState.value.isTorchOn
            cam.cameraControl.enableTorch(nextState)
            _cameraState.value = _cameraState.value.copy(isTorchOn = nextState)
        }
    }

    /**
     * Captura a foto diretamente via CameraX em resolução nativa do sensor,
     * corrigindo rotação e retornando um Bitmap otimizado para fiscalização.
     */
    suspend fun capturePhoto(): Bitmap = suspendCancellableCoroutine { continuation ->
        val capture = imageCapture
        if (capture == null) {
            continuation.resumeWithException(IllegalStateException("ImageCapture não está inicializado no CameraX."))
            return@suspendCancellableCoroutine
        }

        _cameraState.value = _cameraState.value.copy(isTakingPicture = true)

        capture.takePicture(
            mainExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val rotationDegrees = image.imageInfo.rotationDegrees
                        val bitmap = imageProxyToBitmap(image, rotationDegrees)
                        image.close()
                        _cameraState.value = _cameraState.value.copy(isTakingPicture = false)
                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        image.close()
                        _cameraState.value = _cameraState.value.copy(isTakingPicture = false)
                        continuation.resumeWithException(e)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _cameraState.value = _cameraState.value.copy(
                        isTakingPicture = false,
                        errorMessage = "Erro na captura da foto: ${exception.message}"
                    )
                    continuation.resumeWithException(exception)
                }
            }
        )
    }

    /**
     * Captura e criptografa diretamente com AES-256 no disco, retornando caminho e hash SHA-256.
     */
    suspend fun captureAndEncryptDirectly(prefix: String): Pair<String, String> {
        val bitmap = capturePhoto()
        return CryptoManager.encryptAndSaveBitmap(context, bitmap, prefix)
    }

    private fun imageProxyToBitmap(image: ImageProxy, rotationDegrees: Int): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
        } else {
            originalBitmap
        }
    }

    fun unbind() {
        cameraProvider?.unbindAll()
        _cameraState.value = _cameraState.value.copy(isInitialized = false)
    }
}
