package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoInfracao(val label: String) {
    DESMATAMENTO_ILEGAL("Supressão Vegetal Não Autorizada (DETER/PRODES)"),
    QUEIMADA_NAO_AUTORIZADA("Uso Irregular do Fogo / Foco de Calor"),
    POLUICAO_HIDRICA("Contaminação de Manancial / Rio"),
    OCUPACAO_APP("Invasão ou Dano em Área de Preservação Permanente (APP)"),
    MINERACAO_ILEGAL("Garimpo ou Extração Mineral Clandestina"),
    OUTROS("Outras Infrações Ambientais")
}

enum class GravidadeInfracao(val label: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica (Embargo Imediato)")
}

enum class StatusDenuncia(val label: String) {
    RECEBIDA("Recebida na Triagem"),
    EM_APURACAO("Em Investigação de Campo"),
    VISTORIADA("Vistoria Concluída"),
    AUTUADA("Auto de Infração Lavrado"),
    ARQUIVADA("Arquivada")
}

@Entity(
    tableName = "denuncias",
    indices = [
        Index(value = ["protocolo"], unique = true),
        Index(value = ["status"]),
        Index(value = ["syncStatus"]),
        Index(value = ["municipio"])
    ]
)
data class DenunciaEntity(
    @PrimaryKey
    val id: String, // UUID
    val protocolo: String, // Ex: DEN-2026-SEMA-9842
    val titulo: String,
    val descricao: String,
    val tipoInfracao: String, // TipoInfracao name
    val gravidade: String, // GravidadeInfracao name
    val status: String, // StatusDenuncia name
    val origem: String, // OUVIDORIA, SATELITE_SEMA, FISCAL_CAMPO
    val denuncianteAnonimo: Boolean = true,
    val dataRegistro: Long,
    val municipio: String,
    val bioma: String, // Amazônia, Cerrado, Pantanal
    val carNumero: String? = null,
    val fiscalAtribuido: String? = null,
    val observacoesFiscais: String = "",
    val syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val syncTimestamp: Long? = null,
    val remoteId: String? = null
)
