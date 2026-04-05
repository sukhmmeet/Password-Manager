package com.dhaliwal.passwordmanager.data

import javax.crypto.SecretKey

object VaultSession {
    private var derivedKey: SecretKey? = null
    private var vaultKey: SecretKey? = null
    private var salt: ByteArray? = null

    fun storeKey(key: SecretKey) {
        derivedKey = key
    }


    fun storeSalt(s: ByteArray) {
        salt = s
    }

    fun getKey(): SecretKey {
        return derivedKey ?: throw IllegalStateException("Derived key not initialized")
    }

    fun getSalt(): ByteArray {
        return salt ?: throw IllegalStateException("Salt not initialized")
    }

    fun clear() {
        derivedKey = null
        vaultKey = null
        salt = null
    }
}