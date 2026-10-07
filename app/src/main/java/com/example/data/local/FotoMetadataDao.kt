package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FotoMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoMetadataDao {

    @Query("SELECT * FROM foto_metadados WHERE denunciaId = :denunciaId ORDER BY dataCaptura DESC")
    fun getFotosByDenuncia(denunciaId: String): Flow<List<FotoMetadataEntity>>

    @Query("SELECT * FROM foto_metadados WHERE vistoriaId = :vistoriaId ORDER BY dataCaptura DESC")
    fun getFotosByVistoria(vistoriaId: String): Flow<List<FotoMetadataEntity>>

    @Query("SELECT * FROM foto_metadados WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingSyncFotos(): List<FotoMetadataEntity>

    @Query("SELECT * FROM foto_metadados WHERE hashSha256 = :hash LIMIT 1")
    suspend fun getFotoByHash(hash: String): FotoMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFotoMetadata(foto: FotoMetadataEntity)

    @Update
    suspend fun updateFotoMetadata(foto: FotoMetadataEntity)

    @Query("UPDATE foto_metadados SET syncStatus = :syncStatus, urlRemota = :urlRemota, syncTimestamp = :syncTimestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, urlRemota: String?, syncTimestamp: Long)

    @Delete
    suspend fun deleteFotoMetadata(foto: FotoMetadataEntity)
}
