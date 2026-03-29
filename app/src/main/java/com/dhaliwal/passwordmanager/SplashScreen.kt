package com.dhaliwal.passwordmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dhaliwal.passwordmanager.presentation.auth.LoginAndSignupActivity
import com.dhaliwal.passwordmanager.ui.theme.PasswordManagerTheme
import com.dhaliwal.passwordmanager.utils.AppTheme
import com.dhaliwal.passwordmanager.utils.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeMode = AppTheme.getAppTheme(context)

            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            PasswordManagerTheme(isDark) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.password_manager),
                        contentDescription = "Splash Logo",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }
        Handler().postDelayed(
            {
                val intent = Intent(
                    applicationContext,
                    LoginAndSignupActivity::class.java
                )
                startActivity(intent)
            },
            1800
        )
    }
}
