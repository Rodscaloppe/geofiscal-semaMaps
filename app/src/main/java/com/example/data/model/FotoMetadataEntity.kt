package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foto_metadados",
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
        Index(value = ["hashSha256"], unique = true),
        Index(value = ["syncStatus"])
    ]
)
data class FotoMetadataEntity(
    @PrimaryKey
    val id: String, // UUID
    val denunciaId: String? = null,
    val vistoriaId: String? = null,
    val caminhoArquivoCriptografado: String, // Caminho local do arquivo .enc
    val hashSha256: String, // Hash pericial da imagem original para cadeia de custódia
    val algoritmoCriptografia: String = "AES-256-CBC",
    val larguraPixels: Int,
    val alturaPixels: Int,
    val tamanhoBytes: Long,
    val mimeType: String = "image/jpeg.enc",
    val dataCaptura: Long,
    val latitudeExif: Double,
    val longitudeExif: Double,
    val altitudeExif: Double,
    val azimuteCamera: Float, // Azimute da visada
    val inclinacaoPitch: Float, // Inclinação vertical do smartphone
    val inclinacaoRoll: Float, // Inclinação lateral
    val pressaoBarometricaHpa: Float = 1013.25f,
    val modeloDispositivo: String = "Android Device",
    val syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val urlRemota: String? = null,
    val syncTimestamp: Long? = null
)
