package com.dhaliwal.passwordmanager.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val username: String,
    val password: String,
    val createdAt: Long,
    val updatedAt: Long
)

