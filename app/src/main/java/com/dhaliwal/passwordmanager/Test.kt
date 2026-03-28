package com.dhaliwal.passwordmanager

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Test {

    private lateinit var auth: FirebaseAuth

    fun runFirebase(context: Context) {
        auth = Firebase.auth

        auth.createUserWithEmailAndPassword("test1@gmail.com", "123456789")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(context, "Signup Success", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Signup Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}