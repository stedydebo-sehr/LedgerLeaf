package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.model.ExpenseReport
import com.ledgerleaf.domain.model.ReportBreakdownItem
import com.ledgerleaf.domain.repository.ReportableExpense
import javax.inject.Inject

class GenerateExpenseReportUseCase @Inject constructor() {
    operator fun invoke(
        expenses: List<ReportableExpense>,
        fromEpochMillis: Long,
        toEpochMillis: Long,
        generatedAtEpochMillis: Long = System.currentTimeMillis()
    ): ExpenseReport {
        val ordered = expenses.sortedByDescending { it.expense.occurredAtEpochMillis }
        val domainExpenses = ordered.map { it.expense }
        val total = domainExpenses.sumOf { it.amountMinor }
        val currency = domainExpenses.firstOrNull()?.currencyCode ?: "INR"

        fun breakdownBy(label: (ReportableExpense) -> String): List<ReportBreakdownItem> =
            ordered.groupBy(label)
                .map { (name, rows) ->
                    ReportBreakdownItem(name, rows.sumOf { it.expense.amountMinor }, rows.size)
                }
                .sortedWith(compareByDescending<ReportBreakdownItem> { it.amountMinor }.thenBy { it.label })

        return ExpenseReport(
            fromEpochMillis = fromEpochMillis,
            toEpochMillis = toEpochMillis,
            generatedAtEpochMillis = generatedAtEpochMillis,
            currencyCode = currency,
            totalMinor = total,
            transactionCount = domainExpenses.size,
            archivedTransactionCount = ordered.count { it.archivedAtEpochMillis != null },
            averageMinor = if (domainExpenses.isEmpty()) 0L else total / domainExpenses.size,
            largestExpense = domainExpenses.maxByOrNull { it.amountMinor },
            categoryBreakdown = breakdownBy { it.expense.category.name },
            paymentMethodBreakdown = breakdownBy { it.expense.paymentMethod.name },
            expenses = domainExpenses
        )
    }
}
