package com.example.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.InspectionRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelReportGenerator {

    /**
     * Exports all inspection records into an Excel-compatible CSV file (with UTF-8 BOM for Microsoft Excel),
     * and triggers system Share dialog.
     */
    fun exportAndShareExcel(context: Context, records: List<InspectionRecord>): File? {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Relatorio_SEMA_MT_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val csvFile = File(reportsDir, fileName)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))

        FileOutputStream(csvFile).use { out ->
            // Write UTF-8 BOM so Excel opens accents correctly
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val header = "UUID;Data/Hora;Municipio;Bioma;CAR;Status SEMA;Irregularidade;Detalhes;Latitude;Longitude;Altitude(m);Acuracia(m);Azimute(graus);Fiscal;Hash Foto SHA256;Hash Registro SHA256;Status Sincronizacao;URL Validacao\n"
            out.write(header.toByteArray(Charsets.UTF_8))

            for (r in records) {
                val row = StringBuilder()
                row.append(escapeCsv(r.id)).append(";")
                row.append(escapeCsv(dateFormat.format(Date(r.timestamp)))).append(";")
                row.append(escapeCsv(r.municipality)).append(";")
                row.append(escapeCsv(r.biome)).append(";")
                row.append(escapeCsv(r.carNumber)).append(";")
                row.append(escapeCsv(r.semaStatus)).append(";")
                row.append(if (r.hasIrregularity) "SIM" else "NAO").append(";")
                row.append(escapeCsv(r.irregularityDetails)).append(";")
                row.append(String.format(Locale.US, "%.6f", r.latitude)).append(";")
                row.append(String.format(Locale.US, "%.6f", r.longitude)).append(";")
                row.append(String.format(Locale.US, "%.1f", r.altitude)).append(";")
                row.append(String.format(Locale.US, "%.1f", r.accuracy)).append(";")
                row.append(String.format(Locale.US, "%.1f", r.azimuthBearing)).append(";")
                row.append(escapeCsv(r.inspectorName)).append(";")
                row.append(escapeCsv(r.imageHashSha256)).append(";")
                row.append(escapeCsv(r.recordHashSha256)).append(";")
                row.append(escapeCsv(r.syncStatus)).append(";")
                row.append(escapeCsv("https://sema.mt.gov.br/valida/${r.id}")).append("\n")

                out.write(row.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Planilha de Vistorias SEMA-MT (Excel/GIS)")
            putExtra(Intent.EXTRA_TEXT, "Exportação consolidada de ${records.size} vistorias com georreferenciamento e dados de sensoriamento remoto.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Exportar Relatório Excel/CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        return csvFile
    }

    private fun escapeCsv(text: String): String {
        val sanitized = text.replace("\"", "\"\"").replace("\n", " ")
        return "\"$sanitized\""
    }
}
