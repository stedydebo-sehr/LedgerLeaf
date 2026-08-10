package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.CategorySelectionMode
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.repository.ReportableExpense
import org.junit.Assert.assertEquals
import org.junit.Test

class GenerateExpenseReportUseCaseTest {
    private val category = Category("cat", "Food", true, CategorySelectionMode.NONE)
    private val payment = PaymentMethod("pay", "Cash", true, true)

    private fun expense(id: String, amount: Long, time: Long) = Expense(
        id, amount, "INR", category, payment, emptyList(), "note", time, false, false, null
    )

    @Test fun aggregatesDeterministicallyAndCountsArchived() {
        val report = GenerateExpenseReportUseCase().invoke(
            expenses = listOf(
                ReportableExpense(expense("a", 1000, 10), null),
                ReportableExpense(expense("b", 2500, 20), 30)
            ),
            fromEpochMillis = 0,
            toEpochMillis = 100,
            generatedAtEpochMillis = 50
        )
        assertEquals(3500L, report.totalMinor)
        assertEquals(2, report.transactionCount)
        assertEquals(1, report.archivedTransactionCount)
        assertEquals(1750L, report.averageMinor)
        assertEquals(2500L, report.largestExpense?.amountMinor)
        assertEquals(3500L, report.categoryBreakdown.single().amountMinor)
        assertEquals(listOf("b", "a"), report.expenses.map { it.id })
    }

    @Test fun emptyReportIsSafe() {
        val report = GenerateExpenseReportUseCase().invoke(emptyList(), 0, 100, 50)
        assertEquals(0, report.totalMinor)
        assertEquals(0, report.transactionCount)
        assertEquals(0, report.averageMinor)
        assertEquals("INR", report.currencyCode)
    }
}
