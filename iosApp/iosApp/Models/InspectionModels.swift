import Foundation
import SwiftUI

// MARK: - SEMA Status
enum SemaStatus: String, Codable, CaseIterable {
    case regular = "REGULAR"
    case sobEmbargo = "SOB_EMBARGO"
    case desmatamentoDetectado = "DESMATAMENTO_DETECTADO"
    case focoQueimada = "FOCO_QUEIMADA"
    case pendenciaLicenca = "PENDENCIA_LICENCA"

    var label: String {
        switch self {
        case .regular: return "Regular SEMA-MT"
        case .sobEmbargo: return "Área Embargada SEMA-MT"
        case .desmatamentoDetectado: return "Alerta PRODES/DETER - Supressão Não Autorizada"
        case .focoQueimada: return "Foco de Calor Ativo (INPE/SEMA)"
        case .pendenciaLicenca: return "Licença Ambiental Suspensa / Vencida"
        }
    }

    var isIrregular: Bool {
        return self != .regular
    }

    var color: Color {
        switch self {
        case .regular: return Color.green
        case .sobEmbargo: return Color.red
        case .desmatamentoDetectado: return Color.red
        case .focoQueimada: return Color.orange
        case .pendenciaLicenca: return Color.yellow
        }
    }
}

// MARK: - Sync Status
enum SyncStatus: String, Codable, CaseIterable {
    case pending = "PENDING"
    case syncing = "SYNCING"
    case synced = "SYNCED"
    case failed = "FAILED"

    var label: String {
        switch self {
        case .pending: return "Pendente"
        case .syncing: return "Sincronizando..."
        case .synced: return "Sincronizado"
        case .failed: return "Falha"
        }
    }

    var color: Color {
        switch self {
        case .pending: return Color.orange
        case .syncing: return Color.blue
        case .synced: return Color.green
        case .failed: return Color.red
        }
    }
}

// MARK: - Mining Enums & Models
enum RegimeMinerario: String, Codable, CaseIterable {
    case plg = "PLG"
    case concessaoLavra = "CONCESSAO_LAVRA"
    case guiaUtilizacao = "GUIA_UTILIZACAO"
    case autorizacaoPesquisa = "AUTORIZACAO_PESQUISA"
    case requerimentoLavra = "REQUERIMENTO_LAVRA"
    case semTitulo = "SEM_TITULO"

    var label: String {
        switch self {
        case .plg: return "Permissão de Lavra Garimpeira (PLG - ANM)"
        case .concessaoLavra: return "Concessão de Lavra Definitiva (ANM)"
        case .guiaUtilizacao: return "Guia de Utilização Prévia (ANM)"
        case .autorizacaoPesquisa: return "Autorização de Pesquisa (Extração PROIBIDA)"
        case .requerimentoLavra: return "Requerimento em Análise (Sem Título)"
        case .semTitulo: return "Clandestino / Sem Processo na ANM"
        }
    }

    var allowsExtraction: Bool {
        switch self {
        case .plg, .concessaoLavra, .guiaUtilizacao: return true
        case .autorizacaoPesquisa, .requerimentoLavra, .semTitulo: return false
        }
    }
}

enum SubstanciaMineral: String, Codable, CaseIterable {
    case ouro = "OURO"
    case cassiterita = "CASSITERITA"
    case diamante = "DIAMANTE"
    case manganes = "MANGANES"
    case areiaCascalho = "AREIA_CASCALHO"
    case outros = "OUTROS"

    var label: String {
        switch self {
        case .ouro: return "Ouro Aluvionar e Filão"
        case .cassiterita: return "Cassiterita / Estanho"
        case .diamante: return "Diamante em Aluvião"
        case .manganes: return "Manganês"
        case .areiaCascalho: return "Areia / Cascalho / Saibro"
        case .outros: return "Outras Substâncias Minerais"
        }
    }

    var symbol: String {
        switch self {
        case .ouro: return "Au"
        case .cassiterita: return "Sn"
        case .diamante: return "C"
        case .manganes: return "Mn"
        case .areiaCascalho: return "SiO2"
        case .outros: return "Min"
        }
    }
}

enum StatusJusticaAmbiental: String, Codable, CaseIterable {
    case regular = "REGULAR"
    case clandestinoCrimeFederal = "CLANDESTINO_CRIME_FEDERAL"
    case desvioPesquisa = "DESVIO_PESQUISA"
    case descumprimentoEmbargo = "DESCUMPRIMENTO_EMBARGO"
    case invasaoTiUc = "INVASAO_TI_UC"

    var label: String {
        switch self {
        case .regular: return "Conforme: Título ANM e LO SEMA Ativos"
        case .clandestinoCrimeFederal: return "CRIME FEDERAL: Usurpação de Patrimônio da União (Art. 2º Lei 8.176/91)"
        case .desvioPesquisa: return "IRREGULAR: Extração Ativa sob Guisa de Pesquisa"
        case .descumprimentoEmbargo: return "CRIME: Descumprimento de Termo de Embargo SEMA/IBAMA"
        case .invasaoTiUc: return "CRIME CONSTITUCIONAL: Invasão de Terra Indígena / UC (Art. 231 CF)"
        }
    }

    var isCrime: Bool {
        return self != .regular
    }

    var color: Color {
        switch self {
        case .regular: return Color.green
        case .clandestinoCrimeFederal: return Color.red
        case .desvioPesquisa: return Color.orange
        case .descumprimentoEmbargo: return Color.purple
        case .invasaoTiUc: return Color(red: 0.72, green: 0.11, blue: 0.11)
        }
    }
}

struct MineracaoTelemetryResult: Equatable {
    var anmProcess: String
    var titularProcesso: String
    var regimeMinerario: RegimeMinerario
    var substancia: SubstanciaMineral
    var statusJustica: StatusJusticaAmbiental
    var licencaOperacaoSema: String?
    var hasLicencaAtiva: Bool
    var isNearProtectedArea: Bool
    var protectedAreaName: String?
    var enquadramentoLegal: String
    var cavasAreaHectares: Double
    var alertaTurbidezHidrica: Bool
    var presencaMercurio: Bool
}

// MARK: - Inspection Record
struct InspectionRecord: Identifiable, Codable, Equatable {
    let id: String
    let timestamp: Int64
    var title: String
    var inspectorName: String
    var inspectorRole: String
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var accuracy: Double
    var azimuthBearing: Double
    var pitch: Double
    var roll: Double
    var municipality: String
    var biome: String // Amazônia, Cerrado, Pantanal
    var carNumber: String
    var semaStatus: SemaStatus
    var hasIrregularity: Bool
    var irregularityDetails: String
    var notes: String
    var encryptedImagePath: String
    var imageBase64: String?
    var imageHashSha256: String
    var recordHashSha256: String
    var syncStatus: SyncStatus
    var syncTimestamp: Int64?
    var qrCodePayload: String
    var anmProcessNumber: String
    var miningRegime: String
    var mineralSubstance: String
    var justiceStatus: String
    var seizedMachinery: String

    var formattedDate: String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000.0)
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        return formatter.string(from: date)
    }

    init(
        id: String,
        timestamp: Int64,
        title: String,
        inspectorName: String,
        inspectorRole: String,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        accuracy: Double,
        azimuthBearing: Double,
        pitch: Double,
        roll: Double,
        municipality: String,
        biome: String,
        carNumber: String,
        semaStatus: SemaStatus,
        hasIrregularity: Bool,
        irregularityDetails: String,
        notes: String,
        encryptedImagePath: String,
        imageBase64: String? = nil,
        imageHashSha256: String,
        recordHashSha256: String,
        syncStatus: SyncStatus,
        syncTimestamp: Int64? = nil,
        qrCodePayload: String,
        anmProcessNumber: String = "",
        miningRegime: String = "SEM_TITULO",
        mineralSubstance: String = "OURO",
        justiceStatus: String = "REGULAR",
        seizedMachinery: String = ""
    ) {
        self.id = id
        self.timestamp = timestamp
        self.title = title
        self.inspectorName = inspectorName
        self.inspectorRole = inspectorRole
        self.latitude = latitude
        self.longitude = longitude
        self.altitude = altitude
        self.accuracy = accuracy
        self.azimuthBearing = azimuthBearing
        self.pitch = pitch
        self.roll = roll
        self.municipality = municipality
        self.biome = biome
        self.carNumber = carNumber
        self.semaStatus = semaStatus
        self.hasIrregularity = hasIrregularity
        self.irregularityDetails = irregularityDetails
        self.notes = notes
        self.encryptedImagePath = encryptedImagePath
        self.imageBase64 = imageBase64
        self.imageHashSha256 = imageHashSha256
        self.recordHashSha256 = recordHashSha256
        self.syncStatus = syncStatus
        self.syncTimestamp = syncTimestamp
        self.qrCodePayload = qrCodePayload
        self.anmProcessNumber = anmProcessNumber
        self.miningRegime = miningRegime
        self.mineralSubstance = mineralSubstance
        self.justiceStatus = justiceStatus
        self.seizedMachinery = seizedMachinery
    }

    enum CodingKeys: String, CodingKey {
        case id, timestamp, title, inspectorName, inspectorRole
        case latitude, longitude, altitude, accuracy, azimuthBearing, pitch, roll
        case municipality, biome, carNumber, semaStatus, hasIrregularity, irregularityDetails
        case notes, encryptedImagePath, imageBase64, imageHashSha256, recordHashSha256
        case syncStatus, syncTimestamp, qrCodePayload
        case anmProcessNumber, miningRegime, mineralSubstance, justiceStatus, seizedMachinery
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        timestamp = try container.decode(Int64.self, forKey: .timestamp)
        title = try container.decode(String.self, forKey: .title)
        inspectorName = try container.decode(String.self, forKey: .inspectorName)
        inspectorRole = try container.decode(String.self, forKey: .inspectorRole)
        latitude = try container.decode(Double.self, forKey: .latitude)
        longitude = try container.decode(Double.self, forKey: .longitude)
        altitude = try container.decode(Double.self, forKey: .altitude)
        accuracy = try container.decode(Double.self, forKey: .accuracy)
        azimuthBearing = try container.decode(Double.self, forKey: .azimuthBearing)
        pitch = try container.decode(Double.self, forKey: .pitch)
        roll = try container.decode(Double.self, forKey: .roll)
        municipality = try container.decode(String.self, forKey: .municipality)
        biome = try container.decode(String.self, forKey: .biome)
        carNumber = try container.decode(String.self, forKey: .carNumber)
        semaStatus = try container.decode(SemaStatus.self, forKey: .semaStatus)
        hasIrregularity = try container.decode(Bool.self, forKey: .hasIrregularity)
        irregularityDetails = try container.decode(String.self, forKey: .irregularityDetails)
        notes = try container.decode(String.self, forKey: .notes)
        encryptedImagePath = try container.decode(String.self, forKey: .encryptedImagePath)
        imageBase64 = try container.decodeIfPresent(String.self, forKey: .imageBase64)
        imageHashSha256 = try container.decode(String.self, forKey: .imageHashSha256)
        recordHashSha256 = try container.decode(String.self, forKey: .recordHashSha256)
        syncStatus = try container.decode(SyncStatus.self, forKey: .syncStatus)
        syncTimestamp = try container.decodeIfPresent(Int64.self, forKey: .syncTimestamp)
        qrCodePayload = try container.decode(String.self, forKey: .qrCodePayload)
        anmProcessNumber = try container.decodeIfPresent(String.self, forKey: .anmProcessNumber) ?? ""
        miningRegime = try container.decodeIfPresent(String.self, forKey: .miningRegime) ?? "SEM_TITULO"
        mineralSubstance = try container.decodeIfPresent(String.self, forKey: .mineralSubstance) ?? "OURO"
        justiceStatus = try container.decodeIfPresent(String.self, forKey: .justiceStatus) ?? "REGULAR"
        seizedMachinery = try container.decodeIfPresent(String.self, forKey: .seizedMachinery) ?? ""
    }
}

// MARK: - Denúncia Enums
enum TipoInfracao: String, Codable, CaseIterable {
    case desmatamentoIlegal = "DESMATAMENTO_ILEGAL"
    case queimadaNaoAutorizada = "QUEIMADA_NAO_AUTORIZADA"
    case poluicaoHidrica = "POLUICAO_HIDRICA"
    case ocupacaoApp = "OCUPACAO_APP"
    case mineracaoIlegal = "MINERACAO_ILEGAL"
    case outros = "OUTROS"

    var label: String {
        switch self {
        case .desmatamentoIlegal: return "Supressão Vegetal Não Autorizada (DETER/PRODES)"
        case .queimadaNaoAutorizada: return "Uso Irregular do Fogo / Foco de Calor"
        case .poluicaoHidrica: return "Contaminação de Manancial / Rio"
        case .ocupacaoApp: return "Invasão ou Dano em APP de Nascente"
        case .mineracaoIlegal: return "Garimpo ou Extração Mineral Clandestina"
        case .outros: return "Outras Infrações Ambientais"
        }
    }

    var shortLabel: String {
        switch self {
        case .desmatamentoIlegal: return "Desmatamento"
        case .queimadaNaoAutorizada: return "Queimada Ilegal"
        case .poluicaoHidrica: return "Poluição Hídrica"
        case .ocupacaoApp: return "Dano em APP"
        case .mineracaoIlegal: return "Garimpo Ilegal"
        case .outros: return "Outros"
        }
    }
}

enum GravidadeInfracao: String, Codable, CaseIterable {
    case baixa = "BAIXA"
    case media = "MEDIA"
    case alta = "ALTA"
    case critica = "CRITICA"

    var label: String {
        switch self {
        case .baixa: return "Baixa"
        case .media: return "Média"
        case .alta: return "Alta"
        case .critica: return "Crítica (Embargo)"
        }
    }

    var color: Color {
        switch self {
        case .baixa: return Color.green
        case .media: return Color.yellow
        case .alta: return Color.orange
        case .critica: return Color.red
        }
    }
}

enum StatusDenuncia: String, Codable, CaseIterable {
    case recebida = "RECEBIDA"
    case emApuracao = "EM_APURACAO"
    case vistoriada = "VISTORIADA"
    case autuada = "AUTUADA"
    case arquivada = "ARQUIVADA"

    var label: String {
        switch self {
        case .recebida: return "Recebida na Triagem"
        case .emApuracao: return "Em Investigação"
        case .vistoriada: return "Vistoria Concluída"
        case .autuada: return "Auto de Infração"
        case .arquivada: return "Arquivada"
        }
    }
}

// MARK: - Denúncia Model
struct DenunciaModel: Identifiable, Codable, Equatable {
    let id: String
    let protocolo: String // Ex: DEN-2026-SEMA-9842
    var titulo: String
    var descricao: String
    var tipoInfracao: TipoInfracao
    var gravidade: GravidadeInfracao
    var status: StatusDenuncia
    var origem: String
    var dataRegistro: Int64
    var municipio: String
    var bioma: String
    var carNumero: String?
    var latitude: Double
    var longitude: Double
    var altitude: Double
    var acuracia: Double
    var azimute: Double
    var syncStatus: SyncStatus

    var formattedDate: String {
        let date = Date(timeIntervalSince1970: Double(dataRegistro) / 1000.0)
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        return formatter.string(from: date)
    }
}

// MARK: - Audit Log Model
struct AuditLog: Identifiable, Codable, Equatable {
    let id: String
    let recordId: String
    let action: String
    let inspectorName: String
    let timestamp: Int64
    let details: String
    let hashIntegrity: String

    var formattedDate: String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000.0)
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm:ss"
        return formatter.string(from: date)
    }
}

// MARK: - User Role & Permissions
struct UserRolePermissions: Identifiable, Codable, Equatable, Hashable {
    var id: String { roleName }
    let roleName: String
    let title: String
    let canApprove: Bool
    let canExportPdf: Bool
    let canExportExcel: Bool
    let canChangeApi: Bool
    let canBatchSync: Bool
    let canEditRecords: Bool

    static let admin = UserRolePermissions(
        roleName: "ADMIN",
        title: "Administrador Geral SEMA-MT",
        canApprove: true,
        canExportPdf: true,
        canExportExcel: true,
        canChangeApi: true,
        canBatchSync: true,
        canEditRecords: true
    )

    static let fiscalTecnico = UserRolePermissions(
        roleName: "FISCAL_TECNICO",
        title: "Fiscal Técnico Ambiental (SEMA)",
        canApprove: true,
        canExportPdf: true,
        canExportExcel: true,
        canChangeApi: false,
        canBatchSync: true,
        canEditRecords: true
    )

    static let inspetorCampo = UserRolePermissions(
        roleName: "INSPETOR_CAMPO",
        title: "Inspetor Operacional de Campo",
        canApprove: false,
        canExportPdf: true,
        canExportExcel: false,
        canChangeApi: false,
        canBatchSync: true,
        canEditRecords: false
    )

    static let auditorExterno = UserRolePermissions(
        roleName: "AUDITOR_EXTERNO",
        title: "Auditor Externo / Ministério Público",
        canApprove: false,
        canExportPdf: true,
        canExportExcel: true,
        canChangeApi: false,
        canBatchSync: false,
        canEditRecords: false
    )

    static let allRoles = [admin, fiscalTecnico, inspetorCampo, auditorExterno]
}
