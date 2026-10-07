package com.example.ui.components

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.CameraCaptureService
import com.example.service.SensorTelemetry
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import kotlinx.coroutines.launch

@Composable
fun CameraCapturePreview(
    cameraService: CameraCaptureService,
    telemetry: SensorTelemetry,
    municipality: String,
    onPhotoCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val cameraState by cameraService.cameraState.collectAsStateWithLifecycle()
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraService.unbind()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .testTag("camerax_preview_container")
    ) {
        // CameraX Surface View
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef = this
                    cameraService.bindCamera(lifecycleOwner, this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Grid & Leveling Reticle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Rule of thirds lines (subtle guide)
            val gridColor = Color.White.copy(alpha = 0.25f)
            drawLine(gridColor, Offset(w / 3, 0f), Offset(w / 3, h), strokeWidth = 1f)
            drawLine(gridColor, Offset(2 * w / 3, 0f), Offset(2 * w / 3, h), strokeWidth = 1f)
            drawLine(gridColor, Offset(0f, h / 3), Offset(w, h / 3), strokeWidth = 1f)
            drawLine(gridColor, Offset(0f, 2 * h / 3), Offset(w, 2 * h / 3), strokeWidth = 1f)

            // Center targeting crosshairs
            val centerX = w / 2
            val centerY = h / 2
            val crosshairColor = Color(0xFF2EE59D).copy(alpha = 0.7f)
            drawLine(crosshairColor, Offset(centerX - 16f, centerY), Offset(centerX + 16f, centerY), strokeWidth = 2f)
            drawLine(crosshairColor, Offset(centerX, centerY - 16f), Offset(centerX, centerY + 16f), strokeWidth = 2f)
        }

        // Top Toolbar: Flash, Torch, Flip Camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                    // Flash Mode Toggle
                    IconButton(
                        onClick = { cameraService.toggleFlash() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val flashIcon = when (cameraState.flashMode) {
                            androidx.camera.core.ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            androidx.camera.core.ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        }
                        Icon(flashIcon, contentDescription = "Flash", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    // Torch Toggle (Lanterna)
                    if (cameraState.hasTorch) {
                        IconButton(
                            onClick = { cameraService.toggleTorch() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Highlight,
                                contentDescription = "Lanterna",
                                tint = if (cameraState.isTorchOn) Color(0xFFFFD54F) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Flip Camera
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                IconButton(
                    onClick = {
                        previewViewRef?.let { cameraService.toggleCamera(lifecycleOwner, it) }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Inverter Câmera",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Telemetry Live Watermark Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LAT: ${String.format("%.6f", telemetry.latitude)}°  LNG: ${String.format("%.6f", telemetry.longitude)}°",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ALT: ${String.format("%.1f", telemetry.altitude)}m | AZIMUTE: ${String.format("%.0f", telemetry.azimuthBearing)}° | $municipality - MT",
                        color = EmeraldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, ForestGreenPrimary, CircleShape)
                        .clickable(enabled = !isCapturing && !cameraState.isTakingPicture) {
                            isCapturing = true
                            coroutineScope.launch {
                                try {
                                    val bitmap = cameraService.capturePhoto()
                                    onPhotoCaptured(bitmap)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isCapturing = false
                                }
                            }
                        }
                        .testTag("camerax_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing || cameraState.isTakingPicture) {
                        CircularProgressIndicator(
                            color = ForestGreenPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ForestGreenPrimary)
                        )
                    }
                }
            }
        }
    }
}
