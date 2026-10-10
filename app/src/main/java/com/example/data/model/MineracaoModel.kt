package com.example.data.model

enum class RegimeMinerario(val label: String, val allowsExtraction: Boolean) {
    PLG("Permissão de Lavra Garimpeira (PLG - ANM)", true),
    CONCESSAO_LAVRA("Concessão de Lavra Definitiva (ANM)", true),
    GUIA_UTILIZACAO("Guia de Utilização Prévia (ANM)", true),
    AUTORIZACAO_PESQUISA("Autorização de Pesquisa (Extração PROIBIDA)", false),
    REQUERIMENTO_LAVRA("Requerimento em Análise (Sem Título)", false),
    SEM_TITULO("Clandestino / Sem Processo na ANM", false)
}

enum class SubstanciaMineral(val label: String, val symbol: String) {
    OURO("Ouro Aluvionar e Filão", "Au"),
    CASSITERITA("Cassiterita / Estanho", "Sn"),
    DIAMANTE("Diamante em Aluvião", "C"),
    MANGANES("Manganês", "Mn"),
    AREIA_CASCALHO("Areia / Cascalho / Saibro", "SiO2"),
    OUTROS("Outras Substâncias Minerais", "Min")
}

enum class StatusJusticaAmbiental(val label: String, val isCrime: Boolean, val colorHex: Long) {
    REGULAR("Conforme: Título ANM e LO SEMA Ativos", false, 0xFF2E7D32),
    CLANDESTINO_CRIME_FEDERAL("CRIME FEDERAL: Usurpação de Patrimônio da União (Art. 2º Lei 8.176/91)", true, 0xFFD32F2F),
    DESVIO_PESQUISA("IRREGULAR: Extração Ativa sob Guisa de Pesquisa", true, 0xFFE65100),
    DESCUMPRIMENTO_EMBARGO("CRIME: Descumprimento de Termo de Embargo SEMA/IBAMA", true, 0xFFC2185B),
    INVASAO_TI_UC("CRIME CONSTITUCIONAL: Invasão de Terra Indígena / UC (Art. 231 CF)", true, 0xFFB71C1C)
}

data class MineracaoTelemetryResult(
    val anmProcess: String,
    val titularProcesso: String,
    val regimeMinerario: RegimeMinerario,
    val substancia: SubstanciaMineral,
    val statusJustica: StatusJusticaAmbiental,
    val licencaOperacaoSema: String?, // Ex: "LO nº 310842/2024" ou null
    val hasLicencaAtiva: Boolean,
    val isNearProtectedArea: Boolean,
    val protectedAreaName: String?,
    val enquadramentoLegal: String,
    val cavasAreaHectares: Double,
    val alertaTurbidezHidrica: Boolean,
    val presencaMercurio: Boolean
)
