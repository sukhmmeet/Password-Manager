package com.dhaliwal.passwordmanager.utils

import com.dhaliwal.passwordmanager.data.repository.EncryptedVault
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager.decryptVault
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager.encryptVault
import javax.crypto.SecretKey

object VaultManager {

    fun addVaultEntry(
        newEntry: VaultEntry,
        encryptedVault: EncryptedVault,
        vaultKey: SecretKey
    ): EncryptedVault = modifyVault(encryptedVault, vaultKey) {
        it.add(newEntry)
    }

    fun updateVaultEntry(
        selectedEntry: VaultEntry,
        encryptedVault: EncryptedVault,
        vaultKey: SecretKey
    ): EncryptedVault = modifyVault(encryptedVault, vaultKey) {
        val index = it.indexOfFirst { e -> e.id == selectedEntry.id }
        if (index != -1) {
            it[index] = selectedEntry
        }
    }

    fun deleteVaultEntry(
        selectedEntry: VaultEntry,
        encryptedVault: EncryptedVault,
        vaultKey: SecretKey
    ): EncryptedVault = modifyVault(encryptedVault, vaultKey) {
        it.removeAll { e -> e.id == selectedEntry.id }
    }

    private fun modifyVault(
        encryptedVault: EncryptedVault,
        vaultKey: SecretKey,
        block: (MutableList<VaultEntry>) -> Unit
    ): EncryptedVault {
        val list = decryptVault(
            encryptedVault.encryptedData,
            encryptedVault.iv,
            vaultKey
        ).toMutableList()

        block(list)

        val (data, iv) = encryptVault(list, vaultKey)

        return EncryptedVault(
            encryptedData = data,
            iv = iv,
            updatedAt = System.currentTimeMillis()
        )
    }
}