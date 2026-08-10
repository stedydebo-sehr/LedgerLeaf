package com.ledgerleaf.domain.usecase

import com.ledgerleaf.domain.repository.NewExpense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ExpenseValidationTest {
    private fun valid() = NewExpense(
        amountMinor = 1250,
        currencyCode = "INR",
        categoryId = "category",
        paymentMethodId = "payment",
        subcategoryIds = emptyList(),
        notes = "Lunch",
        occurredAtEpochMillis = 1L,
        isFavorite = false,
        isRecurring = false,
        recurringFrequency = null
    )

    @Test fun acceptsValidExpense() = validateExpense(valid())

    @Test fun rejectsZeroAmount() {
        val ex = assertThrows(IllegalArgumentException::class.java) { validateExpense(valid().copy(amountMinor = 0)) }
        assertEquals("Enter an amount greater than zero.", ex.message)
    }

    @Test fun rejectsBlankNotes() {
        val ex = assertThrows(IllegalArgumentException::class.java) { validateExpense(valid().copy(notes = "   ")) }
        assertEquals("Detailed notes are required.", ex.message)
    }
}
