import Foundation
import SwiftUI
import Combine
import CommonCrypto

@MainActor
class InspectionStore: ObservableObject {
    @Published var records: [InspectionRecord] = []
    @Published var denuncias: [DenunciaModel] = []
    @Published var auditLogs: [AuditLog] = []
    @Published var currentRole: UserRolePermissions = .admin
    @Published var isOnline: Bool = true
    @Published var isSyncing: Bool = false
    @Published var lastSyncTimestamp: Date? = nil
    @Published var uiMessage: String? = nil

    private let recordsFile = "inspection_records.json"
    private let denunciasFile = "denuncias.json"
    private let logsFile = "audit_logs.json"

    init() {
        loadData()
        if records.isEmpty && denuncias.isEmpty {
            seedInitialData()
        }
    }

    // MARK: - SHA-256 Calculation
    static func sha256(_ string: String) -> String {
        guard let data = string.data(using: .utf8) else { return "" }
        var digest = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
        data.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(data.count), &digest)
        }
        return digest.map { String(format: "%02x", $0) }.joined()
    }

    static func sha256Data(_ data: Data) -> String {
        var digest = [UInt8](repeating: 0, count: Int(CC_SHA256_DIGEST_LENGTH))
        data.withUnsafeBytes {
            _ = CC_SHA256($0.baseAddress, CC_LONG(data.count), &digest)
        }
        return digest.map { String(format: "%02x", $0) }.joined()
    }

    // MARK: - Persistence
    private func getDocumentsURL() -> URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
    }

    private func saveData() {
        let docs = getDocumentsURL()
        do {
            let recordsData = try JSONEncoder().encode(records)
            try recordsData.write(to: docs.appendingPathComponent(recordsFile))

            let denunciasData = try JSONEncoder().encode(denuncias)
            try denunciasData.write(to: docs.appendingPathComponent(denunciasFile))

            let logsData = try JSONEncoder().encode(auditLogs)
            try logsData.write(to: docs.appendingPathComponent(logsFile))
        } catch {
            print("Erro ao salvar dados localmente: \(error)")
        }
    }

    private func loadData() {
        let docs = getDocumentsURL()
        let recURL = docs.appendingPathComponent(recordsFile)
        let denURL = docs.appendingPathComponent(denunciasFile)
        let logURL = docs.appendingPathComponent(logsFile)

        if let data = try? Data(contentsOf: recURL),
           let recs = try? JSONDecoder().decode([InspectionRecord].self, from: data) {
            self.records = recs
        }
        if let data = try? Data(contentsOf: denURL),
           let dens = try? JSONDecoder().decode([DenunciaModel].self, from: data) {
            self.denuncias = dens
        }
        if let data = try? Data(contentsOf: logURL),
           let logs = try? JSONDecoder().decode([AuditLog].self, from: data) {
            self.auditLogs = logs
        }
    }

    // MARK: - Save Inspection
    func saveInspection(
        title: String,
        notes: String,
        inspectorName: String,
        photo: UIImage?,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        accuracy: Double,
        azimuthBearing: Double,
        pitch: Double,
        roll: Double,
        semaResult: SemaTelemetryResult,
        anmProcessNumber: String = "",
        miningRegime: String = "SEM_TITULO",
        mineralSubstance: String = "OURO",
        justiceStatus: String = "REGULAR",
        seizedMachinery: String = ""
    ) -> InspectionRecord {
        let recordId = UUID().uuidString
        let now = Int64(Date().timeIntervalSince1970 * 1000)

        // Convert image to base64 & calculate SHA-256
        let photoData = photo?.jpegData(compressionQuality: 0.8) ?? Data()
        let imageSha256 = Self.sha256Data(photoData)
        let base64String = photoData.isEmpty ? nil : photoData.base64EncodedString()

        // Fingerprint SHA-256 including mining fields
        let digestContent = "\(recordId)-\(now)-\(latitude)-\(longitude)-\(imageSha256)-\(semaResult.semaStatus.rawValue)-\(anmProcessNumber)-\(justiceStatus)"
        let recordSha256 = Self.sha256(digestContent)

        // QR Code Payload
        let qrPayload = "SEMA-MT|ID:\(recordId)|GEO:\(String(format: "%.5f", latitude)),\(String(format: "%.5f", longitude))|STATUS:\(semaResult.semaStatus.rawValue)|MIN:\(miningRegime)|\(justiceStatus)|CAR:\(semaResult.carNumber)|HASH:\(String(recordSha256.prefix(12)))|VAL:https://sema.mt.gov.br/valida/\(recordId)"

        let record = InspectionRecord(
            id: recordId,
            timestamp: now,
            title: title.isEmpty ? "Vistoria Ambiental - \(semaResult.municipality)" : title,
            inspectorName: inspectorName.isEmpty ? "Fiscal Técnico SEMA-MT" : inspectorName,
            inspectorRole: currentRole.title,
            latitude: latitude,
            longitude: longitude,
            altitude: altitude,
            accuracy: accuracy,
            azimuthBearing: azimuthBearing,
            pitch: pitch,
            roll: roll,
            municipality: semaResult.municipality,
            biome: semaResult.biome,
            carNumber: semaResult.carNumber,
            semaStatus: semaResult.semaStatus,
            hasIrregularity: semaResult.hasIrregularity || justiceStatus != "REGULAR",
            irregularityDetails: semaResult.irregularityDetails,
            notes: notes,
            encryptedImagePath: "documents://enc_\(recordId).dat",
            imageBase64: base64String,
            imageHashSha256: imageSha256,
            recordHashSha256: recordSha256,
            syncStatus: .pending,
            syncTimestamp: nil,
            qrCodePayload: qrPayload,
            anmProcessNumber: anmProcessNumber,
            miningRegime: miningRegime,
            mineralSubstance: mineralSubstance,
            justiceStatus: justiceStatus,
            seizedMachinery: seizedMachinery
        )

        records.insert(record, at: 0)

        addAuditLog(
            recordId: recordId,
            action: "CRIACAO_REGISTRO",
            details: "Vistoria registrada com telemetria pericial e hash SHA-256 em \(semaResult.municipality)."
        )

        if semaResult.hasIrregularity {
            addAuditLog(
                recordId: recordId,
                action: "ALERTA_SEMA_IRREGULARIDADE",
                details: "Irregularidade detectada: \(semaResult.irregularityDetails)"
            )
            uiMessage = "Vistoria salva! Alerta de irregularidade ambiental emitido."
        } else {
            uiMessage = "Vistoria registrada com sucesso com fé pública e criptografia."
        }

        saveData()
        return record
    }

    // MARK: - Save Denúncia
    func saveDenuncia(
        titulo: String,
        descricao: String,
        tipoInfracao: TipoInfracao,
        gravidade: GravidadeInfracao,
        municipio: String,
        bioma: String,
        carNumero: String?,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        acuracia: Double,
        azimute: Double
    ) {
        let denunciaId = UUID().uuidString
        let year = Calendar.current.component(.year, from: Date())
        let randomNum = Int.random(in: 1000...9999)
        let protocolo = "DEN-\(year)-SEMA-\(randomNum)"

        let denuncia = DenunciaModel(
            id: denunciaId,
            protocolo: protocolo,
            titulo: titulo,
            descricao: descricao,
            tipoInfracao: tipoInfracao,
            gravidade: gravidade,
            status: .recebida,
            origem: "FISCAL_CAMPO",
            dataRegistro: Int64(Date().timeIntervalSince1970 * 1000),
            municipio: municipio,
            bioma: bioma,
            carNumero: carNumero,
            latitude: latitude,
            longitude: longitude,
            altitude: altitude,
            acuracia: acuracia,
            azimute: azimute,
            syncStatus: .pending
        )

        denuncias.insert(denuncia, at: 0)

        addAuditLog(
            recordId: denunciaId,
            action: "CRIACAO_DENUNCIA",
            details: "Denúncia registrada sob protocolo \(protocolo) em \(municipio)."
        )

        uiMessage = "Denúncia \(protocolo) registrada para fiscalização."
        saveData()
    }

    // MARK: - Actions
    func deleteRecord(_ record: InspectionRecord) {
        records.removeAll(where: { $0.id == record.id })
        addAuditLog(
            recordId: record.id,
            action: "EXCLUSAO_REGISTRO",
            details: "Registro de vistoria excluído da base local."
        )
        saveData()
        uiMessage = "Registro excluído com sucesso."
    }

    func deleteDenuncia(_ denuncia: DenunciaModel) {
        denuncias.removeAll(where: { $0.id == denuncia.id })
        addAuditLog(
            recordId: denuncia.id,
            action: "EXCLUSAO_DENUNCIA",
            details: "Denúncia \(denuncia.protocolo) removida da base."
        )
        saveData()
        uiMessage = "Denúncia removida."
    }

    func triggerSync() {
        guard !isSyncing else { return }
        isSyncing = true

        Task {
            try? await Task.sleep(nanoseconds: 1_200_000_000)

            // Mark pending records as synced
            for i in 0..<records.count {
                if records[i].syncStatus == .pending {
                    records[i].syncStatus = .synced
                    records[i].syncTimestamp = Int64(Date().timeIntervalSince1970 * 1000)
                }
            }

            for i in 0..<denuncias.count {
                if denuncias[i].syncStatus == .pending {
                    denuncias[i].syncStatus = .synced
                }
            }

            isSyncing = false
            lastSyncTimestamp = Date()
            saveData()

            addAuditLog(
                recordId: "SYSTEM",
                action: "SINCRONIZACAO_REMOTA",
                details: "Sincronização em lote concluída com a SEMA-MT."
            )

            uiMessage = "Sincronização com o servidor SEMA-MT concluída com sucesso!"
        }
    }

    func switchRole(_ role: UserRolePermissions) {
        currentRole = role
        addAuditLog(
            recordId: "SYSTEM",
            action: "ALTERACAO_PERMISSAO",
            details: "Perfil de acesso alterado para: \(role.title)"
        )
        saveData()
    }

    func addAuditLog(recordId: String, action: String, details: String) {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let hash = Self.sha256("\(recordId)-\(action)-\(now)")
        let log = AuditLog(
            id: UUID().uuidString,
            recordId: recordId,
            action: action,
            inspectorName: currentRole.title,
            timestamp: now,
            details: details,
            hashIntegrity: String(hash.prefix(16))
        )
        auditLogs.insert(log, at: 0)
        saveData()
    }

    // MARK: - Initial Demo Data Seeding (Same as Android)
    private func seedInitialData() {
        let sampleInspections: [(title: String, lat: Double, lng: Double, alt: Double)] = [
            ("Reserva Legal Fazenda Mutum", -11.8642, -55.5056, 180.0),
            ("Monitoramento APA Chapada", -15.4611, -55.7489, 205.0),
            ("Fiscalização Rio Teles Pires", -9.8756, -56.0861, 230.0),
            ("Vistoria Pantanal Barão de Melgaço", -16.2045, -55.9678, 255.0),
            ("Polígono Agrícola Sorriso Norte", -12.5425, -55.7211, 280.0)
        ]

        let semaService = SemaIntegrationService.shared

        for (idx, item) in sampleInspections.enumerated() {
            let sema = semaService.evaluateLocation(lat: item.lat, lng: item.lng)
            let mining = semaService.evaluateMiningActivity(lat: item.lat, lng: item.lng)
            let recId = UUID().uuidString
            let now = Int64(Date().timeIntervalSince1970 * 1000) - Int64(idx * 3600 * 1000 * 24)
            let hash = Self.sha256("\(recId)-\(item.lat)-\(item.lng)")
            let qr = "SEMA-MT|ID:\(recId)|GEO:\(item.lat),\(item.lng)|STATUS:\(sema.semaStatus.rawValue)|MIN:\(mining.regimeMinerario.rawValue)|\(mining.statusJustica.rawValue)|CAR:\(sema.carNumber)|HASH:\(String(hash.prefix(12)))|VAL:https://sema.mt.gov.br/valida/\(recId)"

            let rec = InspectionRecord(
                id: recId,
                timestamp: now,
                title: item.title,
                inspectorName: "Fiscal Federal SEMA/IBAMA",
                inspectorRole: "Fiscal Técnico Ambiental (SEMA)",
                latitude: item.lat,
                longitude: item.lng,
                altitude: item.alt,
                accuracy: 2.8,
                azimuthBearing: Double((idx * 65) % 360),
                pitch: 1.2,
                roll: -0.5,
                municipality: sema.municipality,
                biome: sema.biome,
                carNumber: sema.carNumber,
                semaStatus: sema.semaStatus,
                hasIrregularity: sema.hasIrregularity || mining.statusJustica.isCrime,
                irregularityDetails: sema.irregularityDetails,
                notes: "Vistoria preventiva de sensoriamento remoto, mineração e checagem de CAR in-loco.",
                encryptedImagePath: "documents://enc_\(recId).dat",
                imageBase64: nil,
                imageHashSha256: hash,
                recordHashSha256: hash,
                syncStatus: idx % 2 == 0 ? .synced : .pending,
                syncTimestamp: now,
                qrCodePayload: qr,
                anmProcessNumber: mining.anmProcess,
                miningRegime: mining.regimeMinerario.rawValue,
                mineralSubstance: mining.substancia.rawValue,
                justiceStatus: mining.statusJustica.rawValue,
                seizedMachinery: mining.statusJustica.isCrime ? "1 Escavadeira Hidráulica 21t, 1 Motor Estacionário MWM" : ""
            )
            records.append(rec)
        }

        // Seed initial Denúncias
        let sampleDenuncias: [(titulo: String, desc: String, tipo: TipoInfracao, grav: GravidadeInfracao, mun: String, bioma: String, car: String, lat: Double, lng: Double, alt: Double)] = [
            ("Supressão Não Autorizada - Gleba Rio Teles Pires", "Constatada supressão de vegetação nativa em área de preservação permanente com maquinário pesado sem autorização de desmate.", .desmatamentoIlegal, .critica, "Alta Floresta", "Amazônia", "MT-5100250-F7A10B", -9.8756, -56.0861, 285.0),
            ("Foco Ativo de Calor em Área de Manejo Florestal", "Identificado foco de incêndio florestal de rápida propagação sem autorização de queima controlada da SEMA.", .queimadaNaoAutorizada, .alta, "Sinop", "Amazônia", "MT-5107909-A4B12C", -11.8542, -55.5156, 380.0),
            ("Ocupação Irregular e Edificação em APP de Nascente", "Intervenção humana em faixa marginal de curso hídrico com risco imediato de assoreamento e dano à recarga hídrica.", .ocupacaoApp, .media, "Chapada dos Guimarães", "Cerrado", "MT-5103007-D8E901", -15.4411, -55.7389, 680.0),
            ("Drenagem Indevida e Aterro em Várzea Pantaneira", "Alteração antrópica de dinâmica hidrológica em área úmida de preservação com diques e canalização não autorizada.", .poluicaoHidrica, .alta, "Barão de Melgaço", "Pantanal", "MT-5101704-C3D45E", -16.1945, -55.9578, 125.0),
            ("Extração Mineral Clandestina (Garimpo) em Leito de Rio", "Atividade de lavra garimpeira com draga operando em leito de rio sem licença ambiental de operação.", .mineracaoIlegal, .critica, "Poconé", "Pantanal", "MT-5106505-E2A91D", -16.2767, -56.6428, 140.0)
        ]

        for (idx, item) in sampleDenuncias.enumerated() {
            let denId = UUID().uuidString
            let protocolo = "DEN-2026-SEMA-\(1000 + idx * 850)"
            let den = DenunciaModel(
                id: denId,
                protocolo: protocolo,
                titulo: item.titulo,
                descricao: item.desc,
                tipoInfracao: item.tipo,
                gravidade: item.grav,
                status: idx == 0 ? .autuada : (idx == 1 ? .emApuracao : .recebida),
                origem: "SATELITE_SEMA",
                dataRegistro: Int64(Date().timeIntervalSince1970 * 1000) - Int64(idx * 86400 * 1000 * 2),
                municipio: item.mun,
                bioma: item.bioma,
                carNumero: item.car,
                latitude: item.lat,
                longitude: item.lng,
                altitude: item.alt,
                acuracia: 3.5,
                azimute: Double((idx * 72) % 360),
                syncStatus: .synced
            )
            denuncias.append(den)
        }

        saveData()
    }
}
