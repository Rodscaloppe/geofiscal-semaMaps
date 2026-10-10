import SwiftUI

struct RecordDetailSheet: View {
    let record: InspectionRecord
    @ObservedObject var store: InspectionStore

    @Environment(\.dismiss) private var dismiss
    @State private var showingQrSheet = false
    @State private var pdfShareURL: URL? = nil
    @State private var showingPdfShare = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // Title & Date Header
                    VStack(alignment: .leading, spacing: 4) {
                        Text(record.title)
                            .font(.system(size: 18, weight: .black))
                        Text("\(record.formattedDate) • Fiscal: \(record.inspectorName)")
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                    }

                    // Image Preview if available
                    if let base64 = record.imageBase64,
                       let data = Data(base64Encoded: base64),
                       let uiImage = UIImage(data: data) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFill()
                            .frame(height: 220)
                            .clipped()
                            .cornerRadius(12)
                    } else {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(UIColor.secondarySystemBackground))
                                .frame(height: 140)
                            VStack(spacing: 6) {
                                Image(systemName: "photo.badge.checkmark.fill")
                                    .font(.title)
                                    .foregroundColor(Color("ForestGreen"))
                                Text("Foto pericial criptografada (AES-256-CBC)")
                                    .font(.system(size: 11, weight: .semibold))
                                    .foregroundColor(.secondary)
                            }
                        }
                    }

                    // Environmental Evaluation Card
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(record.hasIrregularity ? "ALERTA AMBIENTAL DETECTADO" : "CONFORMIDADE AMBIENTAL REGULAR")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(record.semaStatus.color)
                            Spacer()
                            Text(record.biome)
                                .font(.system(size: 10, weight: .bold))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(record.semaStatus.color.opacity(0.15))
                                .cornerRadius(6)
                        }

                        Text(record.irregularityDetails)
                            .font(.system(size: 12))

                        Divider()

                        detailField(label: "Cadastro Ambiental Rural (CAR)", value: record.carNumber)
                        detailField(label: "Município", value: "\(record.municipality) - MT")
                        detailField(label: "Status de Sincronização", value: record.syncStatus.label)
                    }
                    .padding()
                    .background(record.semaStatus.color.opacity(0.08))
                    .cornerRadius(12)

                    // Sensor & Geolocation Card
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Geolocalização & Telemetria")
                            .font(.system(size: 13, weight: .bold))

                        detailField(label: "Coordenadas", value: "\(String(format: "%.6f", record.latitude))°, \(String(format: "%.6f", record.longitude))°")
                        detailField(label: "Altitude", value: "\(String(format: "%.1f", record.altitude)) metros")
                        detailField(label: "Precisão GPS", value: "±\(String(format: "%.1f", record.accuracy)) metros")
                        detailField(label: "Azimute da Câmera", value: "\(String(format: "%.1f", record.azimuthBearing))°")
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)

                    // Mining & ANM Specialized Section
                    if !record.anmProcessNumber.isEmpty || record.miningRegime != "SEM_TITULO" || record.justiceStatus != "REGULAR" || !record.seizedMachinery.isEmpty {
                        let isCrime = record.justiceStatus != "REGULAR"
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Image(systemName: isCrime ? "exclamationmark.triangle.fill" : "shield.lefthalf.filled")
                                    .foregroundColor(isCrime ? .red : Color("ForestGreen"))
                                Text("Perícia de Mineração & ANM")
                                    .font(.system(size: 13, weight: .bold))
                                Spacer()
                                Text(record.justiceStatus)
                                    .font(.system(size: 10, weight: .heavy))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(isCrime ? Color.red : Color.green)
                                    .cornerRadius(6)
                            }

                            Divider()

                            detailField(label: "Processo ANM", value: record.anmProcessNumber.isEmpty ? "Sem processo cadastrado" : record.anmProcessNumber)
                            detailField(label: "Regime Minerário", value: record.miningRegime)
                            detailField(label: "Substância Mineral", value: record.mineralSubstance)
                            detailField(label: "Justiça Ambiental", value: record.justiceStatus)
                            if !record.seizedMachinery.isEmpty {
                                detailField(label: "Maquinários Apreendidos", value: record.seizedMachinery)
                            }
                        }
                        .padding()
                        .background(isCrime ? Color.red.opacity(0.08) : Color.green.opacity(0.08))
                        .cornerRadius(12)
                    }

                    // Notes
                    if !record.notes.isEmpty {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Parecer do Fiscal")
                                .font(.system(size: 13, weight: .bold))
                            Text(record.notes)
                                .font(.system(size: 12))
                                .foregroundColor(.primary)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(12)
                    }

                    // Cryptographic Chain of Custody Card
                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Image(systemName: "checkmark.shield.fill")
                                .foregroundColor(Color("ForestGreen"))
                            Text("Cadeia de Custódia (SHA-256)")
                                .font(.system(size: 12, weight: .bold))
                        }
                        Text(record.recordHashSha256)
                            .font(.system(size: 9, design: .monospaced))
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)

                    // Actions
                    HStack(spacing: 12) {
                        Button(action: {
                            if let url = ExportService.shared.generateInspectionPdf(record: record) {
                                pdfShareURL = url
                                showingPdfShare = true
                            }
                        }) {
                            Label("Exportar Laudo PDF", systemImage: "doc.richtext.fill")
                                .font(.system(size: 13, weight: .bold))
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(Color("ForestGreen"))

                        Button(action: {
                            showingQrSheet = true
                        }) {
                            Label("QR Code", systemImage: "qrcode")
                                .font(.system(size: 13, weight: .bold))
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                        .tint(Color("ForestGreen"))
                    }
                    .padding(.top, 8)
                }
                .padding()
            }
            .navigationTitle("Detalhes da Vistoria")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Fechar") { dismiss() }
                }
            }
            .sheet(isPresented: $showingQrSheet) {
                QrCodeSheet(record: record)
            }
            .sheet(isPresented: $showingPdfShare) {
                if let url = pdfShareURL {
                    ShareSheet(items: [url])
                }
            }
        }
    }

    private func detailField(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 11))
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.system(size: 12, weight: .semibold))
        }
    }
}
