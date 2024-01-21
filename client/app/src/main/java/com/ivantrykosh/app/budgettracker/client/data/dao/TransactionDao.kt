package com.ivantrykosh.app.budgettracker.client.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import java.util.Date

/**
 * Account data access object
 */
@Dao
interface TransactionDao {

    /**
     * Insert transaction with its data to DB.
     *
     * @param transaction Transaction entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: Transaction)

    /**
     * Update transaction in DB.
     *
     * @param transaction Transaction entity to update
     */
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Delete transaction from DB.
     *
     * @param transactionId ID of transaction to delete
     */
    @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransaction(transactionId: Long)

    /**
     * Get transaction from DB.
     *
     * @param transactionId ID of transaction to get
     */
    @Query("SELECT * FROM transactions WHERE transactionId = :transactionId")
    suspend fun getTransaction(transactionId: Long): Transaction

    /**
     * Get specific data of transactions from DB.
     *
     * @param startDate start date to get transaction
     * @param endDate end date to get transaction
     * @param accountIds ID of accounts to get transactions
     */
    @Query("SELECT t.transactionId, a.name, t.category, t.value, t.date FROM transactions t INNER JOIN accounts a ON t.accountId = a.accountId WHERE t.date BETWEEN :startDate AND :endDate AND t.accountId IN (:accountIds)")
    suspend fun getTransactions(startDate: Date, endDate: Date, accountIds: List<Long>): List<SubTransaction>
}