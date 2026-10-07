package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CoordenadaGeograficaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoordenadaDao {

    @Query("SELECT * FROM coordenadas_geograficas WHERE denunciaId = :denunciaId ORDER BY ordemTrajetoria ASC, timestamp ASC")
    fun getCoordenadasByDenuncia(denunciaId: String): Flow<List<CoordenadaGeograficaEntity>>

    @Query("SELECT * FROM coordenadas_geograficas WHERE vistoriaId = :vistoriaId ORDER BY ordemTrajetoria ASC, timestamp ASC")
    fun getCoordenadasByVistoria(vistoriaId: String): Flow<List<CoordenadaGeograficaEntity>>

    @Query("SELECT * FROM coordenadas_geograficas WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingSyncCoordenadas(): List<CoordenadaGeograficaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordenada(coordenada: CoordenadaGeograficaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordenadas(coordenadas: List<CoordenadaGeograficaEntity>)

    @Update
    suspend fun updateCoordenada(coordenada: CoordenadaGeograficaEntity)

    @Query("UPDATE coordenadas_geograficas SET syncStatus = :syncStatus, syncTimestamp = :syncTimestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, syncTimestamp: Long)

    @Delete
    suspend fun deleteCoordenada(coordenada: CoordenadaGeograficaEntity)
}
