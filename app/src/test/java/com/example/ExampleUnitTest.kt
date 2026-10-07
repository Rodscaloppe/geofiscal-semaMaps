package com.example

import com.example.security.CryptoManager
import com.example.service.SemaIntegrationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSha256Calculation() {
        val testString = "SEMA-MT-INSPECTION-HASH-TEST"
        val hash = CryptoManager.calculateStringSha256(testString)
        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 produces 64 hex characters
    }

    @Test
    fun testSemaTelemetryEvaluationCuiaba() {
        // Cuiabá coordinates in Cerrado
        val result = SemaIntegrationService.evaluateLocation(-15.6014, -56.0979)
        assertEquals("Cuiabá", result.municipality)
        assertEquals("Cerrado", result.biome)
        assertTrue(result.carNumber.startsWith("MT-"))
    }

    @Test
    fun testSemaTelemetryEvaluationSinop() {
        // Sinop coordinates in Amazônia
        val result = SemaIntegrationService.evaluateLocation(-11.8642, -55.5056)
        assertEquals("Sinop", result.municipality)
        assertEquals("Amazônia", result.biome)
    }

    @Test
    fun testRoomEntitiesCreationAndOfflineSyncDefaults() {
        val denuncia = com.example.data.model.DenunciaEntity(
            id = "test-uuid-1",
            protocolo = "DEN-2026-SEMA-1001",
            titulo = "Alerta de Desmatamento em Alta Floresta",
            descricao = "Indício de supressão de vegetação nativa em APP",
            tipoInfracao = com.example.data.model.TipoInfracao.DESMATAMENTO_ILEGAL.name,
            gravidade = com.example.data.model.GravidadeInfracao.ALTA.name,
            status = com.example.data.model.StatusDenuncia.RECEBIDA.name,
            origem = "SATELITE_SEMA",
            dataRegistro = System.currentTimeMillis(),
            municipio = "Alta Floresta",
            bioma = "Amazônia",
            syncStatus = "PENDING"
        )
        assertEquals("PENDING", denuncia.syncStatus)
        assertEquals("DEN-2026-SEMA-1001", denuncia.protocolo)

        val coord = com.example.data.model.CoordenadaGeograficaEntity(
            id = "coord-1",
            denunciaId = denuncia.id,
            latitude = -9.8756,
            longitude = -56.0861,
            altitude = 280.0,
            acuraciaMetros = 3.2f,
            azimuteGraus = 180f,
            municipio = "Alta Floresta",
            bioma = "Amazônia",
            isPontoCritico = true,
            timestamp = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )
        assertEquals(denuncia.id, coord.denunciaId)
        assertTrue(coord.isPontoCritico)

        val foto = com.example.data.model.FotoMetadataEntity(
            id = "foto-1",
            denunciaId = denuncia.id,
            caminhoArquivoCriptografado = "/data/user/0/app/files/foto.enc",
            hashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            larguraPixels = 1920,
            alturaPixels = 1080,
            tamanhoBytes = 450200L,
            dataCaptura = System.currentTimeMillis(),
            latitudeExif = -9.8756,
            longitudeExif = -56.0861,
            altitudeExif = 280.0,
            azimuteCamera = 180f,
            inclinacaoPitch = 5.0f,
            inclinacaoRoll = 0.5f,
            syncStatus = "PENDING"
        )
        assertEquals("AES-256-CBC", foto.algoritmoCriptografia)
        assertEquals(64, foto.hashSha256.length)
        assertEquals("PENDING", foto.syncStatus)
    }

    @Test
    fun testGpsLocationDataCreationAndAntiFraudCheck() {
        val locationData = com.example.service.GpsLocationData(
            latitude = -15.6014,
            longitude = -56.0979,
            altitude = 165.0,
            accuracyMetros = 2.5f,
            speedKmh = 45.0f,
            bearingGraus = 90.0f,
            isGpsFixed = true,
            isMockLocation = false,
            satellitesProvider = "fused",
            distanceTraveledMeters = 1520.0
        )
        assertTrue(locationData.isGpsFixed)
        assertEquals(2.5f, locationData.accuracyMetros)
        assertEquals(45.0f, locationData.speedKmh)
        org.junit.Assert.assertFalse(locationData.isMockLocation)
        assertEquals(1520.0, locationData.distanceTraveledMeters, 0.001)
    }

    @Test
    fun testCameraStateDefaults() {
        val state = com.example.service.CameraState()
        org.junit.Assert.assertFalse(state.isInitialized)
        org.junit.Assert.assertFalse(state.isTakingPicture)
        assertEquals(androidx.camera.core.CameraSelector.LENS_FACING_BACK, state.lensFacing)
        assertEquals(androidx.camera.core.ImageCapture.FLASH_MODE_OFF, state.flashMode)
    }

    @Test
    fun testDenunciaComDetalhesSpatialMapping() {
        val denuncia = com.example.data.model.DenunciaEntity(
            id = "denuncia-map-101",
            protocolo = "DEN-2026-SEMA-4499",
            titulo = "Desmatamento Detectado via Satélite",
            descricao = "Supressão em reserva legal",
            tipoInfracao = "DESMATAMENTO_ILEGAL",
            gravidade = "CRITICA",
            status = "RECEBIDA",
            origem = "SATELITE_SEMA",
            dataRegistro = System.currentTimeMillis(),
            municipio = "Alta Floresta",
            bioma = "Amazônia"
        )

        val coord = com.example.data.model.CoordenadaGeograficaEntity(
            id = "coord-101",
            denunciaId = denuncia.id,
            latitude = -9.8756,
            longitude = -56.0861,
            altitude = 285.0,
            acuraciaMetros = 2.1f,
            azimuteGraus = 175f,
            municipio = "Alta Floresta",
            bioma = "Amazônia",
            isPontoCritico = true,
            timestamp = System.currentTimeMillis()
        )

        val detalhe = com.example.data.model.DenunciaComDetalhes(
            denuncia = denuncia,
            coordenadas = listOf(coord),
            fotos = emptyList()
        )

        assertEquals("DEN-2026-SEMA-4499", detalhe.denuncia.protocolo)
        assertEquals(1, detalhe.coordenadas.size)
        val firstCoord = detalhe.coordenadas.first()
        assertEquals(-9.8756, firstCoord.latitude, 0.0001)
        assertEquals(-56.0861, firstCoord.longitude, 0.0001)
        assertTrue(firstCoord.isPontoCritico)
    }
}
