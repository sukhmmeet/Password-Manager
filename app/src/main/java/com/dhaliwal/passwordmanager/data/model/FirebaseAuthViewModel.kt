package com.dhaliwal.passwordmanager.data.model

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhaliwal.passwordmanager.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
@HiltViewModel
class FirebaseAuthViewModel @Inject constructor(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // 🔹 Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.login(email, password)

            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // 🔹 Signup
    fun signup(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.signup(email, password)

            result.fold(
                onSuccess = {
                    val uid = Firebase.auth.currentUser?.uid

                    if (uid != null) {

                        val saltResult = repository.storeSaltInFirebase(uid)

                        saltResult.fold(
                            onSuccess = {
                                _authState.value = AuthState.Success
                            },
                            onFailure = {
                                _authState.value = AuthState.Error("Salt store failed: ${it.message}")
                            }
                        )

                    } else {
                        _authState.value = AuthState.Error("User ID is null")
                    }
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Unknown error")
                }
            )
        }
    }

    // 🔹 Google Login
    fun loginWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        request: GetCredentialRequest
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.loginWithGoogle(
                context,
                credentialManager,
                request
            )

            result.fold(
                onSuccess = {
                    val uid = Firebase.auth.currentUser?.uid

                    if (uid == null) {
                        _authState.value = AuthState.Error("User ID is null")
                        return@launch
                    }

                    try {
                        val saltExists = repository.doesSaltExist(uid)

                        if (!saltExists) {
                            val setupResult = repository.storeSaltInFirebase(uid)

                            setupResult.fold(
                                onSuccess = {
                                    _authState.value = AuthState.Success
                                },
                                onFailure = {
                                    _authState.value = AuthState.Error("Setup failed: ${it.message}")
                                }
                            )
                        } else {
                            _authState.value = AuthState.Success
                        }

                    } catch (e: Exception) {
                        _authState.value = AuthState.Error("Salt check failed: ${e.message}")
                    }
                },
                onFailure = {
                    _authState.value = AuthState.Error(it.message ?: "Google login failed")
                }
            )
        }
    }

    // 🔹 Reset Password
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.sendResetPasswordEmail(email)

            _authState.value = result.fold(
                onSuccess = { AuthState.Success },
                onFailure = { AuthState.Error(it.message ?: "Failed") }
            )
        }
    }
}