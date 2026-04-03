package com.dhaliwal.passwordmanager.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dhaliwal.passwordmanager.data.local.db.entities.VaultEntryEntity
import com.dhaliwal.passwordmanager.data.local.db.entities.VaultMetadataEntity

@Database(
    entities = [VaultEntryEntity::class, VaultMetadataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vault_database"          // filename
                )
                    .fallbackToDestructiveMigration() // change this for production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}