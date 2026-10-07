package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SemaTelemetryResult
import com.example.service.SensorTelemetry
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GoldenAlert
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.viewmodel.InspectionViewModel

@Composable
fun CaptureScreen(
    viewModel: InspectionViewModel,
    onInspectionSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val sema by viewModel.currentSemaTelemetry.collectAsStateWithLifecycle()
    val capturedBitmap by viewModel.capturedPhoto.collectAsStateWithLifecycle()
    val isGpsTracking by viewModel.isGpsTracking.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var inspectorName by remember { mutableStateOf("Fiscal Técnico SEMA-MT") }
    var isSaving by remember { mutableStateOf(false) }

    // Permission launchers
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(perms.toTypedArray())
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            viewModel.setCapturedPhoto(bmp)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.setCapturedPhoto(bmp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Top Banner: Status Header
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ForestGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "VISTORIA GEOESPACIAL SEMA-MT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC7F9DC)
                    )
                    Text(
                        text = "Câmera com Telemetria e Criptografia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Criptografia AES-256",
                    tint = Color(0xFF2EE59D),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // CameraX Live Viewfinder or Captured Photo Preview
        if (capturedBitmap == null) {
            com.example.ui.components.CameraCapturePreview(
                cameraService = viewModel.cameraCaptureService,
                telemetry = telemetry,
                municipality = sema.municipality,
                onPhotoCaptured = { bmp ->
                    viewModel.setCapturedPhoto(bmp)
                },
                modifier = Modifier.testTag("camerax_viewfinder_live")
            )

            // Secondary option: Import from Gallery
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gallery_button")
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ou Importar Imagem da Galeria")
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("camera_preview_card")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay watermark with real-time GPS
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "LAT: ${String.format("%.6f", telemetry.latitude)}°  LNG: ${String.format("%.6f", telemetry.longitude)}°",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ALT: ${String.format("%.1f", telemetry.altitude)}m | AZIMUTE: ${String.format("%.1f", telemetry.azimuthBearing)}° | ${sema.municipality} - MT",
                                color = Color(0xFF2EE59D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Buttons when photo is captured
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.setCapturedPhoto(null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("retake_photo_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tirar Outra Foto")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gallery_button_retake")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeria")
                }
            }
        }

        // Telemetry & Sensor HUD Card (FusedLocationProviderClient)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("fused_location_telemetry_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.isGpsFixed) Color(0xFF00C853) else Color(0xFFFFB300))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "FusedLocationProviderClient",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (telemetry.isGpsFixed) "GPS em Tempo Real (Alta Precisão)" else "Localizando satélites...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Compass Needle Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ForestGreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Bússola",
                                tint = ForestGreenPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(telemetry.azimuthBearing)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.0f", telemetry.azimuthBearing)}°",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sensor Grid Values
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "Latitude", value = String.format("%.6f°", telemetry.latitude))
                    TelemetryItem(label = "Longitude", value = String.format("%.6f°", telemetry.longitude))
                    TelemetryItem(label = "Altitude", value = "${String.format("%.1f", telemetry.altitude)}m")
                    TelemetryItem(
                        label = "Acurácia GPS",
                        value = "±${String.format("%.1f", telemetry.accuracy)}m"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Speed, Distance & Anti-Fraud Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", telemetry.speed)} km/h • ${String.format("%.2f", telemetry.distanceTraveledKm)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Anti-fraud pericial badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (telemetry.isMockLocation) Icons.Default.Warning else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (telemetry.isMockLocation) CriticalRed else Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (telemetry.isMockLocation) "ALERTA: Mock GPS" else "Hardware Autêntico",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isMockLocation) CriticalRed else Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons for location precision and tracking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.refreshPrecisionGps() },
                        modifier = Modifier.weight(1f).testTag("refresh_gps_precision_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix de Precisão", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.toggleBackgroundTracking(!isGpsTracking) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGpsTracking) CriticalRed else ForestGreenDark
                        ),
                        modifier = Modifier.weight(1f).testTag("toggle_background_tracking_button")
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGpsTracking) "Pausar GPS" else "Rastrear em 2º Plano",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // SEMA-MT Remote Sensing & Environmental Evaluation Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (sema.hasIrregularity) CriticalRedContainer else EmeraldContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (sema.hasIrregularity) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (sema.hasIrregularity) CriticalRed else ForestGreenDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sensoriamento Remoto SEMA-MT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (sema.hasIrregularity) CriticalRed else OnEmeraldContainer
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (sema.hasIrregularity) CriticalRed else ForestGreenPrimary
                    ) {
                        Text(
                            text = sema.biome.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Município: ${sema.municipality} (MT)  |  CAR: ${sema.carNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (sema.hasIrregularity) CriticalRed else OnEmeraldContainer
                )

                Text(
                    text = sema.irregularityDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sema.hasIrregularity) Color(0xFF690005) else Color(0xFF034024),
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (sema.hasIrregularity) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notificação imediata habilitada. QR Code pericial será gerado ao salvar.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CriticalRed
                    )
                }
            }
        }

        // Form Inputs: Title, Inspector, Notes
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Identificação / Título da Vistoria") },
            placeholder = { Text("Ex: Fazenda Santa Maria - Talhão 04") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inspection_title_input"),
            singleLine = true
        )

        OutlinedTextField(
            value = inspectorName,
            onValueChange = { inspectorName = it },
            label = { Text("Fiscal Responsável") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inspector_name_input"),
            singleLine = true
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Observações Técnicas de Campo") },
            placeholder = { Text("Descreva anomalias vegetais, indícios de queima ou conformidade...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inspection_notes_input"),
            maxLines = 3
        )

        // Save & Sync Button
        Button(
            onClick = {
                isSaving = true
                viewModel.saveInspection(
                    title = title,
                    notes = notes,
                    inspectorName = inspectorName
                ) {
                    isSaving = false
                    onInspectionSaved()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_inspection_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criptografando AES-256 e Salvando...")
            } else {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salvar com Criptografia e Gerar QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TelemetryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
