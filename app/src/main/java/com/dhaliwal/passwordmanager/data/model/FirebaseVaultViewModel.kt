package com.dhaliwal.passwordmanager.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhaliwal.passwordmanager.data.repository.FirebaseVaultAccessRepository
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.dhaliwal.passwordmanager.utils.VaultOperation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VaultState {
    object Idle : VaultState()
    object Loading : VaultState()
    object Empty : VaultState()
    data class Success(val entries: List<VaultEntry>) : VaultState()
    object Updated : VaultState()
    data class Error(val message: String) : VaultState()
}
@HiltViewModel
class FirebaseVaultViewModel @Inject constructor(
    private val repository: FirebaseVaultAccessRepository,
) : ViewModel() {

    private val _vaultState = MutableStateFlow<VaultState>(VaultState.Idle)
    val vaultState: Flow<VaultState> = _vaultState

    private var observeJob: Job? = null

    fun observeEntries() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            _vaultState.value = VaultState.Loading

            repository.observeVault(uid).collect { entries ->
                _vaultState.value = if (entries.isEmpty()) {
                    VaultState.Empty
                } else {
                    VaultState.Success(entries)
                }
            }
        }
    }

    fun addEntry(entry: VaultEntry) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = repository.modifyVault(
                uid = uid,
                operation = VaultOperation.Add(entry)
            )
            if (result.isSuccess){
                _vaultState.value = VaultState.Updated
            }
            if (result.isFailure) {
                _vaultState.value = VaultState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to add entry"
                )
            }
        }
    }

    fun deleteEntry(entry: VaultEntry) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = repository.modifyVault(
                uid = uid,
                operation = VaultOperation.Delete(entry)
            )

            if (result.isFailure) {
                _vaultState.value = VaultState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete entry"
                )
            }
        }
    }

    fun updateEntry(entry: VaultEntry) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val result = repository.modifyVault(
                uid = uid,
                operation = VaultOperation.Update(entry)
            )

            if (result.isSuccess){
                _vaultState.value = VaultState.Updated
            }

            if (result.isFailure) {
                _vaultState.value = VaultState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update entry"
                )
            }
        }
    }

    fun resetState() {
        _vaultState.value = VaultState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}