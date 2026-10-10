import Foundation

struct SemaTelemetryResult: Equatable {
    let municipality: String
    let biome: String
    let carNumber: String
    let carStatus: String
    let semaStatus: SemaStatus
    let hasIrregularity: Bool
    let irregularityDetails: String
    let remoteSensingSatellite: String
    let deforestationAreaHectares: Double
    let inpeFireConfidencePercent: Int
    let embargoTermNumber: String?
}

class SemaIntegrationService {
    static let shared = SemaIntegrationService()

    private struct MtCity {
        let name: String
        let biome: String
        let lat: Double
        let lng: Double
    }

    private let mtCities = [
        MtCity(name: "Cuiabá", biome: "Cerrado", lat: -15.6014, lng: -56.0979),
        MtCity(name: "Sinop", biome: "Amazônia", lat: -11.8642, lng: -55.5056),
        MtCity(name: "Alta Floresta", biome: "Amazônia", lat: -9.8756, lng: -56.0861),
        MtCity(name: "Rondonópolis", biome: "Cerrado", lat: -16.4674, lng: -54.6372),
        MtCity(name: "Cáceres", biome: "Pantanal", lat: -16.0747, lng: -57.6789),
        MtCity(name: "Sorriso", biome: "Amazônia", lat: -12.5425, lng: -55.7211),
        MtCity(name: "Barra do Garças", biome: "Cerrado", lat: -15.8931, lng: -52.2567),
        MtCity(name: "Poconé", biome: "Pantanal", lat: -16.2567, lng: -56.6228),
        MtCity(name: "Juína", biome: "Amazônia", lat: -11.3789, lng: -58.7414),
        MtCity(name: "Primavera do Leste", biome: "Cerrado", lat: -15.5592, lng: -54.2961)
    ]

    func evaluateLocation(lat: Double, lng: Double) -> SemaTelemetryResult {
        // Find closest municipality in MT
        let closest = mtCities.min(by: { city1, city2 in
            let d1 = (city1.lat - lat) * (city1.lat - lat) + (city1.lng - lng) * (city1.lng - lng)
            let d2 = (city2.lat - lat) * (city2.lat - lat) + (city2.lng - lng) * (city2.lng - lng)
            return d1 < d2
        }) ?? mtCities[0]

        // Deterministic pseudo-random seed based on coordinate hashes
        let coordHash = abs(Int(lat * 1000)) + abs(Int(lng * 1000))
        var generator = SeededRandomGenerator(seed: UInt64(coordHash))

        let satellite = generator.nextBool() ? "Sentinel-2 / MSI (Copernicus)" : "CBERS-4A / WPM (INPE)"
        let carRandomNum = generator.nextInt(min: 1000000, max: 9999999)
        let carSuffix = String(format: "%06X", generator.nextInt(min: 0x100000, max: 0xFFFFFF))
        let carNumber = "MT-\(carRandomNum)-\(carSuffix)"

        let anomalyRoll = generator.nextInt(min: 0, max: 100)

        let status: SemaStatus
        let hasIrregularity: Bool
        let details: String
        let embargoNum: String?
        let deforestHa: Double
        let fireConf: Int

        if anomalyRoll < 18 {
            let embargoCode = "TE-SEMA-MT nº \(generator.nextInt(min: 1024, max: 9890))/2026"
            status = .sobEmbargo
            hasIrregularity = true
            details = "Área sob Termo de Embargo SEMA-MT ativo (\(embargoCode)). Descumprimento de embargo fiscal constatado no polígono."
            embargoNum = embargoCode
            deforestHa = Double(generator.nextInt(min: 125, max: 840)) / 10.0
            fireConf = 0
        } else if anomalyRoll < 34 {
            let deforest = Double(generator.nextInt(min: 54, max: 468)) / 10.0
            status = .desmatamentoDetectado
            hasIrregularity = true
            details = "Alerta DETER/PRODES SEMA-MT confirmado. Supressão vegetal nativa não autorizada detectada: \(String(format: "%.1f", deforest)) ha no bioma \(closest.biome)."
            embargoNum = nil
            deforestHa = deforest
            fireConf = 0
        } else if anomalyRoll < 48 {
            let fireConfVal = generator.nextInt(min: 75, max: 99)
            status = .focoQueimada
            hasIrregularity = true
            details = "Foco de calor ativo detectado por sensor térmico MODIS/VIIRS (Confiança: \(fireConfVal)%). Queimada não autorizada em período proibitivo."
            embargoNum = nil
            deforestHa = 0.0
            fireConf = fireConfVal
        } else if anomalyRoll < 55 {
            status = .pendenciaLicenca
            hasIrregularity = true
            details = "Licença de Operação (LO) do imóvel suspensa junto ao Sistema Integrado de Monitoramento SEMA-MT (SIMLAM)."
            embargoNum = nil
            deforestHa = 0.0
            fireConf = 0
        } else {
            status = .regular
            hasIrregularity = false
            details = "Imóvel com CAR Ativo e Validado. Cobertura florestal e Reserva Legal em conformidade com o Código Florestal e SEMA-MT."
            embargoNum = nil
            deforestHa = 0.0
            fireConf = 0
        }

        let carStatus = hasIrregularity ? "CAR Pendente / Notificado" : "CAR Ativo e Regular"

        return SemaTelemetryResult(
            municipality: closest.name,
            biome: closest.biome,
            carNumber: carNumber,
            carStatus: carStatus,
            semaStatus: status,
            hasIrregularity: hasIrregularity,
            irregularityDetails: details,
            remoteSensingSatellite: satellite,
            deforestationAreaHectares: deforestHa,
            inpeFireConfidencePercent: fireConf,
            embargoTermNumber: embargoNum
        )
    }

    func isValidCarNumber(_ car: String) -> Bool {
        let pattern = "^[A-Za-z]{2}-\\d{7}-[A-Fa-f0-9]{4,32}$"
        return car.range(of: pattern, options: .regularExpression) != nil
    }

    private struct ProtectedZone {
        let name: String
        let lat: Double
        let lng: Double
        let radiusKm: Double
        let isIndigenous: Bool
    }

    private let protectedZones: [ProtectedZone] = [
        ProtectedZone(name: "Terra Indígena Sararé (Povo Nambikwara)", lat: -15.02, lng: -59.58, radiusKm: 45.0, isIndigenous: true),
        ProtectedZone(name: "Terra Indígena Menkragnoti / Kayapó", lat: -9.50, lng: -55.20, radiusKm: 50.0, isIndigenous: true),
        ProtectedZone(name: "Parque Estadual Cristalino (UC Proteção Integral)", lat: -9.70, lng: -55.90, radiusKm: 35.0, isIndigenous: false),
        ProtectedZone(name: "Estação Ecológica Serra das Araras", lat: -15.65, lng: -57.20, radiusKm: 25.0, isIndigenous: false),
        ProtectedZone(name: "Terra Indígena Enawenê-Nawê", lat: -12.10, lng: -58.80, radiusKm: 40.0, isIndigenous: true)
    ]

    func evaluateMiningActivity(lat: Double, lng: Double, customAnm: String? = nil) -> MineracaoTelemetryResult {
        let coordHash = abs(Int(lat * 1000)) + abs(Int(lng * 1000))
        var generator = SeededRandomGenerator(seed: UInt64(coordHash))

        // 1. Check intersection with Indigenous Lands or Conservation Units (Absolute Prohibition)
        let invadedZone = protectedZones.first { zone in
            let dLat = (zone.lat - lat) * 111.0
            let dLng = (zone.lng - lng) * 111.0
            let distKm = sqrt(dLat * dLat + dLng * dLng)
            return distKm <= zone.radiusKm
        }

        // 2. Proximity to Gold / Mineral Belts in MT (Poconé, Peixoto de Azevedo, Alta Floresta, Juína)
        let isGoldBelt = (lat >= -16.8 && lat <= -15.8 && lng >= -57.2 && lng <= -56.0) ||
                         (lat >= -10.8 && lat <= -9.5 && lng >= -55.8 && lng <= -54.5) ||
                         (lat >= -10.5 && lat <= -9.2 && lng >= -56.8 && lng <= -55.8)

        let substance: SubstanciaMineral
        if lat >= -12.0 && lat <= -10.8 && lng >= -59.5 && lng <= -58.0 {
            substance = .diamante
        } else if isGoldBelt {
            substance = .ouro
        } else if generator.nextBool() {
            substance = .cassiterita
        } else {
            substance = .ouro
        }

        let anmNum: String
        if let custom = customAnm, !custom.trimmingCharacters(in: .whitespaces).isEmpty {
            anmNum = custom
        } else {
            anmNum = "866.\(generator.nextInt(min: 100, max: 999))/\(generator.nextInt(min: 2018, max: 2026))"
        }

        let titular = "Cooperativa / Mineradora do \(isGoldBelt ? "Vale do Peixoto" : "Guaporé") Ltda."
        let cavasHa = Double(generator.nextInt(min: 15, max: 180)) / 10.0

        if let zone = invadedZone {
            return MineracaoTelemetryResult(
                anmProcess: anmNum,
                titularProcesso: "SEM TITULARIDADE VÁLIDA (ZONA VEDADA)",
                regimeMinerario: .semTitulo,
                substancia: substance,
                statusJustica: .invasaoTiUc,
                licencaOperacaoSema: nil,
                hasLicencaAtiva: false,
                isNearProtectedArea: true,
                protectedAreaName: zone.name,
                enquadramentoLegal: "Art. 231 CF (Terra Indígena Inviolável) • Art. 2º Lei 8.176/91 (Usurpação) • Art. 55 Lei 9.605/98",
                cavasAreaHectares: cavasHa,
                alertaTurbidezHidrica: true,
                presencaMercurio: true
            )
        } else {
            let roll = generator.nextInt(min: 0, max: 100)
            if roll < 35 {
                return MineracaoTelemetryResult(
                    anmProcess: "SEM PROCESSO",
                    titularProcesso: "Clandestino / Não Cadastrado",
                    regimeMinerario: .semTitulo,
                    substancia: substance,
                    statusJustica: .clandestinoCrimeFederal,
                    licencaOperacaoSema: nil,
                    hasLicencaAtiva: false,
                    isNearProtectedArea: false,
                    protectedAreaName: nil,
                    enquadramentoLegal: "Crime Federal: Art. 2º Lei 8.176/91 (Pena: Detenção 1 a 5 anos) + Art. 55 Lei Crimes Ambientais",
                    cavasAreaHectares: cavasHa,
                    alertaTurbidezHidrica: true,
                    presencaMercurio: true
                )
            } else if roll < 55 {
                return MineracaoTelemetryResult(
                    anmProcess: anmNum,
                    titularProcesso: titular,
                    regimeMinerario: .autorizacaoPesquisa,
                    substancia: substance,
                    statusJustica: .desvioPesquisa,
                    licencaOperacaoSema: "Apenas Guia de Pesquisa (Extração Proibida)",
                    hasLicencaAtiva: false,
                    isNearProtectedArea: false,
                    protectedAreaName: nil,
                    enquadramentoLegal: "Desvio de Finalidade Minerária: Lavra comercial clandestina mascarada de pesquisa (Art. 55 Lei 9.605/98)",
                    cavasAreaHectares: cavasHa,
                    alertaTurbidezHidrica: true,
                    presencaMercurio: false
                )
            } else if roll < 70 {
                return MineracaoTelemetryResult(
                    anmProcess: anmNum,
                    titularProcesso: titular,
                    regimeMinerario: .plg,
                    substancia: substance,
                    statusJustica: .descumprimentoEmbargo,
                    licencaOperacaoSema: "LO Suspensa por Despejo de Rejeitos",
                    hasLicencaAtiva: false,
                    isNearProtectedArea: false,
                    protectedAreaName: nil,
                    enquadramentoLegal: "Descumprimento de Termo de Embargo SEMA nº 4891/2025 • Reincidência de Dano Ambiental",
                    cavasAreaHectares: cavasHa,
                    alertaTurbidezHidrica: true,
                    presencaMercurio: true
                )
            } else {
                return MineracaoTelemetryResult(
                    anmProcess: anmNum,
                    titularProcesso: titular,
                    regimeMinerario: .plg,
                    substancia: substance,
                    statusJustica: .regular,
                    licencaOperacaoSema: "LO SEMA nº \(generator.nextInt(min: 200000, max: 999999))/2024 (Válida)",
                    hasLicencaAtiva: true,
                    isNearProtectedArea: false,
                    protectedAreaName: nil,
                    enquadramentoLegal: "Regularidade Integral: PLG ativa na ANM com Licença de Operação (LO) válida na SEMA-MT",
                    cavasAreaHectares: cavasHa,
                    alertaTurbidezHidrica: false,
                    presencaMercurio: false
                )
            }
        }
    }
}

// Simple deterministic generator for repeatability on the same coordinates
private struct SeededRandomGenerator {
    private var state: UInt64

    init(seed: UInt64) {
        self.state = seed != 0 ? seed : 0xCAFEBABE
    }

    mutating func next() -> UInt64 {
        state = state &* 6364136223846793005 &+ 1442695040888963407
        return state
    }

    mutating func nextBool() -> Bool {
        return (next() >> 32) % 2 == 0
    }

    mutating func nextInt(min: Int, max: Int) -> Int {
        guard max > min else { return min }
        let range = UInt64(max - min)
        let value = (next() >> 16) % (range + 1)
        return min + Int(value)
    }
}
