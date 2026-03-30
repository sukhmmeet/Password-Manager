package com.dhaliwal.passwordmanager.data.remote

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthMethods {
    fun login(
        email: String,
        password: String,
        auth: FirebaseAuth,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
    fun signup(
        email: String,
        password: String,
        auth: FirebaseAuth,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}