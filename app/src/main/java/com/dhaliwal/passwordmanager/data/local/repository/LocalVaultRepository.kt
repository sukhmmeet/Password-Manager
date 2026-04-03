package com.dhaliwal.passwordmanager.data.local.repository

import com.dhaliwal.passwordmanager.data.local.db.AppDatabase
import com.dhaliwal.passwordmanager.data.local.db.entities.VaultEntryEntity

class LocalVaultRepository(private val db: AppDatabase) {

    private val dao = db.vaultDao()

    suspend fun getAllEntries() = dao.getAllEntries()
    suspend fun insertEntry(entry: VaultEntryEntity) = dao.insertEntry(entry)
    suspend fun insertEntries(entries: List<VaultEntryEntity>) = dao.insertEntries(entries)
    suspend fun deleteEntry(entry: VaultEntryEntity) = dao.deleteEntry(entry)
    suspend fun deleteEntryById(id: String) = dao.deleteEntryById(id)
    suspend fun clearVault() {
        dao.deleteAllEntries()
        dao.deleteAllMetadata()
    }
}