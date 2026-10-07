package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InspectionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspection_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<InspectionRecord>>

    @Query("SELECT * FROM inspection_records WHERE id = :id")
    suspend fun getRecordById(id: String): InspectionRecord?

    @Query("SELECT * FROM inspection_records WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    suspend fun getPendingSyncRecords(): List<InspectionRecord>

    @Query("SELECT * FROM inspection_records WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getPendingSyncRecordsFlow(): Flow<List<InspectionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: InspectionRecord)

    @Update
    suspend fun updateRecord(record: InspectionRecord)

    @Query("UPDATE inspection_records SET syncStatus = :syncStatus, syncTimestamp = :syncTimestamp, remoteServerId = :remoteServerId WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String, syncTimestamp: Long, remoteServerId: String?)

    @Delete
    suspend fun deleteRecord(record: InspectionRecord)

    @Query("SELECT COUNT(*) FROM inspection_records")
    fun getRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inspection_records WHERE hasIrregularity = 1")
    fun getIrregularityCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inspection_records WHERE syncStatus = 'PENDING' OR syncStatus = 'FAILED'")
    fun getPendingCount(): Flow<Int>
}
