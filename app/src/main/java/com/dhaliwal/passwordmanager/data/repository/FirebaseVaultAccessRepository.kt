package com.dhaliwal.passwordmanager.data.repository

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseVaultAccessRepository @Inject constructor(
    private val database: DatabaseReference
) {
    suspend fun fetchVaultKey(uid : String) : Result<VaultKeyData?> {
        return try {
            val snapshot = database
                .child("users")
                .child(uid)
                .child("vaultKey")
                .get()
                .await()
            val vaultKeyData = snapshot.getValue(VaultKeyData::class.java)
            Result.success(vaultKeyData)
        }catch (e : Exception){
            Result.failure(e)
        }
    }
    suspend fun fetchVault(uid: String): Result<EncryptedVault> {
        return try {
            val snapshot = database
                .child("users")
                .child(uid)
                .child("vault")
                .get()
                .await()

            val vault = snapshot.getValue(EncryptedVault::class.java)
                ?: return Result.failure(Exception("Vault is empty"))

            Result.success(vault)

        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch vault", e))
        }
    }
    fun addEntryToFirebase(
        uid : String,
        vaultEntry: VaultEntry,
        vaultKeyData: VaultKeyData
    ) : Result<Unit>{
        return try {

            Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }
    }
}
data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val createdAt: Long
)
data class EncryptedVault(
    val encryptedData: String,
    val iv: String,
    val updatedAt: Long
)