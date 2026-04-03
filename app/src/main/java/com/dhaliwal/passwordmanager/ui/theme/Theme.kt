package com.dhaliwal.passwordmanager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.dhaliwal.passwordmanager.utils.Util

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6659),
    onPrimary = Color(0xFF3B0000),

    primaryContainer = Color(0xFF8C1D18),
    onPrimaryContainer = Color(0xFFFFDAD6),

    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF1C1B1F),

    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color(0xFFE0E0E0),

    tertiary = Color(0xFFFF8A80),
    onTertiary = Color(0xFF3B0000),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),

    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCAC4D0),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),

    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFD32F2F),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFFFCDD2),
    onPrimaryContainer = Color(0xFF410002),

    secondary = Color(0xFF9E9E9E),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF1C1B1F),

    tertiary = Color(0xFFB71C1C),
    onTertiary = Color.White,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),

    error = Color(0xFFB3261E),
    onError = Color.White,

    outline = Color(0xFFBDBDBD)
)

@Composable
fun PasswordManagerTheme(
    darkTheme: Boolean = Util.isDarkTheme(LocalContext.current),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}