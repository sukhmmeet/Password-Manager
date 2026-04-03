package com.dhaliwal.passwordmanager.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_metadata")
data class VaultMetadataEntity(
    @PrimaryKey val id: Int = 0,
    val version: Long,
    val lastSynced: Long
)