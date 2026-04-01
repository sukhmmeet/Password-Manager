package com.dhaliwal.passwordmanager.utils

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object KeyUtils {

    // Generate AES key from master password + salt
    fun generateKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 128) // 128-bit key
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    // Generate random salt (store this along with encrypted password)
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun saltToString(salt: ByteArray): String = Base64.encodeToString(salt, Base64.DEFAULT)
    fun stringToSalt(s: String): ByteArray = Base64.decode(s, Base64.DEFAULT)
}