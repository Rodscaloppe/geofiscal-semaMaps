package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DenunciaComDetalhes(
    @Embedded
    val denuncia: DenunciaEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "denunciaId"
    )
    val coordenadas: List<CoordenadaGeograficaEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "denunciaId"
    )
    val fotos: List<FotoMetadataEntity>
)
