package com.dhaliwal.passwordmanager.utils

import com.dhaliwal.passwordmanager.data.repository.EncryptedVault
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager.decryptVault
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager.encryptVault
import javax.crypto.SecretKey

object VaultManager {
    fun modifyVault(
        encryptedVault: EncryptedVault,
        vaultKey: SecretKey,
        operation: VaultOperation
    ): EncryptedVault {
        val list = decryptVault(
            encryptedVault.encryptedData,
            encryptedVault.iv,
            vaultKey
        ).toMutableList()

        when (operation) {
            is VaultOperation.Add -> {
                list.removeAll { it.id == operation.entry.id }
                list.add(operation.entry)
            }
            is VaultOperation.Update -> {
                val index = list.indexOfFirst { it.id == operation.entry.id }
                if (index != -1) list[index] = operation.entry
            }
            is VaultOperation.Delete -> {
                list.removeAll { it.id == operation.entry.id }
            }
        }

        val (data, iv) = encryptVault(list, vaultKey)

        return EncryptedVault(
            encryptedData = data,
            iv = iv,
            updatedAt = encryptedVault.updatedAt
        )
    }

    fun addEntry(encryptedVault: EncryptedVault, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVault(encryptedVault, vaultKey, VaultOperation.Add(entry))

    fun updateEntry(encryptedVault: EncryptedVault, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVault(encryptedVault, vaultKey, VaultOperation.Update(entry))

    fun deleteEntry(encryptedVault: EncryptedVault, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVault(encryptedVault, vaultKey, VaultOperation.Delete(entry))
}

sealed class VaultOperation {
    data class Add(val entry: VaultEntry) : VaultOperation()
    data class Update(val entry: VaultEntry) : VaultOperation()
    data class Delete(val entry: VaultEntry) : VaultOperation()
}