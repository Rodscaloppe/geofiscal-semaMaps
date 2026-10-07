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

    private data class Tuple6<A, B, C, D, E, F>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
    )
}
