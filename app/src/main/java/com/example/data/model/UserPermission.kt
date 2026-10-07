package com.example.data.model

data class UserRolePermissions(
    val roleName: String,
    val title: String,
    val canApprove: Boolean,
    val canExportPdf: Boolean,
    val canExportExcel: Boolean,
    val canChangeApi: Boolean,
    val canBatchSync: Boolean,
    val canEditRecords: Boolean
) {
    companion object {
        val ADMIN = UserRolePermissions(
            roleName = "ADMIN",
            title = "Administrador Geral SEMA-MT",
            canApprove = true,
            canExportPdf = true,
            canExportExcel = true,
            canChangeApi = true,
            canBatchSync = true,
            canEditRecords = true
        )

        val FISCAL_TECNICO = UserRolePermissions(
            roleName = "FISCAL_TECNICO",
            title = "Fiscal Técnico Ambiental (SEMA)",
            canApprove = true,
            canExportPdf = true,
            canExportExcel = true,
            canChangeApi = false,
            canBatchSync = true,
            canEditRecords = true
        )

        val INSPETOR_CAMPO = UserRolePermissions(
            roleName = "INSPETOR_CAMPO",
            title = "Inspetor Operacional de Campo",
            canApprove = false,
            canExportPdf = true,
            canExportExcel = false,
            canChangeApi = false,
            canBatchSync = true,
            canEditRecords = false
        )

        val AUDITOR_EXTERNO = UserRolePermissions(
            roleName = "AUDITOR_EXTERNO",
            title = "Auditor Externo / Ministério Público",
            canApprove = false,
            canExportPdf = true,
            canExportExcel = true,
            canChangeApi = false,
            canBatchSync = false,
            canEditRecords = false
        )

        val ALL_ROLES = listOf(ADMIN, FISCAL_TECNICO, INSPETOR_CAMPO, AUDITOR_EXTERNO)
    }
}
