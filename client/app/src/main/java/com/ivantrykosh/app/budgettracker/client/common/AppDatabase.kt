package com.ivantrykosh.app.budgettracker.client.common

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ivantrykosh.app.budgettracker.client.data.dao.AccountDao
import com.ivantrykosh.app.budgettracker.client.data.dao.TransactionDao
import com.ivantrykosh.app.budgettracker.client.domain.model.AccountEntity
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction

/**
 * Database of application
 */
@Database(
    entities = [AccountEntity::class, Transaction::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val accountDao: AccountDao
    abstract val transactionDao: TransactionDao
}