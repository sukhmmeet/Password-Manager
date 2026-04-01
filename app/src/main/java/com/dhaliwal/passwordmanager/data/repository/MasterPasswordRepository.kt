package com.dhaliwal.passwordmanager.data.repository

import android.util.Base64
import com.dhaliwal.passwordmanager.utils.CryptoManager
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class MasterPasswordRepository @Inject constructor(
    private val database : DatabaseReference
) {
    // set change verify

    // 1 Set Password
    suspend fun setPassword(
        masterPassword: String,
        uid: String,
        salt: String
    ): Result<Unit> {
        return try {
            // 🔹 Decode salt properly
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)

            // 🔹 Derive key
            val key = CryptoManager.deriveKey(masterPassword, saltBytes)

            // 🔹 Encrypt (returns encryptedData + iv)
            val (encryptedData, iv, _) = CryptoManager.encrypt("AUTH_SUCCESS", key)

            val encryptedDataAndIV = EncryptedDataAndIV(
                encryptedData = encryptedData,
                iv = iv
            )

            // 🔹 Store in Firebase
            database.child("users")
                .child(uid)
                .child("masterPassword")
                .setValue(encryptedDataAndIV)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2 Verify
    suspend fun verifyPassword(
        masterPassword: String,
        uid: String,
        salt: String
    ): Result<Unit> {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            val key = CryptoManager.deriveKey(masterPassword, saltBytes)

            val snapshot = database
                .child("users")
                .child(uid)
                .child("masterPassword")
                .get()
                .await()

            val encryptedData = snapshot.child("encryptedData").getValue(String::class.java)
            val iv = snapshot.child("iv").getValue(String::class.java)

            if (encryptedData == null || iv == null) {
                return Result.failure(Exception("Missing data"))
            }

            val decrypted = CryptoManager.decrypt(encryptedData, iv, key)

            return if (decrypted == "AUTH_SUCCESS") {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid password"))
            }

        } catch (e: Exception) {
            Result.failure(Exception("Invalid password"))
        }
    }

    // 3 Change
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        uid: String,
        salt: String
    ): Result<Unit> {
        return try {
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)

            // 🔹 Step 1: Verify old password
            val verifyResult = verifyPassword(oldPassword, uid, salt)
            if (verifyResult.isFailure) {
                return Result.failure(Exception("Old password incorrect"))
            }

            // 🔹 Step 2: Derive new key
            val newKey = CryptoManager.deriveKey(newPassword, saltBytes)

            // 🔹 Step 3: Update master password block
            val (encryptedData, iv, _) = CryptoManager.encrypt("AUTH_SUCCESS", newKey)

            val encryptedDataAndIV = EncryptedDataAndIV(
                encryptedData = encryptedData,
                iv = iv
            )

            database.child("users")
                .child(uid)
                .child("masterPassword")
                .setValue(encryptedDataAndIV)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalt(uid: String): Result<String> {
        return try {
            val snapshot = database
                .child("users")
                .child(uid)
                .child("salt")
                .get()
                .await()

            val salt = snapshot.getValue(String::class.java)

            if (salt != null) {
                Result.success(salt)
            } else {
                Result.failure(Exception("Salt not found"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class EncryptedDataAndIV(
    val encryptedData : String,
    val iv : String
)