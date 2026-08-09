package com.ledgerleaf.core.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateFormatter {
    fun formatDate(
        date: LocalDate,
        locale: Locale = Locale.getDefault()
    ): String = date.format(
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
    )

    fun formatDateTime(
        dateTime: LocalDateTime,
        locale: Locale = Locale.getDefault()
    ): String = dateTime.format(
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
    )
}
