package com.dhaliwal.passwordmanager.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhaliwal.passwordmanager.data.repository.MasterPasswordRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PassState{
    object Idle : PassState()
    object Loading : PassState()
    object Success : PassState()
    object PasswordChanged : PassState()
    data class Error(val message : String) : PassState()
}
sealed class SecurityState{
    object Loading : SecurityState()
    object NeedToSetPassword : SecurityState()
    object NeedToVerifyPassword : SecurityState()
}
@HiltViewModel
class MasterPasswordViewModel @Inject constructor(
    private val repository: MasterPasswordRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PassState>(PassState.Idle)
    private val _securityState = MutableStateFlow<SecurityState>(SecurityState.Loading)
    val state = _state
    val securityState = _securityState

    private var salt: String? = null

    private fun getUid(): String? {
        return Firebase.auth.currentUser?.uid
    }

    fun resetState() {
        _state.value = PassState.Idle
    }

    fun checkUser() {
        val uid = getUid() ?: return

        viewModelScope.launch {
            _securityState.value = SecurityState.Loading

            val result = repository.hasMasterPassword(uid)

            if (result.isSuccess) {
                val hasPassword = result.getOrNull() ?: false

                _securityState.value = if (hasPassword) {
                    SecurityState.NeedToVerifyPassword
                } else {
                    SecurityState.NeedToSetPassword
                }

            } else {
                _state.value = PassState.Error(
                    result.exceptionOrNull()?.message ?: "Something went wrong"
                )
            }
        }
    }

    fun fetchSalt() {
        val uid = getUid() ?: return

        viewModelScope.launch {
            _state.value = PassState.Loading

            val result = repository.getSalt(uid)

            if (result.isSuccess) {
                salt = result.getOrNull()
                _state.value = PassState.Idle
            } else {
                _state.value = PassState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to get salt"
                )
            }
        }
    }

    fun setupMasterPasswordAndVault(masterPassword: String) {
        val uid = getUid() ?: return

        viewModelScope.launch {
            _state.value = PassState.Loading

            val result = repository.initializeSecurity(masterPassword = masterPassword, uid = uid)

            _state.value = if (result.isSuccess) {
                PassState.Success
            } else {
                PassState.Error(
                    result.exceptionOrNull()?.message ?: "Something went wrong"
                )
            }
        }
    }

    fun verifyPassword(masterPassword: String) {
        val uid = getUid()

        if (uid.isNullOrBlank()) {
            _state.value = PassState.Error("User not logged in")
            return
        }

        val currentSalt = salt ?: run {
            _state.value = PassState.Error("Salt not loaded")
            return
        }

        viewModelScope.launch {
            _state.value = PassState.Loading

            val result = repository.verifyPassword(
                masterPassword = masterPassword,
                uid = uid,
                salt = currentSalt
            )

            _state.value = if (result.isSuccess) {
                PassState.Success
            } else {
                PassState.Error(
                    result.exceptionOrNull()?.message ?: "Invalid password"
                )
            }
        }
    }
    fun changePassword(
        oldMasterPassword : String,
        newMasterPassword : String
    ){
        val uid = getUid()

        if (uid.isNullOrBlank()) {
            _state.value = PassState.Error("User not logged in")
            return
        }

        val currentSalt = salt ?: run {
            _state.value = PassState.Error("Salt not loaded")
            return
        }

        viewModelScope.launch {
            _state.value = PassState.Loading
            val result = repository.changePassword(
                oldPassword = oldMasterPassword,
                newPassword = newMasterPassword,
                uid = uid,
                salt = currentSalt
            )
            _state.value = if (result.isSuccess) {
                PassState.PasswordChanged
            } else {
                PassState.Error(
                    result.exceptionOrNull()?.message ?: "Incorrect password"
                )
            }
        }
    }
}