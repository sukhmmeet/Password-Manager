package com.dhaliwal.passwordmanager.utils

import java.util.Base64

private const val AES_MODE = "AES/GCM/NoPadding"
private const val TAG_LENGTH = 128
private const val IV_SIZE = 12

object PasswordManagerHash{

    fun convertToHash(s : String) : String{
        return ""
    }

    fun encryptMessage(s : String) : String{
        val encryptMessageByte = s.encodeToByteArray()
        val encodedMsg = Base64.getEncoder().encodeToString(encryptMessageByte)
        return encodedMsg
    }


}