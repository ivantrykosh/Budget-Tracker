package com.ivantrykosh.app.budgettracker.client.domain.repository

import com.ivantrykosh.app.budgettracker.client.domain.model.Transaction
import com.ivantrykosh.app.budgettracker.client.domain.model.SubTransaction
import java.util.Date

/**
 * Transaction repository interface
 */
interface TransactionRepository {

    /**
     * Create transaction with token and transactionDto
     *
     * @param transaction Transaction data
     */
    suspend fun createTransaction(transaction: Transaction)

    /**
     * Get transaction with token and id
     *
     * @param id Transaction ID to get
     */
    suspend fun getTransaction(id: Long): Transaction

    /**
     * Get transactions with token, accountIds, startDate and endDate
     *
     * @param accountIds Account IDs by which transactions are got
     * @param startDate start date to get transactions
     * @param endDate end date to get transactions
     */
    suspend fun getTransactionByAllAccountAndDateBetween(accountIds: List<Long>, startDate: Date, endDate: Date): List<SubTransaction>

    /**
     * Update transaction with token and transactionDto
     *
     * @param transaction Transaction data
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Delete transaction with token and id
     *
     * @param id Transaction ID to delete
     */
    suspend fun deleteTransaction(id: Long)
}