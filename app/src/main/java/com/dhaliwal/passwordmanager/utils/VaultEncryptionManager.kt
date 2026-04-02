package com.dhaliwal.passwordmanager.utils

import android.util.Base64
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.google.gson.Gson
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object VaultEncryptionManager {
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

        val encryptedData = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

        return Pair(encryptedData, ivBase64)
    }
    fun decryptVault(
        encryptedData: String,
        ivBase64: String,
        key: SecretKey
    ): List<VaultEntry> {

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decodedData = Base64.decode(encryptedData, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedData)

        val json = String(decryptedBytes, Charsets.UTF_8)

        return Gson().fromJson(json, Array<VaultEntry>::class.java).toList()
    }
}