package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AuditLog
import com.example.data.model.InspectionRecord
import com.example.data.model.UserRolePermissions
import com.example.data.repository.InspectionRepository
import com.example.export.ExcelReportGenerator
import com.example.export.PdfReportGenerator
import com.example.service.FieldSensorManager
import com.example.service.NotificationHelper
import com.example.service.SemaIntegrationService
import com.example.service.SemaTelemetryResult
import com.example.service.SensorTelemetry
import com.example.service.SyncManager
import com.example.service.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class InspectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val syncManager = SyncManager(application, database, viewModelScope)
    val sensorManager = FieldSensorManager(application)
    val cameraCaptureService = com.example.service.CameraCaptureService(application)
    val repository = InspectionRepository(application, database, syncManager, viewModelScope)

    val allRecords: StateFlow<List<InspectionRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<AuditLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDenuncias: StateFlow<List<com.example.data.model.DenunciaEntity>> = repository.allDenuncias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDenunciasComDetalhes: StateFlow<List<com.example.data.model.DenunciaComDetalhes>> = repository.allDenunciasComDetalhes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentRole: StateFlow<UserRolePermissions> = repository.currentRole

    val telemetry: StateFlow<SensorTelemetry> = sensorManager.telemetry
    val syncState: StateFlow<SyncState> = syncManager.syncState
    val isGpsTracking: StateFlow<Boolean> = sensorManager.fusedLocationService.isTracking

    // Live SEMA-MT Remote Sensing evaluation derived from current coordinates
    private val _currentSemaTelemetry = MutableStateFlow<SemaTelemetryResult>(
        SemaIntegrationService.evaluateLocation(-15.6014, -56.0979)
    )
    val currentSemaTelemetry: StateFlow<SemaTelemetryResult> = _currentSemaTelemetry.asStateFlow()

    // Captured image state for current inspection form
    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    private val _selectedRecord = MutableStateFlow<InspectionRecord?>(null)
    val selectedRecord: StateFlow<InspectionRecord?> = _selectedRecord.asStateFlow()

    private val _selectedDenuncia = MutableStateFlow<com.example.data.model.DenunciaComDetalhes?>(null)
    val selectedDenuncia: StateFlow<com.example.data.model.DenunciaComDetalhes?> = _selectedDenuncia.asStateFlow()

    private val _showQrDialog = MutableStateFlow<InspectionRecord?>(null)
    val showQrDialog: StateFlow<InspectionRecord?> = _showQrDialog.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        NotificationHelper.initNotificationChannels(application)
        sensorManager.startListening()

        // Update SEMA evaluation whenever GPS moves noticeably
        viewModelScope.launch {
            sensorManager.telemetry.collect { tele ->
                _currentSemaTelemetry.value = SemaIntegrationService.evaluateLocation(tele.latitude, tele.longitude)
            }
        }

        // Initialize sample demo records if database is empty so map & dashboard look rich immediately
        viewModelScope.launch {
            allRecords.collect { records ->
                if (records.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    fun setCapturedPhoto(bitmap: Bitmap?) {
        _capturedPhoto.value = bitmap
    }

    fun selectRecord(record: InspectionRecord?) {
        _selectedRecord.value = record
    }

    fun selectDenuncia(item: com.example.data.model.DenunciaComDetalhes?) {
        _selectedDenuncia.value = item
    }

    fun showQr(record: InspectionRecord?) {
        _showQrDialog.value = record
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun saveInspection(title: String, notes: String, inspectorName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val photo = _capturedPhoto.value ?: createPlaceholderInspectionBitmap()
            val tele = telemetry.value
            val sema = currentSemaTelemetry.value

            val saved = repository.saveInspection(
                title = title,
                photoBitmap = photo,
                sensorTelemetry = tele,
                semaTelemetry = sema,
                inspectorName = inspectorName,
                notes = notes
            )

            _capturedPhoto.value = null
            _uiMessage.value = if (saved.hasIrregularity) {
                "Vistoria salva! Alerta de irregularidade emitido para a fiscalização."
            } else {
                "Vistoria registrada com foto criptografada AES-256 e geolocalização."
            }
            onDone()
        }
    }

    fun saveDenuncia(
        titulo: String,
        descricao: String,
        tipoInfracao: String,
        gravidade: String,
        carNumero: String?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val photo = _capturedPhoto.value
            val tele = telemetry.value
            val sema = currentSemaTelemetry.value

            repository.saveDenuncia(
                titulo = titulo,
                descricao = descricao,
                tipoInfracao = tipoInfracao,
                gravidade = gravidade,
                municipio = sema.municipality,
                bioma = sema.biome,
                carNumero = carNumero,
                latitude = tele.latitude,
                longitude = tele.longitude,
                altitude = tele.altitude,
                acuracia = tele.accuracy,
                azimute = tele.azimuthBearing,
                photoBitmap = photo
            )

            _capturedPhoto.value = null
            _uiMessage.value = "Denúncia registrada com coordenadas e foto anexada para sincronização offline."
            onDone()
        }
    }

    fun deleteRecord(record: InspectionRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            _uiMessage.value = "Registro excluído da base local."
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncManager.triggerSyncAll()
        }
    }

    fun exportPdf(record: InspectionRecord) {
        viewModelScope.launch {
            PdfReportGenerator.generateAndSharePdf(getApplication(), record)
            repository.addAuditLog(record.id, "EXPORTACAO_PDF", "Laudo oficial em PDF gerado e compartilhado.")
        }
    }

    fun exportExcel() {
        viewModelScope.launch {
            val records = allRecords.value
            if (records.isEmpty()) {
                _uiMessage.value = "Nenhum registro para exportar."
                return@launch
            }
            ExcelReportGenerator.exportAndShareExcel(getApplication(), records)
            repository.addAuditLog("ALL", "EXPORTACAO_EXCEL", "Relatório consolidado Excel de ${records.size} registros exportado.")
        }
    }

    fun exportDenunciaPdf(item: com.example.data.model.DenunciaComDetalhes) {
        viewModelScope.launch {
            com.example.export.SemaDenunciaPdfService.exportAndShareDenunciaPdf(getApplication(), item)
            repository.addAuditLog(item.denuncia.id, "EXPORTACAO_PDF_DENUNCIA", "Laudo oficial da denúncia ${item.denuncia.protocolo} exportado em PDF.")
        }
    }

    fun exportConsolidatedDenunciasPdf() {
        viewModelScope.launch {
            val denuncias = allDenuncias.value
            if (denuncias.isEmpty()) {
                _uiMessage.value = "Nenhuma denúncia cadastrada para exportação."
                return@launch
            }
            com.example.export.SemaDenunciaPdfService.exportConsolidatedDenunciasPdf(getApplication(), denuncias)
            repository.addAuditLog("ALL", "EXPORTACAO_CONSOLIDADO_DENUNCIAS", "Relatório consolidado de ${denuncias.size} denúncias exportado em PDF.")
        }
    }

    fun switchRole(role: UserRolePermissions) {
        repository.setRole(role)
    }

    fun updateServerConfig(url: String, apiKey: String) {
        syncManager.updateServerConfig(url, apiKey)
        _uiMessage.value = "Configurações da API REST SEMA atualizadas com sucesso."
    }

    fun refreshPrecisionGps() {
        viewModelScope.launch {
            val fix = sensorManager.fusedLocationService.getCurrentPrecisionFix()
            if (fix != null) {
                _uiMessage.value = "Fix GPS Fused obtido: Acurácia de ±${String.format("%.1f", fix.accuracy)}m"
            } else {
                _uiMessage.value = "Aguardando satélites GPS..."
            }
        }
    }

    fun toggleBackgroundTracking(enable: Boolean) {
        if (enable) {
            com.example.service.FieldInspectionLocationService.startService(getApplication())
            _uiMessage.value = "Rastreamento GPS em 2º plano ativado (Notificação contínua)."
        } else {
            com.example.service.FieldInspectionLocationService.stopService(getApplication())
            _uiMessage.value = "Rastreamento em 2º plano pausado."
        }
    }

    /**
     * Seeds initial real-world data points in Mato Grosso (Cuiabá, Sinop, Alta Floresta, Pantanal)
     * so user can immediately experience the interactive map, trajectory paths, and analytical dashboard!
     */
    private suspend fun seedInitialData() {
        val initialPoints = listOf(
            Triple("Reserva Legal Fazenda Mutum", -11.8642, -55.5056), // Sinop (Amazônia)
            Triple("Monitoramento APA Chapada", -15.4611, -55.7489),   // Chapada/Cuiabá (Cerrado)
            Triple("Fiscalização Rio Teles Pires", -9.8756, -56.0861),  // Alta Floresta (Amazônia)
            Triple("Vistoria Pantanal Barão de Melgaço", -16.2045, -55.9678), // Pantanal
            Triple("Polígono Agrícola Sorriso Norte", -12.5425, -55.7211)  // Sorriso (Amazônia)
        )

        for ((idx, item) in initialPoints.withIndex()) {
            val (title, lat, lng) = item
            val sema = SemaIntegrationService.evaluateLocation(lat, lng)
            val tele = SensorTelemetry(
                latitude = lat,
                longitude = lng,
                altitude = 180.0 + idx * 25,
                accuracy = 2.8f,
                azimuthBearing = (idx * 65f) % 360f,
                pitch = 1.2f,
                roll = -0.5f,
                isGpsFixed = true
            )
            val placeholderBitmap = createInitialSampleBitmap(title, sema.biome)
            repository.saveInspection(
                title = title,
                photoBitmap = placeholderBitmap,
                sensorTelemetry = tele,
                semaTelemetry = sema,
                inspectorName = "Fiscal Federal SEMA/IBAMA",
                notes = "Vistoria preventiva de sensoriamento remoto e checagem de CAR in-loco."
            )
        }

        // Seed initial Denúncias with coordinates and photo metadata for instant Google Maps visualization
        val initialDenuncias = listOf(
            DenunciaSeed(
                titulo = "Supressão Não Autorizada - Gleba Rio Teles Pires",
                descricao = "Constatada supressão de vegetação nativa em área de preservação permanente com maquinário pesado sem autorização de desmate.",
                tipoInfracao = "DESMATAMENTO_ILEGAL",
                gravidade = "CRITICA",
                municipio = "Alta Floresta",
                bioma = "Amazônia",
                carNumero = "MT-5100250-F7A10B",
                lat = -9.8756,
                lng = -56.0861,
                alt = 285.0
            ),
            DenunciaSeed(
                titulo = "Foco Ativo de Calor em Área de Manejo Florestal",
                descricao = "Identificado foco de incêndio florestal de rápida propagação sem autorização de queima controlada da SEMA.",
                tipoInfracao = "QUEIMADA_NAO_AUTORIZADA",
                gravidade = "ALTA",
                municipio = "Sinop",
                bioma = "Amazônia",
                carNumero = "MT-5107909-A4B12C",
                lat = -11.8542,
                lng = -55.5156,
                alt = 380.0
            ),
            DenunciaSeed(
                titulo = "Ocupação Irregular e Edificação em APP de Nascente",
                descricao = "Intervenção humana em faixa marginal de curso hídrico com risco imediato de assoreamento e dano à recarga hídrica.",
                tipoInfracao = "OCUPACAO_APP",
                gravidade = "MEDIA",
                municipio = "Chapada dos Guimarães",
                bioma = "Cerrado",
                carNumero = "MT-5103007-D8E901",
                lat = -15.4411,
                lng = -55.7389,
                alt = 680.0
            ),
            DenunciaSeed(
                titulo = "Drenagem Indevida e Aterro em Várzea Pantaneira",
                descricao = "Alteração antrópica de dinâmica hidrológica em área úmida de preservação com diques e canalização não autorizada.",
                tipoInfracao = "POLUICAO_HIDRICA",
                gravidade = "ALTA",
                municipio = "Barão de Melgaço",
                bioma = "Pantanal",
                carNumero = "MT-5101704-C3D45E",
                lat = -16.1945,
                lng = -55.9578,
                alt = 125.0
            ),
            DenunciaSeed(
                titulo = "Extração Mineral Clandestina (Garimpo) em Leito de Rio",
                descricao = "Operação de dragagem e lavagem de cascalho em leito ativo sem licença de operação ou outorga de recursos hídricos.",
                tipoInfracao = "MINERACAO_ILEGAL",
                gravidade = "CRITICA",
                municipio = "Juína",
                bioma = "Amazônia",
                carNumero = "MT-5105150-H1K23L",
                lat = -11.3789,
                lng = -58.7412,
                alt = 310.0
            ),
            DenunciaSeed(
                titulo = "Queimada Irregular em Área de Refúgio de Fauna",
                descricao = "Uso de fogo sem aceiros regulamentares em pastagem limítrofe a remanescente florestal nativo.",
                tipoInfracao = "QUEIMADA_NAO_AUTORIZADA",
                gravidade = "BAIXA",
                municipio = "Rondonópolis",
                bioma = "Cerrado",
                carNumero = "MT-5107602-M5N67P",
                lat = -16.4674,
                lng = -54.6361,
                alt = 240.0
            )
        )

        for (d in initialDenuncias) {
            val samplePhoto = createInitialSampleBitmap(d.titulo, d.bioma)
            repository.saveDenuncia(
                titulo = d.titulo,
                descricao = d.descricao,
                tipoInfracao = d.tipoInfracao,
                gravidade = d.gravidade,
                municipio = d.municipio,
                bioma = d.bioma,
                carNumero = d.carNumero,
                latitude = d.lat,
                longitude = d.lng,
                altitude = d.alt,
                acuracia = 2.4f,
                azimute = 145f,
                photoBitmap = samplePhoto
            )
        }
    }

    private data class DenunciaSeed(
        val titulo: String,
        val descricao: String,
        val tipoInfracao: String,
        val gravidade: String,
        val municipio: String,
        val bioma: String,
        val carNumero: String,
        val lat: Double,
        val lng: Double,
        val alt: Double
    )

    private fun createPlaceholderInspectionBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.parseColor("#1B4D3E"))
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("VISTORIA GEOREFERENCIADA SEMA-MT", 320f, 220f, paint)
        paint.textSize = 20f
        paint.color = android.graphics.Color.parseColor("#2EE59D")
        canvas.drawText("FOTO DE CAMPO CRIPTOGRAFADA AES-256", 320f, 260f, paint)
        return bitmap
    }

    private fun createInitialSampleBitmap(title: String, biome: String): Bitmap {
        val bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val bgCol = if (biome == "Amazônia") "#094025" else if (biome == "Pantanal") "#1A4968" else "#63471D"
        canvas.drawColor(android.graphics.Color.parseColor(bgCol))
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 26f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(title.take(35), 320f, 220f, paint)
        paint.textSize = 18f
        paint.color = android.graphics.Color.parseColor("#C7F9DC")
        canvas.drawText("BIOMA: $biome | SEMA-MT GEOAUDIT", 320f, 260f, paint)
        return bitmap
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
        cameraCaptureService.unbind()
    }
}
