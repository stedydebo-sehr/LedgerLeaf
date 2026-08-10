package com.ledgerleaf.feature.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.CategorySelectionMode
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.model.Subcategory

data class ExpenseEditorInitialValues(
    val amount: String = "",
    val categoryId: String? = null,
    val subcategoryIds: List<String> = emptyList(),
    val paymentMethodId: String? = null,
    val notes: String = "",
    val dateText: String,
    val timeText: String,
    val favorite: Boolean = false,
    val recurring: Boolean = false,
    val recurringFrequency: String = "MONTHLY"
)

data class ExpenseEditorSubmission(
    val amount: String,
    val categoryId: String?,
    val subcategoryIds: List<String>,
    val paymentMethodId: String?,
    val notes: String,
    val dateText: String,
    val timeText: String,
    val favorite: Boolean,
    val recurring: Boolean,
    val recurringFrequency: String?
)

@Composable
fun ExpenseEditorForm(
    initialValues: ExpenseEditorInitialValues,
    categories: List<Category>,
    subcategories: List<Subcategory>,
    paymentMethods: List<PaymentMethod>,
    errorMessage: String?,
    actionLabel: String,
    onCategorySelected: (String) -> Unit,
    onAddCustomCategory: (String) -> Unit,
    onSubmit: (ExpenseEditorSubmission) -> Unit,
    modifier: Modifier = Modifier
) {
    key(initialValues) {
        var amount by remember { mutableStateOf(initialValues.amount) }
        var notes by remember { mutableStateOf(initialValues.notes) }
        var dateText by remember { mutableStateOf(initialValues.dateText) }
        var timeText by remember { mutableStateOf(initialValues.timeText) }
        var customCategory by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf(initialValues.categoryId) }
        var selectedPaymentMethodId by remember { mutableStateOf(initialValues.paymentMethodId) }
        val selectedSubs = remember { mutableStateListOf<String>().apply { addAll(initialValues.subcategoryIds) } }
        var favorite by remember { mutableStateOf(initialValues.favorite) }
        var recurring by remember { mutableStateOf(initialValues.recurring) }
        var frequency by remember { mutableStateOf(initialValues.recurringFrequency) }

        val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

        Column(modifier.imePadding().navigationBarsPadding().verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' } },
                label = { Text("Amount") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SectionTitle("Category")
            categories.forEach { category ->
                SelectionRow(
                    selected = selectedCategoryId == category.id,
                    label = category.name,
                    onClick = {
                        selectedCategoryId = category.id
                        selectedSubs.clear()
                        onCategorySelected(category.id)
                    }
                )
            }

            OutlinedTextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = { Text("New custom category") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )
            Button(
                onClick = {
                    if (customCategory.isNotBlank()) {
                        onAddCustomCategory(customCategory)
                        customCategory = ""
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text("Add custom category") }

            if (subcategories.isNotEmpty()) {
                SectionTitle("Details")
                subcategories.forEach { sub ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCategory?.selectionMode == CategorySelectionMode.SINGLE) {
                            RadioButton(
                                selected = selectedSubs.contains(sub.id),
                                onClick = {
                                    selectedSubs.clear()
                                    selectedSubs.add(sub.id)
                                }
                            )
                        } else {
                            Checkbox(
                                checked = selectedSubs.contains(sub.id),
                                onCheckedChange = { checked ->
                                    if (checked) selectedSubs.add(sub.id) else selectedSubs.remove(sub.id)
                                }
                            )
                        }
                        Text(sub.name)
                    }
                }
            }

            SectionTitle("Payment method")
            paymentMethods.forEach { method ->
                SelectionRow(
                    selected = selectedPaymentMethodId == method.id,
                    label = method.name,
                    onClick = { selectedPaymentMethodId = method.id }
                )
            }

            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it },
                label = { Text("Time (HH:MM)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Detailed notes (required)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            ToggleRow("Favorite", favorite) { favorite = it }
            ToggleRow("Recurring", recurring) { recurring = it }

            if (recurring) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    FilterChip(
                        selected = frequency == "WEEKLY",
                        onClick = { frequency = "WEEKLY" },
                        label = { Text("Weekly") }
                    )
                    FilterChip(
                        selected = frequency == "MONTHLY",
                        onClick = { frequency = "MONTHLY" },
                        label = { Text("Monthly") },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    onSubmit(
                        ExpenseEditorSubmission(
                            amount = amount,
                            categoryId = selectedCategoryId,
                            subcategoryIds = selectedSubs.toList(),
                            paymentMethodId = selectedPaymentMethodId,
                            notes = notes,
                            dateText = dateText,
                            timeText = timeText,
                            favorite = favorite,
                            recurring = recurring,
                            recurringFrequency = if (recurring) frequency else null
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) { Text(actionLabel) }
        }
    }
}

@Composable
private fun SelectionRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
