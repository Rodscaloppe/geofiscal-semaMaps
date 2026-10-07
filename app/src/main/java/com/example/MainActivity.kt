package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.QrCodeDialog
import com.example.ui.dialogs.RecordDetailDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CaptureScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MapTrajectoryScreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.GoldenAlert
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InspectionViewModel

enum class AppNavTab(val label: String, val icon: ImageVector, val tag: String) {
    CAPTURE("Captura", Icons.Default.CameraAlt, "tab_capture"),
    HISTORY("Histórico", Icons.Default.History, "tab_history"),
    MAP("Mapa", Icons.Default.Map, "tab_map"),
    DASHBOARD("Painel", Icons.Default.Assessment, "tab_dashboard"),
    ADMIN("Admin", Icons.Default.AdminPanelSettings, "tab_admin")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GeoFiscalApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoFiscalApp(viewModel: InspectionViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(AppNavTab.CAPTURE) }
    val snackbarHostState = remember { SnackbarHostState() }

    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val selectedRecord by viewModel.selectedRecord.collectAsStateWithLifecycle()
    val qrRecord by viewModel.showQrDialog.collectAsStateWithLifecycle()

    // Handle back button on secondary screens
    if (currentTab != AppNavTab.CAPTURE) {
        BackHandler {
            currentTab = AppNavTab.CAPTURE
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF2EE59D),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GeoFiscal SEMA-MT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = currentRole.title.take(24),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC7F9DC)
                            )
                        }
                    }
                },
                actions = {
                    // Online / Offline Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (syncState.isOnline) ForestGreenDark else Color(0xFF4A1E1E),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (syncState.isOnline) Color(0xFF00C853) else Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (syncState.isOnline) "Online" else "Offline",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sync Action Button
                    IconButton(
                        onClick = { viewModel.triggerSync() },
                        enabled = !syncState.isSyncing,
                        modifier = Modifier.testTag("topbar_sync_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ForestGreenPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ForestGreenPrimary,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppNavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreenPrimary,
                            selectedTextColor = ForestGreenPrimary,
                            indicatorColor = Color(0xFFC7F9DC)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.CAPTURE -> CaptureScreen(
                    viewModel = viewModel,
                    onInspectionSaved = { currentTab = AppNavTab.HISTORY }
                )
                AppNavTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AppNavTab.MAP -> MapTrajectoryScreen(viewModel = viewModel)
                AppNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppNavTab.ADMIN -> AdminPanelScreen(viewModel = viewModel)
            }
        }
    }

    // Inspection Record Details Modal
    selectedRecord?.let { record ->
        RecordDetailDialog(
            record = record,
            onDismiss = { viewModel.selectRecord(null) },
            onPdfExport = { viewModel.exportPdf(record) },
            onQrClick = { viewModel.showQr(record) }
        )
    }

    // QR Code Dialog Modal
    qrRecord?.let { record ->
        QrCodeDialog(
            record = record,
            onDismiss = { viewModel.showQr(null) }
        )
    }
}
