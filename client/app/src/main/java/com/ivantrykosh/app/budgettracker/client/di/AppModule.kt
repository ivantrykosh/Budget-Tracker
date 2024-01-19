package com.ivantrykosh.app.budgettracker.client.di

import android.content.Context
import androidx.room.Room
import com.ivantrykosh.app.budgettracker.client.common.AppDatabase
import com.ivantrykosh.app.budgettracker.client.data.dao.AccountDao
import com.ivantrykosh.app.budgettracker.client.data.dao.TransactionDao
import com.ivantrykosh.app.budgettracker.client.data.repository.AccountRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.data.repository.TransactionRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Object with provided APIs and repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budgetracker.db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase) : TransactionDao {
        return db.transactionDao
    }

    @Provides
    @Singleton
    fun provideAccountDao(db: AppDatabase) : AccountDao {
        return db.accountDao
    }

    /**
     * Provide account repository with api instance
     *
     * @param api instance of AccountApi
     */
    @Provides
    @Singleton
    fun provideAccountRepository(dao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(dao)
    }

    /**
     * Provide transaction repository with api instance
     *
     * @param api instance of TransactionApi
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }
}