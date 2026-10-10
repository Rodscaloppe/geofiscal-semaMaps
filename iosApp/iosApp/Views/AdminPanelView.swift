import SwiftUI

struct AdminPanelView: View {
    @ObservedObject var store: InspectionStore

    @State private var serverUrl = "https://api.sema.mt.gov.br/v2/geofiscal"
    @State private var forceOffline = false
    @State private var simulateMockGps = false

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Banner
                HStack {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("PAINEL DE ADMINISTRAÇÃO & AUDITORIA")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(red: 0xC7/255.0, green: 0xF9/255.0, blue: 0xDC/255.0))
                        Text("Controle de Perfis, Segurança e Logs")
                            .font(.system(size: 15, weight: .black))
                            .foregroundColor(.white)
                    }
                    Spacer()
                    Image(systemName: "person.badge.shield.checkmark.fill")
                        .font(.title)
                        .foregroundColor(Color(red: 0x2E/255.0, green: 0xE5/255.0, blue: 0x9D/255.0))
                }
                .padding()
                .background(Color("ForestGreen"))
                .cornerRadius(14)

                // Role Switcher Card
                VStack(alignment: .leading, spacing: 12) {
                    Text("Perfil do Operador Atual")
                        .font(.system(size: 13, weight: .bold))

                    Picker("Perfil", selection: Binding(
                        get: { store.currentRole },
                        set: { store.switchRole($0) }
                    )) {
                        ForEach(UserRolePermissions.allRoles) { role in
                            Text(role.title).tag(role)
                        }
                    }
                    .pickerStyle(.menu)
                    .padding(8)
                    .background(Color(UIColor.tertiarySystemBackground))
                    .cornerRadius(8)

                    Divider()

                    // Permissions Checklist
                    Text("Matriz de Permissões do Perfil")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.secondary)

                    VStack(spacing: 6) {
                        permissionRow("Aprovar Vistorias e Autos", granted: store.currentRole.canApprove)
                        permissionRow("Exportar Laudos em PDF Oficial", granted: store.currentRole.canExportPdf)
                        permissionRow("Exportar Planilhas Gerenciais (CSV)", granted: store.currentRole.canExportExcel)
                        permissionRow("Configurar Endpoints da API", granted: store.currentRole.canChangeApi)
                        permissionRow("Sincronização em Lote", granted: store.currentRole.canBatchSync)
                        permissionRow("Editar Registros Locais", granted: store.currentRole.canEditRecords)
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Cryptographic Security Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(Color("ForestGreen"))
                        Text("Criptografia e Fé Pública")
                            .font(.system(size: 13, weight: .bold))
                    }

                    VStack(spacing: 6) {
                        cryptoInfoRow(label: "Algoritmo de Fotos", value: "AES-256-CBC")
                        cryptoInfoRow(label: "Integridade de Cadeia", value: "HMAC-SHA256")
                        cryptoInfoRow(label: "Selo de Fé Pública", value: "Validação Gov.br / SEMA")
                        cryptoInfoRow(label: "Detecção Anti-Fraude", value: "Hardware GPS Autêntico")
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // System & Network Settings
                VStack(alignment: .leading, spacing: 12) {
                    Text("Configurações do Sistema")
                        .font(.system(size: 13, weight: .bold))

                    Toggle("Modo Offline Forçado (Simulação Campo)", isOn: Binding(
                        get: { !store.isOnline },
                        set: { store.isOnline = !$0 }
                    ))
                    .font(.system(size: 13))

                    Toggle("Simular Alerta de Mock GPS", isOn: $simulateMockGps)
                        .font(.system(size: 13))

                    VStack(alignment: .leading, spacing: 4) {
                        Text("URL do Endpoint REST SEMA-MT")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.secondary)
                        TextField("Endpoint", text: $serverUrl)
                            .font(.system(size: 12, design: .monospaced))
                            .textFieldStyle(.roundedBorder)
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Audit Log Trail (Trilha de Auditoria)
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Image(systemName: "list.bullet.rectangle.portrait.fill")
                            .foregroundColor(Color("ForestGreen"))
                        Text("Trilha de Auditoria Pericial (\(store.auditLogs.count))")
                            .font(.system(size: 13, weight: .bold))
                    }

                    ForEach(store.auditLogs.prefix(6)) { log in
                        VStack(alignment: .leading, spacing: 3) {
                            HStack {
                                Text(log.action)
                                    .font(.system(size: 10, weight: .black, design: .monospaced))
                                    .foregroundColor(Color("ForestGreen"))
                                Spacer()
                                Text(log.formattedDate)
                                    .font(.system(size: 9))
                                    .foregroundColor(.secondary)
                            }
                            Text(log.details)
                                .font(.system(size: 11))
                            HStack {
                                Text("Fiscal: \(log.inspectorName)")
                                    .font(.system(size: 9))
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("Hash: \(log.hashIntegrity)")
                                    .font(.system(size: 8, design: .monospaced))
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding(8)
                        .background(Color(UIColor.tertiarySystemBackground))
                        .cornerRadius(8)
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)
            }
            .padding()
        }
        .navigationTitle("Admin")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func permissionRow(_ text: String, granted: Bool) -> some View {
        HStack(spacing: 8) {
            Image(systemName: granted ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(granted ? .green : .red)
                .font(.system(size: 13))
            Text(text)
                .font(.system(size: 12))
            Spacer()
        }
    }

    private func cryptoInfoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .font(.system(size: 12, weight: .bold, design: .monospaced))
                .foregroundColor(Color("ForestGreen"))
        }
    }
}
