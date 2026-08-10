package com.ledgerleaf.feature.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerleaf.core.export.PdfReportExporter
import com.ledgerleaf.domain.model.ExpenseReport
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.SettingsRepository
import com.ledgerleaf.domain.usecase.GenerateExpenseReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReportsUiState(
    val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    val endDate: LocalDate = LocalDate.now(),
    val report: ExpenseReport? = null,
    val isGenerating: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val settingsRepository: SettingsRepository,
    private val generateReport: GenerateExpenseReportUseCase,
    private val pdfExporter: PdfReportExporter
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState
    private var generationJob: Job? = null

    fun generate(start: LocalDate, end: LocalDate) {
        if (end.isBefore(start)) {
            _uiState.value = _uiState.value.copy(error = "End date cannot be before start date.", message = null)
            return
        }
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val from = start.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            _uiState.value = _uiState.value.copy(startDate = start, endDate = end, isGenerating = true, error = null, message = null)
            repository.observeReportableExpensesInRange(from, to).collectLatest { rows ->
                _uiState.value = _uiState.value.copy(
                    startDate = start,
                    endDate = end,
                    report = generateReport(rows, from, to),
                    isGenerating = false,
                    error = null
                )
            }
        }
    }

    fun exportPdf(destination: Uri) {
        val report = _uiState.value.report ?: return
        if (_uiState.value.isExporting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null, message = null)
            runCatching {
                val preferences = settingsRepository.preferences.first()
                withContext(Dispatchers.IO) {
                    pdfExporter.export(
                        report = report,
                        destination = destination,
                        includeTransactions = preferences.pdfIncludeTransactions,
                        includeNotes = preferences.pdfIncludeTransactions && preferences.pdfIncludeNotes
                    )
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isExporting = false, message = "PDF exported successfully.")
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = throwable.message ?: "Unable to export PDF."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
