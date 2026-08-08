package com.ledgerleaf.core.di
import android.content.Context
import androidx.room.Room
import com.ledgerleaf.core.database.LedgerLeafDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideLedgerLeafDatabase(@ApplicationContext context: Context): LedgerLeafDatabase =
        Room.databaseBuilder(context, LedgerLeafDatabase::class.java, LedgerLeafDatabase.DATABASE_NAME).build()
}
