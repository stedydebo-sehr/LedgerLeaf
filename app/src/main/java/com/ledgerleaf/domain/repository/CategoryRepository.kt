package com.ledgerleaf.domain.repository

import com.ledgerleaf.domain.model.Category
import com.ledgerleaf.domain.model.Subcategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeSubcategories(categoryId: String): Flow<List<Subcategory>>
    suspend fun ensureSystemDefaults()
    suspend fun addCustomCategory(name: String): String
}
