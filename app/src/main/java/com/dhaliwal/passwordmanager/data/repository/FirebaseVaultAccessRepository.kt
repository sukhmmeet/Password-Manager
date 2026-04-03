package com.dhaliwal.passwordmanager.data.repository

import com.dhaliwal.passwordmanager.utils.VaultManager
import com.dhaliwal.passwordmanager.utils.VaultOperation
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.crypto.SecretKey
import javax.inject.Inject
import kotlin.coroutines.resume

class FirebaseVaultAccessRepository @Inject constructor(
    private val database: DatabaseReference
) {

    suspend fun fetchVaultKey(uid: String): Result<VaultKeyData?> = try {
        val snapshot = database.child("users").child(uid).child("vaultKey").get().await()
        Result.success(snapshot.getValue(VaultKeyData::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchVault(uid: String): Result<EncryptedVault?> = try {
        val snapshot = database.child("users").child(uid).child("vault").get().await()
        Result.success(snapshot.getValue(EncryptedVault::class.java))
    } catch (e: Exception) {
        Result.failure(Exception("Failed to fetch vault", e))
    }
    private suspend fun modifyVaultTransaction(
        uid: String,
        vaultKey: SecretKey,
        operation: VaultOperation
    ): Result<Unit> = try {
        suspendCancellableCoroutine { cont ->
            val ref = database.child("vaults").child(uid)

            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentWrapper = currentData.getValue(VaultWrapper::class.java)
                        ?: VaultWrapper(
                            encryptedVault = EncryptedVault("", "", System.currentTimeMillis()),
                            version = 0
                        )

                    val updatedEncryptedVault = VaultManager.modifyVault(
                        currentWrapper.encryptedVault,
                        vaultKey,
                        operation
                    )

                    val updatedWrapper = currentWrapper.copy(
                        encryptedVault = updatedEncryptedVault,
                        version = currentWrapper.version + 1
                    )

                    currentData.value = updatedWrapper
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    snapshot: DataSnapshot?
                ) {
                    if (error != null || !committed) {
                        cont.resume(Result.failure(error?.toException() ?: Exception("Transaction failed")))
                    } else {
                        cont.resume(Result.success(Unit))
                    }
                }
            })
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Public suspend functions
    suspend fun addEntry(uid: String, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVaultTransaction(uid, vaultKey, VaultOperation.Add(entry))

    suspend fun updateEntry(uid: String, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVaultTransaction(uid, vaultKey, VaultOperation.Update(entry))

    suspend fun deleteEntry(uid: String, vaultKey: SecretKey, entry: VaultEntry) =
        modifyVaultTransaction(uid, vaultKey, VaultOperation.Delete(entry))
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

data class VaultWrapper(
    val encryptedVault: EncryptedVault = EncryptedVault("", "", 0),
    val version: Long = 0
)