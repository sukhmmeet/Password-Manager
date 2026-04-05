package com.dhaliwal.passwordmanager.data

import com.dhaliwal.passwordmanager.data.repository.VaultEntry

object VaultEntryHolder {
    private var currentEntry: VaultEntry? = null

    fun setEntry(entry: VaultEntry) {
        currentEntry = entry
    }

    fun getEntry(): VaultEntry? {
        return currentEntry
    }

    fun clear() {
        currentEntry = null
    }
}