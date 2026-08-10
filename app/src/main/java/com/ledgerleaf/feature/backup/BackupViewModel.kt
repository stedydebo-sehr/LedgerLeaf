package com.ledgerleaf.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.domain.backup.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Working : BackupUiState
    data class BackupReady(val json: String, val suggestedFileName: String) : BackupUiState
    data class Message(val text: String) : BackupUiState
    data class Error(val text: String) : BackupUiState
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repository: BackupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun prepareBackup() = viewModelScope.launch {
        _uiState.value = BackupUiState.Working
        runCatching { withContext(Dispatchers.IO) { repository.createBackup() } }
            .onSuccess { (json, summary) ->
                val date = java.time.LocalDate.now().toString()
                _uiState.value = BackupUiState.BackupReady(json, "LedgerLeaf-backup-$date.json")
            }
            .onFailure { _uiState.value = BackupUiState.Error(it.message ?: "Backup could not be created.") }
    }

    fun backupSaved(expenseCount: Int? = null) {
        _uiState.value = BackupUiState.Message(if (expenseCount == null) "Backup saved." else "Backup saved with $expenseCount expenses.")
    }

    fun restore(rawJson: String) = viewModelScope.launch {
        _uiState.value = BackupUiState.Working
        runCatching { withContext(Dispatchers.IO) { repository.restoreBackup(rawJson) } }
            .onSuccess { result ->
                _uiState.value = BackupUiState.Message("Restore complete: ${result.expenseCount} expenses restored atomically.")
            }
            .onFailure { error ->
                _uiState.value = BackupUiState.Error(error.message ?: "Backup restore failed. No partial database restore was kept.")
            }
    }

    fun clearFeedback() {
        if (_uiState.value is BackupUiState.Message || _uiState.value is BackupUiState.Error) {
            _uiState.value = BackupUiState.Idle
        }
    }
}
