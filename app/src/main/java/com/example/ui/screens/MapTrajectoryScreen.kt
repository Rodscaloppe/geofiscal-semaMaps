package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DenunciaComDetalhes
import com.example.data.model.InspectionRecord
import com.example.service.SensorTelemetry
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GoldenAlert
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.viewmodel.InspectionViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MapFilterCategory(val label: String) {
    TODAS("Todas"),
    DENUNCIAS("Denúncias"),
    VISTORIAS("Vistorias"),
    CRITICAS("Críticas"),
    AMAZONIA("Amazônia"),
    CERRADO("Cerrado"),
    PANTANAL("Pantanal")
}

@Composable
fun MapTrajectoryScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val denunciasComDetalhes by viewModel.allDenunciasComDetalhes.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()

    var selectedDenuncia by remember { mutableStateOf<DenunciaComDetalhes?>(null) }
    var selectedInspection by remember { mutableStateOf<InspectionRecord?>(null) }

    // Map visualization settings
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var showLayersMenu by remember { mutableStateOf(false) }
    var isRadarTacticalMode by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf(MapFilterCategory.TODAS) }

    // Default center in Mato Grosso (geographic center around Cuiabá/Sinop)
    val defaultCenter = LatLng(-12.8, -55.8)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 6.2f)
    }

    // Sort records chronologically for polyline trajectory
    val sortedRecords = remember(records) { records.sortedBy { it.timestamp } }

    val trajectoryPoints = remember(sortedRecords) {
        sortedRecords.map { LatLng(it.latitude, it.longitude) }
    }

    // Trajectory calculations (Total distance in km)
    val totalDistanceKm = remember(sortedRecords) {
        var distance = 0.0
        for (i in 0 until sortedRecords.size - 1) {
            distance += calculateDistanceKm(
                sortedRecords[i].latitude, sortedRecords[i].longitude,
                sortedRecords[i + 1].latitude, sortedRecords[i + 1].longitude
            )
        }
        distance
    }

    // Filter denuncias based on selected chip
    val filteredDenuncias = remember(denunciasComDetalhes, activeFilter) {
        when (activeFilter) {
            MapFilterCategory.TODAS, MapFilterCategory.DENUNCIAS -> denunciasComDetalhes
            MapFilterCategory.VISTORIAS -> emptyList()
            MapFilterCategory.CRITICAS -> denunciasComDetalhes.filter {
                it.denuncia.gravidade == "CRITICA" || it.denuncia.gravidade == "ALTA"
            }
            MapFilterCategory.AMAZONIA -> denunciasComDetalhes.filter {
                it.denuncia.bioma.equals("Amazônia", ignoreCase = true)
            }
            MapFilterCategory.CERRADO -> denunciasComDetalhes.filter {
                it.denuncia.bioma.equals("Cerrado", ignoreCase = true)
            }
            MapFilterCategory.PANTANAL -> denunciasComDetalhes.filter {
                it.denuncia.bioma.equals("Pantanal", ignoreCase = true)
            }
        }
    }

    // Filter inspections
    val filteredRecords = remember(records, activeFilter) {
        when (activeFilter) {
            MapFilterCategory.TODAS, MapFilterCategory.VISTORIAS -> records
            MapFilterCategory.DENUNCIAS -> emptyList()
            MapFilterCategory.CRITICAS -> records.filter { it.hasIrregularity }
            MapFilterCategory.AMAZONIA -> records.filter { it.biome.equals("Amazônia", ignoreCase = true) }
            MapFilterCategory.CERRADO -> records.filter { it.biome.equals("Cerrado", ignoreCase = true) }
            MapFilterCategory.PANTANAL -> records.filter { it.biome.equals("Pantanal", ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_map_screen")
    ) {
        if (!isRadarTacticalMode) {
            // ==========================================
            // 1. GOOGLE MAPS SDK VIEW
            // ==========================================
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = selectedMapType,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = {
                    selectedDenuncia = null
                    selectedInspection = null
                }
            ) {
                // Denúncias cadastradas no Room como Marcadores Geolocalizados
                filteredDenuncias.forEach { item ->
                    val coord = item.coordenadas.firstOrNull()
                    if (coord != null) {
                        val pos = LatLng(coord.latitude, coord.longitude)
                        val hue = when (item.denuncia.gravidade) {
                            "CRITICA" -> BitmapDescriptorFactory.HUE_RED
                            "ALTA" -> BitmapDescriptorFactory.HUE_ORANGE
                            "MEDIA" -> BitmapDescriptorFactory.HUE_YELLOW
                            else -> BitmapDescriptorFactory.HUE_AZURE
                        }

                        Marker(
                            state = remember(item.denuncia.id) { MarkerState(position = pos) },
                            title = "${item.denuncia.protocolo}: ${item.denuncia.titulo}",
                            snippet = "${item.denuncia.municipio} • ${item.denuncia.gravidade}",
                            icon = BitmapDescriptorFactory.defaultMarker(hue),
                            onClick = {
                                selectedDenuncia = item
                                selectedInspection = null
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(pos, 11f),
                                        durationMs = 600
                                    )
                                }
                                true // consume event to show custom bottom card
                            }
                        )
                    }
                }

                // Vistorias de campo como Marcadores
                filteredRecords.forEach { record ->
                    val pos = LatLng(record.latitude, record.longitude)
                    val hue = if (record.hasIrregularity) {
                        BitmapDescriptorFactory.HUE_ROSE
                    } else {
                        BitmapDescriptorFactory.HUE_GREEN
                    }

                    Marker(
                        state = remember(record.id) { MarkerState(position = pos) },
                        title = record.title,
                        snippet = "Vistoria SEMA: ${record.municipality} (${record.biome})",
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            selectedInspection = record
                            selectedDenuncia = null
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(pos, 11f),
                                    durationMs = 600
                                )
                            }
                            true
                        }
                    )
                }

                // Trajetória de Vistoria / Fiscalização
                if (trajectoryPoints.size > 1 && (activeFilter == MapFilterCategory.TODAS || activeFilter == MapFilterCategory.VISTORIAS)) {
                    Polyline(
                        points = trajectoryPoints,
                        color = Color(0xFF2EE59D),
                        width = 9f,
                        geodesic = true
                    )
                }

                // Marcador do GPS do Fiscal em Tempo Real
                val liveGpsPos = LatLng(telemetry.latitude, telemetry.longitude)
                Circle(
                    center = liveGpsPos,
                    radius = telemetry.accuracy.toDouble().coerceAtLeast(10.0),
                    fillColor = Color(0x3300E5FF),
                    strokeColor = Color(0xFF00E5FF),
                    strokeWidth = 3f
                )
                Marker(
                    state = remember(telemetry.latitude, telemetry.longitude) {
                        MarkerState(position = liveGpsPos)
                    },
                    title = "Fiscal em Campo (GPS ao Vivo)",
                    snippet = "Acurácia: ±${String.format("%.1f", telemetry.accuracy)}m",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
            }
        } else {
            // ==========================================
            // 2. RADAR TÁTICO OFFLINE (CANVAS FALLBACK)
            // ==========================================
            TacticalRadarCanvas(
                records = filteredRecords,
                denuncias = filteredDenuncias,
                telemetry = telemetry,
                sortedRecords = sortedRecords,
                onSelectRecord = {
                    selectedInspection = it
                    selectedDenuncia = null
                },
                onSelectDenuncia = {
                    selectedDenuncia = it
                    selectedInspection = null
                }
            )
        }

        // ==========================================
        // TOP OVERLAY: RESUMO GEOESPACIAL E FILTROS
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (!isRadarTacticalMode) Icons.Default.Map else Icons.Default.Radar,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!isRadarTacticalMode) "Mapa Google Maps SDK (SEMA-MT)" else "Radar Tático de Campo (Offline)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ForestGreenPrimary
                        ) {
                            Text(
                                text = "${denunciasComDetalhes.size} DENÚNCIAS",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricText("Denúncias no Room", "${denunciasComDetalhes.size} cadastradas")
                        MetricText("Vistorias", "${records.size} pontos")
                        MetricText("Trajetória", "${String.format("%.1f", totalDistanceKm)} km")
                        MetricText("Alertas Críticos", "${denunciasComDetalhes.count { it.denuncia.gravidade == "CRITICA" }} focos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips Horizontal Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MapFilterCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = activeFilter == cat,
                        onClick = { activeFilter = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    MapFilterCategory.TODAS -> "Todas (${denunciasComDetalhes.size + records.size})"
                                    MapFilterCategory.DENUNCIAS -> "Denúncias (${denunciasComDetalhes.size})"
                                    MapFilterCategory.VISTORIAS -> "Vistorias (${records.size})"
                                    MapFilterCategory.CRITICAS -> "Críticas (${denunciasComDetalhes.count { it.denuncia.gravidade == "CRITICA" }})"
                                    else -> cat.label
                                },
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // ==========================================
        // FLOATING ACTION CONTROLS (RIGHT SIDE)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Toggle Google Maps vs Offline Radar
            FilledIconButton(
                onClick = { isRadarTacticalMode = !isRadarTacticalMode },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRadarTacticalMode) ForestGreenDark else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("toggle_map_mode_button")
            ) {
                Icon(
                    imageVector = if (isRadarTacticalMode) Icons.Default.Map else Icons.Default.Radar,
                    contentDescription = "Alternar Modo de Mapa",
                    tint = if (isRadarTacticalMode) Color.White else ForestGreenPrimary
                )
            }

            // Layer Selector (Google Maps)
            if (!isRadarTacticalMode) {
                Box {
                    FilledIconButton(
                        onClick = { showLayersMenu = !showLayersMenu },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.testTag("map_layer_button")
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Camadas do Mapa")
                    }

                    DropdownMenu(
                        expanded = showLayersMenu,
                        onDismissRequest = { showLayersMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Padrão / Vetorial") },
                            onClick = {
                                selectedMapType = MapType.NORMAL
                                showLayersMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Satélite (Alta Resolução)") },
                            onClick = {
                                selectedMapType = MapType.SATELLITE
                                showLayersMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Híbrido (Satélite + Vias)") },
                            onClick = {
                                selectedMapType = MapType.HYBRID
                                showLayersMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Terreno (Topográfico)") },
                            onClick = {
                                selectedMapType = MapType.TERRAIN
                                showLayersMenu = false
                            }
                        )
                    }
                }
            }

            // Center on Current GPS Live Location
            FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        val livePos = LatLng(telemetry.latitude, telemetry.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(livePos, 14f),
                            durationMs = 800
                        )
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("btn_center_my_location")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Meu Local GPS", tint = Color.White)
            }

            // Fit All Fiscalizações in View (Bounds)
            FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        val allPoints = mutableListOf<LatLng>()
                        filteredDenuncias.forEach { d ->
                            d.coordenadas.firstOrNull()?.let {
                                allPoints.add(LatLng(it.latitude, it.longitude))
                            }
                        }
                        filteredRecords.forEach { r ->
                            allPoints.add(LatLng(r.latitude, r.longitude))
                        }

                        if (allPoints.isNotEmpty()) {
                            val builder = LatLngBounds.Builder()
                            allPoints.forEach { builder.include(it) }
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(builder.build(), 120),
                                durationMs = 800
                            )
                        } else {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(defaultCenter, 6.2f),
                                durationMs = 800
                            )
                        }
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.testTag("btn_fit_all_markers")
            ) {
                Icon(Icons.Default.ZoomOutMap, contentDescription = "Ajustar Enquadramento")
            }

            // Zoom In / Zoom Out
            FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Mais Zoom")
            }

            FilledIconButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos Zoom")
            }
        }

        // ==========================================
        // BOTTOM OVERLAYS: DETAIL PREVIEW CARDS
        // ==========================================

        // 1. CARD DE DENÚNCIA SELECIONADA NO MAPA
        AnimatedVisibility(
            visible = selectedDenuncia != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedDenuncia?.let { item ->
                val coord = item.coordenadas.firstOrNull()
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .testTag("denuncia_map_detail_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (item.denuncia.gravidade) {
                                        "CRITICA" -> CriticalRedContainer
                                        "ALTA" -> Color(0xFFFFE0B2)
                                        else -> Color(0xFFE8F5E9)
                                    }
                                ) {
                                    Text(
                                        text = "${item.denuncia.protocolo} • GRAVIDADE ${item.denuncia.gravidade}",
                                        color = when (item.denuncia.gravidade) {
                                            "CRITICA" -> CriticalRed
                                            "ALTA" -> Color(0xFFE65100)
                                            else -> ForestGreenDark
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.denuncia.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { selectedDenuncia = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }

                        Text(
                            text = item.denuncia.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Metadados Geográficos e Ambientais
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Local: ${item.denuncia.municipio} • Bioma: ${item.denuncia.bioma}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    item.denuncia.carNumero?.let {
                                        Text(
                                            text = "CAR: $it",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (coord != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "GPS: Lat ${String.format("%.5f", coord.latitude)}, Lng ${String.format("%.5f", coord.longitude)} (±${String.format("%.1f", coord.acuraciaMetros)}m)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportDenunciaPdf(item) },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Laudo PDF SEMA", fontSize = 12.sp)
                            }

                            if (coord != null) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Coordenadas", "${coord.latitude}, ${coord.longitude}")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Coordenadas copiadas!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copiar GPS", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CARD DE VISTORIA SELECIONADA NO MAPA
        AnimatedVisibility(
            visible = selectedInspection != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedInspection?.let { pinRecord ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .testTag("map_pin_detail_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pinRecord.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { selectedInspection = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        }

                        Text(
                            text = "${pinRecord.municipality} (MT) • ${pinRecord.biome} • CAR: ${pinRecord.carNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (pinRecord.hasIrregularity) CriticalRedContainer else EmeraldContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (pinRecord.hasIrregularity) "INFRAÇÃO: ${pinRecord.irregularityDetails}" else "REGULAR SEMA-MT",
                                color = if (pinRecord.hasIrregularity) CriticalRed else OnEmeraldContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportPdf(pinRecord) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Laudo PDF", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.showQr(pinRecord) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR Code", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Radar Tático de Campo desenhado via Canvas para alta performance e operação 100% offline.
 */
@Composable
private fun TacticalRadarCanvas(
    records: List<InspectionRecord>,
    denuncias: List<DenunciaComDetalhes>,
    telemetry: SensorTelemetry,
    sortedRecords: List<InspectionRecord>,
    onSelectRecord: (InspectionRecord) -> Unit,
    onSelectDenuncia: (DenunciaComDetalhes) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val minLat = -17.5
    val maxLat = -9.0
    val minLng = -59.5
    val maxLng = -51.5

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.6f, 4.0f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(records, denuncias, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()

                    fun toScreenOffset(lat: Double, lng: Double): Offset {
                        val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                        val normY = ((maxLat - lat) / (maxLat - minLat)).toFloat()
                        val x = normX * width * scale + offsetX
                        val y = normY * height * scale + offsetY
                        return Offset(x, y)
                    }

                    // Check denuncias first
                    for (d in denuncias) {
                        d.coordenadas.firstOrNull()?.let { c ->
                            val pinPos = toScreenOffset(c.latitude, c.longitude)
                            if ((tapOffset - pinPos).getDistance() < 40f) {
                                onSelectDenuncia(d)
                                return@detectTapGestures
                            }
                        }
                    }

                    // Check inspection records
                    for (r in records) {
                        val pinPos = toScreenOffset(r.latitude, r.longitude)
                        if ((tapOffset - pinPos).getDistance() < 40f) {
                            onSelectRecord(r)
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // 1. Fundo do terreno tático
        drawRect(Color(0xFF0F1E17))

        // 2. Linhas de grade geoespacial
        val gridColor = Color(0x22FFFFFF)
        for (step in 1..8) {
            val y = height * (step / 9f)
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            val x = width * (step / 9f)
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        }

        fun toScreenOffset(lat: Double, lng: Double): Offset {
            val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
            val normY = ((maxLat - lat) / (maxLat - minLat)).toFloat()
            val x = normX * width * scale + offsetX
            val y = normY * height * scale + offsetY
            return Offset(x, y)
        }

        // 3. Trajetória percorrida
        if (sortedRecords.size > 1) {
            val path = Path()
            val first = toScreenOffset(sortedRecords[0].latitude, sortedRecords[0].longitude)
            path.moveTo(first.x, first.y)
            for (i in 1 until sortedRecords.size) {
                val pt = toScreenOffset(sortedRecords[i].latitude, sortedRecords[i].longitude)
                path.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = path,
                color = Color(0x442EE59D),
                style = Stroke(width = 8f * scale.coerceAtMost(2f), cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = Color(0xFF2EE59D),
                style = Stroke(
                    width = 3.5f * scale.coerceAtMost(2f),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
            )
        }

        // 4. Marcador ao vivo do GPS
        val currentPos = toScreenOffset(telemetry.latitude, telemetry.longitude)
        drawCircle(color = Color(0x4400E5FF), radius = 24f * scale.coerceAtMost(2f), center = currentPos)
        drawCircle(color = Color(0xFF00E5FF), radius = 10f * scale.coerceAtMost(2f), center = currentPos)
        drawCircle(color = Color.White, radius = 4f * scale.coerceAtMost(2f), center = currentPos)

        // 5. Marcadores de Denúncias no Room
        for (d in denuncias) {
            d.coordenadas.firstOrNull()?.let { c ->
                val pt = toScreenOffset(c.latitude, c.longitude)
                val pinCol = if (d.denuncia.gravidade == "CRITICA") CriticalRed else Color(0xFFFF9800)
                val pinRadius = 14f * scale.coerceAtMost(2f)

                drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = pinRadius + 3f, center = pt.copy(y = pt.y + 2f))
                drawCircle(color = pinCol, radius = pinRadius, center = pt)
                drawCircle(color = Color.White, radius = pinRadius * 0.45f, center = pt)
            }
        }

        // 6. Marcadores de Vistorias
        for (r in records) {
            val pt = toScreenOffset(r.latitude, r.longitude)
            val pinCol = if (r.hasIrregularity) CriticalRed else Color(0xFF00C853)
            val pinRadius = 11f * scale.coerceAtMost(2f)

            drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = pinRadius + 2f, center = pt.copy(y = pt.y + 2f))
            drawCircle(color = pinCol, radius = pinRadius, center = pt)
            drawCircle(color = Color.White, radius = pinRadius * 0.4f, center = pt)
        }
    }
}

@Composable
private fun MetricText(label: String, value: String) {
    Column {
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

private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
