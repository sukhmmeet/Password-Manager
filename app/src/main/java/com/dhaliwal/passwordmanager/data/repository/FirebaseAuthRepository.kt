package com.dhaliwal.passwordmanager.data.repository

import android.content.Context
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.dhaliwal.passwordmanager.utils.CryptoManager
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Signup
    suspend fun signup(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun storeSaltInFirebase(uid: String): Result<Unit> {
        return try {
            val database = Firebase.database.reference

            val saltBytes = CryptoManager.generateSalt()
            val saltString = Base64.encodeToString(saltBytes, Base64.NO_WRAP)

            database.child("users")
                .child(uid)
                .child("salt")
                .setValue(saltString)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun doesSaltExist(uid: String): Boolean {
        val snapshot = Firebase.database.reference
            .child("users")
            .child(uid)
            .child("salt")
            .get()
            .await()

        return snapshot.exists()
    }

    // 🔹 Reset Password
    suspend fun sendResetPasswordEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Google Login (Clean version)
    suspend fun loginWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        request: GetCredentialRequest
    ): Result<Unit> {
        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken,
                    null
                )

                auth.signInWithCredential(firebaseCredential).await()

                Result.success(Unit)

            } else {
                Result.failure(Exception("Invalid credential type"))
            }

        } catch (e: NoCredentialException) {
            Result.failure(Exception("No Google account found"))
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Logout
    fun logout() {
        auth.signOut()
    }
}