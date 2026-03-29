package com.dhaliwal.passwordmanager.utils

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.edit

object AppTheme {

    private const val PREF_NAME = "theme_pref"
    private const val KEY_THEME = "theme_mode"

    fun getAppTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return ThemeMode.valueOf(value!!)
    }

    fun setAppTheme(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, mode.name) }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// it used when need of theme
//val context = LocalContext.current
//val themeMode = AppTheme.getTheme(context)
//val systemDark = isSystemInDarkTheme()
//
//val isDark = when (themeMode) {
//    ThemeMode.LIGHT -> false
//    ThemeMode.DARK -> true
//    ThemeMode.SYSTEM -> systemDark
//}