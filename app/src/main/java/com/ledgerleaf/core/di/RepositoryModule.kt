package com.ledgerleaf.core.di

import com.ledgerleaf.data.repository.RoomBackupRepository
import com.ledgerleaf.data.repository.RoomCategoryRepository
import com.ledgerleaf.data.repository.RoomExpenseRepository
import com.ledgerleaf.data.repository.RoomPaymentMethodRepository
import com.ledgerleaf.domain.backup.BackupRepository
import com.ledgerleaf.domain.repository.CategoryRepository
import com.ledgerleaf.domain.repository.ExpenseRepository
import com.ledgerleaf.domain.repository.PaymentMethodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: RoomBackupRepository): BackupRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: RoomCategoryRepository): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(impl: RoomPaymentMethodRepository): PaymentMethodRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: RoomExpenseRepository): ExpenseRepository
}
