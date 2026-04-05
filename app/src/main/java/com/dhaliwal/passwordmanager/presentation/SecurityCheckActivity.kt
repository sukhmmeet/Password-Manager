package com.dhaliwal.passwordmanager.presentation

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dhaliwal.passwordmanager.data.model.MasterPasswordViewModel
import com.dhaliwal.passwordmanager.data.model.PassState
import com.dhaliwal.passwordmanager.data.model.SecurityState
import com.dhaliwal.passwordmanager.presentation.vault.VaultActivity
import com.dhaliwal.passwordmanager.ui.theme.PasswordManagerTheme
import com.dhaliwal.passwordmanager.utils.AppTheme
import com.dhaliwal.passwordmanager.utils.Util.isDarkTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecurityCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            PasswordManagerTheme() {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(if(isSystemInDarkTheme()) Color.Black else Color.White)
                        .statusBarsPadding()
                ){
                    SecurityCheckUI()
                }
            }
        }
    }
}

@Composable
fun SecurityCheckUI() {

    val viewModel: MasterPasswordViewModel = hiltViewModel()
    val securityState by viewModel.securityState.collectAsState()
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Initialize on first composition
    LaunchedEffect(Unit) {
        viewModel.refreshSecurityCheck()
    }

    // Handle lifecycle events to refresh when activity resumes
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSecurityCheck()
            }
        }
        activity?.lifecycle?.addObserver(observer)

        onDispose {
            activity?.lifecycle?.removeObserver(observer)
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {

            is PassState.Error -> {
                Toast.makeText(context, current.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }

            PassState.PasswordChanged -> {
                Toast.makeText(context, "Password Changed", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }

            PassState.Success -> {
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

                context.startActivity(Intent(context, VaultActivity::class.java))

                viewModel.resetState()
            }

            PassState.PasswordChangeLoading -> {
                Toast.makeText(context, "Changing password...", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    when (securityState) {

        is SecurityState.Loading -> {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is SecurityState.NeedToSetPassword -> {
            SetPassword(viewModel = viewModel)
        }

        is SecurityState.NeedToVerifyPassword -> {

            VerifyPassword(
                viewModel = viewModel,
                onChangePasswordClick = { showDialog = true }
            )

            if (showDialog) {
                ChangePasswordDialog(
                    onDismiss = { showDialog = false },
                    onConfirm = { old, new ->
                        viewModel.changePassword(old, new)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SetPassword(viewModel: MasterPasswordViewModel) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Set Master Password",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This password will protect all your data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔒 Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Master Password") },
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

                Spacer(modifier = Modifier.height(16.dp))

                // 🔁 Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                val state by viewModel.state.collectAsState()
                val isLoading = state is PassState.Loading

                Button(
                    onClick = {
                        viewModel.setupMasterPasswordAndVault(masterPassword = password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = password.length >= 6 &&
                            password == confirmPassword &&
                            !isLoading
                ) {
                    Text(if (isLoading) "Please wait..." else "Set Password")
                }
            }
        }
    }
}
@Composable
fun VerifyPassword(
    onChangePasswordClick: () -> Unit = {},
    viewModel: MasterPasswordViewModel
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Enter Master Password",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlock your vault",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Master Password") },
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Change Password?",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onChangePasswordClick()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                val state by viewModel.state.collectAsState()
                val isLoading = state is PassState.Loading

                Button(
                    onClick = {
                        viewModel.verifyPassword(password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = password.isNotBlank() && !isLoading
                ) {
                    Text(if (isLoading) "Unlocking..." else "Unlock")
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPass: String, newPass: String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Change Password")
        },
        text = {
            Column {

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null)
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(oldPassword, newPassword)
                },
                enabled = newPassword.length >= 6 &&
                        newPassword == confirmPassword &&
                        oldPassword.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
