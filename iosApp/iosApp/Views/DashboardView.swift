import SwiftUI

struct DashboardView: View {
    @ObservedObject var store: InspectionStore
    @ObservedObject var locationManager: LocationManager

    @State private var shareURL: URL? = nil
    @State private var showingShareSheet = false
    @State private var selectedRecord: InspectionRecord? = nil

    private var totalRecords: Int { store.records.count }
    private var irregularCount: Int { store.records.filter { $0.hasIrregularity }.count }
    private var regularCount: Int { totalRecords - irregularCount }
    private var complianceRate: Int {
        totalRecords > 0 ? Int((Double(regularCount) / Double(totalRecords)) * 100) : 100
    }
    private var pendingSyncCount: Int { store.records.filter { $0.syncStatus == .pending }.count }

    private var amazoniaCount: Int { store.records.filter { $0.biome == "Amazônia" }.count }
    private var cerradoCount: Int { store.records.filter { $0.biome == "Cerrado" }.count }
    private var pantanalCount: Int { store.records.filter { $0.biome == "Pantanal" }.count }

    private var criticalRecords: [InspectionRecord] {
        store.records.filter { $0.hasIrregularity }.prefix(3).map { $0 }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Card
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("PAINEL GERENCIAL DE FISCALIZAÇÃO")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(red: 0xC7/255.0, green: 0xF9/255.0, blue: 0xDC/255.0))
                        Text("SEMA-MT • Métricas e Inteligência Territorial")
                            .font(.system(size: 15, weight: .black))
                            .foregroundColor(.white)
                    }
                    Spacer()
                    Image(systemName: "chart.bar.xaxis")
                        .font(.title)
                        .foregroundColor(Color(red: 0x2E/255.0, green: 0xE5/255.0, blue: 0x9D/255.0))
                }
                .padding()
                .background(Color("ForestGreen"))
                .cornerRadius(14)

                // KPI 2x2 Grid
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    kpiCard(
                        title: "Total Vistorias",
                        value: "\(totalRecords)",
                        icon: "doc.text.fill",
                        color: Color("ForestGreen"),
                        caption: "\(store.denuncias.count) denúncias registradas"
                    )

                    // Compliance Rate with circular ring
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("Conformidade")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(.secondary)
                            Spacer()
                            ZStack {
                                Circle()
                                    .stroke(Color.secondary.opacity(0.2), lineWidth: 4)
                                Circle()
                                    .trim(from: 0, to: CGFloat(complianceRate) / 100.0)
                                    .stroke(Color.green, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                                    .rotationEffect(.degrees(-90))
                                Text("\(complianceRate)%")
                                    .font(.system(size: 9, weight: .heavy))
                            }
                            .frame(width: 32, height: 32)
                        }
                        Text("\(regularCount) Regulares")
                            .font(.system(size: 18, weight: .black))
                        Text("Índice de conformidade do CAR")
                            .font(.system(size: 9))
                            .foregroundColor(.secondary)
                    }
                    .padding(12)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(12)

                    kpiCard(
                        title: "Irregularidades",
                        value: "\(irregularCount)",
                        icon: "exclamationmark.triangle.fill",
                        color: .red,
                        caption: "\(irregularCount > 0 ? "Requer autuação imediata" : "Nenhum alerta crítico")"
                    )

                    kpiCard(
                        title: "Pendências Sync",
                        value: "\(pendingSyncCount)",
                        icon: "cloud.fill",
                        color: pendingSyncCount > 0 ? .orange : .green,
                        caption: pendingSyncCount > 0 ? "Pronto p/ envio à nuvem" : "Base sincronizada"
                    )
                }

                // Biome Distribution Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: "leaf.fill")
                            .foregroundColor(Color("ForestGreen"))
                        Text("Distribuição por Bioma (Mato Grosso)")
                            .font(.system(size: 13, weight: .bold))
                    }

                    biomeProgressBar(name: "Amazônia", count: amazoniaCount, total: totalRecords, color: Color.green)
                    biomeProgressBar(name: "Cerrado", count: cerradoCount, total: totalRecords, color: Color.orange)
                    biomeProgressBar(name: "Pantanal", count: pantanalCount, total: totalRecords, color: Color.blue)
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Report Export Actions
                VStack(alignment: .leading, spacing: 12) {
                    Text("Exportação de Relatórios Gerenciais")
                        .font(.system(size: 13, weight: .bold))

                    HStack(spacing: 12) {
                        Button(action: {
                            if let first = store.records.first,
                               let url = ExportService.shared.generateInspectionPdf(record: first) {
                                shareURL = url
                                showingShareSheet = true
                            }
                        }) {
                            Label("Laudo PDF", systemImage: "doc.richtext.fill")
                                .font(.system(size: 12, weight: .bold))
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(Color("ForestGreen"))

                        Button(action: {
                            if let url = ExportService.shared.generateCsvExport(records: store.records) {
                                shareURL = url
                                showingShareSheet = true
                            }
                        }) {
                            Label("Planilha Excel/CSV", systemImage: "tablecells.fill")
                                .font(.system(size: 12, weight: .bold))
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                        .tint(Color("ForestGreen"))
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Batch Sync Card
                VStack(spacing: 10) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Sincronização em Lote")
                                .font(.system(size: 13, weight: .bold))
                            Text(store.isSyncing ? "Enviando pacotes criptografados..." : "Servidor SEMA-MT: Online")
                                .font(.system(size: 11))
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        Image(systemName: store.isSyncing ? "arrow.triangle.2.circlepath" : "icloud.and.arrow.up.fill")
                            .font(.title2)
                            .foregroundColor(Color("ForestGreen"))
                    }

                    Button(action: {
                        store.triggerSync()
                    }) {
                        Text(store.isSyncing ? "Sincronizando..." : "Sincronizar Todas as Vistorias Pendentes")
                            .font(.system(size: 13, weight: .bold))
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(Color("ForestGreen"))
                    .disabled(store.isSyncing)
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Critical Irregular Records List
                if !criticalRecords.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Alertas de Irregularidade Recentes")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.red)

                        ForEach(criticalRecords) { rec in
                            Button(action: { selectedRecord = rec }) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(rec.title)
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(.primary)
                                        Text("\(rec.municipality) • \(rec.semaStatus.label)")
                                            .font(.system(size: 10))
                                            .foregroundColor(.red)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                                .padding(10)
                                .background(Color.red.opacity(0.08))
                                .cornerRadius(10)
                            }
                        }
                    }
                    .padding()
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(14)
                }
            }
            .padding()
        }
        .navigationTitle("Painel")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $showingShareSheet) {
            if let url = shareURL {
                ShareSheet(items: [url])
            }
        }
        .sheet(item: $selectedRecord) { rec in
            RecordDetailSheet(record: rec, store: store)
        }
    }

    private func kpiCard(title: String, value: String, icon: String, color: Color, caption: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(.secondary)
                Spacer()
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundColor(color)
            }
            Text(value)
                .font(.system(size: 22, weight: .black))
            Text(caption)
                .font(.system(size: 9))
                .foregroundColor(.secondary)
                .lineLimit(1)
        }
        .padding(12)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }

    private func biomeProgressBar(name: String, count: Int, total: Int, color: Color) -> some View {
        let pct = total > 0 ? Double(count) / Double(total) : 0.0
        return VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(name)
                    .font(.system(size: 12, weight: .semibold))
                Spacer()
                Text("\(count) (\(Int(pct * 100))%)")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundColor(.secondary)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color.secondary.opacity(0.15))
                        .frame(height: 8)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(color)
                        .frame(width: max(0, min(geo.size.width * CGFloat(pct), geo.size.width)), height: 8)
                }
            }
            .frame(height: 8)
        }
    }
}
