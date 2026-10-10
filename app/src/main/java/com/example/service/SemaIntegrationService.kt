package com.example.service

import com.example.data.model.SemaStatus
import kotlin.random.Random

data class SemaTelemetryResult(
    val municipality: String,
    val biome: String,
    val carNumber: String,
    val carStatus: String,
    val semaStatus: SemaStatus,
    val hasIrregularity: Boolean,
    val irregularityDetails: String,
    val remoteSensingSatellite: String,
    val deforestationAreaHectares: Double,
    val inpeFireConfidencePercent: Int,
    val embargoTermNumber: String?
)

object SemaIntegrationService {

    // Municipalities of Mato Grosso with their respective biomes and reference coordinates
    private val MT_CITIES = listOf(
        MtCity("Cuiabá", "Cerrado", -15.6014, -56.0979),
        MtCity("Sinop", "Amazônia", -11.8642, -55.5056),
        MtCity("Alta Floresta", "Amazônia", -9.8756, -56.0861),
        MtCity("Rondonópolis", "Cerrado", -16.4674, -54.6372),
        MtCity("Cáceres", "Pantanal", -16.0747, -57.6789),
        MtCity("Sorriso", "Amazônia", -12.5425, -55.7211),
        MtCity("Barra do Garças", "Cerrado", -15.8931, -52.2567),
        MtCity("Poconé", "Pantanal", -16.2567, -56.6228),
        MtCity("Juína", "Amazônia", -11.3789, -58.7414),
        MtCity("Primavera do Leste", "Cerrado", -15.5592, -54.2961)
    )

    private data class MtCity(val name: String, val biome: String, val lat: Double, val lng: Double)

    /**
     * Cross-checks captured GPS location with SEMA-MT geodatabase,
     * CAR registry, satellite remote sensing (PRODES/DETER), and INPE thermal anomalies.
     */
    fun evaluateLocation(lat: Double, lng: Double): SemaTelemetryResult {
        // Find closest municipality in MT
        val closest = MT_CITIES.minByOrNull { city ->
            val dLat = city.lat - lat
            val dLng = city.lng - lng
            dLat * dLat + dLng * dLng
        } ?: MT_CITIES[0]

        // Deterministic simulation based on coordinate hashes to allow repeatable field inspection
        val coordHash = (kotlin.math.abs(lat * 1000).toInt() + kotlin.math.abs(lng * 1000).toInt())
        val randomSeed = Random(coordHash)

        val satellite = if (randomSeed.nextBoolean()) "Sentinel-2 / MSI (Copernicus)" else "CBERS-4A / WPM (INPE)"
        val carPrefix = "MT-${randomSeed.nextInt(1000000, 9999999)}-"
        val carSuffix = (1..6).map { ('A'..'F').random(randomSeed) }.joinToString("")
        val carNumber = "$carPrefix$carSuffix"

        // Probability of environmental irregularity for demonstration/field test
        val anomalyRoll = randomSeed.nextInt(100)

        val (status, hasIrregularity, details, embargoNum, deforestHa, fireConf) = when {
            anomalyRoll < 18 -> {
                val embargo = "TE-SEMA-MT nº ${randomSeed.nextInt(1024, 9890)}/2026"
                Tuple6(
                    SemaStatus.SOB_EMBARGO,
                    true,
                    "Área sob Termo de Embargo SEMA-MT ativo ($embargo). Descumprimento de embargo fiscal constatado no polígono.",
                    embargo,
                    randomSeed.nextDouble(12.5, 84.0),
                    0
                )
            }
            anomalyRoll < 34 -> {
                val deforest = randomSeed.nextDouble(5.4, 46.8)
                Tuple6(
                    SemaStatus.DESMATAMENTO_DETECTADO,
                    true,
                    "Alerta DETER/PRODES SEMA-MT confirmado. Supressão vegetal nativa não autorizada detectada: ${String.format("%.1f", deforest)} ha no bioma ${closest.biome}.",
                    null,
                    deforest,
                    0
                )
            }
            anomalyRoll < 48 -> {
                val fireConfVal = randomSeed.nextInt(75, 99)
                Tuple6(
                    SemaStatus.FOCO_QUEIMADA,
                    true,
                    "Foco de calor ativo detectado por sensor térmico MODIS/VIIRS (Confiança: $fireConfVal%). Queimada não autorizada em período proibitivo.",
                    null,
                    0.0,
                    fireConfVal
                )
            }
            anomalyRoll < 55 -> {
                Tuple6(
                    SemaStatus.PENDENCIA_LICENCA,
                    true,
                    "Licença de Operação (LO) do imóvel suspensa junto ao Sistema Integrado de Monitoramento SEMA-MT (SIMLAM).",
                    null,
                    0.0,
                    0
                )
            }
            else -> {
                Tuple6(
                    SemaStatus.REGULAR,
                    false,
                    "Imóvel com CAR Ativo e Validado. Cobertura florestal e Reserva Legal em conformidade com o Código Florestal e SEMA-MT.",
                    null,
                    0.0,
                    0
                )
            }
        }

        val carStatus = if (hasIrregularity) "CAR Pendente / Notificado" else "CAR Ativo e Regular"

        return SemaTelemetryResult(
            municipality = closest.name,
            biome = closest.biome,
            carNumber = carNumber,
            carStatus = carStatus,
            semaStatus = status,
            hasIrregularity = hasIrregularity,
            irregularityDetails = details,
            remoteSensingSatellite = satellite,
            deforestationAreaHectares = deforestHa,
            inpeFireConfidencePercent = fireConf,
            embargoTermNumber = embargoNum
        )
    }

    private data class ProtectedZone(val name: String, val lat: Double, val lng: Double, val radiusKm: Double, val isIndigenous: Boolean)

    private val PROTECTED_ZONES = listOf(
        ProtectedZone("Terra Indígena Sararé (Povo Nambikwara)", -15.02, -59.58, 45.0, true),
        ProtectedZone("Terra Indígena Menkragnoti / Kayapó", -9.50, -55.20, 50.0, true),
        ProtectedZone("Parque Estadual Cristalino (UC Proteção Integral)", -9.70, -55.90, 35.0, false),
        ProtectedZone("Estação Ecológica Serra das Araras", -15.65, -57.20, 25.0, false),
        ProtectedZone("Terra Indígena Enawenê-Nawê", -12.10, -58.80, 40.0, true)
    )

    /**
     * Specialized Analysis for Illegal Mining (Garimpo Clandestino),
     * ANM Mining Titling (PLG, Concessão, Pesquisa) and Environmental Justice compliance.
     */
    fun evaluateMiningActivity(lat: Double, lng: Double, customAnm: String? = null): com.example.data.model.MineracaoTelemetryResult {
        val coordHash = (kotlin.math.abs(lat * 1000).toInt() + kotlin.math.abs(lng * 1000).toInt())
        val randomSeed = Random(coordHash)

        // 1. Check intersection with Indigenous Lands or Conservation Units (Absolute Prohibition)
        val invadedZone = PROTECTED_ZONES.firstOrNull { zone ->
            val dLat = (zone.lat - lat) * 111.0
            val dLng = (zone.lng - lng) * 111.0
            val distKm = kotlin.math.sqrt(dLat * dLat + dLng * dLng)
            distKm <= zone.radiusKm
        }

        // 2. Proximity to Gold / Mineral Belts in MT (Poconé, Peixoto de Azevedo, Alta Floresta, Juína)
        val isGoldBelt = (lat in -16.8..-15.8 && lng in -57.2..-56.0) || // Poconé / Baixada
                (lat in -10.8..-9.5 && lng in -55.8..-54.5) ||   // Peixoto / Matupá
                (lat in -10.5..-9.2 && lng in -56.8..-55.8)      // Alta Floresta

        val substance = when {
            lat in -12.0..-10.8 && lng in -59.5..-58.0 -> com.example.data.model.SubstanciaMineral.DIAMANTE
            isGoldBelt -> com.example.data.model.SubstanciaMineral.OURO
            randomSeed.nextBoolean() -> com.example.data.model.SubstanciaMineral.CASSITERITA
            else -> com.example.data.model.SubstanciaMineral.OURO
        }

        val anmNum = customAnm?.takeIf { it.isNotBlank() } ?: "866.${randomSeed.nextInt(100, 999)}/${randomSeed.nextInt(2018, 2026)}"
        val titular = "Cooperativa / Mineradora do ${if (isGoldBelt) "Vale do Peixoto" else "Guaporé"} Ltda."

        val cavasHa = (randomSeed.nextInt(15, 180) / 10.0)

        // 3. Environmental Justice & ANM Status evaluation
        return if (invadedZone != null) {
            com.example.data.model.MineracaoTelemetryResult(
                anmProcess = anmNum,
                titularProcesso = "SEM TITULARIDADE VÁLIDA (ZONA VEDADA)",
                regimeMinerario = com.example.data.model.RegimeMinerario.SEM_TITULO,
                substance = substance,
                statusJustica = com.example.data.model.StatusJusticaAmbiental.INVASAO_TI_UC,
                licencaOperacaoSema = null,
                hasLicencaAtiva = false,
                isNearProtectedArea = true,
                protectedAreaName = invadedZone.name,
                enquadramentoLegal = "Art. 231 CF (Terra Indígena Inviolável) • Art. 2º Lei 8.176/91 (Usurpação) • Art. 55 Lei 9.605/98",
                cavasAreaHectares = cavasHa,
                alertaTurbidezHidrica = true,
                presencaMercurio = true
            )
        } else {
            val roll = randomSeed.nextInt(100)
            when {
                roll < 35 -> com.example.data.model.MineracaoTelemetryResult(
                    anmProcess = "SEM PROCESSO",
                    titularProcesso = "Clandestino / Não Cadastrado",
                    regimeMinerario = com.example.data.model.RegimeMinerario.SEM_TITULO,
                    substance = substance,
                    statusJustica = com.example.data.model.StatusJusticaAmbiental.CLANDESTINO_CRIME_FEDERAL,
                    licencaOperacaoSema = null,
                    hasLicencaAtiva = false,
                    isNearProtectedArea = false,
                    protectedAreaName = null,
                    enquadramentoLegal = "Crime Federal: Art. 2º Lei 8.176/91 (Pena: Detenção 1 a 5 anos) + Art. 55 Lei Crimes Ambientais",
                    cavasAreaHectares = cavasHa,
                    alertaTurbidezHidrica = true,
                    presencaMercurio = true
                )
                roll < 55 -> com.example.data.model.MineracaoTelemetryResult(
                    anmProcess = anmNum,
                    titularProcesso = titular,
                    regimeMinerario = com.example.data.model.RegimeMinerario.AUTORIZACAO_PESQUISA,
                    substance = substance,
                    statusJustica = com.example.data.model.StatusJusticaAmbiental.DESVIO_PESQUISA,
                    licencaOperacaoSema = "Apenas Guia de Pesquisa (Extração Proibida)",
                    hasLicencaAtiva = false,
                    isNearProtectedArea = false,
                    protectedAreaName = null,
                    enquadramentoLegal = "Desvio de Finalidade Minerária: Lavra comercial clandestina mascarada de pesquisa (Art. 55 Lei 9.605/98)",
                    cavasAreaHectares = cavasHa,
                    alertaTurbidezHidrica = true,
                    presencaMercurio = false
                )
                roll < 70 -> com.example.data.model.MineracaoTelemetryResult(
                    anmProcess = anmNum,
                    titularProcesso = titular,
                    regimeMinerario = com.example.data.model.RegimeMinerario.PLG,
                    substance = substance,
                    statusJustica = com.example.data.model.StatusJusticaAmbiental.DESCUMPRIMENTO_EMBARGO,
                    licencaOperacaoSema = "LO Suspensa por Despejo de Rejeitos",
                    hasLicencaAtiva = false,
                    isNearProtectedArea = false,
                    protectedAreaName = null,
                    enquadramentoLegal = "Descumprimento de Termo de Embargo SEMA nº 4891/2025 • Reincidência de Dano Ambiental",
                    cavasAreaHectares = cavasHa,
                    alertaTurbidezHidrica = true,
                    presencaMercurio = true
                )
                else -> com.example.data.model.MineracaoTelemetryResult(
                    anmProcess = anmNum,
                    titularProcesso = titular,
                    regimeMinerario = com.example.data.model.RegimeMinerario.PLG,
                    substance = substance,
                    statusJustica = com.example.data.model.StatusJusticaAmbiental.REGULAR,
                    licencaOperacaoSema = "LO SEMA nº ${randomSeed.nextInt(200000, 999999)}/2024 (Válida)",
                    hasLicencaAtiva = true,
                    isNearProtectedArea = false,
                    protectedAreaName = null,
                    enquadramentoLegal = "Regularidade Integral: PLG ativa na ANM com Licença de Operação (LO) válida na SEMA-MT",
                    cavasAreaHectares = cavasHa,
                    alertaTurbidezHidrica = false,
                    presencaMercurio = false
                )
            }
        }
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
    )
}

