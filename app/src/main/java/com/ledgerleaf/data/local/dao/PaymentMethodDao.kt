package com.ledgerleaf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE isActive = 1 ORDER BY sortOrder, name COLLATE NOCASE")
    fun observeActive(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<PaymentMethodEntity>)

    @Insert
    suspend fun insert(item: PaymentMethodEntity)

    @Query("UPDATE payment_methods SET isActive = :active WHERE id = :id AND isSystem = 0")
    suspend fun setCustomActive(id: String, active: Boolean)
}
