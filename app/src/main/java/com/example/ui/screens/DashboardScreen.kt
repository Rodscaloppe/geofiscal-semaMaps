package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CriticalRedContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GoldenAlert
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.viewmodel.InspectionViewModel

@Composable
fun DashboardScreen(
    viewModel: InspectionViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    val totalRecords = records.size
    val irregularCount = records.count { it.hasIrregularity }
    val regularCount = totalRecords - irregularCount
    val complianceRate = if (totalRecords > 0) ((regularCount.toDouble() / totalRecords) * 100).toInt() else 100
    val pendingSyncCount = records.count { it.syncStatus != "SYNCED" }

    // Biome distribution
    val amazoniaCount = records.count { it.biome == "Amazônia" }
    val cerradoCount = records.count { it.biome == "Cerrado" }
    val pantanalCount = records.count { it.biome == "Pantanal" }

    // Critical irregular records
    val criticalRecords = remember(records) {
        records.filter { it.hasIrregularity }.take(3)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header Title Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ForestGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PAINEL ESTRATÉGICO DE MONITORAMENTO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC7F9DC)
                    )
                    Text(
                        text = "SEMA-MT Inteligência Geoespacial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = Color(0xFF2EE59D),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 4 KPI Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Vistorias",
                value = "$totalRecords",
                icon = Icons.Default.Forest,
                accentColor = ForestGreenPrimary,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Irregularidades",
                value = "$irregularCount",
                icon = Icons.Default.Warning,
                accentColor = CriticalRed,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Conformidade",
                value = "$complianceRate%",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Fila Offline",
                value = "$pendingSyncCount",
                icon = Icons.Default.CloudSync,
                accentColor = GoldenAlert,
                modifier = Modifier.weight(1f)
            )
        }

        // Environmental Anomaly Alert Feed
        if (criticalRecords.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CriticalRedContainer),
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
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CriticalRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Anomalias Ambientais Críticas (SEMA-MT)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CriticalRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    criticalRecords.forEach { record ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "${record.municipality} - ${record.title}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF690005)
                            )
                            Text(
                                text = record.irregularityDetails,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1B1B1B)
                            )
                        }
                    }
                }
            }
        }

        // Chart 1: Environmental Compliance Donut Chart
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Índice de Regularidade Ambiental",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Donut Chart Canvas
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            val strokeWidth = 22f
                            val radius = (size.minDimension - strokeWidth) / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            // Background circle (irregular)
                            drawCircle(
                                color = CriticalRed,
                                radius = radius,
                                center = center,
                                style = Stroke(strokeWidth)
                            )

                            // Foreground arc (regular)
                            val sweepAngle = if (totalRecords > 0) (regularCount.toFloat() / totalRecords) * 360f else 360f
                            drawArc(
                                color = Color(0xFF00C853),
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$complianceRate%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Regular",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChartLegendItem(color = Color(0xFF00C853), label = "Regulares", count = regularCount)
                        ChartLegendItem(color = CriticalRed, label = "Irregulares / Embargo", count = irregularCount)
                    }
                }
            }
        }

        // Chart 2: Biome Distribution Bar Chart
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Distribuição por Biomas em Mato Grosso",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                BiomeBarItem(name = "Amazônia", count = amazoniaCount, total = totalRecords, color = Color(0xFF0F5A37))
                Spacer(modifier = Modifier.height(8.dp))
                BiomeBarItem(name = "Cerrado", count = cerradoCount, total = totalRecords, color = Color(0xFF8D6E63))
                Spacer(modifier = Modifier.height(8.dp))
                BiomeBarItem(name = "Pantanal", count = pantanalCount, total = totalRecords, color = Color(0xFF0288D1))
            }
        }

        // Quick Export and Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.exportExcel() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dashboard_export_excel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exportar Relatório Geral")
            }

            Button(
                onClick = { viewModel.triggerSync() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dashboard_sync_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                enabled = !syncState.isSyncing
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (syncState.isSyncing) "Sincronizando..." else "Sincronizar Nuvem")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BiomeBarItem(name: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = "$count (${(fraction * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}
