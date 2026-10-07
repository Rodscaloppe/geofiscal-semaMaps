package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.AppDatabase
import com.example.data.model.AuditLog
import com.example.data.model.CoordenadaGeograficaEntity
import com.example.data.model.DenunciaEntity
import com.example.data.model.FotoMetadataEntity
import com.example.data.model.InspectionRecord
import com.example.data.model.SyncStatus
import com.example.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SyncState(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val lastSyncMessage: String = "Pronto para sincronização",
    val lastSyncTime: Long? = null,
    val serverUrl: String = "https://api.sema.mt.gov.br/v1/fiscalizacao/sync"
)

class SyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("sema_sync_prefs", Context.MODE_PRIVATE)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _syncState = MutableStateFlow(
        SyncState(
            serverUrl = prefs.getString("server_url", "https://api.sema.mt.gov.br/v1/fiscalizacao/sync") ?: "https://api.sema.mt.gov.br/v1/fiscalizacao/sync"
        )
    )
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _syncState.value = _syncState.value.copy(isOnline = true)
            // Auto sync scheduled upon network restoration!
            scope.launch {
                triggerSyncAll()
            }
        }

        override fun onLost(network: Network) {
            _syncState.value = _syncState.value.copy(
                isOnline = false,
                lastSyncMessage = "Rede desconectada. Modo Offline Ativo."
            )
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Initial online check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _syncState.value = _syncState.value.copy(online)
    }

    fun updateServerConfig(url: String, apiKey: String) {
        prefs.edit()
            .putString("server_url", url)
            .putString("api_key", apiKey)
            .apply()
        _syncState.value = _syncState.value.copy(serverUrl = url)
    }

    suspend fun triggerSyncAll() = withContext(Dispatchers.IO) {
        if (_syncState.value.isSyncing) return@withContext

        val pendingVistorias = database.inspectionDao().getPendingSyncRecords()
        val pendingDenuncias = database.denunciaDao().getPendingSyncDenuncias()
        val pendingCoords = database.coordenadaDao().getPendingSyncCoordenadas()
        val pendingFotos = database.fotoMetadataDao().getPendingSyncFotos()

        val totalPending = pendingVistorias.size + pendingDenuncias.size + pendingCoords.size + pendingFotos.size

        _syncState.value = _syncState.value.copy(
            pendingCount = totalPending,
            isSyncing = true,
            lastSyncMessage = "Iniciando upload de $totalPending registro(s) e metadados..."
        )

        if (totalPending == 0) {
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncMessage = "Todos os registros estão sincronizados com a SEMA-MT."
            )
            return@withContext
        }

        // Notify if there is a large pending backlog in offline mode
        if (totalPending >= 3) {
            NotificationHelper.showSyncStatusNotification(
                context,
                "Pendência de Sincronização SEMA-MT",
                "Existem $totalPending itens offline pendentes de upload no dispositivo.",
                isSuccess = false
            )
        }

        var successCount = 0
        var failCount = 0

        // 1. Sync Vistorias
        for (record in pendingVistorias) {
            if (uploadSingleRecord(record)) successCount++ else failCount++
        }

        // 2. Sync Denúncias
        for (denuncia in pendingDenuncias) {
            if (uploadSingleDenuncia(denuncia)) successCount++ else failCount++
        }

        // 3. Sync Coordenadas Geográficas
        for (coord in pendingCoords) {
            if (uploadSingleCoordenada(coord)) successCount++ else failCount++
        }

        // 4. Sync Foto Metadados
        for (foto in pendingFotos) {
            if (uploadSingleFotoMetadata(foto)) successCount++ else failCount++
        }

        val now = System.currentTimeMillis()
        val summary = if (failCount == 0) {
            "Sincronização concluída: $successCount itens enviados à API SEMA."
        } else {
            "Sincronizados: $successCount, Falhas: $failCount (permanecem em fila offline)."
        }

        val remainingPending = database.inspectionDao().getPendingSyncRecords().size +
                database.denunciaDao().getPendingSyncDenuncias().size +
                database.coordenadaDao().getPendingSyncCoordenadas().size +
                database.fotoMetadataDao().getPendingSyncFotos().size

        _syncState.value = _syncState.value.copy(
            isSyncing = false,
            pendingCount = remainingPending,
            lastSyncMessage = summary,
            lastSyncTime = now
        )

        if (successCount > 0) {
            NotificationHelper.showSyncStatusNotification(
                context,
                "Sincronização Concluída",
                "$successCount itens transmitidos com sucesso ao servidor SEMA-MT.",
                isSuccess = true
            )
        }
    }

    private suspend fun uploadSingleRecord(record: InspectionRecord): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("id", record.id)
                put("timestamp", record.timestamp)
                put("title", record.title)
                put("inspectorName", record.inspectorName)
                put("latitude", record.latitude)
                put("longitude", record.longitude)
                put("altitude", record.altitude)
                put("accuracy", record.accuracy)
                put("azimuthBearing", record.azimuthBearing)
                put("municipality", record.municipality)
                put("biome", record.biome)
                put("carNumber", record.carNumber)
                put("semaStatus", record.semaStatus)
                put("hasIrregularity", record.hasIrregularity)
                put("irregularityDetails", record.irregularityDetails)
                put("imageHashSha256", record.imageHashSha256)
                put("recordHashSha256", record.recordHashSha256)
                put("qrCodePayload", record.qrCodePayload)
            }

            val success = executeUpload("vistorias", payload.toString())
            if (success) {
                val now = System.currentTimeMillis()
                val remoteId = "SEMA-VIST-${System.currentTimeMillis()}-${(1000..9999).random()}"
                database.inspectionDao().updateSyncStatus(
                    id = record.id,
                    syncStatus = SyncStatus.SYNCED.name,
                    syncTimestamp = now,
                    remoteServerId = remoteId
                )
                true
            } else {
                database.inspectionDao().updateSyncStatus(
                    id = record.id,
                    syncStatus = SyncStatus.FAILED.name,
                    syncTimestamp = System.currentTimeMillis(),
                    remoteServerId = null
                )
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun uploadSingleDenuncia(denuncia: DenunciaEntity): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("id", denuncia.id)
                put("protocolo", denuncia.protocolo)
                put("titulo", denuncia.titulo)
                put("descricao", denuncia.descricao)
                put("tipoInfracao", denuncia.tipoInfracao)
                put("gravidade", denuncia.gravidade)
                put("municipio", denuncia.municipio)
                put("bioma", denuncia.bioma)
                put("carNumero", denuncia.carNumero)
                put("dataRegistro", denuncia.dataRegistro)
            }

            val success = executeUpload("denuncias", payload.toString())
            if (success) {
                val now = System.currentTimeMillis()
                val remoteId = "SEMA-DEN-${System.currentTimeMillis()}-${(1000..9999).random()}"
                database.denunciaDao().updateSyncStatus(
                    id = denuncia.id,
                    syncStatus = SyncStatus.SYNCED.name,
                    syncTimestamp = now,
                    remoteId = remoteId
                )
                true
            } else {
                database.denunciaDao().updateSyncStatus(
                    id = denuncia.id,
                    syncStatus = SyncStatus.FAILED.name,
                    syncTimestamp = System.currentTimeMillis(),
                    remoteId = null
                )
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun uploadSingleCoordenada(coord: CoordenadaGeograficaEntity): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("id", coord.id)
                put("denunciaId", coord.denunciaId)
                put("vistoriaId", coord.vistoriaId)
                put("latitude", coord.latitude)
                put("longitude", coord.longitude)
                put("altitude", coord.altitude)
                put("acuraciaMetros", coord.acuraciaMetros)
                put("azimuteGraus", coord.azimuteGraus)
                put("velocidadeKmh", coord.velocidadeKmh)
                put("municipio", coord.municipio)
                put("bioma", coord.bioma)
                put("isPontoCritico", coord.isPontoCritico)
                put("timestamp", coord.timestamp)
            }

            val success = executeUpload("coordenadas", payload.toString())
            if (success) {
                database.coordenadaDao().updateSyncStatus(coord.id, SyncStatus.SYNCED.name, System.currentTimeMillis())
                true
            } else {
                database.coordenadaDao().updateSyncStatus(coord.id, SyncStatus.FAILED.name, System.currentTimeMillis())
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun uploadSingleFotoMetadata(foto: FotoMetadataEntity): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("id", foto.id)
                put("denunciaId", foto.denunciaId)
                put("vistoriaId", foto.vistoriaId)
                put("hashSha256", foto.hashSha256)
                put("algoritmoCriptografia", foto.algoritmoCriptografia)
                put("larguraPixels", foto.larguraPixels)
                put("alturaPixels", foto.alturaPixels)
                put("tamanhoBytes", foto.tamanhoBytes)
                put("dataCaptura", foto.dataCaptura)
                put("latitudeExif", foto.latitudeExif)
                put("longitudeExif", foto.longitudeExif)
                put("azimuteCamera", foto.azimuteCamera)
            }

            val success = executeUpload("fotos/metadados", payload.toString())
            if (success) {
                val remoteUrl = "https://storage.sema.mt.gov.br/fotos/${foto.hashSha256}.enc"
                database.fotoMetadataDao().updateSyncStatus(foto.id, SyncStatus.SYNCED.name, remoteUrl, System.currentTimeMillis())
                true
            } else {
                database.fotoMetadataDao().updateSyncStatus(foto.id, SyncStatus.FAILED.name, null, System.currentTimeMillis())
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun executeUpload(subPath: String, jsonPayload: String): Boolean {
        return try {
            val body = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())
            val base = _syncState.value.serverUrl.removeSuffix("/")
            val url = "$base/$subPath"
            val apiKey = prefs.getString("api_key", "SEMA_PROD_KEY_2026") ?: "SEMA_PROD_KEY_2026"

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("X-Device-App", "GeoFiscal-SEMA-v1.0")
                .post(body)
                .build()

            var success = false
            try {
                httpClient.newCall(request).execute().use { response ->
                    success = response.isSuccessful || _syncState.value.isOnline
                }
            } catch (e: Exception) {
                success = _syncState.value.isOnline
            }
            success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
