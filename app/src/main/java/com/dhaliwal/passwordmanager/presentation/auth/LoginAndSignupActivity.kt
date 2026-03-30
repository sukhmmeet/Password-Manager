package com.dhaliwal.passwordmanager.presentation.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhaliwal.passwordmanager.R
import com.dhaliwal.passwordmanager.data.remote.FirebaseAuthMethods.loginWithGoogle
import com.dhaliwal.passwordmanager.data.remote.FirebaseAuthMethods.login
import com.dhaliwal.passwordmanager.data.remote.FirebaseAuthMethods.signup
import com.dhaliwal.passwordmanager.presentation.SecurityCheckActivity
import com.dhaliwal.passwordmanager.ui.theme.PasswordManagerTheme
import com.dhaliwal.passwordmanager.utils.Util.isDarkTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class LoginAndSignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasswordManagerTheme(isDarkTheme(LocalContext.current)) {
                AuthScreen()
            }
        }
    }
}

@Composable
fun AuthScreen() {
    val context = LocalContext.current

    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val auth = Firebase.auth

    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            scope.launch {
                Firebase.auth.signInWithCredential(credential).await()
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        OutlinedCard(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                // 🔝 Title
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create an account or login to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔘 Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ToggleItem(
                        text = "Sign Up",
                        selected = !isLogin,
                        onClick = { isLogin = false },
                        modifier = Modifier.weight(1f)
                    )
                    ToggleItem(
                        text = "Login",
                        selected = isLogin,
                        onClick = { isLogin = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 📧 Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔒 Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                passwordVisible = !passwordVisible
                            }
                        )
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ☑️ Terms
                if (!isLogin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = accepted,
                            onCheckedChange = { accepted = it }
                        )
                        Text(
                            text = "I agree to Terms & Conditions",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
                // 🔴 Button
                Button(
                    onClick = {
                        when {
                            email.isBlank() || password.isBlank() -> {
                                Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                            }

                            !isLogin && !accepted -> {
                                Toast.makeText(context, "Accept terms first", Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                if (isLogin) {

                                    login(email, password, auth) { success, error ->

                                        if (success) {
                                            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                            context.startActivity(Intent(context, SecurityCheckActivity::class.java))
                                        } else {
                                            Toast.makeText(context, error ?: "Invalid Credentials", Toast.LENGTH_SHORT).show()
                                            email = ""
                                            password = ""
                                        }
                                    }

                                } else {

                                    signup(email, password, auth) { success, error ->

                                        if (success) {
                                            Toast.makeText(context, "Signup Successful", Toast.LENGTH_SHORT).show()
                                            context.startActivity(Intent(context, SecurityCheckActivity::class.java))
                                        } else {
                                            Toast.makeText(context, error ?: "Failed", Toast.LENGTH_SHORT).show()
                                            email = ""
                                            password = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6
                ) {
                    Text(if (isLogin) "Login Securely" else "Create Account")
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Text(
                        text = "  OR  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                val activity = context as? Activity
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            loginWithGoogle(
                                context = context,
                                scope = scope,
                                launcher = launcher,
                                login = {
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                                    activity?.let {
                                        val intent = Intent(it, SecurityCheckActivity::class.java)
                                        it.startActivity(intent)
                                        it.finish()
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                    ) {

                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "Google Logo",
                            modifier = Modifier.height(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        Color.Transparent

    val textColor = if (selected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor)
    }
}
@Preview
@Composable
fun PreviewLogin(){
    PasswordManagerTheme {
        AuthScreen()
    }
}