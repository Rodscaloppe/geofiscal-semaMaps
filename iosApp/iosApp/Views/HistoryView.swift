import SwiftUI

struct HistoryView: View {
    @ObservedObject var store: InspectionStore

    @State private var selectedSection = 0 // 0 = Vistorias, 1 = Denúncias
    @State private var searchText = ""
    @State private var selectedFilter = "TODOS"

    @State private var selectedRecord: InspectionRecord? = nil
    @State private var qrRecord: InspectionRecord? = nil
    @State private var pdfShareURL: URL? = nil
    @State private var showingPdfShare = false

    private let filterOptions = ["TODOS", "IRREGULAR", "REGULAR", "PENDENTE SYNC"]

    private var filteredRecords: [InspectionRecord] {
        store.records.filter { record in
            let matchesSearch = searchText.isEmpty ||
                record.title.localizedCaseInsensitiveContains(searchText) ||
                record.municipality.localizedCaseInsensitiveContains(searchText) ||
                record.carNumber.localizedCaseInsensitiveContains(searchText)

            let matchesFilter: Bool
            switch selectedFilter {
            case "IRREGULAR": matchesFilter = record.hasIrregularity
            case "REGULAR": matchesFilter = !record.hasIrregularity
            case "PENDENTE SYNC": matchesFilter = record.syncStatus == .pending
            default: matchesFilter = true
            }

            return matchesSearch && matchesFilter
        }
    }

    private var filteredDenuncias: [DenunciaModel] {
        store.denuncias.filter { d in
            searchText.isEmpty ||
                d.titulo.localizedCaseInsensitiveContains(searchText) ||
                d.protocolo.localizedCaseInsensitiveContains(searchText) ||
                d.municipio.localizedCaseInsensitiveContains(searchText)
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Section Switcher: Vistorias vs Denúncias
            Picker("Seção", selection: $selectedSection) {
                Text("Vistorias (\(store.records.count))").tag(0)
                Text("Denúncias (\(store.denuncias.count))").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
            .padding(.top, 10)
            .padding(.bottom, 8)

            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("Buscar por título, município ou CAR...", text: $searchText)
                    .textFieldStyle(.plain)
                if !searchText.isEmpty {
                    Button(action: { searchText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(8)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .padding(.horizontal)
            .padding(.bottom, 8)

            if selectedSection == 0 {
                // Filter Chips for Inspections
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(filterOptions, id: \.self) { filter in
                            Button(action: { selectedFilter = filter }) {
                                Text(filter)
                                    .font(.system(size: 11, weight: .semibold))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(selectedFilter == filter ? Color("ForestGreen") : Color(UIColor.secondarySystemBackground))
                                    .foregroundColor(selectedFilter == filter ? .white : .primary)
                                    .cornerRadius(16)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 8)

                // Inspections List
                if filteredRecords.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "doc.text.magnifyingglass")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                        Text("Nenhum registro de vistoria encontrado")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        Text("Tente alterar os filtros ou realizar uma nova captura.")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(filteredRecords) { record in
                            InspectionRowView(
                                record: record,
                                onSelect: { selectedRecord = record },
                                onPdf: {
                                    if let url = ExportService.shared.generateInspectionPdf(record: record) {
                                        pdfShareURL = url
                                        showingPdfShare = true
                                    }
                                },
                                onQr: { qrRecord = record },
                                onDelete: { store.deleteRecord(record) }
                            )
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(.plain)
                }
            } else {
                // Denúncias List
                if filteredDenuncias.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "exclamationmark.bubble")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                        Text("Nenhuma denúncia cadastrada")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(filteredDenuncias) { denuncia in
                            DenunciaRowView(
                                denuncia: denuncia,
                                onDelete: { store.deleteDenuncia(denuncia) }
                            )
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(.plain)
                }
            }
        }
        .navigationTitle("Histórico de Campo")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $selectedRecord) { record in
            RecordDetailSheet(record: record, store: store)
        }
        .sheet(item: $qrRecord) { record in
            QrCodeSheet(record: record)
        }
        .sheet(isPresented: $showingPdfShare) {
            if let url = pdfShareURL {
                ShareSheet(items: [url])
            }
        }
    }
}

// MARK: - Inspection Row View
struct InspectionRowView: View {
    let record: InspectionRecord
    let onSelect: () -> Void
    let onPdf: () -> Void
    let onQr: () -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(record.title)
                        .font(.system(size: 14, weight: .bold))
                        .lineLimit(2)
                    Text(record.formattedDate)
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)
                }
                Spacer()

                // Irregularity / Status Badge
                HStack(spacing: 4) {
                    Circle()
                        .fill(record.semaStatus.color)
                        .frame(width: 8, height: 8)
                    Text(record.hasIrregularity ? "ALERTA" : "REGULAR")
                        .font(.system(size: 10, weight: .heavy))
                        .foregroundColor(record.semaStatus.color)
                }
                .padding(.horizontal, 6)
                .padding(.vertical, 3)
                .background(record.semaStatus.color.opacity(0.12))
                .cornerRadius(6)
            }

            // Badges Row
            HStack(spacing: 6) {
                badgeView(icon: "mappin.circle.fill", text: record.municipality)
                badgeView(icon: "leaf.fill", text: record.biome)
                badgeView(icon: "doc.plaintext", text: record.carNumber)
            }

            // Geolocation & Sync Status
            HStack {
                Text(String(format: "%.4f, %.4f (±%.1fm)", record.latitude, record.longitude, record.accuracy))
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(.secondary)

                Spacer()

                // Sync status
                HStack(spacing: 3) {
                    Image(systemName: record.syncStatus == .synced ? "cloud.fill" : "cloud.slash.fill")
                        .font(.system(size: 10))
                    Text(record.syncStatus.label)
                        .font(.system(size: 10, weight: .semibold))
                }
                .foregroundColor(record.syncStatus.color)
            }

            Divider()

            // Row Action Buttons
            HStack(spacing: 12) {
                Button(action: onSelect) {
                    Label("Detalhes", systemImage: "info.circle")
                        .font(.system(size: 12, weight: .medium))
                }
                .foregroundColor(Color("ForestGreen"))

                Spacer()

                Button(action: onPdf) {
                    Image(systemName: "doc.richtext")
                        .font(.system(size: 14))
                }
                .foregroundColor(.primary)

                Button(action: onQr) {
                    Image(systemName: "qrcode")
                        .font(.system(size: 14))
                }
                .foregroundColor(.primary)

                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 14))
                        .foregroundColor(.red)
                }
            }
        }
        .padding(12)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(record.hasIrregularity ? Color.red.opacity(0.25) : Color.clear, lineWidth: 1)
        )
    }

    private func badgeView(icon: String, text: String) -> some View {
        HStack(spacing: 3) {
            Image(systemName: icon)
                .font(.system(size: 9))
            Text(text)
                .font(.system(size: 10, weight: .medium))
        }
        .padding(.horizontal, 6)
        .padding(.vertical, 2)
        .background(Color(UIColor.tertiarySystemBackground))
        .cornerRadius(4)
        .foregroundColor(.secondary)
    }
}

// MARK: - Denúncia Row View
struct DenunciaRowView: View {
    let denuncia: DenunciaModel
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(denuncia.protocolo)
                    .font(.system(size: 12, weight: .heavy, design: .monospaced))
                    .foregroundColor(Color("ForestGreen"))
                Spacer()
                Text(denuncia.gravidade.label)
                    .font(.system(size: 10, weight: .bold))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(denuncia.gravidade.color.opacity(0.15))
                    .foregroundColor(denuncia.gravidade.color)
                    .cornerRadius(4)
            }

            Text(denuncia.titulo)
                .font(.system(size: 13, weight: .bold))
            Text(denuncia.descricao)
                .font(.system(size: 11))
                .foregroundColor(.secondary)
                .lineLimit(2)

            HStack {
                Text("\(denuncia.municipio) • \(denuncia.bioma)")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.secondary)
                Spacer()
                Text(denuncia.status.label)
                    .font(.system(size: 10, weight: .semibold))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.blue.opacity(0.12))
                    .foregroundColor(.blue)
                    .cornerRadius(4)

                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 12))
                        .foregroundColor(.red)
                }
            }
        }
        .padding(12)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}

// MARK: - UIActivityViewController Bridge for Sharing PDF
struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) { }
}
