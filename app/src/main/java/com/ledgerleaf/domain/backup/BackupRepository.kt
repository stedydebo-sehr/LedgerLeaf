package com.ledgerleaf.domain.backup

import com.ledgerleaf.domain.model.AppPreferences

data class BackupSummary(
    val expenseCount: Int,
    val categoryCount: Int,
    val paymentMethodCount: Int,
    val createdAtEpochMillis: Long
)

data class RestoreSummary(
    val expenseCount: Int,
    val categoryCount: Int,
    val paymentMethodCount: Int,
    val sourceFormatVersion: Int
)

interface BackupRepository {
    suspend fun createBackup(): Pair<String, BackupSummary>
    suspend fun restoreBackup(rawJson: String): RestoreSummary
}
