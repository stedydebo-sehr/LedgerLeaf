package com.ledgerleaf

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ledgerleaf.core.export.PdfReportExporter
import com.ledgerleaf.domain.model.ExpenseReport
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfReportExporterTest {
    @Test fun exporterWritesValidPdfHeader() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "ledgerleaf-report-test.pdf").apply { delete() }
        val report = ExpenseReport(0, 1000, 500, "INR", 0, 0, 0, 0, null, emptyList(), emptyList(), emptyList())
        PdfReportExporter(context).export(report, Uri.fromFile(file), includeTransactions = true, includeNotes = true)
        assertTrue(file.length() > 4)
        assertTrue(file.inputStream().use { input -> String(input.readNBytes(4), Charsets.US_ASCII) } == "%PDF")
        file.delete()
    }
}
