package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SemaStatus(val label: String, val isIrregular: Boolean) {
    REGULAR("Regular SEMA-MT", false),
    SOB_EMBARGO("Área Embargada SEMA-MT", true),
    DESMATAMENTO_DETECTADO("Alerta PRODES/DETER - Supressão Não Autorizada", true),
    FOCO_QUEIMADA("Foco de Calor Ativo (INPE/SEMA)", true),
    PENDENCIA_LICENCA("Licença Ambiental Suspensa / Vencida", true)
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

@Entity(tableName = "inspection_records")
data class InspectionRecord(
    @PrimaryKey
    val id: String, // UUID
    val timestamp: Long,
    val title: String,
    val inspectorName: String,
    val inspectorRole: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val azimuthBearing: Float, // Compass heading in degrees
    val pitch: Float,
    val roll: Float,
    val municipality: String,
    val biome: String, // Amazônia, Cerrado, Pantanal
    val carNumber: String, // MT-XXXXXXXX
    val semaStatus: String, // SemaStatus name
    val hasIrregularity: Boolean,
    val irregularityDetails: String,
    val notes: String,
    val encryptedImagePath: String,
    val imageHashSha256: String,
    val recordHashSha256: String,
    val syncStatus: String, // SyncStatus name
    val syncTimestamp: Long? = null,
    val remoteServerId: String? = null,
    val qrCodePayload: String
)
