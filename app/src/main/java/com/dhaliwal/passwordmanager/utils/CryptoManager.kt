package com.dhaliwal.passwordmanager.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {

    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val ITERATIONS = 100000
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_LENGTH = 128

    // 🔑 Generate random salt (store this)
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        return salt
    }

    // 🔑 Generate AES key from password
    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    // 🔐 Encrypt
    fun encrypt(plainText: String, key: SecretKey): Triple<String, String, String> {
        val cipher = Cipher.getInstance(AES_MODE)

        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        return Triple(
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
            Base64.encodeToString(iv, Base64.DEFAULT),
            "" // salt handled separately
        )
    }

    // 🔓 Decrypt
    fun decrypt(
        encryptedData: String,
        ivString: String,
        key: SecretKey
    ): String {
        val cipher = Cipher.getInstance(AES_MODE)

        val iv = Base64.decode(ivString, Base64.DEFAULT)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedData)

        return String(decryptedBytes)
    }

    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256) // 🔐 256-bit AES key
        return keyGenerator.generateKey()
    }
}