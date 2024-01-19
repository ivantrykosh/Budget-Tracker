package com.ivantrykosh.app.budgettracker.client.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import java.util.Date

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransaction(transactionId: Long)

    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun getTransaction(transactionId: Long): Transaction

    @Query("SELECT t.transactionId, a.name, t.category, t.value, t.date FROM transactions t INNER JOIN accounts a ON t.accountId = a.accountId WHERE t.date BETWEEN :startDate AND :endDate AND t.accountId IN (:accountIds)")
    suspend fun getTransactions(startDate: Date, endDate: Date, accountIds: List<Long>): List<SubTransaction>
}