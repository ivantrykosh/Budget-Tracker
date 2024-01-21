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

    /**
     * Provide database
     *
     * @param context Application context
     */
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

    /**
     * Provide transaction dao
     *
     * @param db Application DB
     */
    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase) : TransactionDao {
        return db.transactionDao
    }

    /**
     * Provide account dao
     *
     * @param db Application DB
     */
    @Provides
    @Singleton
    fun provideAccountDao(db: AppDatabase) : AccountDao {
        return db.accountDao
    }

    /**
     * Provide account repository with dao instance
     *
     * @param dao instance of AccountDao
     */
    @Provides
    @Singleton
    fun provideAccountRepository(dao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(dao)
    }

    /**
     * Provide transaction repository with dao instance
     *
     * @param dao instance of TransactionDao
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepositoryImpl(dao)
    }
}