package com.ledgerleaf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ledgerleaf.data.local.entity.CategoryEntity
import com.ledgerleaf.data.local.entity.SubcategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, name") fun observeCategories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY sortOrder, name") fun observeSubcategories(categoryId: String): Flow<List<SubcategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCategory(category: CategoryEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSubcategories(items: List<SubcategoryEntity>)
}
