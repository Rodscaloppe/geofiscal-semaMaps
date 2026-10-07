package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "coordenadas_geograficas",
    foreignKeys = [
        ForeignKey(
            entity = DenunciaEntity::class,
            parentColumns = ["id"],
            childColumns = ["denunciaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["denunciaId"]),
        Index(value = ["vistoriaId"]),
        Index(value = ["syncStatus"]),
        Index(value = ["timestamp"])
    ]
)
data class CoordenadaGeograficaEntity(
    @PrimaryKey
    val id: String, // UUID
    val denunciaId: String? = null,
    val vistoriaId: String? = null,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val acuraciaMetros: Float,
    val azimuteGraus: Float, // Orientação da bússola (0-360)
    val velocidadeKmh: Float = 0f,
    val provedorGps: String = "GPS_FUSED", // GPS_HARDWARE, GPS_FUSED, NETWORK
    val municipio: String,
    val bioma: String, // Amazônia, Cerrado, Pantanal
    val carNumero: String? = null,
    val isPontoCritico: Boolean = false, // Ponto de dano direto / foco de queimada
    val timestamp: Long,
    val ordemTrajetoria: Int = 0,
    val syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val syncTimestamp: Long? = null
)
