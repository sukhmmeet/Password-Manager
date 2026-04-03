package com.dhaliwal.passwordmanager.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhaliwal.passwordmanager.data.local.db.entities.VaultEntryEntity

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_entries ORDER BY createdAt DESC")
    suspend fun getAllEntries(): List<VaultEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<VaultEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VaultEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: VaultEntryEntity)

    @Query("DELETE FROM vault_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM vault_entries")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM vault_metadata")
    suspend fun deleteAllMetadata()
}