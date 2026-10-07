package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AuditLog
import com.example.data.model.CoordenadaGeograficaEntity
import com.example.data.model.DenunciaEntity
import com.example.data.model.FotoMetadataEntity
import com.example.data.model.InspectionRecord

@Database(
    entities = [
        InspectionRecord::class,
        AuditLog::class,
        DenunciaEntity::class,
        CoordenadaGeograficaEntity::class,
        FotoMetadataEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspectionDao(): InspectionDao
    abstract fun auditDao(): AuditDao
    abstract fun denunciaDao(): DenunciaDao
    abstract fun coordenadaDao(): CoordenadaDao
    abstract fun fotoMetadataDao(): FotoMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geofiscal_sema.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
