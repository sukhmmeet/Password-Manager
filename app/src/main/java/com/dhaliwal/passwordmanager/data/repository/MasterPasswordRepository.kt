package com.dhaliwal.passwordmanager.data.repository

import android.util.Base64
import com.dhaliwal.passwordmanager.utils.CryptoManager
import com.dhaliwal.passwordmanager.utils.VaultEncryptionManager.encryptVault
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MasterPasswordRepository @Inject constructor(
    private val database: DatabaseReference
) {

    // Initialize security for a user (only once)
    suspend fun initializeSecurity(masterPassword: String, uid: String): Result<Unit> {
        return try {
            val userRef = database.child("users").child(uid)
            val snapshot = userRef.get().await()

            val initialized = snapshot.child("securityInitialized").getValue(Boolean::class.java) ?: false
            if (initialized) return Result.failure(Exception("Security already initialized"))

            val saltBytes = CryptoManager.generateSalt() // ByteArray
            val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
            val derivedKey = CryptoManager.deriveKey(masterPassword, saltBytes)

            val vaultKey = CryptoManager.generateAESKey()
            val vaultKeyBase64 = Base64.encodeToString(vaultKey.encoded, Base64.NO_WRAP)

            val (encryptedVaultKey, keyIv, _) = CryptoManager.encrypt(vaultKeyBase64, derivedKey)

            val vaultKeyData = VaultKeyData(
                encryptedVaultKey = encryptedVaultKey,
                iv = keyIv
            )

            val (encryptedData, dataIv) = encryptVault(emptyList(), vaultKey)
            val vault = EncryptedVault(
                encryptedData = encryptedData,
                iv = dataIv,
                updatedAt = System.currentTimeMillis()
            )

            val updates = mapOf(
                "vaultKey" to vaultKeyData,
                "vault" to vault,
                "salt" to saltBase64,
                "securityInitialized" to true
            )
            userRef.updateChildren(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPassword(masterPassword: String, uid: String, saltBase64: String): Result<Unit> {
        return try {
            val saltBytes = Base64.decode(saltBase64, Base64.NO_WRAP)
            val key = CryptoManager.deriveKey(masterPassword, saltBytes)

            val (encryptedData, iv, _) = CryptoManager.encrypt("AUTH_SUCCESS", key)
            val data = EncryptedDataAndIV(encryptedData, iv)

            database.child("users")
                .child(uid)
                .child("masterPassword")
                .setValue(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPassword(masterPassword: String, uid: String, salt: String): Result<Unit> {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            val key = CryptoManager.deriveKey(masterPassword, saltBytes)

            val snapshot = database.child("users").child(uid).child("masterPassword").get().await()
            val encryptedData = snapshot.child("encryptedData").getValue(String::class.java)
            val iv = snapshot.child("iv").getValue(String::class.java)

            requireNotNull(encryptedData)
            requireNotNull(iv)

            val decrypted = CryptoManager.decrypt(encryptedData, iv, key)
            if (decrypted == "AUTH_SUCCESS") Result.success(Unit)
            else Result.failure(Exception("Invalid password"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String, uid: String, salt: String): Result<Unit> {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)

            val verifyResult = verifyPassword(oldPassword, uid, salt)
            if (verifyResult.isFailure) return Result.failure(Exception("Old password incorrect"))

            val newKey = CryptoManager.deriveKey(newPassword, saltBytes)
            val (encryptedData, iv, _) = CryptoManager.encrypt("AUTH_SUCCESS", newKey)
            val data = EncryptedDataAndIV(encryptedData, iv)

            database.child("users").child(uid).child("masterPassword").setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasMasterPassword(uid: String): Result<Boolean> {
        return try {
            val snapshot = database.child("users").child(uid).child("masterPassword").get().await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalt(uid: String): Result<String> {
        return try {
            val snapshot = database.child("users").child(uid).child("salt").get().await()
            val saltBase64 = snapshot.getValue(String::class.java)
            if (saltBase64 != null) Result.success(saltBase64)
            else Result.failure(Exception("Salt not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class EncryptedDataAndIV(
    val encryptedData: String,
    val iv: String
)

data class VaultKeyData(
    val encryptedVaultKey: String = "",
    val iv: String = ""
)
