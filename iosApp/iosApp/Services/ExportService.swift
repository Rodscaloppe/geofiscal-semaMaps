import Foundation
import UIKit
import CoreImage.CIFilterBuiltins

class ExportService {
    static let shared = ExportService()

    // MARK: - QR Code Generation
    func generateQrCode(from string: String) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        guard let data = string.data(using: .utf8) else { return nil }
        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("Q", forKey: "inputCorrectionLevel")

        guard let ciImage = filter.outputImage else { return nil }
        let transform = CGAffineTransform(scaleX: 10, y: 10)
        let scaledImage = ciImage.transformed(by: transform)

        let context = CIContext()
        guard let cgImage = context.createCGImage(scaledImage, from: scaledImage.extent) else { return nil }
        return UIImage(cgImage: cgImage)
    }

    // MARK: - PDF Report Generation
    func generateInspectionPdf(record: InspectionRecord) -> URL? {
        let pdfMetaData = [
            kCGPDFContextCreator: "GeoFiscal SEMA-MT",
            kCGPDFContextAuthor: record.inspectorName,
            kCGPDFContextTitle: "Laudo de Fiscalização Ambiental - \(record.id)"
        ]
        let format = UIGraphicsPDFRendererFormat()
        format.documentInfo = pdfMetaData as [String: Any]

        let pageWidth: CGFloat = 595.2 // A4 width in points
        let pageHeight: CGFloat = 841.8 // A4 height in points
        let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)

        let renderer = UIGraphicsPDFRenderer(bounds: pageRect, format: format)

        let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent("Laudo_SEMA_\(record.id.prefix(8)).pdf")

        do {
            try renderer.writePDF(to: fileURL) { context in
                context.beginPage()

                var currentY: CGFloat = 40.0

                // Header Background
                let headerRect = CGRect(x: 36, y: currentY, width: pageWidth - 72, height: 65)
                let headerColor = UIColor(red: 27/255, green: 94/255, blue: 32/255, alpha: 1.0)
                headerColor.setFill()
                UIBezierPath(roundedRect: headerRect, cornerRadius: 8).fill()

                // Header Text
                let titleAttributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 16),
                    .foregroundColor: UIColor.white
                ]
                "ESTADO DE MATO GROSSO - SEMA/MT".draw(at: CGPoint(x: 50, y: currentY + 12), withAttributes: titleAttributes)

                let subAttributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 11),
                    .foregroundColor: UIColor(red: 199/255, green: 249/255, blue: 220/255, alpha: 1.0)
                ]
                "Secretaria de Estado de Meio Ambiente • Laudo Pericial Geoespacial".draw(at: CGPoint(x: 50, y: currentY + 36), withAttributes: subAttributes)

                currentY += 85

                // Document Title
                let docTitleAttributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 15),
                    .foregroundColor: UIColor.black
                ]
                record.title.draw(at: CGPoint(x: 36, y: currentY), withAttributes: docTitleAttributes)
                currentY += 25

                // Section: Identificação
                func drawSectionTitle(_ title: String) {
                    let secAttributes: [NSAttributedString.Key: Any] = [
                        .font: UIFont.boldSystemFont(ofSize: 12),
                        .foregroundColor: headerColor
                    ]
                    title.uppercased().draw(at: CGPoint(x: 36, y: currentY), withAttributes: secAttributes)
                    currentY += 16
                    let line = UIBezierPath()
                    line.move(to: CGPoint(x: 36, y: currentY))
                    line.addLine(to: CGPoint(x: pageWidth - 36, y: currentY))
                    UIColor.lightGray.setStroke()
                    line.lineWidth = 0.5
                    line.stroke()
                    currentY += 10
                }

                func drawField(label: String, value: String) {
                    let lblAttr: [NSAttributedString.Key: Any] = [
                        .font: UIFont.boldSystemFont(ofSize: 10),
                        .foregroundColor: UIColor.darkGray
                    ]
                    let valAttr: [NSAttributedString.Key: Any] = [
                        .font: UIFont.systemFont(ofSize: 10),
                        .foregroundColor: UIColor.black
                    ]
                    label.draw(at: CGPoint(x: 40, y: currentY), withAttributes: lblAttr)
                    value.draw(at: CGPoint(x: 200, y: currentY), withAttributes: valAttr)
                    currentY += 16
                }

                drawSectionTitle("1. Identificação da Vistoria")
                drawField(label: "Protocolo ID:", value: record.id)
                drawField(label: "Data / Hora:", value: record.formattedDate)
                drawField(label: "Fiscal Responsável:", value: "\(record.inspectorName) (\(record.inspectorRole))")
                drawField(label: "Status de Sync:", value: record.syncStatus.label)
                currentY += 10

                drawSectionTitle("2. Georreferenciamento e Telemetria de Sensores")
                drawField(label: "Coordenadas:", value: "\(String(format: "%.6f", record.latitude))°, \(String(format: "%.6f", record.longitude))°")
                drawField(label: "Altitude / Precisão GPS:", value: "\(String(format: "%.1f", record.altitude)) m (±\(String(format: "%.1f", record.accuracy)) m)")
                drawField(label: "Bússola / Azimute:", value: "\(String(format: "%.1f", record.azimuthBearing))°")
                drawField(label: "Município / Bioma:", value: "\(record.municipality) - MT | Bioma \(record.biome)")
                drawField(label: "Cadastro Ambiental Rural:", value: record.carNumber)
                currentY += 10

                drawSectionTitle("3. Avaliação Ambiental SEMA-MT")
                drawField(label: "Enquadramento:", value: record.semaStatus.label)
                drawField(label: "Irregularidade:", value: record.hasIrregularity ? "SIM" : "NÃO CONSTATADA")
                drawField(label: "Detalhes Técnicos:", value: record.irregularityDetails)
                if !record.notes.isEmpty {
                    drawField(label: "Parecer do Fiscal:", value: record.notes)
                }
                currentY += 10

                if !record.anmProcessNumber.isEmpty || record.miningRegime != "SEM_TITULO" || record.justiceStatus != "REGULAR" || !record.seizedMachinery.isEmpty {
                    drawSectionTitle("4. Perícia de Mineração & ANM")
                    let anmTxt = record.anmProcessNumber.isEmpty ? "Sem processo cadastrado" : record.anmProcessNumber
                    drawField(label: "Processo ANM / Regime:", value: "\(anmTxt) (\(record.miningRegime))")
                    drawField(label: "Substância Mineral:", value: record.mineralSubstance)
                    drawField(label: "Status Justiça Ambiental:", value: record.justiceStatus)
                    if !record.seizedMachinery.isEmpty {
                        drawField(label: "Maquinários Apreendidos:", value: record.seizedMachinery)
                    }
                    currentY += 10
                }

                // QR Code & Hash Block
                let qrBoxRect = CGRect(x: 36, y: currentY, width: pageWidth - 72, height: 110)
                UIColor(white: 0.96, alpha: 1.0).setFill()
                UIBezierPath(roundedRect: qrBoxRect, cornerRadius: 6).fill()

                if let qrImage = self.generateQrCode(from: record.qrCodePayload) {
                    qrImage.draw(in: CGRect(x: 50, y: currentY + 10, width: 90, height: 90))
                }

                let hashLabelAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 10),
                    .foregroundColor: UIColor.black
                ]
                let hashValueAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.monospacedSystemFont(ofSize: 8, weight: .regular),
                    .foregroundColor: UIColor.darkGray
                ]

                "Assinatura Pericial de Integridade (SHA-256):".draw(at: CGPoint(x: 155, y: currentY + 15), withAttributes: hashLabelAttr)
                record.recordHashSha256.draw(at: CGPoint(x: 155, y: currentY + 30), withAttributes: hashValueAttr)

                "Hash da Imagem Fotográfica:".draw(at: CGPoint(x: 155, y: currentY + 50), withAttributes: hashLabelAttr)
                record.imageHashSha256.draw(at: CGPoint(x: 155, y: currentY + 65), withAttributes: hashValueAttr)

                "Validação oficial: sema.mt.gov.br/valida".draw(at: CGPoint(x: 155, y: currentY + 85), withAttributes: hashLabelAttr)
            }
            return fileURL
        } catch {
            print("Erro ao gerar PDF: \(error)")
            return nil
        }
    }

    // MARK: - CSV Export
    func generateCsvExport(records: [InspectionRecord]) -> URL? {
        var csv = "ID,Data,Titulo,Fiscal,Cargo,Latitude,Longitude,Altitude,Acuracia,Municipio,Bioma,CAR,StatusSEMA,Irregularidade,ProcessoANM,RegimeMinerario,Substancia,StatusJustica,Maquinarios,HashSHA256\n"
        for r in records {
            let row = "\"\(r.id)\",\"\(r.formattedDate)\",\"\(r.title)\",\"\(r.inspectorName)\",\"\(r.inspectorRole)\",\(r.latitude),\(r.longitude),\(r.altitude),\(r.accuracy),\"\(r.municipality)\",\"\(r.biome)\",\"\(r.carNumber)\",\"\(r.semaStatus.rawValue)\",\"\(r.hasIrregularity ? "SIM" : "NAO")\",\"\(r.anmProcessNumber)\",\"\(r.miningRegime)\",\"\(r.mineralSubstance)\",\"\(r.justiceStatus)\",\"\(r.seizedMachinery)\",\"\(r.recordHashSha256)\"\n"
            csv.append(row)
        }

        let fileURL = FileManager.default.temporaryDirectory.appendingPathComponent("Vistorias_SEMA_MT.csv")
        do {
            try csv.write(to: fileURL, atomically: true, encoding: .utf8)
            return fileURL
        } catch {
            print("Erro ao gerar CSV: \(error)")
            return nil
        }
    }
}
