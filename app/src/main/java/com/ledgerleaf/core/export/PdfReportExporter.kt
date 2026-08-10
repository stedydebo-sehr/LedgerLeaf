package com.ledgerleaf.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.ledgerleaf.core.utils.CurrencyFormatter
import com.ledgerleaf.core.utils.DateFormatter
import com.ledgerleaf.domain.model.Expense
import com.ledgerleaf.domain.model.ExpenseReport
import com.ledgerleaf.domain.model.ReportBreakdownItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun export(
        report: ExpenseReport,
        destination: Uri,
        includeTransactions: Boolean,
        includeNotes: Boolean
    ) {
        val document = PdfDocument()
        try {
            val writer = PdfWriter(document)
            writer.title("LedgerLeaf Expense Report")
            writer.text("Period: ${date(report.fromEpochMillis)} - ${date(report.toEpochMillis)}")
            writer.text("Generated: ${dateTime(report.generatedAtEpochMillis)}")
            writer.space(8f)

            writer.section("Summary")
            writer.keyValue("Total", money(report.totalMinor, report.currencyCode))
            writer.keyValue("Transactions", report.transactionCount.toString())
            writer.keyValue("Archived transactions", report.archivedTransactionCount.toString())
            writer.keyValue("Average", money(report.averageMinor, report.currencyCode))
            report.largestExpense?.let {
                writer.keyValue("Largest expense", "${money(it.amountMinor, it.currencyCode)} - ${it.category.name}")
            }

            writer.section("By category")
            report.categoryBreakdown.forEach { writer.breakdown(it, report.currencyCode) }

            writer.section("By payment method")
            report.paymentMethodBreakdown.forEach { writer.breakdown(it, report.currencyCode) }

            if (includeTransactions) {
                writer.section("Transactions")
                if (report.expenses.isEmpty()) {
                    writer.text("No reportable expenses in this period.")
                } else {
                    report.expenses.forEachIndexed { index, expense ->
                        writer.expense(index + 1, expense, includeNotes)
                    }
                }
            }

            writer.finish()
            context.contentResolver.openOutputStream(destination, "w")?.use { output ->
                document.writeTo(output)
            } ?: error("Unable to open the selected PDF destination.")
        } finally {
            document.close()
        }
    }

    private fun money(amountMinor: Long, currencyCode: String): String =
        CurrencyFormatter.format(BigDecimal.valueOf(amountMinor, 2), currencyCode)

    private fun date(epochMillis: Long): String =
        DateFormatter.formatDate(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate())

    private fun dateTime(epochMillis: Long): String {
        val value = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return "${DateFormatter.formatDate(value.toLocalDate())} ${value.toLocalTime().withSecond(0).withNano(0)}"
    }

    private inner class PdfWriter(private val document: PdfDocument) {
        private val pageWidth = 595
        private val pageHeight = 842
        private val margin = 42f
        private val contentWidth = pageWidth - (margin * 2)
        private val body = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        private val bold = Paint(body).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
        private val titlePaint = Paint(bold).apply { textSize = 20f }
        private val sectionPaint = Paint(bold).apply { textSize = 13f }
        private val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 0.8f }

        private var pageNumber = 0
        private var page: PdfDocument.Page? = null
        private var y = margin

        init { newPage() }

        fun title(value: String) {
            ensure(30f)
            canvas().drawText(value, margin, y, titlePaint)
            y += 30f
        }

        fun section(value: String) {
            ensure(34f)
            y += 8f
            canvas().drawText(value, margin, y, sectionPaint)
            y += 8f
            canvas().drawLine(margin, y, pageWidth - margin, y, divider)
            y += 15f
        }

        fun text(value: String, indent: Float = 0f) {
            wrap(value, body, contentWidth - indent).forEach { line ->
                ensure(15f)
                canvas().drawText(line, margin + indent, y, body)
                y += 15f
            }
        }

        fun keyValue(key: String, value: String) {
            ensure(17f)
            canvas().drawText(key, margin, y, body)
            val valueWidth = body.measureText(value)
            canvas().drawText(value, pageWidth - margin - valueWidth, y, bold)
            y += 17f
        }

        fun breakdown(item: ReportBreakdownItem, currencyCode: String) {
            val label = "${item.label} (${item.transactionCount})"
            keyValue(label, money(item.amountMinor, currencyCode))
        }

        fun expense(index: Int, expense: Expense, includeNotes: Boolean) {
            ensure(if (includeNotes) 64f else 44f)
            val amount = money(expense.amountMinor, expense.currencyCode)
            canvas().drawText("$index. ${date(expense.occurredAtEpochMillis)}", margin, y, bold)
            val amountWidth = bold.measureText(amount)
            canvas().drawText(amount, pageWidth - margin - amountWidth, y, bold)
            y += 15f
            text("${expense.category.name} • ${expense.paymentMethod.name}", 10f)
            if (expense.subcategories.isNotEmpty()) {
                text("Subcategories: ${expense.subcategories.joinToString { it.name }}", 10f)
            }
            if (includeNotes) text("Notes: ${expense.notes}", 10f)
            ensure(8f)
            y += 4f
            canvas().drawLine(margin, y, pageWidth - margin, y, divider)
            y += 8f
        }

        fun space(points: Float) {
            ensure(points)
            y += points
        }

        fun finish() {
            finishPage()
        }

        private fun ensure(required: Float) {
            if (y + required > pageHeight - margin - 24f) newPage()
        }

        private fun newPage() {
            finishPage()
            pageNumber += 1
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            y = margin
            val footer = "LedgerLeaf • Page $pageNumber"
            val width = body.measureText(footer)
            canvas().drawText(footer, pageWidth - margin - width, pageHeight - 20f, body)
        }

        private fun finishPage() {
            page?.let(document::finishPage)
            page = null
        }

        private fun canvas() = requireNotNull(page).canvas

        private fun wrap(value: String, paint: Paint, maxWidth: Float): List<String> {
            if (value.isBlank()) return listOf("")
            val lines = mutableListOf<String>()
            value.split("\n").forEach { paragraph ->
                var current = ""
                paragraph.split(Regex("\\s+")).filter { it.isNotBlank() }.forEach { word ->
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = candidate
                    } else {
                        if (current.isNotEmpty()) lines += current
                        current = word
                    }
                }
                if (current.isNotEmpty()) lines += current
            }
            return lines.ifEmpty { listOf("") }
        }
    }
}
