package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DenunciaComDetalhes
import com.example.data.model.DenunciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DenunciaDao {

    @Query("SELECT * FROM denuncias ORDER BY dataRegistro DESC")
    fun getAllDenuncias(): Flow<List<DenunciaEntity>>

    @Query("SELECT * FROM denuncias WHERE id = :id")
    suspend fun getDenunciaById(id: String): DenunciaEntity?

    @Transaction
    @Query("SELECT * FROM denuncias ORDER BY dataRegistro DESC")
    fun getAllDenunciasComDetalhes(): Flow<List<DenunciaComDetalhes>>

    @Transaction
    @Query("SELECT * FROM denuncias WHERE id = :id")
    fun getDenunciaComDetalhesById(id: String): Flow<DenunciaComDetalhes?>

    @Query("SELECT * FROM denuncias WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingSyncDenuncias(): List<DenunciaEntity>

    @Query("SELECT * FROM denuncias WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getPendingSyncDenunciasFlow(): Flow<List<DenunciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDenuncia(denuncia: DenunciaEntity)

    @Update
    suspend fun updateDenuncia(denuncia: DenunciaEntity)

    @Query("UPDATE denuncias SET syncStatus = :syncStatus, syncTimestamp = :syncTimestamp, remoteId = :remoteId WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, syncTimestamp: Long, remoteId: String?)

    @Delete
    suspend fun deleteDenuncia(denuncia: DenunciaEntity)

    @Query("SELECT COUNT(*) FROM denuncias")
    fun getTotalDenunciasCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM denuncias WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getPendingSyncCount(): Flow<Int>
}
