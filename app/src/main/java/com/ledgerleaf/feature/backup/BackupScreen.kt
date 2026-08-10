package com.ledgerleaf.feature.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupScreen(viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingJson by remember { mutableStateOf<String?>(null) }
    var confirmRestoreJson by remember { mutableStateOf<String?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingJson
        if (uri != null && json != null) {
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri, "w")?.bufferedWriter()?.use { it.write(json) }
                            ?: error("Could not open the selected file.")
                    }
                }
                pendingJson = null
                result.onSuccess { viewModel.backupSaved() }
                    .onFailure { snackbar.showSnackbar(it.message ?: "Backup file could not be saved.") }
            }
        } else {
            pendingJson = null
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                            ?: error("Could not read the selected file.")
                    }
                }.onSuccess { confirmRestoreJson = it }
                    .onFailure { snackbar.showSnackbar(it.message ?: "Backup file could not be read.") }
            }
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {
            is BackupUiState.BackupReady -> {
                pendingJson = current.json
                saveLauncher.launch(current.suggestedFileName)
            }
            is BackupUiState.Message -> {
                snackbar.showSnackbar(current.text)
                viewModel.clearFeedback()
            }
            is BackupUiState.Error -> {
                snackbar.showSnackbar(current.text)
                viewModel.clearFeedback()
            }
            else -> Unit
        }
    }

    Column(
        Modifier.fillMaxSize().imePadding().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LedgerLeafTopBar("Backup & Restore")

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Export backup", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Creates a versioned LedgerLeaf JSON backup containing expenses, categories, payment methods, relationships and local preferences. The file stays under your control.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = viewModel::prepareBackup, enabled = state !is BackupUiState.Working) {
                    Text("Choose location & save")
                }
            }
        }

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Restore backup", style = MaterialTheme.typography.titleMedium)
                Text(
                    "LedgerLeaf validates the backup version, IDs and relationships before replacing local finance data in one Room transaction.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain")) },
                    enabled = state !is BackupUiState.Working
                ) { Text("Choose backup file") }
            }
        }

        if (state is BackupUiState.Working) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }
        SnackbarHost(snackbar)
    }

    confirmRestoreJson?.let { json ->
        AlertDialog(
            onDismissRequest = { confirmRestoreJson = null },
            title = { Text("Restore this backup?") },
            text = { Text("Current LedgerLeaf finance data on this device will be replaced only after the selected backup passes validation. This cannot be undone unless you export a backup first.") },
            confirmButton = {
                Button(onClick = {
                    confirmRestoreJson = null
                    viewModel.restore(json)
                }) { Text("Validate & restore") }
            },
            dismissButton = { TextButton(onClick = { confirmRestoreJson = null }) { Text("Cancel") } }
        )
    }
}
