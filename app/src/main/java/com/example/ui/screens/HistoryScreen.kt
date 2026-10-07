package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InspectionRecord
import com.example.security.CryptoManager
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GoldenAlert
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.viewmodel.InspectionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val denunciasComDetalhes by viewModel.allDenunciasComDetalhes.collectAsStateWithLifecycle()

    var selectedSection by remember { androidx.compose.runtime.mutableIntStateOf(0) } // 0 = Vistorias, 1 = Denúncias
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredRecords = records.filter { record ->
        val matchesSearch = record.title.contains(searchQuery, ignoreCase = true) ||
                record.municipality.contains(searchQuery, ignoreCase = true) ||
                record.carNumber.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "IRREGULAR" -> record.hasIrregularity
            "REGULAR" -> !record.hasIrregularity
            "PENDENTE" -> record.syncStatus == "PENDING" || record.syncStatus == "FAILED"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Section selector: Vistorias vs Denúncias SEMA
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = ForestGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("Vistorias (${records.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Denúncias SEMA (${denunciasComDetalhes.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSection == 0) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar por município, CAR ou título...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "TODOS",
                    onClick = { selectedFilter = "TODOS" },
                    label = { Text("Todos (${records.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ForestGreenPrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = selectedFilter == "IRREGULAR",
                    onClick = { selectedFilter = "IRREGULAR" },
                    label = { Text("Irregulares (${records.count { it.hasIrregularity }})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CriticalRed, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = selectedFilter == "PENDENTE",
                    onClick = { selectedFilter = "PENDENTE" },
                    label = { Text("Offline (${records.count { it.syncStatus != "SYNCED" }})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldenAlert, selectedLabelColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sync and Export Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredRecords.size} registro(s) encontrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.exportExcel() },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("export_excel_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.triggerSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !syncState.isSyncing,
                        modifier = Modifier.testTag("sync_all_button")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (syncState.isSyncing) "Sincronizando..." else "Sincronizar", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Records List
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhuma vistoria encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Realize uma nova vistoria na aba 'Captura'",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        InspectionCard(
                            record = record,
                            canEdit = currentRole.canEditRecords,
                            onPdfClick = { viewModel.exportPdf(record) },
                            onQrClick = { viewModel.showQr(record) },
                            onDeleteClick = { viewModel.deleteRecord(record) },
                            onCardClick = { viewModel.selectRecord(record) }
                        )
                    }
                }
            }
        } else {
            // Seção de Denúncias SEMA-MT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${denunciasComDetalhes.size} denúncia(s) salvas no Room",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.exportConsolidatedDenunciasPdf() },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_consolidated_denuncias_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Consolidado (SEMA)", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (denunciasComDetalhes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma denúncia cadastrada ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(denunciasComDetalhes, key = { it.denuncia.id }) { item ->
                        DenunciaCard(
                            item = item,
                            onExportPdf = { viewModel.exportDenunciaPdf(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DenunciaCard(
    item: com.example.data.model.DenunciaComDetalhes,
    onExportPdf: () -> Unit
) {
    val denuncia = item.denuncia
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val dataStr = remember(denuncia.dataRegistro) { dateFormat.format(Date(denuncia.dataRegistro)) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("denuncia_card_${denuncia.protocolo}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ForestGreenPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = denuncia.protocolo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (denuncia.gravidade == "CRITICA" || denuncia.gravidade == "ALTA") CriticalRedContainer else EmeraldContainer
                ) {
                    Text(
                        text = "GRAVIDADE: ${denuncia.gravidade}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (denuncia.gravidade == "CRITICA" || denuncia.gravidade == "ALTA") CriticalRed else OnEmeraldContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = denuncia.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${denuncia.municipio} (MT) • Bioma ${denuncia.bioma}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Infração: ${denuncia.tipoInfracao}",
                style = MaterialTheme.typography.bodySmall,
                color = ForestGreenPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = denuncia.descricao,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dataStr • ${item.coordenadas.size} coord. • ${item.fotos.size} foto(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Button(
                    onClick = onExportPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_pdf_button_${denuncia.protocolo}")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Laudo Oficial PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InspectionCard(
    record: InspectionRecord,
    canEdit: Boolean,
    onPdfClick: () -> Unit,
    onQrClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val formattedDate = remember(record.timestamp) { dateFormat.format(Date(record.timestamp)) }

    // Decrypt thumbnail asynchronously
    val thumbnailBitmap by produceState<Bitmap?>(initialValue = null, record.encryptedImagePath) {
        value = withContext(Dispatchers.IO) {
            CryptoManager.decryptBitmap(record.encryptedImagePath)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("inspection_card_${record.id.take(6)}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ForestGreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbnailBitmap != null) {
                        Image(
                            bitmap = thumbnailBitmap!!.asImageBitmap(),
                            contentDescription = "Foto da Vistoria",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${record.municipality} (MT) • Bioma ${record.biome}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "CAR: ${record.carNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SEMA Status badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (record.hasIrregularity) CriticalRedContainer else EmeraldContainer
                        ) {
                            Text(
                                text = if (record.hasIrregularity) "IRREGULAR / EMBARGO" else "REGULAR SEMA",
                                color = if (record.hasIrregularity) CriticalRed else OnEmeraldContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Sync Status badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (record.syncStatus == "SYNCED") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (record.syncStatus == "SYNCED") Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (record.syncStatus == "SYNCED") Color(0xFF2E7D32) else GoldenAlert,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (record.syncStatus == "SYNCED") "Nuvem" else "Offline",
                                    color = if (record.syncStatus == "SYNCED") Color(0xFF2E7D32) else GoldenAlert,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPdfClick) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar Laudo PDF",
                            tint = CriticalRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onQrClick) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Ver QR Code",
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (canEdit) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir Vistoria",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
