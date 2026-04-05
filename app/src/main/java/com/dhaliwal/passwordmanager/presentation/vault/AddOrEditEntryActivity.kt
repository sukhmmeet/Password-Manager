package com.dhaliwal.passwordmanager.presentation.vault

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dhaliwal.passwordmanager.data.VaultEntryHolder
import com.dhaliwal.passwordmanager.data.model.FirebaseAuthViewModel
import com.dhaliwal.passwordmanager.data.model.FirebaseVaultViewModel
import com.dhaliwal.passwordmanager.data.model.VaultState
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.dhaliwal.passwordmanager.ui.theme.PasswordManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class AddOrEditEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            PasswordManagerTheme {
                val intent = intent
                val isEdit = intent.getBooleanExtra("isEdit", false)
                val entry = if (isEdit) VaultEntryHolder.getEntry() else null
                AddOrEditEntryActivityScreen(initialEntry = entry, isEdit = isEdit)
                VaultEntryHolder.clear()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        finish() // close activity when user leaves
    }
}

@Composable
fun AddOrEditEntryActivityScreen(initialEntry: VaultEntry?, isEdit: Boolean) {
    val id by remember { mutableStateOf(initialEntry?.id ?: UUID.randomUUID().toString()) }
    var name by remember { mutableStateOf(initialEntry?.title ?: "") }
    var username by remember { mutableStateOf(initialEntry?.username ?: "") }
    var password by remember { mutableStateOf(initialEntry?.password ?: "") }
    var notes by remember { mutableStateOf(initialEntry?.notes ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }

    val viewModel: FirebaseVaultViewModel = hiltViewModel()
    val state by viewModel.vaultState.collectAsState(VaultState.Idle)
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is VaultState.Updated -> {
                if (context is ComponentActivity) {
                    context.finish()
                }
            }
            is VaultState.Error -> {
                Toast.makeText(
                    context,
                    "Error: ${(state as VaultState.Error).message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is VaultState.Loading -> {
                Toast.makeText(
                    context,
                    if (isEdit) "Updating entry..." else "Adding entry...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // 🔹 Dynamic Screen Title
                    Text(
                        text = if (isEdit) "Edit Entry" else "Add Entry",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = if (passwordVisible) "Hide Password" else "Show Password")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                    var isButtonClick by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            isButtonClick = true
                            if (isEdit) {
                                viewModel.updateEntry(
                                    VaultEntry(
                                        id = id,
                                        title = name,
                                        username = username,
                                        password = password,
                                        notes = notes,
                                        createdAt = initialEntry?.createdAt ?: System.currentTimeMillis()
                                    )
                                )
                            } else {
                                viewModel.addEntry(
                                    VaultEntry(
                                        id = id,
                                        title = name,
                                        username = username,
                                        password = password,
                                        notes = notes,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isButtonClick && name.isNotBlank() && username.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(
                            if (isButtonClick) "Saving..."
                            else if (isEdit) "Update Entry"
                            else "Add Entry"
                        )
                    }
                }
            }
        }
    }
}