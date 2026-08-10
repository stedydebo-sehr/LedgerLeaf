package com.ledgerleaf.domain.model

data class ReportBreakdownItem(
    val label: String,
    val amountMinor: Long,
    val transactionCount: Int
)

data class ExpenseReport(
    val fromEpochMillis: Long,
    val toEpochMillis: Long,
    val generatedAtEpochMillis: Long,
    val currencyCode: String,
    val totalMinor: Long,
    val transactionCount: Int,
    val archivedTransactionCount: Int,
    val averageMinor: Long,
    val largestExpense: Expense?,
    val categoryBreakdown: List<ReportBreakdownItem>,
    val paymentMethodBreakdown: List<ReportBreakdownItem>,
    val expenses: List<Expense>
)
