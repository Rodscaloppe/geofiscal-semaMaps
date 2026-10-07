package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: String,
    val action: String, // CRIACAO_REGISTRO, ATUALIZACAO, SINCRONIZACAO_REMOTA, EXPORTACAO_PDF, EXPORTACAO_EXCEL, ALTERACAO_PERMISSAO, ALERTA_SEMA_IRREGULARIDADE
    val inspectorName: String,
    val timestamp: Long,
    val details: String,
    val hashIntegrity: String
)
