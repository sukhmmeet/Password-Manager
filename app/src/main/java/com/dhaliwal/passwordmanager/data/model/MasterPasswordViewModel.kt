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
    data class Error(val message : String) : PassState()
}
@HiltViewModel
class MasterPasswordViewModel @Inject constructor(
    private val repository: MasterPasswordRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PassState>(PassState.Idle)
    val state = _state

    private var salt: String? = null

    // run this in LaunchedEffect from UI
    fun fetchSalt() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _state.value = PassState.Loading

            val result = repository.getSalt(uid)

            if (result.isSuccess) {
                salt = result.getOrNull()
                _state.value = PassState.Success
            } else {
                _state.value = PassState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to get salt"
                )
            }
        }
    }

}