import SwiftUI

struct QrCodeSheet: View {
    let record: InspectionRecord
    @Environment(\.dismiss) private var dismiss
    @State private var copied = false

    private var qrImage: UIImage? {
        ExportService.shared.generateQrCode(from: record.qrCodePayload)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Text("QR Code Pericial SEMA-MT")
                    .font(.system(size: 16, weight: .bold))

                if let img = qrImage {
                    Image(uiImage: img)
                        .interpolation(.none)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 220, height: 220)
                        .padding(12)
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(radius: 4)
                }

                VStack(spacing: 8) {
                    Text("Cadeia de Custódia Digital")
                        .font(.system(size: 13, weight: .bold))

                    Text("Hash SHA-256: \(String(record.recordHashSha256.prefix(24)))...")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundColor(.secondary)

                    Text("Validação online: https://sema.mt.gov.br/valida/\(record.id.prefix(8))")
                        .font(.system(size: 10))
                        .foregroundColor(Color("ForestGreen"))
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(12)

                Button(action: {
                    UIPasteboard.general.string = record.qrCodePayload
                    copied = true
                }) {
                    Label(copied ? "Copiado!" : "Copiar Payload do QR", systemImage: copied ? "checkmark" : "doc.on.doc")
                        .font(.system(size: 13, weight: .semibold))
                }
                .buttonStyle(.bordered)
                .tint(Color("ForestGreen"))

                Spacer()
            }
            .padding()
            .navigationTitle("Autenticidade")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Fechar") { dismiss() }
                }
            }
        }
    }
}
