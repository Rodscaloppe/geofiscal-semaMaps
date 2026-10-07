package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.InspectionRecord
import com.example.security.CryptoManager
import com.example.security.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates an official SEMA-MT Inspection Technical PDF Report,
     * decrypts the photo, embeds QR Code, integrity hashes and opens Android Share/View Intent.
     */
    fun generateAndSharePdf(context: Context, record: InspectionRecord): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
        val dateStr = dateFormat.format(Date(record.timestamp))

        // 1. Header Background Banner
        paint.color = Color.parseColor("#0F5A37")
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Accent gold stripe
        paint.color = Color.parseColor("#E68A00")
        canvas.drawRect(0f, 75f, 595f, 80f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.isFakeBoldText = true
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("GOVERNO DO ESTADO DE MATO GROSSO", 297f, 30f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("SECRETARIA DE ESTADO DE MEIO AMBIENTE - SEMA-MT", 297f, 48f, paint)

        paint.textSize = 10f
        paint.color = Color.parseColor("#C7F9DC")
        canvas.drawText("SISTEMA INTEGRADO DE FISCALIZAÇÃO GEOESPACIAL E REMOTA", 297f, 65f, paint)

        // 2. Subheader Document Box
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.parseColor("#1B2420")
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText("LAUDO TÉCNICO DE VISTORIA GEOESPACIAL #${record.id.take(8).uppercase()}", 30f, 105f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Data/Hora da Captura: $dateStr  |  Fiscal: ${record.inspectorName}", 30f, 120f, paint)

        // 3. Photo & Sensor Telemetry Card
        val decryptedBitmap = CryptoManager.decryptBitmap(record.encryptedImagePath)
        var currentY = 135f

        if (decryptedBitmap != null) {
            // Frame for photo
            val photoW = 240f
            val photoH = 180f
            val photoRect = RectF(30f, currentY, 30f + photoW, currentY + photoH)

            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(photoRect, paint)
            paint.style = Paint.Style.FILL

            // Draw bitmap scaled
            canvas.drawBitmap(decryptedBitmap, null, photoRect, null)

            // Telemetry alongside photo
            val textLeft = 285f
            paint.textSize = 9f
            paint.color = Color.BLACK

            fun drawInfoLine(label: String, value: String, y: Float, isBoldVal: Boolean = false) {
                paint.isFakeBoldText = true
                paint.color = Color.parseColor("#0F5A37")
                canvas.drawText(label, textLeft, y, paint)
                paint.isFakeBoldText = isBoldVal
                paint.color = Color.BLACK
                canvas.drawText(value, textLeft + 85f, y, paint)
            }

            drawInfoLine("Latitude:", String.format(Locale.US, "%.6f°", record.latitude), currentY + 15f)
            drawInfoLine("Longitude:", String.format(Locale.US, "%.6f°", record.longitude), currentY + 30f)
            drawInfoLine("Altitude / Acur.:", "${String.format("%.1f", record.altitude)}m (±${String.format("%.1f", record.accuracy)}m)", currentY + 45f)
            drawInfoLine("Azimute Bússola:", "${String.format("%.1f", record.azimuthBearing)}° (${getCompassDirection(record.azimuthBearing)})", currentY + 60f)
            drawInfoLine("Município / UF:", "${record.municipality} - MT", currentY + 75f)
            drawInfoLine("Bioma Monitorado:", record.biome, currentY + 90f, true)
            drawInfoLine("Cadastro CAR:", record.carNumber, currentY + 105f)

            // SEMA Status badge
            val statusColor = if (record.hasIrregularity) Color.parseColor("#D32F2F") else Color.parseColor("#0F5A37")
            val statusBg = if (record.hasIrregularity) Color.parseColor("#FFDAD6") else Color.parseColor("#C7F9DC")

            val badgeRect = RectF(textLeft, currentY + 120f, 565f, currentY + 148f)
            paint.color = statusBg
            canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

            paint.color = statusColor
            paint.isFakeBoldText = true
            paint.textSize = 10f
            val statusLabel = if (record.hasIrregularity) "ALERTA: INFRAÇÃO IDENTIFICADA" else "CONFORMIDADE AMBIENTAL REGULAR"
            canvas.drawText(statusLabel, textLeft + 10f, currentY + 137f, paint)

            currentY += photoH + 20f
        }

        // 4. Irregularity Details Box
        if (record.hasIrregularity) {
            val alertRect = RectF(30f, currentY, 565f, currentY + 55f)
            paint.color = Color.parseColor("#FFF3CD")
            canvas.drawRoundRect(alertRect, 4f, 4f, paint)
            paint.color = Color.parseColor("#856404")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(alertRect, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            paint.isFakeBoldText = true
            paint.textSize = 10f
            paint.color = Color.parseColor("#856404")
            canvas.drawText("PARECER TÉCNICO DE IRREGULARIDADE / EMBARGO:", 40f, currentY + 18f, paint)

            paint.isFakeBoldText = false
            paint.textSize = 9f
            paint.color = Color.BLACK
            canvas.drawText(record.irregularityDetails.take(95), 40f, currentY + 33f, paint)
            if (record.irregularityDetails.length > 95) {
                canvas.drawText(record.irregularityDetails.substring(95).take(95), 40f, currentY + 46f, paint)
            }
            currentY += 65f
        } else {
            val regularRect = RectF(30f, currentY, 565f, currentY + 40f)
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRoundRect(regularRect, 4f, 4f, paint)
            paint.color = Color.parseColor("#2E7D32")
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(regularRect, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            paint.isFakeBoldText = true
            paint.textSize = 10f
            paint.color = Color.parseColor("#2E7D32")
            canvas.drawText("PARECER TÉCNICO SEMA-MT:", 40f, currentY + 16f, paint)
            paint.isFakeBoldText = false
            paint.textSize = 9f
            paint.color = Color.BLACK
            canvas.drawText(record.irregularityDetails.take(100), 40f, currentY + 30f, paint)
            currentY += 50f
        }

        // Notes
        if (record.notes.isNotBlank()) {
            paint.isFakeBoldText = true
            paint.textSize = 9f
            paint.color = Color.DKGRAY
            canvas.drawText("Observações de Campo:", 30f, currentY + 10f, paint)
            paint.isFakeBoldText = false
            paint.color = Color.BLACK
            canvas.drawText(record.notes, 30f, currentY + 24f, paint)
            currentY += 35f
        }

        // 5. QR Code and Chain-of-Custody Section
        val qrBoxY = 600f
        val qrBitmap = QrCodeGenerator.generateQrBitmap(record.qrCodePayload, 120)
        canvas.drawBitmap(qrBitmap, 30f, qrBoxY, null)

        val hashLeft = 165f
        paint.textSize = 9f
        paint.color = Color.parseColor("#0F5A37")
        paint.isFakeBoldText = true
        canvas.drawText("AUTENTICAÇÃO DIGITAL & CADEIA DE CUSTÓDIA", hashLeft, qrBoxY + 15f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 8f
        paint.color = Color.DKGRAY
        canvas.drawText("Validação por QR Code para consulta pública e judicial no portal SEMA-MT.", hashLeft, qrBoxY + 28f, paint)
        canvas.drawText("Hash SHA-256 da Foto: ${record.imageHashSha256}", hashLeft, qrBoxY + 45f, paint)
        canvas.drawText("Hash SHA-256 do Registro: ${record.recordHashSha256}", hashLeft, qrBoxY + 58f, paint)
        canvas.drawText("Algoritmo Criptográfico: AES-256-CBC com Salt de Hardware do Dispositivo", hashLeft, qrBoxY + 71f, paint)
        canvas.drawText("Status de Sincronização: ${record.syncStatus}  |  UUID: ${record.id}", hashLeft, qrBoxY + 84f, paint)

        // 6. Signature line
        val signY = 750f
        paint.color = Color.GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(150f, signY, 445f, signY, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 9f
        paint.isFakeBoldText = true
        canvas.drawText(record.inspectorName.uppercase(), 297f, signY + 15f, paint)
        paint.isFakeBoldText = false
        paint.textSize = 8f
        paint.color = Color.DKGRAY
        canvas.drawText("${record.inspectorRole} - Matrícula Fiscal SEMA-MT", 297f, signY + 27f, paint)

        // Footer
        paint.color = Color.LTGRAY
        paint.strokeWidth = 0.5f
        canvas.drawLine(30f, 805f, 565f, 805f, paint)
        paint.textSize = 7f
        paint.color = Color.GRAY
        canvas.drawText("Documento emitido pelo App GeoFiscal SEMA-MT com base na Lei Complementar Estadual nº 233/2005.", 297f, 818f, paint)

        pdfDocument.finishPage(page)

        // Save PDF file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val pdfFile = File(reportsDir, "Laudo_SEMA_${record.municipality}_${record.id.take(6)}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        // Share via Intent
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laudo Técnico Fiscalização SEMA-MT - ${record.municipality}")
            putExtra(Intent.EXTRA_TEXT, "Laudo pericial georreferenciado e criptografado da vistoria em ${record.municipality}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Laudo Técnico SEMA-MT").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        return pdfFile
    }

    private fun getCompassDirection(bearing: Float): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = (((bearing + 22.5f) % 360) / 45).toInt()
        return directions[index.coerceIn(0, 7)]
    }
}
