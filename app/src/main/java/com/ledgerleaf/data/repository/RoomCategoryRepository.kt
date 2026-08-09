package com.ledgerleaf.data.repository

import com.ledgerleaf.core.utils.UuidGenerator
import com.ledgerleaf.data.local.dao.CategoryDao
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity
import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.CategorySelectionMode
import com.ledgerleaf.domain.model.Subcategory
import com.ledgerleaf.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomCategoryRepository @Inject constructor(private val dao: CategoryDao) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = dao.observeCategories().map { list -> list.map { it.toDomain() } }
    override fun observeSubcategories(categoryId: String): Flow<List<Subcategory>> = dao.observeSubcategories(categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun ensureSystemDefaults() {
        val now = System.currentTimeMillis()
        DefaultCategories.categories.forEachIndexed { index, def ->
            dao.insertCategory(CategoryEntity(def.id, def.name, true, def.mode.name, index, now))
            dao.insertSubcategories(def.subcategories.mapIndexed { subIndex, pair -> SubcategoryEntity(pair.first, def.id, pair.second, true, subIndex, now) })
        }
    }

    override suspend fun addCustomCategory(name: String): String {
        val cleaned = name.trim()
        require(cleaned.isNotEmpty()) { "Category name is required" }
        val id = UuidGenerator.newId()
        dao.insertCategory(CategoryEntity(id, cleaned, false, CategorySelectionMode.NONE.name, 10_000, System.currentTimeMillis()))
        return id
    }

    private fun CategoryEntity.toDomain() = Category(id, name, isSystem, runCatching { CategorySelectionMode.valueOf(selectionMode) }.getOrDefault(CategorySelectionMode.NONE))
    private fun SubcategoryEntity.toDomain() = Subcategory(id, categoryId, name, isSystem)
}

private data class DefaultCategory(val id: String, val name: String, val mode: CategorySelectionMode, val subcategories: List<Pair<String,String>> = emptyList())
private object DefaultCategories {
    val categories = listOf(
        DefaultCategory("11111111-1111-4111-8111-111111111111", "Groceries", CategorySelectionMode.MULTI, listOf(
            "11111111-1111-4111-8111-111111111101" to "Kitchen", "11111111-1111-4111-8111-111111111102" to "Laundry", "11111111-1111-4111-8111-111111111103" to "Home Cleaning / Maintenance", "11111111-1111-4111-8111-111111111104" to "Lights", "11111111-1111-4111-8111-111111111105" to "Vegetable", "11111111-1111-4111-8111-111111111106" to "Dairy", "11111111-1111-4111-8111-111111111107" to "Dry Food / Cereals / Grains", "11111111-1111-4111-8111-111111111108" to "Sauce / Achar", "11111111-1111-4111-8111-111111111109" to "Oils", "11111111-1111-4111-8111-111111111110" to "Eggs / Meat / Chicken / Fish")),
        DefaultCategory("22222222-2222-4222-8222-222222222222", "Food Orders", CategorySelectionMode.NONE),
        DefaultCategory("33333333-3333-4333-8333-333333333333", "Utilities", CategorySelectionMode.SINGLE, listOf(
            "33333333-3333-4333-8333-333333333301" to "Electricity Bill", "33333333-3333-4333-8333-333333333302" to "Gas Bill", "33333333-3333-4333-8333-333333333303" to "Water Bill", "33333333-3333-4333-8333-333333333304" to "Maid Bill", "33333333-3333-4333-8333-333333333305" to "Internet Bill", "33333333-3333-4333-8333-333333333306" to "Phone Bill")),
        DefaultCategory("44444444-4444-4444-8444-444444444444", "Travel", CategorySelectionMode.NONE),
        DefaultCategory("55555555-5555-4555-8555-555555555555", "Pet", CategorySelectionMode.NONE),
        DefaultCategory("66666666-6666-4666-8666-666666666666", "Medical", CategorySelectionMode.MULTI, listOf(
            "66666666-6666-4666-8666-666666666601" to "Doctor Consultation", "66666666-6666-4666-8666-666666666602" to "Medicines", "66666666-6666-4666-8666-666666666603" to "Diagnostic Tests", "66666666-6666-4666-8666-666666666604" to "Hospital", "66666666-6666-4666-8666-666666666605" to "Health Insurance", "66666666-6666-4666-8666-666666666606" to "Dental", "66666666-6666-4666-8666-666666666607" to "Eye Care", "66666666-6666-4666-8666-666666666608" to "Physiotherapy")),
        DefaultCategory("77777777-7777-4777-8777-777777777777", "Debt", CategorySelectionMode.NONE),
        DefaultCategory("88888888-8888-4888-8888-888888888888", "Entertainment", CategorySelectionMode.MULTI, listOf("88888888-8888-4888-8888-888888888801" to "Events / Hangouts")),
        DefaultCategory("99999999-9999-4999-8999-999999999999", "Leisure", CategorySelectionMode.MULTI, listOf("99999999-9999-4999-8999-999999999901" to "Smoking", "99999999-9999-4999-8999-999999999902" to "Alcohol")),
        DefaultCategory("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", "Emergency", CategorySelectionMode.MULTI, listOf("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaa01" to "New Appliances")),
        DefaultCategory("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", "Maintenance", CategorySelectionMode.MULTI, listOf("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbb01" to "Plumbing", "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbb02" to "Electrical Work", "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbb03" to "Garden"))
    )
}
