package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AppDatabase
import com.example.data.model.AuditLog
import com.example.data.model.CoordenadaGeograficaEntity
import com.example.data.model.DenunciaComDetalhes
import com.example.data.model.DenunciaEntity
import com.example.data.model.FotoMetadataEntity
import com.example.data.model.InspectionRecord
import com.example.data.model.SyncStatus
import com.example.data.model.UserRolePermissions
import com.example.security.CryptoManager
import com.example.service.NotificationHelper
import com.example.service.SemaIntegrationService
import com.example.service.SemaTelemetryResult
import com.example.service.SensorTelemetry
import com.example.service.SyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class InspectionRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val syncManager: SyncManager,
    private val repositoryScope: CoroutineScope
) {
    val allRecords: Flow<List<InspectionRecord>> = database.inspectionDao().getAllRecords()
    val allLogs: Flow<List<AuditLog>> = database.auditDao().getAllLogs()
    val allDenuncias: Flow<List<DenunciaEntity>> = database.denunciaDao().getAllDenuncias()
    val allDenunciasComDetalhes: Flow<List<DenunciaComDetalhes>> = database.denunciaDao().getAllDenunciasComDetalhes()

    private val _currentRole = MutableStateFlow(UserRolePermissions.ADMIN)
    val currentRole: StateFlow<UserRolePermissions> = _currentRole.asStateFlow()

    fun setRole(role: UserRolePermissions) {
        _currentRole.value = role
        repositoryScope.launch(Dispatchers.IO) {
            database.auditDao().insertLog(
                AuditLog(
                    recordId = "SYSTEM",
                    action = "ALTERACAO_PERMISSAO",
                    inspectorName = "Administrador",
                    timestamp = System.currentTimeMillis(),
                    details = "Perfil de acesso chaveado para: ${role.title}",
                    hashIntegrity = CryptoManager.calculateStringSha256("ROLE-${role.roleName}-${System.currentTimeMillis()}")
                )
            )
        }
    }

    suspend fun saveInspection(
        title: String,
        photoBitmap: Bitmap,
        sensorTelemetry: SensorTelemetry,
        semaTelemetry: SemaTelemetryResult,
        inspectorName: String,
        notes: String
    ): InspectionRecord = withContext(Dispatchers.IO) {
        val recordId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. AES Encryption of Captured Image
        val (encryptedPath, imageSha256) = CryptoManager.encryptAndSaveBitmap(
            context = context,
            bitmap = photoBitmap,
            filenamePrefix = "INSP_${semaTelemetry.municipality}"
        )

        // 2. Cryptographic Record Fingerprint / Hash
        val recordDigestContent = "$recordId-$now-${sensorTelemetry.latitude}-${sensorTelemetry.longitude}-$imageSha256-${semaTelemetry.semaStatus.name}"
        val recordSha256 = CryptoManager.calculateStringSha256(recordDigestContent)

        // 3. QR Code Payload
        val qrPayload = "SEMA-MT|ID:$recordId|GEO:${sensorTelemetry.latitude},${sensorTelemetry.longitude}|STATUS:${semaTelemetry.semaStatus.name}|CAR:${semaTelemetry.carNumber}|HASH:${recordSha256.take(12)}|VAL:https://sema.mt.gov.br/valida/$recordId"

        val record = InspectionRecord(
            id = recordId,
            timestamp = now,
            title = title.ifBlank { "Vistoria Ambiental - ${semaTelemetry.municipality}" },
            inspectorName = inspectorName.ifBlank { "Fiscal SEMA-MT" },
            inspectorRole = _currentRole.value.title,
            latitude = sensorTelemetry.latitude,
            longitude = sensorTelemetry.longitude,
            altitude = sensorTelemetry.altitude,
            accuracy = sensorTelemetry.accuracy,
            azimuthBearing = sensorTelemetry.azimuthBearing,
            pitch = sensorTelemetry.pitch,
            roll = sensorTelemetry.roll,
            municipality = semaTelemetry.municipality,
            biome = semaTelemetry.biome,
            carNumber = semaTelemetry.carNumber,
            semaStatus = semaTelemetry.semaStatus.name,
            hasIrregularity = semaTelemetry.hasIrregularity,
            irregularityDetails = semaTelemetry.irregularityDetails,
            notes = notes,
            encryptedImagePath = encryptedPath,
            imageHashSha256 = imageSha256,
            recordHashSha256 = recordSha256,
            syncStatus = SyncStatus.PENDING.name,
            qrCodePayload = qrPayload
        )

        // 4. Save to Room database (inspection record)
        database.inspectionDao().insertRecord(record)

        // 4b. Also save normalized CoordenadaGeograficaEntity
        val coordenadaEntity = CoordenadaGeograficaEntity(
            id = UUID.randomUUID().toString(),
            vistoriaId = recordId,
            latitude = sensorTelemetry.latitude,
            longitude = sensorTelemetry.longitude,
            altitude = sensorTelemetry.altitude,
            acuraciaMetros = sensorTelemetry.accuracy,
            azimuteGraus = sensorTelemetry.azimuthBearing,
            velocidadeKmh = sensorTelemetry.speed,
            provedorGps = "GPS_FUSED",
            municipio = semaTelemetry.municipality,
            bioma = semaTelemetry.biome,
            carNumero = semaTelemetry.carNumber,
            isPontoCritico = semaTelemetry.hasIrregularity,
            timestamp = now,
            syncStatus = SyncStatus.PENDING.name
        )
        database.coordenadaDao().insertCoordenada(coordenadaEntity)

        // 4c. Also save normalized FotoMetadataEntity
        val fileBytes = File(encryptedPath).length()
        val fotoMetadataEntity = FotoMetadataEntity(
            id = UUID.randomUUID().toString(),
            vistoriaId = recordId,
            caminhoArquivoCriptografado = encryptedPath,
            hashSha256 = imageSha256,
            algoritmoCriptografia = "AES-256-CBC",
            larguraPixels = photoBitmap.width,
            alturaPixels = photoBitmap.height,
            tamanhoBytes = fileBytes,
            dataCaptura = now,
            latitudeExif = sensorTelemetry.latitude,
            longitudeExif = sensorTelemetry.longitude,
            altitudeExif = sensorTelemetry.altitude,
            azimuteCamera = sensorTelemetry.azimuthBearing,
            inclinacaoPitch = sensorTelemetry.pitch,
            inclinacaoRoll = sensorTelemetry.roll,
            pressaoBarometricaHpa = sensorTelemetry.pressureHpa,
            syncStatus = SyncStatus.PENDING.name
        )
        database.fotoMetadataDao().insertFotoMetadata(fotoMetadataEntity)

        // 5. Add Audit Log
        database.auditDao().insertLog(
            AuditLog(
                recordId = recordId,
                action = "CRIACAO_REGISTRO",
                inspectorName = record.inspectorName,
                timestamp = now,
                details = "Registro criado com foto criptografada AES-256 e sensoramento SEMA em ${record.municipality}.",
                hashIntegrity = recordSha256
            )
        )

        // 6. IMMEDIATE ALERT NOTIFICATION if SEMA-MT irregularity detected!
        if (semaTelemetry.hasIrregularity) {
            NotificationHelper.showImmediateIrregularityAlert(
                context = context,
                municipality = semaTelemetry.municipality,
                irregularityType = semaTelemetry.semaStatus.label,
                details = semaTelemetry.irregularityDetails
            )

            database.auditDao().insertLog(
                AuditLog(
                    recordId = recordId,
                    action = "ALERTA_SEMA_IRREGULARIDADE",
                    inspectorName = record.inspectorName,
                    timestamp = now,
                    details = "Disparo de Notificação Push de Alta Prioridade: ${semaTelemetry.irregularityDetails}",
                    hashIntegrity = CryptoManager.calculateStringSha256("ALERT-$recordId")
                )
            )
        }

        // 7. Auto Sync trigger if online
        repositoryScope.launch {
            syncManager.triggerSyncAll()
        }

        record
    }

    /**
     * Saves a new Denúncia (infraction complaint) with full offline support,
     * including associated geographical coordinates and encrypted photo metadata.
     */
    suspend fun saveDenuncia(
        titulo: String,
        descricao: String,
        tipoInfracao: String,
        gravidade: String,
        municipio: String,
        bioma: String,
        carNumero: String?,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        acuracia: Float,
        azimute: Float,
        photoBitmap: Bitmap?,
        denuncianteAnonimo: Boolean = true
    ): DenunciaEntity = withContext(Dispatchers.IO) {
        val denunciaId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val protocolo = "DEN-${java.text.SimpleDateFormat("yyyy", java.util.Locale.US).format(java.util.Date())}-SEMA-${(1000..9999).random()}"

        val denuncia = DenunciaEntity(
            id = denunciaId,
            protocolo = protocolo,
            titulo = titulo,
            descricao = descricao,
            tipoInfracao = tipoInfracao,
            gravidade = gravidade,
            status = "RECEBIDA",
            origem = "FISCAL_CAMPO",
            denuncianteAnonimo = denuncianteAnonimo,
            dataRegistro = now,
            municipio = municipio,
            bioma = bioma,
            carNumero = carNumero,
            syncStatus = SyncStatus.PENDING.name
        )

        database.denunciaDao().insertDenuncia(denuncia)

        // Insert Coordinate
        val coord = CoordenadaGeograficaEntity(
            id = UUID.randomUUID().toString(),
            denunciaId = denunciaId,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            acuraciaMetros = acuracia,
            azimuteGraus = azimute,
            municipio = municipio,
            bioma = bioma,
            carNumero = carNumero,
            isPontoCritico = true,
            timestamp = now,
            syncStatus = SyncStatus.PENDING.name
        )
        database.coordenadaDao().insertCoordenada(coord)

        // Encrypt & Insert Photo Metadata if provided
        if (photoBitmap != null) {
            val (encPath, sha256) = CryptoManager.encryptAndSaveBitmap(
                context = context,
                bitmap = photoBitmap,
                filenamePrefix = "DEN_${protocolo.replace("-", "_")}"
            )
            val fotoMeta = FotoMetadataEntity(
                id = UUID.randomUUID().toString(),
                denunciaId = denunciaId,
                caminhoArquivoCriptografado = encPath,
                hashSha256 = sha256,
                algoritmoCriptografia = "AES-256-CBC",
                larguraPixels = photoBitmap.width,
                alturaPixels = photoBitmap.height,
                tamanhoBytes = File(encPath).length(),
                dataCaptura = now,
                latitudeExif = latitude,
                longitudeExif = longitude,
                altitudeExif = altitude,
                azimuteCamera = azimute,
                inclinacaoPitch = 0f,
                inclinacaoRoll = 0f,
                syncStatus = SyncStatus.PENDING.name
            )
            database.fotoMetadataDao().insertFotoMetadata(fotoMeta)
        }

        database.auditDao().insertLog(
            AuditLog(
                recordId = denunciaId,
                action = "REGISTRO_DENUNCIA",
                inspectorName = _currentRole.value.title,
                timestamp = now,
                details = "Denúncia $protocolo registrada em $municipio ($tipoInfracao).",
                hashIntegrity = CryptoManager.calculateStringSha256("$denunciaId-$protocolo-$now")
            )
        )

        repositoryScope.launch {
            syncManager.triggerSyncAll()
        }

        denuncia
    }

    suspend fun deleteRecord(record: InspectionRecord) = withContext(Dispatchers.IO) {
        database.inspectionDao().deleteRecord(record)
        database.auditDao().insertLog(
            AuditLog(
                recordId = record.id,
                action = "EXCLUSAO_REGISTRO",
                inspectorName = _currentRole.value.title,
                timestamp = System.currentTimeMillis(),
                details = "Registro ${record.id} excluído da base local.",
                hashIntegrity = CryptoManager.calculateStringSha256("DELETE-${record.id}")
            )
        )
    }

    suspend fun addAuditLog(recordId: String, action: String, details: String) = withContext(Dispatchers.IO) {
        database.auditDao().insertLog(
            AuditLog(
                recordId = recordId,
                action = action,
                inspectorName = _currentRole.value.title,
                timestamp = System.currentTimeMillis(),
                details = details,
                hashIntegrity = CryptoManager.calculateStringSha256("$recordId-$action-${System.currentTimeMillis()}")
            )
        )
    }
}
