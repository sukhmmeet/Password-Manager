package com.dhaliwal.passwordmanager.presentation.vault

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dhaliwal.passwordmanager.data.VaultEntryHolder
import com.dhaliwal.passwordmanager.data.model.FirebaseVaultViewModel
import com.dhaliwal.passwordmanager.data.model.VaultState
import com.dhaliwal.passwordmanager.data.repository.VaultEntry
import com.dhaliwal.passwordmanager.ui.theme.PasswordManagerTheme
import com.dhaliwal.passwordmanager.utils.AppTheme
import com.dhaliwal.passwordmanager.utils.ThemeMode
import com.dhaliwal.passwordmanager.utils.Util.isDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class VaultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                finish()
            }
        })
        setContent {
            PasswordManagerTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if(isSystemInDarkTheme()) Color.Black else Color.White)
                        .statusBarsPadding()
                ){
                    VaultScreen()
                }
            }
        }
    }
}

@Composable
fun VaultScreen(viewModel: FirebaseVaultViewModel = hiltViewModel()) {
    val state by viewModel.vaultState.collectAsState(initial = VaultState.Idle)

    LaunchedEffect(Unit) {
        viewModel.observeEntries()
    }

    when (val currentState = state) {
        is VaultState.Idle -> FullScreenMessage("Loading vault entries...")
        is VaultState.Loading -> FullScreenMessage("Loading vault entries...")
        is VaultState.Empty -> VaultSuccessScreen(emptyList<VaultEntry>(), viewModel, true)
        is VaultState.Success -> VaultSuccessScreen(currentState.entries)
        is VaultState.Error -> FullScreenMessage("Error: ${currentState.message}")
        VaultState.Updated -> {}
    }
}

@Composable
fun FullScreenMessage(message: String, onAddFirstEntry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onAddFirstEntry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddFirstEntry,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(200.dp)
            ) {
                Text("Add First Entry")
            }
        }
    }
}

@Composable
fun VaultSuccessScreen(entries: List<VaultEntry>, viewModel: FirebaseVaultViewModel = hiltViewModel(), isEmpty : Boolean = false) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDarkTheme = isDarkTheme(context)

    var selectedEntry by remember { mutableStateOf<VaultEntry?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Settings", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = {
                            val newMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                            AppTheme.setTheme(context, newMode)
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = { MinimalTopBar(drawerState, scope) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AddOrEditEntryActivity::class.java)
                        intent.putExtra("isEdit", false)
                        context.startActivity(intent)
                    },
                    containerColor = MaterialTheme.colorScheme.primary, // modern color
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp) // 🔹 larger touch target
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Entry",
                        modifier = Modifier.size(28.dp) // 🔹 bigger icon
                    )
                }
            }
        ) { padding ->
            if(isEmpty){
                FullScreenMessage("Your Vault is empty. Start by adding your first password entry!") {
                    val intent = Intent(context, AddOrEditEntryActivity::class.java)
                    intent.putExtra("isEdit", false)
                    context.startActivity(intent)
                }
            }else{
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    items(entries) { entry ->
                        VaultItem(entry) { selectedEntry = entry }
                    }
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        VaultEntryDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
            onEdit = {
                VaultEntryHolder.setEntry(entry)
                val intent = Intent(context, AddOrEditEntryActivity::class.java)
                intent.putExtra("isEdit", true)
                context.startActivity(intent)
                selectedEntry = null
            },
            onDelete = { showDeleteDialog = true }
        )
    }

    if (showDeleteDialog && selectedEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedEntry = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete ${selectedEntry!!.title}?") },
            confirmButton = { TextButton(onClick = { viewModel.deleteEntry(selectedEntry!!); showDeleteDialog=false; selectedEntry= null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog=false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun VaultItem(entry: VaultEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔹 Icon on the left
            Icon(
                imageVector = Icons.Default.Lock, // use Lock icon for password item
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 🔹 Text Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun VaultEntryDialog(
    entry: VaultEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = null,
        title = {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                // Username
                Text(
                    "Username / Email: ${entry.username}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp)) // 🔹 more space before password

                // Password Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Password: ${if (passwordVisible) entry.password else "••••••••"}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp)) // 🔹 more space before notes

                // Notes
                if (entry.notes.isNotEmpty())
                    Text(
                        "Notes: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                Spacer(modifier = Modifier.height(12.dp)) // 🔹 more space before createdAt

                // Created At
                Text(
                    text = "Created At: ${
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(entry.createdAt))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            Row {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalTopBar(drawerState: DrawerState, scope: CoroutineScope) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Secure Vault",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Drawer",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp) // 🔹 increased size
                )
            }
        }
    )
}