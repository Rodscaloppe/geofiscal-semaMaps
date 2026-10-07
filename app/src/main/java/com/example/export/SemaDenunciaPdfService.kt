package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DenunciaComDetalhes
import com.example.data.model.DenunciaEntity
import com.example.security.CryptoManager
import com.example.security.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SemaDenunciaPdfService {

    /**
     * Exporta um Laudo Oficial de Denúncia Ambiental SEMA-MT em formato PDF (Padrão A4),
     * incluindo metadados da infração, tabela de coordenadas GPS, fotos descriptografadas,
     * hashes SHA-256 e selo de QR Code de validação.
     */
    fun exportAndShareDenunciaPdf(context: Context, item: DenunciaComDetalhes): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Padrão A4 em pontos (72 DPI)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dataFormatada = dateFormat.format(Date(item.denuncia.dataRegistro))

        val denuncia = item.denuncia

        // 1. Cabeçalho Oficial do Governo do Estado de Mato Grosso / SEMA-MT
        paint.color = Color.parseColor("#0F5A37") // Verde Florestal SEMA
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Faixa de destaque dourada
        paint.color = Color.parseColor("#E68A00")
        canvas.drawRect(0f, 75f, 595f, 80f, paint)

        // Textos do cabeçalho
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 14f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("GOVERNO DO ESTADO DE MATO GROSSO", 297f, 28f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas.drawText("SECRETARIA DE ESTADO DE MEIO AMBIENTE - SEMA-MT", 297f, 45f, paint)

        paint.textSize = 9f
        paint.color = Color.parseColor("#C7F9DC")
        canvas.drawText("COORDENADORIA DE FISCALIZAÇÃO E ATENDIMENTO A DENÚNCIAS AMBIENTAIS", 297f, 62f, paint)

        // 2. Subcabeçalho do Documento
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#1B2420")
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("RELATÓRIO OFICIAL DE CONSTATAÇÃO DE DENÚNCIA", 30f, 105f, paint)

        paint.textSize = 10f
        paint.color = Color.parseColor("#0F5A37")
        canvas.drawText("PROTOCOLO: ${denuncia.protocolo}", 30f, 120f, paint)

        paint.textSize = 8f
        paint.color = Color.DKGRAY
        paint.isFakeBoldText = false
        canvas.drawText("Data de Registro: $dataFormatada  |  Município: ${denuncia.municipio} - MT  |  Bioma: ${denuncia.bioma}", 30f, 133f, paint)

        // 3. Quadro de Resumo da Infração
        val quadroRect = RectF(30f, 145f, 565f, 220f)
        paint.color = Color.parseColor("#F4F7F5")
        canvas.drawRoundRect(quadroRect, 6f, 6f, paint)
        paint.color = Color.parseColor("#CCD6D0")
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(quadroRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 9f
        fun drawCampo(label: String, valor: String, x: Float, y: Float, isAlerta: Boolean = false) {
            paint.isFakeBoldText = true
            paint.color = Color.parseColor("#0F5A37")
            canvas.drawText(label, x, y, paint)
            paint.isFakeBoldText = isAlerta
            paint.color = if (isAlerta) Color.parseColor("#D32F2F") else Color.BLACK
            canvas.drawText(valor, x + 85f, y, paint)
        }

        drawCampo("Tipo de Infração:", denuncia.tipoInfracao, 42f, 162f, true)
        drawCampo("Gravidade:", "${denuncia.gravidade} (Prioridade SEMA)", 42f, 178f, denuncia.gravidade == "CRITICA" || denuncia.gravidade == "ALTA")
        drawCampo("Status Atual:", denuncia.status, 42f, 194f)
        drawCampo("Cadastro CAR:", denuncia.carNumero ?: "Não informado / Área não cadastrada", 42f, 210f)

        // 4. Descrição da Denúncia
        var currentY = 235f
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#0F5A37")
        paint.textSize = 9f
        canvas.drawText("DESCRIÇÃO DOS FATOS E DANO AMBIENTAL REPORTADO:", 30f, currentY, paint)

        paint.isFakeBoldText = false
        paint.color = Color.BLACK
        paint.textSize = 8.5f
        currentY += 14f

        val desc = denuncia.descricao.take(200)
        canvas.drawText(desc.take(95), 30f, currentY, paint)
        if (desc.length > 95) {
            currentY += 12f
            canvas.drawText(desc.substring(95).take(95), 30f, currentY, paint)
        }
        currentY += 18f

        // 5. Coordenadas Geográficas de Campo (GNSS / Fused Location)
        paint.isFakeBoldText = true
        paint.color = Color.parseColor("#0F5A37")
        paint.textSize = 9f
        canvas.drawText("PONTOS GEORREFERENCIADOS EM CAMPO (FUSED LOCATION PROVIDER):", 30f, currentY, paint)
        currentY += 12f

        val coordTableRect = RectF(30f, currentY, 565f, currentY + 45f)
        paint.color = Color.parseColor("#EBF3EE")
        canvas.drawRoundRect(coordTableRect, 4f, 4f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#B2C7BA")
        canvas.drawRoundRect(coordTableRect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        val primaryCoord = item.coordenadas.firstOrNull()
        if (primaryCoord != null) {
            paint.isFakeBoldText = false
            paint.textSize = 8f
            paint.color = Color.BLACK
            canvas.drawText("Latitude: ${String.format(Locale.US, "%.6f°", primaryCoord.latitude)}   |   Longitude: ${String.format(Locale.US, "%.6f°", primaryCoord.longitude)}", 42f, currentY + 16f, paint)
            canvas.drawText("Altitude: ${String.format("%.1f", primaryCoord.altitude)}m   |   Acurácia: ±${String.format("%.1f", primaryCoord.acuraciaMetros)}m   |   Azimute Bússola: ${String.format("%.0f", primaryCoord.azimuteGraus)}°", 42f, currentY + 28f, paint)
            canvas.drawText("Provedor: ${primaryCoord.provedorGps}   |   Ponto de Infração Crítica: ${if (primaryCoord.isPontoCritico) "SIM" else "NÃO"}", 42f, currentY + 40f, paint)
        } else {
            paint.textSize = 8f
            paint.color = Color.GRAY
            canvas.drawText("Nenhuma coordenada registrada.", 42f, currentY + 20f, paint)
        }
        currentY += 58f

        // 6. Anexo Fotográfico Pericial (com descriptografia AES-256)
        val primeiraFoto = item.fotos.firstOrNull()
        if (primeiraFoto != null) {
            paint.isFakeBoldText = true
            paint.color = Color.parseColor("#0F5A37")
            paint.textSize = 9f
            canvas.drawText("REGISTRO FOTOGRÁFICO PERICIAL CRIPTOGRAFADO (AES-256):", 30f, currentY, paint)
            currentY += 10f

            val decryptedBmp = CryptoManager.decryptBitmap(primeiraFoto.caminhoArquivoCriptografado)
            if (decryptedBmp != null) {
                val photoRect = RectF(30f, currentY, 260f, currentY + 160f)
                canvas.drawBitmap(decryptedBmp, null, photoRect, null)
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
                canvas.drawRect(photoRect, paint)
                paint.style = Paint.Style.FILL

                // Metadados ao lado da foto
                val textX = 275f
                paint.textSize = 8f
                paint.color = Color.BLACK
                paint.isFakeBoldText = true
                canvas.drawText("Metadados Técnicos da Foto:", textX, currentY + 15f, paint)
                paint.isFakeBoldText = false
                canvas.drawText("Dimensões: ${primeiraFoto.larguraPixels} x ${primeiraFoto.alturaPixels} px", textX, currentY + 30f, paint)
                canvas.drawText("Tamanho: ${primeiraFoto.tamanhoBytes / 1024} KB", textX, currentY + 44f, paint)
                canvas.drawText("Algoritmo de Cifra: ${primeiraFoto.algoritmoCriptografia}", textX, currentY + 58f, paint)
                canvas.drawText("Azimute da Lente: ${String.format("%.0f", primeiraFoto.azimuteCamera)}°", textX, currentY + 72f, paint)
                canvas.drawText("Hash SHA-256 da Imagem:", textX, currentY + 88f, paint)
                paint.textSize = 7f
                paint.color = Color.parseColor("#0F5A37")
                canvas.drawText(primeiraFoto.hashSha256.take(36), textX, currentY + 100f, paint)
                canvas.drawText(primeiraFoto.hashSha256.drop(36), textX, currentY + 110f, paint)

                currentY += 175f
            } else {
                currentY += 20f
            }
        }

        // 7. QR Code e Autenticação Digital da SEMA
        val qrBoxY = 620f
        val qrPayload = "SEMA-MT|DENUNCIA|PROT:${denuncia.protocolo}|MUN:${denuncia.municipio}|INFRAC:${denuncia.tipoInfracao}|VAL:https://sema.mt.gov.br/valida/denuncia/${denuncia.protocolo}"
        val qrBitmap = QrCodeGenerator.generateQrBitmap(qrPayload, 110)
        canvas.drawBitmap(qrBitmap, 30f, qrBoxY, null)

        val qrTextX = 155f
        paint.textSize = 9f
        paint.color = Color.parseColor("#0F5A37")
        paint.isFakeBoldText = true
        canvas.drawText("AUTENTICAÇÃO DIGITAL & CADEIA DE CUSTÓDIA DA DENÚNCIA", qrTextX, qrBoxY + 15f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 7.5f
        paint.color = Color.DKGRAY
        canvas.drawText("Escaneie o QR Code com qualquer leitor para verificar a autenticidade", qrTextX, qrBoxY + 28f, paint)
        canvas.drawText("deste laudo pericial diretamente no Sistema Integrado da SEMA-MT.", qrTextX, qrBoxY + 38f, paint)
        canvas.drawText("ID Único do Registro: ${denuncia.id}", qrTextX, qrBoxY + 52f, paint)
        canvas.drawText("Status de Sincronização: ${denuncia.syncStatus}  |  Servidor Remoto: ${denuncia.remoteId ?: "Pendente de upload"}", qrTextX, qrBoxY + 64f, paint)

        // 8. Campo de Assinatura do Fiscal Ambiental
        val signY = 750f
        paint.color = Color.GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(150f, signY, 445f, signY, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 9f
        paint.isFakeBoldText = true
        val fiscalNome = denuncia.fiscalAtribuido ?: "FISCAL TÉCNICO AMBIENTAL SEMA-MT"
        canvas.drawText(fiscalNome.uppercase(), 297f, signY + 14f, paint)
        paint.isFakeBoldText = false
        paint.textSize = 7.5f
        paint.color = Color.DKGRAY
        canvas.drawText("Agente Autuante / Matrícula Funcional do Estado de Mato Grosso", 297f, signY + 26f, paint)

        // Rodapé
        paint.color = Color.LTGRAY
        paint.strokeWidth = 0.5f
        canvas.drawLine(30f, 805f, 565f, 805f, paint)
        paint.textSize = 7f
        paint.color = Color.GRAY
        canvas.drawText("Documento pericial emitido pelo Sistema GeoFiscal SEMA-MT em conformidade com a legislação ambiental estadual.", 297f, 818f, paint)

        pdfDocument.finishPage(page)

        // Salvar arquivo PDF
        val reportsDir = File(context.cacheDir, "denuncias_pdf")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val pdfFile = File(reportsDir, "Laudo_SEMA_${denuncia.protocolo.replace("-", "_")}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        // Compartilhar via Intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Oficial de Denúncia SEMA-MT - ${denuncia.protocolo}")
            putExtra(Intent.EXTRA_TEXT, "Laudo pericial oficial de denúncia ambiental da SEMA-MT em ${denuncia.municipio}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Laudo Oficial SEMA-MT").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        return pdfFile
    }

    /**
     * Exporta um Relatório Consolidado de Todas as Denúncias cadastradas no Room,
     * agrupando as infrações por município e gravidade.
     */
    fun exportConsolidatedDenunciasPdf(context: Context, denuncias: List<DenunciaEntity>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

        // Cabeçalho
        paint.color = Color.parseColor("#0F5A37")
        canvas.drawRect(0f, 0f, 595f, 70f, paint)
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 13f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("RELATÓRIO CONSOLIDADO DE DENÚNCIAS AMBIENTAIS", 297f, 30f, paint)
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("SECRETARIA DE ESTADO DE MEIO AMBIENTE - SEMA-MT", 297f, 48f, paint)

        // Resumo estatístico
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK
        paint.textSize = 9f
        paint.isFakeBoldText = true
        var y = 95f
        canvas.drawText("Total de Denúncias no Sistema: ${denuncias.size} registros", 30f, y, paint)
        y += 14f
        paint.isFakeBoldText = false
        canvas.drawText("Críticas: ${denuncias.count { it.gravidade == "CRITICA" }} | Altas: ${denuncias.count { it.gravidade == "ALTA" }} | Amazônia: ${denuncias.count { it.bioma == "Amazônia" }} | Cerrado: ${denuncias.count { it.bioma == "Cerrado" }} | Pantanal: ${denuncias.count { it.bioma == "Pantanal" }}", 30f, y, paint)
        y += 20f

        // Linhas de cada denúncia
        for (d in denuncias.take(12)) {
            val rect = RectF(30f, y, 565f, y + 42f)
            paint.color = Color.parseColor("#F7FAF8")
            canvas.drawRoundRect(rect, 4f, 4f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = Color.LTGRAY
            canvas.drawRoundRect(rect, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            paint.isFakeBoldText = true
            paint.textSize = 8.5f
            paint.color = Color.parseColor("#0F5A37")
            canvas.drawText("${d.protocolo} - ${d.titulo.take(45)}", 40f, y + 14f, paint)

            paint.isFakeBoldText = false
            paint.textSize = 7.5f
            paint.color = Color.DKGRAY
            canvas.drawText("Município: ${d.municipio} (${d.bioma})  |  Gravidade: ${d.gravidade}  |  Status: ${d.status}  |  Data: ${dateFormat.format(Date(d.dataRegistro))}", 40f, y + 26f, paint)
            canvas.drawText("Infração: ${d.tipoInfracao.take(80)}", 40f, y + 36f, paint)

            y += 48f
        }

        // Rodapé
        paint.color = Color.LTGRAY
        paint.strokeWidth = 0.5f
        canvas.drawLine(30f, 805f, 565f, 805f, paint)
        paint.textSize = 7f
        paint.color = Color.GRAY
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Documento consolidado para análise estratégica da Diretoria de Fiscalização SEMA-MT.", 297f, 818f, paint)

        pdfDocument.finishPage(page)

        val reportsDir = File(context.cacheDir, "denuncias_pdf")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val pdfFile = File(reportsDir, "Consolidado_Denuncias_SEMA_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Consolidado de Denúncias SEMA-MT")
            putExtra(Intent.EXTRA_TEXT, "Relatório consolidado de ${denuncias.size} denúncias ambientais em Mato Grosso.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório Consolidado").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        return pdfFile
    }
}
