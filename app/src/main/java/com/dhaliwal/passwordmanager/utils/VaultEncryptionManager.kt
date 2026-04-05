package com.dhaliwal.passwordmanager.utils

import java.util.Base64
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.google.gson.Gson
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object VaultEncryptionManager {

    private val gson = Gson()
    fun encryptVault(
        entries: List<VaultEntry>,
        key: SecretKey
    ): Pair<String, String> {

        val json = Gson().toJson(entries)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)

        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encryptedBytes = cipher.doFinal(json.toByteArray(Charsets.UTF_8))

        val encryptedData = Base64.getEncoder().encodeToString(encryptedBytes)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)

        return Pair(encryptedData, ivBase64)
    }
    fun decryptVault(
        encryptedData: String,
        ivBase64: String,
        key: SecretKey
    ): List<VaultEntry> {

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val iv = Base64.getDecoder().decode(ivBase64)
        require(iv.size == 12) { "Invalid IV size" }

        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decodedData = Base64.getDecoder().decode(encryptedData)

        val decryptedBytes = try {
            cipher.doFinal(decodedData)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }

        val json = String(decryptedBytes, Charsets.UTF_8)

        val array = gson.fromJson(json, Array<VaultEntry>::class.java)
            ?: throw Exception("Invalid vault data")

        return array.toList()
    }
}