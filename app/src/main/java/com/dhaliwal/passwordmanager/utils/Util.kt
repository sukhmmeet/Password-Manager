package com.dhaliwal.passwordmanager.utils

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

object Util {
    @Composable
    fun isDarkTheme(context: Context) : Boolean{
        val themeMode = AppTheme.getAppTheme(context)
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}