package com.ledgerleaf.feature.settings

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.core.ui.components.LedgerLeafTopBar

@Composable
fun SettingsScreen(
    onSearchClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onBudgetsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRecurringClick: () -> Unit,
    onMonthlyClosingClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var profileName by remember(preferences.displayName) { mutableStateOf(preferences.displayName) }
    var currency by remember(preferences.currencyCode) { mutableStateOf(preferences.currencyCode) }
    var budget by remember(preferences.monthlyBudgetMinor) {
        mutableStateOf(preferences.monthlyBudgetMinor?.let(::minorToInput) ?: "")
    }
    var income by remember(preferences.monthlyIncomeMinor) {
        mutableStateOf(preferences.monthlyIncomeMinor?.let(::minorToInput) ?: "")
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::importProfileImage)
    }

    LaunchedEffect(uiState.message, uiState.error) {
        val feedback = uiState.error ?: uiState.message
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        LedgerLeafTopBar(title = "Settings")

        SectionTitle("Profile")
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileImage(preferences.profileImagePath, preferences.displayName)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it.take(80) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.setDisplayName(profileName) }) { Text("Save name") }
                    OutlinedButton(onClick = { photoPicker.launch("image/*") }) { Text("Choose photo") }
                }
                if (!preferences.profileImagePath.isNullOrBlank()) {
                    TextButton(onClick = viewModel::clearProfileImage) { Text("Remove photo") }
                }
            }
        }

        HorizontalDivider()
        SectionTitle("Appearance")
        ThemeMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = preferences.themeMode == mode, onClick = { viewModel.setThemeMode(mode) })
                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        HorizontalDivider()
        SectionTitle("Ledger configuration")
        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it.filter { ch -> ch.isLetter() }.uppercase().take(3) },
            label = { Text("Currency code") },
            supportingText = { Text("Three-letter ISO currency code, for example INR") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Button(onClick = { viewModel.setCurrencyCode(currency) }, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Save currency")
        }

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = moneyInput(it) },
            label = { Text("Monthly budget (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Row(Modifier.padding(horizontal = 16.dp)) {
            Button(onClick = { viewModel.setMonthlyBudget(budget) }) { Text("Save budget") }
            TextButton(onClick = { budget = ""; viewModel.clearMonthlyBudget() }) { Text("Clear") }
        }

        OutlinedTextField(
            value = income,
            onValueChange = { income = moneyInput(it) },
            label = { Text("Monthly income (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Row(Modifier.padding(horizontal = 16.dp)) {
            Button(onClick = { viewModel.setMonthlyIncome(income) }) { Text("Save income") }
            TextButton(onClick = { income = ""; viewModel.clearMonthlyIncome() }) { Text("Clear") }
        }

        Text("Monthly period starts on day ${preferences.monthStartDay}", modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
        Text(
            "Choose a safe start day between 1 and 28.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf(1, 5, 10, 15, 20, 25, 28).forEach { day ->
                TextButton(onClick = { viewModel.setMonthStartDay(day) }) {
                    Text(if (preferences.monthStartDay == day) "[$day]" else day.toString())
                }
            }
        }

        HorizontalDivider()
        SectionTitle("PDF export")
        SettingSwitchRow(
            title = "Include transaction list",
            subtitle = "Adds individual report transactions after the summaries.",
            checked = preferences.pdfIncludeTransactions,
            onCheckedChange = viewModel::setPdfIncludeTransactions
        )
        SettingSwitchRow(
            title = "Include expense notes",
            subtitle = "Adds mandatory expense notes to transaction rows in exported PDFs.",
            checked = preferences.pdfIncludeNotes,
            enabled = preferences.pdfIncludeTransactions,
            onCheckedChange = viewModel::setPdfIncludeNotes
        )

        HorizontalDivider()
        SectionTitle("Data & tools")
        SettingsEntry("Search", onSearchClick)
        SettingsEntry("Favorites & Frequently Used", onFavoritesClick)
        SettingsEntry("Recurring Expenses", onRecurringClick)
        SettingsEntry("Monthly Closing", onMonthlyClosingClick)
        SettingsEntry("Budgets", onBudgetsClick)
        SettingsEntry("Archive", onArchiveClick)
        SettingsEntry("Recycle Bin", onRecycleBinClick)
        SettingsEntry("Backup & Restore", onBackupRestoreClick)

        Text(
            "LedgerLeaf stores settings, profile and finance data locally on this device. No login, analytics, ads, or cloud connection is used.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
        SnackbarHost(snackbarHostState)
    }
}

@Composable
private fun ProfileImage(path: String?, displayName: String) {
    val bitmap = remember(path) {
        path?.takeIf { it.isNotBlank() }?.let { BitmapFactory.decodeFile(it) }?.asImageBitmap()
    }
    Box(
        Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "♧",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
}

@Composable
private fun SettingsEntry(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
    }
    HorizontalDivider()
}

private fun moneyInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot < 0) return filtered
    val whole = filtered.substring(0, firstDot)
    val decimals = filtered.substring(firstDot + 1).replace(".", "").take(2)
    return "$whole.$decimals"
}

private fun minorToInput(minor: Long): String =
    java.math.BigDecimal.valueOf(minor, 2).stripTrailingZeros().toPlainString()
