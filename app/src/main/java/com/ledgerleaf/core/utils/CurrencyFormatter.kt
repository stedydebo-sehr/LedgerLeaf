package com.ledgerleaf.core.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun format(
        amount: BigDecimal,
        currencyCode: String = "INR",
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}
