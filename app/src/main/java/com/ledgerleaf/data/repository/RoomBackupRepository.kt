package com.ledgerleaf.data.repository

import com.ledgerleaf.core.database.LedgerLeafDatabase
import com.ledgerleaf.core.datastore.ThemeMode
import com.ledgerleaf.data.local.dao.BackupDao
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.ExpenseEntity
import com.ledgerleaf.data.local.entity.ExpenseSubcategoryCrossRef
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity
import com.ledgerleaf.domain.backup.BackupRepository
import com.ledgerleaf.domain.backup.BackupSummary
import com.ledgerleaf.domain.backup.RestoreSummary
import com.ledgerleaf.domain.model.AppPreferences
import com.ledgerleaf.domain.repository.SettingsRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class RoomBackupRepository @Inject constructor(
    private val dao: BackupDao,
    private val settingsRepository: SettingsRepository
) : BackupRepository {

    override suspend fun createBackup(): Pair<String, BackupSummary> {
        val createdAt = System.currentTimeMillis()
        val categories = dao.getCategories()
        val subcategories = dao.getSubcategories()
        val paymentMethods = dao.getPaymentMethods()
        val expenses = dao.getExpenses()
        val refs = dao.getExpenseSubcategoryRefs()
        val preferences = settingsRepository.preferences.first()

        val root = JSONObject()
            .put("format", FORMAT_NAME)
            .put("formatVersion", FORMAT_VERSION)
            .put("databaseVersion", LedgerLeafDatabase.VERSION)
            .put("createdAtEpochMillis", createdAt)
            .put("preferences", preferences.toJson())
            .put("categories", categories.toJsonArray { it.toJson() })
            .put("subcategories", subcategories.toJsonArray { it.toJson() })
            .put("paymentMethods", paymentMethods.toJsonArray { it.toJson() })
            .put("expenses", expenses.toJsonArray { it.toJson() })
            .put("expenseSubcategories", refs.toJsonArray { it.toJson() })

        return root.toString() to BackupSummary(
            expenseCount = expenses.size,
            categoryCount = categories.size,
            paymentMethodCount = paymentMethods.size,
            createdAtEpochMillis = createdAt
        )
    }

    override suspend fun restoreBackup(rawJson: String): RestoreSummary {
        val root = try {
            JSONObject(rawJson)
        } catch (_: Exception) {
            throw IllegalArgumentException("This is not a valid LedgerLeaf backup file.")
        }

        require(root.optString("format") == FORMAT_NAME) { "Unsupported backup format." }
        val version = root.optInt("formatVersion", -1)
        require(version in 1..FORMAT_VERSION) {
            if (version > FORMAT_VERSION) "This backup was created by a newer LedgerLeaf version."
            else "Unsupported backup version."
        }

        val categories = root.requireArray("categories").mapObjects { it.toCategory() }
        val subcategories = root.requireArray("subcategories").mapObjects { it.toSubcategory() }
        val paymentMethods = root.requireArray("paymentMethods").mapObjects { it.toPaymentMethod() }
        val expenses = root.requireArray("expenses").mapObjects { it.toExpense() }
        val refs = root.requireArray("expenseSubcategories").mapObjects { it.toExpenseSubcategoryRef() }
        val preferences = root.optJSONObject("preferences")?.toPreferences() ?: AppPreferences()

        validate(categories, subcategories, paymentMethods, expenses, refs)

        // Room executes this as one transaction. A validation or insert failure rolls back
        // the financial database rather than leaving a partial restore behind.
        dao.replaceAll(categories, subcategories, paymentMethods, expenses, refs)
        settingsRepository.restorePreferences(preferences)

        return RestoreSummary(
            expenseCount = expenses.size,
            categoryCount = categories.size,
            paymentMethodCount = paymentMethods.size,
            sourceFormatVersion = version
        )
    }

    private fun validate(
        categories: List<CategoryEntity>,
        subcategories: List<SubcategoryEntity>,
        paymentMethods: List<PaymentMethodEntity>,
        expenses: List<ExpenseEntity>,
        refs: List<ExpenseSubcategoryCrossRef>
    ) {
        require(categories.isNotEmpty()) { "Backup contains no categories." }
        require(paymentMethods.isNotEmpty()) { "Backup contains no payment methods." }

        requireUnique("category", categories.map { it.id })
        requireUnique("subcategory", subcategories.map { it.id })
        requireUnique("payment method", paymentMethods.map { it.id })
        requireUnique("expense", expenses.map { it.id })
        requireUnique("expense/subcategory relationship", refs.map { "${it.expenseId}|${it.subcategoryId}" })

        categories.forEach { requireUuid(it.id, "category") }
        subcategories.forEach {
            requireUuid(it.id, "subcategory")
            requireUuid(it.categoryId, "subcategory category")
        }
        paymentMethods.forEach {
            if (!it.isSystem || !it.id.startsWith("system-payment-")) requireUuid(it.id, "payment method")
        }
        expenses.forEach { requireUuid(it.id, "expense") }

        val categoryIds = categories.mapTo(HashSet()) { it.id }
        val subcategoryIds = subcategories.mapTo(HashSet()) { it.id }
        val paymentIds = paymentMethods.mapTo(HashSet()) { it.id }
        val expenseIds = expenses.mapTo(HashSet()) { it.id }

        subcategories.forEach { require(it.categoryId in categoryIds) { "Subcategory references a missing category." } }
        expenses.forEach {
            require(it.amountMinor > 0L) { "Backup contains an expense with an invalid amount." }
            require(it.notes.isNotBlank()) { "Backup contains an expense without mandatory notes." }
            require(it.categoryId in categoryIds) { "Expense references a missing category." }
            require(it.paymentMethodId in paymentIds) { "Expense references a missing payment method." }
        }
        refs.forEach {
            require(it.expenseId in expenseIds) { "Backup contains an orphan expense/subcategory relationship." }
            require(it.subcategoryId in subcategoryIds) { "Backup contains an orphan expense/subcategory relationship." }
        }
    }

    private fun requireUnique(label: String, ids: List<String>) {
        require(ids.size == ids.toSet().size) { "Backup contains duplicate $label IDs." }
    }

    private fun requireUuid(value: String, label: String) {
        require(runCatching { UUID.fromString(value) }.isSuccess) { "Backup contains an invalid $label UUID." }
    }

    companion object {
        const val FORMAT_NAME = "ledgerleaf-backup"
        const val FORMAT_VERSION = 1
    }
}

private fun JSONObject.requireArray(name: String): JSONArray =
    optJSONArray(name) ?: throw IllegalArgumentException("Backup is missing $name data.")

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private inline fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray =
    JSONArray().also { array -> forEach { array.put(transform(it)) } }

private fun JSONObject.nullableLong(name: String): Long? = if (isNull(name)) null else getLong(name)
private fun JSONObject.nullableString(name: String): String? = if (isNull(name)) null else getString(name)
private fun JSONObject.putNullable(name: String, value: Any?): JSONObject = put(name, value ?: JSONObject.NULL)

private fun CategoryEntity.toJson() = JSONObject()
    .put("id", id).put("name", name).put("isSystem", isSystem)
    .put("selectionMode", selectionMode).put("sortOrder", sortOrder).put("createdAtEpochMillis", createdAtEpochMillis)

private fun JSONObject.toCategory() = CategoryEntity(
    id = getString("id"), name = getString("name"), isSystem = getBoolean("isSystem"),
    selectionMode = getString("selectionMode"), sortOrder = getInt("sortOrder"), createdAtEpochMillis = getLong("createdAtEpochMillis")
)

private fun SubcategoryEntity.toJson() = JSONObject()
    .put("id", id).put("categoryId", categoryId).put("name", name).put("isSystem", isSystem)
    .put("sortOrder", sortOrder).put("createdAtEpochMillis", createdAtEpochMillis)

private fun JSONObject.toSubcategory() = SubcategoryEntity(
    id = getString("id"), categoryId = getString("categoryId"), name = getString("name"),
    isSystem = getBoolean("isSystem"), sortOrder = getInt("sortOrder"), createdAtEpochMillis = getLong("createdAtEpochMillis")
)

private fun PaymentMethodEntity.toJson() = JSONObject()
    .put("id", id).put("name", name).put("isSystem", isSystem).put("isActive", isActive)
    .put("sortOrder", sortOrder).put("createdAtEpochMillis", createdAtEpochMillis)

private fun JSONObject.toPaymentMethod() = PaymentMethodEntity(
    id = getString("id"), name = getString("name"), isSystem = getBoolean("isSystem"), isActive = getBoolean("isActive"),
    sortOrder = getInt("sortOrder"), createdAtEpochMillis = getLong("createdAtEpochMillis")
)

private fun ExpenseEntity.toJson() = JSONObject()
    .put("id", id).put("amountMinor", amountMinor).put("currencyCode", currencyCode)
    .put("categoryId", categoryId).put("paymentMethodId", paymentMethodId).put("notes", notes)
    .put("occurredAtEpochMillis", occurredAtEpochMillis).put("isFavorite", isFavorite).put("isRecurring", isRecurring)
    .putNullable("recurringFrequency", recurringFrequency).put("createdAtEpochMillis", createdAtEpochMillis)
    .put("updatedAtEpochMillis", updatedAtEpochMillis).putNullable("deletedAtEpochMillis", deletedAtEpochMillis)
    .putNullable("archivedAtEpochMillis", archivedAtEpochMillis)

private fun JSONObject.toExpense() = ExpenseEntity(
    id = getString("id"), amountMinor = getLong("amountMinor"), currencyCode = getString("currencyCode"),
    categoryId = getString("categoryId"), paymentMethodId = getString("paymentMethodId"), notes = getString("notes"),
    occurredAtEpochMillis = getLong("occurredAtEpochMillis"), isFavorite = getBoolean("isFavorite"),
    isRecurring = getBoolean("isRecurring"), recurringFrequency = nullableString("recurringFrequency"),
    createdAtEpochMillis = getLong("createdAtEpochMillis"), updatedAtEpochMillis = getLong("updatedAtEpochMillis"),
    deletedAtEpochMillis = nullableLong("deletedAtEpochMillis"), archivedAtEpochMillis = nullableLong("archivedAtEpochMillis")
)

private fun ExpenseSubcategoryCrossRef.toJson() = JSONObject().put("expenseId", expenseId).put("subcategoryId", subcategoryId)
private fun JSONObject.toExpenseSubcategoryRef() = ExpenseSubcategoryCrossRef(getString("expenseId"), getString("subcategoryId"))

private fun AppPreferences.toJson() = JSONObject()
    .put("themeMode", themeMode.name).put("currencyCode", currencyCode)
    .putNullable("monthlyBudgetMinor", monthlyBudgetMinor).putNullable("monthlyIncomeMinor", monthlyIncomeMinor)
    .put("monthStartDay", monthStartDay).put("pdfIncludeTransactions", pdfIncludeTransactions).put("pdfIncludeNotes", pdfIncludeNotes)
    .put("displayName", displayName)

private fun JSONObject.toPreferences() = AppPreferences(
    themeMode = runCatching { ThemeMode.valueOf(optString("themeMode", ThemeMode.SYSTEM.name)) }.getOrDefault(ThemeMode.SYSTEM),
    currencyCode = optString("currencyCode", "INR").takeIf { it.length == 3 } ?: "INR",
    monthlyBudgetMinor = nullableLong("monthlyBudgetMinor"), monthlyIncomeMinor = nullableLong("monthlyIncomeMinor"),
    monthStartDay = optInt("monthStartDay", 1).coerceIn(1, 28),
    pdfIncludeTransactions = optBoolean("pdfIncludeTransactions", true), pdfIncludeNotes = optBoolean("pdfIncludeNotes", true),
    displayName = optString("displayName", ""), profileImagePath = null
)
