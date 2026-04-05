package com.dhaliwal.passwordmanager.utils

import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.crypto.SecretKey

class VaultEncryptionManagerTest {
    var deriveKey : SecretKey
    private var password = "testpassword"
    private val salt = CryptoManager.generateSalt()

    init {
        deriveKey = CryptoManager.deriveKey(password, salt)
    }
    private val entries = listOf(
        VaultEntry(
            id = "1",
            title = "Google",
            username = "testuser",
            password = "testpass",
            notes = "Test note",
            createdAt = System.currentTimeMillis()
        )
    )
    lateinit var encryptedData : String
    lateinit var iv : String

    @Before
    fun setup() {
        val (encData, ivBase64) = VaultEncryptionManager.encryptVault(entries, deriveKey)
        encryptedData = encData
        iv = ivBase64
    }
    @Test(expected = SecurityException::class)
    fun testWrongKeyFails() {
        val wrongKey = CryptoManager.generateAESKey()
        VaultEncryptionManager.decryptVault(encryptedData, iv, wrongKey)
    }
    @Test(expected = SecurityException::class)
    fun testTamperedDataFails() {
        val tamperedData = encryptedData.dropLast(2) + "AA"
        VaultEncryptionManager.decryptVault(tamperedData, iv, deriveKey)
    }
    @Test
    fun testEncryptionDecryption() {
        val decryptedEntries = VaultEncryptionManager.decryptVault(encryptedData, iv, deriveKey)
        assertEquals(entries.size, decryptedEntries.size)
        assertEquals(entries[0].id, decryptedEntries[0].id)
        assertEquals(entries[0].title, decryptedEntries[0].title)
        assertEquals(entries[0].username, decryptedEntries[0].username)
        assertEquals(entries[0].password, decryptedEntries[0].password)
        assertEquals(entries[0].notes, decryptedEntries[0].notes)
    }

}