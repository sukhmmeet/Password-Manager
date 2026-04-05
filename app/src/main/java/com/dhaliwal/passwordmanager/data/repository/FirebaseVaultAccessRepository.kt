package com.dhaliwal.passwordmanager.data.repository

import com.dhaliwal.passwordmanager.data.VaultSession
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager
import com.dhaliwal.passwordmanager.utils.VaultManager
import com.dhaliwal.passwordmanager.utils.VaultOperation
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseVaultAccessRepository @Inject constructor(
    private val database: DatabaseReference
) {

    fun observeVault(uid: String): Flow<List<VaultEntry>> = callbackFlow {

        val ref = database.child("users").child(uid).child("vault")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                try {
                    val encryptedVault = snapshot.getValue(EncryptedVault::class.java)

                    if (encryptedVault == null) {
                        trySend(emptyList())
                        return
                    }

                    val key = VaultSession.getKey()

                    val entries = VaultEncryptionManager.decryptVault(
                        encryptedData = encryptedVault.encryptedData,
                        ivBase64 = encryptedVault.iv,
                        key = key
                    )

                    trySend(entries)

                } catch (e: Exception) {
                    close(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
        }
    }
    suspend fun fetchVaultEntries(uid: String): Result<List<VaultEntry>> {
        return try {
            val snapshot = database.child("users").child(uid).child("vault").get().await()

            val encryptedVault = snapshot.getValue(EncryptedVault::class.java)
                ?: return Result.failure(Exception("Vault not found"))

            if (encryptedVault.encryptedData.isEmpty()) {
                return Result.success(emptyList())
            }

            val decryptedEntries = VaultEncryptionManager.decryptVault(
                encryptedData = encryptedVault.encryptedData,
                ivBase64 = encryptedVault.iv,
                key = VaultSession.getKey()
            )

            Result.success(decryptedEntries)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun modifyVault(uid: String, operation: VaultOperation): Result<Unit> = try {
        val snapshot = database.child("users").child(uid).child("vault").get().await()
        val encryptedVault = snapshot.getValue(EncryptedVault::class.java)
            ?: return Result.failure(Exception("Vault not found"))

        val vaultKey = VaultSession.getKey()

        val modifiedVault = VaultManager.modifyVault(encryptedVault, vaultKey, operation)

        database.child("users").child(uid).child("vault").setValue(modifiedVault).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to modify vault", e))
    }

}

data class VaultEntry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val createdAt: Long
)

data class EncryptedVault(
    val encryptedData: String = "",
    val iv: String = "",
    val updatedAt: Long = 0L
)